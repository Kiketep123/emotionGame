package roguelike_emotions.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import roguelike_emotions.MainGame;
import roguelike_emotions.mainMechanics.EmotionEffect;
import roguelike_emotions.mainMechanics.EmotionInstance;
import roguelike_emotions.managers.GameManager;
import roguelike_emotions.ui.IconRegistry;
import roguelike_emotions.ui.fonts.FontManager;

/**
 * Pantalla post-combate autocontenida. - Muestra victoria/derrota - Permite
 * elegir 1 de 3 recompensas (solo selección por ahora) - ENTER / click en
 * "Siguiente combate" reinicia TurnCombatScreen
 */
public final class PostCombatScreen implements Screen {
	private static final float VIEWPORT_WIDTH = 1280f;
	private static final float VIEWPORT_HEIGHT = 720f;
	// Resultado mínimo del combate (sin acoplarte a sistemas futuros)
	private final boolean victory;
	private final int enemiesDefeated;

	// Render básico como en tus screens actuales
	private final SpriteBatch batch;
	private final OrthographicCamera camera;
	private final Viewport viewport;
	private final BitmapFont font;
	private final BitmapFont fontBig;
	private EmotionInstance rewardEmotion;
	private TextureRegion rewardIcon;
	// Textura 1x1 para paneles (creada runtime, no depende de assets)
	private final Texture pixel;

	// UI / selección
	private final RewardCard[] cards;
	private int selectedIndex = 0;
	private final InputAdapter inputAdapter;

	private final Rectangle btnNext;
	private final Vector3 tmp = new Vector3();

	// ----------------------------
	// Modelo simple de recompensas
	// ----------------------------
	private enum RewardType {
		HEAL("Curación ligera", "+10% vida (pendiente de sistema)"),
		DAMAGE("Fuerza emocional", "+1 daño base (pendiente de sistema)"),
		DEFENSE("Calma mental", "+1 defensa base (pendiente de sistema)"),
		SPEED("Impulso", "+1 velocidad (pendiente de sistema)"),
		EMOTION("Eco emocional", "Nueva emoción aleatoria (pendiente)");

		final String title;
		final String desc;

		RewardType(String title, String desc) {
			this.title = title;
			this.desc = desc;
		}
	}

	private static final class RewardCard {
		final RewardType type;
		final Rectangle bounds;

		RewardCard(RewardType type, Rectangle bounds) {
			this.type = type;
			this.bounds = bounds;
		}
	}

	public PostCombatScreen(boolean victory, int enemiesDefeated) {
		this.victory = victory;
		this.enemiesDefeated = enemiesDefeated;

		this.batch = new SpriteBatch();
		this.camera = new OrthographicCamera();
		this.viewport = new FitViewport(1280, 720, camera);

		this.font = FontManager.body();
		this.fontBig = FontManager.title();

		// pixel 1x1
		Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
		pm.setColor(1, 1, 1, 1);
		pm.fill();
		this.pixel = new Texture(pm);
		pm.dispose();
		GameManager gm = GameManager.getInstance();
		rewardEmotion = gm.getLastVictoryReward();
		rewardIcon = (rewardEmotion != null) ? IconRegistry.emotionRegion(rewardEmotion.getTipoBase()) : null;
		// Layout cards
		float w = viewport.getWorldWidth();
		float h = viewport.getWorldHeight();

		float cardW = 300;
		float cardH = 220;
		float gap = 60;
		float startX = (w - (3 * cardW + 2 * gap)) / 2f;
		float y = h * 0.30f;

		RewardType[] pool = RewardType.values();
		RewardType a = pool[(int) (Math.random() * pool.length)];
		RewardType b = pool[(int) (Math.random() * pool.length)];
		RewardType c = pool[(int) (Math.random() * pool.length)];

		this.cards = new RewardCard[] { new RewardCard(a, new Rectangle(startX, y, cardW, cardH)),
				new RewardCard(b, new Rectangle(startX + cardW + gap, y, cardW, cardH)),
				new RewardCard(c, new Rectangle(startX + 2 * (cardW + gap), y, cardW, cardH)) };

		// Botón siguiente combate
		this.btnNext = new Rectangle(w * 0.5f - 180, h * 0.08f, 360, 70);

		// Input propio
		this.inputAdapter = new InputAdapter() {
			@Override
			public boolean keyDown(int keycode) {
				switch (keycode) {
				case Input.Keys.LEFT -> selectedIndex = (selectedIndex + 2) % 3;
				case Input.Keys.RIGHT -> selectedIndex = (selectedIndex + 1) % 3;
				case Input.Keys.ENTER -> advanceToNextCombat();
				case Input.Keys.ESCAPE -> Gdx.app.exit();
				}
				return true;
			}

			@Override
			public boolean touchDown(int screenX, int screenY, int pointer, int button) {
				tmp.set(screenX, screenY, 0);
				viewport.unproject(tmp);

				float x = tmp.x, y = tmp.y;

				for (int i = 0; i < cards.length; i++) {
					if (cards[i].bounds.contains(x, y)) {
						selectedIndex = i;
						return true;
					}
				}
				if (btnNext.contains(x, y)) {
					advanceToNextCombat();
					return true;
				}
				return false;
			}
		};
	}

	private void advanceToNextCombat() {

		GameManager gm = GameManager.getInstance();

		if (victory) {
			gm.prepareNextCombat();
			gm.grantVictoryEmotionReward();
			if (gm.getCodexEntries().size() >= 2) {
				MainGame.cambiarPantalla(
						new FusionCodexScreen(MainGame.getInstance().getBatch(), MainGame.getInstance().getFont()));
			}
			return;
		} else {
			gm.reset();
		}

		MainGame game = MainGame.getInstance();
		MainGame.cambiarPantalla(new TurnCombatScreen(game.getBatch(), game.getFont()));
	}

