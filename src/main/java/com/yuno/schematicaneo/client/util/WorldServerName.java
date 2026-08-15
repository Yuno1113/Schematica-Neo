package com.yuno.schematicaneo.client.util;

import net.minecraft.client.Minecraft;

import cpw.mods.fml.common.FMLCommonHandler;

public class WorldServerName {

    /**
     * Gets the world name if in singleplayer, or the name in server list when in multiplayer
     * 
     * @param mc {@link Minecraft}
     * @return {@link String} world name or server name
     */
    public static String worldServerName(Minecraft mc) {
        String WorldOrServerName;
        if (mc.isSingleplayer()) {
            WorldOrServerName = FMLCommonHandler.instance()
                .getMinecraftServerInstance()
                .getWorldName();
        } else {
            // Gets the server data, only works if you're playing on a server. if you're using direct connect the name
            // will be "Minecraft Server". Crashes if singleplayer
            WorldOrServerName = mc.func_147104_D().serverName;
        }
        return WorldOrServerName;
    }
}
