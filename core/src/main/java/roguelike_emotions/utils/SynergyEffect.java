package roguelike_emotions.utils;

/**
 * Representa un efecto de sinergia entre dos emociones.
 * Inmutable y construido mediante el patrón Builder.
 */
public class SynergyEffect {

    // ==================== ATRIBUTOS ====================

    private final String name;
    private final double damageMultiplier;
    private final double defenseMultiplier;
    private final double speedMultiplier;

    // Heal Over Time (HoT)
    private final int hotAmount;
    private final int hotTurns;

    // Poison/Damage Over Time (DoT)
    private final int poisonAmount;
    private final int poisonTurns;

    // Stun
    private final int stunTurns;

    // Buff
    private final String buffType;
    private final double buffMultiplier;
    private final int buffTurns;

    // Debuff
    private final String debuffType;
    private final int debuffTurns;

    // ==================== CONSTRUCTOR PRIVADO ====================

    private SynergyEffect(Builder builder) {
        this.name = builder.name;
        this.damageMultiplier = builder.damageMultiplier;
        this.defenseMultiplier = builder.defenseMultiplier;
        this.speedMultiplier = builder.speedMultiplier;
        this.hotAmount = builder.hotAmount;
        this.hotTurns = builder.hotTurns;
        this.poisonAmount = builder.poisonAmount;
        this.poisonTurns = builder.poisonTurns;
        this.stunTurns = builder.stunTurns;
        this.buffType = builder.buffType;
        this.buffMultiplier = builder.buffMultiplier;
        this.buffTurns = builder.buffTurns;
        this.debuffType = builder.debuffType;
        this.debuffTurns = builder.debuffTurns;
    }

    // ==================== GETTERS ====================

    public String getName() { return name; }
    public double getDamageMultiplier() { return damageMultiplier; }
    public double getDefenseMultiplier() { return defenseMultiplier; }
    public double getSpeedMultiplier() { return speedMultiplier; }
    public int getHotAmount() { return hotAmount; }
    public int getHotTurns() { return hotTurns; }
    public int getPoisonAmount() { return poisonAmount; }
    public int getPoisonTurns() { return poisonTurns; }
    public int getStunTurns() { return stunTurns; }
    public String getBuffType() { return buffType; }
    public double getBuffMultiplier() { return buffMultiplier; }
    public int getBuffTurns() { return buffTurns; }
    public String getDebuffType() { return debuffType; }
    public int getDebuffTurns() { return debuffTurns; }

    // ==================== MÉTODOS DE UTILIDAD ====================

    /**
     * Verifica si este efecto tiene algún modificador activo.
     */
    public boolean hasAnyEffect() {
        return damageMultiplier != 1.0
            || defenseMultiplier != 1.0
            || speedMultiplier != 1.0
            || hotAmount > 0
            || poisonAmount > 0
            || stunTurns > 0
            || buffType != null
            || debuffType != null;
    }

    /**
     * Verifica si este efecto es beneficioso (positivo neto).
     */
    public boolean isBeneficial() {
        int positiveScore = 0;
        int negativeScore = 0;

        if (damageMultiplier > 1.0) positiveScore++;
        if (damageMultiplier < 1.0) negativeScore++;

        if (defenseMultiplier > 1.0) positiveScore++;
        if (defenseMultiplier < 1.0) negativeScore++;

        if (speedMultiplier > 1.0) positiveScore++;
        if (speedMultiplier < 1.0) negativeScore++;

        if (hotAmount > 0) positiveScore += 2;
        if (poisonAmount > 0) negativeScore += 2;

        if (buffType != null) positiveScore++;
        if (debuffType != null) negativeScore++;

        return positiveScore > negativeScore;
    }

    /**
     * Calcula un "poder" aproximado del efecto.
     */
    public double calculatePower() {
        double power = 0.0;

        power += Math.abs(damageMultiplier - 1.0) * 10;
        power += Math.abs(defenseMultiplier - 1.0) * 10;
        power += Math.abs(speedMultiplier - 1.0) * 5;
        power += hotAmount * hotTurns * 0.5;
        power += poisonAmount * poisonTurns * 0.5;
        power += stunTurns * 3;
        power += (buffType != null) ? buffMultiplier * buffTurns : 0;
        power += (debuffType != null) ? debuffTurns * 2 : 0;

        return power;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SynergyEffect{name='").append(name).append("'");

        if (damageMultiplier != 1.0) {
            sb.append(", damage=").append(String.format("%.2f", damageMultiplier));
        }
        if (defenseMultiplier != 1.0) {
            sb.append(", defense=").append(String.format("%.2f", defenseMultiplier));
        }
        if (speedMultiplier != 1.0) {
            sb.append(", speed=").append(String.format("%.2f", speedMultiplier));
        }
        if (hotAmount > 0) {
            sb.append(", HoT=").append(hotAmount).append("x").append(hotTurns);
        }
        if (poisonAmount > 0) {
            sb.append(", poison=").append(poisonAmount).append("x").append(poisonTurns);
        }
        if (stunTurns > 0) {
            sb.append(", stun=").append(stunTurns);
        }
        if (buffType != null) {
            sb.append(", buff=").append(buffType).append("(").append(buffMultiplier).append(")x").append(buffTurns);
        }
        if (debuffType != null) {
            sb.append(", debuff=").append(debuffType).append("x").append(debuffTurns);
        }

        sb.append("}");
        return sb.toString();
    }

    // ==================== BUILDER ====================

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name = "Unnamed Synergy";
        private double damageMultiplier = 1.0;
        private double defenseMultiplier = 1.0;
        private double speedMultiplier = 1.0;
        private int hotAmount = 0;
        private int hotTurns = 0;
        private int poisonAmount = 0;
        private int poisonTurns = 0;
        private int stunTurns = 0;
        private String buffType = null;
        private double buffMultiplier = 1.0;
        private int buffTurns = 0;
        private String debuffType = null;
        private int debuffTurns = 0;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder damage(double multiplier) {
            this.damageMultiplier = multiplier;
            return this;
        }

        public Builder defense(double multiplier) {
            this.defenseMultiplier = multiplier;
            return this;
        }

        public Builder speed(double multiplier) {
            this.speedMultiplier = multiplier;
            return this;
        }

        public Builder healOverTime(int amount, int turns) {
            this.hotAmount = amount;
            this.hotTurns = turns;
            return this;
        }

        public Builder poisonOverTime(int amount, int turns) {
            this.poisonAmount = amount;
            this.poisonTurns = turns;
            return this;
        }

        public Builder stun(int turns) {
            this.stunTurns = turns;
            return this;
        }

        public Builder buff(String type, double multiplier, int turns) {
            this.buffType = type;
            this.buffMultiplier = multiplier;
            this.buffTurns = turns;
            return this;
        }

        public Builder debuff(String type, int turns) {
            this.debuffType = type;
            this.debuffTurns = turns;
            return this;
        }

        public SynergyEffect build() {
            return new SynergyEffect(this);
        }
    }

    // ==================== CONSTRUCTOR LEGACY (para compatibilidad) ====================

    /**
     * Constructor legacy para compatibilidad con código existente.
     * @deprecated Usar builder() en su lugar
     */
    @Deprecated
    public SynergyEffect(double damageMultiplier, double defenseMultiplier,
                        double speedMultiplier, int hotAmount, int hotTurns,
                        int poisonAmount, int poisonTurns, int stunTurns,
                        String buffType, double buffMultiplier, int buffTurns,
                        String debuffType, int debuffTurns) {
        this.name = "Legacy Synergy";
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
}