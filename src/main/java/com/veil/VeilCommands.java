package com.veil;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Veil.ID)
public final class VeilCommands {
	private VeilCommands() {}

	@SubscribeEvent
	public static void register(RegisterCommandsEvent e) {
		e.getDispatcher().register(Commands.literal("veil")
				.requires(src -> src.hasPermission(2))
				.then(Commands.literal("dread")
						.then(Commands.argument("v", FloatArgumentType.floatArg(0, 100))
								.executes(c -> {
									ServerPlayer p = c.getSource().getPlayerOrException();
									Director.set(p, FloatArgumentType.getFloat(c, "v"));
									return 1;
								})))
				.then(Commands.literal("fire")
						.then(Commands.argument("e", StringArgumentType.word())
								.executes(c -> {
									ServerPlayer p = c.getSource().getPlayerOrException();
									return Director.fireNamed(p, StringArgumentType.getString(c, "e")) ? 1 : 0;
								}))));
	}
}
