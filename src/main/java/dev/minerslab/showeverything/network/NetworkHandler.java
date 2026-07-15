package dev.minerslab.showeverything.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";

    private NetworkHandler() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION).optional();
        registrar.playToClient(
                ShowItemChatMessage.TYPE,
                ShowItemChatMessage.STREAM_CODEC,
                ShowItemChatMessage::handle);
    }

    public static boolean hasClientMod(ServerPlayer player) {
        return player.connection.hasChannel(ShowItemChatMessage.TYPE);
    }

    public static void sendTo(ShowItemChatMessage message, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, message);
    }
}
