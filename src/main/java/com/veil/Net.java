package com.veil;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class Net {
	private static final String V = "1";

	public static final SimpleChannel CH = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(Veil.ID, "main"), () -> V,
			NetworkRegistry.acceptMissingOr(V), NetworkRegistry.acceptMissingOr(V));

	public record DreadMsg(float v) {}
	public record ScareMsg() {}

	private Net() {}

	public static void init() {
		CH.registerMessage(0, DreadMsg.class,
				(m, b) -> b.writeFloat(m.v()),
				b -> new DreadMsg(b.readFloat()),
				(m, ctx) -> {
					ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
							() -> () -> com.veil.client.ClientState.setDread(m.v())));
					ctx.get().setPacketHandled(true);
				});
		CH.registerMessage(1, ScareMsg.class,
				(m, b) -> {},
				b -> new ScareMsg(),
				(m, ctx) -> {
					ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
							() -> () -> com.veil.client.ClientState.scare()));
					ctx.get().setPacketHandled(true);
				});
	}

	private static boolean ok(ServerPlayer p) {
		return CH.isRemotePresent(p.connection.connection);
	}

	public static void sendDread(ServerPlayer p, float v) {
		if (ok(p)) CH.send(PacketDistributor.PLAYER.with(() -> p), new DreadMsg(v));
	}

	public static void sendScare(ServerPlayer p) {
		if (ok(p)) CH.send(PacketDistributor.PLAYER.with(() -> p), new ScareMsg());
	}
}
