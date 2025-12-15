package roguelike_emotions.screens;

import java.util.Collections;
import java.util.List;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import controller.HotkeyMap;
import controller.InputController;
import roguelike_emotions.MainGame;
import roguelike_emotions.cfg.ActionConfig;
import roguelike_emotions.characters.Enemy;
import roguelike_emotions.characters.Player;
import roguelike_emotions.graphics.RenderContext;
import roguelike_emotions.graphics.RenderGraph;
import roguelike_emotions.graphics.passes.*;
import roguelike_emotions.managers.GameManager;
import roguelike_emotions.ui.actions.ActionDescriptor;
import roguelike_emotions.ui.actions.ActionId;
import roguelike_emotions.ui.actions.ActionOption;
import roguelike_emotions.ui.turns.EventPacer;
import roguelike_emotions.ui.turns.GameTurnOrderProvider;
import roguelike_emotions.ui.turns.TurnOrchestrator;
import roguelike_emotions.ui.turns.TurnOrderProvider;
import roguelike_emotions.vfx.Director;
import roguelike_emotions.visual.VisualStyle;

public class TurnCombatScreen implements Screen {

    private enum CombatPhase {
        IN_PROGRESS, VICTORY, DEFEAT
    }

    // Constantes
    private static final int WORLD_W = 1280;
    private static final int WORLD_H = 720;
    private static final float MAX_DELTA = 0.05f;
    private static final float END_SCREEN_DELAY = 0.6f;
    private static final int TITLE_FONT_SIZE = 48;
    private static final int NORMAL_FONT_SIZE = 24;

    // Estado del combate
    private CombatPhase phase = CombatPhase.IN_PROGRESS;
    private float endTimer = 0f;
    private boolean roundActive = false;

    // Renderizado
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final BitmapFont fontTitle;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final FrameBuffer sceneFbo;
    private final Texture whitePx;
    private final RenderGraph graph;
    private final RenderContext ctx;

    // Lógica de juego
    private final Director director;
    private final VisualStyle style;
    private final TurnOrderProvider turnProvider;
    private final EventPacer pacer;

    // Input
    private InputController input;
    private InputMultiplexer inputMultiplexer;
    private com.badlogic.gdx.InputProcessor previousInput;

    // Configuración de acciones
    private final ActionConfig actionCfg;
    private final List<ActionDescriptor> actionModel;
    private final HotkeyMap hotkeys;

    public TurnCombatScreen(SpriteBatch sharedBatch, BitmapFont sharedFont) {
        this.batch = sharedBatch;

        // Inicializar fuentes
        if (sharedFont != null) {
            this.font = sharedFont;
            this.fontTitle = createTitleFont();
        } else {
            FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal("fonts/RobotoNormal.ttf"));
            this.font = createFont(gen, NORMAL_FONT_SIZE);
            this.fontTitle = createFont(gen, TITLE_FONT_SIZE);
            gen.dispose();
        }

        // Configurar cámara y viewport
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(WORLD_W, WORLD_H, camera);
        camera.position.set(WORLD_W * 0.5f, WORLD_H * 0.5f, 0f);
        camera.update();

        // Crear framebuffer y texture
        this.sceneFbo = new FrameBuffer(Pixmap.Format.RGBA8888, WORLD_W, WORLD_H, false);
        this.whitePx = createWhitePixelTexture();

        // Inicializar sistemas de renderizado
        this.director = new Director();
        this.style = new VisualStyle();
        VfxPass vfxPassInstance = new VfxPass();

        this.ctx = new RenderContext(batch, font, camera, viewport, sceneFbo, whitePx, director, style);
        this.ctx.vfxPass = vfxPassInstance;

        this.graph = new RenderGraph()
            .add(new WorldBackgroundPass())
            .add(new EntityPass())
            .add(vfxPassInstance)
            .add(new PostProcessPass())
            .add(new HudOverlayPass())
            .add(new TurnTimelinePass());

        // Inicializar sistemas de juego
        this.turnProvider = new GameTurnOrderProvider(GameManager.getInstance());
        this.pacer = new EventPacer();

        // Configurar acciones e input
        this.hotkeys = new HotkeyMap();
        this.actionCfg = ActionConfig.load(Gdx.files.internal("actions/actions.json"));
        this.actionModel = actionCfg.actions();
        this.hotkeys.rebuildIfChanged(actionModel);

