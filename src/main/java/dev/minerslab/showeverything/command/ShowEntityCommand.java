package dev.minerslab.showeverything.command;

import java.util.Collections;
import java.util.List;

import dev.minerslab.showeverything.util.ChatComponents;
import dev.minerslab.showeverything.util.Raycasts;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerSelector;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MovingObjectPosition;

public class ShowEntityCommand extends ShowEverythingCommand {
    @Override
    public String getCommandName() {
        return "show-entity";
    }

    @Override
    public List getCommandAliases() {
        return Collections.singletonList("showentity");
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/show-entity [player]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        Entity entity;
        if (args.length == 0) {
            MovingObjectPosition hit = Raycasts.entity(player, 15.0D);
            entity = hit != null && hit.entityHit != null ? hit.entityHit : player;
        } else if (args.length == 1) {
            entity = getPlayerEntity(sender, args[0]);
        } else {
            throw new WrongUsageException(getCommandUsage(sender));
        }

        IChatComponent message = new ChatComponentText("");
        message.appendSibling(ChatComponents.entity(entity));
        message.appendText(" ");
        message.appendSibling(ChatComponents.labelValue("id", ChatComponents.entityId(entity)));
        message.appendText(" ");
        message.appendSibling(ChatComponents.labelValue("uuid", entity.getUniqueID().toString()));
        message.appendText(" ");
        message.appendSibling(ChatComponents.position(
                floor_double(entity.posX), floor_double(entity.posY), floor_double(entity.posZ)));
        ChatComponents.broadcast(player, message);
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames()) : null;
    }

    private static Entity getPlayerEntity(ICommandSender sender, String name) {
        EntityPlayerMP player = PlayerSelector.hasArguments(name)
                ? PlayerSelector.matchOnePlayer(sender, name)
                : getPlayer(sender, name);
        if (player == null) {
            throw new CommandException("Player not found: %s", name);
        }
        return player;
    }

    private static int floor_double(double value) {
        int integer = (int) value;
        return value < integer ? integer - 1 : integer;
    }
}
