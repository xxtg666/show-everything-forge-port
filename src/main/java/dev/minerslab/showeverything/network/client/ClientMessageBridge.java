package dev.minerslab.showeverything.network.client;

import dev.minerslab.showeverything.network.ShowItemChatMessage;
import dev.minerslab.showeverything.util.ChatComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;
import net.minecraft.event.ClickEvent;

public final class ClientMessageBridge {
    private ClientMessageBridge() {
    }

    public static void handleShowItemChat(ShowItemChatMessage message) {
        if (message.stack == null) {
            return;
        }
        IChatComponent line = new ChatComponentText("");
        IChatComponent senderName = new ChatComponentText(message.senderName);
        senderName.setChatStyle(new ChatStyle().setChatClickEvent(new ClickEvent(
                ClickEvent.Action.SUGGEST_COMMAND, "/msg " + message.senderCommandName + " ")));
        line.appendSibling(senderName);
        line.appendText(": ");
        line.appendSibling(ChatComponents.item(message.stack));
        try {
            line.appendSibling(IChatComponent.Serializer.func_150699_a(message.messageSuffix));
        } catch (Exception ignored) {
        }
        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(line);
    }
}
