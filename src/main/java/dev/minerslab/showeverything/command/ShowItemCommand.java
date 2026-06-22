package dev.minerslab.showeverything.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.minerslab.showeverything.network.NetworkHandler;
import dev.minerslab.showeverything.network.ShowItemChatMessage;
import dev.minerslab.showeverything.util.ChatComponents;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.HoverEvent;

public class ShowItemCommand extends CommandBase {
    @Override
    public String getName() {
        return "show-item";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("showitem");
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/show-item";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        ItemStack stack = player.getHeldItemMainhand();
        if (stack.isEmpty()) {
            stack = player.getHeldItemOffhand();
        }
        if (stack.isEmpty()) {
            throw new CommandException("You are not holding an item.");
        }

        ITextComponent suffix = new TextComponentString(" ");
        suffix.appendSibling(ChatComponents.labelValue("id ", ChatComponents.registryName(stack.getItem())));
        ITextComponent fullMessage = itemMessage(stack, suffix, false);
        ITextComponent fullLine = ChatComponents.chatLine(player, fullMessage);
        if (ChatComponents.isSafeChatComponent(fullLine)) {
            server.getPlayerList().sendMessage(fullLine);
            return;
        }

        List<String> vanillaFallbackPlayers = new ArrayList<String>();
        boolean oversizedClientPayload = false;
        for (EntityPlayerMP target : server.getPlayerList().getPlayers()) {
            boolean hasClientMod = NetworkHandler.hasClientMod(target);
            if (hasClientMod) {
                ShowItemChatMessage packet = new ShowItemChatMessage(player.getDisplayNameString(), player.getName(), stack, suffix);
                if (packet.isSafePayload()) {
                    NetworkHandler.CHANNEL.sendTo(packet, target);
                } else {
                    target.sendMessage(ChatComponents.chatLine(player, itemMessage(stack, suffix, true)));
                    oversizedClientPayload = true;
                }
            } else {
                target.sendMessage(ChatComponents.chatLine(player, itemMessage(stack, suffix, true)));
                vanillaFallbackPlayers.add(target.getName());
            }
        }

        if (!vanillaFallbackPlayers.isEmpty()) {
            player.sendMessage(missingClientWarning(vanillaFallbackPlayers));
        }
        if (oversizedClientPayload) {
            player.sendMessage(new TextComponentString("This item is too large to send full NBT even to modded clients."));
        }
    }

    private static ITextComponent itemMessage(ItemStack stack, ITextComponent suffix, boolean omitted) {
        ITextComponent message = new TextComponentString("");
        message.appendSibling(omitted ? ChatComponents.itemOmitted(stack, false) : ChatComponents.item(stack));
        message.appendSibling(suffix.createCopy());
        return message;
    }

    private static ITextComponent missingClientWarning(List<String> players) {
        ITextComponent message = new TextComponentString("Some players cannot see this item's full NBT ");
        message.getStyle().setColor(TextFormatting.YELLOW);
        ITextComponent info = new TextComponentString("[i]");
        info.getStyle()
                .setColor(TextFormatting.YELLOW)
                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(joinPlayerNames(players))));
        message.appendSibling(info);
        return message;
    }

    private static String joinPlayerNames(List<String> players) {
        StringBuilder builder = new StringBuilder("Missing client mod:\n");
        for (int i = 0; i < players.size(); i++) {
            if (i > 0) {
                builder.append('\n');
            }
            builder.append(players.get(i));
        }
        return builder.toString();
    }
}
