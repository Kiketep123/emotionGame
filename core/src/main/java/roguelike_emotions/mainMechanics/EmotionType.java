package roguelike_emotions.mainMechanics;

import java.util.Random;

public enum EmotionType {
    IRA, MIEDO, TRISTEZA, ALEGRIA, CULPA, ESPERANZA,FUSIONADA,CALMA,RABIA;

	public static EmotionType random() {
		EmotionType[] values = values();
		return values[new Random().nextInt(values.length)];
	}
}