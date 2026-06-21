package dev.minerslab.showeverything.command;

import java.util.Collections;
import java.util.List;

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

        ITextComponent message = new TextComponentString("");
        message.appendSibling(ChatComponents.item(stack));
        message.appendText(" ");
        message.appendSibling(ChatComponents.labelValue("id ", ChatComponents.registryName(stack.getItem())));
        ChatComponents.broadcast(server, player, message);
    }
}
