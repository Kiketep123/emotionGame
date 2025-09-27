package roguelike_emotions.effects;

public class OverTimeHeal extends AbstractTimedEffect {
	private final int amount;

	public OverTimeHeal(int amount, int duration) {
		super(duration); // 
		this.amount = amount;
	}

	@Override
	public String getNombre() {
		return "Heal over time";
	}

	public int getAmount() {
		return amount;
	}
}
