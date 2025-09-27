package roguelike_emotions.utils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import roguelike_emotions.effects.EffectDetail;
import roguelike_emotions.mainMechanics.DominantEmotionType;
import roguelike_emotions.mainMechanics.EmotionDominanceMatrix;
import roguelike_emotions.mainMechanics.EmotionEffect;
import roguelike_emotions.mainMechanics.EmotionInstance;
import roguelike_emotions.mainMechanics.EmotionNameGenerator;
import roguelike_emotions.mainMechanics.EmotionType;

/**
 * Combina dos emociones **reales** y registra solo aquí, aplicando
 * sinergias/tensiones según la EmotionDominanceMatrix.
 */
public class EmotionCombiner {
	// Matriz de dominancia compartida (inyectada desde GameManager)
	private static EmotionDominanceMatrix matrix;
	private static final double FUSION_THRESHOLD = 1.1;

	/** Debes llamar a esto una vez al inicializar el GameManager */
	public static void setDominanceMatrix(EmotionDominanceMatrix m) {
		matrix = m;
	}

	public static EmotionInstance combinar(EmotionInstance e1, EmotionInstance e2) {
		// 1) verificar registro
		if (!canFuse(e1, e2)) {
			throw new IllegalArgumentException("Estas emociones no son compatibles para fusionar.");
		}
		EmotionInstance ex = FusionRegistry.obtenerFusion(e1.getId(), e2.getId());
		if (ex != null)
			return ex;

		// 2) juntar y agrupar efectos sin duplicados
		Map<EmotionEffect, EffectDetail> map = new LinkedHashMap<>();
		Stream.concat(e1.getEfectos().stream(), e2.getEfectos().stream()).forEach(ed -> {
			EmotionEffect tipo = ed.getTipo(); // ← Usa el string para obtener el enum

			if (!map.containsKey(tipo)) {
				map.put(tipo,
						new EffectDetail(tipo, ed.getIntensidad(), ed.getProbabilidad(), ed.getDuracionRestante()));
			} else {
				EffectDetail o = map.get(tipo);
				map.put(tipo,
						new EffectDetail(tipo, o.getIntensidad() + ed.getIntensidad(),
								Math.max(o.getProbabilidad(), ed.getProbabilidad()),
								Math.max(o.getDuracionRestante(), ed.getDuracionRestante())));
			}
		});

		// 3) convertir a lista
		List<EffectDetail> fusionDetalles = new ArrayList<>(map.values());

		// 4) calcular factor de compatibilidad
		double factor = calcularFactorCompatibilidad(e1.getTipoBase(), e2.getTipoBase());

		// 5) ajustar intensidades
		List<EffectDetail> ajustados = new ArrayList<>();
		for (EffectDetail ed : fusionDetalles) {
			ajustados.add(new EffectDetail(ed.getTipo(), ed.getIntensidad() * factor, ed.getProbabilidad(),
					ed.getDuracionRestante()));
		}

		// 6) categoría dominante y tipo base resultante
		DominantEmotionType dom = EmotionUtils.detectarTipoDominante(e1, e2);
		EmotionType tipoBase = EmotionUtils.convertirDominantToEmotionType(dom);

		// 7) nombre, símbolo y color mixto
		String nombre = EmotionNameGenerator.generarNombreGuiado(dom);
		String simbolo = EmotionNameGenerator.generarSimbolo(dom);

		Color c1 = Color.decode(e1.getColor());
		Color c2 = Color.decode(e2.getColor());
		String mixColor = EmotionUtils.mixColorrs(c1, c2);

		// 8) crear y registrar
		EmotionInstance fusionada = new EmotionInstance(nombre, tipoBase, ajustados, mixColor, simbolo);
		FusionRegistry.registrarFusion(e1.getId(), e2.getId(), fusionada);
		return fusionada;
	}

	/**
	 * Factor de compatibilidad entre dos tipos base: media de peso(t1→t2) y
	 * peso(t2→t1) normalizada a [0.75,1.25].
	 */
	private static double calcularFactorCompatibilidad(EmotionType t1, EmotionType t2) {
		if (matrix == null) {
			// Fallback: sin factor si no se ha inyectado
			return 1.0;
		}
		double p12 = matrix.getPeso(t1, t2);
		double p21 = matrix.getPeso(t2, t1);
		double media = (p12 + p21) / 2.0;
		double factor = 1.0 + (media - 1.0) * 0.5;
		return Math.max(0.75, Math.min(1.25, factor));
	}

