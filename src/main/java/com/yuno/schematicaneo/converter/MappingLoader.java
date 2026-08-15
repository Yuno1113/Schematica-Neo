package com.yuno.schematicaneo.converter;

import com.yuno.schematicaneo.reference.Reference;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

import java.io.*;
import java.util.*;


public class MappingLoader {

    private static final String VANILLA_FILE = "vanilla_blocks_reference";
    private static final String MMM_FILE = "mmm_blocks_reference";

    // Vanilla block state (no properties) → index in vanilla_blocks_reference.txt
    private static final Map<String, Integer> vanillaStateToIndex = new HashMap<>();

    // Index (from vanilla_blocks_reference.txt) → {registry_name, metadata} from mmm_blocks_reference.txt
    private static final Map<Integer, MappingEntry> indexToMMM = new HashMap<>();

    // Reverse map: MMM registry name → corresponding vanilla block state
    private static final Map<String, String> mmmNameToVanillaState = new HashMap<>();

    // Vanilla block state → Chinese name from vanilla_blocks_reference.txt
    private static final Map<String, String> vanillaStateToChineseName = new HashMap<>();

    private static File schematicaneoDir;
    private static boolean loaded = false;

    public static void init(File configDir) {
        if (loaded) return;
        loaded = true;

        schematicaneoDir = new File(configDir, "schematicaneo");
        if (!schematicaneoDir.exists()) schematicaneoDir.mkdirs();

        // 1. Load vanilla_blocks_reference.txt → build state → index map
        loadVanillaReference();
        FMLLog.info("[SchematicaNeo] Loaded %d vanilla block states from reference", vanillaStateToIndex.size());

        // 2. Load mmm_blocks_reference.txt (Parts 1 & 2) → build index → MMM mapping
        loadMMMReference();
        FMLLog.info("[SchematicaNeo] Loaded %d MMM block mappings from reference", indexToMMM.size());

        // 3. Build reverse map: MMM registry name → vanilla block state
        buildReverseMap();


    }

    public static void initPhase2() {
        // Scan the block registry to correct MMM block names
        // (reference file has guessed names like "manametalmod:concrete",
        //  but the actual mod might use "manametalmod:BlockConcrete")
        Map<String, String> unlocToRegistry = new HashMap<>();
        for (Object key : Block.blockRegistry.getKeys()) {
            String fullName = key.toString(); // e.g. "manametalmod:BlockConcrete"
            Block block = (Block) Block.blockRegistry.getObject(key);
            if (block == null) continue;
            String unloc = block.getUnlocalizedName(); // e.g. "tile.concrete"
            if (unloc == null) continue;
            if (unloc.startsWith("tile.")) unloc = unloc.substring(5);
            unlocToRegistry.put(unloc, fullName);
        }
        FMLLog.info("[SchematicaNeo] Scanned %d registered blocks, built %d unlocalized name mappings",
                Block.blockRegistry.getKeys().size(), unlocToRegistry.size());

        int corrected = 0;
        for (Map.Entry<Integer, MappingEntry> e : indexToMMM.entrySet()) {
            MappingEntry entry = e.getValue();
            if (entry.blockName == null || !entry.blockName.contains(":")) continue;
            String[] parts = entry.blockName.split(":", 2);
            String modid = parts[0];
            String name = parts[1];
            Block existing = GameRegistry.findBlock(modid, name);
            if (existing != null) continue; // this name works

            // Try to find the correct registry name via unlocalized name
            String correctedName = unlocToRegistry.get(name);
            if (correctedName != null) {
                e.setValue(new MappingEntry(entry.blockState, correctedName, entry.metadata));
                corrected++;
                FMLLog.info("[SchematicaNeo] Corrected MMM block: %s → %s", entry.blockName, correctedName);
            } else {
                FMLLog.warning("[SchematicaNeo] MMM block not found in registry: %s", entry.blockName);
            }
        }

        if (corrected > 0) {
            FMLLog.info("[SchematicaNeo] Corrected %d MMM block names at runtime", corrected);
        }
        FMLLog.info("[SchematicaNeo] Reference-based mapping ready (%d states → %d indices)",
                vanillaStateToIndex.size(), indexToMMM.size());
    }

