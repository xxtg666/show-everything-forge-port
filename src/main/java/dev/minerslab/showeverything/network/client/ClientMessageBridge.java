package dev.minerslab.showeverything.network.client;

import dev.minerslab.showeverything.network.ShowItemChatMessage;
import dev.minerslab.showeverything.util.ChatComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class ClientMessageBridge {
    private ClientMessageBridge() {
    }

    public static void handleShowItemChat(ShowItemChatMessage message) {
        Minecraft minecraft = Minecraft.getInstance();
        Component suffix = Component.Serializer.fromJson(message.messageSuffix);
        Component line = new TextComponent(message.senderName).withStyle(
                Style.EMPTY.withClickEvent(new ClickEvent(
                        ClickEvent.Action.SUGGEST_COMMAND,
                        "/msg " + message.senderCommandName + " "
                ))
        ).append(": ").append(ChatComponents.item(message.stack));
        if (suffix != null) {
            line = line.copy().append(suffix);
        }
        minecraft.gui.getChat().addMessage(line);
    }
}
