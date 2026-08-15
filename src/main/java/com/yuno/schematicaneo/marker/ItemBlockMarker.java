package com.yuno.schematicaneo.marker;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockMarker extends ItemBlock {

    public ItemBlockMarker(Block block) {
        super(block);
        setHasSubtypes(true);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        if (field_150939_a instanceof BlockMarker) {
            return "标记方块";
        }
        return super.getItemStackDisplayName(stack);
    }
}
