package dev.minerslab.showeverything.network;

import dev.minerslab.showeverything.ShowEverythingMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    private static final ResourceLocation CHANNEL_NAME = new ResourceLocation(ShowEverythingMod.MOD_ID, "main");
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            CHANNEL_NAME,
            () -> PROTOCOL_VERSION,
            NetworkRegistry.acceptMissingOr(PROTOCOL_VERSION),
            NetworkRegistry.acceptMissingOr(PROTOCOL_VERSION));

    private NetworkHandler() {
    }

    public static void register() {
        CHANNEL.messageBuilder(ShowItemChatMessage.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ShowItemChatMessage::encode)
                .decoder(ShowItemChatMessage::decode)
                .consumerMainThread(ShowItemChatMessage::handle)
                .add();
    }

    public static boolean hasClientMod(ServerPlayer player) {
        return CHANNEL.isRemotePresent(player.connection.connection);
    }

    public static void sendTo(ShowItemChatMessage message, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
