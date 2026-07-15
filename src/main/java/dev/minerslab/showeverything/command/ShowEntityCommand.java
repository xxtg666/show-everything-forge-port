package dev.minerslab.showeverything.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.minerslab.showeverything.util.ChatComponents;
import dev.minerslab.showeverything.util.Raycasts;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.registries.ForgeRegistries;

public final class ShowEntityCommand {
    private ShowEntityCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(command("show-entity"));
        dispatcher.register(command("showentity"));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> command(String name) {
        return Commands.literal(name)
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    Entity target = Raycasts.entity(player, 15.0D);
                    return execute(context.getSource(), target == null ? player : target);
                })
                .then(Commands.argument("selector", EntityArgument.entity())
                        .executes(context -> execute(
                                context.getSource(),
                                EntityArgument.getEntity(context, "selector")
                        )));
    }

    private static int execute(CommandSourceStack source, Entity entity) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sender = source.getPlayerOrException();
        net.minecraft.resources.ResourceLocation typeId = ForgeRegistries.ENTITIES.getKey(entity.getType());
        MutableComponent message = ChatComponents.entity(entity)
                .append(" ")
                .append(ChatComponents.labelValue("id", typeId == null ? "unknown" : typeId.toString()))
                .append(" ")
                .append(ChatComponents.labelValue("uuid", entity.getUUID().toString()))
                .append(" ")
                .append(ChatComponents.position(entity.blockPosition()));
        ChatComponents.broadcast(source.getServer(), sender, message);
        return 1;
    }
}
