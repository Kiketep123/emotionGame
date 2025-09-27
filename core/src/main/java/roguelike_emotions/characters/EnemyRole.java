package roguelike_emotions.characters;

public enum EnemyRole {
	TANK("Provocar") {
		@Override
		public double fuseDefenseBonus(int fusions) {
			return 1.0 + 0.05 * fusions;
		}

		@Override
		public double fuseDamageBonus(int fusions) {
			return 1.0;
		}

		@Override
		public double fuseHealBonus(int fusions) {
			return 1.0;
		}
	},
	DPS("GolpeCrítico") {
		@Override
		public double fuseDefenseBonus(int fusions) {
			return 1.0;
		}

		@Override
		public double fuseDamageBonus(int fusions) {
			return 1.0 + 0.10 * fusions;
		}

		@Override
		public double fuseHealBonus(int fusions) {
			return 1.0;
		}
	},
	SUPPORT("VínculoEmocional") {
		@Override
		public double fuseDefenseBonus(int fusions) {
			return 1.0;
		}

		@Override
		public double fuseDamageBonus(int fusions) {
			return 1.0;
		}

		@Override
		public double fuseHealBonus(int fusions) {
			return 1.0 + 0.08 * fusions;
		}
	};

	private final String activeSkill;

	EnemyRole(String skill) {
		this.activeSkill = skill;
	}

	public String getActiveSkill() {
		return activeSkill;
	}

	/** Bonos por número de fusiones activas */
	public double fuseDefenseBonus(int fusions) {
		return 1.0;
	}

	public double fuseDamageBonus(int fusions) {
		return 1.0;
	}

	public double fuseHealBonus(int fusions) {
		return 1.0;
	}
}
