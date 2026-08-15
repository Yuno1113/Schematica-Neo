package com.yuno.schematicaneo.converter;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;

public class MappingEntry {

    public final String blockState;
    public final int blockId;
    public final int metadata;
    public final String blockName;

    public MappingEntry(String blockState, int blockId, int metadata) {
        this.blockState = blockState;
        this.blockId = blockId;
        this.metadata = metadata;
        this.blockName = null;
    }

    public MappingEntry(String blockState, String blockName, int metadata) {
        this.blockState = blockState;
        this.blockId = -1;
        this.metadata = metadata;
        this.blockName = blockName;
    }

    public Block resolveBlock() {
        if (blockName != null) {
            String[] parts = blockName.split(":", 2);
            if (parts.length == 2) {
                Block block = GameRegistry.findBlock(parts[0], parts[1]);
                if (block != null) return block;
                FMLLog.warning("[SchematicaNeo] Block not found: " + blockName);
            }
        return null;
    }
    if (blockId >= 0) {
        Block block = Block.getBlockById(blockId);
        if (block != null) return block;
        FMLLog.warning("[SchematicaNeo] Block ID not found: " + blockId);
    }
    return null;
    }
}
