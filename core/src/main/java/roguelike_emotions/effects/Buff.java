package roguelike_emotions.effects;

public class Buff extends AbstractTimedEffect {
	private String type;
	private double multiplier;

	public Buff(String type, double m, int duration) {
		super(duration);
		this.type = type;
		this.multiplier = m;
	}

	@Override
	public String getNombre() {
		return "Buff: " + type;
	}

	public String getType() {
		return type;
	}

	public double getMultiplier() {
		return multiplier;
	}
}