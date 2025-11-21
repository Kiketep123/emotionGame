package roguelike_emotions.characters;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import roguelike_emotions.effects.EffectDetail;
import roguelike_emotions.utils.CombatLogger;

public class Attack {
	private double velocidad = 1.0;
	private boolean rebote = false;
	private String efectoEspecial = "ninguno";
	private List<EffectDetail> efectos = new ArrayList<>();

	public void setVelocidad(double velocidad) {
		this.velocidad = velocidad;
		CombatLogger.get().log("[Ataque] Velocidad ajustada a " + velocidad);
	}

	public void setRebote(boolean rebote) {
		this.rebote = rebote;
		CombatLogger.get().log("[Ataque] Rebote activado.");
	}

	public void setEfectoEspecial(String efecto) {
		this.efectoEspecial = efecto;
		CombatLogger.get().log("[Ataque] Efecto especial: " + efecto);
	}

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

	/**
	 * Aplica este ataque al jugador: 1) Inflige la cantidad de daño indicada (con
	 * defensa incluida) 2) Aplica todos los EffectDetail adicionales
	 *
	 * @param jugador El jugador receptor
	 * @param damage  El daño bruto que debe recibir (antes de efectos extra)
	 */
	public void applyToPlayer(Player jugador, int damage) {
		// 1) Infligir daño usando el método existente
		jugador.recibirDanyo(damage);

		// 2) Aplicar efectos adicionales, uno por uno
		for (EffectDetail ed : efectos) {
			ed.aplicarA(jugador);
		}
		tickDuracion();

	}

	public void applyToEnemy(Enemy enemigo, int damage) {
		// 1) Infligir daño
		enemigo.recibirDanyo(damage);

		// TODO Esto no ahce nada
		// 2) Aplicar efectos secundarios del ataque
		for (EffectDetail ed : efectos) {
			// Aplica modificación al ataque (rebote, velocidad, etc.)
			ed.aplicarA(this);
			// Si más adelante quisieras efectos directos al enemigo,
			// añade aquí ed.aplicarAlEnemy(enemigo)
		}
	}

	public void tickDuracion() {
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
