package roguelike_emotions.vfx;

public abstract class Clip {
	protected float t = 0f;
	protected final float dur;

	protected Clip(float dur) {
		this.dur = dur;
	}

	public boolean update(float dt) {
		t += dt;
		return t >= dur;
	}
}