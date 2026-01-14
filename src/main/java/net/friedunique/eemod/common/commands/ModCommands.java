package net.friedunique.eemod.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.friedunique.eemod.common.ModTags;
import net.friedunique.eemod.core.network.Circuit;
import net.friedunique.eemod.core.network.NetworkManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.List;

public class ModCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {


        dispatcher.register(
                Commands.literal("ee")
                        .then(Commands.literal("circuit")
                                .then(Commands.literal("run") // You need to attach .executes() directly to this
                                        .executes(context -> {
                                            NetworkManager nm = NetworkManager.get(context.getSource().getLevel());
                                            nm.refreshAll(context.getSource().getLevel());
                                            nm.run();
                                            context.getSource().sendSystemMessage(Component.literal("Ran"));
                                            return 1;
                                        })
                                ) // Close 'run'
                                .then(Commands.literal("count")
                                        .executes(context -> {
                                            NetworkManager nm = NetworkManager.get(context.getSource().getLevel());
                                            List<Circuit> circuits = nm.getCircuits();
                                            context.getSource().sendSystemMessage(Component.literal("Amount of circuits found: " + circuits.size()));
                                            return 1;
                                        })
                                ) // Close 'count'
                        ) // Close 'circuit'
                        .then(Commands.literal("voltage")
                                .executes(context -> {
                                    if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
                                        context.getSource().sendFailure(Component.literal("Only players can use this command!"));
                                        return 0;
                                    }

                                    BlockHitResult raycastHit = getLookedAtBlock(player, 20.0D);

                                    // CRITICAL FIX: Check if null BEFORE using it to get BlockState
                                    if (raycastHit == null || raycastHit.getType() == HitResult.Type.MISS) {
                                        context.getSource().sendFailure(Component.literal("Not looking at a block!"));
                                        return 0;
                                    }

                                    BlockPos pos = raycastHit.getBlockPos();
                                    BlockState blockState = context.getSource().getLevel().getBlockState(pos);

                                    // Check the tag
                                    if (!blockState.is(ModTags.CONDUCTIVE_BLOCKS)) {
                                        context.getSource().sendFailure(Component.literal("That block is not conductive!"));
                                        return 0;
                                    }

                                    NetworkManager nm = NetworkManager.get(context.getSource().getLevel());
                                    double voltage = nm.getVoltageAt(pos);
                                    context.getSource().sendSystemMessage(Component.literal("Voltage: " + String.format("%.2f", voltage) + "V"));
//                                    context.getSource().sendSystemMessage(Component.literal(Double.toString(nm.getPosToNode().get(pos).connectedEdges.get(0).simulatedEdgeVoltage)));
//                                    context.getSource().sendSystemMessage(Component.literal(Double.toString(nm.getPosToNode().get(pos).connectedEdges.get(1).simulatedEdgeVoltage)));

                                    return 1;
                                })
                        ) // Close 'current'
                        .then(Commands.literal("current")
                                .executes(context -> {
                                    if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
                                        context.getSource().sendFailure(Component.literal("Only players can use this command!"));
                                        return 0;
                                    }

                                    BlockHitResult raycastHit = getLookedAtBlock(player, 20.0D);

                                    // CRITICAL FIX: Check if null BEFORE using it to get BlockState
                                    if (raycastHit == null || raycastHit.getType() == HitResult.Type.MISS) {
                                        context.getSource().sendFailure(Component.literal("Not looking at a block!"));
                                        return 0;
                                    }

                                    BlockPos pos = raycastHit.getBlockPos();
                                    BlockState blockState = context.getSource().getLevel().getBlockState(pos);

                                    // Check the tag
                                    if (!blockState.is(ModTags.CONDUCTIVE_BLOCKS)) {
                                        context.getSource().sendFailure(Component.literal("That block is not conductive!"));
                                        return 0;
                                    }

                                    NetworkManager nm = NetworkManager.get(context.getSource().getLevel());
                                    double current = nm.getCurrentAt(pos);

                                    context.getSource().sendSystemMessage(Component.literal("Current: " + String.format("%.2f", current) + "A"));


                                    return 1;
                                })
                        ) // Close 'current'
        );

    }

    private static BlockHitResult getLookedAtBlock(ServerPlayer player, double distance) {
        // We project a line from the eye position in the direction the player is looking
        HitResult hit = player.pick(distance, 0.0F, false);

        if (hit.getType() == HitResult.Type.BLOCK) {
            return ((BlockHitResult) hit);
        }
        return null;
    }
}
