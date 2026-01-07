package net.friedunique.eemod.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.friedunique.eemod.core.network.Circuit;
import net.friedunique.eemod.core.network.NetworkManager;
import net.friedunique.eemod.core.test.CircuitTest;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ModCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("ee")
                .then(Commands.literal("circuit_test")
                    .executes(context -> {
                        CircuitTest test = new CircuitTest();
                        test.Test(context.getSource().getLevel());

                        context.getSource().sendSuccess(() ->
                                Component.literal("§eElectrical Grid: §aTESTSTEST"), false);
                        return 1; // Success
                    })
                )
        );

        dispatcher.register(
                Commands.literal("ee")
                        .then(Commands.literal("count")
                                .executes(context -> {


                                    NetworkManager nm = NetworkManager.get(context.getSource().getLevel());
                                    List<Circuit> circuits = nm.getCircuits();


                                    context.getSource().sendSystemMessage(Component.literal("Amount of circuits found: " + circuits.size()));
                                    return 1; // Success
                                })
                        )
        );

        dispatcher.register(
                Commands.literal("ee")
                        .then(Commands.literal("run")
                                .executes(context -> {


                                    NetworkManager nm = NetworkManager.get(context.getSource().getLevel());
                                    nm.refreshAll(context.getSource().getLevel());
                                    nm.run();
                                    context.getSource().sendSystemMessage(Component.literal("Ran"));
                                    return 1; // Success
                                })
                        )
        );
        dispatcher.register(
                Commands.literal("ee")
                        .then(Commands.literal("refresh")
                                .executes(context -> {


                                    NetworkManager nm = NetworkManager.get(context.getSource().getLevel());
                                    nm.refreshAll(context.getSource().getLevel());
                                    return 1; // Success
                                })
                        )
        );
    }
}
