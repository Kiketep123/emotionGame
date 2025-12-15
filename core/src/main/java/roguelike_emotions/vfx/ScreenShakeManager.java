package roguelike_emotions.vfx;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;

/**
 * Sistema de screen shake con múltiples "traumas" simultáneos.
 * Cada evento puede añadir su propio trauma que se acumula.
 */
public class ScreenShakeManager {
    
    private float trauma = 0f;
    private float traumaDecay = 1.5f; // Velocidad de recuperación
    
    private float originalX;
    private float originalY;
    
    /**
     * Añade trauma al sistema (0.0 - 1.0)
     * 0.3 = shake suave, 0.6 = shake medio, 1.0 = shake extremo
     */
    public void addTrauma(float amount) {
        trauma = Math.min(1f, trauma + amount);
    }
    
    /**
     * Actualiza el shake y aplica a la cámara
     */
    public void update(OrthographicCamera camera, float delta) {
        // Guardar posición original en el primer frame
        if (trauma > 0f && originalX == 0f && originalY == 0f) {
            originalX = camera.position.x;
            originalY = camera.position.y;
        }
        
        if (trauma > 0f) {
            // Shake con curva exponencial (más natural)
            float shake = trauma * trauma;
            
            float maxOffset = 15f;
            float offsetX = MathUtils.random(-maxOffset, maxOffset) * shake;
            float offsetY = MathUtils.random(-maxOffset, maxOffset) * shake;
            
            camera.position.x = originalX + offsetX;
            camera.position.y = originalY + offsetY;
            
            // Decay del trauma
            trauma = Math.max(0f, trauma - traumaDecay * delta);
        } else {
            // Restaurar posición original
            if (originalX != 0f || originalY != 0f) {
                camera.position.x = originalX;
                camera.position.y = originalY;
                originalX = 0f;
                originalY = 0f;
            }
        }
        
        camera.update();
    }
    
    /**
     * Resetea todo el shake inmediatamente
     */
    public void reset(OrthographicCamera camera) {
        trauma = 0f;
        if (originalX != 0f || originalY != 0f) {
            camera.position.x = originalX;
            camera.position.y = originalY;
            originalX = 0f;
            originalY = 0f;
            camera.update();
        }
    }
    
    public boolean isShaking() {
        return trauma > 0.01f;
    }
}
