package com.github.xandergos.terraindiffusionmc.world;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.github.xandergos.terraindiffusionmc.platform.PlatformPaths;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Which numbers in a carver's configuration are altitudes, so that a cave mod's carver can be
 * moved into our stretched world without this mod knowing anything about that mod.
 *
 * <h2>The problem this solves</h2>
 * <p>{@link ScaledCarvers} lifts a carver's altitudes so that content authored for a
 * vanilla-height world reaches our terrain. It can do that unaided only for the configuration
 * types the game itself defines, because those are the only ones whose fields it can name. A cave
 * mod that ships its own configuration class -- YUNG's Better Caves keeps its cave and cavern
 * bands in {@code bottom_y}/{@code top_y} pairs, for one -- is otherwise passed through untouched,
 * and its caves then stop wherever the mod's own author put the ceiling: around y=80 for Better
 * Caves, which in a world scaled 6x leaves every mountain above that solid.</p>
 *
 * <h2>The answer: let the number be declared, not guessed</h2>
 * <p>A carver type may declare which JSON keys of its configuration hold absolute altitudes. Any
 * such key found anywhere in that carver's serialized configuration is lifted by
 * {@link ScaledAltitude#worldY}, exactly as a vanilla carver's {@code y} range is. Nothing else in
 * the configuration is touched, and a carver type with no declaration is passed through as before
 * -- so guessing never happens, and a mod that wants its own vertical bounds respected simply says
 * nothing (or lists itself under {@code excluded}).</p>
 *
 * <h2>The file is the source of truth</h2>
 * <p>Rules live in {@code config/terrain-diffusion-mc/carver_altitudes.json}. On the first run
 * the defaults below are written to that file, so every rule in force is one a user can see and
 * edit; from then on only the file is read and these constants take no further part. A file that
 * fails to parse is left exactly as its owner left it, with a loud report, and the defaults are
 * used in memory until it parses again -- a user mid-edit never has their file overwritten.</p>
 *
 * <pre>{@code
 * {
 *   "altitudeKeys": {
 *     "somemod:crystal_cavern": ["min_height", "max_height"]
 *   },
 *   "excluded": [
 *     "othermod:vertical_shaft"
 *   ],
 *   "shellExempt": [
 *     "minecraft:canyon"
 *   ]
 * }
 * }</pre>
 */
public final class CarverAltitudeRules {

    private static final String FILE_NAME = "carver_altitudes.json";
    private static final Gson GSON = new Gson();
    private static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Rules seeded into the config file on first run, and the in-memory stand-in while that file
     * is missing or unreadable. Never merged with the file: once the file exists, it alone
     * decides.
     *
     * <p>Better Caves is the mod this one bundles: its cave and cavern bands carry the
     * {@code bottom_y}/{@code top_y} pair that decides how far up its noise reaches, and both
     * names are unambiguous within its own config.</p>
     */
    private static final Map<String, Set<String>> DEFAULT_ALTITUDE_KEYS = Map.of(
            "bettercaves:better_cave", Set.of("bottom_y", "top_y")
    );

    /**
     * Configured carvers never rewritten. Vanilla's canyon is authored as a rare, near-surface
     * landform (probability 0.01, y 10..67, all of it under td's lowland surface): summit
     * extension reads its top (67) being above sea level as "meant to reach the surface", and
     * density compensation then multiplies its spawn chance by the stretched span -- together
     * turning it into a frequent summit-piercing scar at every world scale. Left as authored it
     * stays buried, carving only the band it was written for.
     *
     * <p>Better Caves' surface cave exists to open cave mouths: authored as a vanilla-style
     * carver at 0.15 per chunk with a band topping out at y=180, but lifted to the world ceiling
     * and density-compensated to nearly every start chunk it sheets whole mountainsides with
     * entrances. Left as authored it stays a rare, lowland cave-maker -- and the surface shell
     * ({@code caves.surface_shell}) is what keeps even its lowland mouths sealed.</p>
     */
    private static final Set<String> DEFAULT_EXCLUDED = Set.of(
            "minecraft:canyon", "bettercaves:surface_cave");

    /**
     * Carvers the surface shell leaves alone. Shelling a landform that was never a problem
     * would only cut it into shape: vanilla's canyon is deliberately buried (see
     * {@link #DEFAULT_EXCLUDED}) and only its floors come near the surface, so the shell would
     * trim away the top half of every canyon. The price of the exemption is the rare opening a
     * canyon still makes on the sea floor; delete the entry to shell it too.
     */
    private static final Set<String> DEFAULT_SHELL_EXEMPT = Set.of("minecraft:canyon");

    private static volatile Rules rules;

    private CarverAltitudeRules() {}

    /** JSON keys of {@code carverTypeId}'s configuration that hold absolute altitudes. */
    public static Set<String> altitudeKeys(String carverTypeId) {
        if (carverTypeId == null) return Set.of();
        Set<String> keys = load().altitudeKeys.get(carverTypeId);
        return keys == null ? Set.of() : keys;
    }

    /** Whether {@code configuredCarverId} opted out of being rewritten at all. */
    public static boolean isExcluded(String configuredCarverId) {
        return configuredCarverId != null && load().excluded.contains(configuredCarverId);
    }

    /** Whether {@code configuredCarverId} opted out of the surface shell. */
    public static boolean isShellExempt(String configuredCarverId) {
        return configuredCarverId != null && load().shellExempt.contains(configuredCarverId);
    }

    /** Re-reads the config file. For tests and for a reload command. */
    public static void reload() {
        rules = null;
    }

    private static Rules load() {
        Rules current = rules;
        if (current != null) return current;
        return loadSynchronized();
    }

    private static synchronized Rules loadSynchronized() {
        if (rules != null) return rules;
        Path file = file();
        boolean present = Files.exists(file);
        Rules loaded = null;
        if (present) {
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                loaded = parse(GSON.fromJson(reader, JsonObject.class));
            } catch (Exception e) {
                // A malformed file is reported loudly and left exactly as its owner left it:
                // overwriting a half-finished manual edit would destroy the user's work. The
                // defaults stand in, in memory only, until the file parses again.
                System.err.println("Failed to read " + FILE_NAME
                        + ", using built-in defaults until it parses again: " + e);
            }
        }
        if (loaded == null) {
            loaded = defaultRules();
            // First run only: a file that already exists, however it got there, is never
            // overwritten -- including one that just failed to parse.
            if (!present) seed(file);
        }
        rules = loaded;
        return rules;
    }

    private static Path file() {
        return PlatformPaths.configDir().resolve("terrain-diffusion-mc").resolve(FILE_NAME);
    }

    /** The rules exactly as the file states them; nothing is merged in. */
    private static Rules parse(JsonObject root) {
        if (root == null) return new Rules(Map.of(), Set.of(), Set.of());
        Map<String, Set<String>> keys = new HashMap<>();
        Set<String> excluded = new HashSet<>();
        Set<String> shellExempt = new HashSet<>();
        JsonElement altitudeKeys = root.get("altitudeKeys");
        if (altitudeKeys != null && altitudeKeys.isJsonObject()) {
            for (Map.Entry<String, JsonElement> entry : altitudeKeys.getAsJsonObject().entrySet()) {
                Set<String> declared = readStrings(entry.getValue());
                // An empty list declared nothing, so it rules nothing out either.
                if (!declared.isEmpty()) keys.put(entry.getKey(), declared);
            }
        }
        excluded.addAll(readStrings(root.get("excluded")));
        shellExempt.addAll(readStrings(root.get("shellExempt")));
        return new Rules(Map.copyOf(keys), Set.copyOf(excluded), Set.copyOf(shellExempt));
    }

    private static Rules defaultRules() {
        return new Rules(Map.copyOf(DEFAULT_ALTITUDE_KEYS), Set.copyOf(DEFAULT_EXCLUDED),
                Set.copyOf(DEFAULT_SHELL_EXEMPT));
    }

    /**
     * Writes the defaults to the config file so a user can edit every rule that applies. Sorted,
     * so that future re-seedings of a changed default diff cleanly. Failure is reported and
     * otherwise ignored: the rules still work from memory for this session.
     */
    private static void seed(Path file) {
        JsonObject root = new JsonObject();
        JsonObject keys = new JsonObject();
        for (Map.Entry<String, Set<String>> entry : new TreeMap<>(DEFAULT_ALTITUDE_KEYS).entrySet()) {
            JsonArray declared = new JsonArray();
            new TreeSet<>(entry.getValue()).forEach(declared::add);
            keys.add(entry.getKey(), declared);
        }
        root.add("altitudeKeys", keys);
        JsonArray excluded = new JsonArray();
        new TreeSet<>(DEFAULT_EXCLUDED).forEach(excluded::add);
        root.add("excluded", excluded);
        JsonArray shellExempt = new JsonArray();
        new TreeSet<>(DEFAULT_SHELL_EXEMPT).forEach(shellExempt::add);
        root.add("shellExempt", shellExempt);
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, PRETTY.toJson(root), StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.err.println("Failed to write " + FILE_NAME + "; defaults stay in memory: " + e);
        }
    }

    private static Set<String> readStrings(JsonElement element) {
        if (element == null || !element.isJsonArray()) return Set.of();
        JsonArray array = element.getAsJsonArray();
        Set<String> values = new LinkedHashSet<>(array.size());
        for (JsonElement item : array) {
            if (item != null && item.isJsonPrimitive()) values.add(item.getAsString());
        }
        return values;
    }

    private record Rules(Map<String, Set<String>> altitudeKeys, Set<String> excluded,
                         Set<String> shellExempt) {}
}