	@Override
	public void show() {
		Gdx.input.setInputProcessor(inputAdapter);

	}

	@Override
	public void render(float delta) {

		Gdx.gl.glClearColor(0.05f, 0.05f, 0.07f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		viewport.apply();
		batch.setProjectionMatrix(camera.combined);

		float w = viewport.getWorldWidth();
		float h = viewport.getWorldHeight();

		batch.begin();

		// Fondo
		batch.setColor(0, 0, 0, 0.85f);
		batch.draw(pixel, 0, 0, w, h);
		batch.setColor(1, 1, 1, 1);

		// Título
		String title = victory ? "VICTORIA" : "DERROTA";
		fontBig.draw(batch, title, 0, h * 0.86f, w, Align.center, false);

		// Stats combate
		font.draw(batch, "Enemigos derrotados: " + enemiesDefeated, w * 0.1f, h * 0.70f);

		// Subtítulo recompensa
		font.draw(batch, "Elige una recompensa:", 0, h * 0.62f, w, Align.center, false);

		// Cards
		for (int i = 0; i < cards.length; i++) {
			drawCard(cards[i], i == selectedIndex);
		}

		// Botón siguiente combate
		drawButton(btnNext, "Siguiente combate [ENTER]");
		float centerX = VIEWPORT_WIDTH / 2f;
		float topY = VIEWPORT_HEIGHT - 180f;

		// Icono grande de recompensa
		if (rewardIcon != null) {
		    float size = 96f;
		    batch.setColor(1,1,1,1);
		    batch.draw(rewardIcon,
		            centerX - size/2f,
		            topY,
		            size, size);
		}

		// Texto recompensa
		if (rewardEmotion != null) {
		    String name = rewardEmotion.getNombre() + " " + rewardEmotion.getSimbolo();
		    font.draw(batch, "RECOMPENSA OBTENIDA", centerX - 110, topY - 20);
		    font.draw(batch, name, centerX - 110, topY - 55);
		    font.draw(batch, "Pulsa ENTER para continuar", centerX - 140, 120);
		} else {
		    font.draw(batch, "RECOMPENSA OBTENIDA", centerX - 110, topY - 20);
		    font.draw(batch, "(no disponible)", centerX - 80, topY - 55);
		    font.draw(batch, "Pulsa ENTER para continuar", centerX - 140, 120);
		}

		batch.end();
	}

	private void drawCard(RewardCard card, boolean selected) {
		Rectangle r = card.bounds;

		// panel
		batch.setColor(selected ? 0.20f : 0.12f, selected ? 0.50f : 0.12f, selected ? 0.90f : 0.18f,
				selected ? 0.9f : 0.8f);
		batch.draw(pixel, r.x, r.y, r.width, r.height);
		batch.setColor(1, 1, 1, 1);

		// borde simple
		batch.setColor(1, 1, 1, selected ? 0.9f : 0.25f);
		batch.draw(pixel, r.x, r.y, r.width, 2);
		batch.draw(pixel, r.x, r.y + r.height - 2, r.width, 2);
		batch.draw(pixel, r.x, r.y, 2, r.height);
		batch.draw(pixel, r.x + r.width - 2, r.y, 2, r.height);
		batch.setColor(1, 1, 1, 1);

		// texto
		font.draw(batch, card.type.title, r.x + 16, r.y + r.height - 20);
		font.draw(batch, card.type.desc, r.x + 16, r.y + r.height - 60, r.width - 32, Align.left, true);

		if (selected) {
			font.draw(batch, "Seleccionada", r.x + 16, r.y + 24);
		}
		TextureRegion icon = null;

		switch (card.type) {
		case HEAL:
			icon = IconRegistry.effectRegion(EmotionEffect.CURACION);
			break;
		case DEFENSE:
			icon = IconRegistry.effectRegion(EmotionEffect.DEFENDER);
			break;
		case SPEED:
			icon = IconRegistry.effectRegion(EmotionEffect.ENERGIZAR);
			break;
		case DAMAGE:
			// no tienes EmotionEffect “DAÑO” directo -> usa uno neutro si quieres
			icon = IconRegistry.smallRegion("turn_icon");
			break;
		case EMOTION:
			// si quieres mostrar la emoción reward real:
//		        if (rewardEmotion != null) { // tu variable de recompensa
//		            icon = IconRegistry.emotionRegion(rewardEmotion.getTipoBase());
//		        }
			break;
		}

		if (icon != null) {
			float size = 64f;
			batch.setColor(1, 1, 1, 1);
			batch.draw(icon, r.x + r.width / 2f - size / 2f, r.y + r.height - size - 18f, size, size);
		}
	}

	private void drawButton(Rectangle r, String label) {
		batch.setColor(0.15f, 0.15f, 0.18f, 1f);
		batch.draw(pixel, r.x, r.y, r.width, r.height);

		batch.setColor(1, 1, 1, 0.7f);
		batch.draw(pixel, r.x, r.y, r.width, 2);
		batch.draw(pixel, r.x, r.y + r.height - 2, r.width, 2);
		batch.draw(pixel, r.x, r.y, 2, r.height);
		batch.draw(pixel, r.x + r.width - 2, r.y, 2, r.height);

		batch.setColor(1, 1, 1, 1f);
		font.draw(batch, label, r.x, r.y + r.height * 0.65f, r.width, Align.center, false);
	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height, true);
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
		batch.dispose();
		font.dispose();
		fontBig.dispose();
		pixel.dispose();
	}

	public InputAdapter getInputAdapter() {
		return inputAdapter;
	}
}
