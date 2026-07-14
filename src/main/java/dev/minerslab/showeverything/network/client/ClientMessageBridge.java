package dev.minerslab.showeverything.network.client;

import dev.minerslab.showeverything.network.ShowItemChatMessage;
import dev.minerslab.showeverything.util.ChatComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class ClientMessageBridge {
    private ClientMessageBridge() {
    }

    public static void handleShowItemChat(ShowItemChatMessage message) {
        Minecraft minecraft = Minecraft.getInstance();
        MutableComponent line = ChatComponents.chatLine(
                Component.literal(message.senderName), message.senderCommandName, ChatComponents.item(message.stack));
        line.append(message.messageSuffix.copy());
        minecraft.gui.getChat().addMessage(line);
    }
}
