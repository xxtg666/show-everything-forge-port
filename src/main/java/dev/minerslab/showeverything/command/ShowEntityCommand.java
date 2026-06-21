package dev.minerslab.showeverything.command;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

import dev.minerslab.showeverything.util.ChatComponents;
import dev.minerslab.showeverything.util.Raycasts;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

public class ShowEntityCommand extends CommandBase {
    @Override
    public String getName() {
        return "show-entity";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("showentity");
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/show-entity [selector]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        Entity entity;
        if (args.length == 0) {
            RayTraceResult hit = Raycasts.entity(player, 15.0D);
            entity = hit != null && hit.entityHit != null ? hit.entityHit : player;
        } else if (args.length == 1) {
            entity = getEntity(server, sender, args[0]);
        } else {
            throw new WrongUsageException(getUsage(sender));
        }

        ITextComponent message = new TextComponentString("");
        message.appendSibling(ChatComponents.entity(entity));
        message.appendText(" ");
        message.appendSibling(ChatComponents.labelValue("id ", entityId(entity)));
        message.appendText(" ");
        message.appendSibling(ChatComponents.labelValue("uuid ", entity.getUniqueID().toString()));
        message.appendSibling(ChatComponents.position(entity.getPosition()));
        ChatComponents.broadcast(server, player, message);
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames()) : Collections.emptyList();
    }

    private static String entityId(Entity entity) {
        ResourceLocation id = EntityList.getKey(entity);
        return id == null ? "unknown" : id.toString();
    }
}
