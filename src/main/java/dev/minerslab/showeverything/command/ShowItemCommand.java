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
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.event.HoverEvent;

public class ShowItemCommand extends CommandBase {
    @Override
    public String getCommandName() {
        return "show-item";
    }

    @Override
    public List getCommandAliases() {
        return Collections.singletonList("showitem");
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/show-item";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        ItemStack stack = player.getCurrentEquippedItem();
        if (stack == null) {
            throw new CommandException("You are not holding an item.");
        }

        IChatComponent suffix = new ChatComponentText(" ");
        suffix.appendSibling(ChatComponents.labelValue("id", ChatComponents.registryName(stack.getItem())));
        IChatComponent fullMessage = itemMessage(stack, suffix, false);
        IChatComponent fullLine = ChatComponents.chatLine(player, fullMessage);
        MinecraftServer server = MinecraftServer.getServer();
        if (ChatComponents.isSafeChatComponent(fullLine)) {
            server.getConfigurationManager().sendChatMsg(fullLine);
            return;
        }

        List<String> vanillaFallbackPlayers = new ArrayList<String>();
        boolean oversizedClientPayload = false;
        for (Object object : server.getConfigurationManager().playerEntityList) {
            EntityPlayerMP target = (EntityPlayerMP) object;
            if (NetworkHandler.hasClientMod(target)) {
                ShowItemChatMessage packet = new ShowItemChatMessage(
                        player.getDisplayName(), player.getCommandSenderName(), stack, suffix);
                if (packet.isSafePayload()) {
                    NetworkHandler.CHANNEL.sendTo(packet, target);
                } else {
                    target.addChatMessage(ChatComponents.chatLine(player, itemMessage(stack, suffix, true)));
                    oversizedClientPayload = true;
                }
            } else {
                target.addChatMessage(ChatComponents.chatLine(player, itemMessage(stack, suffix, true)));
                vanillaFallbackPlayers.add(target.getCommandSenderName());
            }
        }

        if (!vanillaFallbackPlayers.isEmpty()) {
            player.addChatMessage(missingClientWarning(vanillaFallbackPlayers));
        }
        if (oversizedClientPayload) {
            player.addChatMessage(new ChatComponentText("This item is too large to send full NBT even to modded clients."));
        }
    }

    private static IChatComponent itemMessage(ItemStack stack, IChatComponent suffix, boolean omitted) {
        IChatComponent message = new ChatComponentText("");
        message.appendSibling(omitted ? ChatComponents.itemOmitted(stack) : ChatComponents.item(stack));
        message.appendSibling(suffix.createCopy());
        return message;
    }

    private static IChatComponent missingClientWarning(List<String> players) {
        IChatComponent message = new ChatComponentText("Some players cannot see this item's full NBT ");
        message.getChatStyle().setColor(EnumChatFormatting.YELLOW);
        IChatComponent info = new ChatComponentText("[i]");
        info.getChatStyle()
                .setColor(EnumChatFormatting.YELLOW)
                .setChatHoverEvent(new HoverEvent(
                        HoverEvent.Action.SHOW_TEXT, new ChatComponentText(joinPlayerNames(players))));
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
