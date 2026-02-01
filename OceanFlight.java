import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class OceanFlight extends JPanel implements ActionListener, KeyListener {

    // --- Game Settings ---
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int PLAYER_SPEED = 5;
    
    // --- State Variables ---
    private int playerX = WIDTH / 2; // Start in the middle
    private int playerY = HEIGHT - 150; // Fixed vertical position
    private int velX = 0; // Horizontal velocity
    private Timer timer;

    public OceanFlight() {
        // 1. Setup the "Ocean" Background
        this.setBackground(new Color(0, 105, 148)); // Deep Ocean Blue
        this.setFocusable(true);
        this.addKeyListener(this);

        // 2. Setup the Game Loop (Animation Timer)
        // Runs every 16ms (~60 Frames Per Second)
        timer = new Timer(16, this);
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Anti-aliasing for smoother shapes
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 3. Draw the Aircraft
        drawAircraft(g2d, playerX, playerY);
    }

    private void drawAircraft(Graphics2D g2d, int x, int y) {
        g2d.setColor(Color.LIGHT_GRAY);

        // Simple geometry for a top-down airplane
        // Fuselage (Body)
        g2d.fillRoundRect(x - 5, y, 10, 60, 5, 5);
        
        // Wings
        Polygon wings = new Polygon();
        wings.addPoint(x, y + 15);      // Top center
        wings.addPoint(x - 40, y + 45); // Left wing tip
        wings.addPoint(x + 40, y + 45); // Right wing tip
        g2d.fillPolygon(wings);

        // Tail
        Polygon tail = new Polygon();
        tail.addPoint(x, y + 50);       // Tail connection
        tail.addPoint(x - 15, y + 60);  // Left tail tip
        tail.addPoint(x + 15, y + 60);  // Right tail tip
        g2d.fillPolygon(tail);
        
        // Subtle engine glow (Animation effect)
        g2d.setColor(new Color(255, 100, 0, 150));
        g2d.fillOval(x - 3, y + 60, 6, 10);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // 4. Update Physics
        playerX += velX;

        // Keep player inside the screen boundaries
        if (playerX < 0) playerX = 0;
        if (playerX > getWidth()) playerX = getWidth();

        // 5. Trigger a Redraw
        repaint();
    }

    // --- Input Handling ---
    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_LEFT) {
            velX = -PLAYER_SPEED;
        } else if (code == KeyEvent.VK_RIGHT) {
            velX = PLAYER_SPEED;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        // Stop moving when the key is released
        if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_RIGHT) {
            velX = 0;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {} // Not used

    // --- Main Entry Point ---
    public static void main(String[] args) {
        JFrame frame = new JFrame("Ocean Flight");
        OceanFlight game = new OceanFlight();
        
        frame.add(game);
        frame.setSize(WIDTH, HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null); // Center on screen
        frame.setResizable(false);
        frame.setVisible(true);
    }
}