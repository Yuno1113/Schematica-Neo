package com.yuno.schematicaneo.handler.client;

import net.minecraftforge.client.event.ClientChatReceivedEvent;

import com.yuno.schematicaneo.SchematicaNeo;
import com.yuno.schematicaneo.client.printer.SchematicPrinter;
import com.yuno.schematicaneo.reference.Names;
import com.yuno.schematicaneo.reference.Reference;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class ChatEventHandler {

    public static final ChatEventHandler INSTANCE = new ChatEventHandler();

    public int chatLines = 0;

    private ChatEventHandler() {}

    @SubscribeEvent
    public void onClientChatReceivedEvent(ClientChatReceivedEvent event) {
        if (event.message != null && this.chatLines < 20) {
            String message = event.message.getFormattedText();
            if (message != null && !message.isEmpty()) {
                this.chatLines++;
                Reference.logger.debug("Message #{}: {}", this.chatLines, message);
                if (message.contains(Names.SBC.DISABLE_PRINTER)) {
                    Reference.logger.info("Printer is disabled on this server.");
                    SchematicPrinter.INSTANCE.setEnabled(false);
                }
                if (message.contains(Names.SBC.DISABLE_SAVE)) {
                    Reference.logger.info("Saving is disabled on this server.");
                    SchematicaNeo.proxy.isSaveEnabled = false;
                }
                if (message.contains(Names.SBC.DISABLE_LOAD)) {
                    Reference.logger.info("Loading is disabled on this server.");
                    SchematicaNeo.proxy.isLoadEnabled = false;
                }
            }
        }
    }
}
