import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class OceanFlight extends JPanel implements ActionListener, KeyListener {

    // --- Game Settings ---
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int PLAYER_SPEED = 5;
    private static final int FLIGHT_SPEED = 6;
    
    // --- State Variables ---
    private int playerX = WIDTH / 2;
    private int playerY = HEIGHT - 150;
    private int velX = 0;
    private Timer timer; // The engine of our game
    private boolean isPaused = false;
    
    private ArrayList<Wave> waves;
    private Random random;

    public OceanFlight() {
        this.setBackground(new Color(0, 105, 148));
        this.setFocusable(true);
        this.addKeyListener(this);

        waves = new ArrayList<>();
        random = new Random();

        // Initial wave generation
        for (int i = 0; i < 20; i++) {
            waves.add(new Wave(random.nextInt(WIDTH), random.nextInt(HEIGHT)));
        }

        // Timer runs every 16ms (approx 60 FPS)
        timer = new Timer(16, this);
        timer.start();
    }

    // --- Control Methods ---
    public void pauseGame() {
        if (timer.isRunning()) {
            timer.stop();
            isPaused = true;
            System.out.println("Game Paused: Saving Resources"); // Debug message in console
        }
    }

    public void resumeGame() {
        if (!timer.isRunning()) {
            timer.start();
            isPaused = false;
            System.out.println("Game Resumed");
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Draw Waves
        g2d.setColor(new Color(255, 255, 255, 50));
        for (Wave wave : waves) {
            g2d.drawLine(wave.x, wave.y, wave.x + wave.width, wave.y);
        }

        // 2. Draw Aircraft
        drawAircraft(g2d, playerX, playerY);
        
        // 3. Optional: Draw "PAUSED" text if minimized/paused
        if (isPaused) {
            g2d.setColor(Color.WHITE);
            g2d.drawString("PAUSED", 10, 20);
        }
    }

    private void drawAircraft(Graphics2D g2d, int x, int y) {
        g2d.setColor(Color.LIGHT_GRAY);
        // Fuselage
        g2d.fillRoundRect(x - 5, y, 10, 60, 5, 5);
        // Wings
        Polygon wings = new Polygon();
        wings.addPoint(x, y + 15);
        wings.addPoint(x - 40, y + 45);
        wings.addPoint(x + 40, y + 45);
        g2d.fillPolygon(wings);
        // Tail
        Polygon tail = new Polygon();
        tail.addPoint(x, y + 50);
        tail.addPoint(x - 15, y + 60);
        tail.addPoint(x + 15, y + 60);
        g2d.fillPolygon(tail);
        // Engine
        g2d.setColor(new Color(255, 100, 0, 150));
        g2d.fillOval(x - 3, y + 60, 6, 10);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // This method ONLY runs if the timer is running
        
        // Update Player Physics
        playerX += velX;
        if (playerX < 0) playerX = 0;
        if (playerX > getWidth()) playerX = getWidth();

        // Update Waves
        updateWaves();

        repaint();
    }

    private void updateWaves() {
        for (Wave wave : waves) {
            wave.y += FLIGHT_SPEED;
        }
        // CLEANUP: This prevents memory leaks!
        waves.removeIf(w -> w.y > HEIGHT);

        if (random.nextInt(100) < 15) {
            waves.add(new Wave(random.nextInt(WIDTH), -10));
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_LEFT) velX = -PLAYER_SPEED;
        else if (code == KeyEvent.VK_RIGHT) velX = PLAYER_SPEED;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_RIGHT) velX = 0;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    private static class Wave {
        int x, y, width;
        public Wave(int x, int y) {
            this.x = x;
            this.y = y;
            this.width = 20 + new Random().nextInt(40);
        }
    }

    // --- Updated Main Method ---
    public static void main(String[] args) {
        JFrame frame = new JFrame("Ocean Flight");
        OceanFlight game = new OceanFlight();
        
        frame.add(game);
        frame.setSize(WIDTH, HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);

        // --- THE FIX: Window Listener ---
        // This detects when the window is minimized or hidden
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowIconified(WindowEvent e) {
                // Window Minimized
                game.pauseGame();
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
                // Window Restored
                game.resumeGame();
            }
            
            @Override
            public void windowDeactivated(WindowEvent e) {
                // User clicked on another window (optional, can remove if you want background play)
                game.pauseGame();
            }

            @Override
            public void windowActivated(WindowEvent e) {
                 // User clicked back on the game
                game.resumeGame();
            }
        });

        frame.setVisible(true);
    }
}