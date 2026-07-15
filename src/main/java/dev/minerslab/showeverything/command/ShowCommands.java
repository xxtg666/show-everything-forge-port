package dev.minerslab.showeverything.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.minerslab.showeverything.network.NetworkHandler;
import dev.minerslab.showeverything.network.ShowItemChatMessage;
import dev.minerslab.showeverything.util.ChatComponents;
import dev.minerslab.showeverything.util.Raycasts;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public final class ShowCommands {
    private static final SimpleCommandExceptionType EMPTY_HAND = new SimpleCommandExceptionType(
            Component.literal("You are not holding an item."));

    private ShowCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        registerItem(dispatcher, "show-item");
        registerItem(dispatcher, "showitem");
        registerBlock(dispatcher, "show-block");
        registerBlock(dispatcher, "showblock");
        registerFluid(dispatcher, "show-fluid");
        registerFluid(dispatcher, "showfluid");
        registerEntity(dispatcher, "show-entity");
        registerEntity(dispatcher, "showentity");
    }

    private static void registerItem(CommandDispatcher<CommandSourceStack> dispatcher, String name) {
        dispatcher.register(Commands.literal(name).executes(ShowCommands::showItem));
    }

    private static void registerBlock(CommandDispatcher<CommandSourceStack> dispatcher, String name) {
        dispatcher.register(Commands.literal(name)
                .executes(context -> showBlock(context, null))
                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .executes(context -> showBlock(
                                context, BlockPosArgument.getLoadedBlockPos(context, "pos")))));
    }

    private static void registerFluid(CommandDispatcher<CommandSourceStack> dispatcher, String name) {
        dispatcher.register(Commands.literal(name)
                .executes(context -> showFluid(context, null))
                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .executes(context -> showFluid(
                                context, BlockPosArgument.getLoadedBlockPos(context, "pos")))));
    }

    private static void registerEntity(CommandDispatcher<CommandSourceStack> dispatcher, String name) {
        dispatcher.register(Commands.literal(name)
                .executes(context -> showEntity(context, null))
                .then(Commands.argument("entity", EntityArgument.entity())
                        .executes(context -> showEntity(
                                context, EntityArgument.getEntity(context, "entity")))));
    }

    private static int showItem(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            stack = player.getOffhandItem();
        }
        if (stack.isEmpty()) {
            throw EMPTY_HAND.create();
        }

        MutableComponent suffix = Component.literal(" ").append(
                ChatComponents.token("id", ChatComponents.registryName(stack.getItem())));
        Component fullMessage = ChatComponents.itemMessage(stack, suffix, false);
        Component fullLine = ChatComponents.chatLine(player.getDisplayName(), player.getGameProfile().getName(), fullMessage);
        if (ChatComponents.isSafe(fullLine, player.registryAccess())) {
            player.getServer().getPlayerList().broadcastSystemMessage(fullLine, false);
            return 1;
        }

        List<String> vanillaFallbackPlayers = new ArrayList<>();
        boolean oversizedClientPayload = false;
        for (ServerPlayer target : player.getServer().getPlayerList().getPlayers()) {
            if (NetworkHandler.hasClientMod(target)) {
                ShowItemChatMessage packet = new ShowItemChatMessage(
                        player.getDisplayName().getString(), player.getGameProfile().getName(), stack, suffix);
                if (packet.isSafePayload(player.registryAccess())) {
                    NetworkHandler.sendTo(packet, target);
                } else {
                    target.sendSystemMessage(ChatComponents.chatLine(player.getDisplayName(),
                            player.getGameProfile().getName(), ChatComponents.itemMessage(stack, suffix, true)));
                    oversizedClientPayload = true;
                }
            } else {
                target.sendSystemMessage(ChatComponents.chatLine(player.getDisplayName(),
                        player.getGameProfile().getName(), ChatComponents.itemMessage(stack, suffix, true)));
                vanillaFallbackPlayers.add(target.getGameProfile().getName());
            }
        }

        if (!vanillaFallbackPlayers.isEmpty()) {
            player.sendSystemMessage(missingClientWarning(vanillaFallbackPlayers));
        }
        if (oversizedClientPayload) {
            player.sendSystemMessage(Component.literal(
                    "This item is too large to send full item data even to modded clients.").withStyle(ChatFormatting.YELLOW));
        }
        return 1;
    }

    private static int showBlock(CommandContext<CommandSourceStack> context, BlockPos requested) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ServerLevel level = player.serverLevel();
        BlockPos pos = requested;
        if (pos == null) {
            HitResult hit = Raycasts.block(player, false);
            pos = hit.getType() == HitResult.Type.BLOCK ? ((BlockHitResult) hit).getBlockPos() : player.blockPosition();
        }
        if (!level.isLoaded(pos)) {
            context.getSource().sendFailure(Component.literal("Block is not loaded: " + pos.toShortString()));
            return 0;
        }

        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        ItemStack stack = new ItemStack(block.asItem());
        if (stack.isEmpty()) {
            stack = new ItemStack(Blocks.BARRIER);
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof Nameable && ((Nameable) blockEntity).hasCustomName()) {
            stack.set(DataComponents.CUSTOM_NAME, ((Nameable) blockEntity).getDisplayName());
        }
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        MutableComponent message = ChatComponents.item(stack)
                .append(" ").append(ChatComponents.token("id", id == null ? "unknown" : id.toString()))
                .append(" ").append(ChatComponents.position(pos));
        ChatComponents.broadcast(player, message);
        return 1;
    }

    private static int showFluid(CommandContext<CommandSourceStack> context, BlockPos requested) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ServerLevel level = player.serverLevel();
        BlockPos pos = requested;
        if (pos == null) {
            HitResult hit = Raycasts.block(player, true);
            pos = hit.getType() == HitResult.Type.BLOCK ? ((BlockHitResult) hit).getBlockPos() : player.blockPosition();
        }
        FluidState state = level.getFluidState(pos);
        if (state.isEmpty()) {
            context.getSource().sendFailure(Component.literal("No fluid found at " + pos.toShortString()));
            return 0;
        }
        Fluid fluid = state.getType();
        ItemStack stack = new ItemStack(fluid.getBucket());
        if (stack.isEmpty()) {
            stack = new ItemStack(Items.BUCKET);
        }
        ResourceLocation id = BuiltInRegistries.FLUID.getKey(fluid);
        MutableComponent message = ChatComponents.item(stack)
                .append(" ").append(ChatComponents.token("id", id == null ? "unknown" : id.toString()))
                .append(" ").append(ChatComponents.position(pos));
        ChatComponents.broadcast(player, message);
        return 1;
    }

    private static int showEntity(CommandContext<CommandSourceStack> context, Entity selected) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Entity entity = selected;
        if (entity == null) {
            EntityHitResult hit = Raycasts.entity(player);
            entity = hit == null ? player : hit.getEntity();
        }
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        HoverEvent.EntityTooltipInfo tooltip = new HoverEvent.EntityTooltipInfo(
                entity.getType(), entity.getUUID(), entity.getDisplayName());
        MutableComponent name = entity.getDisplayName().copy().withStyle(style -> style
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ENTITY, tooltip)));
        MutableComponent message = name
                .append(" ").append(ChatComponents.token("id", id == null ? "unknown" : id.toString()))
                .append(" ").append(ChatComponents.token("uuid", entity.getUUID().toString()))
                .append(" ").append(ChatComponents.position(entity.blockPosition()));
        ChatComponents.broadcast(player, message);
        return 1;
    }

    private static Component missingClientWarning(List<String> players) {
        String names = String.join("\n", players);
        Component hover = Component.literal("Missing client mod:\n" + names);
        return Component.literal("Some players cannot see this item's full item data ").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal("[i]").withStyle(style -> style.withColor(ChatFormatting.YELLOW)
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover))));
    }
}
