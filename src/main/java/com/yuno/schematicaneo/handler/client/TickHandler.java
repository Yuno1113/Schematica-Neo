package com.yuno.schematicaneo.handler.client;

import net.minecraft.client.Minecraft;

import com.yuno.schematicaneo.SchematicaNeo;
import com.yuno.schematicaneo.client.printer.SchematicPrinter;
import com.yuno.schematicaneo.client.renderer.RendererSchematicChunk;
import com.yuno.schematicaneo.client.world.SchematicWorld;
import com.yuno.schematicaneo.handler.ConfigurationHandler;
import com.yuno.schematicaneo.proxy.ClientProxy;
import com.yuno.schematicaneo.reference.Reference;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;

public class TickHandler {

    public static final TickHandler INSTANCE = new TickHandler();

    private final Minecraft minecraft = Minecraft.getMinecraft();

    private int ticks = -1;
    private int updateCounter = 0;
    private static final int UPDATE_INTERVAL = 3;

    private TickHandler() {}

    @SubscribeEvent
    public void onClientConnect(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        Reference.logger.info("Scheduling client settings reset.");
        ClientProxy.isPendingReset = true;
    }

    @SubscribeEvent
    public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        Reference.logger.info("Scheduling client settings reset.");
        ClientProxy.isPendingReset = true;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            this.minecraft.mcProfiler.startSection("schematica");
            SchematicWorld schematic = ClientProxy.schematic;
            if (this.minecraft.thePlayer != null && schematic != null && schematic.isRendering) {
                this.minecraft.mcProfiler.startSection("printer");
                SchematicPrinter printer = SchematicPrinter.INSTANCE;
                if (printer.isEnabled() && printer.isPrinting() && this.ticks-- < 0) {
                    this.ticks = ConfigurationHandler.placeDelay;

                    for (int i = 0; i < ConfigurationHandler.placementsPerCycle; i++) {
                        printer.print();
                    }
                }

                this.minecraft.mcProfiler.endStartSection("canUpdate");
                if (++this.updateCounter >= UPDATE_INTERVAL) {
                    this.updateCounter = 0;
                    RendererSchematicChunk.setCanUpdate(true);
                }

                this.minecraft.mcProfiler.endSection();
            }

            if (ClientProxy.isPendingReset) {
                SchematicaNeo.proxy.resetSettings();
                ClientProxy.isPendingReset = false;
            }

            this.minecraft.mcProfiler.endSection();
        }
    }
}
