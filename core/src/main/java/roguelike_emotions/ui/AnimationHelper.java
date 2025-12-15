package roguelike_emotions.ui;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.utils.Align;

/**
 * Utilidades para animaciones reutilizables. Patrón: Utility Class + Fluent
 * Interface
 * 
 * @version 1.0
 */
public final class AnimationHelper {

	// Duraciones estándar
	public static final float FAST = 0.15f;
	public static final float MEDIUM = 0.25f;
	public static final float SLOW = 0.35f;

	// Escalas estándar
	public static final float HOVER_SCALE = 1.05f;
	public static final float CLICK_SCALE = 0.95f;

	private AnimationHelper() {
		throw new UnsupportedOperationException("Utility class");
	}

	/**
	 * Animación de entrada con fade y scale
	 */
	public static void fadeInWithScale(Actor actor) {
		fadeInWithScale(actor, MEDIUM);
	}

	public static void fadeInWithScale(Actor actor, float duration) {
		actor.getColor().a = 0f;
		actor.setScale(0.85f);
		actor.setOrigin(Align.center);
		enableTransform(actor);
		actor.addAction(Actions.parallel(Actions.fadeIn(duration, Interpolation.smooth),
				Actions.scaleTo(1f, 1f, duration, Interpolation.elasticOut)));
	}

	/**
	 * Animación de salida con fade y scale
	 */
	public static void fadeOutWithScale(Actor actor, Runnable onComplete) {
		fadeOutWithScale(actor, FAST, onComplete);
	}

	public static void fadeOutWithScale(Actor actor, float duration, Runnable onComplete) {
		actor.addAction(Actions.sequence(
				Actions.parallel(Actions.fadeOut(duration),
						Actions.scaleTo(0.9f, 0.9f, duration, Interpolation.smooth)),
				Actions.run(onComplete), Actions.removeActor()));
	}

	/**
	 * Animación de entrada deslizando desde abajo
	 */
	public static void slideInFromBottom(Actor actor, float distance) {
		actor.getColor().a = 0f;
		actor.setY(actor.getY() - distance);
		actor.addAction(Actions.parallel(Actions.fadeIn(MEDIUM, Interpolation.smooth),
				Actions.moveBy(0f, distance, MEDIUM, Interpolation.smooth)));
	}

	/**
	 * Animación de "shake" horizontal
	 */
	public static void shakeHorizontal(Actor actor) {
		actor.addAction(Actions.sequence(Actions.moveBy(-8f, 0f, 0.04f), Actions.moveBy(16f, 0f, 0.08f),
				Actions.moveBy(-16f, 0f, 0.08f), Actions.moveBy(16f, 0f, 0.08f), Actions.moveBy(-8f, 0f, 0.04f)));
	}

	/**
	 * Animación de "pulse" (respiración)
	 */
	public static void pulse(Actor actor) {
		pulse(actor, 1.05f, 2f);
	}

	public static void pulse(Actor actor, float scale, float duration) {
		actor.setOrigin(Align.center);
		enableTransform(actor);
		actor.addAction(Actions.forever(Actions.sequence(Actions.scaleTo(scale, scale, duration, Interpolation.sine),
				Actions.scaleTo(1f, 1f, duration, Interpolation.sine))));
	}

	/**
	 * Animación de "bounce" (rebote)
	 */
	public static void bounce(Actor actor) {
		actor.setOrigin(Align.center);
		enableTransform(actor);
		actor.addAction(Actions.sequence(Actions.scaleTo(1.15f, 1.15f, 0.12f, Interpolation.smooth),
				Actions.scaleTo(1f, 1f, 0.2f, Interpolation.elasticOut)));
	}

	/**
	 * Animación de rotación completa
	 */
	public static void spin(Actor actor, float duration) {
		actor.setOrigin(Align.center);
		enableTransform(actor);
		actor.addAction(Actions.sequence(Actions.rotateTo(360f, duration, Interpolation.smooth), Actions.rotateTo(0f)));
	}

	/**
	 * Animación de hover (al pasar el mouse)
	 */
	public static void onHoverEnter(Actor actor) {
		actor.clearActions();
		actor.setOrigin(Align.center);
		enableTransform(actor);
		actor.addAction(Actions.scaleTo(HOVER_SCALE, HOVER_SCALE, FAST, Interpolation.smooth));
	}

	/**
	 * Animación de hover salida
	 */
	public static void onHoverExit(Actor actor) {
		actor.clearActions();
		actor.addAction(Actions.scaleTo(1f, 1f, FAST, Interpolation.smooth));
	}

	/**
	 * Animación de click
	 */
	public static void onClick(Actor actor) {
		actor.clearActions();
		actor.setOrigin(Align.center);
		enableTransform(actor);
		actor.addAction(Actions.sequence(Actions.scaleTo(CLICK_SCALE, CLICK_SCALE, 0.08f, Interpolation.smooth),
				Actions.scaleTo(HOVER_SCALE, HOVER_SCALE, 0.1f, Interpolation.smooth)));
	}

	/**
	 * Animación de glow/highlight
	 */
	public static void highlight(Actor actor) {
		actor.addAction(Actions.forever(Actions.sequence(Actions.alpha(0.85f, 1.5f, Interpolation.sine),
				Actions.alpha(1f, 1.5f, Interpolation.sine))));
	}

	/**
	 * Detiene todas las animaciones
	 */
	public static void stopAll(Actor actor) {
		actor.clearActions();
		actor.setScale(1f);
		actor.setRotation(0f);
		actor.getColor().a = 1f;
	}

	/**
	 * Habilita transformaciones en un actor (solo para Widgets/Groups)
	 */
	private static void enableTransform(Actor actor) {
		if (actor instanceof com.badlogic.gdx.scenes.scene2d.Group) {
			((com.badlogic.gdx.scenes.scene2d.Group) actor).setTransform(true);
		} else if (actor instanceof Widget) {
			// Widget no tiene setTransform, pero funciona con acciones
		}
		// Para otros actores, las acciones funcionan sin setTransform
	}
}
