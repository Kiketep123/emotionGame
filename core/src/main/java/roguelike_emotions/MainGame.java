package roguelike_emotions;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import roguelike_emotions.characters.Player;
import roguelike_emotions.managers.GameManager;

public class MainGame extends Game {

	private static MainGame instancia; // acceso global controlado
	private SpriteBatch batch;
	private BitmapFont font;
	private Player jugador;

	@Override
	public void create() {
		batch = new SpriteBatch();
		font = new BitmapFont();

		// Inicializar el jugador
		GameManager.getInstance();
		setScreen(new roguelike_emotions.screens.TurnCombatScreen(getBatch(), getFont()));

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

}
