import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.Random;

public class OceanFlight extends JPanel implements ActionListener, KeyListener {

    // --- Game Settings ---
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int PLAYER_SPEED = 5;
    private static final int SCROLL_SPEED = 6; 
    
    // --- State Variables ---
    private int playerX = WIDTH / 2;
    private int playerY = HEIGHT - 150;
    private int velX = 0;
    private Timer timer;
    private boolean isPaused = false;
    private boolean isCrashed = false;
    
    // --- ASSETS ---
    private BufferedImage planeImage; // Variable to hold our image
    
    // Game Objects
    private ArrayList<Wave> waves;
    private ArrayList<Bird> birds;
    private Random random;

    public OceanFlight() {
        this.setBackground(new Color(0, 105, 148));
        this.setFocusable(true);
        this.addKeyListener(this);

        // --- LOAD IMAGES ---
        try {
            // This looks for a file named "plane.png" in the same folder
            planeImage = ImageIO.read(new File("plane.png"));
            System.out.println("Image loaded successfully!");
        } catch (IOException e) {
            System.out.println("Image not found. Using fallback shapes.");
        }

        waves = new ArrayList<>();
        birds = new ArrayList<>();
        random = new Random();

        // Initial wave generation
        for (int i = 0; i < 20; i++) {
            waves.add(new Wave(random.nextInt(WIDTH), random.nextInt(HEIGHT)));
        }

        timer = new Timer(16, this);
        timer.start();
    }

    // --- Logic Loop ---
    @Override
    public void actionPerformed(ActionEvent e) {
        if (isCrashed) return;

        playerX += velX;
        if (playerX < 0) playerX = 0;
        if (playerX > getWidth()) playerX = getWidth();

        updateWaves();
        updateBirds();
        checkCollisions();

        repaint();
    }

    private void updateWaves() {
        for (Wave wave : waves) wave.y += SCROLL_SPEED;
        waves.removeIf(w -> w.y > HEIGHT);
        if (random.nextInt(100) < 15) {
            waves.add(new Wave(random.nextInt(WIDTH), -10));
        }
    }

    private void updateBirds() {
        for (Bird bird : birds) bird.y += (SCROLL_SPEED + 2);
        birds.removeIf(b -> b.y > HEIGHT);
        if (random.nextInt(100) < 2) {
            birds.add(new Bird(random.nextInt(WIDTH - 50), -50));
        }
    }

    private void checkCollisions() {
        // We use a smaller hitbox than the image size to be forgiving to the player
        Rectangle playerRect = new Rectangle(playerX + 10, playerY + 10, 50, 50);

        for (Bird bird : birds) {
            Rectangle birdRect = new Rectangle(bird.x, bird.y, 30, 20);
            if (playerRect.intersects(birdRect)) {
                triggerCrash();
                break;
            }
        }
    }

    private void triggerCrash() {
        isCrashed = true;
        repaint();
        timer.stop();
        SwingUtilities.invokeLater(() -> {
            int response = JOptionPane.showConfirmDialog(this, 
                "End to your endless journey.\nWill you buy a new aircraft?", 
                "CRASHED", 
                JOptionPane.YES_NO_OPTION);

            if (response == JOptionPane.YES_OPTION) {
                resetGame();
            } else {
                System.exit(0);
            }
        });
    }

    private void resetGame() {
        birds.clear();
        waves.clear();
        for (int i = 0; i < 20; i++) {
            waves.add(new Wave(random.nextInt(WIDTH), random.nextInt(HEIGHT)));
        }
        playerX = WIDTH / 2;
        isCrashed = false;
        velX = 0;
        timer.start();
        repaint();
    }

    // --- Drawing Loop ---
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw Waves
        g2d.setColor(new Color(255, 255, 255, 50));
        for (Wave wave : waves) {
            g2d.drawLine(wave.x, wave.y, wave.x + wave.width, wave.y);
        }

        // Draw Birds
        g2d.setColor(Color.BLACK);
        for (Bird bird : birds) {
            g2d.setStroke(new BasicStroke(3));
            g2d.drawLine(bird.x, bird.y, bird.x + 15, bird.y + 10);
            g2d.drawLine(bird.x + 15, bird.y + 10, bird.x + 30, bird.y);
        }

        // Draw Player OR Explosion
        if (isCrashed) {
            drawExplosion(g2d, playerX, playerY);
        } else {
            drawAircraft(g2d, playerX, playerY);
        }
        
        // Debug info if image failed
        if (planeImage == null) {
            g2d.setColor(Color.WHITE);
            g2d.drawString("Image not found: Ensure plane.png is in the folder", 10, 20);
        }
    }

    private void drawAircraft(Graphics2D g2d, int x, int y) {
        if (planeImage != null) {
            // --- NEW: Draw the Image ---
            // We resize the image to 70x70 pixels on the fly so it fits perfectly
            g2d.drawImage(planeImage, x, y, 70, 70, null);
        } else {
            // --- OLD: Fallback Shape ---
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillRoundRect(x - 5, y, 10, 60, 5, 5); // Fuselage
            Polygon wings = new Polygon();
            wings.addPoint(x, y + 15);
            wings.addPoint(x - 40, y + 45);
            wings.addPoint(x + 40, y + 45);
            g2d.fillPolygon(wings);
            g2d.setColor(new Color(255, 100, 0, 150));
            g2d.fillOval(x - 3, y + 60, 6, 10);
        }
    }

    private void drawExplosion(Graphics2D g2d, int x, int y) {
        g2d.setColor(Color.ORANGE);
        g2d.fillOval(x, y, 80, 80);
        g2d.setColor(Color.RED);
        g2d.fillOval(x + 15, y + 15, 50, 50);
        g2d.setColor(Color.YELLOW);
        g2d.fillOval(x + 30, y + 30, 20, 20);
    }

    // --- Input & Helpers ---
    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_LEFT) velX = -PLAYER_SPEED;
        else if (code == KeyEvent.VK_RIGHT) velX = PLAYER_SPEED;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT) velX = 0;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    private static class Wave {
        int x, y, width;
        public Wave(int x, int y) {
            this.x = x; this.y = y;
            this.width = 20 + new Random().nextInt(40);
        }
    }
    
    private static class Bird {
        int x, y;
        public Bird(int x, int y) {
            this.x = x; this.y = y;
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Ocean Flight - Textures");
        OceanFlight game = new OceanFlight();
        
        frame.add(game);
        frame.setSize(WIDTH, HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        
        frame.addWindowListener(new WindowAdapter() {
            public void windowIconified(WindowEvent e) { if(game.timer.isRunning()) game.timer.stop(); }
            public void windowDeiconified(WindowEvent e) { if(!game.isCrashed) game.timer.start(); }
        });

        frame.setVisible(true);
    }
}