package dev.minerslab.showeverything.network;

import dev.minerslab.showeverything.ShowEverythingMod;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.connection.ConnectionType;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ShowItemChatMessage implements CustomPacketPayload {
    private static final int CUSTOM_PAYLOAD_LIMIT = 1048576;
    private static final int CUSTOM_PAYLOAD_SAFETY_MARGIN = 8192;

    public static final Type<ShowItemChatMessage> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ShowEverythingMod.MOD_ID, "show_item_chat"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ShowItemChatMessage> STREAM_CODEC = StreamCodec.of(
            ShowItemChatMessage::encode,
            ShowItemChatMessage::decode);

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

    private static void encode(RegistryFriendlyByteBuf buffer, ShowItemChatMessage message) {
        buffer.writeUtf(message.senderName, 64);
        buffer.writeUtf(message.senderCommandName, 64);
        ItemStack.STREAM_CODEC.encode(buffer, message.stack);
        ComponentSerialization.STREAM_CODEC.encode(buffer, message.messageSuffix);
    }

    private static ShowItemChatMessage decode(RegistryFriendlyByteBuf buffer) {
        return new ShowItemChatMessage(
                buffer.readUtf(64),
                buffer.readUtf(64),
                ItemStack.STREAM_CODEC.decode(buffer),
                ComponentSerialization.STREAM_CODEC.decode(buffer));
    }

    public static void handle(ShowItemChatMessage message, IPayloadContext context) {
        try {
            Class<?> bridge = Class.forName("dev.minerslab.showeverything.network.client.ClientMessageBridge");
            bridge.getMethod("handleShowItemChat", ShowItemChatMessage.class).invoke(null, message);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    public boolean isSafePayload(RegistryAccess registries) {
        ByteBuf rawBuffer = Unpooled.buffer();
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(rawBuffer, registries, ConnectionType.NEOFORGE);
        try {
            STREAM_CODEC.encode(buffer, this);
            return buffer.writerIndex() <= CUSTOM_PAYLOAD_LIMIT - CUSTOM_PAYLOAD_SAFETY_MARGIN;
        } finally {
            buffer.release();
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
