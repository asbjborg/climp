package com.asbjborg.climp.event;

import com.asbjborg.climp.ClimpConfig;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class ClimpCommandEvents {
    private static final int TREE_LIMIT_MIN = 1;
    private static final int TREE_LIMIT_MAX = 2048;

    private ClimpCommandEvents() {
    }

    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("climp")
                        .then(Commands.literal("config")
                                .then(Commands.literal("show")
                                        .executes(context -> showConfig(context.getSource())))
                                .then(Commands.literal("set")
                                        .requires(source -> source.hasPermission(2))
                                        .then(Commands.literal("commandTreeScanLimit")
                                                .then(Commands.argument("value", IntegerArgumentType.integer(TREE_LIMIT_MIN, TREE_LIMIT_MAX))
                                                        .executes(ClimpCommandEvents::setTreeScanLimit)))
                                        .then(Commands.literal("commandTreeBreakLimit")
                                                .then(Commands.argument("value", IntegerArgumentType.integer(TREE_LIMIT_MIN, TREE_LIMIT_MAX))
                                                        .executes(ClimpCommandEvents::setTreeBreakLimit)))
                                        .then(Commands.literal("commandTreeScanDebugMessages")
                                                .then(Commands.argument("value", BoolArgumentType.bool())
                                                        .executes(ClimpCommandEvents::setTreeScanDebugMessages))))));
    }

    private static int showConfig(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal(
                "Climp config: commandTreeScanLimit=" + ClimpConfig.COMMAND_TREE_SCAN_LIMIT.getAsInt()
                        + ", commandTreeBreakLimit=" + ClimpConfig.COMMAND_TREE_BREAK_LIMIT.getAsInt()
                        + ", commandTreeScanDebugMessages=" + ClimpConfig.COMMAND_TREE_SCAN_DEBUG_MESSAGES.getAsBoolean()),
                false);
        return 1;
    }

    private static int setTreeScanLimit(CommandContext<CommandSourceStack> context) {
        int oldValue = ClimpConfig.COMMAND_TREE_SCAN_LIMIT.getAsInt();
        int newValue = IntegerArgumentType.getInteger(context, "value");
        ClimpConfig.COMMAND_TREE_SCAN_LIMIT.set(newValue);
        context.getSource().sendSuccess(
                () -> Component.literal("Climp config updated: commandTreeScanLimit " + oldValue + " -> " + newValue),
                true);
        return 1;
    }

    private static int setTreeBreakLimit(CommandContext<CommandSourceStack> context) {
        int oldValue = ClimpConfig.COMMAND_TREE_BREAK_LIMIT.getAsInt();
        int newValue = IntegerArgumentType.getInteger(context, "value");
        ClimpConfig.COMMAND_TREE_BREAK_LIMIT.set(newValue);
        context.getSource().sendSuccess(
                () -> Component.literal("Climp config updated: commandTreeBreakLimit " + oldValue + " -> " + newValue),
                true);
        return 1;
    }

    private static int setTreeScanDebugMessages(CommandContext<CommandSourceStack> context) {
        boolean oldValue = ClimpConfig.COMMAND_TREE_SCAN_DEBUG_MESSAGES.getAsBoolean();
        boolean newValue = BoolArgumentType.getBool(context, "value");
        ClimpConfig.COMMAND_TREE_SCAN_DEBUG_MESSAGES.set(newValue);
        context.getSource().sendSuccess(
                () -> Component.literal("Climp config updated: commandTreeScanDebugMessages " + oldValue + " -> " + newValue),
                true);
        return 1;
    }
}
