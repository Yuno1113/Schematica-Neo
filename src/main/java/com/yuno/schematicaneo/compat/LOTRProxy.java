package com.yuno.schematicaneo.compat;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

public class LOTRProxy implements ILOTRPresent {

    private static Class<?> lotrModClass;
    private static Object lotrChisel;
    private static Object lotrChiselIthildin;

    static {
        try {
            lotrModClass = Class.forName("lotr.common.LOTRMod");
            lotrChisel = lotrModClass.getField("chisel").get(null);
            lotrChiselIthildin = lotrModClass.getField("chiselIthildin").get(null);
        } catch (Exception e) {
        }
    }

    @Override
    public Boolean isBlackListed(Block block, ItemStack itemStack) {
        if (lotrChisel != null && itemStack.getItem() == lotrChisel) {
            return true;
        }
        if (lotrChiselIthildin != null && itemStack.getItem() == lotrChiselIthildin) {
            return true;
        }
        return false;
    }
}
