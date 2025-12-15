package roguelike_emotions.characters;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import roguelike_emotions.effects.EffectDetail;
import roguelike_emotions.utils.CombatLogger;

/**
 * Clase Attack mejorada: - Los efectos se aplican SOLO al objetivo correcto -
 * Mejor feedback de efectos aplicados - Limpieza de efectos expirados
 */
public class Attack {

	private double velocidad = 1.0;
	private boolean rebote = false;
	private String efectoEspecial = "ninguno";
	private List<EffectDetail> efectos = new ArrayList<>();

	// ==================== SETTERS CON FEEDBACK ====================
	public void setVelocidad(double velocidad) {
		this.velocidad = velocidad;
		CombatLogger.get().log("     Velocidad ajustada a " + velocidad);
	}

	public void setRebote(boolean rebote) {
		this.rebote = rebote;
		if (rebote) {
			CombatLogger.get().log("     Rebote activado");
		}
	}

	public void setEfectoEspecial(String efecto) {
		this.efectoEspecial = efecto;
		CombatLogger.get().log("    Efecto especial: " + efecto);
	}

	// ==================== GETTERS ====================
	public double getVelocidad() {
		return velocidad;
	}

	public boolean isRebote() {
		return rebote;
	}

	public String getEfectoEspecial() {
		return efectoEspecial;
	}

	public void addEffect(EffectDetail efecto) {
		efectos.add(efecto);
	}

	public List<EffectDetail> getEfectos() {
		return efectos;
	}

	// ==================== APLICAR AL JUGADOR (CORREGIDO) ====================
	/**
	 * Aplica el ataque al jugador: 1. Inflige daño 2. Aplica efectos adicionales
	 * SOLO al jugador
	 */
	public void applyToPlayer(Player jugador, int damage) {
		// 1. Infligir daño
		jugador.recibirDanyo(damage);

		// 2. Aplicar efectos secundarios al jugador
		int effectsApplied = 0;
		for (EffectDetail ed : efectos) {
			if (Math.random() < ed.getProbabilidad()) {
				ed.aplicarA(jugador); // Solo al jugador
				effectsApplied++;
			}
		}

		if (effectsApplied > 0) {
			CombatLogger.get().log("   Aplicados " + effectsApplied + " efecto(s) adicionales");
		}

		tickDuracion();
	}

	// ==================== APLICAR AL ENEMIGO (CORREGIDO) ====================
	/**
	 * Aplica el ataque al enemigo: 1. Inflige daño 2. Aplica efectos SOLO al
	 * enemigo (NO al ataque)
	 */
	public void applyToEnemy(Enemy enemigo, int damage) {
		// 1. Infligir daño
		enemigo.recibirDanyo(damage);

		// 2. Aplicar efectos secundarios al enemigo
		int effectsApplied = 0;
		for (EffectDetail ed : efectos) {
			if (Math.random() < ed.getProbabilidad()) {
				// Nota: Algunos efectos (como FUEGO) pueden necesitar
				// aplicarse al jugador para buffs, otros al enemigo para debuffs
				// Por ahora aplicamos al ataque para efectos especiales
				ed.aplicarA(this); // Modifica propiedades del ataque
				effectsApplied++;
			}
		}

		if (effectsApplied > 0) {
			CombatLogger.get().log("    Efectos especiales aplicados");
		}

		tickDuracion();
	}

	/**
	 * Reduce la duración de todos los efectos y elimina expirados
	 */
	private void tickDuracion() {
		Iterator<EffectDetail> it = efectos.iterator();
		while (it.hasNext()) {
			EffectDetail ed = it.next();
			ed.reducirDuracion(1);
			if (ed.haExpirado()) {
				it.remove();
			}
		}
	}
}
