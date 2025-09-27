package roguelike_emotions.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import roguelike_emotions.characters.Enemy;
import roguelike_emotions.characters.EnemyFactory;
import roguelike_emotions.characters.Player;
import roguelike_emotions.mainMechanics.EmotionDominanceMatrix;
import roguelike_emotions.mainMechanics.EmotionInstance;

/**
 * GameCanvasUI.java
 *
 * Interfaz visual "tipo videojuego" basada en Swing:
 *   • Dibuja al jugador como un círculo azul (izquierda) y al enemigo como un círculo rojo (derecha).
 *   • Muestra barras de vida encima de cada círculo.
 *   • Dibuja las emociones activas de cada entidad debajo del círculo como pequeños rectángulos con su nombre.
 *   • Al pulsar el botón "Atacar", se muestra una línea amarilla que simula el ataque del jugador hacia el enemigo,
 *     luego se aplica la lógica real (player.attack(enemy), enemy.realizarAccion(player), tickTurnoEmocional, etc.),
 *     y se refresca el panel con las nuevas vidas y emociones.
 */
public class GameCanvasUI extends JFrame {
    // --- Tamaño de la ventana ---
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;

    // Referencias a tu lógica de juego
    private final Player player;
    private final Enemy enemy;

    // Vida máxima inicial para barras
    private final int playerMaxHp;
    private final int enemyMaxHp;

    // El panel donde dibujamos (círculos, barras de vida, emociones, ataque)
    private final GamePanel gamePanel;

    // Botón de "Atacar"
    private final JButton attackButton;

    // Flag para saber si estamos en la animación de ataque
    private boolean animating = false;

    public GameCanvasUI() {
        super("Combate Emocional – Visual");

        // 1) Configuración básica de la ventana
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);

        // 2) ----- INICIALIZACIONES DE JSON, FUSIONES, ETC. -----
        //   Si tu MainGameLoop original o EffectConfigLoader hacía algo como:
        //     roguelike_emotions.cfg.EffectConfigLoader.initialize("/visual_effects.json");
        //     roguelike_emotions.utils.FusionRegistry.initialize();
        //     roguelike_emotions.ui.EmotionEffectVisualRegistry.initialize("/visual_effects.json");
        //   Entonces llámalo aquí.

        // 3) Crear instancias de Player y Enemy
        player = new Player();
        playerMaxHp = player.getVida();

        EnemyFactory factory = new EnemyFactory();
        List<Enemy> list = factory.generarEnemigos(1, new EmotionDominanceMatrix());
        enemy = list.get(0);
        enemyMaxHp = enemy.getVida();

