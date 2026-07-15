package dev.minerslab.showeverything.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.minerslab.showeverything.network.NetworkHandler;
import dev.minerslab.showeverything.network.ShowItemChatMessage;
import dev.minerslab.showeverything.util.ChatComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class ShowItemCommand {
    private ShowItemCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(command("show-item"));
        dispatcher.register(command("showitem"));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> command(String name) {
        return Commands.literal(name)
                .executes(context -> execute(context.getSource()));
    }

    private static int execute(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sender = source.getPlayerOrException();
        ItemStack stack = sender.getMainHandItem();
        if (stack.isEmpty()) {
            stack = sender.getOffhandItem();
        }
        if (stack.isEmpty()) {
            throw new com.mojang.brigadier.exceptions.SimpleCommandExceptionType(
                    new TextComponent("You are not holding an item.")
            ).create();
        }

        MutableComponent suffix = new TextComponent(" ")
                .append(ChatComponents.labelValue("id", ChatComponents.registryName(stack.getItem())));
        MutableComponent fullMessage = itemMessage(stack, suffix, false);
        MutableComponent fullLine = ChatComponents.chatLine(sender, fullMessage);
        if (ChatComponents.isSafeChatComponent(fullLine)) {
            for (ServerPlayer target : source.getServer().getPlayerList().getPlayers()) {
                target.sendMessage(fullLine, sender.getUUID());
            }
            return 1;
        }

        List<String> missingClientMod = new ArrayList<>();
        boolean oversizedCustomPayload = false;
        ShowItemChatMessage packet = new ShowItemChatMessage(
                sender.getDisplayName().getString(),
                sender.getGameProfile().getName(),
                stack,
                suffix
        );
        for (ServerPlayer target : source.getServer().getPlayerList().getPlayers()) {
            if (NetworkHandler.canSendTo(target) && packet.isSafePayload()) {
                NetworkHandler.send(target, packet);
            } else {
                target.sendMessage(ChatComponents.chatLine(sender, itemMessage(stack, suffix, true)), sender.getUUID());
                if (!NetworkHandler.canSendTo(target)) {
                    missingClientMod.add(target.getGameProfile().getName());
                } else {
                    oversizedCustomPayload = true;
                }
            }
        }

        if (!missingClientMod.isEmpty()) {
            sender.sendMessage(missingClientWarning(missingClientMod), sender.getUUID());
        }
        if (oversizedCustomPayload) {
            sender.sendMessage(
                    new TextComponent("This item is too large to send safely, even to modded clients.")
                            .withStyle(ChatFormatting.YELLOW),
                    sender.getUUID()
            );
        }
        return 1;
    }

    private static MutableComponent itemMessage(ItemStack stack, Component suffix, boolean omitted) {
        return (omitted ? ChatComponents.itemOmitted(stack) : ChatComponents.item(stack)).append(suffix.copy());
    }

    private static MutableComponent missingClientWarning(List<String> players) {
        String names = "Missing client mod:\n" + String.join("\n", players);
        MutableComponent info = new TextComponent("[i]").withStyle(style -> style
                .withColor(ChatFormatting.YELLOW)
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent(names))));
        return new TextComponent("Some players cannot see this item's full NBT ")
                .withStyle(ChatFormatting.YELLOW)
                .append(info);
    }
}
