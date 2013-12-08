import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JTextField;

/**
 * Our improvements:
 *
 * 1. We'll provide two modes: seeker mode and manual mode. In seeker mode, the positions and weights of planets are
 * randomized by the program, and the seeker only knows the target and projectile positions. And in manual mode, the
 * positions and weights can also be specified by the user and user can see everything
 *
 * 2. In seeker mode, we'll provide two additional modes: endless mode and original mode. In endless mode, the number of
 * trials is not limited, so that the player can use this mode to come up with different strategies. In original mode,
 * there are only 5 trials
 *
 * 3. We'll draw gravitational force of both planets using different colors if it's the "manual mode", so that the
 * players will have a deeper understanding about how the gravity works
 *
 * 4. We'll display the trajectory history for both modes, so that the user can better analyze the situation
 *
 * 5. We'll randomize the target position and the projectile position for both modes, so that the users can test their
 * strategies in different situations
 *
 * @author JunHeeLee, JohnMu
 *
 */
public class GravityGameApplet extends JApplet implements ActionListener, AdjustmentListener, MouseListener,
MouseMotionListener {
    // Constants
    static final int SIZE_X = 500;
    static final int SIZE_Y = 500;
    static final int NUM_PLANETS = 2;
    static final int SUM_PLANET_WEIGHTS = 1000;
    static final int NUM_TRIALS = 5;
    static final double SPEED = 5;
    
    static final double SCALER = 1;
    static final int DOT_SIZE = 5;
    static final int INDICATOR_DIST = DOT_SIZE / 2 + 2;
    static final Color COLOR_SOURCE = new Color(127, 127, 255);
    static final Color COLOR_DESTINATION = new Color(255, 127, 63);
    static final Color COLOR_PLANETS = Color.WHITE;
    static final Color COLOR_INDICATOR = Color.GREEN;
    static final Color COLOR_PULL = new Color(0, 95, 47);
    static final double GRADIENT_SCALER = 10.0;
    static final int BOARD_WIDTH = (int) ((SIZE_X + 1) * SCALER + 0.5);
    static final int BOARD_HEIGHT = (int) ((SIZE_Y + 1) * SCALER + 0.5);
    static final int EXTRA_WIDTH = 300;
    static final int APPLET_WIDTH = BOARD_WIDTH + EXTRA_WIDTH;
    static final int APPLET_HEIGHT = BOARD_HEIGHT;
    
    static final double MIN_DIST_BETWEEN_S_D = 400;
    static final double MIN_DIST_BETWEEN_SD_P = 50;
    static final double MIN_DIST_BETWEEN_PLANETS = 100;
    static final int MIN_PLANET_WEIGHT = 100;
    
    // Game data
    boolean started; // Has the game started?
    int source; // Source location
    int destination; // Destination location
    int[] locations; // Planet locations
    int[] weights; // Planet weights
    int trial; // Number of trials so far
    int sumVelocities; // Sum of velocities so far
    double bestScore; // Best score so far
    boolean shooting; // Are we shooting now?
    boolean showPlanets; // For seeker mode
    boolean placing; // Are we placing planets now?
    boolean selected; // Is a planet selected?
    int selectedPlanet; // Selected planet
    boolean playing; // Are we playing now?
    String winner;
    String winnerScore;
    final static String WARNING = "<html>You can keep on playing, but this is your final score: ";
    List<Double> shootHistory;
    boolean showHistory;
    
    // Extra stuffs
    Random random;
    
    // GUI
    GravityGameCanvas canvas;
    JPanel panelHider;
    JLabel labHider;
    JLabel labLocX1;
    JLabel labLocY1;
    JLabel labWeight1;
    JLabel labLocX2;
    JLabel labLocY2;
    JLabel labWeight2;
    JTextField tfLocX1;
    JTextField tfLocY1;
    JTextField tfWeight1;
    JTextField tfLocX2;
    JTextField tfLocY2;
    JTextField tfWeight2;
    JScrollBar sbWeights;
    JButton butStart;
    JPanel panelSeeker;
    JLabel labSeeker;
    JLabel labTrial;
    JLabel labVelAngle;
    JLabel labScore;
    JTextField tfTrial;
    JTextField tfVelAngle;
    JTextField tfScore;
    JButton butShoot;
    JButton butRestart;
    JButton butHistory;
    JLabel warningSeeker;
    
    private JPanel gameModes;
    private JButton seekerMode;
    private JButton manualMode;
    private JLabel labGame;
    private JLabel srcColor;
    private JLabel dstColor;
    private JButton changePlanets;
    
    public String getWinner() {
        return winner;
    }
    
    public String getWinnerScore() {
        return winnerScore;
    }
    
    public void init() {
        started = false;
        
        locations = new int[NUM_PLANETS];
        weights = new int[NUM_PLANETS];
        random = new Random(System.currentTimeMillis());
        canvas = new GravityGameCanvas(BOARD_WIDTH, BOARD_HEIGHT);
        canvas.setBackground(Color.BLACK);
        canvas.addMouseListener(this);
        canvas.addMouseMotionListener(this);
        
        gameModes = new JPanel();
        labGame = new JLabel("Welcom to the gravity game!");
        srcColor = new JLabel("Source: Blue");
        dstColor = new JLabel("Target: Red");
        seekerMode = new JButton("Seeker Mode");
        manualMode = new JButton("Manual Mode");
        
        panelSeeker = new JPanel();
        labSeeker = new JLabel("Shoot projectiles!");
        labTrial = new JLabel("Trial:");
        labVelAngle = new JLabel("Velocity Angle:");
        labScore = new JLabel("Score:");
        tfTrial = new JTextField();
        tfTrial.setEditable(false);
        tfVelAngle = new JTextField();
        tfScore = new JTextField();
        tfScore.setEditable(false);
        butShoot = new JButton("Shoot");
        butRestart = new JButton("Restart");
        changePlanets = new JButton("Show/Hide Planets");
        butHistory = new JButton("Turn the history on/off");
        warningSeeker = new JLabel();
        
        panelHider = new JPanel();
        labHider = new JLabel("Place your planets");
        labLocX1 = new JLabel("Planet 1 X:");
        labLocY1 = new JLabel("Planet 1 Y:");
        labWeight1 = new JLabel("Planet 1 Weight:");
        labLocX2 = new JLabel("Planet 2 X:");
        labLocY2 = new JLabel("Planet 2 Y:");
        labWeight2 = new JLabel("Planet 2 Weight:");
        tfLocX1 = new JTextField();
        tfLocY1 = new JTextField();
        tfWeight1 = new JTextField();
        tfLocX2 = new JTextField();
        tfLocY2 = new JTextField();
        tfWeight2 = new JTextField();
        sbWeights = new JScrollBar();
        sbWeights.setOrientation(JScrollBar.HORIZONTAL);
        sbWeights.setMinimum(10);
        sbWeights.setMaximum(SUM_PLANET_WEIGHTS);
        butStart = new JButton("Start");
        
        createUI();
    }
    
    public void stop() {
    }
    
    public void start() {
        // Create layout
        setLayout(null);
        setSize(APPLET_WIDTH, APPLET_HEIGHT);
        add(canvas);
        canvas.setBounds(0, 0, BOARD_WIDTH, BOARD_HEIGHT);
        add(gameModes);
        gameModes.setBounds(BOARD_WIDTH, 0, EXTRA_WIDTH, BOARD_HEIGHT);
        add(panelHider);
        panelHider.setBounds(BOARD_WIDTH, 0, EXTRA_WIDTH, BOARD_HEIGHT);
        add(panelSeeker);
        panelSeeker.setBounds(BOARD_WIDTH, 0, EXTRA_WIDTH, BOARD_HEIGHT);
        
        // Init game
        initGame();
    }
    
    // Checked
    private void createUI() {
        // Temporary panel
        JPanel p;
        
        gameModes.setLayout(new GridLayout(10, 1));
        gameModes.setSize(EXTRA_WIDTH, BOARD_HEIGHT);
        gameModes.add(labGame);
        gameModes.add(srcColor);
        gameModes.add(dstColor);
        gameModes.add(seekerMode);
        gameModes.add(manualMode);
        seekerMode.addActionListener(this);
        manualMode.addActionListener(this);
        
        panelHider.setLayout(new GridLayout(10, 1));
        panelHider.setSize(EXTRA_WIDTH, BOARD_HEIGHT);
        panelHider.add(labHider);
        p = new JPanel();
        p.setLayout(new GridLayout(1, 2));
        p.add(labLocX1);
        p.add(tfLocX1);
        panelHider.add(p);
        p = new JPanel();
        p.setLayout(new GridLayout(1, 2));
        p.add(labLocY1);
        p.add(tfLocY1);
        panelHider.add(p);
        p = new JPanel();
        p.setLayout(new GridLayout(1, 2));
        p.add(labWeight1);
        p.add(tfWeight1);
        panelHider.add(p);
        panelHider.add(sbWeights);
        p = new JPanel();
        p.setLayout(new GridLayout(1, 2));
        p.add(labLocX2);
        p.add(tfLocX2);
        panelHider.add(p);
        p = new JPanel();
        p.setLayout(new GridLayout(1, 2));
        p.add(labLocY2);
        p.add(tfLocY2);
        panelHider.add(p);
        p = new JPanel();
        p.setLayout(new GridLayout(1, 2));
        p.add(labWeight2);
        p.add(tfWeight2);
        panelHider.add(p);
        panelHider.add(butStart);
        // John: cbMode.addMouseListener(this);
        tfLocX1.addActionListener(this);
        tfLocY1.addActionListener(this);
        tfWeight1.addActionListener(this);
        tfLocX2.addActionListener(this);
        tfLocY2.addActionListener(this);
        tfWeight2.addActionListener(this);
        tfWeight2.setEnabled(false);
        sbWeights.addAdjustmentListener(this);
        butStart.addActionListener(this);
        
        // Seeker panel
        panelSeeker.setLayout(new GridLayout(10, 1));
        panelSeeker.setSize(EXTRA_WIDTH, BOARD_HEIGHT);
        panelSeeker.add(labSeeker);
        p = new JPanel();
        p.setLayout(new GridLayout(1, 2));
        p.add(labTrial);
        p.add(tfTrial);
        panelSeeker.add(p);
        p = new JPanel();
        p.setLayout(new GridLayout(1, 2));
        p.add(labVelAngle);
        p.add(tfVelAngle);
        panelSeeker.add(p);
        p = new JPanel();
        p.setLayout(new GridLayout(1, 2));
        p.add(labScore);
        p.add(tfScore);
        panelSeeker.add(p);
        panelSeeker.add(butShoot);
        panelSeeker.add(butRestart);
        panelSeeker.add(changePlanets);
        panelSeeker.add(butHistory);
        panelSeeker.add(warningSeeker);
        tfVelAngle.addActionListener(this);
        butShoot.addActionListener(this);
        butRestart.addActionListener(this);
        changePlanets.addActionListener(this);
        butHistory.addActionListener(this);
    }
    
    private double getDist(int source, int destination) {
        int x1 = getX(source);
        int y1 = getY(source);
        int x2 = getX(destination);
        int y2 = getY(destination);
        return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }
    
    private int getLoc(int x, int y) {
        return x * (SIZE_Y + 1) + y;
    }
    
    private int getX(int loc) {
        return loc / (SIZE_Y + 1);
    }
    
    private int getY(int loc) {
        return loc % (SIZE_Y + 1);
    }
    
    private boolean isValid(String str) {
        try {
            Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
    
    boolean checkSrcDstLocations() {
        if (getDist(source, destination) < MIN_DIST_BETWEEN_S_D
            || getX(source) / (SIZE_X / 4) == getX(destination) / (SIZE_X / 4)
            || getY(source) / (SIZE_Y / 4) == getY(destination) / (SIZE_Y / 4))
            return false;
        return true;
    }
    
    boolean checkPlanetLocations() {
        double distSrcDst = getDist(source, destination);
        for (int i = 0; i < NUM_PLANETS; ++i) {
            if (getDist(source, locations[i]) < MIN_DIST_BETWEEN_SD_P
                || getDist(source, locations[i]) > distSrcDst * 0.7
                || getDist(destination, locations[i]) < MIN_DIST_BETWEEN_SD_P
                || getDist(destination, locations[i]) > distSrcDst * 0.7)
                return false;
            for (int j = i + 1; j < NUM_PLANETS; ++j)
                if (getDist(locations[i], locations[j]) < MIN_DIST_BETWEEN_PLANETS)
                    return false;
        }
        double dot = ((getX(source) - getX(destination)) * (getX(locations[0]) - getX(locations[1]))
                      + (getY(source) - getY(destination)) * (getY(locations[0]) - getY(locations[1])))
        / getDist(source, destination) / getDist(locations[0], locations[1]);
        if (Math.abs(dot) < 0.3)
            return false;
        return true;
    }
    
    private void initGame() {
        started = false;
        showPlanets = false;
        placing = true;
        winner = "unknown";
        winnerScore = "0";
        playing = false;
        showHistory = false;
        shootHistory = new ArrayList<Double>();
        
        // Switch the panel
        gameModes.setVisible(true);
        panelSeeker.setVisible(false);
        panelHider.setVisible(false);
        warningSeeker.setText("");
        
        do {
            source = getLoc(random.nextInt(SIZE_X), random.nextInt(SIZE_Y));
            destination = getLoc(random.nextInt(SIZE_X), random.nextInt(SIZE_Y));
        } while (!checkSrcDstLocations());
        
        // Default planet locations for NUM_PLANETS=2
        locations[0] = getLoc(SIZE_X / 4, 2 * SIZE_Y / 3);
        locations[1] = getLoc(3 * SIZE_X / 4, SIZE_Y / 4);
        
        // Default weights
        weights[NUM_PLANETS - 1] = SUM_PLANET_WEIGHTS;
        for (int i = 0; i < NUM_PLANETS - 1; ++i)
            weights[NUM_PLANETS - 1] -= (weights[i] = SUM_PLANET_WEIGHTS / NUM_PLANETS);
        
        // Init game params
        trial = 0;
        sumVelocities = 0;
        bestScore = SIZE_X + SIZE_Y; // Something larger than the max distance in the board
        shooting = false;
        
        initCanvasSeeker();
    }
    
    private void initCanvasSeeker() {
        Graphics g = canvas.getOffscreenGraphics();
        
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);
        
        g.setColor(COLOR_SOURCE);
        g.fillOval((int) (getX(source) * SCALER) - DOT_SIZE / 2, (int) (getY(source) * SCALER) - DOT_SIZE / 2,
                   DOT_SIZE, DOT_SIZE);
        g.setColor(COLOR_DESTINATION);
        g.fillOval((int) (getX(destination) * SCALER) - DOT_SIZE / 2,
                   (int) (getY(destination) * SCALER) - DOT_SIZE / 2, DOT_SIZE, DOT_SIZE);
        
        canvas.repaint();
    }
    
    private void initCanvasHider() {
        Graphics g = canvas.getOffscreenGraphics();
        
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);
        
        g.setColor(COLOR_SOURCE);
        g.fillOval((int) (getX(source) * SCALER) - DOT_SIZE / 2, (int) (getY(source) * SCALER) - DOT_SIZE / 2,
                   DOT_SIZE, DOT_SIZE);
        g.setColor(COLOR_DESTINATION);
        g.fillOval((int) (getX(destination) * SCALER) - DOT_SIZE / 2,
                   (int) (getY(destination) * SCALER) - DOT_SIZE / 2, DOT_SIZE, DOT_SIZE);
        
        g.setColor(COLOR_PLANETS);
        for (int i = 0; i < NUM_PLANETS; ++i)
            g.fillOval((int) (getX(locations[i]) * SCALER) - DOT_SIZE / 2, (int) (getY(locations[i]) * SCALER)
                       - DOT_SIZE / 2, DOT_SIZE, DOT_SIZE);
        
        canvas.repaint();
    }
    
    private void placePlanets() {
        do {
            locations[0] = getLoc(random.nextInt(SIZE_X), random.nextInt(SIZE_Y));
            locations[1] = getLoc(random.nextInt(SIZE_X), random.nextInt(SIZE_Y));
        } while (!checkPlanetLocations());
        
        weights[0] = random.nextInt(SUM_PLANET_WEIGHTS - 2 * MIN_PLANET_WEIGHT) + MIN_PLANET_WEIGHT;
        weights[1] = SUM_PLANET_WEIGHTS - weights[0];
    }
    
    private void startGame() {
        
        // Switch the panel
        gameModes.setVisible(false);
        panelHider.setVisible(false);
        panelSeeker.setVisible(true);
        
        // Default values
        tfTrial.setText("1");
        tfVelAngle.setText("");
        tfScore.setText("N/A");
        
        // Now we are playing
        playing = true;
    }
    
    private void hiderConfig() {
        tfLocX1.setText("" + getX(locations[0]));
        tfLocY1.setText("" + (SIZE_Y - getY(locations[0])));
        tfWeight1.setText("" + weights[0]);
        tfLocX2.setText("" + getX(locations[1]));
        tfLocY2.setText("" + (SIZE_Y - getY(locations[1])));
        tfWeight2.setText("" + weights[1]);
        sbWeights.setValue(weights[1]);
    }
    
    private void shootProjectile(double velX, double velY) {
        shooting = true;
        
        double locX = getX(source);
        double locY = getY(source);
        // System.out.println("source locs: " + locX + ", " + locY);
        
        while (locX >= -SIZE_X && locX <= 2 * SIZE_X && locY >= -SIZE_Y && locY <= 2 * SIZE_Y) {
            
            double[] accel = getAccel(locX, locY);
            // System.out.println("accel: " + accel[0] + ", " + accel[1]);
            // System.out.println("vel: " + velX + ", " + velY);
            
            if (Math.abs(accel[2]) > 20)
                break;
            
            locX += velX + accel[0] / 2;
            locY += velY + accel[1] / 2;
            velX += accel[0];
            velY += accel[1];
            
            if (showHistory)
                updateCanvasSeeker(locX, locY, accel[2]);
            
            double dist =
            Math.sqrt((getX(destination) - locX) * (getX(destination) - locX)
                      + (getY(destination) - locY)
                      * (getY(destination) - locY));
            if (dist < bestScore) {
                bestScore = dist;
                tfScore.setText(String.format("%.2f", dist));
            }
        }
        
        onClickAngle();
        shooting = false;
    }
    
    private Color getGradient(double pull) {
        double ratio = Math.tanh(pull * GRADIENT_SCALER);
        int r1 = COLOR_PULL.getRed();
        int g1 = COLOR_PULL.getGreen();
        int b1 = COLOR_PULL.getBlue();
        int r2 = COLOR_PLANETS.getRed();
        int g2 = COLOR_PLANETS.getGreen();
        int b2 = COLOR_PLANETS.getBlue();
        
        int r = (int) (r1 * (1 - ratio) + r2 * ratio);
        int g = (int) (g1 * (1 - ratio) + g2 * ratio);
        int b = (int) (b1 * (1 - ratio) + b2 * ratio);
        
        return new Color(r, g, b);
    }
    
    private void updateCanvasSeeker(double locX, double locY, double pull) {
        Graphics g = canvas.getOffscreenGraphics();
        
        g.setColor(getGradient(pull));
        g.drawRect((int) (locX * SCALER + 0.5), (int) (locY * SCALER + 0.5), 1, 1);
        
        canvas.repaint();
    }
    
    private double[] getAccel(double locX, double locY) {
        // System.out.println("projectile locs: " + locX + ", " + locY);
        double aX = 0;
        double aY = 0;
        double maxA = 0;
        for (int i = 0; i < NUM_PLANETS; i++) {
            double[] pulls = getPulls(getX(locations[i]) - locX, getY(locations[i]) - locY, weights[i]);
            aX += pulls[0];
            aY += pulls[1];
            if (pulls[2] > maxA)
                maxA = pulls[2];
        }
        return new double[] {aX, aY, maxA };
    }
    
    private double[] getPulls(double relX, double relY, double weight) {
        double distSq = relX * relX + relY * relY;
        double dist = Math.sqrt(distSq);
        double pull = weight / distSq;
        return new double[] {pull * relX / dist, pull * relY / dist, pull };
    }
    
    private void onClickStart() {
        // If we are in seeker only mode, randomly place planets first
        // placePlanets();
        
        // We are done placing planets
        placing = false;
        selected = false;
        
        // Start the game
        startGame();
    }
    
    private void onClickShoot() {
        if (shooting)
            return;
        
        if (!isValid(tfVelAngle.getText()))
            return;
        
        // double velX = Double.parseDouble(tfVelX.getText());
        // double velY = Double.parseDouble(tfVelY.getText());
        double velAngle = Double.parseDouble(tfVelAngle.getText()) * Math.PI / 180;
        shootHistory.add(velAngle);
        double velX = SPEED * Math.cos(velAngle);
        double velY = -SPEED * Math.sin(velAngle);
        
        shootProjectile(velX, velY);
        
        ++trial;
        tfTrial.setText((trial + 1) + "");
        if (trial == NUM_TRIALS) {
            changePlanets.setEnabled(true);
            butHistory.setEnabled(true);
            if (!showPlanets)
                warningSeeker.setText(WARNING + String.format("%.2f", bestScore));
        }
    }
    
    private void onClickRestart() {
        initGame();
    }
    
    private void onEditLocX1() {
        try {
            int val = Integer.parseInt(tfLocX1.getText());
            if (val >= 0 && val <= SIZE_X) {
                locations[0] = getLoc(val, getY(locations[0]));
                initCanvasHider();
            }
        } catch (NumberFormatException e) {
        }
        tfLocX1.setText("" + getX(locations[0]));
    }
    
    private void onEditLocY1() {
        try {
            int val = SIZE_Y - Integer.parseInt(tfLocY1.getText());
            if (val >= 0 && val <= SIZE_Y) {
                locations[0] = getLoc(getX(locations[0]), val);
                initCanvasHider();
            }
        } catch (NumberFormatException e) {
        }
        tfLocY1.setText("" + (SIZE_Y - getY(locations[0])));
    }
    
    private void onEditWeight1() {
        try {
            int val = Integer.parseInt(tfWeight1.getText());
            if (val > 0 && val < SUM_PLANET_WEIGHTS) {
                weights[0] = val;
                weights[1] = SUM_PLANET_WEIGHTS - val;
                initCanvasHider();
            }
        } catch (NumberFormatException e) {
        }
        tfWeight1.setText("" + weights[0]);
        tfWeight2.setText("" + weights[1]);
    }
    
    private void onEditLocX2() {
        try {
            int val = Integer.parseInt(tfLocX2.getText());
            if (val >= 0 && val <= SIZE_X) {
                locations[1] = getLoc(val, getY(locations[1]));
                initCanvasHider();
            }
        } catch (NumberFormatException e) {
        }
        tfLocX2.setText("" + getX(locations[1]));
    }
    
    private void onEditLocY2() {
        try {
            int val = SIZE_Y - Integer.parseInt(tfLocY2.getText());
            if (val >= 0 && val <= SIZE_Y) {
                locations[1] = getLoc(getX(locations[1]), val);
                initCanvasHider();
            }
        } catch (NumberFormatException e) {
        }
        tfLocY2.setText("" + (SIZE_Y - getY(locations[1])));
    }
    
    private void onEditWeight2() {
        try {
            int val = Integer.parseInt(tfWeight2.getText());
            if (val > 0 && val < SUM_PLANET_WEIGHTS) {
                weights[1] = val;
                weights[0] = SUM_PLANET_WEIGHTS - val;
                initCanvasHider();
            }
        } catch (NumberFormatException e) {
        }
        tfWeight1.setText("" + weights[0]);
        tfWeight2.setText("" + weights[1]);
    }
    
    private void onClickSeeker() {
        
        // Switch the panel
        gameModes.setVisible(false);
        panelHider.setVisible(false);
        panelSeeker.setVisible(true);
        
        started = true;
        placing = false;
        
        butShoot.setEnabled(true);
        changePlanets.setEnabled(false);
        butHistory.setEnabled(false);
        placePlanets();
        startGame();
    }
    
    private void onClickManual() {
        // Switch the panel
        gameModes.setVisible(false);
        panelHider.setVisible(true);
        panelSeeker.setVisible(false);
        
        // We are placing planets now
        showPlanets = true;
        started = true;
        
        butShoot.setEnabled(true);
        hiderConfig();
        initCanvasHider();
    }
    
    private void onClickPlanets() {
        Graphics g = canvas.getOffscreenGraphics();
        
        showPlanets = !showPlanets;
        if (showPlanets) {
            g.setColor(COLOR_PLANETS);
            for (int i = 0; i < NUM_PLANETS; ++i)
                g.fillOval((int) (getX(locations[i]) * SCALER) - DOT_SIZE / 2, (int) (getY(locations[i]) * SCALER)
                           - DOT_SIZE / 2, DOT_SIZE, DOT_SIZE);
        } else {
            g.setColor(Color.BLACK);
            for (int i = 0; i < NUM_PLANETS; ++i)
                g.fillOval((int) (getX(locations[i]) * SCALER) - DOT_SIZE / 2, (int) (getY(locations[i]) * SCALER)
                           - DOT_SIZE / 2, DOT_SIZE, DOT_SIZE);
        }
        canvas.repaint();
    }
    
    private void onClickAngle() {
        if (!isValid(tfVelAngle.getText()))
            return;
        
        double angle = Double.parseDouble(tfVelAngle.getText()) * Math.PI / 180;
        
        Graphics g = canvas.getOffscreenGraphics();
        
        int x = (int) (getX(source) + Math.cos(angle) * INDICATOR_DIST + 0.5);
        int y = (int) (getY(source) - Math.sin(angle) * INDICATOR_DIST + 0.5);
        
        g.setColor(Color.BLACK);
        g.fillOval((int) (getX(source) * SCALER) - DOT_SIZE / 2 - INDICATOR_DIST,
                   (int) (getY(source) * SCALER) - DOT_SIZE / 2 - INDICATOR_DIST,
                   DOT_SIZE + INDICATOR_DIST * 2 + 1, DOT_SIZE + INDICATOR_DIST * 2 + 1);
        g.setColor(COLOR_SOURCE);
        g.fillOval((int) (getX(source) * SCALER) - DOT_SIZE / 2, (int) (getY(source) * SCALER) - DOT_SIZE / 2,
                   DOT_SIZE, DOT_SIZE);
        g.setColor(COLOR_INDICATOR);
        g.drawRect(x, y, 1, 1);
        
        canvas.repaint();
    }
    
    private void onClickHistory() {
        showHistory = !showHistory;
        if (showHistory) {
            for (double angle : shootHistory) {
                double velX = SPEED * Math.cos(angle);
                double velY = -SPEED * Math.sin(angle);
                shootProjectile(velX, velY);
            }
        } else {
            if (showPlanets)
                initCanvasHider();
            else
                initCanvasSeeker();
        }
    }
    
    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();
        
        if (obj == butStart)
            onClickStart();
        else if (obj == butShoot)
            onClickShoot();
        else if (obj == butRestart)
            onClickRestart();
        else if (obj == tfLocX1)
            onEditLocX1();
        else if (obj == tfLocY1)
            onEditLocY1();
        else if (obj == tfWeight1)
            onEditWeight1();
        else if (obj == tfLocX2)
            onEditLocX2();
        else if (obj == tfLocY2)
            onEditLocY2();
        else if (obj == tfWeight2)
            onEditWeight2();
        else if (obj == seekerMode)
            onClickSeeker();
        else if (obj == manualMode)
            onClickManual();
        else if (obj == changePlanets)
            onClickPlanets();
        else if (obj == tfVelAngle)
            onClickAngle();
        else if (obj == butHistory)
            onClickHistory();
    }
    
    public void adjustmentValueChanged(AdjustmentEvent e) {
        int val = e.getValue();
        tfWeight2.setText("" + val);
        onEditWeight2();
    }
    
    public void mousePressed(MouseEvent e) {
        // If we are not placing planets now, just return
        if (!placing)
            return;
        
        // JL: for some reason, these are off by (2, 4)
        int x = e.getX() - 2;
        int y = e.getY() - 4;
        
        int planet = -1;
        double minDist = SIZE_X * SIZE_X + SIZE_Y * SIZE_Y;
        for (int i = 0; i < NUM_PLANETS; ++i) {
            double dist = getDist(locations[i], getLoc((int) (x / SCALER), (int) (y / SCALER)));
            if (dist < minDist) {
                planet = i;
                minDist = dist;
            }
        }
        
        // Grab if close enough
        if (planet >= 0 && minDist < 10) {
            selected = true;
            selectedPlanet = planet;
        } else if (!started) {
            double dist = getDist(source, getLoc((int) (x / SCALER), (int) (y / SCALER)));
            if (dist < 10) {
                selected = true;
                selectedPlanet = Integer.MAX_VALUE;
            } else {
                dist = getDist(destination, getLoc((int) (x / SCALER), (int) (y / SCALER)));
                if (dist < 10) {
                    selected = true;
                    selectedPlanet = Integer.MIN_VALUE;
                }
            }
        }
    }
    
    public void mouseDragged(MouseEvent e) {
        if (!placing || !selected)
            return;
        
        // JL: for some reason, these are off by (2, 4)
        int x = e.getX() - 2;
        int y = e.getY() - 4;
        
        // Calculate new planet location
        int newX = (int) (x / SCALER + 0.5);
        int newY = (int) (y / SCALER + 0.5);
        
        // Check ranges
        if (newX < 0)
            newX = 0;
        if (newX >= SIZE_X)
            newX = SIZE_X - 1;
        if (newY < 0)
            newY = 0;
        if (newY >= SIZE_Y)
            newY = SIZE_Y - 1;
        
        // Update location
        if (selectedPlanet == Integer.MAX_VALUE) {
            source = getLoc(newX, newY);
            initCanvasSeeker();
        } else if (selectedPlanet == Integer.MIN_VALUE) {
            destination = getLoc(newX, newY);
            initCanvasSeeker();
        } else {
            locations[selectedPlanet] = getLoc(newX, newY);
            hiderConfig();
            initCanvasHider();
        }
    }
    
    public void mouseReleased(MouseEvent e) {
        if (!placing)
            return;
        
        selected = false;
    }
    
    public void mouseMoved(MouseEvent e) {
        if (shooting || !playing)
            return;
        
        // JL: for some reason, these are off by (2, 4)
        int x = e.getX() - 2;
        int y = e.getY() - 4;
        
        if (getLoc(x, y) == source) {
            // We are pointing at the source
            return;
        }
        
        double angle = Math.atan2(getY(source) - y, x - getX(source));
        tfVelAngle.setText(String.format("%.3f", angle / Math.PI * 180));
        
        onClickAngle();
    }
    
    public void mouseClicked(MouseEvent e) {
        if (shooting || !playing)
            return;
        
        onClickShoot();
    }
    
    public void mouseEntered(MouseEvent e) {
    }
    
    public void mouseExited(MouseEvent e) {
    }
    
    private class GravityGameCanvas extends Canvas {
        Image m_imageOffscreen;
        Graphics m_graphOffscreen;
        int m_uWidth;
        int m_uHeight;
        
        public GravityGameCanvas(int uWidth, int uHeight) {
            super();
            
            m_uWidth = uWidth;
            m_uHeight = uHeight;
            setSize(m_uWidth, m_uHeight);
            setBounds(0, 0, m_uWidth, m_uHeight);
            
            m_imageOffscreen = null;
            m_graphOffscreen = null;
        }
        
        public Graphics getOffscreenGraphics() {
            if (m_imageOffscreen == null || m_graphOffscreen == null) {
                m_imageOffscreen = this.createImage(m_uWidth, m_uHeight);
                m_graphOffscreen = m_imageOffscreen.getGraphics();
            }
            
            return m_graphOffscreen;
        }
        
        public void update(Graphics g) {
            paint(g);
        }
        
        public void paint(Graphics g) {
            if (m_imageOffscreen == null || m_graphOffscreen == null) {
                m_imageOffscreen = this.createImage(m_uWidth, m_uHeight);
                m_graphOffscreen = m_imageOffscreen.getGraphics();
            }
            
            if (m_imageOffscreen != null) {
                g.drawImage(m_imageOffscreen, 0, 0, m_uWidth, m_uHeight, this);
            } else {
                // Couldn't create image properly
            }
        }
    }
}