        // 4) Panel principal donde se dibuja todo
        gamePanel = new GamePanel();
        gamePanel.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT - 80));

        // 5) Botón de "Atacar"
        attackButton = new JButton("Atacar");
        attackButton.setFocusable(false);
        attackButton.addActionListener(e -> triggerAttack());

        // 6) Disposición de componentes en el JFrame
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.add(attackButton);

        Container cp = getContentPane();
        cp.setLayout(new BorderLayout());
        cp.add(gamePanel, BorderLayout.CENTER);
        cp.add(bottomPanel, BorderLayout.SOUTH);

        // 7) Mostrar la ventana
        setVisible(true);
    }

    /**
     * Se invoca al pulsar "Atacar".
     * Si no hay animación en curso y ambos siguen vivos:
     * 1) Empieza a mostrar la línea amarilla (animación breve).
     * 2) Tras 300 ms, aplica el ataque en tu lógica real:
     *       player.attack(enemy);
     *       if (enemy.isAlive()) enemy.realizarAccion(player);
     *       player.tickTurnoEmocional();
     *       enemy.tickTurnoEmocional(player);
     * 3) Refresh del panel para actualizar vida y emociones.
     */
    private void triggerAttack() {
        if (animating || !player.isAlive() || !enemy.isAlive()) return;

        animating = true;
        gamePanel.setShowAttackLine(true);

        // Timer de Swing no funciona en AWT Graphics; usamos java.util.Timer
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                gamePanel.setShowAttackLine(false);

                // –– Aquí llamamos a TU LÓGICA REAL ––
                player.attack(enemy);
                if (enemy.isAlive()) {
                    enemy.atacar(player);
                }
                player.tickTurnoEmocional();
                enemy.tickTurnoEmocional(player);
                // –– FIN DE TU LÓGICA REAL ––

                // Repintar para reflejar nuevos HP y emociones
                gamePanel.repaint();
                animating = false;
            }
        }, 300);
    }

    /**
     * GamePanel: dibuja el “campo” de combate.
     *   • Fondo gris oscuro.
     *   • Jugador (círculo azul, a la izquierda) y enemigo (círculo rojo, a la derecha).
     *   • Barra de vida encima de cada círculo.
     *   • Emociones activas (pequeños rectángulos con el nombre) debajo de cada círculo.
     *   • Si showAttackLine == true, dibuja una línea amarilla de jugador→enemigo.
     */
    private class GamePanel extends JPanel {
        private boolean showAttackLine = false;

        public void setShowAttackLine(boolean show) {
            this.showAttackLine = show;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            // 1) Fondo
            g.setColor(Color.DARK_GRAY);
            g.fillRect(0, 0, getWidth(), getHeight());

            // 2) Posiciones centrales
            int centerY = getHeight() / 2;
            int playerX = getWidth() / 4;
            int enemyX = getWidth() * 3 / 4;
            int radius = 50;

            // 3) Dibujar jugador (círculo azul)
            g.setColor(Color.BLUE);
            g.fillOval(playerX - radius, centerY - radius, radius * 2, radius * 2);

            // 4) Dibujar enemigo (círculo rojo)
            g.setColor(Color.RED);
            g.fillOval(enemyX - radius, centerY - radius, radius * 2, radius * 2);

            // 5) Dibujar barras de vida (encima de cada círculo)
            drawPlayerHealthBar(g, playerX, centerY - radius - 20);
            drawEnemyHealthBar(g, enemyX, centerY - radius - 20);

            // 6) Dibujar emociones activas (debajo de cada círculo)
            drawEmotions(g, player.getEmocionesActivas(), playerX, centerY + radius + 20);
            drawEmotions(g, enemy.getEmocionesActivas(), enemyX, centerY + radius + 20);

            // 7) Línea de ataque (si procede)
            if (showAttackLine) {
                g.setColor(Color.YELLOW);
                g.drawLine(playerX, centerY, enemyX, centerY);
            }
        }

        private void drawPlayerHealthBar(Graphics g, int cx, int cy) {
            int barWidth = 120, barHeight = 12;
            int x = cx - barWidth / 2;
            int curHp = player.getVida();
            double ratio = (double) curHp / playerMaxHp;
            // Fondo + contorno
            g.setColor(Color.BLACK);
            g.fillRect(x - 1, cy - 1, barWidth + 2, barHeight + 2);
            // Barra verde
            g.setColor(Color.GREEN);
            g.fillRect(x, cy, (int) (barWidth * ratio), barHeight);
            // Borde blanco
            g.setColor(Color.WHITE);
            g.drawRect(x, cy, barWidth, barHeight);
            // Texto encima: “curHp / maxHp”
            g.setColor(Color.WHITE);
            String texto = curHp + " / " + playerMaxHp;
            int textWidth = g.getFontMetrics().stringWidth(texto);
            g.drawString(texto, cx - textWidth / 2, cy - 3);
        }

        private void drawEnemyHealthBar(Graphics g, int cx, int cy) {
            int barWidth = 120, barHeight = 12;
            int x = cx - barWidth / 2;
            int curHp = enemy.getVida();
            double ratio = (double) curHp / enemyMaxHp;
            // Fondo + contorno
            g.setColor(Color.BLACK);
            g.fillRect(x - 1, cy - 1, barWidth + 2, barHeight + 2);
            // Barra de color según porcentaje
            if (ratio > 0.5) {
                g.setColor(Color.GREEN);
            } else if (ratio > 0.2) {
                g.setColor(Color.ORANGE);
            } else {
                g.setColor(Color.RED);
            }
            g.fillRect(x, cy, (int) (barWidth * ratio), barHeight);
            // Borde blanco
            g.setColor(Color.WHITE);
            g.drawRect(x, cy, barWidth, barHeight);
            // Texto “curHp / maxHp”
            g.setColor(Color.WHITE);
            String texto = curHp + " / " + enemyMaxHp;
            int textWidth = g.getFontMetrics().stringWidth(texto);
            g.drawString(texto, cx - textWidth / 2, cy - 3);
        }

        private void drawEmotions(Graphics g, List<EmotionInstance> emos, int cx, int cy) {
            if (emos == null || emos.isEmpty()) return;
            int total = emos.size();
            int width = 60, height = 20;
            int startX = cx - (total * (width + 10)) / 2 + 10;
            int y = cy;
            for (int i = 0; i < total; i++) {
                EmotionInstance e = emos.get(i);
                int x = startX + i * (width + 10);
                g.setColor(Color.LIGHT_GRAY);
                g.fillRect(x, y, width, height);
                g.setColor(Color.BLACK);
                g.drawRect(x, y, width, height);
                // Escribe el nombre (con recorte si es muy largo)
                String nombre = e.getNombre();
                if (nombre.length() > 8) nombre = nombre.substring(0, 7) + "...";
                g.setColor(Color.BLACK);
                g.drawString(nombre, x + 5, y + 15);
            }
        }
    }

    /**
     * Punto de entrada.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(GameCanvasUI::new);
    }
}