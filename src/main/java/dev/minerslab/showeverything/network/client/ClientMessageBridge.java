package dev.minerslab.showeverything.network.client;

import dev.minerslab.showeverything.network.ShowItemChatMessage;
import dev.minerslab.showeverything.util.ChatComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.ClickEvent;

public final class ClientMessageBridge {
    private ClientMessageBridge() {
    }

    public static void handleShowItemChat(final ShowItemChatMessage message) {
        Minecraft.getMinecraft().addScheduledTask(new Runnable() {
            @Override
            public void run() {
                ITextComponent line = new TextComponentString("");
                ITextComponent senderName = new TextComponentString(message.senderName);
                senderName.setStyle(new Style().setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + message.senderCommandName + " ")));
                line.appendSibling(senderName);
                line.appendText(": ");
                line.appendSibling(ChatComponents.item(message.stack));
                try {
                    line.appendSibling(ITextComponent.Serializer.jsonToComponent(message.messageSuffix));
                } catch (Exception ignored) {
                }
                Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(line);
            }
        });
    }
}
