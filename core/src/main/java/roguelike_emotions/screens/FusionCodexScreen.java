package roguelike_emotions.screens;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import roguelike_emotions.MainGame;
import roguelike_emotions.characters.Player;
import roguelike_emotions.fusionCodex.EmotionCard;
import roguelike_emotions.fusionCodex.FusionVisualHelpers;
import roguelike_emotions.mainMechanics.EmotionInstance;
import roguelike_emotions.mainMechanics.FusionNegotiation;
import roguelike_emotions.mainMechanics.FusionNegotiationHandler;
import roguelike_emotions.mainMechanics.SentientEmotion;
import roguelike_emotions.managers.GameManager;
import roguelike_emotions.sound.SoundManager;
import roguelike_emotions.ui.AnimationHelper;
import roguelike_emotions.ui.DialogBuilder;
import roguelike_emotions.ui.ElegantSkinFactory;
import roguelike_emotions.utils.CombatLogger;
import roguelike_emotions.utils.EmotionCombiner;
import roguelike_emotions.vfx.FusionParticleSystem;

/**
 * Pantalla de fusión de emociones - VERSIÓN 3.0 REFACTORIZADA
 * 
 * Mejoras: - Separación de responsabilidades con FusionNegotiationHandler -
 * DialogBuilder para construcción de diálogos - AnimationHelper para efectos
 * reutilizables - Métodos más pequeños y cohesivos - Eliminación de código
 * duplicado
 * 
 * @version 3.0
 */
public class FusionCodexScreen implements Screen {

	// ==================== CONSTANTS ====================

	private static final float VIEWPORT_W = 1280f;
	private static final float VIEWPORT_H = 720f;
	private static final float CARD_HEIGHT = 95f;

	private static final Color COLOR_SENTIENT_GLOW = new Color(1f, 0.85f, 0.3f, 1f);
	private static final Color COLOR_DANGER = new Color(1f, 0.3f, 0.3f, 1f);
	private static final Color COLOR_SUCCESS = new Color(0.4f, 1f, 0.5f, 1f);
	private static final Color COLOR_WARNING = new Color(1f, 0.7f, 0.2f, 1f);

	// ==================== CORE DEPENDENCIES ====================

	private final SpriteBatch batch;
	private final Stage stage;
	private final Skin skin;
	private final GameManager gm;
	private final Player player;
	private final SoundManager soundManager;
	private final FusionParticleSystem particleSystem;
	private final FusionNegotiationHandler negotiationHandler;

	// ==================== STATE ====================

	private final List<EmotionInstance> emocionesView;
	private final List<EmotionCard> cards;
	private EmotionInstance selectedA, selectedB, preview;
	private int highlightedIndex = 0;

	// ==================== UI COMPONENTS ====================

	private ScrollPane cardScroll;
	private Table cardListGroup;
	private Table slotAContainer, slotBContainer;
	private Label slotALabel, slotBLabel;
	private Image slotAIcon, slotBIcon;
	private Label previewTitle, previewMeta;
	private Image previewIcon;
	private Table previewEffectsTable, previewContentPanel;
	private TextButton btnConfirm, btnSkip, btnClearA, btnClearB, btnSwap;
	private Label lastFusionLabel;

	// ==================== CONSTRUCTOR ====================

	public FusionCodexScreen(SpriteBatch batch, com.badlogic.gdx.graphics.g2d.BitmapFont font) {
		this.batch = batch;
		this.gm = GameManager.getInstance();
		this.player = gm.getPlayer();
		this.emocionesView = new ArrayList<>(player.getEmocionesActivas());
		this.cards = new ArrayList<>();

		this.stage = new Stage(new FitViewport(VIEWPORT_W, VIEWPORT_H), batch);
		this.skin = ElegantSkinFactory.create();
		this.soundManager = SoundManager.getInstance();
		this.particleSystem = new FusionParticleSystem(VIEWPORT_W, VIEWPORT_H);
		this.negotiationHandler = new FusionNegotiationHandler(player);

		buildUI();
		wireInput();
		rebuildCards();
		updateAll();
	}

	// ==================== UI CONSTRUCTION ====================

	private void buildUI() {
		Table root = new Table();
		root.setFillParent(true);
		root.pad(VIEWPORT_W * 0.02f);
		stage.addActor(root);

		// Título
		Label title = new Label("CÓDICE DE FUSIÓN EMOCIONAL", skin, "title");
		title.setAlignment(Align.center);
		title.setColor(new Color(0.85f, 0.92f, 1f, 1f));
		AnimationHelper.pulse(title, 1.08f, 2f);
		root.add(title).expandX().fillX().colspan(3).padBottom(VIEWPORT_H * 0.03f);
		root.row();

		// Paneles principales
		Table leftPanel = buildLeftPanel();
		Table middlePanel = buildMiddlePanel();
		Table rightPanel = buildRightPanel();

		root.defaults().top().space(VIEWPORT_W * 0.013f);
		root.add(leftPanel).width(VIEWPORT_W * 0.33f).fillY().expandY();
		root.add(middlePanel).width(VIEWPORT_W * 0.27f).fillY().expandY();
		root.add(rightPanel).width(VIEWPORT_W * 0.33f).fillY().expandY();
		root.row();

		// Footer
		Table footer = buildFooter();
		root.add(footer).colspan(3).expandX().center();

		// Animaciones de entrada
		AnimationHelper.fadeInWithScale(root);
		AnimationHelper.slideInFromBottom(leftPanel, 20f);
		Gdx.app.postRunnable(() -> AnimationHelper.slideInFromBottom(middlePanel, 20f));
		Gdx.app.postRunnable(() -> AnimationHelper.slideInFromBottom(rightPanel, 20f));
	}