        this.input = new InputController(this::getEnemies, this.hotkeys, this::onActionSelected, this::onConfirm);
    }

    @Override
    public void show() {
        previousInput = Gdx.input.getInputProcessor();

        inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(input);
        if (previousInput != null) {
            inputMultiplexer.addProcessor(previousInput);
        }

        Gdx.input.setInputProcessor(inputMultiplexer);

        // Reiniciar estado
        input.setEnabled(true);
        phase = CombatPhase.IN_PROGRESS;
        endTimer = 0f;
        roundActive = false;
    }

    @Override
    public void hide() {
        if (previousInput != null) {
            Gdx.input.setInputProcessor(previousInput);
        } else {
            Gdx.input.setInputProcessor(null);
        }
    }

    @Override
    public void render(float delta) {
        final float dt = clampDelta(delta);

        pumpEvents(dt);
        rebuildTurnQueue();
        settleRoundIfIdle();
        syncUiState();
        checkEndPhase(dt);
        draw(dt);
    }

    @Override
    public void resize(int w, int h) {
        viewport.update(w, h, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void dispose() {
        if (sceneFbo != null) sceneFbo.dispose();
        if (whitePx != null) whitePx.dispose();
        if (fontTitle != null) fontTitle.dispose();
        if (ctx.vfxPass != null) ctx.vfxPass.dispose();
    }

    // ========== Métodos de Inicialización ==========

    private BitmapFont createFont(FreeTypeFontGenerator gen, int size) {
        FreeTypeFontParameter param = new FreeTypeFontParameter();
        param.size = size;
        param.borderColor = Color.BLACK;
        param.borderWidth = 1.5f;
        return gen.generateFont(param);
    }

    private BitmapFont createTitleFont() {
        FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Roboto-Regular.ttf"));
        BitmapFont titleFont = createFont(gen, TITLE_FONT_SIZE);
        gen.dispose();
        return titleFont;
    }

    private Texture createWhitePixelTexture() {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        Texture tex = new Texture(pm);
        pm.dispose();
        return tex;
    }

    // ========== Callbacks ==========

    private List<Enemy> getEnemies() {
        try {
            return GameManager.getInstance().getEnemies();
        } catch (Throwable t) {
            return Collections.emptyList();
        }
    }

    private void onActionSelected(ActionId id) {
        ActionOption opt = id.toOption();
        input.setSelectedAction(opt);
    }

    private void onConfirm() {
        if (phase != CombatPhase.IN_PROGRESS) return;

        pacer.reset();
        GameManager gm = GameManager.getInstance();
        Player p = gm.getPlayer();
        List<Enemy> es = gm.getEnemies();
        ActionOption action = input.getSelectedAction();
        int index = input.getSelectedEnemyIndex();

        roundActive = TurnOrchestrator.tryExecute(gm, director, p, es, action, index);
    }

    // ========== Lógica del Juego ==========

    private float clampDelta(float delta) {
        return (delta <= 0f) ? 0f : Math.min(delta, MAX_DELTA);
    }

    private void pumpEvents(float dt) {
        pacer.drainBus();
        pacer.update(ctx, director, dt);
    }

    private void rebuildTurnQueue() {
        ctx.turnQueue.rebuild(turnProvider);
    }

    private void settleRoundIfIdle() {
        if (!roundActive || !pacer.isIdle(director)) return;

        if (isCombatOngoing()) {
            ctx.turnQueue.setCursor(0);
        }

        roundActive = false;
    }

    private boolean isCombatOngoing() {
        GameManager gm = GameManager.getInstance();
        Player p = gm.getPlayer();

        if (p == null || !p.isAlive()) return false;

        List<Enemy> enemies = gm.getEnemies();
        if (enemies == null) return false;

        for (Enemy e : enemies) {
            if (e != null && e.isAlive()) return true;
        }

        return false;
    }

    private void syncUiState() {
        ctx.selectedActionLabel = input.getSelectedAction().label();
        ctx.selectedEnemyViewId = 100 + Math.max(0, input.getSelectedEnemyIndex());
    }

    private void checkEndPhase(float dt) {
        if (phase != CombatPhase.IN_PROGRESS) {
            endTimer += dt;
            if (endTimer > END_SCREEN_DELAY) {
                restartCombat();
            }
            return;
        }

        if (roundActive || !pacer.isIdle(director)) return;

        if (!isCombatOngoing()) {
            GameManager gm = GameManager.getInstance();
            boolean playerAlive = gm.getPlayer() != null && gm.getPlayer().isAlive();

            phase = playerAlive ? CombatPhase.VICTORY : CombatPhase.DEFEAT;
            input.setEnabled(false);
            endTimer = 0f;
        }
    }

    // ========== Renderizado ==========

    private void draw(float dt) {
        director.update(dt);
        ctx.updateViews(dt);
        graph.render(ctx);

        if (phase != CombatPhase.IN_PROGRESS) {
            drawEndOverlay();
        }
    }

    private void drawEndOverlay() {
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        batch.setColor(0, 0, 0, 0.6f);
        batch.draw(whitePx, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        batch.setColor(1, 1, 1, 1);

        String title = (phase == CombatPhase.VICTORY) ? "VICTORIA" : "DERROTA";
        String subtitle = "Pulsa ENTER para continuar";

        fontTitle.draw(batch, title, viewport.getWorldWidth() * 0.4f, viewport.getWorldHeight() * 0.6f);
        font.draw(batch, subtitle, viewport.getWorldWidth() * 0.35f, viewport.getWorldHeight() * 0.5f);

        batch.end();
    }

    private void restartCombat() {
        boolean victory = (phase == CombatPhase.VICTORY);
        int defeated = GameManager.getInstance().getEnemies().size();
        MainGame.cambiarPantalla(new PostCombatScreen(victory, defeated));
    }
}
