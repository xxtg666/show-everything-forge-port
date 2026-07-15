package dev.minerslab.showeverything.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * 1.7.10 keeps most custom commands OP-only regardless of permission level.
 * These commands operate on a player view/inventory, so allow any player and
 * let the command implementation handle the player-only contract.
 */
abstract class ShowEverythingCommand extends CommandBase {
    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return sender instanceof EntityPlayerMP;
    }
}
