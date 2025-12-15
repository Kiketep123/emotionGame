package roguelike_emotions.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
import roguelike_emotions.managers.CombatManager.CombatResult;
import roguelike_emotions.managers.CombatManager.PlayerAction;
import roguelike_emotions.ui.ElegantSkinFactory;
import roguelike_emotions.utils.EmotionCombiner;

/**
 * Gestor principal del juego - Coordinador de subsistemas. Responsabilidad
 * única: Orquestar los diferentes managers y mantener el estado del juego.
 */
public class GameManager {

	private static GameManager instance;

	// Subsistemas especializados
	private final EmotionManager emotionManager;
	private final CombatManager combatManager;
	private final VisualManager visualManager;
	private final WaveManager waveManager;
	private EmotionInstance lastVictoryReward;

	public EmotionInstance getLastVictoryReward() {
		return lastVictoryReward;
	}

	// Estado del juego
	private final GameState gameState;

	private GameManager() {
		instance = this;
		this.gameState = new GameState();
		this.emotionManager = new EmotionManager(gameState);
		this.combatManager = new CombatManager();
		this.waveManager = new WaveManager(gameState);

		this.visualManager = new VisualManager();
		initialize();
	}

	public static GameManager getInstance() {
		if (instance == null) {
			instance = new GameManager();
		}
		return instance;
	}

	/**
	 * Reinicia completamente el estado del juego
	 */
	public void reset() {
		gameState.reset();
		emotionManager.reset();
		waveManager.createWave(1 + new Random().nextInt(3));
	}

	/**
	 * Prepara el siguiente combate sin reiniciar el run. Mantiene emociones activas
	 * y códice.
	 */
	public void prepareNextCombat() {
		// Reset solo de combate
		gameState.getPlayer().resetCombatState();

		// Nueva oleada
		waveManager.createWave(1 + new Random().nextInt(1));
	}

	private void initialize() {
		gameState.getPlayer().resetState();
		EmotionNameGenerator.resetTracking();
		waveManager.createWave(1 + new Random().nextInt(1));
		emotionManager.generateInitialEmotions(1);
	}

	// ========== Delegación a Emotion Manager ==========

	public List<EmotionInstance> getBaseEmotions() {
		return emotionManager.getBaseEmotions();
	}

	public List<EmotionInstance> getCodexEntries() {
		return emotionManager.getCodexEntries();
	}

	public EmotionInstance fuseEmotions(EmotionInstance e1, EmotionInstance e2) {
		return emotionManager.fuse(e1, e2);
	}

	public EmotionInstance fuseMultipleEmotions(List<EmotionInstance> emotions) {
		if (emotions == null || emotions.size() < 2) {
			throw new IllegalArgumentException("Se requieren al menos 2 emociones para fusionar");
		}
		return emotionManager.fuseMultiple(emotions);
	}

	public EmotionDominanceMatrix getDominanceMatrix() {
		return emotionManager.getDominanceMatrix();
	}

	public EmotionCodex getCodex() {
		return emotionManager.getCodex();
	}

	// ========== Delegación a Combat Manager ==========
	public CombatResult executeCombatRound(Player player, List<Enemy> enemies, String actionLabel, Enemy target) {
		return combatManager.executeRound(player, enemies, PlayerAction.fromString(actionLabel), target);
	}

	// ========== Delegación a Wave Manager ==========

	public List<Enemy> createWave(int enemyCount) {
		return waveManager.createWave(enemyCount);
	}

	public List<Enemy> getEnemies() {
		return waveManager.getEnemies();
	}

	// ========== Delegación a Visual Manager ==========

	public BitmapFont getFont() {
		return visualManager.getFont();
	}

	public SpriteBatch getBatch() {
		return visualManager.getBatch();
	}

	public Skin getSkin() {
		return visualManager.getSkin();
	}

	public Stage getStage() {
		return visualManager.getStage();
	}

	// ========== Acceso al estado del juego ==========

	public Player getPlayer() {
		return gameState.getPlayer();
	}

	public EmotionInstance grantVictoryEmotionReward() {

		Player p = this.gameState.getPlayer();
		EmotionInstance reward = emotionManager.generateEmotion();

		// Añadir al jugador (acumula)
		p.añadirEmocion(reward);

		// Registrar en el códice (architecture-friendly)
		emotionManager.getCodex().registrar(reward);
		this.lastVictoryReward = reward;
		return reward;
	}

}
// ============================================================
// CLASES AUXILIARES
// ============================================================


/**
 * Encapsula el estado global del juego
 */
