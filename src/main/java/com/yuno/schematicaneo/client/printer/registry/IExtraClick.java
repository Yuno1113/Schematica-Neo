package com.yuno.schematicaneo.client.printer.registry;

import net.minecraft.block.Block;

public interface IExtraClick {

    int getExtraClicks(Block block, int metadata);
}
