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
    static final double SPEED = 3;
    
    static final int PLANET_SIZE = 13;
    static final int INDICATOR_DIST = PLANET_SIZE / 2 + 2;
    static final Color COLOR_SOURCE = new Color(127, 127, 255);
    static final Color COLOR_DESTINATION = new Color(255, 63, 63);
    static final Color COLOR_PLANETS = Color.WHITE;
    static final Color COLOR_INDICATOR = Color.GREEN;
    static final Color COLOR_PULL = new Color(41, 47, 24);
    static final double GRADIENT_SCALER = 4.0;
    static final int BOARD_WIDTH = SIZE_X + 1;
    static final int BOARD_HEIGHT = SIZE_Y + 1;
    static final int EXTRA_WIDTH = 250;
    static final int EXTRA_HEIGHT = 24;
    static final int APPLET_WIDTH = BOARD_WIDTH + EXTRA_WIDTH;
    static final int APPLET_HEIGHT = BOARD_HEIGHT + EXTRA_HEIGHT;
    static final Color COLOR_PANEL_TEXTS = new Color(178, 167, 160);
    static final Color COLOR_PANEL_BACKGROUND = new Color(51, 37, 24);
    
    static final double MIN_DIST_BETWEEN_S_D = 400;
    static final double MIN_DIST_BETWEEN_SD_P = 50;
    static final double MIN_DIST_BETWEEN_PLANETS = 100;
    static final int MIN_PLANET_WEIGHT = 100;
    
    final double[] PLANET_SHADE = getPlanetShade();
    
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
    boolean endless; // Allow infinite trials?
    String winner;
    String winnerScore;
    
    // Extra stuffs
    Random random;
    
    // GUI
    GravityGameCanvas canvas;
    JPanel panelHider;
    JLabel labHider;
    JLabel labSrc;
    JLabel labSrcLoc;
    JLabel labDst;
    JLabel labDstLoc;
    JLabel labPlanets;
    JLabel labLoc1;
    JLabel labWeight1;
    JLabel labLoc2;
    JLabel labWeight2;
    JTextField tfSrcX;
    JTextField tfSrcY;
    JTextField tfDstX;
    JTextField tfDstY;
    JTextField tfLocX1;
    JTextField tfLocY1;
    JTextField tfWeight1;
    JTextField tfLocX2;
    JTextField tfLocY2;
    JTextField tfWeight2;
    JLabel labBar;
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
    JLabel warningSeeker;
    JPanel panelAuthors;
    JLabel labAuthors;
    
    private JPanel gameModes;
    private JButton seekerMode;
    private JButton manualMode;
    private JLabel labGame;
    private JLabel labInstr;
    private JLabel labModes;
    private JLabel labGoal;
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
        gameModes.setBackground(COLOR_PANEL_BACKGROUND);
        labGame = new JLabel("<html><b>Welcome to Gravity Game!</b></html>", JLabel.CENTER);
        labGame.setForeground(COLOR_PANEL_TEXTS);
        labInstr = new JLabel("<html><center>Send a projectile from source (blue) to target (red) in an unknown gravitational field by two planets.</center></html>");
        labInstr.setForeground(COLOR_PANEL_TEXTS);
        labModes = new JLabel("<html><center>You can play a normal game with hidden planets in seeker mode, or a practice game with custom positions in manual mode.</center></html>");
        labModes.setForeground(COLOR_PANEL_TEXTS);
        labGoal = new JLabel("<html><center>You have 5 chances to shoot. Your score is the closest distance you get to the target. The score is only recorded in seeker mode.</center></html>");
        labGoal.setForeground(COLOR_PANEL_TEXTS);
        seekerMode = new JButton("Start Game in Seeker Mode");
        manualMode = new JButton("Place Planets in Manual Mode");
        
        panelSeeker = new JPanel();
        panelSeeker.setBackground(COLOR_PANEL_BACKGROUND);
        labSeeker = new JLabel("<html><b>Aim and click on screen to shoot!</b></html>", JLabel.CENTER);
        labSeeker.setForeground(COLOR_PANEL_TEXTS);
        labTrial = new JLabel("Trial:", JLabel.RIGHT);
        labTrial.setForeground(COLOR_PANEL_TEXTS);
        labVelAngle = new JLabel("Angle:", JLabel.RIGHT);
        labVelAngle.setForeground(COLOR_PANEL_TEXTS);
        labScore = new JLabel("Score:", JLabel.RIGHT);
        labScore.setForeground(COLOR_PANEL_TEXTS);
        tfTrial = new JTextField();
        tfTrial.setForeground(COLOR_PANEL_TEXTS);
        tfTrial.setBackground(COLOR_PANEL_BACKGROUND);
        tfTrial.setEditable(false);
        tfVelAngle = new JTextField();
        tfVelAngle.setForeground(COLOR_PANEL_TEXTS);
        tfVelAngle.setBackground(COLOR_PANEL_BACKGROUND);
        tfScore = new JTextField();
        tfScore.setForeground(COLOR_PANEL_TEXTS);
        tfScore.setBackground(COLOR_PANEL_BACKGROUND);
        tfScore.setEditable(false);
        butShoot = new JButton("Shoot Projectile");
        butRestart = new JButton("Restart");
        changePlanets = new JButton("Show/Hide Planets");
        warningSeeker = new JLabel("<html><center>You can keep playing, but the score will not be updated.</center></html>");
        warningSeeker.setForeground(COLOR_PANEL_TEXTS);
        warningSeeker.setVisible(false);
        
        panelAuthors = new JPanel();
        panelAuthors.setBackground(COLOR_PANEL_BACKGROUND);
        labAuthors = new JLabel("<html><center><b>JJ</b>: Jun Hee Lee, John Mu (2013)</center></html>");
        labAuthors.setForeground(COLOR_PANEL_TEXTS);
        
        panelHider = new JPanel();
        panelHider.setBackground(COLOR_PANEL_BACKGROUND);
        labHider = new JLabel("<html><b>Drag planets to reposition</b></html>", JLabel.CENTER);
        labHider.setForeground(COLOR_PANEL_TEXTS);
        labSrc = new JLabel("Source (Blue)", JLabel.CENTER);
        labSrc.setForeground(COLOR_PANEL_TEXTS);
        labSrcLoc = new JLabel("Location:", JLabel.RIGHT);
        labSrcLoc.setForeground(COLOR_PANEL_TEXTS);
        labDst = new JLabel("Target (Red)", JLabel.CENTER);
        labDst.setForeground(COLOR_PANEL_TEXTS);
        labDstLoc = new JLabel("Location:", JLabel.RIGHT);
        labDstLoc.setForeground(COLOR_PANEL_TEXTS);
        labPlanets = new JLabel("Planets (White)", JLabel.CENTER);
        labPlanets.setForeground(COLOR_PANEL_TEXTS);
        labLoc1 = new JLabel("Location:", JLabel.RIGHT);
        labLoc1.setForeground(COLOR_PANEL_TEXTS);
        labWeight1 = new JLabel("Weight:", JLabel.RIGHT);
        labWeight1.setForeground(COLOR_PANEL_TEXTS);
        labLoc2 = new JLabel("Location:", JLabel.RIGHT);
        labLoc2.setForeground(COLOR_PANEL_TEXTS);
        labWeight2 = new JLabel("Weight:", JLabel.RIGHT);
        labWeight2.setForeground(COLOR_PANEL_TEXTS);
        labBar = new JLabel("<html>Drag to<br>adjust &darr</html>", JLabel.CENTER);
        labBar.setForeground(COLOR_PANEL_TEXTS);
        labBar.setVerticalAlignment(JLabel.BOTTOM);
        tfSrcX = new JTextField();
        tfSrcX.setForeground(COLOR_PANEL_TEXTS);
        tfSrcX.setBackground(COLOR_PANEL_BACKGROUND);
        tfSrcY = new JTextField();
        tfSrcY.setForeground(COLOR_PANEL_TEXTS);
        tfSrcY.setBackground(COLOR_PANEL_BACKGROUND);
        tfDstX = new JTextField();
        tfDstX.setForeground(COLOR_PANEL_TEXTS);
        tfDstX.setBackground(COLOR_PANEL_BACKGROUND);
        tfDstY = new JTextField();
        tfDstY.setForeground(COLOR_PANEL_TEXTS);
        tfDstY.setBackground(COLOR_PANEL_BACKGROUND);
        tfLocX1 = new JTextField();
        tfLocX1.setForeground(COLOR_PANEL_TEXTS);
        tfLocX1.setBackground(COLOR_PANEL_BACKGROUND);
        tfLocY1 = new JTextField();
        tfLocY1.setForeground(COLOR_PANEL_TEXTS);
        tfLocY1.setBackground(COLOR_PANEL_BACKGROUND);
        tfWeight1 = new JTextField();
        tfWeight1.setForeground(COLOR_PANEL_TEXTS);
        tfWeight1.setBackground(COLOR_PANEL_BACKGROUND);
        tfLocX2 = new JTextField();
        tfLocX2.setForeground(COLOR_PANEL_TEXTS);
        tfLocX2.setBackground(COLOR_PANEL_BACKGROUND);
        tfLocY2 = new JTextField();
        tfLocY2.setForeground(COLOR_PANEL_TEXTS);
        tfLocY2.setBackground(COLOR_PANEL_BACKGROUND);
        tfWeight2 = new JTextField();
        tfWeight2.setForeground(COLOR_PANEL_TEXTS);
        tfWeight2.setBackground(COLOR_PANEL_BACKGROUND);
        sbWeights = new JScrollBar();
        sbWeights.setOrientation(JScrollBar.HORIZONTAL);
        sbWeights.setMinimum(10);
        sbWeights.setMaximum(SUM_PLANET_WEIGHTS);
        butStart = new JButton("Start Game");
        
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
        gameModes.setBounds(BOARD_WIDTH, 0, EXTRA_WIDTH, APPLET_HEIGHT);
        add(panelHider);
        panelHider.setBounds(BOARD_WIDTH, 0, EXTRA_WIDTH, APPLET_HEIGHT);
        add(panelSeeker);
        panelSeeker.setBounds(BOARD_WIDTH, 0, EXTRA_WIDTH, APPLET_HEIGHT);
        add(panelAuthors);
        panelAuthors.setBounds(0, BOARD_HEIGHT, BOARD_WIDTH, EXTRA_HEIGHT);
        
        // Init game
        initGame();
    }
    
    // Checked
    private void createUI() {
        // Temporary panel
        JPanel p;
        JPanel dummy1 = new JPanel();
        JPanel dummy2 = new JPanel();
        JPanel dummy3 = new JPanel();
        JPanel dummy4 = new JPanel();
        JPanel dummy5 = new JPanel();
        JPanel dummy6 = new JPanel();
        JPanel dummy7 = new JPanel();
        JPanel dummy8 = new JPanel();
        dummy1.setBackground(COLOR_PANEL_BACKGROUND);
        dummy2.setBackground(COLOR_PANEL_BACKGROUND);
        dummy3.setBackground(COLOR_PANEL_BACKGROUND);
        dummy4.setBackground(COLOR_PANEL_BACKGROUND);
        dummy5.setBackground(COLOR_PANEL_BACKGROUND);
        dummy6.setBackground(COLOR_PANEL_BACKGROUND);
        dummy7.setBackground(COLOR_PANEL_BACKGROUND);
        dummy8.setBackground(COLOR_PANEL_BACKGROUND);
        
        gameModes.setLayout(new GridLayout(7, 1));
        gameModes.setSize(EXTRA_WIDTH, BOARD_HEIGHT);
        gameModes.add(labGame);
        gameModes.add(labInstr);
        gameModes.add(labModes);
        gameModes.add(labGoal);
        gameModes.add(dummy1);
        gameModes.add(seekerMode);
        gameModes.add(manualMode);
        seekerMode.addActionListener(this);
        manualMode.addActionListener(this);
        
        panelHider.setLayout(new GridLayout(16, 1));
        panelHider.setSize(EXTRA_WIDTH, BOARD_HEIGHT);
        panelHider.add(labHider);
        panelHider.add(dummy2);
        panelHider.add(labSrc);
        p = new JPanel();
        p.setLayout(new GridLayout(1, 3));
        p.setBackground(COLOR_PANEL_BACKGROUND);
        p.add(labSrcLoc);
        p.add(tfSrcX);
        p.add(tfSrcY);
        panelHider.add(p);
        panelHider.add(dummy3);
        panelHider.add(labDst);
        p = new JPanel();
        p.setLayout(new GridLayout(1, 3));
        p.setBackground(COLOR_PANEL_BACKGROUND);
        p.add(labDstLoc);
        p.add(tfDstX);
        p.add(tfDstY);
        panelHider.add(p);
        panelHider.add(dummy4);
        panelHider.add(labPlanets);
        p = new JPanel();
        p.setLayout(new GridLayout(1, 3));
        p.setBackground(COLOR_PANEL_BACKGROUND);
        p.add(labLoc1);
        p.add(tfLocX1);
        p.add(tfLocY1);
        panelHider.add(p);
        p = new JPanel();
        p.setLayout(new GridLayout(1, 3));
        p.setBackground(COLOR_PANEL_BACKGROUND);
        p.add(labWeight1);
        p.add(tfWeight1);
        p.add(labBar);
        panelHider.add(p);
        panelHider.add(sbWeights);
        p = new JPanel();
        p.setLayout(new GridLayout(1, 3));
        p.setBackground(COLOR_PANEL_BACKGROUND);
        p.add(labLoc2);
        p.add(tfLocX2);
        p.add(tfLocY2);
        panelHider.add(p);
        p = new JPanel();
        p.setLayout(new GridLayout(1, 3));
        p.setBackground(COLOR_PANEL_BACKGROUND);
        p.add(labWeight2);
        p.add(tfWeight2);
        p.add(dummy5);
        panelHider.add(p);
        panelHider.add(dummy6);
        panelHider.add(butStart);
        
        tfSrcX.addActionListener(this);
        tfSrcY.addActionListener(this);
        tfDstX.addActionListener(this);
        tfDstY.addActionListener(this);
        tfLocX1.addActionListener(this);
        tfLocY1.addActionListener(this);
        tfWeight1.addActionListener(this);
        tfLocX2.addActionListener(this);
        tfLocY2.addActionListener(this);
        tfWeight2.addActionListener(this);
        sbWeights.addAdjustmentListener(this);
        butStart.addActionListener(this);
        
        // Seeker panel
        panelSeeker.setLayout(new GridLayout(10, 1));
        panelSeeker.setSize(EXTRA_WIDTH, BOARD_HEIGHT);
        panelSeeker.add(labSeeker);
        panelSeeker.add(dummy7);
        p = new JPanel();
        p.setLayout(new GridLayout(1, 2));
        p.setBackground(COLOR_PANEL_BACKGROUND);
        p.add(labTrial);
        p.add(tfTrial);
        panelSeeker.add(p);
        p = new JPanel();
        p.setLayout(new GridLayout(1, 2));
        p.setBackground(COLOR_PANEL_BACKGROUND);
        p.add(labVelAngle);
        p.add(tfVelAngle);
        panelSeeker.add(p);
        p = new JPanel();
        p.setLayout(new GridLayout(1, 2));
        p.setBackground(COLOR_PANEL_BACKGROUND);
        p.add(labScore);
        p.add(tfScore);
        panelSeeker.add(p);
        panelSeeker.add(warningSeeker);
        panelSeeker.add(butShoot);
        panelSeeker.add(changePlanets);
        panelSeeker.add(dummy8);
        panelSeeker.add(butRestart);
        tfVelAngle.addActionListener(this);
        butShoot.addActionListener(this);
        butRestart.addActionListener(this);
        changePlanets.addActionListener(this);
        
        panelAuthors.add(labAuthors);
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
         || getX(source) / (SIZE_X/4) == getX(destination) / (SIZE_X/4)
         || getY(source) / (SIZE_Y/4) == getY(destination) / (SIZE_Y/4))
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
        double dot = ((getX(source)-getX(destination)) * (getX(locations[0]) - getX(locations[1]))
                    + (getY(source)-getY(destination)) * (getY(locations[0]) - getY(locations[1])))
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
        
        // Switch the panel
        gameModes.setVisible(true);
        panelSeeker.setVisible(false);
        panelHider.setVisible(false);
        warningSeeker.setVisible(false);
        
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
    
    private double[] getPlanetShade() {
        double[] planetShade = new double[PLANET_SIZE * PLANET_SIZE];
        double halfSize = (double) PLANET_SIZE / 2;
        
        for (int i = 0; i < PLANET_SIZE; ++i) {
            double x = ((double) i - halfSize) / halfSize;
            int w = (int) (Math.sqrt(1.0 - x * x) * halfSize + 0.5);
            for (int j = -w + (int) (halfSize + 0.5); j < w + (int) (halfSize + 0.5); ++j) {
                double y = ((double) j - halfSize) / halfSize;
                double z = Math.sqrt(1.0 - x * x - y * y);
                planetShade[i * PLANET_SIZE + j] = 0.3 + 0.7 * Math.pow(Math.max((x * .35 - y * .35 + z * .87), 0.0), 1.5);
            }
        }
        
        return planetShade;
    }
    
    private void drawPlanet(Graphics g, Color c, double x, double y) {
        for (int i = 0; i < PLANET_SIZE * PLANET_SIZE; ++i)
            if (PLANET_SHADE[i] > 0) {
                g.setColor(new Color((int) (c.getRed() * PLANET_SHADE[i]),
                        (int) (c.getGreen() * PLANET_SHADE[i]),
                        (int) (c.getBlue() * PLANET_SHADE[i])));
                g.drawLine((int) x + i / PLANET_SIZE - PLANET_SIZE / 2, (int) y + i % PLANET_SIZE - PLANET_SIZE / 2,
                        (int) x + i / PLANET_SIZE - PLANET_SIZE / 2, (int) y + i % PLANET_SIZE - PLANET_SIZE / 2);
            }
    }
    
    private void initCanvasSeeker() {
        Graphics g = canvas.getOffscreenGraphics();
        
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);
        
        drawPlanet(g, COLOR_SOURCE, getX(source), getY(source));
        drawPlanet(g, COLOR_DESTINATION, getX(destination), getY(destination));
        
        canvas.repaint();
    }
    
    private void initCanvasHider() {
        Graphics g = canvas.getOffscreenGraphics();
        
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);
        
        drawPlanet(g, COLOR_SOURCE, getX(source), getY(source));
        drawPlanet(g, COLOR_DESTINATION, getX(destination), getY(destination));
        
        for (int i = 0; i < NUM_PLANETS; ++i)
            drawPlanet(g, getPlanetColor(weights[i]), getX(locations[i]), getY(locations[i]));
        
        canvas.repaint();
    }
    
    private Color getPlanetColor(int weight) {
        double ratio = ((double) weight) / SUM_PLANET_WEIGHTS * 1.25;
        if (ratio > 1)
            ratio = 1;
        
        int r1 = COLOR_PLANETS.getRed() / 2;
        int g1 = COLOR_PLANETS.getGreen() / 2;
        int b1 = COLOR_PLANETS.getBlue() / 2;
        int r2 = COLOR_PLANETS.getRed();
        int g2 = COLOR_PLANETS.getGreen();
        int b2 = COLOR_PLANETS.getBlue();
        
        int r = (int) (r1 * (1 - ratio) + r2 * ratio);
        int g = (int) (g1 * (1 - ratio) + g2 * ratio);
        int b = (int) (b1 * (1 - ratio) + b2 * ratio);
        
        return new Color(r, g, b);
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
        if (endless)
            tfTrial.setText("1");
        else
            tfTrial.setText("1 / " + NUM_TRIALS);
        tfVelAngle.setText("");
        tfScore.setText("N/A");
        
        // Now we are playing
        playing = true;
    }
    
    private void hiderConfig() {
        tfSrcX.setText("" + getX(source));
        tfSrcY.setText("" + (SIZE_Y - getY(source) - 1));
        tfDstX.setText("" + getX(destination));
        tfDstY.setText("" + (SIZE_Y - getY(destination) - 1));
        tfLocX1.setText("" + getX(locations[0]));
        tfLocY1.setText("" + (SIZE_Y - getY(locations[0]) - 1));
        tfWeight1.setText("" + weights[0]);
        tfLocX2.setText("" + getX(locations[1]));
        tfLocY2.setText("" + (SIZE_Y - getY(locations[1]) - 1));
        tfWeight2.setText("" + weights[1]);
        sbWeights.setValue(weights[1]);
    }
    
    private void shootProjectile(double velX, double velY) {
        shooting = true;
        
        double locX = getX(source);
        double locY = getY(source);
        // System.out.println("source locs: " + locX + ", " + locY);
        
        while (locX >= -SIZE_X / 4 && locX <= 5 * SIZE_X / 4 && locY >= -SIZE_Y / 4 && locY <= 5 * SIZE_Y / 4) {
            
            double[] accel = getAccel(locX, locY);
            // System.out.println("accel: " + accel[0] + ", " + accel[1]);
            // System.out.println("vel: " + velX + ", " + velY);
            
            if (Math.abs(accel[2]) > 20)
                break;
            
            locX += velX + accel[0] / 2;
            locY += velY + accel[1] / 2;
            velX += accel[0];
            velY += accel[1];
            
            updateCanvasSeeker(locX, locY, accel[2]);
            
            double dist =
            Math.sqrt((getX(destination) - locX) * (getX(destination) - locX)
                      + (getY(destination) - locY)
                      * (getY(destination) - locY));
            if ((endless || trial < NUM_TRIALS) && dist < bestScore) {
                bestScore = dist;
                tfScore.setText(String.format("%.2f", dist));
            }
        }
        
        ++trial;
        if (endless)
            tfTrial.setText((trial + 1) + "");
        
        if (!endless && trial < NUM_TRIALS)
            tfTrial.setText((trial + 1) + " / " + NUM_TRIALS);
        
        if (!endless && trial == NUM_TRIALS) {
            tfTrial.setText("FINISHED");
            warningSeeker.setVisible(true);
            changePlanets.setEnabled(true);
            
            winner = "Seeker";
            winnerScore = bestScore + "";
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
        g.drawRect((int) (locX + 0.5), (int) (locY + 0.5), 1, 1);
        
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
        double velX = SPEED * Math.cos(velAngle);
        double velY = -SPEED * Math.sin(velAngle);
        
        shootProjectile(velX, velY);
    }
    
    private void onClickRestart() {
        initGame();
    }
    
    private void onEditSrcX() {
        try {
            int val = Integer.parseInt(tfSrcX.getText());
            if (val >= 0 && val < SIZE_X) {
                source = getLoc(val, getY(source));
                initCanvasHider();
            }
        } catch (NumberFormatException e) {
        }
        tfSrcX.setText("" + getX(source));
    }
    
    private void onEditSrcY() {
        try {
            int val = SIZE_Y - Integer.parseInt(tfSrcY.getText()) - 1;
            if (val >= 0 && val < SIZE_Y) {
                source = getLoc(getX(source), val);
                initCanvasHider();
            }
        } catch (NumberFormatException e) {
        }
        tfSrcY.setText("" + (SIZE_Y - getY(source) - 1));
    }
    
    private void onEditDstX() {
        try {
            int val = Integer.parseInt(tfDstX.getText());
            if (val >= 0 && val < SIZE_X) {
                destination = getLoc(val, getY(destination));
                initCanvasHider();
            }
        } catch (NumberFormatException e) {
        }
        tfDstX.setText("" + getX(destination));
    }
    
    private void onEditDstY() {
        try {
            int val = SIZE_Y - Integer.parseInt(tfDstY.getText()) - 1;
            if (val >= 0 && val < SIZE_Y) {
                destination = getLoc(getX(destination), val);
                initCanvasHider();
            }
        } catch (NumberFormatException e) {
        }
        tfDstY.setText("" + (SIZE_Y - getY(destination) - 1));
    }
    
    private void onEditLocX1() {
        try {
            int val = Integer.parseInt(tfLocX1.getText());
            if (val >= 0 && val < SIZE_X) {
                locations[0] = getLoc(val, getY(locations[0]));
                initCanvasHider();
            }
        } catch (NumberFormatException e) {
        }
        tfLocX1.setText("" + getX(locations[0]));
    }
    
    private void onEditLocY1() {
        try {
            int val = SIZE_Y - Integer.parseInt(tfLocY1.getText()) - 1;
            if (val >= 0 && val < SIZE_Y) {
                locations[0] = getLoc(getX(locations[0]), val);
                initCanvasHider();
            }
        } catch (NumberFormatException e) {
        }
        tfLocY1.setText("" + (SIZE_Y - getY(locations[0]) - 1));
    }
    
    private void onEditWeight1() {
        try {
            int val = Integer.parseInt(tfWeight1.getText());
            if (val > 0 && val < SUM_PLANET_WEIGHTS) {
                weights[0] = val;
                weights[1] = SUM_PLANET_WEIGHTS - val;
                sbWeights.setValue(weights[1]);
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
            if (val >= 0 && val < SIZE_X) {
                locations[1] = getLoc(val, getY(locations[1]));
                initCanvasHider();
            }
        } catch (NumberFormatException e) {
        }
        tfLocX2.setText("" + getX(locations[1]));
    }
    
    private void onEditLocY2() {
        try {
            int val = SIZE_Y - Integer.parseInt(tfLocY2.getText()) - 1;
            if (val >= 0 && val < SIZE_Y) {
                locations[1] = getLoc(getX(locations[1]), val);
                initCanvasHider();
            }
        } catch (NumberFormatException e) {
        }
        tfLocY2.setText("" + (SIZE_Y - getY(locations[1]) - 1));
    }
    
    private void onEditWeight2() {
        try {
            int val = Integer.parseInt(tfWeight2.getText());
            if (val > 0 && val < SUM_PLANET_WEIGHTS) {
                weights[1] = val;
                weights[0] = SUM_PLANET_WEIGHTS - val;
                sbWeights.setValue(weights[1]);
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
        endless = false;
        
        butShoot.setEnabled(true);
        changePlanets.setEnabled(false);
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
        endless = true;
        
        butShoot.setEnabled(true);
        placePlanets();
        hiderConfig();
        initCanvasHider();
    }
    
    private void onClickPlanets() {
        Graphics g = canvas.getOffscreenGraphics();
        
        showPlanets = !showPlanets;
        if (showPlanets) {
            for (int i = 0; i < NUM_PLANETS; ++i)
                drawPlanet(g, getPlanetColor(weights[i]), getX(locations[i]), getY(locations[i]));
        } else {
            g.setColor(Color.BLACK);
            for (int i = 0; i < NUM_PLANETS; ++i)
                g.fillOval(getX(locations[i]) - PLANET_SIZE / 2,
                           getY(locations[i]) - PLANET_SIZE / 2, PLANET_SIZE, PLANET_SIZE);
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
        g.fillOval(getX(source) - PLANET_SIZE / 2 - INDICATOR_DIST,
                   getY(source) - PLANET_SIZE / 2 - INDICATOR_DIST,
                   PLANET_SIZE + INDICATOR_DIST * 2 + 1, PLANET_SIZE + INDICATOR_DIST * 2 + 1);
        drawPlanet(g, COLOR_SOURCE, getX(source), getY(source));
        g.setColor(COLOR_INDICATOR);
        g.drawRect(x, y, 1, 1);
        
        canvas.repaint();
    }
    
    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();
        
        if (obj == butStart)
            onClickStart();
        else if (obj == butShoot)
            onClickShoot();
        else if (obj == butRestart)
            onClickRestart();
        else if (obj == tfSrcX)
            onEditSrcX();
        else if (obj == tfSrcY)
            onEditSrcY();
        else if (obj == tfDstX)
            onEditDstX();
        else if (obj == tfDstY)
            onEditDstY();
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
    }
    
    public void adjustmentValueChanged(AdjustmentEvent e) {
        int val = e.getValue();
        tfWeight2.setText("" + val);
        onEditWeight2();
    }
    
    public void mousePressed(MouseEvent e) {
        // If we are not placing planets now, just return
        if (!placing || !started)
            return;
        
        // JL: for some reason, these are off by (2, 4)
        int x = e.getX() - 2;
        int y = e.getY() - 4;
        
        int planet = -1;
        double minDist = SIZE_X * SIZE_X + SIZE_Y * SIZE_Y;
        for (int i = 0; i < NUM_PLANETS; ++i) {
            double dist = getDist(locations[i], getLoc(x, y));
            if (dist < minDist) {
                planet = i;
                minDist = dist;
            }
        }
        
        // Grab if close enough
        if (planet >= 0 && minDist < 10) {
            selected = true;
            selectedPlanet = planet;
        } else {
            double dist = getDist(source, getLoc(x, y));
            if (dist < 10) {
                selected = true;
                selectedPlanet = Integer.MAX_VALUE;
            } else {
                dist = getDist(destination, getLoc(x, y));
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
        int newX = x;
        int newY = y;
        
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
            hiderConfig();
            initCanvasHider();
        } else if (selectedPlanet == Integer.MIN_VALUE) {
            destination = getLoc(newX, newY);
            hiderConfig();
            initCanvasHider();
        } else {
            locations[selectedPlanet] = getLoc(newX, newY);
            hiderConfig();
            initCanvasHider();
        }
    }
    
    public void mouseReleased(MouseEvent e) {
        if (!placing)
            return;
        mouseDragged(e);
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
