package roguelike_emotions.mainMechanics;

import java.util.List;
import java.util.UUID;

import roguelike_emotions.characters.Attack;
import roguelike_emotions.characters.Player;
import roguelike_emotions.effects.EffectDetail;

public class EmotionInstance implements Emotion {
	private final String id;
	private String nombre;
	private EmotionType tipoBase;
	private List<EffectDetail> efectos;
	private String color; // "#RRGGBB"
	private String simbolo;

	public EmotionInstance(String nombre, EmotionType tipoBase, List<EffectDetail> efectos, String color,
			String simbolo) {
		this.id = UUID.randomUUID().toString();
		this.nombre = nombre;
		this.tipoBase = tipoBase;
		this.efectos = efectos;
		this.color = color;
		this.simbolo = simbolo;
	}

	public String getId() {
		return id;
	}

	@Override
	public String getNombre() {
		return nombre;
	}

	@Override
	public EmotionType getTipoBase() {
		return tipoBase;
	}

	@Override
	public EmotionEffect getEfecto() {
		// Devolvemos el efecto principal (primer elemento)
		return efectos.isEmpty() ? null : efectos.get(0).getTipo();
	}

	@Override
	public String getColor() {
		return color;
	}

	public String getSimbolo() {
		return simbolo;
	}

	public List<EffectDetail> getEfectos() {
		return efectos;
	}

	@Override
	public void aplicarAlJugador(Player player) {
		for (EffectDetail e : efectos) {
			e.aplicarA(player);
		}
	}

	@Override
	public void modificarAtaque(Attack attack) {
		for (EffectDetail e : efectos) {
			e.aplicarA(attack);
		}
	}

	public void setEfectos(List<EffectDetail> nuevosEfectos) {
		this.efectos = nuevosEfectos;
	}

	@Override
	public String toString() {
		return simbolo + " " + nombre + " (" + tipoBase.name().toLowerCase() + ")";
	}

	public void tickEfectos(Player jugador, Attack ataque) {
		for (EffectDetail detalle : efectos) {
			detalle.aplicarA(jugador);
			detalle.aplicarA(ataque);
		}
	}

	public void tickDuracion() {
		for (EffectDetail detalle : efectos) {
			detalle.reducirDuracion(1); // Resta 1 turno
		}
		efectos.removeIf(e -> e.haExpirado());
	}

	public boolean estaExpirada() {
		return efectos.isEmpty();
	}

}
