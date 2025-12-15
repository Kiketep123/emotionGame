package roguelike_emotions.combat;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;

import roguelike_emotions.characters.Player;
import roguelike_emotions.mainMechanics.EmotionInstance;
import roguelike_emotions.managers.GameManager;

import java.util.ArrayList;
import java.util.List;

public class LivingEmotionPanel extends Table {

	private final Skin skin;
	private final List<EmotionOrbCard> orbCards = new ArrayList<>();
	private final Table orbContainer;

	private EmotionSelectedCallback onEmotionSelected;

	public LivingEmotionPanel(Skin skin) {
		this.skin = skin;

		// Panel más estrecho y elegante
		setBackground(createModernPanelBackground());
		pad(12f, 8f, 12f, 8f);
		align(Align.top);

		// Header más compacto
		Label header = new Label("EMOCIONES", skin, "title");
		header.setColor(new Color(0.95f, 0.95f, 1f, 1f));
		header.setFontScale(0.9f);
		header.setAlignment(Align.center);
		add(header).expandX().center().padBottom(8f).row();

		// Línea decorativa sutil
		Table line = new Table();
		line.setBackground(skin.newDrawable("white", new Color(0.4f, 0.6f, 0.8f, 0.4f)));
		add(line).height(1f).expandX().fillX().padBottom(10f).row();

		// Contenedor de orbes (SIN scrollpane, diseño vertical fijo)
		orbContainer = new Table();
		orbContainer.top();

		add(orbContainer).expand().fill();

		refreshEmotions();
	}

	public void refreshEmotions() {
		orbContainer.clear();
		orbCards.clear();

		Player player = GameManager.getInstance().getPlayer();
		if (player == null)
			return;

		List<EmotionInstance> emotions = player.getEmocionesActivas();
		if (emotions == null || emotions.isEmpty()) {
			Label emptyLabel = new Label("Sin emociones", skin, "default");
			emptyLabel.setColor(new Color(0.5f, 0.5f, 0.6f, 0.8f));
			emptyLabel.setAlignment(Align.center);
			emptyLabel.setFontScale(0.75f);
			orbContainer.add(emptyLabel).center().pad(15f);
			return;
		}

		// Mostrar máximo 3 emociones (las primeras)
		int maxToShow = Math.min(3, emotions.size());

		for (int i = 0; i < maxToShow; i++) {
			EmotionInstance emotion = emotions.get(i);
			EmotionOrbCard card = new EmotionOrbCard(emotion, skin);

			card.setOnClick(() -> {
				if (onEmotionSelected != null) {
					onEmotionSelected.onEmotionSelected(emotion);
				}
			});

			orbCards.add(card);
			orbContainer.add(card).width(200f).height(140f).padBottom(8f).row();
		}

		// Si hay más emociones, mostrar contador
		if (emotions.size() > maxToShow) {
			Label moreLabel = new Label("+" + (emotions.size() - maxToShow) + " más", skin, "default");
			moreLabel.setColor(new Color(0.6f, 0.7f, 0.9f, 0.7f));
			moreLabel.setFontScale(0.7f);
			moreLabel.setAlignment(Align.center);
			orbContainer.add(moreLabel).center().padTop(6f);
		}
	}

	public void update(float delta) {
		for (EmotionOrbCard card : orbCards) {
			card.update(delta);
		}
	}

	public void markEmotionUsed(EmotionInstance emotion, int cooldownTurns) {
		for (EmotionOrbCard card : orbCards) {
			if (card.getEmotion() == emotion) {
				card.setUsed(cooldownTurns);
				break;
			}
		}
	}

	public void tickAllCooldowns() {
		for (EmotionOrbCard card : orbCards) {
			card.tickCooldown();
		}
	}

	public void setOnEmotionSelected(EmotionSelectedCallback callback) {
		this.onEmotionSelected = callback;
	}

	private NinePatchDrawable createModernPanelBackground() {
		// Crear background con bordes redondeados y sombra
		Pixmap pixmap = new Pixmap(32, 32, Pixmap.Format.RGBA8888);

		// Relleno oscuro con transparencia
		pixmap.setColor(new Color(0.06f, 0.09f, 0.14f, 0.95f));
		pixmap.fillRectangle(4, 4, 24, 24);

		// Borde sutil
		pixmap.setColor(new Color(0.2f, 0.3f, 0.4f, 0.6f));
		pixmap.drawRectangle(4, 4, 24, 24);

		Texture texture = new Texture(pixmap);
		pixmap.dispose();

		NinePatch ninePatch = new NinePatch(texture, 8, 8, 8, 8);
		return new NinePatchDrawable(ninePatch);
	}

	@Override
	public void act(float delta) {
		super.act(delta);
		update(delta);
	}

	public void dispose() {
		// Cleanup si es necesario
	}

	@FunctionalInterface
	public interface EmotionSelectedCallback {
		void onEmotionSelected(EmotionInstance emotion);
	}
}
