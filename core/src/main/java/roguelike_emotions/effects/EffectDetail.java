package roguelike_emotions.effects;

import java.util.List;

import roguelike_emotions.cfg.EffectConfig;
import roguelike_emotions.cfg.EffectConfigLoader;
import roguelike_emotions.characters.Attack;
import roguelike_emotions.characters.Player;
import roguelike_emotions.mainMechanics.EmotionEffect;
import roguelike_emotions.ui.EffectVisualData;
import roguelike_emotions.ui.EmotionEffectVisualRegistry;

public class EffectDetail extends AbstractTimedEffect {
	public EffectDetail(EmotionEffect tipo, double intensidad, double probabilidad, int duracionRestante) {
		super(duracionRestante);
		this.tipo = tipo;
		this.intensidad = intensidad;
		this.probabilidad = probabilidad;
		this.duracionRestante = duracionRestante;
	}

	private EmotionEffect tipo;
	private double intensidad;
	private double probabilidad;
	private int duracionRestante;

	public static EffectDetail fromConfig(EmotionEffect tipo) {
		// Asumimos que tu JSON es una lista; si hay varios configs por tipo, tú decides
		// la lógica
		EffectConfig cfg = EffectConfigLoader.getConfigs(tipo).stream().findFirst()
				.orElseThrow(() -> new IllegalStateException("No hay config para " + tipo));

		return new EffectDetail(tipo, cfg.intensity, // lee intensidad del JSON
				cfg.probability, // lee probabilidad del JSON
				cfg.duration // lee duración del JSON
		);
	}

	public void aplicarA(Object objetivo) {
		List<EffectConfig> configs = EffectConfigLoader.getConfigs(tipo);
		if (configs == null || configs.isEmpty())
			return;

		for (EffectConfig config : configs) {
			boolean esPlayer = objetivo instanceof Player && "player".equalsIgnoreCase(config.getTarget());
			boolean esAttack = objetivo instanceof Attack && "attack".equalsIgnoreCase(config.getTarget());
			if (!esPlayer && !esAttack)
				continue;

			try {
			    List<Object> args = config.getArgs();
			    Class<?>[] argTypes = args.stream().map(arg -> {
			        if (arg instanceof Integer) return int.class;
			        if (arg instanceof Double) return double.class;
			        if (arg instanceof Boolean) return boolean.class;
			        return String.class;
			    }).toArray(Class<?>[]::new);

			    // Invoca el método con argumentos exactos
			    objetivo.getClass()
			            .getMethod(config.getMethod(), argTypes)
			            .invoke(objetivo, args.toArray());

			} catch (NoSuchMethodException e) {
			    System.err.println("[ERROR] Método no encontrado: " + config.getMethod() + " con argumentos " + config.getArgs());
			    e.printStackTrace();
			} catch (Exception e) {
			    System.err.println("[ERROR] Fallo al aplicar efecto " + tipo + " sobre " + config.getTarget() + ": " + e.getMessage());
			    e.printStackTrace();
			}

		}
	}

	public EmotionEffect getTipo() {
		return tipo;
	}

	public void setTipo(EmotionEffect tipo) {
		this.tipo = tipo;
	}

	public double getIntensidad() {
		return intensidad;
	}

	public void setIntensidad(double intensidad) {
		this.intensidad = intensidad;
	}

	public double getProbabilidad() {
		return probabilidad;
	}

	public void setProbabilidad(double probabilidad) {
		this.probabilidad = probabilidad;
	}

	public int getDuracionRestante() {
		return duracionRestante;
	}

	public void setDuracionRestante(int duracionRestante) {
		this.duracionRestante = duracionRestante;
	}

	public void reducirDuracion(int turnos) {
		duracionRestante = Math.max(0, duracionRestante - turnos);
	}

	public boolean haExpirado() {
		return duracionRestante <= 0;
	}

	@Override
	public String getNombre() {
		// Esto se usa como identificador en tus logs/tablas/UI
		return tipo.name();
	}

	@Override
	public int getRemainingTurns() {
		return duracionRestante;
	}
	public String getFormattedEffectInfo() {
	    EffectVisualData visual = EmotionEffectVisualRegistry.getVisualData(tipo);
	    if (visual == null) {
	        return "[ERROR] Sin datos visuales para: " + tipo;
	    }

	    return String.format(
	        "[%s]\n" +
	        " → Duración: %d turnos\n" +
	        " → Intensidad: %.2f\n" +
	        " → Sprite: %s\n" +
	        " → Color UI: %s\n" +
	        " → CSS: %s\n",
	        visual.getDisplayName(),
	        duracionRestante,
	        intensidad,
	        visual.getSpriteId(),
	        visual.getUiColorHex(),
	        visual.getCssClass()
	    );
	}

}
