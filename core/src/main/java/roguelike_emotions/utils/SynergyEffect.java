// SynergyEffect.java
package roguelike_emotions.utils;

public class SynergyEffect {
	private final double damageMultiplier; // e.g. 1.10 para +10% daño
	private final double defenseMultiplier; // e.g. 0.90 para –10% defensa
	private final double speedMultiplier; // e.g. 1.05 para +5% velocidad
	private final int hotAmount; // curación por turno (si >0)
	private final int hotTurns; // duración HoT
	private final int poisonAmount; // daño por veneno por turno (si >0)
	private final int poisonTurns; // duración veneno
	private final int stunTurns; // si aplica stun directo (si >0)
	private final String buffType; // si aplica un buff genérico (ej: "defenseBoost")
	private final double buffMultiplier; // multiplicador del buff
	private final int buffTurns; // duración del buff
	private final String debuffType; // si aplica un debuff genérico (ej: "silence" o "slow")
	private final int debuffTurns; // duración del debuff

	public SynergyEffect(double damageMultiplier, double defenseMultiplier, double speedMultiplier, int hotAmount,
			int hotTurns, int poisonAmount, int poisonTurns, int stunTurns, String buffType, double buffMultiplier,
			int buffTurns, String debuffType, int debuffTurns) {
		this.damageMultiplier = damageMultiplier;
		this.defenseMultiplier = defenseMultiplier;
		this.speedMultiplier = speedMultiplier;
		this.hotAmount = hotAmount;
		this.hotTurns = hotTurns;
		this.poisonAmount = poisonAmount;
		this.poisonTurns = poisonTurns;
		this.stunTurns = stunTurns;
		this.buffType = buffType;
		this.buffMultiplier = buffMultiplier;
		this.buffTurns = buffTurns;
		this.debuffType = debuffType;
		this.debuffTurns = debuffTurns;
	}

	// Getters para cada campo…
	public double getDamageMultiplier() {
		return damageMultiplier;
	}

	public double getDefenseMultiplier() {
		return defenseMultiplier;
	}

	public double getSpeedMultiplier() {
		return speedMultiplier;
	}

	public int getHotAmount() {
		return hotAmount;
	}

	public int getHotTurns() {
		return hotTurns;
	}

	public int getPoisonAmount() {
		return poisonAmount;
	}

	public int getPoisonTurns() {
		return poisonTurns;
	}

	public int getStunTurns() {
		return stunTurns;
	}

	public String getBuffType() {
		return buffType;
	}

	public double getBuffMultiplier() {
		return buffMultiplier;
	}

	public int getBuffTurns() {
		return buffTurns;
	}

	public String getDebuffType() {
		return debuffType;
	}

	public int getDebuffTurns() {
		return debuffTurns;
	}
}