	public static boolean canFuse(EmotionInstance e1, EmotionInstance e2) {
		EmotionType t1 = e1.getTipoBase(), t2 = e2.getTipoBase();
		double peso12 = matrix.getPeso(t1, t2);
		double peso21 = matrix.getPeso(t2, t1);
		double media = (peso12 + peso21) / 2;
		return media >= FUSION_THRESHOLD;
	}
	
	// ---------------------------------------------------------------------------------
    // 2.2. NUEVO MÉTODO: combinarMultiples(List<EmotionInstance>)
    //       Combina simultáneamente n emociones (n ≥ 2).
    // ---------------------------------------------------------------------------------
    public static EmotionInstance combinarMultiples(List<EmotionInstance> entradas) {
        if (entradas == null || entradas.size() < 2) {
            throw new IllegalArgumentException("Se requieren al menos dos emociones para fusionar.");
        }

        //  a) Verificar si ya existe en el registro una fusión exacta de este conjunto
        //     Para simplificar, concatenamos los IDs en orden lexicográfico:
        List<String> ids = entradas.stream()
                .map(EmotionInstance::getId)
                .sorted()
                .collect(Collectors.toList());
        String keyConcatenado = String.join("|", ids);
        EmotionInstance posible = FusionRegistry.obtenerFusionPorClave(keyConcatenado);
        if (posible != null) {
            return posible;
        }

        //  b) Consolidar todos los EffectDetail de las n emociones en un mapa
        Map<String, EffectDetail> mapaUnificado = new LinkedHashMap<>();
        for (EmotionInstance e : entradas) {
            for (EffectDetail ed : e.getEfectos()) {
                String t = ed.getTipo().name();
                if (!mapaUnificado.containsKey(t)) {
                    // Copiamos
                    mapaUnificado.put(t, new EffectDetail(
                            ed.getTipo(),
                            ed.getIntensidad(),
                            ed.getProbabilidad(),
                            ed.getDuracionRestante()
                    ));
                } else {
                    EffectDetail existente = mapaUnificado.get(t);
                    double sumaInt = existente.getIntensidad() + ed.getIntensidad();
                    int maxDur   = Math.max(existente.getDuracionRestante(), ed.getDuracionRestante());
                    double maxProb= Math.max(existente.getProbabilidad(), ed.getProbabilidad());
                    mapaUnificado.put(t, new EffectDetail(
                            ed.getTipo(),
                            sumaInt,
                            maxProb,
                            maxDur
                    ));
                }
            }
        }
        List<EffectDetail> fusionDetalles = new ArrayList<>(mapaUnificado.values());

        //  c) Determinar “tipoBase” dominante en el grupo
        EmotionType tipoBase = detectarTipoBaseMultiple(entradas);

        //  d) Nombre, símbolo y color resultante
        String nombre = EmotionNameGenerator.generarNombrePorTipo(tipoBase); 
        // (podríamos usar un nombre guiado más complejo si quisiéramos distinguir
        //  fusiones de 3, 4 emociones, etc. pero para simplicidad usamos generarNombreUnico)
        String simbolo = "�mix"; 
        // (puedes definir un emoji o un símbolo genérico para fusiones múltiples)
        // Mezclamos colores promediando componentes RGB:
        Color c0 = Color.decode(entradas.get(0).getColor());
        float rSum = c0.getRed(), gSum = c0.getGreen(), bSum = c0.getBlue();
        for (int i = 1; i < entradas.size(); i++) {
            Color ci = Color.decode(entradas.get(i).getColor());
            rSum += ci.getRed();
            gSum += ci.getGreen();
            bSum += ci.getBlue();
        }
        int n = entradas.size();
        String mixColor = String.format("#%02X%02X%02X",
                Math.min(255, Math.round(rSum / n)),
                Math.min(255, Math.round(gSum / n)),
                Math.min(255, Math.round(bSum / n)));

        //  e) Aplicar factor de compatibilidad global de todo el grupo
        double factorGlobal = calcularFactorCompatibilidadMultiple(entradas);

        //  f) Aplicar reglas de “umbral especial” para pares prohibidos o “antagónicos”
        double  factorPenalizacion = aplicarPenalizacionesAntagonistas(entradas);

        //  g) Ajustar la lista de EffectDetail con factorGlobal * factorPenalizacion
        List<EffectDetail> ajustadosFinal = fusionDetalles.stream()
                .map(ed -> new EffectDetail(
                        ed.getTipo(),
                        ed.getIntensidad() * factorGlobal * factorPenalizacion,
                        ed.getProbabilidad(),
                        ed.getDuracionRestante()
                ))
                .collect(Collectors.toList());

        //  h) Crear y registrar con la “clave” multiple (concatenada) para no repetir
        EmotionInstance fusionada = new EmotionInstance(nombre, tipoBase, ajustadosFinal, mixColor, simbolo);
        FusionRegistry.registrarFusionMultiple(keyConcatenado, entradas, fusionada);
        return fusionada;
    }
    
