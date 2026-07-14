package dev.minerslab.showeverything.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class ShowItemChatMessage {
    private static final int MAX_PAYLOAD_BYTES = 900 * 1024;

    public final String senderName;
    public final String senderCommandName;
    public final String messageSuffix;
    public final ItemStack stack;

    public ShowItemChatMessage(String senderName, String senderCommandName, ItemStack stack, Component suffix) {
        this(senderName, senderCommandName, stack.copy(), Component.Serializer.toJson(suffix));
    }

    private ShowItemChatMessage(String senderName, String senderCommandName, ItemStack stack, String messageSuffix) {
        this.senderName = senderName;
        this.senderCommandName = senderCommandName;
        this.stack = stack;
        this.messageSuffix = messageSuffix;
    }

    public static void encode(ShowItemChatMessage message, FriendlyByteBuf buffer) {
        buffer.writeUtf(message.senderName, 64);
        buffer.writeUtf(message.senderCommandName, 64);
        buffer.writeItem(message.stack);
        buffer.writeUtf(message.messageSuffix, 32767);
    }

    public static ShowItemChatMessage decode(FriendlyByteBuf buffer) {
        String senderName = buffer.readUtf(64);
        String senderCommandName = buffer.readUtf(64);
        ItemStack stack = buffer.readItem();
        String suffix = buffer.readUtf(32767);
        return new ShowItemChatMessage(senderName, senderCommandName, stack, suffix);
    }

    public static void handle(ShowItemChatMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> invokeClientHandler(message)
        ));
        context.setPacketHandled(true);
    }

    private static void invokeClientHandler(ShowItemChatMessage message) {
        try {
            Class<?> bridge = Class.forName("dev.minerslab.showeverything.network.client.ClientMessageBridge");
            bridge.getMethod("handleShowItemChat", ShowItemChatMessage.class).invoke(null, message);
        } catch (ReflectiveOperationException exception) {
            // A dedicated server never has the client bridge; the packet is client-only.
        }
    }

    public boolean isSafePayload() {
        ByteBuf bytes = Unpooled.buffer(256, MAX_PAYLOAD_BYTES);
        try {
            encode(this, new FriendlyByteBuf(bytes));
            return bytes.readableBytes() <= MAX_PAYLOAD_BYTES;
        } catch (RuntimeException exception) {
            return false;
        } finally {
            bytes.release();
        }
    }
}
