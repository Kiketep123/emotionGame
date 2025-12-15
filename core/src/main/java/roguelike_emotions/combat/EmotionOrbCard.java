package roguelike_emotions.combat;

import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;

import roguelike_emotions.effects.EffectDetail;
import roguelike_emotions.mainMechanics.EmotionInstance;
import roguelike_emotions.mainMechanics.EmotionType;
import roguelike_emotions.mainMechanics.SentientEmotion;

/**
 * 🎨 EmotionOrbCard v2.0 - CON SISTEMA COMPLETO DE DESPERTAR Tarjeta visual
 * para emociones con animaciones, progreso y estado
 */
public class EmotionOrbCard extends Table {

	private final EmotionInstance emotion;
	private final Skin skin;

	// Visual state
	private boolean isReady = true;
	private int cooldownTurns = 0;
	private float breathePhase = MathUtils.random(MathUtils.PI2);
	private float glowIntensity = 0f;
	private float hoverScale = 1f;
	private boolean isHovered = false;

	private Runnable onClickCallback;
	private Container<Label> iconContainer;

	// 🆕 Componentes de progreso de despertar
	private Table progressBarContainer;
	private Table progressBarFill;
	private Label progressLabel;

	// 🆕 Label del icono para animaciones
	private Label iconLabel;

	public EmotionOrbCard(EmotionInstance emotion, Skin skin) {
		this.emotion = emotion;
		this.skin = skin;

		setBackground(createCardBackground());
		pad(12f);

		buildLayout();
		setupInteraction();
	}

	private void buildLayout() {
		// Icono principal
		iconLabel = new Label(emotion.getSimbolo(), skin, "title");
		iconLabel.setFontScale(2.0f);
		iconLabel.setAlignment(Align.center);

		iconContainer = new Container<>(iconLabel);
		iconContainer.size(60f, 60f);
		iconContainer.align(Align.center);

		add(iconContainer).size(60f, 60f).padRight(10f);

		// Columna de info
		Table infoColumn = new Table();
		infoColumn.left().top();

		// Nombre
		Label nameLabel = new Label(emotion.getNombre(), skin, "default");
		nameLabel.setFontScale(0.9f);
		nameLabel.setWrap(true);
		nameLabel.setAlignment(Align.left);
		infoColumn.add(nameLabel).left().width(150f).padBottom(4f);
		infoColumn.row();

		// Tipo
		String tipo = emotion.getTipoBase().name().toLowerCase();
		Label typeLabel = new Label(tipo, skin, "default");
		typeLabel.setFontScale(0.7f);
		typeLabel.setColor(getTypeColor(emotion.getTipoBase()));
		infoColumn.add(typeLabel).left().padBottom(4f);
		infoColumn.row();

		// 🆕 INDICADOR DE DESPERTAR
		if (!(emotion instanceof SentientEmotion)) {
			addAwakeningProgressIndicator(infoColumn);
		} else {
			addSentientIndicator(infoColumn);
		}

		// Efectos
		addEffectsRow(infoColumn);

		add(infoColumn).expand().fill().left();
	}

	// ==================== 🆕 SISTEMA DE PROGRESO DE DESPERTAR ====================

	/**
	 * 🆕 Añade barra de progreso de despertar
	 */
	private void addAwakeningProgressIndicator(Table parent) {
		int progress = emotion.getAwakeningProgress();

		if (progress < 10) {
			// No mostrar nada si el progreso es muy bajo
			return;
		}

		Table container = new Table();
		container.left();

		// Barra de progreso
		progressBarContainer = new Table();
		progressBarContainer.setBackground(createProgressBackground());

		progressBarFill = new Table();
		progressBarFill.setBackground(createProgressFill(progress));

		float width = 140f * (progress / 100f);
		progressBarContainer.add(progressBarFill).width(width).height(6f).left();

		container.add(progressBarContainer).width(140f).height(6f).left().padRight(6f);

		// Label de progreso
		progressLabel = new Label(progress + "%", skin, "default");
		progressLabel.setFontScale(0.65f);

		Color labelColor = getProgressColor(progress);
		progressLabel.setColor(labelColor);

		container.add(progressLabel).left();

		parent.add(container).left().padBottom(2f);
		parent.row();

		// Texto de estado
		if (progress >= 80) {
			Label statusLabel = new Label("✨ Cerca del despertar", skin, "default");
			statusLabel.setFontScale(0.6f);
			statusLabel.setColor(new Color(1f, 0.85f, 0.3f, 1f));
			parent.add(statusLabel).left().padBottom(2f);
			parent.row();
		} else if (progress >= 50) {
			Label statusLabel = new Label("💭 Evolucionando", skin, "default");
			statusLabel.setFontScale(0.6f);
			statusLabel.setColor(new Color(0.7f, 0.7f, 1f, 1f));
			parent.add(statusLabel).left().padBottom(2f);
			parent.row();
		}
	}

