package dev.minerslab.showeverything.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.minerslab.showeverything.network.NetworkHandler;
import dev.minerslab.showeverything.network.ShowItemChatMessage;
import dev.minerslab.showeverything.util.ChatComponents;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.HoverEvent;

import java.util.ArrayList;
import java.util.List;

public final class ShowItemCommand {
    private static final SimpleCommandExceptionType EMPTY_HAND = new SimpleCommandExceptionType(
            new StringTextComponent("You are not holding an item."));

    private ShowItemCommand() {
    }

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        // Register aliases as complete command trees. Redirect-only roots can be
        // synchronized without an executable no-argument child on 1.16 clients.
        dispatcher.register(command("show-item"));
        dispatcher.register(command("showitem"));
    }

    private static LiteralArgumentBuilder<CommandSource> command(String name) {
        return Commands.literal(name).executes(context -> execute(context.getSource()));
    }

    private static int execute(CommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrException();
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            stack = player.getOffhandItem();
        }
        if (stack.isEmpty()) {
            throw EMPTY_HAND.create();
        }

        IFormattableTextComponent suffix = new StringTextComponent(" ")
                .append(ChatComponents.labelValue("id", ChatComponents.registryName(stack.getItem())));
        ITextComponent fullMessage = itemMessage(stack, suffix, false);
        ITextComponent fullLine = ChatComponents.chatLine(player, fullMessage);
        if (ChatComponents.isSafeChatComponent(fullLine)) {
            ChatComponents.broadcastLine(player.getServer(), player, fullLine);
            return 1;
        }

        List<String> vanillaFallbackPlayers = new ArrayList<>();
        boolean oversizedClientPayload = false;
        ShowItemChatMessage packet = new ShowItemChatMessage(
                player.getDisplayName(), player.getGameProfile().getName(), stack, suffix);

        for (ServerPlayerEntity target : player.getServer().getPlayerList().getPlayers()) {
            if (NetworkHandler.hasClientMod(target) && packet.isSafePayload()) {
                NetworkHandler.sendTo(packet, target);
            } else {
                target.sendMessage(ChatComponents.chatLine(player, itemMessage(stack, suffix, true)), player.getUUID());
                if (NetworkHandler.hasClientMod(target)) {
                    oversizedClientPayload = true;
                } else {
                    vanillaFallbackPlayers.add(target.getGameProfile().getName());
                }
            }
        }

        if (!vanillaFallbackPlayers.isEmpty()) {
            player.sendMessage(missingClientWarning(vanillaFallbackPlayers), player.getUUID());
        }
        if (oversizedClientPayload) {
            player.sendMessage(new StringTextComponent(
                    "This item is too large to send full NBT even to modded clients."), player.getUUID());
        }
        return 1;
    }

    private static ITextComponent itemMessage(ItemStack stack, ITextComponent suffix, boolean omitted) {
        return new StringTextComponent("")
                .append(omitted ? ChatComponents.itemOmitted(stack) : ChatComponents.item(stack))
                .append(suffix.copy());
    }

    private static ITextComponent missingClientWarning(List<String> players) {
        IFormattableTextComponent message = new StringTextComponent(
                "Some players cannot see this item's full NBT ").withStyle(TextFormatting.YELLOW);
        IFormattableTextComponent info = new StringTextComponent("[i]").withStyle(style -> style
                .withColor(TextFormatting.YELLOW)
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new StringTextComponent(joinPlayerNames(players)))));
        return message.append(info);
    }

    private static String joinPlayerNames(List<String> players) {
        return "Missing client mod:\n" + String.join("\n", players);
    }
}
