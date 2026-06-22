package dev.minerslab.showeverything.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ShowItemChatMessage implements IMessage {
    private static final int CUSTOM_PAYLOAD_LIMIT = 1048576;
    private static final int CUSTOM_PAYLOAD_SAFETY_MARGIN = 8192;

    public String senderName;
    public String senderCommandName;
    public String messageSuffix;
    public ItemStack stack;

    public ShowItemChatMessage() {
    }

    public ShowItemChatMessage(String senderName, String senderCommandName, ItemStack stack, ITextComponent messageSuffix) {
        this.senderName = senderName;
        this.senderCommandName = senderCommandName;
        this.stack = stack.copy();
        this.messageSuffix = ITextComponent.Serializer.componentToJson(messageSuffix);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer packet = new PacketBuffer(buf);
        senderName = packet.readString(64);
        try {
            senderCommandName = packet.readString(64);
            stack = packet.readItemStack();
            messageSuffix = packet.readString(32767);
        } catch (Exception e) {
            senderCommandName = senderName;
            stack = ItemStack.EMPTY;
            messageSuffix = "{\"text\":\"\"}";
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer packet = new PacketBuffer(buf);
        packet.writeString(senderName);
        packet.writeString(senderCommandName);
        packet.writeItemStack(stack);
        packet.writeString(messageSuffix);
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
        public IMessage onMessage(ShowItemChatMessage message, MessageContext ctx) {
            try {
                Class<?> bridge = Class.forName("dev.minerslab.showeverything.network.client.ClientMessageBridge");
                bridge.getMethod("handleShowItemChat", ShowItemChatMessage.class).invoke(null, message);
            } catch (Exception ignored) {
            }
            return null;
        }
    }
}
