package dev.minerslab.showeverything.command;

import java.util.Collections;
import java.util.List;

import dev.minerslab.showeverything.util.ChatComponents;
import dev.minerslab.showeverything.util.Raycasts;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidBlock;

public class ShowFluidCommand extends CommandBase {
    @Override
    public String getName() {
        return "show-fluid";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("showfluid");
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/show-fluid [x y z]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        BlockPos pos;
        if (args.length == 0) {
            RayTraceResult hit = Raycasts.blocks(player, 15.0D, false);
            pos = hit != null && hit.typeOfHit == RayTraceResult.Type.BLOCK ? hit.getBlockPos() : player.getPosition();
        } else if (args.length == 3) {
            pos = parseBlockPos(sender, args, 0, false);
        } else {
            throw new WrongUsageException(getUsage(sender));
        }

        World world = player.getEntityWorld();
        if (!world.isBlockLoaded(pos)) {
            throw new WrongUsageException("Block is not loaded: %s", pos.toString());
        }

        IBlockState state = world.getBlockState(pos);
        Fluid fluid = fluidFromBlock(state.getBlock());
        ItemStack stack = fluid != null ? FluidUtil.getFilledBucket(new FluidStack(fluid, Fluid.BUCKET_VOLUME)) : ItemStack.EMPTY;
        if (stack.isEmpty()) {
            stack = new ItemStack(Items.BUCKET);
        }
        ITextComponent message = new TextComponentString("");
        message.appendSibling(ChatComponents.item(stack));
        message.appendText(" ");
        message.appendSibling(ChatComponents.labelValue("id ", fluid != null ? fluid.getName() : ChatComponents.registryName(state.getBlock())));
        message.appendSibling(ChatComponents.position(pos));
        ChatComponents.broadcast(server, player, message);
    }

    private static Fluid fluidFromBlock(Block block) {
        if (block instanceof IFluidBlock) {
            return ((IFluidBlock) block).getFluid();
        }
        if (block.getDefaultState().getMaterial() == Material.WATER) {
            return FluidRegistry.WATER;
        }
        if (block.getDefaultState().getMaterial() == Material.LAVA) {
            return FluidRegistry.LAVA;
        }
        return null;
    }
}
