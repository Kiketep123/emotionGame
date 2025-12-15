package roguelike_emotions.fusionCodex;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;

import roguelike_emotions.effects.EffectDetail;
import roguelike_emotions.mainMechanics.EmotionInstance;
import roguelike_emotions.screens.FusionCodexScreen;
import roguelike_emotions.sound.SoundManager;

public class EmotionCard extends Table {

	private static final float ANIM_DURATION_FAST = 0.15f;

	private final EmotionInstance emotion;
	private final int index;
	private final Skin skin;
	private final SoundManager soundManager;
	private final Stage stage;
	private final FusionCodexScreen parentScreen;

	private Table tooltipContainer;

	public EmotionCard(EmotionInstance emotion, int index, Skin skin, SoundManager soundManager, Stage stage,
			FusionCodexScreen parentScreen) {
		this.emotion = emotion;
		this.index = index;
		this.skin = skin;
		this.soundManager = soundManager;
		this.stage = stage;
		this.parentScreen = parentScreen;

		setBackground(EmotionCardRenderer.makeCardGradient(emotion, getEmotionColor(), false));
		pad(10f);
		defaults().space(8f);
		setTransform(true);
		setOrigin(Align.center);
		left();

		buildCardContent();
		setupListeners();
	}

	private void buildCardContent() {
		String name = safe(() -> emotion.getNombre(), emotion.toString());
		String type = safe(() -> String.valueOf(emotion.getTipoBase()), "");
		String symbol = safe(() -> String.valueOf(emotion.getSimbolo()), "");

		Drawable emoIcon = emotion.getIconDrawable();
		Table iconContainer = new Table();
		iconContainer.setBackground(EmotionCardRenderer.makeCardIconBg(emotion, getEmotionColor()));
		iconContainer.setTransform(true);
		iconContainer.setOrigin(Align.center);
		iconContainer.setName("iconContainer");

		Image icon = (emoIcon != null) ? new Image(emoIcon) : new Image();
		iconContainer.add(icon).size(40).pad(8);
		add(iconContainer).size(60).left();

		Table textCol = new Table();
		textCol.left().top();

		Label nameLabel = new Label(name, skin, "cardTitle");
		nameLabel.setWrap(true);
		nameLabel.setEllipsis(true);
		nameLabel.setFontScale(0.95f);
		textCol.add(nameLabel).expandX().fillX().left().padBottom(3f);
		textCol.row();

		Label typeLabel = new Label(type + " " + symbol, skin, "muted");
		typeLabel.setFontScale(0.75f);
		typeLabel.setColor(new Color(0.65f, 0.75f, 0.9f, 1f));
		typeLabel.setWrap(true);
		textCol.add(typeLabel).expandX().fillX().left();

		add(textCol).expand().fillX().left().padRight(8f);
	}

