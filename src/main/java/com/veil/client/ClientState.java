package com.veil.client;

import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.gui.overlay.ForgeGui;

public final class ClientState {
	private static final Random R = new Random();

	private static float dread;
	private static float shown;
	private static int scare;
	private static int eyeTicks;
	private static int eyeX;
	private static int eyeY;

	private ClientState() {}

	public static void setDread(float v) {
		dread = v;
	}

	public static void scare() {
		scare = 14;
	}

	public static void tick() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) {
			dread = 0;
			shown = 0;
			scare = 0;
			eyeTicks = 0;
			return;
		}
		shown += (dread - shown) * 0.02f;
		if (scare > 0) scare--;
		if (eyeTicks > 0) {
			eyeTicks--;
		} else if (dread > 35 && R.nextInt(Math.max(200, (int) (1800 - dread * 14))) == 0) {
			eyeTicks = 6 + R.nextInt(14);
			eyeX = 12 + R.nextInt(76);
			eyeY = 30 + R.nextInt(50);
		}
	}

	public static void render(ForgeGui gui, GuiGraphics g, float partialTick, int w, int h) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.options.hideGui) return;

		if (scare > 0) {
			if (scare >= 13) {
				g.fill(0, 0, w, h, 0xFFFFFFFF);
				return;
			}
			int a = Math.min(255, scare * 40);
			g.fill(0, 0, w, h, a << 24);
			int ew = Math.max(6, w / 10);
			int eh = Math.max(3, h / 36);
			int cx = w / 2 + R.nextInt(5) - 2;
			int cy = h / 2 - h / 10 + R.nextInt(5) - 2;
			int gap = ew;
			int col = (a << 24) | 0xFFFFFF;
			g.fill(cx - gap - ew, cy, cx - gap, cy + eh, col);
			g.fill(cx + gap, cy, cx + gap + ew, cy + eh, col);
			g.fill(cx - gap - ew / 2, cy - eh, cx - gap, cy, col);
			g.fill(cx + gap, cy - eh, cx + gap + ew / 2, cy, col);
			return;
		}

		float a = Math.min(1f, shown / 100f);
		if (a > 0.12f) {
			int layers = 20;
			int sv = Math.max(1, h / 70);
			int sh = Math.max(1, w / 70);
			for (int i = 0; i < layers; i++) {
				double f = 1.0 - i / (double) layers;
				int alpha = (int) (255 * a * 0.6 * f * f);
				if (alpha <= 0) continue;
				int col = alpha << 24;
				g.fill(0, i * sv, w, (i + 1) * sv, col);
				g.fill(0, h - (i + 1) * sv, w, h - i * sv, col);
				g.fill(i * sh, 0, (i + 1) * sh, h, col);
				g.fill(w - (i + 1) * sh, 0, w - i * sh, h, col);
			}
		}

		if (eyeTicks > 0) {
			int alpha = Math.min(200, eyeTicks * 40);
			int col = (alpha << 24) | 0xF0E8E8;
			int ex = w * eyeX / 100;
			int ey = h * eyeY / 100;
			int ew = Math.max(3, w / 150);
			int eh = Math.max(2, h / 300);
			int gap = ew * 3;
			g.fill(ex - gap - ew, ey, ex - gap, ey + eh, col);
			g.fill(ex + gap, ey, ex + gap + ew, ey + eh, col);
		}
	}
}