	private Table buildLeftPanel() {
		Table container = new Table();
		container.setBackground(FusionVisualHelpers.makeGradientPanel());
		container.pad(15f);

		Label header = new Label("EMOCIONES ACTIVAS", skin, "h2");
		header.setColor(new Color(0.75f, 0.88f, 1f, 1f));
		container.add(header).left().expandX().fillX().padBottom(10f);
		container.row();

		container.add(FusionVisualHelpers.makeGradientDivider()).height(2f).expandX().fillX().padBottom(12f);
		container.row();

		cardListGroup = new Table();
		cardListGroup.top().left();
		cardListGroup.defaults().expandX().fillX().space(8f);

		cardScroll = new ScrollPane(cardListGroup, skin, "scroll");
		cardScroll.setFadeScrollBars(false);
		cardScroll.setScrollingDisabled(true, false);
		cardScroll.setOverscroll(false, false);
		cardScroll.setSmoothScrolling(true);

		container.add(cardScroll).expand().fill();
		return container;
	}

	private Table buildMiddlePanel() {
		Table container = new Table();
		container.setBackground(FusionVisualHelpers.makeGradientPanel());
		container.pad(20f);

		Label header = new Label("FUSIÓN", skin, "h2");
		header.setColor(new Color(0.75f, 0.88f, 1f, 1f));
		header.setAlignment(Align.center);
		container.add(header).center().expandX().padBottom(12f);
		container.row();

		container.add(FusionVisualHelpers.makeGradientDivider()).height(2f).expandX().fillX().padBottom(18f);
		container.row();

		slotAContainer = buildSlot("A");
		slotAIcon = slotAContainer.findActor("icon");
		slotALabel = slotAContainer.findActor("label");
		container.add(slotAContainer).height(100f).expandX().fillX().padBottom(12f);
		container.row();

		slotBContainer = buildSlot("B");
		slotBIcon = slotBContainer.findActor("icon");
		slotBLabel = slotBContainer.findActor("label");
		container.add(slotBContainer).height(100f).expandX().fillX().padBottom(18f);
		container.row();

		Table btnRow = new Table();
		btnClearA = createSmallButton("⌫ A", "secondary");
		btnClearB = createSmallButton("⌫ B", "secondary");
		btnRow.add(btnClearA).height(40f).width(65f).padRight(6f);
		btnRow.add(btnClearB).height(40f).width(65f);
		container.add(btnRow).center().padBottom(10f);
		container.row();

		btnSwap = createSmallButton("⇅ Intercambiar", "secondary");
		container.add(btnSwap).height(42f).expandX().fillX();

		wireMiddlePanelButtons();
		return container;
	}

	private Table buildRightPanel() {
		Table container = new Table();
		container.setBackground(FusionVisualHelpers.makeGradientPanel());
		container.pad(18f);

		Label header = new Label("PREVIEW", skin, "h2");
		header.setColor(new Color(0.75f, 0.88f, 1f, 1f));
		container.add(header).left().padBottom(12f);
		container.row();

		container.add(FusionVisualHelpers.makeGradientDivider()).height(2f).expandX().fillX().padBottom(14f);
		container.row();

		previewTitle = new Label("Sin selección", skin, "h2");
		previewTitle.setWrap(true);
		previewTitle.setFontScale(1.1f);
		container.add(previewTitle).left().expandX().fillX().padBottom(6f);
		container.row();

		previewMeta = new Label("Selecciona dos emociones para fusionar.", skin, "muted");
		previewMeta.setWrap(true);
		previewMeta.setFontScale(0.85f);
		container.add(previewMeta).left().expandX().fillX().padBottom(14f);
		container.row();

		previewContentPanel = new Table();
		previewContentPanel.setBackground(FusionVisualHelpers.makeInnerPanelGradient());
		previewContentPanel.pad(16f);

		Table iconContainer = new Table();
		iconContainer.setBackground(FusionVisualHelpers.makePreviewIconBg());
		iconContainer.setName("previewIconContainer");
		previewIcon = new Image();
		iconContainer.add(previewIcon).size(80f).pad(10f);

		previewContentPanel.add(iconContainer).size(110f).top().left().padRight(14f);

		previewEffectsTable = new Table();
		previewEffectsTable.top().left();
		previewEffectsTable.defaults().left().expandX().fillX().space(6f);

		ScrollPane effectScroll = new ScrollPane(previewEffectsTable, skin, "scroll");
		effectScroll.setFadeScrollBars(false);
		effectScroll.setScrollingDisabled(true, false);

		previewContentPanel.add(effectScroll).expand().fill().top().left();
		container.add(previewContentPanel).expand().fill();

		return container;
	}

