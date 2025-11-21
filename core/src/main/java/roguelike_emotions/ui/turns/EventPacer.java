package roguelike_emotions.ui.turns;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import roguelike_emotions.graphics.RenderContext;
import roguelike_emotions.vfx.BuffAppliedEvent;
import roguelike_emotions.vfx.DamageEvent;
import roguelike_emotions.vfx.DebuffAppliedEvent;
import roguelike_emotions.vfx.Director;
import roguelike_emotions.vfx.HealEvent;
import roguelike_emotions.vfx.TurnStepEvent;
import roguelike_emotions.vfx.VisBus;
import roguelike_emotions.vfx.VisEvent;
import roguelike_emotions.vfx.VisualEvents;

public final class EventPacer {
	private final Deque<VisEvent> queue = new ArrayDeque<>();
	private float cooldown = 0f;

	private static final float PAUSE_TURN_STEP = 0.40f;
	private static final float PAUSE_IMPACT = 0.28f;
	private static final float PAUSE_STATUS = 0.18f;

	public void drainBus() {
		VisEvent ev;
		while ((ev = VisBus.poll()) != null) {
			queue.addLast(ev);
		}
	}

	public void update(RenderContext ctx, Director director, float delta) {
		updateCooldown(delta);

		if (shouldSkipUpdate(director)) {
			return;
		}

		VisEvent ev = queue.removeFirst();
		processEvent(ctx, ev);
		director.post(ev);
		VisualEvents.apply(ctx, ev);
	}

	private void updateCooldown(float delta) {
		if (cooldown > 0f) {
			cooldown = Math.max(0f, cooldown - delta);
		}
	}

	private boolean shouldSkipUpdate(Director director) {
		return cooldown > 0f || director.isBusy() || queue.isEmpty();
	}

	private void processEvent(RenderContext ctx, VisEvent ev) {
		if (ev instanceof TurnStepEvent ts) {
			handleTurnStepEvent(ctx, ts);
		} else if (ev instanceof DamageEvent d) {
			handleDamageEvent(ctx, d);
		} else if (isStatusEvent(ev)) {
			cooldown = PAUSE_STATUS;
		}
	}

	private void handleTurnStepEvent(RenderContext ctx, TurnStepEvent ts) {
		int id = ts.actorViewId();
		updateCursorById(ctx, id);
		cooldown = PAUSE_TURN_STEP;
	}

	private void handleDamageEvent(RenderContext ctx, DamageEvent d) {
		String tag = getUppercaseTag(d);

		if (tag.contains("PLAYER_ATTACK")) {
			ctx.turnQueue.setCursor(0);
		} else if (tag.contains("ENEMY_ATTACK")) {
			updateCursorById(ctx, d.srcId());
		}

		cooldown = PAUSE_IMPACT;
	}

	private String getUppercaseTag(DamageEvent d) {
		return d.tag() != null ? d.tag().toUpperCase() : "";
	}

	private void updateCursorById(RenderContext ctx, int targetId) {
		List<?> tokens = ctx.turnQueue.tokens();
		for (int i = 0; i < tokens.size(); i++) {
			var token = tokens.get(i);
			if (hasMatchingViewId(token, targetId)) {
				ctx.turnQueue.setCursor(i);
				break;
			}
		}
	}

	private boolean hasMatchingViewId(Object token, int targetId) {
		try {
			var method = token.getClass().getMethod("viewId");
			return (int) method.invoke(token) == targetId;
		} catch (Exception e) {
			return false;
		}
	}

	private boolean isStatusEvent(VisEvent ev) {
		return ev instanceof HealEvent || ev instanceof BuffAppliedEvent || ev instanceof DebuffAppliedEvent;
	}

	public boolean isIdle(Director director) {
		return queue.isEmpty() && cooldown <= 0f && !director.isBusy();
	}

	public void reset() {
		queue.clear();
		cooldown = 0f;
	}
}