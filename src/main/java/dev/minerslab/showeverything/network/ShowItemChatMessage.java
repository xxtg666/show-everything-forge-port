package dev.minerslab.showeverything.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public final class ShowItemChatMessage {
    private static final int CUSTOM_PAYLOAD_LIMIT = 1048576;
    private static final int CUSTOM_PAYLOAD_SAFETY_MARGIN = 8192;

    public final String senderName;
    public final String senderCommandName;
    public final ItemStack stack;
    public final Component messageSuffix;

    public ShowItemChatMessage(String senderName, String senderCommandName, ItemStack stack, Component messageSuffix) {
        this.senderName = senderName;
        this.senderCommandName = senderCommandName;
        this.stack = stack.copy();
        this.messageSuffix = messageSuffix.copy();
    }

    public static void encode(ShowItemChatMessage message, FriendlyByteBuf buffer) {
        buffer.writeUtf(message.senderName, 64);
        buffer.writeUtf(message.senderCommandName, 64);
        buffer.writeItem(message.stack);
        buffer.writeComponent(message.messageSuffix);
    }

    public static ShowItemChatMessage decode(FriendlyByteBuf buffer) {
        return new ShowItemChatMessage(buffer.readUtf(64), buffer.readUtf(64), buffer.readItem(), buffer.readComponent());
    }

    public static void handle(ShowItemChatMessage message, Supplier<NetworkEvent.Context> context) {
        try {
            Class<?> bridge = Class.forName("dev.minerslab.showeverything.network.client.ClientMessageBridge");
            bridge.getMethod("handleShowItemChat", ShowItemChatMessage.class).invoke(null, message);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    public boolean isSafePayload() {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            encode(this, buffer);
            return buffer.writerIndex() <= CUSTOM_PAYLOAD_LIMIT - CUSTOM_PAYLOAD_SAFETY_MARGIN;
        } finally {
            buffer.release();
        }
    }
}