    // -----------------------------
    //  Método auxiliar para detectar el tipo dominante entre n emociones
    // -----------------------------
    private static EmotionType detectarTipoBaseMultiple(List<EmotionInstance> emos) {
        // Contamos puntajes para cada candidato T ∈ EmotionType
        Map<EmotionType, Double> puntajes = new LinkedHashMap<>();
        for (EmotionType candidato : EmotionType.values()) {
            puntajes.put(candidato, 0.0);
        }

        int n = emos.size();
        // Sumamos peso(e_i → candidato) para cada i=1..n
        for (EmotionInstance e : emos) {
            EmotionType t_i = e.getTipoBase();
            for (EmotionType candidato : EmotionType.values()) {
                double w = (matrix != null)
                         ? matrix.getPeso(t_i, candidato)
                         : 1.0;
                puntajes.put(candidato, puntajes.get(candidato) + w);
            }
        }
        // Dividimos por n para obtener un promedio
        for (EmotionType candidato : puntajes.keySet()) {
            puntajes.put(candidato, puntajes.get(candidato) / n);
        }

        // Elegimos aquel con mayor puntaje
        return puntajes.entrySet().stream()
                .max((a, b) -> Double.compare(a.getValue(), b.getValue()))
                .get()
                .getKey();
    }
    // -----------------------------
    //  Método auxiliar para calcular factor de compatibilidad global (interacciones internas)
    // -----------------------------
    private static double calcularFactorCompatibilidadMultiple(List<EmotionInstance> emos) {
        if (matrix == null || emos.size() < 2) {
            return 1.0;
        }
        int n = emos.size();
        double sumIJ = 0.0;
        int count = 0;
        for (int i = 0; i < n; i++) {
            EmotionType t1 = emos.get(i).getTipoBase();
            for (int j = i + 1; j < n; j++) {
                EmotionType t2 = emos.get(j).getTipoBase();
                double p12 = matrix.getPeso(t1, t2);
                double p21 = matrix.getPeso(t2, t1);
                sumIJ += (p12 + p21) / 2.0;
                count++;
            }
        }
        double mediaInterna = (count > 0) ? (sumIJ / count) : 1.0;
        double factor = 1.0 + (mediaInterna - 1.0) * 0.5;
        if (factor < 0.75) factor = 0.75;
        if (factor > 1.25) factor = 1.25;
        return factor;
    }
    
    // -----------------------------
    //  Método auxiliar para penalizar combinaciones “antagónicas” específicas
    // -----------------------------
    private static double aplicarPenalizacionesAntagonistas(List<EmotionInstance> emos) {
        // Definimos un mapa de antagonismos: si aparece clave → valores, se aplica penalización.
        // (ajusta según tu diseño de juego)
        Map<EmotionType, List<EmotionType>> antagonismos = Map.of(
            EmotionType.IRA, List.of(EmotionType.TRISTEZA, EmotionType.MIEDO),
            EmotionType.TRISTEZA, List.of(EmotionType.ALEGRIA),
            EmotionType.MIEDO, List.of(EmotionType.RABIA)
            // ... añade más reglas según la temática
        );

        double penal = 1.0;
        for (int i = 0; i < emos.size(); i++) {
            EmotionType t1 = emos.get(i).getTipoBase();
            for (int j = i + 1; j < emos.size(); j++) {
                EmotionType t2 = emos.get(j).getTipoBase();
                // Si (t1→t2) o (t2→t1) está en antagonismos, reducimos en 10%
                if ( antagonismos.getOrDefault(t1, List.of()).contains(t2)
                  || antagonismos.getOrDefault(t2, List.of()).contains(t1)) {
                    penal *= 0.9;
                }
            }
        }
        // Nos aseguramos que no baje de 0.75
        return Math.max(0.75, penal);
    }
}
