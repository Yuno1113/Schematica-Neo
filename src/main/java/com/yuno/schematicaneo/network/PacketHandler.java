package com.yuno.schematicaneo.network;

import com.yuno.schematicaneo.network.message.MessageCapabilities;
import com.yuno.schematicaneo.network.message.MessageDownloadBegin;
import com.yuno.schematicaneo.network.message.MessageDownloadBeginAck;
import com.yuno.schematicaneo.network.message.MessageDownloadChunk;
import com.yuno.schematicaneo.network.message.MessageDownloadChunkAck;
import com.yuno.schematicaneo.network.message.MessageDownloadEnd;
import com.yuno.schematicaneo.reference.Reference;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public class PacketHandler {

    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE
        .newSimpleChannel(Reference.MODID.toLowerCase());

    public static void init() {
        INSTANCE.registerMessage(MessageCapabilities.class, MessageCapabilities.class, 0, Side.CLIENT);

        INSTANCE.registerMessage(MessageDownloadBegin.class, MessageDownloadBegin.class, 1, Side.CLIENT);
        INSTANCE.registerMessage(MessageDownloadBeginAck.class, MessageDownloadBeginAck.class, 2, Side.SERVER);
        INSTANCE.registerMessage(MessageDownloadChunk.class, MessageDownloadChunk.class, 3, Side.CLIENT);
        INSTANCE.registerMessage(MessageDownloadChunkAck.class, MessageDownloadChunkAck.class, 4, Side.SERVER);
        INSTANCE.registerMessage(MessageDownloadEnd.class, MessageDownloadEnd.class, 5, Side.CLIENT);
    }
}