class GameState {
	private final Player player;
	private final EmotionInstanceFactory emotionFactory;
	private final EmotionDominanceMatrix dominanceMatrix;

	public GameState() {
		this.player = new Player();
		this.dominanceMatrix = new EmotionDominanceMatrix();
		this.emotionFactory = new EmotionInstanceFactory();

		// Configuración de dependencias estáticas (mejorable con DI)
		EmotionCombiner.setDominanceMatrix(dominanceMatrix);
		Enemy.setDominanceMatrix(dominanceMatrix);

	}

	public void reset() {
		player.resetState();
		dominanceMatrix.reset();
		EmotionNameGenerator.resetTracking();
	}

	public Player getPlayer() {
		return player;
	}

	public EmotionInstanceFactory getEmotionFactory() {
		return emotionFactory;
	}

	public EmotionDominanceMatrix getDominanceMatrix() {
		return dominanceMatrix;
	}
}

/**
 * Gestor especializado en emociones
 */
class EmotionManager {
	private final EmotionCodex codex;
	private final List<EmotionInstance> baseEmotions;
	private final EmotionInstanceFactory factory;
	private final EmotionDominanceMatrix dominanceMatrix;

	public EmotionManager(GameState gameState) {
		this.dominanceMatrix = gameState.getDominanceMatrix();
		this.codex = new EmotionCodex(dominanceMatrix);
		this.baseEmotions = new ArrayList<>();
		this.factory = gameState.getEmotionFactory();
	}

	public void generateInitialEmotions(int count) {
		baseEmotions.clear();
		for (int i = 0; i < count; i++) {
			baseEmotions.add(factory.generarProcedural());
		}
	}

	public EmotionInstance generateEmotion() {
		EmotionInstance newEmotion = factory.generarProcedural();
		baseEmotions.add(newEmotion);
		return newEmotion;
	}

	public EmotionInstance fuse(EmotionInstance e1, EmotionInstance e2) {
		EmotionInstance fusion = EmotionCombiner.combinar(e1, e2);
		codex.registrar(fusion);
		return fusion;
	}

	public EmotionInstance fuseMultiple(List<EmotionInstance> emotions) {
		EmotionInstance fusion = EmotionCombiner.combinarMultiples(emotions);
		codex.registrar(fusion);
		return fusion;
	}

	public void reset() {
		clearBaseEmotions();
		codex.clear();
		generateInitialEmotions(5);
	}

	public void clearBaseEmotions() {
		baseEmotions.clear();
	}

	public List<EmotionInstance> getBaseEmotions() {
		return new ArrayList<>(baseEmotions);
	}

	public List<EmotionInstance> getCodexEntries() {
		return codex.getEntries();
	}

	public EmotionCodex getCodex() {
		return codex;
	}

	public EmotionDominanceMatrix getDominanceMatrix() {
		return dominanceMatrix;
	}

}

/**
 * Gestor especializado en oleadas de enemigos
 */
class WaveManager {
	private final GameState gameState;
	private final EnemyFactory enemyFactory;
	private List<Enemy> currentEnemies;

	public WaveManager(GameState gameState) {
		this.gameState = gameState;
		this.enemyFactory = new EnemyFactory();
		this.currentEnemies = new ArrayList<>();
	}

	public List<Enemy> createWave(int enemyCount) {
		currentEnemies = enemyFactory.generarEnemigos(enemyCount, gameState.getDominanceMatrix());
		return new ArrayList<>(currentEnemies);
	}

	public List<Enemy> getEnemies() {
		return new ArrayList<>(currentEnemies);
	}

	public int getEnemyViewId(Enemy enemy) {
		int index = currentEnemies.indexOf(enemy);
		return 100 + Math.max(0, index);
	}
}

/**
 * Resultado de un turno de combate
 */

class VisualManager {

	private final BitmapFont font;
	private final SpriteBatch batch;
	private final Skin skin;
	private final Stage stage;

	public VisualManager() {

		// Un solo batch global
		this.batch = new SpriteBatch();

		// Por ahora: font por defecto (tú vas a cambiarlo a FreeType luego)
		this.font = new BitmapFont();

		// Skin UI
		try {
			this.skin = ElegantSkinFactory.create();
		} catch (Exception e) {
			throw new RuntimeException("Error cargando uiskin.json", e);
		}

		// Stage global
		this.stage = new Stage();
		Gdx.input.setInputProcessor(stage);
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

	public void dispose() {
		batch.dispose();
		font.dispose();
		skin.dispose();
		stage.dispose();
	}
}
