package roguelike_emotions.sound;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import java.util.HashMap;
import java.util.Map;

public class SoundManager {
    private static SoundManager instance;
    private Map<String, Sound> sounds;
    private boolean enabled = true;
    private float volume = 0.3f;

    private SoundManager() {
        sounds = new HashMap<>();
        loadSounds();
    }

    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    private void loadSounds() {
        // Si no tienes archivos de audio, esto no romperá nada
        try {
            sounds.put("select", Gdx.audio.newSound(Gdx.files.internal("sounds/select.ogg")));
            sounds.put("hover", Gdx.audio.newSound(Gdx.files.internal("sounds/hover.ogg")));
            sounds.put("success", Gdx.audio.newSound(Gdx.files.internal("sounds/success.ogg")));
            sounds.put("error", Gdx.audio.newSound(Gdx.files.internal("sounds/error.ogg")));
            sounds.put("fill", Gdx.audio.newSound(Gdx.files.internal("sounds/fill.ogg")));
        } catch (Exception e) {
            System.out.println("Audio files not found - running without sound");
        }
    }

    public void play(String soundName) {
        if (!enabled) return;
        Sound sound = sounds.get(soundName);
        if (sound != null) {
            sound.play(volume);
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setVolume(float volume) {
        this.volume = Math.max(0f, Math.min(1f, volume));
    }

    public void dispose() {
        for (Sound sound : sounds.values()) {
            sound.dispose();
        }
        sounds.clear();
    }
}