	private Table buildFooter() {
		Table footer = new Table();
		footer.padTop(VIEWPORT_H * 0.02f);

		lastFusionLabel = new Label("", skin, "muted");
		lastFusionLabel.setFontScale(0.75f);
		lastFusionLabel.setAlignment(Align.center);
		lastFusionLabel.setWrap(true);
		footer.add(lastFusionLabel).width(VIEWPORT_W * 0.6f).center().padBottom(6f);
		footer.row();

		Table btnRow = new Table();
		btnSkip = createButton("Saltar (ESC)", "secondary");
		btnConfirm = createButton("Confirmar (ENTER)", "primary");
		btnConfirm.setDisabled(true);

		btnRow.add(btnSkip).height(50f).width(200f).padRight(16f);
		btnRow.add(btnConfirm).height(50f).width(240f);
		footer.add(btnRow).center();

		AnimationHelper.fadeInWithScale(footer, AnimationHelper.SLOW);
		return footer;
	}

	private Table buildSlot(String slotName) {
		Table slot = new Table();
		slot.setBackground(FusionVisualHelpers.makeSlotGradientBg());
		slot.pad(12f);
		slot.setTransform(true);
		slot.setOrigin(Align.center);

		Table iconCircle = new Table();
		iconCircle.setBackground(FusionVisualHelpers.makeIconCircleBg());
		iconCircle.setName("iconCircle");
		iconCircle.setTransform(true);
		iconCircle.setOrigin(Align.center);

		Image icon = new Image();
		icon.setName("icon");
		iconCircle.add(icon).size(48f).pad(8f);

		slot.add(iconCircle).size(70f).left().padRight(12f);

		Table textColumn = new Table();
		textColumn.left().top();

		Label slotTitle = new Label("SLOT " + slotName, skin, "muted");
		slotTitle.setFontScale(0.75f);
		slotTitle.setColor(new Color(0.6f, 0.7f, 0.85f, 1f));
		textColumn.add(slotTitle).left().padBottom(4f);
		textColumn.row();

		Label label = new Label("Vacío ○", skin, "body");
		label.setName("label");
		label.setWrap(true);
		label.setFontScale(0.95f);
		textColumn.add(label).growX().left();

		slot.add(textColumn).expand().fill().left();
		return slot;
	}

