package roguelike_emotions.cfg;

import com.badlogic.gdx.graphics.Color;

/**
 * Configuración profesional y paramétrica para el efecto Combo x3.
 * Todos los valores están centralizados para facilitar balanceo y tweaking.
 */
public final class ComboMaxConfig {
    
    // ==================== SCREEN SHAKE ====================
    public static final float SHAKE_BASE_TRAUMA = 0.4f;
    public static final int SHAKE_PULSE_COUNT = 3;
    public static final float SHAKE_PULSE_INTERVAL = 0.08f; // segundos
    
    // ==================== PARTICLE RINGS ====================
    public static final int RING_COUNT = 3;
    public static final float RING_BASE_RADIUS = 50f;
    public static final float RING_RADIUS_INCREMENT = 35f;
    public static final int RING_BASE_PARTICLE_COUNT = 16;
    public static final float RING_PARTICLE_COUNT_MULTIPLIER = 1.5f;
    public static final float RING_SPAWN_DELAY = 0.06f; // segundos entre anillos
    public static final float RING_PARTICLE_SPEED_MIN = 180f;
    public static final float RING_PARTICLE_SPEED_MAX = 280f;
    public static final float RING_PARTICLE_LIFETIME = 2.2f;
    public static final float RING_PARTICLE_SCALE_MIN = 1.2f;
    public static final float RING_PARTICLE_SCALE_MAX = 2.8f;
    
    // ==================== CENTRAL BURST ====================
    public static final int BURST_PRIMARY_COUNT = 80;
    public static final int BURST_SECONDARY_COUNT = 50;
    public static final float BURST_SECONDARY_OFFSET_Y = 45f;
    public static final float BURST_SECONDARY_DELAY = 0.12f; // segundos
    public static final float BURST_SPEED_MULTIPLIER = 1.4f;
    
    // ==================== FIREWORKS ====================
    public static final int FIREWORK_COUNT = 8;
    public static final float FIREWORK_ARC_DEGREES = 360f;
    public static final int FIREWORK_TRAIL_SEGMENTS = 6;
    public static final float FIREWORK_ASCENT_HEIGHT = 100f;
    public static final float FIREWORK_TRAIL_SPACING = 15f;
    public static final int FIREWORK_PARTICLES_PER_SEGMENT = 4;
    
    // ==================== VISUAL FEEDBACK ====================
    public static final float FLASH_DURATION = 0.45f;
    public static final Color FLASH_COLOR = new Color(1.0f, 0.84f, 0.0f, 0.7f);
    public static final Color SCREEN_TINT = new Color(0.28f, 0.22f, 0.0f, 0.65f);
    public static final float TINT_DECAY_RATE = 1.8f;
    public static final float PLAYER_KNOCKBACK_X = -95f;
    public static final float PLAYER_KNOCKBACK_Y = 165f;
    public static final float PLAYER_FLASH_DURATION = 0.5f;
    
    // ==================== TEXT DISPLAY ====================
    public static final String TEXT_PRIMARY = "✨ COMBO x3 ✨";
    public static final float TEXT_PRIMARY_OFFSET_Y = 150f;
    public static final String TEXT_PRIMARY_COLOR_HEX = "FFD700";
    public static final String TEXT_SECONDARY = "¡MÁXIMO PODER!";
    public static final float TEXT_SECONDARY_OFFSET_Y = 115f;
    public static final String TEXT_SECONDARY_COLOR_HEX = "FF8C00";
    public static final float TEXT_SCALE_PULSE_SPEED = 8.0f;
    public static final float TEXT_SCALE_PULSE_AMPLITUDE = 0.25f;
    
    // ==================== MOOD SYSTEM ====================
    public static final String MOOD_PRIMARY_EMOTION = "ESPERANZA";
    public static final float MOOD_INTENSITY = 1.0f;
    
    // ==================== TIMING ====================
    public static final float TOTAL_DURATION = 1.8f; // segundos
    public static final float PEAK_IMPACT_TIME = 0.2f; // segundos
    
    private ComboMaxConfig() {} // Prevent instantiation
}
