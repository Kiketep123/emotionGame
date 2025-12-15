package roguelike_emotions.effects;

public class Debuff extends AbstractTimedEffect {
	private final String type;
	private final double severity;

	public Debuff(String type, double severity, int duration) {
		super(duration); // 
		this.type = type;
		this.severity = severity;
	}

	@Override
	public String getNombre() {
		return "Debuff: " + type;
	}

	public String getType() {
		return type;
	}

	public double getSeverity() {
		return severity;
	}

}