	private TextButton createButton(String text, String style) {
		TextButton button = new TextButton(text, skin, style);
		button.addListener(new ClickListener() {
			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
				if (!button.isDisabled()) {
					soundManager.play("hover");
					AnimationHelper.onHoverEnter(button);
				}
			}

			@Override
			public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
				if (!button.isDisabled()) {
					AnimationHelper.onHoverExit(button);
				}
			}

			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int btn) {
				if (!button.isDisabled()) {
					AnimationHelper.onClick(button);
				}
				return super.touchDown(event, x, y, pointer, btn);
			}
		});
		button.setOrigin(Align.center);
		button.setTransform(true);
		return button;
	}

	private TextButton createSmallButton(String text, String style) {
		TextButton button = new TextButton(text, skin, style);
		button.addListener(new ClickListener() {
			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
				soundManager.play("hover");
				AnimationHelper.onHoverEnter(button);
			}

			@Override
			public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
				AnimationHelper.onHoverExit(button);
			}
		});
		button.setOrigin(Align.center);
		button.setTransform(true);
		return button;
	}

	// ==================== CARD MANAGEMENT ====================

	private void rebuildCards() {
		cards.clear();
		cardListGroup.clear();

		if (emocionesView.isEmpty()) {
			Label emptyMsg = new Label("No hay emociones disponibles", skin, "muted");
			emptyMsg.setAlignment(Align.center);
			emptyMsg.setFontScale(0.9f);
			cardListGroup.add(emptyMsg).center().pad(40f);
			return;
		}

		for (int i = 0; i < emocionesView.size(); i++) {
			EmotionInstance e = emocionesView.get(i);
			EmotionCard card = new EmotionCard(e, i, skin, soundManager, stage, this);
			cards.add(card);
			cardListGroup.add(card).expandX().fillX().height(CARD_HEIGHT);
			cardListGroup.row();
			card.getColor().a = 1f;
		}

		cardListGroup.invalidate();
		cardListGroup.validate();
		highlightedIndex = Math.min(highlightedIndex, Math.max(0, cards.size() - 1));
		updateHighlight();

		// Animación escalonada
		Gdx.app.postRunnable(() -> {
			for (int i = 0; i < cards.size(); i++) {
				EmotionCard card = cards.get(i);
				card.getColor().a = 0f;
				card.addAction(Actions.sequence(Actions.delay(i * 0.04f), Actions.fadeIn(0.25f, Interpolation.smooth)));
			}
		});
	}

	public void onCardClicked(EmotionCard card) {
		highlightedIndex = card.getIndex();
		updateHighlight();
		pickEmotion(card.getEmotion());
		AnimationHelper.bounce(card);
	}

	private void updateHighlight() {
		for (int i = 0; i < cards.size(); i++) {
			cards.get(i).setHighlighted(i == highlightedIndex, 1.03f);
		}

		if (highlightedIndex >= 0 && highlightedIndex < cards.size()) {
			Actor a = cards.get(highlightedIndex);
			cardScroll.scrollTo(0, a.getY(), 0, a.getHeight(), false, true);
		}
	}

	private void updateCardCompatibility() {
		if (selectedA == null && selectedB == null) {
			cards.forEach(card -> card.updateCompatibility(EmotionCard.CompatibilityState.NEUTRAL));
			return;
		}

		EmotionInstance reference = (selectedA != null) ? selectedA : selectedB;

		for (EmotionCard card : cards) {
			if (card.getEmotion() == reference) {
				card.updateCompatibility(EmotionCard.CompatibilityState.NEUTRAL);
				continue;
			}

			boolean isCompatible = checkCompatibility(reference, card.getEmotion());
			card.updateCompatibility(isCompatible ? EmotionCard.CompatibilityState.COMPATIBLE
					: EmotionCard.CompatibilityState.INCOMPATIBLE);
		}
	}

	private boolean checkCompatibility(EmotionInstance e1, EmotionInstance e2) {
		try {
			EmotionInstance test = EmotionCombiner.combinar(e1, e2);
			return test != null;
		} catch (Exception ex) {
			return false;
		}
	}

	// ==================== SELECTION & PREVIEW ====================

	private void pickEmotion(EmotionInstance e) {
		if (selectedA == null) {
			selectedA = e;
			animateSlotFill(slotAContainer, e);
			soundManager.play("fill");
			updateCardCompatibility();
		} else if (selectedB == null) {
			if (e != selectedA) {
				selectedB = e;
				animateSlotFill(slotBContainer, e);
				soundManager.play("fill");
			}
		} else {
			if (e != selectedA && e != selectedB) {
				selectedA = selectedB;
				selectedB = e;
				animateSlotFill(slotAContainer, selectedA);
				animateSlotFill(slotBContainer, selectedB);
				soundManager.play("fill");
			}
		}

		updateAll();
	}

	private void updateAll() {
		updateSlots();
		updatePreview();

		if (preview != null) {
			btnConfirm.setDisabled(false);
			AnimationHelper.pulse(btnConfirm);
		} else {
			btnConfirm.setDisabled(true);
			AnimationHelper.stopAll(btnConfirm);
		}
	}

	private void updateSlots() {
		slotALabel.setText(selectedA != null ? formatEmotion(selectedA) : "Vacío ○");
		slotBLabel.setText(selectedB != null ? formatEmotion(selectedB) : "Vacío ○");

		slotAIcon.setDrawable(selectedA != null ? selectedA.getIconDrawable() : null);
		slotBIcon.setDrawable(selectedB != null ? selectedB.getIconDrawable() : null);

		if (selectedA != null) {
			Color emotionColor = safeParseColor(selectedA.getColor());
			slotAContainer.setBackground(FusionVisualHelpers.makeSlotWithEmotionGlow(emotionColor));
		} else {
			slotAContainer.setBackground(FusionVisualHelpers.makeSlotGradientBg());
		}

		if (selectedB != null) {
			Color emotionColor = safeParseColor(selectedB.getColor());
			slotBContainer.setBackground(FusionVisualHelpers.makeSlotWithEmotionGlow(emotionColor));
		} else {
			slotBContainer.setBackground(FusionVisualHelpers.makeSlotGradientBg());
		}
	}

	private void updatePreview() {
		if (selectedA == null || selectedB == null) {
			preview = null;
			updatePreviewDisplay();
			return;
		}

		try {
			preview = EmotionCombiner.combinar(selectedA, selectedB);
		} catch (Exception ex) {
			preview = null;
		}

		updatePreviewDisplay();
	}

	private void updatePreviewDisplay() {
		if (previewEffectsTable == null || previewIcon == null)
			return;

		previewContentPanel.clearActions();
		previewContentPanel.addAction(Actions.sequence(Actions.fadeOut(0.15f, Interpolation.smooth),
				Actions.run(this::updatePreviewContent), Actions.fadeIn(0.2f, Interpolation.smooth)));
	}

	private void updatePreviewContent() {
		previewEffectsTable.clear();
		previewIcon.setDrawable(null);

		if (preview == null) {
			if (selectedA != null && selectedB != null) {
				previewTitle.setText("❌ Incompatible");
				previewTitle.setColor(new Color(1f, 0.55f, 0.50f, 1f));
				previewMeta.setText("Estas emociones no pueden fusionarse.");
				soundManager.play("error");
				AnimationHelper.shakeHorizontal(previewContentPanel);
			} else {
				previewTitle.setText("Esperando...");
				previewTitle.setColor(new Color(0.7f, 0.8f, 0.95f, 1f));
				previewMeta.setText("Selecciona dos emociones para ver el resultado.");

				Table iconContainer = previewContentPanel.findActor("previewIconContainer");
				if (iconContainer != null) {
					AnimationHelper.pulse(iconContainer);
				}
			}
			return;
		}

		// Preview válido
		String name = safe(() -> preview.getNombre(), preview.toString());
		String symbol = safe(() -> String.valueOf(preview.getSimbolo()), "?");
		String type = safe(() -> String.valueOf(preview.getTipoBase()), "");

		previewTitle.setText(name + " " + symbol);
		previewTitle.setColor(new Color(0.85f, 0.95f, 1f, 1f));

		if (preview instanceof SentientEmotion) {
			SentientEmotion sentient = (SentientEmotion) preview;
			previewMeta.setText("🧠 SENTIENTE | " + type + " | " + sentient.getPersonality().name());
			previewMeta.setColor(COLOR_SENTIENT_GLOW);
		} else {
			String color = safe(() -> preview.getColor(), "");
			previewMeta.setText(type + " | " + color);
			previewMeta.setColor(new Color(0.7f, 0.8f, 0.95f, 1f));
		}

		previewIcon.setDrawable(preview.getIconDrawable());

		Table iconContainer = previewContentPanel.findActor("previewIconContainer");
		if (iconContainer != null) {
			AnimationHelper.spin(iconContainer, 0.5f);
		}

		// Mostrar efectos y stats
		if (preview instanceof SentientEmotion) {
			addSentientStatsPanel((SentientEmotion) preview);
		}

		addEffectsList(safe(() -> preview.getEfectos(), new ArrayList<>()));
	}

	private void addSentientStatsPanel(SentientEmotion sentient) {
		Table statsPanel = new Table();
		statsPanel.setBackground(FusionVisualHelpers.makeInnerPanelGradient());
		statsPanel.pad(12f);

		// Fila 1: Loyalty y Hunger
		Table row1 = new Table();

		Label heartIcon = new Label("❤️", skin, "body");
		heartIcon.setFontScale(0.95f);
		row1.add(heartIcon).padRight(6f);

		Label loyaltyLabel = new Label(sentient.getLoyalty() + "%", skin, "body");
		loyaltyLabel.setFontScale(0.9f);
		loyaltyLabel.setColor(getLoyaltyColor(sentient.getLoyalty()));
		row1.add(loyaltyLabel).left().expandX();

		Label foodIcon = new Label("🍖", skin, "body");
		foodIcon.setFontScale(0.95f);
		row1.add(foodIcon).padRight(6f);

		Label hungerLabel = new Label(sentient.getHunger() + "%", skin, "body");
		hungerLabel.setFontScale(0.9f);
		hungerLabel.setColor(getHungerColor(sentient.getHunger()));
		row1.add(hungerLabel).right();

		statsPanel.add(row1).expandX().fillX().padBottom(8f);
		statsPanel.row();

		// Fila 2: Age y Evolution
		Table row2 = new Table();
		Label ageLabel = new Label("⏳ " + sentient.getAge() + " turnos", skin, "muted");
		ageLabel.setFontScale(0.75f);
		row2.add(ageLabel).left().expandX();

		Label evoLabel = new Label("✨ " + sentient.getEvolution() + "%", skin, "muted");
		evoLabel.setFontScale(0.75f);
		evoLabel.setColor(getEvolutionColor(sentient.getEvolution()));
		row2.add(evoLabel).right();

		statsPanel.add(row2).expandX().fillX().padBottom(8f);
		statsPanel.row();

		// Estado especial
		if (sentient.isCorrupted() || sentient.isAscended()) {
			Label stateLabel = new Label(sentient.isCorrupted() ? "💀 CORROMPIDA" : "✨ ASCENDIDA", skin, "body");
			stateLabel.setFontScale(0.85f);
			stateLabel.setColor(sentient.isCorrupted() ? COLOR_DANGER : COLOR_SUCCESS);
			stateLabel.setAlignment(Align.center);
			statsPanel.add(stateLabel).center().colspan(2);
			statsPanel.row();
		}

		AnimationHelper.fadeInWithScale(statsPanel, AnimationHelper.FAST);
		previewEffectsTable.add(statsPanel).expandX().fillX().padBottom(10f);
		previewEffectsTable.row();
	}

	private void addEffectsList(List<roguelike_emotions.effects.EffectDetail> effects) {
		int delayIndex = 0;
		for (roguelike_emotions.effects.EffectDetail ed : effects) {
			Table effectRow = new Table();
			effectRow.setBackground(FusionVisualHelpers.makeEffectRowBg());
			effectRow.pad(8f, 10f, 8f, 10f);

			Label effectLabel = new Label(ed.getTipo().toString(), skin, "body");
			effectLabel.setFontScale(0.95f);
			effectRow.add(effectLabel).left().expandX();

			String statsText = (ed.getRemainingTurns() > 0 ? ed.getRemainingTurns() + "t | " : "")
					+ String.format("%.1f", ed.getIntensidad());
			Label statsLabel = new Label(statsText, skin, "muted");
			statsLabel.setFontScale(0.85f);
			statsLabel.setColor(new Color(0.70f, 0.85f, 1f, 1f));
			effectRow.add(statsLabel).right().padLeft(8f);

			effectRow.getColor().a = 0f;
			effectRow.addAction(
					Actions.sequence(Actions.delay(delayIndex * 0.08f), Actions.fadeIn(0.2f, Interpolation.smooth)));

			previewEffectsTable.add(effectRow).expandX().fillX();
			previewEffectsTable.row();
			delayIndex++;
		}
	}

	// ==================== FUSION EXECUTION ====================

	private void confirmFusion() {
		if (preview == null)
			return;

		FusionNegotiationHandler.NegotiationResult result = negotiationHandler.processNegotiation(selectedA, selectedB);

		switch (result.getType()) {
		case DIRECT_FUSION:
		case ACCEPTED:
			executeFusion();
			break;

		case REJECTED:
			showRejectionDialog(result.getEmotionName(), result.getNegotiation1());
			break;

		case REQUIRES_COST:
			showCostDialog(result);
			break;

		case WEAKENED:
			showWeakenedDialog(result);
			break;

		case UNSTABLE:
			showUnstableDialog(result);
			break;
		}
	}

	private void executeFusion() {
		soundManager.play("success");

		Color colorA = safeParseColor(selectedA.getColor());
		Color colorB = safeParseColor(selectedB.getColor());
		particleSystem.createFusionParticles(colorA, colorB, VIEWPORT_W * 0.5f - 100f, VIEWPORT_H * 0.6f,
				VIEWPORT_W * 0.5f - 100f, VIEWPORT_H * 0.4f);

		String fusionText = safe(() -> selectedA.getNombre(), "?") + " + " + safe(() -> selectedB.getNombre(), "?")
				+ " → FUSIONANDO...";
		lastFusionLabel.setText(fusionText);
		lastFusionLabel.setColor(COLOR_SENTIENT_GLOW);

		stage.getRoot().addAction(
				Actions.sequence(Actions.delay(0.8f), Actions.parallel(Actions.fadeOut(0.3f, Interpolation.smooth),
						Actions.scaleTo(1.05f, 1.05f, 0.3f, Interpolation.smooth)), Actions.run(() -> {
							player.removeEmocion(selectedA);
							player.removeEmocion(selectedB);
							EmotionInstance fusion = EmotionCombiner.combinar(selectedA, selectedB);
							player.añadirEmocion(fusion);
							gm.getCodex().registrar(fusion);
							salirAlCombate();
						})));
	}

	// ==================== DIALOGS ====================

	private void showRejectionDialog(String emotionName, FusionNegotiation negotiation) {
		new DialogBuilder(skin, stage).icon("❌").title("FUSIÓN RECHAZADA").titleColor(COLOR_DANGER)
				.message(emotionName + "\n\n" + negotiation.getMessage()).size(480f, 300f).confirmText("Entendido")
				.onConfirm(() -> soundManager.play("select")).build();

		soundManager.play("error");
		CombatLogger.get().log(negotiation.getMessage());
	}

	private void showCostDialog(FusionNegotiationHandler.NegotiationResult result) {
		int totalHp = result.getHpCost();
		int totalSacrifice = result.getSacrificeCount();

		boolean canAfford = negotiationHandler.canAfford(totalHp, totalSacrifice, emocionesView.size() - 2);

		// Construir panel de costes
		Table costsPanel = new Table();
		costsPanel.setBackground(FusionVisualHelpers.makeInnerPanelGradient());
		costsPanel.pad(18f);

		if (totalHp > 0) {
			Table hpRow = new Table();
			Label hpIcon = new Label("💔", skin, "body");
			hpIcon.setFontScale(1.5f);
			hpRow.add(hpIcon).padRight(12f);

			Label hpText = new Label(totalHp + " HP", skin, "body");
			hpText.setFontScale(1.2f);
			hpText.setColor(COLOR_DANGER);
			hpRow.add(hpText).expandX().left();

			boolean hasHP = player.getHealth() > totalHp;
			Label hpStatus = new Label(hasHP ? "✓ Disponible" : "✗ Insuficiente", skin, "body");
			hpStatus.setFontScale(0.9f);
			hpStatus.setColor(hasHP ? COLOR_SUCCESS : COLOR_DANGER);
			hpRow.add(hpStatus).right();

			costsPanel.add(hpRow).expandX().fillX().padBottom(14f);
			costsPanel.row();
		}

		if (totalSacrifice > 0) {
			Table sacrificeRow = new Table();

			Label sacrificeIcon = new Label("🔥", skin, "body");

			sacrificeIcon.setFontScale(1.5f);
			sacrificeRow.add(sacrificeIcon).padRight(12f);

			Label sacText = new Label(totalSacrifice + " emoción(es)", skin, "body");
			sacText.setFontScale(1.2f);
			sacText.setColor(COLOR_WARNING);
			sacrificeRow.add(sacText).expandX().left();

			boolean hasSac = (emocionesView.size() - 2) >= totalSacrifice;
			Label sacStatus = new Label(hasSac ? "✓ Disponible" : "✗ Insuficiente", skin, "body");
			sacStatus.setFontScale(0.9f);
			sacStatus.setColor(hasSac ? COLOR_SUCCESS : COLOR_DANGER);
			sacrificeRow.add(sacStatus).right();

			costsPanel.add(sacrificeRow).expandX().fillX();
		}

		DialogBuilder builder = new DialogBuilder(skin, stage).icon("⚠️").title("COSTE DE FUSIÓN")
				.titleColor(COLOR_WARNING).message("Las emociones sentientes requieren un pago")
				.customContent(costsPanel).size(560f, totalSacrifice > 0 ? 450f : 380f);

		if (canAfford) {
			builder.cancelText("Cancelar").confirmText("Pagar y Fusionar").onConfirm(() -> {
				if (totalSacrifice > 0) {
					showSacrificeSelector(totalSacrifice, totalHp);
				} else {
					negotiationHandler.payCosts(totalHp, 0);
					executeFusion();
				}
			});
		} else {
			builder.confirmText("No puedes pagar");
		}

		builder.build();
	}

	private void showWeakenedDialog(FusionNegotiationHandler.NegotiationResult result) {
		FusionNegotiation n1 = result.getNegotiation1();
		FusionNegotiation n2 = result.getNegotiation2();

		double penalty = (n1 != null ? n1.getFusionMultiplier() : 1.0) * (n2 != null ? n2.getFusionMultiplier() : 1.0);
		int penaltyPercent = (int) ((1.0 - penalty) * 100);

		String message = (n1 != null ? n1.getMessage() : "") + (n2 != null ? "\n" + n2.getMessage() : "");

		Table penaltyPanel = new Table();
		penaltyPanel.setBackground(FusionVisualHelpers.makeInnerPanelGradient());
		penaltyPanel.pad(16f);

		Label penaltyLabel = new Label("Penalización: -" + penaltyPercent + "% efectividad", skin, "body");
		penaltyLabel.setFontScale(1.1f);
		penaltyLabel.setColor(COLOR_DANGER);
		penaltyLabel.setAlignment(Align.center);
		penaltyPanel.add(penaltyLabel).center();

		new DialogBuilder(skin, stage).icon("⚠️").title("FUSIÓN DEBILITADA").titleColor(COLOR_WARNING).message(message)
				.customContent(penaltyPanel).size(540f, 360f).cancelText("Cancelar").confirmText("Continuar Igual")
				.onConfirm(this::executeFusion).build();
	}

	private void showUnstableDialog(FusionNegotiationHandler.NegotiationResult result) {
		int failChance = result.getFailureChance();

		Table riskPanel = new Table();
		riskPanel.setBackground(FusionVisualHelpers.makeInnerPanelGradient());
		riskPanel.pad(16f);

		Label riskTitle = new Label("Probabilidad de fallo", skin, "muted");
		riskTitle.setFontScale(0.85f);
		riskTitle.setAlignment(Align.center);
		riskPanel.add(riskTitle).center().padBottom(8f);
		riskPanel.row();

		Label riskValue = new Label(failChance + "%", skin, "h2");
		riskValue.setFontScale(1.5f);
		riskValue.setColor(COLOR_DANGER);
		riskValue.setAlignment(Align.center);
		riskPanel.add(riskValue).center();

		new DialogBuilder(skin, stage).icon("⚡").title("FUSIÓN INESTABLE").titleColor(COLOR_WARNING)
				.message(result.getNegotiation1().getMessage() + "\n\nSi falla, la fusión tendrá 50% de efectividad")
				.customContent(riskPanel).size(540f, 380f).cancelText("Cancelar").confirmText("Arriesgar")
				.onConfirm(() -> {
					boolean failed = (Math.random() * 100) < failChance;
					if (failed) {
						CombatLogger.get().log("⚡ ¡La fusión FALLÓ! Efectividad reducida");
					}
					executeFusion();
				}).build();
	}

	private void showSacrificeSelector(int requiredCount, int hpCost) {
		Table dialog = new Table();
		dialog.setBackground(FusionVisualHelpers.makeDialogBg());
		dialog.pad(30f);
		dialog.setSize(600f, 520f);
		dialog.setPosition((VIEWPORT_W - 600f) / 2, (VIEWPORT_H - 520f) / 2);

		Label title = new Label("🔥 SACRIFICIO REQUERIDO", skin, "h2");
		title.setAlignment(Align.center);
		title.setColor(COLOR_WARNING);
		dialog.add(title).center().padBottom(16f);
		dialog.row();

		Label message = new Label("Selecciona " + requiredCount + " emoción(es) para sacrificar", skin, "body");
		message.setAlignment(Align.center);
		message.setWrap(true);
		dialog.add(message).width(520f).center().padBottom(20f);
		dialog.row();

		Table emotionList = new Table();
		emotionList.setBackground(FusionVisualHelpers.makeInnerPanelGradient());
		emotionList.pad(12f);
		emotionList.top();

		List<EmotionInstance> sacrificed = new ArrayList<>();

		for (EmotionInstance emotion : emocionesView) {
			if (emotion == selectedA || emotion == selectedB)
				continue;

			TextButton emotionBtn = new TextButton(emotion.getSimbolo() + " " + emotion.getNombre(), skin, "secondary");

			emotionBtn.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					soundManager.play("select");

					sacrificed.add(emotion);
					emotionBtn.setDisabled(true);
					emotionBtn.setColor(COLOR_DANGER);

					if (sacrificed.size() >= requiredCount) {
						// Ejecutar sacrificio
						SentientEmotion[] gluttons = new SentientEmotion[] {
								selectedA instanceof SentientEmotion ? (SentientEmotion) selectedA : null,
								selectedB instanceof SentientEmotion ? (SentientEmotion) selectedB : null };

						negotiationHandler.sacrificeEmotions(sacrificed.toArray(new EmotionInstance[0]), gluttons);

						if (hpCost > 0) {
							negotiationHandler.payCosts(hpCost, 0);
						}

						AnimationHelper.fadeOutWithScale(dialog, () -> {
							rebuildCards();
							executeFusion();
						});
					}
				}
			});

			emotionList.add(emotionBtn).expandX().fillX().height(50f).padBottom(8f);
			emotionList.row();
		}

		ScrollPane scroll = new ScrollPane(emotionList, skin);
		scroll.setFadeScrollBars(false);
		dialog.add(scroll).expand().fill().padBottom(16f);
		dialog.row();

		TextButton btnCancel = new TextButton("Cancelar", skin, "secondary");
		btnCancel.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				soundManager.play("select");
				AnimationHelper.fadeOutWithScale(dialog, () -> {
				});
			}
		});

		dialog.add(btnCancel).width(200f).height(50f).center();

		AnimationHelper.fadeInWithScale(dialog);
		stage.addActor(dialog);
	}

	// ==================== ANIMATIONS ====================

	private void animateSlotFill(Table slot, EmotionInstance emotion) {
		AnimationHelper.bounce(slot);

		Actor iconCircle = slot.findActor("iconCircle");
		if (iconCircle != null) {
			AnimationHelper.spin(iconCircle, 0.4f);
		}
	}

	// ==================== INPUT ====================

	private void wireInput() {
		btnSkip.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				showSkipConfirmation();
			}
		});

		btnConfirm.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (preview != null) {
					confirmFusion();
				}
			}
		});

		stage.addListener(new InputListener() {
			@Override
			public boolean keyDown(InputEvent event, int keycode) {
				return handleKeyInput(keycode);
			}
		});
	}

	private void wireMiddlePanelButtons() {
		btnClearA.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				soundManager.play("select");
				selectedA = null;
				updateAll();
				updateCardCompatibility();
			}
		});

		btnClearB.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				soundManager.play("select");
				selectedB = null;
				updateAll();
				updateCardCompatibility();
			}
		});

		btnSwap.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				soundManager.play("select");
				EmotionInstance tmp = selectedA;
				selectedA = selectedB;
				selectedB = tmp;
				animateSlotFill(slotAContainer, selectedA);
				animateSlotFill(slotBContainer, selectedB);
				updateAll();
			}
		});
	}

	private boolean handleKeyInput(int keycode) {
		switch (keycode) {
		case Input.Keys.ESCAPE:
			showSkipConfirmation();
			return true;

		case Input.Keys.ENTER:
			if (preview != null) {
				confirmFusion();
			}
			return true;

		case Input.Keys.UP:
			moveHighlight(-1);
			soundManager.play("hover");
			return true;

		case Input.Keys.DOWN:
			moveHighlight(1);
			soundManager.play("hover");
			return true;

		case Input.Keys.SPACE:
			if (highlightedIndex >= 0 && highlightedIndex < cards.size()) {
				pickEmotion(cards.get(highlightedIndex).getEmotion());
			}
			return true;

		case Input.Keys.TAB:
			if (selectedA != null && selectedB != null) {
				EmotionInstance tmp = selectedA;
				selectedA = selectedB;
				selectedB = tmp;
				animateSlotFill(slotAContainer, selectedA);
				animateSlotFill(slotBContainer, selectedB);
				soundManager.play("select");
				updateAll();
			}
			return true;

		case Input.Keys.C:
			selectedA = null;
			selectedB = null;
			soundManager.play("select");
			updateAll();
			updateCardCompatibility();
			return true;
		}

		// Selección numérica (1-9)
		if (keycode >= Input.Keys.NUM_1 && keycode <= Input.Keys.NUM_9) {
			int idx = keycode - Input.Keys.NUM_1;
			if (idx < cards.size()) {
				highlightedIndex = idx;
				updateHighlight();
				pickEmotion(cards.get(idx).getEmotion());
				soundManager.play("select");
			}
			return true;
		}

		return false;
	}

	private void moveHighlight(int delta) {
		if (cards.isEmpty())
			return;
		highlightedIndex = Math.max(0, Math.min(cards.size() - 1, highlightedIndex + delta));
		updateHighlight();
	}

	private void showSkipConfirmation() {
		if (selectedA == null && selectedB == null) {
			skipFusion();
			return;
		}

		new DialogBuilder(skin, stage).title("¿Saltar la fusión?").message("Perderás la selección actual")
				.size(420f, 220f).cancelText("Cancelar").confirmText("Sí, saltar").onConfirm(this::skipFusion).build();
	}

	private void skipFusion() {
		AnimationHelper.fadeOutWithScale(stage.getRoot(), this::salirAlCombate);
	}

	private void salirAlCombate() {
		MainGame game = MainGame.getInstance();
		MainGame.cambiarPantalla(new TurnCombatScreen(game.getBatch(), game.getFont()));
	}

	// ==================== UTILITIES ====================

	private String formatEmotion(EmotionInstance e) {
		if (e == null)
			return "";
		String name = safe(() -> e.getNombre(), e.toString());
		String sym = safe(() -> String.valueOf(e.getSimbolo()), "");

		if (e instanceof SentientEmotion) {
			return name + " " + sym + " 🧠";
		}
		return name + " " + sym;
	}

	private Color safeParseColor(String hexColor) {
		try {
			return Color.valueOf(hexColor);
		} catch (Exception e) {
			return Color.GRAY;
		}
	}

	private Color getLoyaltyColor(int loyalty) {
		if (loyalty >= 80)
			return new Color(0.4f, 1f, 0.5f, 1f);
		if (loyalty >= 50)
			return new Color(1f, 1f, 0.5f, 1f);
		if (loyalty >= 20)
			return new Color(1f, 0.7f, 0.3f, 1f);
		return COLOR_DANGER;
	}

	private Color getHungerColor(int hunger) {
		if (hunger >= 90)
			return COLOR_DANGER;
		if (hunger >= 60)
			return COLOR_WARNING;
		return new Color(0.7f, 0.7f, 0.7f, 1f);
	}

	private Color getEvolutionColor(int evolution) {
		if (evolution >= 80)
			return COLOR_SENTIENT_GLOW;
		if (evolution >= 50)
			return new Color(0.8f, 0.8f, 1f, 1f);
		return new Color(0.6f, 0.6f, 0.8f, 1f);
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

	// ==================== SCREEN LIFECYCLE ====================

	@Override
	public void show() {
		Gdx.input.setInputProcessor(stage);
	}

	@Override
	public void render(float delta) {
		ScreenUtils.clear(0.015f, 0.025f, 0.05f, 1f);
		particleSystem.update(delta);
		stage.act(delta);
		stage.draw();
		particleSystem.render(batch);
	}

	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height, true);
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void hide() {
	}

	@Override
	public void dispose() {
		stage.dispose();
		particleSystem.dispose();
	}
}
