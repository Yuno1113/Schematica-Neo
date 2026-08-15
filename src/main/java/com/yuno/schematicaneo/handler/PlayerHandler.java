package com.yuno.schematicaneo.handler;

import net.minecraft.entity.player.EntityPlayerMP;

import com.yuno.schematicaneo.network.PacketHandler;
import com.yuno.schematicaneo.network.message.MessageCapabilities;
import com.yuno.schematicaneo.reference.Reference;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;

public class PlayerHandler {

    public static final PlayerHandler INSTANCE = new PlayerHandler();

    private PlayerHandler() {}

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            try {
                PacketHandler.INSTANCE.sendTo(
                    new MessageCapabilities(
                        ConfigurationHandler.printerEnabled,
                        ConfigurationHandler.saveEnabled,
                        ConfigurationHandler.loadEnabled),
                    (EntityPlayerMP) event.player);
            } catch (Exception ex) {
                Reference.logger.error("Failed to send capabilities!", ex);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            DownloadHandler.INSTANCE.transferMap.remove(event.player);
        }
    }
}