	private void setupListeners() {
		addListener(new ClickListener() {
			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
				soundManager.play("hover");
				setBackground(EmotionCardRenderer.makeCardGradient(emotion, getEmotionColor(), true));
				clearActions();
				addAction(Actions.scaleTo(1.02f, 1.02f, ANIM_DURATION_FAST, Interpolation.smooth));

				Table ic = (Table) findActor("iconContainer");
				if (ic != null) {
					ic.clearActions();
					ic.addAction(Actions.sequence(Actions.rotateTo(5f, 0.1f, Interpolation.smooth),
							Actions.rotateTo(-5f, 0.2f, Interpolation.smooth),
							Actions.rotateTo(0f, 0.1f, Interpolation.smooth)));
				}

				showTooltip();
			}

			@Override
			public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
				setBackground(EmotionCardRenderer.makeCardGradient(emotion, getEmotionColor(), false));
				clearActions();
				addAction(Actions.scaleTo(1f, 1f, ANIM_DURATION_FAST, Interpolation.smooth));
				hideTooltip();
			}

			@Override
			public void clicked(InputEvent event, float x, float y) {
				soundManager.play("select");
				parentScreen.onCardClicked(EmotionCard.this);
			}
		});
	}

	private void showTooltip() {
		if (tooltipContainer != null)
			return;

		List<EffectDetail> effects = safe(() -> emotion.getEfectos(), new ArrayList<>());
		if (effects.isEmpty())
			return;

		StringBuilder tooltipText = new StringBuilder();
		for (int i = 0; i < Math.min(3, effects.size()); i++) {
			EffectDetail ed = effects.get(i);
			tooltipText.append("• ").append(ed.getTipo());
			if (i < Math.min(3, effects.size()) - 1)
				tooltipText.append("\n");
		}
		if (effects.size() > 3)
			tooltipText.append("\n+").append(effects.size() - 3).append(" más...");

		tooltipContainer = new Table();
		tooltipContainer.setBackground(FusionVisualHelpers.makeTooltipBg());
		tooltipContainer.pad(8f, 10f, 8f, 10f);

		Label tooltipLabel = new Label(tooltipText.toString(), skin, "muted");
		tooltipLabel.setFontScale(0.8f);
		tooltipLabel.setAlignment(Align.left);
		tooltipContainer.add(tooltipLabel);

		// Posicionar a la derecha de la card usando coordenadas del stage
		Vector2 stageCoords = localToStageCoordinates(new Vector2(getWidth(), getHeight() / 2));
		tooltipContainer.setPosition(stageCoords.x + 15f, stageCoords.y - tooltipContainer.getPrefHeight() / 2);

		// Asegurar que no salga de la pantalla
		if (tooltipContainer.getX() + tooltipContainer.getPrefWidth() > stage.getWidth()) {
			// Si sale por la derecha, ponerlo a la izquierda
			Vector2 leftCoords = localToStageCoordinates(new Vector2(0, getHeight() / 2));
			tooltipContainer.setPosition(leftCoords.x - tooltipContainer.getPrefWidth() - 15f,
					leftCoords.y - tooltipContainer.getPrefHeight() / 2);
		}

		stage.addActor(tooltipContainer);

		tooltipContainer.getColor().a = 0f;
		tooltipContainer.addAction(Actions.fadeIn(0.15f, Interpolation.smooth));
	}

	private void hideTooltip() {
		if (tooltipContainer != null) {
			tooltipContainer.addAction(Actions.sequence(Actions.fadeOut(0.1f), Actions.removeActor()));
			tooltipContainer = null;
		}
	}

	public void setHighlighted(boolean highlighted, float selectScale) {
		clearActions();

		if (highlighted) {
			setBackground(EmotionCardRenderer.makeCardGradient(emotion, getEmotionColor(), true));
			addAction(Actions.scaleTo(selectScale, selectScale, 0.25f, Interpolation.smooth));

			addAction(Actions.forever(
					Actions.sequence(Actions.scaleTo(selectScale + 0.01f, selectScale + 0.01f, 1f, Interpolation.sine),
							Actions.scaleTo(selectScale, selectScale, 1f, Interpolation.sine))));
		} else {
			setBackground(EmotionCardRenderer.makeCardGradient(emotion, getEmotionColor(), false));
			addAction(Actions.scaleTo(1f, 1f, 0.25f, Interpolation.smooth));
		}
	}

	public void updateCompatibility(CompatibilityState state) {
		switch (state) {
		case COMPATIBLE:
			setBackground(EmotionCardRenderer.makeCardGradientCompatible(emotion, getEmotionColor(), true));
			break;
		case INCOMPATIBLE:
			setBackground(EmotionCardRenderer.makeCardGradientCompatible(emotion, getEmotionColor(), false));
			break;
		case NEUTRAL:
		default:
			setBackground(EmotionCardRenderer.makeCardGradient(emotion, getEmotionColor(), false));
			break;
		}
	}

	private Color getEmotionColor() {
		return safe(() -> Color.valueOf(emotion.getColor()), new Color(0.3f, 0.4f, 0.6f, 1f));
	}

	public EmotionInstance getEmotion() {
		return emotion;
	}

	public int getIndex() {
		return index;
	}

	private <T> T safe(SupplierWithException<T> s, T fallback) {
		try {
			return s.get();
		} catch (Exception ex) {
			return fallback;
		}
	}

	@FunctionalInterface
	private interface SupplierWithException<T> {
		T get() throws Exception;
	}

	public enum CompatibilityState {
		COMPATIBLE, INCOMPATIBLE, NEUTRAL
	}
}
