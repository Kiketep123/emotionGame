package roguelike_emotions.vfx;

import java.util.ArrayDeque;

public class Director {
	private final ArrayDeque<Clip> queue = new ArrayDeque<>();
	private Clip current;

	public void post(VisEvent e) {
		Clip c = ClipFactory.from(e);
		if (c != null)
			queue.add(c);
	}

	public void update(float dt) {
		if (current == null && !queue.isEmpty())
			current = queue.poll();
		if (current != null && current.update(dt))
			current = null;
	}

	public boolean isBusy() {
		return current != null;
	}
}