	/**
	 * 🆕 Indicador para emociones ya sentientes
	 */
	private void addSentientIndicator(Table parent) {
		SentientEmotion sentient = (SentientEmotion) emotion;

		Table sentientInfo = new Table();
		sentientInfo.left();

		// Icono sentiente
		Label sentientIcon = new Label("🧠", skin, "default");
		sentientIcon.setFontScale(0.8f);
		sentientInfo.add(sentientIcon).padRight(4f);

		// Stats
		Label statsLabel = new Label(
				String.format("❤️ %d%% | 🍖 %d%%", sentient.getLoyalty(), 100 - sentient.getHunger()), skin, "default");
		statsLabel.setFontScale(0.65f);
		statsLabel.setColor(new Color(1f, 0.85f, 0.3f, 1f));
		sentientInfo.add(statsLabel).left();

		parent.add(sentientInfo).left().padBottom(2f);
		parent.row();

		// Personalidad
		Label personalityLabel = new Label(sentient.getPersonality().name(), skin, "default");
		personalityLabel.setFontScale(0.6f);
		personalityLabel.setColor(new Color(0.9f, 0.7f, 1f, 1f));
		parent.add(personalityLabel).left().padBottom(2f);
		parent.row();
	}

	/**
	 * 🆕 Color según progreso
	 */
	private Color getProgressColor(int progress) {
		if (progress >= 80)
			return new Color(1f, 0.85f, 0.3f, 1f); // Dorado
		if (progress >= 50)
			return new Color(0.7f, 0.7f, 1f, 1f); // Azul claro
		if (progress >= 25)
			return new Color(0.5f, 1f, 0.5f, 1f); // Verde
		return new Color(0.7f, 0.7f, 0.7f, 1f); // Gris
	}

	/**
	 * 🆕 Actualiza la barra de progreso (llamar después de cada turno)
	 */
	public void updateAwakeningProgress() {
		if (emotion instanceof SentientEmotion || progressBarFill == null) {
			return;
		}

		int newProgress = emotion.getAwakeningProgress();

		if (newProgress < 10) {
			// Ocultar barra si el progreso es muy bajo
			if (progressBarContainer != null) {
				progressBarContainer.setVisible(false);
			}
			return;
		}

		// Animar cambio de tamaño
		float targetWidth = 140f * (newProgress / 100f);
		progressBarFill.clearActions();
		progressBarFill.addAction(Actions.sizeTo(targetWidth, 6f, 0.3f, Interpolation.smooth));

		// Actualizar label
		if (progressLabel != null) {
			progressLabel.setText(newProgress + "%");
			progressLabel.setColor(getProgressColor(newProgress));
		}
	}

	// ==================== 🆕 MÉTODOS DE ANIMACIÓN Y ESTADO ====================

	/**
	 * 🆕 Actualiza animaciones (respiración, glow, etc.)
	 */
	public void update(float delta) {
		// Animación de respiración
		breathePhase += delta * 2f;
		float breathe = MathUtils.sin(breathePhase) * 0.03f + 1f;

		if (iconContainer != null) {
			iconContainer.setScale(breathe);
		}

		// Glow si está cerca de despertar
		if (!(emotion instanceof SentientEmotion)) {
			int progress = emotion.getAwakeningProgress();
			if (progress >= 70) {
				glowIntensity = MathUtils.sin(breathePhase * 1.5f) * 0.3f + 0.7f;

				Color glowColor = new Color(1f, 0.85f, 0.3f, glowIntensity * 0.5f);
				if (iconLabel != null) {
					iconLabel.setColor(glowColor);
				}
			} else {
				// Color normal
				if (iconLabel != null) {
					iconLabel.setColor(Color.WHITE);
				}
			}
		} else {
			// Glow sentiente (más intenso)
			glowIntensity = MathUtils.sin(breathePhase * 2f) * 0.2f + 0.8f;
			Color sentientGlow = new Color(1f, 0.7f, 1f, glowIntensity);
			if (iconLabel != null) {
				iconLabel.setColor(sentientGlow);
			}
		}
	}

	/**
	 * 🆕 Marca como usada (para cooldown visual)
	 */
	public void setUsed(int cooldownTurns) {
		this.cooldownTurns = cooldownTurns;
		this.isReady = false;

		setColor(new Color(0.5f, 0.5f, 0.5f, 0.7f));

		// Animación de "agotamiento"
		clearActions();
		addAction(Actions.sequence(Actions.alpha(0.5f, 0.2f, Interpolation.smooth), Actions.delay(0.5f),
				Actions.alpha(1f, 0.3f, Interpolation.smooth)));
	}

	/**
	 * 🆕 Reduce cooldown en 1 turno
	 */
	public void tickCooldown() {
		if (cooldownTurns > 0) {
			cooldownTurns--;

			if (cooldownTurns <= 0) {
				isReady = true;
				setColor(Color.WHITE);

				// Animación de "recarga completa"
				clearActions();
				addAction(Actions.sequence(Actions.scaleTo(1.1f, 1.1f, 0.15f, Interpolation.smooth),
						Actions.scaleTo(1f, 1f, 0.15f, Interpolation.elasticOut)));
			}
		}

		// 🆕 Actualizar barra de progreso de despertar
		updateAwakeningProgress();
	}

