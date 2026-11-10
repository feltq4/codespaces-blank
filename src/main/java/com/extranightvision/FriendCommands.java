package com.extranightvision;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.minecraft.network.chat.Component;

import java.util.List;

/** /friend add <name>, /friend remove <name>, /friend list. Add and remove stay silent. */
public final class FriendCommands {
    private FriendCommands() {}

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            dispatcher.register(ClientCommands.literal("friend")
                .then(ClientCommands.literal("add")
                    .then(ClientCommands.argument("playername", StringArgumentType.word())
                        .executes(ctx -> {
                            Friends.add(StringArgumentType.getString(ctx, "playername"));
                            return 1;
                        })))
                .then(ClientCommands.literal("remove")
                    .then(ClientCommands.argument("playername", StringArgumentType.word())
                        .executes(ctx -> {
                            Friends.remove(StringArgumentType.getString(ctx, "playername"));
                            return 1;
                        })))
                .then(ClientCommands.literal("list")
                    .executes(ctx -> {
                        List<String> names = Friends.list();
                        String text = names.isEmpty()
                                ? "Friends: (none)"
                                : "Friends (" + names.size() + "): " + String.join(", ", names);
                        ctx.getSource().sendFeedback(Component.literal(text));
                        return 1;
                    }))));
    }
}
