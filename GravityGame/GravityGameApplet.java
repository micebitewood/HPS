import java.awt.Button;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

import javax.swing.JApplet;

public class GravityGameApplet extends JApplet implements ActionListener {
    // Constants
    static final int SIZE_X = 100;
    static final int SIZE_Y = 100;
    static final int NUM_PIXELS = SIZE_X * SIZE_Y;
    static final int NUM_PLANETS = 2;
    static final int SUM_PLANET_WEIGHTS = 1000; // JL: Should this be a double?
    static final int NUM_TRIALS = 5;
    static final int SUM_VELOCITIES = 10;       // JL: Should this be a double?
    
    static final int SCALER = 5;
    static final int DOT_SIZE = 5;
    static final Color COLOR_SOURCE = Color.YELLOW;
    static final Color COLOR_DESTINATION = Color.WHITE;
    static final Color COLOR_PLANETS = Color.BLUE;
    static final Color COLOR_PULL = Color.RED;
    static final int BOARD_WIDTH = SIZE_X * SCALER;
    static final int BOARD_HEIGHT = SIZE_Y * SCALER;
    static final int EXTRA_WIDTH = 200;
    static final int APPLET_WIDTH = BOARD_WIDTH + EXTRA_WIDTH;
    static final int APPLET_HEIGHT = BOARD_HEIGHT;
    
    // Game data
    boolean started;    // Has the game started?
    int source;         // Source location
    int destination;    // Destination location
    int[] locations;    // Planet locations
    int[] weights;      // Planet weights
    int trial;          // Number of trials so far
    int sumVelocities;  // Sum of velocities so far
    double bestScore;   // Best score so far
    boolean shooting;   // Are we shooting now?
    
    // Extra stuffs
    Random random;
    
    // GUI
    GravityGameCanvas canvas;
    Panel panelHider;
    Label labHider;
    Button butStart;
    Panel panelSeeker;
    Label labSeeker;
    Label labTrial;
    Label labVelX;
    Label labVelY;
    Label labScore;
    TextField tfTrial;
    TextField tfVelX;
    TextField tfVelY;
    TextField tfScore;
    Button butShoot;
    
    public void init() {
        started = false;
        
        random = new Random(System.currentTimeMillis());
        canvas = new GravityGameCanvas(BOARD_WIDTH, BOARD_HEIGHT);
        canvas.setBackground(Color.BLACK);
        
        panelHider = new Panel();
        labHider = new Label("Place your planets");
        butStart = new Button("Start");
        
        panelSeeker = new Panel();
        labSeeker = new Label("Shoot projectiles!");
        labTrial = new Label("Trial:");
        labVelX = new Label("X Velocity:");
        labVelY = new Label("Y Velocity:");
        labScore = new Label("Score:");
        tfTrial = new TextField();
        tfTrial.setEditable(false);
        tfVelX = new TextField();
        tfVelY = new TextField();
        tfScore = new TextField();
        tfScore.setEditable(false);
        butShoot = new Button("Shoot");
        
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
        add(panelHider);
        panelHider.setBounds(BOARD_WIDTH, 0, EXTRA_WIDTH, BOARD_HEIGHT);
        add(panelSeeker);
        panelSeeker.setBounds(BOARD_WIDTH, 0, EXTRA_WIDTH, BOARD_HEIGHT);
        panelSeeker.setVisible(false);
        
        // Init game
        initGame();
        initCanvasHider(false);
        
    }
    
    private void createUI() {
        // Hider panel
        panelHider.setLayout(new GridLayout(10, 1));
        panelHider.setSize(EXTRA_WIDTH, BOARD_HEIGHT);
        panelHider.add(labHider);
        panelHider.add(butStart);
        butStart.addActionListener(this);
        
        // Seeker panel
        panelSeeker.setLayout(new GridLayout(10, 1));
        panelSeeker.setSize(EXTRA_WIDTH, BOARD_HEIGHT);
        panelSeeker.add(labSeeker);
        Panel p;
        p = new Panel();
        p.setLayout(new GridLayout(1, 2));
        p.add(labTrial);
        p.add(tfTrial);
        panelSeeker.add(p);
        p = new Panel();
        p.setLayout(new GridLayout(1, 2));
        p.add(labVelX);
        p.add(tfVelX);
        panelSeeker.add(p);
        p = new Panel();
        p.setLayout(new GridLayout(1, 2));
        p.add(labVelY);
        p.add(tfVelY);
        panelSeeker.add(p);
        p = new Panel();
        p.setLayout(new GridLayout(1, 2));
        p.add(labScore);
        p.add(tfScore);
        panelSeeker.add(p);
        panelSeeker.add(butShoot);
        butShoot.addActionListener(this);
    }
    
    private int getLoc(int x, int y) {
        return x * SIZE_Y + y;
    }
    
    private int getX(int loc) {
        return loc / SIZE_Y;
    }
    
    private int getY(int loc) {
        return loc % SIZE_Y;
    }
    
    private boolean isValid(String str) {
        try {
            Double.parseDouble(str);
        } catch(NumberFormatException e) {
            return false;
        }
        return true;
    }
    
