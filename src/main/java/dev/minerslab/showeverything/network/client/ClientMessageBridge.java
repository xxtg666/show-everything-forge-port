package dev.minerslab.showeverything.network.client;

import dev.minerslab.showeverything.network.ShowItemChatMessage;
import dev.minerslab.showeverything.util.ChatComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.event.ClickEvent;

public final class ClientMessageBridge {
    private ClientMessageBridge() {
    }

    public static void handleShowItemChat(ShowItemChatMessage message) {
        ITextComponent parsedName = ITextComponent.Serializer.fromJson(message.senderDisplayNameJson);
        IFormattableTextComponent senderName = (parsedName == null
                ? new StringTextComponent(message.senderCommandName)
                : parsedName.copy()).withStyle(style -> style.withClickEvent(
                        new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                                "/msg " + message.senderCommandName + " ")));
        IFormattableTextComponent line = new StringTextComponent("")
                .append(senderName)
                .append(": ")
                .append(ChatComponents.item(message.stack));
        ITextComponent suffix = ITextComponent.Serializer.fromJson(message.messageSuffixJson);
        if (suffix != null) {
            line.append(suffix);
        }
        Minecraft.getInstance().gui.getChat().addMessage(line);
    }
}
