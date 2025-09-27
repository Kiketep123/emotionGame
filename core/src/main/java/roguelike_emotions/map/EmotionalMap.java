package roguelike_emotions.map;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import roguelike_emotions.characters.EnemyFactory;
import roguelike_emotions.mainMechanics.EmotionDominanceMatrix;
import roguelike_emotions.mainMechanics.EmotionInstance;
import roguelike_emotions.mainMechanics.EmotionInstanceFactory;

public class EmotionalMap {

	private List<EmotionNode> nodos;
	private Random random = new Random();

	public EmotionalMap(int cantidad, EnemyFactory enemyFactory, EmotionDominanceMatrix matrix) {
		this.nodos = generarMapaProcedural(cantidad, enemyFactory, matrix);
		conectarNodosPorDistancia(200f);
	}

	private List<EmotionNode> generarMapaProcedural(int cantidad, EnemyFactory enemyFactory,
			EmotionDominanceMatrix matrix) {
		List<EmotionNode> lista = new ArrayList<>();
		EmotionInstanceFactory factory = new EmotionInstanceFactory();

		for (int i = 0; i < cantidad; i++) {
			float x = 100 + random.nextFloat() * 600;
			float y = 100 + random.nextFloat() * 400;
			EmotionInstance emotion = factory.generarProcedural();
			lista.add(new EmotionNode(emotion, x, y, enemyFactory, matrix));
		}

		return lista;
	}

	private void conectarNodosPorDistancia(float maxDistancia) {
		for (EmotionNode a : nodos) {
			for (EmotionNode b : nodos) {
				if (a == b)
					continue;
				float dx = a.x - b.x;
				float dy = a.y - b.y;
				float dist = (float) Math.sqrt(dx * dx + dy * dy);
				if (dist <= maxDistancia && !a.getConexiones().contains(b)) {
					a.getConexiones().add(b);
				}
			}
		}
	}

	public List<EmotionNode> getNodos() {
		return nodos;
	}
}
