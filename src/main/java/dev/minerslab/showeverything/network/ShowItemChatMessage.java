package dev.minerslab.showeverything.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IChatComponent;

public class ShowItemChatMessage implements IMessage {
    private static final int CUSTOM_PAYLOAD_LIMIT = 1048576;
    private static final int CUSTOM_PAYLOAD_SAFETY_MARGIN = 8192;

    public String senderName;
    public String senderCommandName;
    public String messageSuffix;
    public ItemStack stack;

    public ShowItemChatMessage() {
    }

    public ShowItemChatMessage(
            String senderName, String senderCommandName, ItemStack stack, IChatComponent messageSuffix) {
        this.senderName = senderName;
        this.senderCommandName = senderCommandName;
        this.stack = stack.copy();
        this.messageSuffix = IChatComponent.Serializer.func_150696_a(messageSuffix);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer packet = new PacketBuffer(buf);
        try {
            senderName = packet.readStringFromBuffer(64);
            senderCommandName = packet.readStringFromBuffer(64);
            stack = packet.readItemStackFromBuffer();
            messageSuffix = packet.readStringFromBuffer(32767);
        } catch (Exception exception) {
            senderName = "unknown";
            senderCommandName = "unknown";
            stack = null;
            messageSuffix = "{\"text\":\"\"}";
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer packet = new PacketBuffer(buf);
        try {
            packet.writeStringToBuffer(senderName);
            packet.writeStringToBuffer(senderCommandName);
            packet.writeItemStackToBuffer(stack);
            packet.writeStringToBuffer(messageSuffix);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Unable to encode Show Everything item packet", exception);
        }
    }

    public boolean isSafePayload() {
        return payloadBytes() <= CUSTOM_PAYLOAD_LIMIT - CUSTOM_PAYLOAD_SAFETY_MARGIN;
    }

    public int payloadBytes() {
        ByteBuf buffer = Unpooled.buffer();
        try {
            toBytes(buffer);
            return buffer.writerIndex();
        } finally {
            buffer.release();
        }
    }

    public static class Handler implements IMessageHandler<ShowItemChatMessage, IMessage> {
        @Override
        public IMessage onMessage(ShowItemChatMessage message, MessageContext context) {
            try {
                Class<?> bridge = Class.forName(
                        "dev.minerslab.showeverything.network.client.ClientMessageBridge");
                bridge.getMethod("handleShowItemChat", ShowItemChatMessage.class).invoke(null, message);
            } catch (Exception ignored) {
            }
            return null;
        }
    }
}
