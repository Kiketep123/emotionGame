package roguelike_emotions.managers;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import roguelike_emotions.characters.Enemy;
import roguelike_emotions.characters.EnemyFactory;
import roguelike_emotions.characters.Player;
import roguelike_emotions.mainMechanics.EmotionCodex;
import roguelike_emotions.mainMechanics.EmotionDominanceMatrix;
import roguelike_emotions.mainMechanics.EmotionInstance;
import roguelike_emotions.mainMechanics.EmotionInstanceFactory;
import roguelike_emotions.mainMechanics.EmotionNameGenerator;
import roguelike_emotions.map.EmotionalMap;
import roguelike_emotions.utils.CombatLogger;
import roguelike_emotions.utils.EmotionCombiner;

public class GameManager {


	private static GameManager instancia;
	private BitmapFont font;
	private SpriteBatch batch;
	private Skin skin;
	private Stage stage;

	public static GameManager getInstance() {
		if (instancia == null) {
			instancia = new GameManager();
		}
		return instancia;
	}

	// ---------------------------
	// Estado del juego
	// ---------------------------

	private final EmotionDominanceMatrix dominanceMatrix = new EmotionDominanceMatrix();
	private final EmotionCodex codex = new EmotionCodex(dominanceMatrix);
	private final List<EmotionInstance> emocionesBase = new ArrayList<>();
	private final EmotionInstanceFactory factory = new EmotionInstanceFactory();
	private final Player player = new Player();
	private final EnemyFactory enemyFactory;
	private List<Enemy> enemigos = new ArrayList<>();
	private EmotionalMap mapa;

	// Constructor privado para el singleton
	private GameManager() {
		player.resetState();
		EmotionNameGenerator.resetTracking();
		EmotionCombiner.setDominanceMatrix(dominanceMatrix);
		Enemy.setDominanceMatrix(dominanceMatrix);
		initVisual();
		enemyFactory = new EnemyFactory();
	    mapa = new EmotionalMap(10, enemyFactory, dominanceMatrix);

		for (int i = 0; i < 5; i++) {
			emocionesBase.add(factory.generarProcedural());
		}
	}



	public List<EmotionInstance> getEmocionesBase() {
		return new ArrayList<>(emocionesBase);
	}

	public List<EmotionInstance> getCodexEntries() {
		return codex.getEntries();
	}

	public EmotionInstance fusionar(EmotionInstance e1, EmotionInstance e2) {
		EmotionInstance fusion = EmotionCombiner.combinar(e1, e2);
		codex.registrar(fusion);
		return fusion;
	}

	public EmotionInstance fusionarVarias(List<EmotionInstance> seleccion) {
		if (seleccion == null || seleccion.size() < 2) {
			throw new IllegalArgumentException("Debes seleccionar al menos 2 emociones para fusionar.");
		}
		EmotionInstance fusion = EmotionCombiner.combinarMultiples(seleccion);
		codex.registrar(fusion);
		return fusion;
	}

	public EmotionDominanceMatrix getDominanceMatrix() {
		return dominanceMatrix;
	}

	public void clearEmocionesBase() {
		emocionesBase.clear();
		codex.clear();
		dominanceMatrix.reset();
	}

	public void resetEmocionesBase() {
		emocionesBase.clear();
		for (int i = 0; i < 5; i++) {
			emocionesBase.add(factory.generarProcedural());
		}
	}

	public Player getPlayer() {
		return player;
	}

	public List<Enemy> crearOleada(int n) {
		enemigos = enemyFactory.generarEnemigos(n, dominanceMatrix);
		return enemigos;
	}

	public List<Enemy> getEnemigos() {
		return enemigos;
	}

	public EmotionInstanceFactory getEmotionInstanceFactory() {
		return factory;
	}

	public EmotionCodex getCodex() {
		return codex;
	}

	public EmotionalMap getMapa() {
		return mapa;
	}

	public void initVisual() {
		batch = new SpriteBatch();
		font = new BitmapFont(); // Puedes reemplazar por fuente personalizada
		skin = new Skin(Gdx.files.internal("uiskin.json")); // Requiere archivo en /assets
		stage = new Stage();
		Gdx.input.setInputProcessor(stage); // Importante para que capte clics
	}

	public BitmapFont getFont() {
		return font;
	}

	public SpriteBatch getBatch() {
		return batch;
	}

	public Skin getSkin() {
		return skin;
	}

	public Stage getStage() {
		return stage;
	}

	   /**
     * Ejecuta un turno completo de combate.
     * @param jugador El jugador activo
     * @param enemigo El enemigo activo
     * @param accionJugador "atacar", "defender", "usarEmocion"
     */
    public void ejecutarTurnoCombate(Player jugador, Enemy enemigo, String accionJugador) {
        CombatLogger logger = CombatLogger.get();

        if (jugador == null || enemigo == null || !jugador.isAlive() || !enemigo.isAlive()) {
            logger.log("[Sistema] Jugador o enemigo nulo o muertos.");
            return;
        }

        switch (accionJugador.toLowerCase()) {
        case "atacar":
            jugador.attack(enemigo);
            break;

        case "defender":
            jugador.defender(25); // Cantidad de defensa como antes
            break;

        case "usaremocion":
            List<EmotionInstance> emociones = jugador.getEmocionesActivas();
            if (!emociones.isEmpty()) {
                jugador.usarEmocion(emociones.get(0));
            } else {
                logger.log("[Jugador] No tiene emociones activas.");
            }
            break;

        default:
            logger.log("[Error] Acción no reconocida: " + accionJugador);
            return;
    }

        // Efectos por turno (buffs, debuffs, curación)
        jugador.tickTurnoEmocional();
        enemigo.tickTurnoEmocional(jugador);

        // Respuesta del enemigo si sigue vivo
        if (enemigo.isAlive()) {
            enemigo.atacar(jugador);
        }

        // Resultado del turno
        if (!jugador.isAlive()) {
            logger.log("[Fin] El jugador ha muerto.");
        } else if (!enemigo.isAlive()) {
            logger.log("[Fin] El enemigo ha sido derrotado.");
        }
    }
}
