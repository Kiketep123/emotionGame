package roguelike_emotions.map;

import java.util.ArrayList;
import java.util.List;

import roguelike_emotions.characters.Enemy;
import roguelike_emotions.characters.EnemyFactory;
import roguelike_emotions.mainMechanics.EmotionDominanceMatrix;
import roguelike_emotions.mainMechanics.EmotionInstance;

public class EmotionNode {
    private EmotionInstance emotion;
    public float x, y;
    private List<EmotionNode> conexiones = new ArrayList<>();
    private List<Enemy> enemigos;

    public EmotionNode(EmotionInstance emotion, float x, float y,
                       EnemyFactory enemyFactory, EmotionDominanceMatrix matrix) {
        this.emotion = emotion;
        this.x = x;
        this.y = y;
        this.enemigos = enemyFactory.generarEnemigosPorTipo(
            2, emotion.getTipoBase(), matrix
        );
    }

    public EmotionInstance getEmotion() {
        return this.emotion;
    }

    public void setEmotion(EmotionInstance emotion) {
        this.emotion = emotion;
    }

    public List<EmotionNode> getConexiones() {
        return conexiones;
    }

    public void setConexiones(List<EmotionNode> conexiones) {
        this.conexiones = conexiones;
    }

    public List<Enemy> getEnemigos() {
        return enemigos;
    }
}
