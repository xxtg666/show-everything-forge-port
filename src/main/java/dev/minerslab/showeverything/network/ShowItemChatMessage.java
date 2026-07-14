package dev.minerslab.showeverything.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public final class ShowItemChatMessage {
    private static final int CUSTOM_PAYLOAD_LIMIT = 1048576;
    private static final int CUSTOM_PAYLOAD_SAFETY_MARGIN = 8192;

    public final String senderDisplayNameJson;
    public final String senderCommandName;
    public final String messageSuffixJson;
    public final ItemStack stack;

    public ShowItemChatMessage(ITextComponent senderDisplayName, String senderCommandName,
                               ItemStack stack, ITextComponent messageSuffix) {
        this(ITextComponent.Serializer.toJson(senderDisplayName), senderCommandName, stack.copy(),
                ITextComponent.Serializer.toJson(messageSuffix));
    }

    private ShowItemChatMessage(String senderDisplayNameJson, String senderCommandName,
                                ItemStack stack, String messageSuffixJson) {
        this.senderDisplayNameJson = senderDisplayNameJson;
        this.senderCommandName = senderCommandName;
        this.stack = stack;
        this.messageSuffixJson = messageSuffixJson;
    }

    public static void encode(ShowItemChatMessage message, PacketBuffer buffer) {
        buffer.writeUtf(message.senderDisplayNameJson, 32767);
        buffer.writeUtf(message.senderCommandName, 64);
        buffer.writeItem(message.stack);
        buffer.writeUtf(message.messageSuffixJson, 32767);
    }

    public static ShowItemChatMessage decode(PacketBuffer buffer) {
        return new ShowItemChatMessage(
                buffer.readUtf(32767),
                buffer.readUtf(64),
                buffer.readItem(),
                buffer.readUtf(32767));
    }

    public static void handle(ShowItemChatMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> {
                    try {
                        Class<?> bridge = Class.forName(
                                "dev.minerslab.showeverything.network.client.ClientMessageBridge");
                        bridge.getMethod("handleShowItemChat", ShowItemChatMessage.class)
                                .invoke(null, message);
                    } catch (ReflectiveOperationException ignored) {
                        // The client bridge is intentionally absent from dedicated-server classpaths.
                    }
                }));
        context.setPacketHandled(true);
    }

    public boolean isSafePayload() {
        return payloadBytes() <= CUSTOM_PAYLOAD_LIMIT - CUSTOM_PAYLOAD_SAFETY_MARGIN;
    }

    private int payloadBytes() {
        ByteBuf bytes = Unpooled.buffer();
        try {
            encode(this, new PacketBuffer(bytes));
            return bytes.writerIndex();
        } catch (RuntimeException exception) {
            return Integer.MAX_VALUE;
        } finally {
            bytes.release();
        }
    }
}
