package dev.minerslab.showeverything.network;

import dev.minerslab.showeverything.ShowEverythingMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public final class NetworkHandler {
    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(ShowEverythingMod.MOD_ID, "main"))
            .networkProtocolVersion(() -> PROTOCOL)
            .clientAcceptedVersions(NetworkRegistry.acceptMissingOr(PROTOCOL))
            .serverAcceptedVersions(NetworkRegistry.acceptMissingOr(PROTOCOL))
            .simpleChannel();

    private NetworkHandler() {
    }

    public static void register() {
        CHANNEL.registerMessage(
                0,
                ShowItemChatMessage.class,
                ShowItemChatMessage::encode,
                ShowItemChatMessage::decode,
                ShowItemChatMessage::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
    }

    public static boolean canSendTo(ServerPlayer player) {
        return CHANNEL.isRemotePresent(player.connection.connection);
    }

    public static void send(ServerPlayer player, ShowItemChatMessage message) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