	/**
	 * 🆕 Obtiene el cooldown actual
	 */
	public int getCooldownTurns() {
		return cooldownTurns;
	}

	// ==================== EFECTOS ====================

	private void addEffectsRow(Table parent) {
		List<EffectDetail> efectos = emotion.getEfectos();
		if (efectos.isEmpty()) {
			return;
		}

		Table effectsTable = new Table();
		effectsTable.left();

		int maxEffects = Math.min(3, efectos.size());
		for (int i = 0; i < maxEffects; i++) {
			EffectDetail efecto = efectos.get(i);

			Label effectLabel = new Label(efecto.getTipo().name().substring(0, 3), skin, "default");
			effectLabel.setFontScale(0.6f);
			effectLabel.setColor(new Color(0.8f, 0.9f, 1f, 0.8f));

			effectsTable.add(effectLabel).padRight(4f);
		}

		if (efectos.size() > 3) {
			Label moreLabel = new Label("+" + (efectos.size() - 3), skin, "default");
			moreLabel.setFontScale(0.6f);
			moreLabel.setColor(new Color(0.7f, 0.7f, 0.7f, 1f));
			effectsTable.add(moreLabel);
		}

		parent.add(effectsTable).left();
		parent.row();
	}

	// ==================== INTERACCIÓN ====================

	private void setupInteraction() {
		addListener(new ClickListener() {
			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
				if (!isReady)
					return;

				isHovered = true;
				clearActions();
				addAction(Actions.sequence(Actions.scaleTo(1.05f, 1.05f, 0.15f, Interpolation.smooth)));
			}

			@Override
			public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
				isHovered = false;
				clearActions();
				addAction(Actions.sequence(Actions.scaleTo(1f, 1f, 0.15f, Interpolation.smooth)));
			}

			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (!isReady)
					return;

				if (onClickCallback != null) {
					onClickCallback.run();
				}

				// Animación de click
				clearActions();
				addAction(Actions.sequence(Actions.scaleTo(0.95f, 0.95f, 0.08f, Interpolation.smooth),
						Actions.scaleTo(1f, 1f, 0.12f, Interpolation.elasticOut)));
			}
		});

		setOrigin(Align.center);
		setTransform(true);
	}

	// ==================== VISUAL ====================

	private NinePatchDrawable createCardBackground() {
		Pixmap pixmap = new Pixmap(3, 3, Pixmap.Format.RGBA8888);

		Color baseColor = getTypeColor(emotion.getTipoBase());
		Color darkened = new Color(baseColor.r * 0.3f, baseColor.g * 0.3f, baseColor.b * 0.3f, 0.85f);

		pixmap.setColor(darkened);
		pixmap.fill();

		Texture texture = new Texture(pixmap);
		pixmap.dispose();

		NinePatch patch = new NinePatch(texture, 1, 1, 1, 1);
		return new NinePatchDrawable(patch);
	}

	private NinePatchDrawable createProgressBackground() {
		Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
		pixmap.setColor(new Color(0.2f, 0.2f, 0.2f, 0.6f));
		pixmap.fill();

		Texture texture = new Texture(pixmap);
		pixmap.dispose();

		return new NinePatchDrawable(new NinePatch(texture, 0, 0, 0, 0));
	}

	private NinePatchDrawable createProgressFill(int progress) {
		Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
		pixmap.setColor(getProgressColor(progress));
		pixmap.fill();

		Texture texture = new Texture(pixmap);
		pixmap.dispose();

		return new NinePatchDrawable(new NinePatch(texture, 0, 0, 0, 0));
	}

	private Color getTypeColor(EmotionType type) {
		return switch (type) {
		case IRA -> new Color(0.9f, 0.2f, 0.2f, 1f);
		case TRISTEZA -> new Color(0.3f, 0.4f, 0.8f, 1f);
		case ALEGRIA -> new Color(1f, 0.9f, 0.2f, 1f);
		case MIEDO -> new Color(0.5f, 0.3f, 0.6f, 1f);
		case CALMA -> new Color(0.4f, 0.8f, 0.7f, 1f);
		case RABIA -> new Color(0.8f, 0.1f, 0.1f, 1f);
		case ESPERANZA -> new Color(0.3f, 0.9f, 0.5f, 1f);
		case CULPA -> new Color(0.6f, 0.5f, 0.3f, 1f);
		default -> new Color(0.6f, 0.6f, 0.6f, 1f);
		};
	}

	// ==================== GETTERS/SETTERS ====================

	public void setOnClick(Runnable callback) {
		this.onClickCallback = callback;
	}

	public EmotionInstance getEmotion() {
		return emotion;
	}

	public boolean isReady() {
		return isReady;
	}

	public void setReady(boolean ready) {
		this.isReady = ready;
		setColor(ready ? Color.WHITE : new Color(0.5f, 0.5f, 0.5f, 0.7f));
	}

	public void setCooldown(int turns) {
		this.cooldownTurns = turns;
		this.isReady = turns <= 0;
		setReady(isReady);
	}
}
