package roguelike_emotions.characters;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import roguelike_emotions.mainMechanics.EmotionDominanceMatrix;
import roguelike_emotions.mainMechanics.EmotionInstance;
import roguelike_emotions.mainMechanics.EmotionInstanceFactory;
import roguelike_emotions.mainMechanics.EmotionType;

public class EnemyFactory {

	private static final Random RNG = new Random();
	private EmotionDominanceMatrix matrix;

	/**
	 * Genera 'cantidad' de enemigos con stats BALANCEADOS
	 */
	public List<Enemy> generarEnemigos(int cantidad, EmotionDominanceMatrix matrix) {
		this.matrix = matrix;
		Enemy.setDominanceMatrix(matrix);

		List<Enemy> list = new ArrayList<>();

		for (int i = 0; i < cantidad; i++) {
			// 1) Elige rol y tipo base
			EnemyRole role = EnemyRole.values()[RNG.nextInt(EnemyRole.values().length)];
			EmotionType tipoBase = EmotionType.random();

			// 2) Stats base REDUCIDOS (eran 100/20/10)
			double factorMulti = calcularFactorMultiEmotion(new ArrayList<>(), tipoBase);
			int vida = escala((int) (100 * factorMulti), tipoBase);
			int danyo = escala((int) (10 * factorMulti), tipoBase);
			int def = escala((int) (5 * factorMulti), tipoBase); //
			double vel = escala((int) (10 * factorMulti), tipoBase) / 10.0;

			// 3) Ajusta según rol (con multiplicadores MÁS MODERADOS)
			switch (role) {
			case TANK:
				vida = (int) (vida * 1.2); // ← 1.3 en lugar de 1.5
				danyo = (int) (danyo * 0.9); // ← 0.8 en lugar de 0.75
				def = (int) (def * 1.3); // ← 1.3 en lugar de 1.5
				vel *= 0.85; // ← 0.9 en lugar de 0.8
				break;

			case DPS:
				vida = (int) (vida * 0.85); // ← 0.85 en lugar de 0.8
				danyo = (int) (danyo * 1.5); // ← 1.3 en lugar de 1.5
				def = (int) (def * 0.9); // ← 0.9 en lugar de 0.8
				vel *= 1.15; // ← 1.15 en lugar de 1.2
				break;

			case SUPPORT:
			default:
				// Stats base sin modificar
				break;
			}

			// 4) Crear Enemy
			Enemy e = new Enemy("Enemigo" + (i + 1), vida, danyo, def, vel, role);
			e.setRole(role);

			// 5) Emoción inicial
			EmotionInstance em = new EmotionInstanceFactory().generarProcedural();
			e.setEstadoEmocional(em);
			e.addEmotion(em);

			list.add(e);

			// Debug: Mostrar stats generados
			System.out.printf("[EnemyFactory] Generado: %s | HP:%d ATK:%d DEF:%d | Role:%s%n", e.getNombre(), vida,
					danyo, def, role);
		}

		return list;
	}

	private double calcularFactorMultiEmotion(List<EmotionInstance> emos, EmotionType tipoJugador) {
		if (matrix == null || emos.isEmpty()) {
			return 1.0;
		}

		// 1) Compatibilidad con jugador
		double sumJ = 0;
		for (EmotionInstance e : emos) {
			sumJ += matrix.getPeso(e.getTipoBase(), tipoJugador);
		}
		double fJ = sumJ / emos.size();

		// 2) Compatibilidad interna entre emociones
		double sumIJ = 0;
		int c = 0;
		for (int i = 0; i < emos.size(); i++) {
			for (int j = i + 1; j < emos.size(); j++) {
				EmotionType t1 = emos.get(i).getTipoBase();
				EmotionType t2 = emos.get(j).getTipoBase();
				sumIJ += (matrix.getPeso(t1, t2) + matrix.getPeso(t2, t1)) / 2.0;
				c++;
			}
		}
		double fI = c > 0 ? sumIJ / c : 1.0;

		// 3) Media de ambas
		double media = (fJ + fI) / 2.0;
		double factor = 1.0 + (media - 1.0) * 0.5;
		return Math.max(0.75, Math.min(1.25, factor));
	}

	/**
	 * Escala un valor base según el tipo emocional (REDUCIDO)
	 */
	private int escala(int base, EmotionType tipo) {
		double factor = switch (tipo) {
		case IRA -> 1.15; // ← 1.15 en lugar de 1.3
		case MIEDO -> 1.1; // ← 1.1 en lugar de 1.2
		case TRISTEZA -> 0.95; // ← 0.95 en lugar de 0.9
		case ALEGRIA -> 1.05; // ← 1.05 en lugar de 1.1
		case CULPA -> 1.0;
		case ESPERANZA -> 1.0;
		case RABIA -> 1.25; // ← 1.25 en lugar de 1.6
		default -> 1.0 + (RNG.nextDouble() - 0.5) * 0.15; // ← 0.15 en lugar de 0.2
		};
		return (int) Math.max(1, base * factor);
	}

	public List<Enemy> generarEnemigosPorTipo(int cantidad, EmotionType tipo, EmotionDominanceMatrix matrix) {
		this.matrix = matrix;
		Enemy.setDominanceMatrix(matrix);

		List<Enemy> list = new ArrayList<>();

		for (int i = 0; i < cantidad; i++) {
			EnemyRole role = EnemyRole.values()[RNG.nextInt(EnemyRole.values().length)];
			EmotionType tipoBase = tipo;

			double factorMulti = calcularFactorMultiEmotion(new ArrayList<>(), tipoBase);
			int vida = escala((int) (50 * factorMulti), tipoBase); // ← 50
			int danyo = escala((int) (8 * factorMulti), tipoBase); // ← 8
			int def = escala((int) (3 * factorMulti), tipoBase); // ← 3
			double vel = escala((int) (10 * factorMulti), tipoBase) / 10.0;

			switch (role) {
			case TANK:
				vida = (int) (vida * 1.3);
				danyo = (int) (danyo * 0.8);
				def = (int) (def * 1.3);
				vel *= 0.9;
				break;

			case DPS:
				vida = (int) (vida * 0.85);
				danyo = (int) (danyo * 1.3);
				def = (int) (def * 0.9);
				vel *= 1.15;
				break;

			case SUPPORT:
			default:
				break;
			}

			Enemy e = new Enemy("Enemigo" + (i + 1), vida, danyo, def, vel, role);
			e.setRole(role);

			EmotionInstance em = new EmotionInstanceFactory().generarProcedural();
			e.setEstadoEmocional(em);
			e.addEmotion(em);

			list.add(e);

			System.out.printf("[EnemyFactory] Generado: %s | HP:%d ATK:%d DEF:%d | Role:%s%n", e.getNombre(), vida,
					danyo, def, role);
		}

		return list;
	}
}
