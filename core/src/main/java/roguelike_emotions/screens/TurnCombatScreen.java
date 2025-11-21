package roguelike_emotions.screens;

import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import controller.HotkeyMap;
import controller.InputController;
import roguelike_emotions.cfg.ActionConfig;
import roguelike_emotions.characters.Enemy;
import roguelike_emotions.characters.Player;
import roguelike_emotions.graphics.RenderContext;
import roguelike_emotions.graphics.RenderGraph;
import roguelike_emotions.graphics.passes.EntityPass;
import roguelike_emotions.graphics.passes.HudOverlayPass;
import roguelike_emotions.graphics.passes.PostProcessPass;
import roguelike_emotions.graphics.passes.TurnTimelinePass;
import roguelike_emotions.graphics.passes.VfxPass;
import roguelike_emotions.graphics.passes.WorldBackgroundPass;
import roguelike_emotions.managers.GameManager;
import roguelike_emotions.ui.actions.ActionDescriptor;
import roguelike_emotions.ui.actions.ActionOption;
import roguelike_emotions.ui.turns.EventPacer;
import roguelike_emotions.ui.turns.GameTurnOrderProvider;
import roguelike_emotions.ui.turns.TurnOrchestrator;
import roguelike_emotions.ui.turns.TurnOrderProvider;
import roguelike_emotions.vfx.Director;
import roguelike_emotions.visual.VisualStyle;

public class TurnCombatScreen implements Screen {
	private final SpriteBatch batch;
	private final BitmapFont font;
	private final OrthographicCamera camera;
	private final Viewport viewport;
	private final FrameBuffer sceneFbo;
	private final Texture whitePx;
	private final RenderGraph graph;
	private final RenderContext ctx;
	private final Director director;
	private final VisualStyle style;
	private final TurnOrderProvider turnProvider = new GameTurnOrderProvider(GameManager.getInstance());
	private final EventPacer pacer = new EventPacer();
	private boolean roundActive = false;
	// UI state via controller
	private final InputController input;
	// Config y hotkeys del selector de acciones
	private ActionConfig actionCfg;
	private List<ActionDescriptor> actionModel = Collections.emptyList();
	private final HotkeyMap hotkeys = new HotkeyMap();

	private static final int WORLD_W = 1280, WORLD_H = 720;

	public TurnCombatScreen(SpriteBatch sharedBatch, BitmapFont sharedFont) {

		this.batch = sharedBatch;
		this.font = sharedFont != null ? sharedFont : new BitmapFont();
		this.camera = new OrthographicCamera();
		this.viewport = new FitViewport(WORLD_W, WORLD_H, camera);
		camera.position.set(WORLD_W * 0.5f, WORLD_H * 0.5f, 0f);
		camera.update();

		this.sceneFbo = new FrameBuffer(Pixmap.Format.RGBA8888, WORLD_W, WORLD_H, false);
		Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
		pm.setColor(Color.WHITE);
		pm.fill();
		this.whitePx = new Texture(pm);
		pm.dispose();

		this.director = new Director();
		this.style = new VisualStyle();
		this.ctx = new RenderContext(batch, font, camera, viewport, sceneFbo, whitePx, director, style);
		this.graph = new RenderGraph().add(new WorldBackgroundPass()).add(new EntityPass()).add(new VfxPass())
				.add(new PostProcessPass()).add(new HudOverlayPass()).add(new TurnTimelinePass());

		// Input controller (inyectamos proveedor de enemigos y confirm)
		this.input = new InputController(() -> {
			try {
				return GameManager.getInstance().getEnemies();
			} catch (Throwable t) {
				return java.util.Collections.emptyList();
			}
		}, this::onConfirm);
		this.actionCfg = ActionConfig.load(com.badlogic.gdx.Gdx.files.internal("ui/actions.json"));
		this.actionModel = actionCfg.actions();
		this.hotkeys.rebuildIfChanged(actionModel);
		var mux = new com.badlogic.gdx.InputMultiplexer();
		mux.addProcessor(input);
		if (Gdx.input.getInputProcessor() != null)
			mux.addProcessor(Gdx.input.getInputProcessor());
		Gdx.input.setInputProcessor(mux);
	}

	private void onConfirm() {
		pacer.reset();
		GameManager gm = GameManager.getInstance();
		Player p = gm.getPlayer();
		List<Enemy> es = gm.getEnemies();
		ActionOption action = input.getSelectedAction();
		int index = input.getSelectedEnemyIndex();
		roundActive = TurnOrchestrator.tryExecute(gm, director, p, es, action, index);
	}

	@Override
	public void render(float delta) {

		final float dt = clampDelta(delta);
		pumpEvents(dt); // VisBus -> EventPacer -> VisualEvents/Director (ritmado)
		rebuildTurnQueue(); // Snapshot visual (no toca cursor)
		settleRoundIfIdle(); // Si terminó la ronda, devuelve el foco al jugador
		syncUiState(); // HUD + resalte de objetivo
		draw(dt); // Avanza timeline y renderiza
	}

	/* ---------- Helpers: pequeños y testeables ---------- */

	private float clampDelta(float delta) {
		if (delta <= 0f)
			return 0f;
		return (delta > 0.05f) ? 0.05f : delta;
	}

	private void pumpEvents(float dt) {
		pacer.drainBus(); // recolecta todos los VisEvents del dominio
		pacer.update(ctx, director, dt); // reproduce 1 evento si toca (cooldown + director libre)

	}

	private void rebuildTurnQueue() {
		ctx.turnQueue.rebuild(turnProvider);
	}

	private void settleRoundIfIdle() {
		if (!roundActive || !pacer.isIdle(director))
			return;
		if (isCombatOngoing())
			ctx.turnQueue.setCursor(0); // vuelve a "Tú"
		roundActive = false;
	}

	private boolean isCombatOngoing() {
		final var gm = roguelike_emotions.managers.GameManager.getInstance();
		final var p = gm.getPlayer();
		if (p == null || !p.isAlive())
			return false;
		final var es = gm.getEnemies();
		if (es == null)
			return false;
		for (var e : es)
			if (e != null && e.isAlive())
				return true;
		return false;
	}

	private void syncUiState() {
		ctx.selectedActionLabel = input.getSelectedAction().label();
		ctx.selectedEnemyViewId = 100 + Math.max(0, input.getSelectedEnemyIndex());
	}

	private void draw(float dt) {
		director.update(dt);
		ctx.updateViews(dt);
		graph.render(ctx);
	}

	@Override
	public void resize(int w, int h) {
		viewport.update(w, h, true);
	}

	@Override
	public void show() {
	}

	@Override
	public void hide() {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void dispose() {
		if (sceneFbo != null)
			sceneFbo.dispose();
		if (whitePx != null)
			whitePx.dispose();
	}
}