    // ── Reference file loading ──

    private static void loadVanillaReference() {
        File file = ensureReferenceFile(VANILLA_FILE);
        if (file == null) return;
        try {
            List<String> lines = readFileLinesUTF8(file);
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("=") || trimmed.startsWith("-")) continue;
                if (!Character.isDigit(trimmed.charAt(0))) continue;

                String[] parts = line.split("\\|");
                if (parts.length < 3) continue;

                String indexStr = parts[0].trim();
                String stateStr = parts[1].trim();
                if (indexStr.isEmpty() || stateStr.isEmpty()) continue;
                if (stateStr.startsWith("minecraft:id_")) continue;

                int bracket = stateStr.indexOf('[');
                if (bracket >= 0) stateStr = stateStr.substring(0, bracket);

                if (parts.length >= 4) {
                    String cn = parts[3].trim();
                    if (!cn.isEmpty()) vanillaStateToChineseName.put(stateStr, cn);
                }

                int index = Integer.parseInt(indexStr);
                vanillaStateToIndex.put(stateStr, index);
            }
            FMLLog.info("[SchematicaNeo] Loaded %d entries from %s", vanillaStateToIndex.size(), file.getName());
        } catch (Exception e) {
            FMLLog.warning("[SchematicaNeo] Failed to load %s: %s", file.getName(), e.getMessage());
            e.printStackTrace();
        }
    }

    private static void loadMMMReference() {
        File file = ensureReferenceFile(MMM_FILE);
        if (file == null) return;
        try {
            List<String> lines = readFileLinesUTF8(file);
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("=") || trimmed.startsWith("-")) continue;
                if (!Character.isDigit(trimmed.charAt(0))) continue;

                String[] parts = line.split("\\|");
                if (parts.length < 3) continue;

                String indexStr = parts[0].trim();
                String registryStr = parts[1].trim();
                if (indexStr.isEmpty() || registryStr.isEmpty()) continue;

                int index = Integer.parseInt(indexStr);
                int meta = 0;
                if (parts.length >= 3) {
                    String metaStr = parts[2].trim();
                    if (!metaStr.isEmpty()) {
                        try {
                            meta = Integer.parseInt(metaStr);
                        } catch (NumberFormatException e) {
                            meta = 0;
                        }
                    }
                }

                indexToMMM.put(index, new MappingEntry(registryStr, registryStr, meta));
            }
            FMLLog.info("[SchematicaNeo] Loaded %d entries from %s", indexToMMM.size(), file.getName());
        } catch (Exception e) {
            FMLLog.warning("[SchematicaNeo] Failed to load %s: %s", file.getName(), e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Ensure the version-specific reference file exists in the config directory.
     * Tries {baseName}-{version}.txt first; if not found, extracts from jar resource.
     * Old version files are never deleted.
     */
    private static File ensureReferenceFile(String baseName) {
        String versionedName = baseName + "-" + Reference.VERSION + ".txt";
        File versionedFile = new File(schematicaneoDir, versionedName);
        if (versionedFile.exists() && versionedFile.length() > 0) return versionedFile;

        // Extract from embedded resource to versioned file
        String resourcePath = "/assets/schematicaneo/" + baseName + ".txt";
        try {
            InputStream in = MappingLoader.class.getResourceAsStream(resourcePath);
            if (in == null) {
                FMLLog.warning("[SchematicaNeo] Embedded resource %s not found in jar", resourcePath);
                return null;
            }
            byte[] data = readAllBytes(in);
            in.close();
            writeBytesToFile(versionedFile, data);
            FMLLog.info("[SchematicaNeo] Extracted %s → %s (%d bytes)", resourcePath, versionedName, data.length);
        } catch (Exception e) {
            FMLLog.warning("[SchematicaNeo] Failed to extract %s: %s", resourcePath, e.getMessage());
            return null;
        }

        return versionedFile;
    }

    private static void buildReverseMap() {
        for (Map.Entry<String, Integer> e : vanillaStateToIndex.entrySet()) {
            String vanillaState = e.getKey();
            int index = e.getValue();
            MappingEntry mmmEntry = indexToMMM.get(index);
            if (mmmEntry != null && mmmEntry.blockName != null) {
                mmmNameToVanillaState.put(mmmEntry.blockName, vanillaState);
            }
        }
        FMLLog.info("[SchematicaNeo] Built reverse map: %d MMM → vanilla mappings", mmmNameToVanillaState.size());
    }

    // ── Mapping lookup ──

    public static MappingEntry findMapping(String blockState) {
        if (blockState == null) return null;

        // Handle numeric ID format (minecraft:id_X:Y) — already a 1.7.10 format
        if (blockState.startsWith("minecraft:id_")) {
            String rest = blockState.substring("minecraft:id_".length());
            int colon = rest.indexOf(':');
            if (colon > 0) {
                try {
                    int id = Integer.parseInt(rest.substring(0, colon));
                    int meta = Integer.parseInt(rest.substring(colon + 1));
                    return new MappingEntry(blockState, id, meta);
                } catch (NumberFormatException e) { }
            }
        }

        // Strip block state properties
        String baseState = blockState;
        int bracket = blockState.indexOf('[');
        if (bracket >= 0) baseState = blockState.substring(0, bracket);

        // 1. Try reverse map: MMM registry name → vanilla block (for converting MMM→vanilla)
        String vanillaState = mmmNameToVanillaState.get(baseState);
        if (vanillaState != null) {
            Block vanillaBlock = Block.getBlockFromName(vanillaState);
            if (vanillaBlock != null) {
                return new MappingEntry(blockState, Block.getIdFromBlock(vanillaBlock), 0);
            }
            // Vanilla block doesn't exist in 1.7.10 (e.g. blackstone) — fall through to keep MMM block
        }

        // 2. Try forward map: vanilla block state → index → MMM mapping (for converting vanilla→MMM)
        Integer index = vanillaStateToIndex.get(baseState);
        if (index != null) {
            MappingEntry mmmEntry = indexToMMM.get(index);
            if (mmmEntry != null) {
                return new MappingEntry(blockState, mmmEntry.blockName, mmmEntry.metadata);
            }
        }

        // 3. Fallback: try direct block lookup by name (works when state is an MMM registry name)
        Block directBlock = Block.getBlockFromName(baseState);
        if (directBlock != null) {
            return new MappingEntry(blockState, Block.getIdFromBlock(directBlock), 0);
        }

        return null;
    }

    public static Block getFallbackBlock() {
        return Blocks.dirt;
    }

    public static int getFallbackMeta() {
        return 0;
    }

    public static String getChineseName(String blockState) {
        if (blockState == null) return null;
        String base = blockState;
        int bracket = blockState.indexOf('[');
        if (bracket >= 0) base = blockState.substring(0, bracket);
        return vanillaStateToChineseName.get(base);
    }

    // ---- Utility helpers ----

    private static byte[] readAllBytes(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = in.read(buf)) >= 0) out.write(buf, 0, n);
        return out.toByteArray();
    }

    private static void writeBytesToFile(File file, byte[] data) throws IOException {
        FileOutputStream out = new FileOutputStream(file);
        try {
            out.write(data);
        } finally {
            out.close();
        }
    }

    private static List<String> readFileLinesUTF8(File file) throws IOException {
        List<String> lines = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } finally {
            reader.close();
        }
        return lines;
    }

}
