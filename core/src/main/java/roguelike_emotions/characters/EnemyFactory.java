package roguelike_emotions.characters;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import roguelike_emotions.mainMechanics.EmotionDominanceMatrix;
import roguelike_emotions.mainMechanics.EmotionInstance;
import roguelike_emotions.mainMechanics.EmotionInstanceFactory;
import roguelike_emotions.mainMechanics.EmotionType;

public class EnemyFactory {
    private static final Random RNG = new Random();

    /** Matriz guardada para poder usarla en calcularFactorMultiEmotion() */
    private EmotionDominanceMatrix matrix;


    /**
     * Genera 'cantidad' de enemigos, asigna rol, stats escalados y emoción inicial.
     */
    public List<Enemy> generarEnemigos(int cantidad, EmotionDominanceMatrix matrix) {
        this.matrix = matrix;                       // Guardamos el parámetro
        // Inyectamos la matriz en la clase Enemy (static)
        Enemy.setDominanceMatrix(matrix);

        List<Enemy> list = new ArrayList<>();
        for (int i = 0; i < cantidad; i++) {
            // 1) Elige rol y tipo base
            EnemyRole role = EnemyRole.values()[RNG.nextInt(EnemyRole.values().length)];
            EmotionType tipoBase = EmotionType.random();

            // 2) Stats base y factor multi-émotion
            //TODO - usar emociones iniciales del enemigo en el cálculo
            double factorMulti = calcularFactorMultiEmotion(new ArrayList<>(), tipoBase);

            int vida  = escala((int)(100 * factorMulti), tipoBase);
            int danyo = escala((int)(20  * factorMulti), tipoBase);
            int def   = escala((int)(10  * factorMulti), tipoBase);
            double vel= escala((int)(10  * factorMulti), tipoBase) / 10.0;

            // 3) Ajusta según rol
            switch (role) {
                case TANK:
                    vida  = (int)(vida  * 1.5);
                    danyo = (int)(danyo * 0.75);
                    def   = (int)(def   * 1.5);
                    vel  *= 0.8;
                    break;
                case DPS:
                    vida  = (int)(vida  * 0.8);
                    danyo = (int)(danyo * 1.5);
                    def   = (int)(def   * 0.8);
                    vel  *= 1.2;
                    break;
                case SUPPORT:
                default:
                    // deja stats base
                    break;
            }

            // 4) Crear Enemy y asignar datos
            Enemy e = new Enemy("Enemigo" + (i + 1), vida, danyo, def, vel, role);
            e.setRole(role);

            // 5) Emoción inicial
            EmotionInstance em = new EmotionInstanceFactory().generarProcedural();
            e.setEstadoEmocional(em);
            e.addEmotion(em);

            list.add(e);
        }
        return list;
    }

    /**
     * Media de dominancia entre la lista de emociones y el tipo del jugador,
     * y también entre pares dentro de la lista, combinado y normalizado.
     */
    private double calcularFactorMultiEmotion(List<EmotionInstance> emos, EmotionType tipoJugador) {
        if (matrix == null || emos.isEmpty()) {
            return 1.0;
        }
        // 1) Compatibilidad con jugador
        double sumJ = 0;
        for (EmotionInstance e : emos) {
            sumJ += matrix.getPeso(e.getTipoBase(), tipoJugador);
        }
        double fJ = sumJ / emos.size();

        // 2) Compatibilidad interna entre emociones
        double sumIJ = 0;
        int c = 0;
        for (int i = 0; i < emos.size(); i++) {
            for (int j = i + 1; j < emos.size(); j++) {
                EmotionType t1 = emos.get(i).getTipoBase();
                EmotionType t2 = emos.get(j).getTipoBase();
                sumIJ += (matrix.getPeso(t1, t2) + matrix.getPeso(t2, t1)) / 2.0;
                c++;
            }
        }
        double fI = c > 0 ? sumIJ / c : 1.0;

        // 3) Media de ambas
        double media = (fJ + fI) / 2.0;
        double factor = 1.0 + (media - 1.0) * 0.5;
        return Math.max(0.75, Math.min(1.25, factor));
    }

    /**
     * Escala un valor base según el tipo emocional
     */
    private int escala(int base, EmotionType tipo) {
        double factor = switch (tipo) {
            case IRA       -> 1.3;
            case MIEDO     -> 1.2;
            case TRISTEZA  -> 0.9;
            case ALEGRIA   -> 1.1;
            case CULPA     -> 1.0;
            case ESPERANZA -> 1.0;
            case RABIA     -> 1.6;
            default        -> 1.0 + (RNG.nextDouble() - 0.5) * 0.2;
        };
        return (int)Math.max(1, base * factor);
    }
    public List<Enemy> generarEnemigosPorTipo(int cantidad, EmotionType tipo, EmotionDominanceMatrix matrix) {
        this.matrix = matrix;
        Enemy.setDominanceMatrix(matrix);

        List<Enemy> list = new ArrayList<>();
        for (int i = 0; i < cantidad; i++) {
            EnemyRole role = EnemyRole.values()[RNG.nextInt(EnemyRole.values().length)];

            // Tipo base es el del nodo
            EmotionType tipoBase = tipo;

            double factorMulti = calcularFactorMultiEmotion(new ArrayList<>(), tipoBase);

            int vida  = escala((int)(100 * factorMulti), tipoBase);
            int danyo = escala((int)(20  * factorMulti), tipoBase);
            int def   = escala((int)(10  * factorMulti), tipoBase);
            double vel= escala((int)(10  * factorMulti), tipoBase) / 10.0;

            switch (role) {
                case TANK:
                    vida  = (int)(vida  * 1.5);
                    danyo = (int)(danyo * 0.75);
                    def   = (int)(def   * 1.5);
                    vel  *= 0.8;
                    break;
                case DPS:
                    vida  = (int)(vida  * 0.8);
                    danyo = (int)(danyo * 1.5);
                    def   = (int)(def   * 0.8);
                    vel  *= 1.2;
                    break;
                case SUPPORT:
                default:
                    break;
            }

            Enemy e = new Enemy("Enemigo" + (i + 1), vida, danyo, def, vel, role);
            e.setRole(role);

            EmotionInstance em = new EmotionInstanceFactory().generarProcedural();
            e.setEstadoEmocional(em);
            e.addEmotion(em);

            list.add(e);
        }
        return list;
    }


}
