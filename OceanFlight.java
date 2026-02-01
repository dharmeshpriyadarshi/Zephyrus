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
    private static final int FLIGHT_SPEED = 6; // How fast the ocean moves
    
    // --- State Variables ---
    private int playerX = WIDTH / 2;
    private int playerY = HEIGHT - 150;
    private int velX = 0;
    private Timer timer;
    
    // List to hold our waves
    private ArrayList<Wave> waves;
    private Random random;

    public OceanFlight() {
        this.setBackground(new Color(0, 105, 148)); // Deep Ocean Blue
        this.setFocusable(true);
        this.addKeyListener(this);

        waves = new ArrayList<>();
        random = new Random();

        // Pre-fill screen with waves so it doesn't start empty
        for (int i = 0; i < 20; i++) {
            waves.add(new Wave(random.nextInt(WIDTH), random.nextInt(HEIGHT)));
        }

        timer = new Timer(16, this);
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Draw Waves (The Ocean)
        g2d.setColor(new Color(255, 255, 255, 50)); // White with transparency
        for (Wave wave : waves) {
            // Draw a small horizontal line representing a wave crest
            g2d.drawLine(wave.x, wave.y, wave.x + wave.width, wave.y);
        }

        // 2. Draw Aircraft (On top of waves)
        drawAircraft(g2d, playerX, playerY);
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
        // Engine glow
        g2d.setColor(new Color(255, 100, 0, 150));
        g2d.fillOval(x - 3, y + 60, 6, 10);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Update Player
        playerX += velX;
        if (playerX < 0) playerX = 0;
        if (playerX > getWidth()) playerX = getWidth();

        // Update Waves (The Illusion of Flight)
        updateWaves();

        repaint();
    }

    private void updateWaves() {
        // Move every wave down
        for (Wave wave : waves) {
            wave.y += FLIGHT_SPEED; // Move down by speed amount
        }

        // Remove waves that have gone off the bottom of the screen
        waves.removeIf(w -> w.y > HEIGHT);

        // Add new waves at the top to replace old ones
        // We add waves randomly to keep the ocean looking natural
        if (random.nextInt(100) < 15) { // 15% chance to spawn a wave per frame
            int newX = random.nextInt(WIDTH);
            // Spawn slightly above 0 so they slide in smoothly
            waves.add(new Wave(newX, -10)); 
        }
    }

    // --- Input Handling ---
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

    // --- Helper Class for Waves ---
    // A simple container for wave properties
    private static class Wave {
        int x, y, width;

        public Wave(int x, int y) {
            this.x = x;
            this.y = y;
            // Random wave width between 20 and 60 pixels
            this.width = 20 + new Random().nextInt(40); 
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Ocean Flight");
        OceanFlight game = new OceanFlight();
        frame.add(game);
        frame.setSize(WIDTH, HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
    }
}