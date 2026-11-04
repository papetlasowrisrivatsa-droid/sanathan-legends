package com.sanathanlegends;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

public final class SanathanLegends implements ModInitializer {
    private static final int GOAL = 100;
    private static final List<String> DEITIES =
            List.of("shiva", "vishnu", "ganesha", "hanuman", "durga", "surya");
    private static final Map<String, Group> GROUPS = new LinkedHashMap<>();
    private static final Map<UUID, String> PLAYER_GROUP = new HashMap<>();
    private static net.minecraft.server.MinecraftServer server;

    @Override
    public void onInitialize() {
        DEITIES.forEach(d -> GROUPS.put(d, new Group()));
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(Commands.literal("sl")
                .then(Commands.literal("deities").executes(ctx -> {
                    ctx.getSource().sendSuccess(() -> Component.literal(
                        "Deities: Shiva, Vishnu, Ganesha, Hanuman, Durga, Surya"), false);
                    return 1;
                }))
                .then(Commands.literal("join")
                    .then(Commands.argument("deity", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            DEITIES.forEach(builder::suggest);
                            return builder.buildFuture();
                        })
                        .executes(ctx -> {
                            server = ctx.getSource().getServer();
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            String deity = StringArgumentType.getString(ctx, "deity").toLowerCase(Locale.ROOT);
                            if (!GROUPS.containsKey(deity)) {
                                ctx.getSource().sendFailure(Component.literal("Unknown deity. Use /sl deities."));
                                return 0;
                            }
                            String old = PLAYER_GROUP.put(player.getUUID(), deity);
                            if (old != null) GROUPS.get(old).members.remove(player.getUUID());
                            Group group = GROUPS.get(deity);
                            group.members.add(player.getUUID());
                            broadcast(group, player.getName().getString() + " joined " + deity
                                    + "'s shared worship. Prayer: " + group.progress + "/" + GOAL);
                            return 1;
                        })))
                .then(Commands.literal("offer").executes(ctx -> {
                    server = ctx.getSource().getServer();
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    String deity = PLAYER_GROUP.get(player.getUUID());
                    if (deity == null) {
                        ctx.getSource().sendFailure(Component.literal("Join a deity first: /sl join <deity>"));
                        return 0;
                    }
                    Group group = GROUPS.get(deity);
                    group.progress = Math.min(GOAL, group.progress + 10);
                    broadcast(group, player.getName().getString()
                            + " contributed to the group. Shared prayer: " + group.progress + "/" + GOAL);
                    if (group.progress >= GOAL) {
                        group.blessings++;
                        broadcast(group, "Blessing granted to every member of " + deity
                                + "'s worship group! Total blessings: " + group.blessings);
                        group.progress = 0;
                    }
                    return 1;
                }))
                .then(Commands.literal("status").executes(ctx -> {
                    for (String deity : DEITIES) {
                        Group g = GROUPS.get(deity);
                        ctx.getSource().sendSuccess(() -> Component.literal(
                                deity + ": " + g.progress + "/" + GOAL + " | members: "
                                        + g.members.size() + " | blessings: " + g.blessings), false);
                    }
                    return 1;
                }))
                .then(Commands.literal("leave").executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    String deity = PLAYER_GROUP.remove(player.getUUID());
                    if (deity == null) {
                        ctx.getSource().sendFailure(Component.literal("You are not in a worship group."));
                        return 0;
                    }
                    GROUPS.get(deity).members.remove(player.getUUID());
                    player.sendSystemMessage(Component.literal("You left " + deity + "'s worship group."));
                    return 1;
                }))
                .then(Commands.literal("help").executes(ctx -> {
                    ctx.getSource().sendSuccess(() -> Component.literal(
                        "SANATHAN LEGENDS: /sl deities, /sl join <deity>, /sl offer, /sl status, /sl leave. "
                        + "Each deity has ONE shared prayer total for all group members."), false);
                    return 1;
                }))
            )
        );
        System.out.println("[SANATHAN LEGENDS] Shared worship prototype loaded.");
    }

    private static void broadcast(Group group, String message) {
        if (server == null) return;
        for (UUID id : group.members) {
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player != null) player.sendSystemMessage(Component.literal(message));
        }
    }

    private static final class Group {
        final Set<UUID> members = new LinkedHashSet<>();
        int progress;
        int blessings;
    }
}
