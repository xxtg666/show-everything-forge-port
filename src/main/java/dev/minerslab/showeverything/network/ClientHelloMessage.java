package dev.minerslab.showeverything.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;

/** Small client-to-server capability handshake; vanilla clients never send it. */
public class ClientHelloMessage implements IMessage {
    private String version;

    public ClientHelloMessage() {
    }

    public ClientHelloMessage(String version) {
        this.version = version;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int length = buf.readInt();
        if (length < 0 || length > 64 || length > buf.readableBytes()) {
            version = "";
            return;
        }
        byte[] bytes = new byte[length];
        buf.readBytes(bytes);
        version = new String(bytes, java.nio.charset.Charset.forName("UTF-8"));
    }

    @Override
    public void toBytes(ByteBuf buf) {
        byte[] bytes = version == null ? new byte[0] : version.getBytes(java.nio.charset.Charset.forName("UTF-8"));
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
    }

    public static class Handler implements IMessageHandler<ClientHelloMessage, IMessage> {
        @Override
        public IMessage onMessage(ClientHelloMessage message, MessageContext context) {
            if (context.getServerHandler() != null) {
                EntityPlayerMP player = context.getServerHandler().playerEntity;
                NetworkHandler.markClientMod(player,
                        dev.minerslab.showeverything.ShowEverythingMod.VERSION.equals(message.version));
            }
            return null;
        }
    }
}
