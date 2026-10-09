package com.veil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class Scheduler {
	private record Task(long time, Runnable run) {}

	private static final List<Task> TASKS = new ArrayList<>();
	private static long now;

	private Scheduler() {}

	public static void after(int ticks, Runnable r) {
		TASKS.add(new Task(now + Math.max(0, ticks), r));
	}

	public static void tick() {
		now++;
		if (TASKS.isEmpty()) return;
		List<Task> due = new ArrayList<>();
		Iterator<Task> it = TASKS.iterator();
		while (it.hasNext()) {
			Task t = it.next();
			if (t.time() <= now) {
				due.add(t);
				it.remove();
			}
		}
		for (Task t : due) {
			try {
				t.run().run();
			} catch (Exception ignored) {
			}
		}
	}
}
