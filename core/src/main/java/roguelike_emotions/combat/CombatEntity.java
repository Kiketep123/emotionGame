package roguelike_emotions.combat;

import java.util.List;
import java.util.Map;

import roguelike_emotions.effects.Buff;
import roguelike_emotions.effects.Debuff;
import roguelike_emotions.effects.EffectDetail;
import roguelike_emotions.effects.OverTimeHeal;
import roguelike_emotions.mainMechanics.EmotionInstance;

/**
 * Interfaz base para cualquier entidad que participe en combate emocional.
 */
public interface CombatEntity {

	// ==================== IDENTIDAD ====================

	String getNombre();

	EntityType getEntityType();

	enum EntityType {
		PLAYER, ENEMY, ALLY, BOSS
	}

	// ==================== ESTADÍSTICAS BÁSICAS ====================

	int getHealth();

	void setHealth(int health);

	int getMaxHealth();

	int getBaseDamage();

	void setBaseDamage(int damage);

	int getBaseDefense();

	void setBaseDefense(int defense);

	int getSpeed();

	void setSpeed(int speed);

	// ==================== SISTEMA EMOCIONAL ====================

	List<EmotionInstance> getEmocionesActivas();

	void addEmocion(EmotionInstance emotion);

	void removeEmocion(EmotionInstance emotion);

	void clearEmociones();

	// ==================== EFECTOS Y ESTADOS ====================

	List<EffectDetail> getEfectosActivos();

	List<OverTimeHeal> getHealOverTimeEffects();

	Map<String, Buff> getActiveBuffs();

	Map<String, Debuff> getActiveDebuffs();

	// ==================== ESTADO DE COMBATE ====================

	boolean isAlive();

	boolean isStunned();

	boolean canAct();

	int takeDamage(int amount);

	void heal(int amount);

	// ==================== HABILIDADES ====================

	boolean canUseActive();

	void setCanUseActive(boolean canUse);

	int getCooldownTurns();

	void setCooldownTurns(int turns);
}
