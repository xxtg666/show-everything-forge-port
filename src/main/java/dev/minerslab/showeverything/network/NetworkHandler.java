package dev.minerslab.showeverything.network;

import dev.minerslab.showeverything.ShowEverythingMod;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public final class NetworkHandler {
    private static final String PROTOCOL = "1.16.5-1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(ShowEverythingMod.MOD_ID, "main"))
            .networkProtocolVersion(() -> PROTOCOL)
            .clientAcceptedVersions(NetworkHandler::acceptVersion)
            .serverAcceptedVersions(NetworkHandler::acceptVersion)
            .simpleChannel();

    private NetworkHandler() {
    }

    public static void register() {
        CHANNEL.registerMessage(0, ShowItemChatMessage.class,
                ShowItemChatMessage::encode,
                ShowItemChatMessage::decode,
                ShowItemChatMessage::handle);
    }

    public static boolean hasClientMod(ServerPlayerEntity player) {
        return CHANNEL.isRemotePresent(player.connection.connection);
    }

    public static void sendTo(ShowItemChatMessage message, ServerPlayerEntity player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    private static boolean acceptVersion(String remoteVersion) {
        return PROTOCOL.equals(remoteVersion)
                || NetworkRegistry.ABSENT.equals(remoteVersion)
                || NetworkRegistry.ACCEPTVANILLA.equals(remoteVersion);
    }
}
