package roguelike_emotions.mainMechanics;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import roguelike_emotions.effects.EffectDetail;
import roguelike_emotions.utils.EmotionUtils;

/**
 * Fábrica de emociones procedurales (no fusionadas).
 */
public class EmotionInstanceFactory {

	private static final String[] COLORES = { "#FF4444", "#44FFAA", "#8888FF", "#FFFF66", "#AA66CC", "#FF8844" };

	private static final int MAX_EFECTOS = 3;
	private static final Random RNG = new Random();

	/**
	 * Genera una nueva emoción procedural completa.
	 */
	public EmotionInstance generarProcedural() {
		// 1. Tipo base aleatorio (NO FUSIONADA)
		EmotionType tipoBase;
		do {
			tipoBase = EmotionType.values()[RNG.nextInt(EmotionType.values().length)];
		} while (tipoBase == EmotionType.FUSIONADA);

		// 2. Nombre
		String nombre = EmotionNameGenerator.generarNombrePorTipo(tipoBase);

		// 3. Color aleatorio
		String color = COLORES[RNG.nextInt(COLORES.length)];

		// 4. Generar efectos únicos aleatorios
		int cantidadEfectos = 1 + RNG.nextInt(MAX_EFECTOS);
		List<EffectDetail> efectos = new ArrayList<>();
		List<EmotionEffect> yaUsados = new ArrayList<>();

		while (efectos.size() < cantidadEfectos) {
			EmotionEffect efecto = EmotionEffect.values()[RNG.nextInt(EmotionEffect.values().length)];
			if (!yaUsados.contains(efecto)) {
				efectos.add(EffectDetail.fromConfig(efecto));
				yaUsados.add(efecto);
			}
		}

		// 5. Símbolo desde efectos
		DominantEmotionType dominante = EmotionUtils.detectarTipoDominanteSimple(efectos);
		String simbolo = EmotionNameGenerator.generarSimbolo(dominante);

		// 6. Crear instancia (no fusionada)
		return new EmotionInstance(nombre, tipoBase, efectos, color, simbolo);
	}

}