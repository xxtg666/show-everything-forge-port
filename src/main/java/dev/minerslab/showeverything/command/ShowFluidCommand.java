package dev.minerslab.showeverything.command;

import java.util.Collections;
import java.util.List;

import dev.minerslab.showeverything.util.ChatComponents;
import dev.minerslab.showeverything.util.Raycasts;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;

public class ShowFluidCommand extends CommandBase {
    @Override
    public String getCommandName() {
        return "show-fluid";
    }

    @Override
    public List getCommandAliases() {
        return Collections.singletonList("showfluid");
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/show-fluid [x y z]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        int x;
        int y;
        int z;
        if (args.length == 0) {
            MovingObjectPosition hit = Raycasts.blocks(player, 15.0D, true, false);
            if (hit != null && hit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                x = hit.blockX;
                y = hit.blockY;
                z = hit.blockZ;
            } else {
                x = floor_double(player.posX);
                y = floor_double(player.posY);
                z = floor_double(player.posZ);
            }
        } else if (args.length == 3) {
            x = parseCoordinate(sender, args[0], floor_double(player.posX));
            y = parseCoordinate(sender, args[1], floor_double(player.posY));
            z = parseCoordinate(sender, args[2], floor_double(player.posZ));
        } else {
            throw new WrongUsageException(getCommandUsage(sender));
        }

        World world = player.getEntityWorld();
        if (!world.blockExists(x, y, z)) {
            throw new CommandException("Block is not loaded: %s %s %s", x, y, z);
        }
        Fluid fluid = fluidFromBlock(world.getBlock(x, y, z));
        if (fluid == null) {
            throw new CommandException("No fluid found at %s, %s, %s", x, y, z);
        }

        ItemStack stack = bucketFor(fluid);
        IChatComponent message = new ChatComponentText("");
        message.appendSibling(ChatComponents.item(stack));
        message.appendText(" ");
        message.appendSibling(ChatComponents.labelValue("id", fluidId(fluid)));
        message.appendText(" ");
        message.appendSibling(ChatComponents.position(x, y, z));
        ChatComponents.broadcast(player, message);
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        return null;
    }

    private static Fluid fluidFromBlock(Block block) {
        if (block instanceof IFluidBlock) {
            return ((IFluidBlock) block).getFluid();
        }
        Material material = block.getMaterial();
        if (material == Material.water) {
            return FluidRegistry.WATER;
        }
        if (material == Material.lava) {
            return FluidRegistry.LAVA;
        }
        return null;
    }

    private static ItemStack bucketFor(Fluid fluid) {
        if (fluid == FluidRegistry.WATER) {
            return new ItemStack(Items.water_bucket);
        }
        if (fluid == FluidRegistry.LAVA) {
            return new ItemStack(Items.lava_bucket);
        }
        ItemStack filled = FluidContainerRegistry.fillFluidContainer(
                new FluidStack(fluid, FluidContainerRegistry.BUCKET_VOLUME), new ItemStack(Items.bucket));
        return filled == null ? new ItemStack(Items.bucket) : filled;
    }

    private static String fluidId(Fluid fluid) {
        if (fluid == FluidRegistry.WATER) {
            return "minecraft:water";
        }
        if (fluid == FluidRegistry.LAVA) {
            return "minecraft:lava";
        }
        Block block = fluid.getBlock();
        if (block != null) {
            String id = ChatComponents.registryName(block);
            if (!"unknown".equals(id)) {
                return id;
            }
        }
        String name = FluidRegistry.getFluidName(fluid);
        return name == null ? fluid.getName() : name;
    }

    private static int parseCoordinate(ICommandSender sender, String value, int origin) {
        boolean relative = value.startsWith("~");
        String number = relative ? value.substring(1) : value;
        int parsed = number.length() == 0 ? 0 : parseInt(sender, number);
        return relative ? origin + parsed : parsed;
    }

    private static int floor_double(double value) {
        int integer = (int) value;
        return value < integer ? integer - 1 : integer;
    }
}