    private void initGame() {
        source = getLoc(0, 0);        // TODO: Randomize
        destination = getLoc(80, 80); // TODO: Randomize
        
        locations = new int[NUM_PLANETS];
        weights = new int[NUM_PLANETS];
        trial = 0;
        sumVelocities = 0;
        bestScore = SIZE_X + SIZE_Y;  // Something larger than the max distance in the board
        shooting = false;
    }
    
    private void initCanvasHider(boolean showPlanets) {
        Graphics g = canvas.getOffscreenGraphics();
        
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);
        
        g.setColor(COLOR_SOURCE);
        g.fillOval(getX(source) * SCALER, getY(source) * SCALER, DOT_SIZE, DOT_SIZE);
        g.setColor(COLOR_DESTINATION);
        g.fillOval(getX(destination) * SCALER, getY(destination) * SCALER, DOT_SIZE, DOT_SIZE);
        
        if(showPlanets) {
            g.setColor(COLOR_PLANETS);
            for(int i=0; i<NUM_PLANETS; ++i)
                g.fillOval(getX(locations[i]) * SCALER, getY(locations[i]) * SCALER, DOT_SIZE, DOT_SIZE);
        }
        
        canvas.repaint();
    }
    
    private void placePlanets() {
        // TODO: Make these random
        locations[0] = getLoc(10, 50);
        locations[1] = getLoc(60, 30);
        weights[0] = 650;
        weights[1] = SUM_PLANET_WEIGHTS - weights[0];
    }
    
    private void startGame() {
        // We are started
        started = true;
        
        // Switch the panel
        panelHider.setVisible(false);
        panelSeeker.setVisible(true);
        tfTrial.setText("1 / " + NUM_TRIALS);
        tfVelX.setText("");
        tfVelY.setText("");
        tfScore.setText("N/A");
        
        // Init canvas for seeker
        initCanvasSeeker(true);
    }
    
    private void finishGame() {
        // We are finished
        started = false;
        
        // Switch the panel
        panelSeeker.setVisible(false);
        panelHider.setVisible(true);
    }
    
    private void initCanvasSeeker(boolean showPlanets) {
        Graphics g = canvas.getOffscreenGraphics();
        
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);
        
        g.setColor(COLOR_SOURCE);
        g.fillOval(getX(source) * SCALER, getY(source) * SCALER, DOT_SIZE, DOT_SIZE);
        g.setColor(COLOR_DESTINATION);
        g.fillOval(getX(destination) * SCALER, getY(destination) * SCALER, DOT_SIZE, DOT_SIZE);
        
        if(showPlanets) {
            g.setColor(COLOR_PLANETS);
            for(int i=0; i<NUM_PLANETS; ++i)
                g.fillOval(getX(locations[i]) * SCALER, getY(locations[i]) * SCALER, DOT_SIZE, DOT_SIZE);
        }
        
        canvas.repaint();
    }
    
    private void shootProjectile(double velX, double velY) {
        shooting = true;
        
        double locX = getX(source);
        double locY = getY(source);
        
        while(locX >= 0 && locX <= SIZE_X && locY >= 0 && locY <= SIZE_Y) {
            double sumPulls = 0.0;
            for(int i=0; i<NUM_PLANETS; ++i) {
                double[] pulls = getPulls(getX(locations[i])-locX, getY(locations[i])-locY, weights[i]);
                velX += pulls[0];
                velY += pulls[1];
                sumPulls += pulls[2];
            }
            
            updateCanvasSeeker(locX, locY, sumPulls);
            
            locX += velX;
            locY += velY;
            
            double dist = Math.sqrt((getX(destination)-locX)*(getX(destination)-locX) + (getY(destination)-locY)*(getY(destination)-locY));
            if(dist < bestScore) {
                bestScore = dist;
                tfScore.setText(String.format("%.2f", dist));
            }
        }
        
        shooting = false;
        ++trial;
        if(trial == NUM_TRIALS)
            finishGame();
        tfTrial.setText((trial+1) + " / " + NUM_TRIALS);
    }
    
    private void updateCanvasSeeker(double locX, double locY, double pull) {
        Graphics g = canvas.getOffscreenGraphics();
        
        // TODO: Map pull to color gradient
        g.setColor(COLOR_PULL);
        g.drawRect((int)(locX * SCALER + 0.5), (int)(locY * SCALER + 0.5), 1, 1);
        
        canvas.repaint();
    }
    
    private double[] getPulls(double relX, double relY, double weight) {
        double distSq = relX*relX + relY*relY;
        double dist = Math.sqrt(distSq);
        double pull = weight/distSq;
        return new double[] { pull*relX/dist, pull*relY/dist, pull };
    }
    
    private void onClickStart() {
        // TODO: Check the mode (seeker/manual) and see if all the required params were entered.
        //       If not, don't start the game yet.
        
        // In seeker mode, automatically place planets
        placePlanets();
        
        // Start the game
        startGame();
    }
    
    private void onClickShoot() {
        if(shooting) {
            // If we are already shooting, just return
            return;
        }
        
        if(!isValid(tfVelX.getText()) || !isValid(tfVelY.getText())) {
            // TODO: Display error message
            return;
        }
        
        double velX = Double.parseDouble(tfVelX.getText());
        double velY = Double.parseDouble(tfVelY.getText());
        
        shootProjectile(velX, velY);
    }
    
    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();
        
        if(obj == butStart)
            onClickStart();
        else if(obj == butShoot)
            onClickShoot();
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
