package roguelike_emotions;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import roguelike_emotions.characters.Player;
import roguelike_emotions.managers.GameManager;
import roguelike_emotions.screens.TurnCombatScreen;
import roguelike_emotions.ui.fonts.FontManager;

public class MainGame extends Game {

	private static  MainGame instancia;
	private SpriteBatch batch;
	private BitmapFont font;
	private Player jugador;

	@Override
	public void create() {
		setInstancia(this);
		batch = new SpriteBatch();
		font = new BitmapFont();
		FontManager.init();
		GameManager.getInstance();
		setScreen(new TurnCombatScreen(getBatch(), getFont()));

	}


	private static void setInstancia(MainGame instancia) {
		MainGame.instancia = instancia;
	}

	@Override
	public void dispose() {
		if (getScreen() != null) {
			getScreen().dispose();
		}
		batch.dispose();
		font.dispose();
	}

	public SpriteBatch getBatch() {
		return batch;
	}

	public BitmapFont getFont() {
		return font;
	}

	public static void cambiarPantalla(com.badlogic.gdx.Screen nuevaPantalla) {
		instancia.setScreen(nuevaPantalla);
	}

	public Player getJugador() {
		return jugador;
	}
	public void restartCombat() {
	    // Reinicia el screen completo (estado limpio)
	    MainGame.cambiarPantalla(new TurnCombatScreen(getBatch(), getFont()));
	}
	public static MainGame getInstance() {
	    return instancia;
	}
}
