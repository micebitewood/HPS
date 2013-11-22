import java.awt.Button;
import java.awt.Canvas;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Random;

import javax.swing.JApplet;

public class GravityGameApplet extends JApplet implements ActionListener, MouseListener {
    // Constants
    static final int SIZE_X = 500;
    static final int SIZE_Y = 500;
    static final int NUM_PLANETS = 2;
    static final int SUM_PLANET_WEIGHTS = 1000; // JL: Should this be a double?
    static final int NUM_TRIALS = 5;
    static final int SUM_VELOCITIES = 10;       // JL: Should this be a double?
    
    static final double SCALER = 1;
    static final int DOT_SIZE = 5;
    static final Color COLOR_SOURCE = Color.YELLOW;
    static final Color COLOR_DESTINATION = Color.WHITE;
    static final Color COLOR_PLANETS = Color.BLUE;
    static final Color COLOR_PULL = Color.RED;
    static final int BOARD_WIDTH = (int)((SIZE_X+1) * SCALER + 0.5);
    static final int BOARD_HEIGHT = (int)((SIZE_Y+1) * SCALER + 0.5);
    static final int EXTRA_WIDTH = 300;
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
    Checkbox cbMode;
    Label labLocX1;
    Label labLocY1;
    Label labWeight1;
    Label labLocX2;
    Label labLocY2;
    Label labWeight2;
    TextField tfLocX1;
    TextField tfLocY1;
    TextField tfWeight1;
    TextField tfLocX2;
    TextField tfLocY2;
    TextField tfWeight2;
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
    Button butRestart;
    
    public void init() {
        started = false;
        
        random = new Random(System.currentTimeMillis());
        canvas = new GravityGameCanvas(BOARD_WIDTH, BOARD_HEIGHT);
        canvas.setBackground(Color.BLACK);
        
        panelHider = new Panel();
        labHider = new Label("Place your planets");
        cbMode = new Checkbox("Manually place planets");
        labLocX1 = new Label("Planet 1 X:");
        labLocY1 = new Label("Planet 1 Y:");
        labWeight1 = new Label("Planet 1 Weight:");
        labLocX2 = new Label("Planet 2 X:");
        labLocY2 = new Label("Planet 2 Y:");
        labWeight2 = new Label("Planet 2 Weight:");
        tfLocX1 = new TextField();
        tfLocY1 = new TextField();
        tfWeight1 = new TextField();
        tfLocX2 = new TextField();
        tfLocY2 = new TextField();
        tfWeight2 = new TextField();
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
        butRestart = new Button("Restart");
        
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
        
        // Init game
        finishGame();
    }
    
    private void createUI() {
        // Temporary panel
        Panel p;
        
        // Hider panel
        panelHider.setLayout(new GridLayout(10, 1));
        panelHider.setSize(EXTRA_WIDTH, BOARD_HEIGHT);
        panelHider.add(labHider);
        panelHider.add(cbMode);
        p = new Panel();
        p.setLayout(new GridLayout(1, 2));
        p.add(labLocX1);
        p.add(tfLocX1);
        panelHider.add(p);
        p = new Panel();
        p.setLayout(new GridLayout(1, 2));
        p.add(labLocY1);
        p.add(tfLocY1);
        panelHider.add(p);
        p = new Panel();
        p.setLayout(new GridLayout(1, 2));
        p.add(labWeight1);
        p.add(tfWeight1);
        panelHider.add(p);
        p = new Panel();
        p.setLayout(new GridLayout(1, 2));
        p.add(labLocX2);
        p.add(tfLocX2);
        panelHider.add(p);
        p = new Panel();
        p.setLayout(new GridLayout(1, 2));
        p.add(labLocY2);
        p.add(tfLocY2);
        panelHider.add(p);
        p = new Panel();
        p.setLayout(new GridLayout(1, 2));
        p.add(labWeight2);
        p.add(tfWeight2);
        panelHider.add(p);
        panelHider.add(butStart);
        cbMode.addMouseListener(this);
        tfLocX1.addActionListener(this);
        tfLocY1.addActionListener(this);
        tfWeight1.addActionListener(this);
        tfLocX2.addActionListener(this);
        tfLocY2.addActionListener(this);
        tfWeight2.addActionListener(this);
        butStart.addActionListener(this);
        
        // Seeker panel
        panelSeeker.setLayout(new GridLayout(10, 1));
        panelSeeker.setSize(EXTRA_WIDTH, BOARD_HEIGHT);
        panelSeeker.add(labSeeker);
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
        panelSeeker.add(butRestart);
        butShoot.addActionListener(this);
        butRestart.addActionListener(this);
    }
    
    private int getLoc(int x, int y) {
        return x * (SIZE_Y+1) + y;
    }
    
    private int getX(int loc) {
        return loc / (SIZE_Y+1);
    }
    
    private int getY(int loc) {
        return loc % (SIZE_Y+1);
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
        // TODO: Randomize source and destination locations
        source = getLoc(0, 0);
        destination = getLoc(4*SIZE_X/5, 4*SIZE_Y/5);
        
        // Default planet locations for NUM_PLANETS=2
        locations = new int[NUM_PLANETS];
        locations[0] = getLoc(SIZE_X/4, 2*SIZE_Y/3);
        locations[1] = getLoc(3*SIZE_X/4, SIZE_Y/4);
        
        // Default weights
        weights = new int[NUM_PLANETS];
        weights[NUM_PLANETS-1] = SUM_PLANET_WEIGHTS;
        for(int i=0; i<NUM_PLANETS-1; ++i)
            weights[NUM_PLANETS-1] -= (weights[i] = SUM_PLANET_WEIGHTS/NUM_PLANETS);
        
        // Init game params
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
        g.fillOval((int)(getX(source) * SCALER), (int)(getY(source) * SCALER), DOT_SIZE, DOT_SIZE);
        g.setColor(COLOR_DESTINATION);
        g.fillOval((int)(getX(destination) * SCALER), (int)(getY(destination) * SCALER), DOT_SIZE, DOT_SIZE);
        
        if(showPlanets) {
            g.setColor(COLOR_PLANETS);
            for(int i=0; i<NUM_PLANETS; ++i)
                g.fillOval((int)(getX(locations[i]) * SCALER), (int)(getY(locations[i]) * SCALER), DOT_SIZE, DOT_SIZE);
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
        
        // Default values
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
        
        // Init game
        initGame();
        
        // Default values
        cbMode.setState(true);
        tfLocX1.setText("" + getX(locations[0]));
        tfLocY1.setText("" + getY(locations[0]));
        tfWeight1.setText("" + weights[0]);
        tfLocX2.setText("" + getX(locations[1]));
        tfLocY2.setText("" + getY(locations[1]));
        tfWeight2.setText("" + weights[1]);
        
        // Init canvas for hider
        initCanvasHider(true);
    }
    
    private void initCanvasSeeker(boolean showPlanets) {
        Graphics g = canvas.getOffscreenGraphics();
        
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);
        
        g.setColor(COLOR_SOURCE);
        g.fillOval((int)(getX(source) * SCALER), (int)(getY(source) * SCALER), DOT_SIZE, DOT_SIZE);
        g.setColor(COLOR_DESTINATION);
        g.fillOval((int)(getX(destination) * SCALER), (int)(getY(destination) * SCALER), DOT_SIZE, DOT_SIZE);
        
        if(showPlanets) {
            g.setColor(COLOR_PLANETS);
            for(int i=0; i<NUM_PLANETS; ++i)
                g.fillOval((int)(getX(locations[i]) * SCALER), (int)(getY(locations[i]) * SCALER), DOT_SIZE, DOT_SIZE);
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
        if(trial < NUM_TRIALS) {
            tfTrial.setText((trial+1) + " / " + NUM_TRIALS);
        } else {
            tfTrial.setText("Done shooting");
            butShoot.setEnabled(false);
        }
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
        // If we are in seeker only mode, randomly place planets first
        if(!cbMode.getState())
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
    
    private void onClickRestart() {
        finishGame();
    }
    
    private void onClickMode() {
        // JL: For some reason, this appears to be the opposite. Maybe UI hasn't been updated yet?
        boolean mode = !cbMode.getState();
        
        tfLocX1.setEnabled(mode);
        tfLocY1.setEnabled(mode);
        tfWeight1.setEnabled(mode);
        tfLocX2.setEnabled(mode);
        tfLocY2.setEnabled(mode);
        tfWeight2.setEnabled(mode);
    }
    
    private void onEditLocX1() {
        try {
            int val = Integer.parseInt(tfLocX1.getText());
            if(val >= 0 && val <= SIZE_X) {
                locations[0] = getLoc(val, getY(locations[0]));
                initCanvasHider(true);
            }
        } catch(NumberFormatException e) {
        }
        tfLocX1.setText("" + getX(locations[0]));
    }
    
    private void onEditLocY1() {
        try {
            int val = Integer.parseInt(tfLocY1.getText());
            if(val >= 0 && val <= SIZE_Y) {
                locations[0] = getLoc(getX(locations[0]), val);
                initCanvasHider(true);
            }
        } catch(NumberFormatException e) {
        }
        tfLocY1.setText("" + getY(locations[0]));
    }
    
    private void onEditWeight1() {
        try {
            int val = Integer.parseInt(tfWeight1.getText());
            if(val > 0 && val < SUM_PLANET_WEIGHTS) {
                weights[0] = val;
                weights[1] = SUM_PLANET_WEIGHTS - val;
                initCanvasHider(true);
            }
        } catch(NumberFormatException e) {
        }
        tfWeight1.setText("" + weights[0]);
        tfWeight2.setText("" + weights[1]);
    }
    
    private void onEditLocX2() {
        try {
            int val = Integer.parseInt(tfLocX2.getText());
            if(val >= 0 && val <= SIZE_X) {
                locations[1] = getLoc(val, getY(locations[1]));
                initCanvasHider(true);
            }
        } catch(NumberFormatException e) {
        }
        tfLocX2.setText("" + getX(locations[1]));
    }
    
    private void onEditLocY2() {
        try {
            int val = Integer.parseInt(tfLocY2.getText());
            if(val >= 0 && val <= SIZE_Y) {
                locations[1] = getLoc(getX(locations[1]), val);
                initCanvasHider(true);
            }
        } catch(NumberFormatException e) {
        }
        tfLocY2.setText("" + getY(locations[1]));
    }
    
    private void onEditWeight2() {
        try {
            int val = Integer.parseInt(tfWeight2.getText());
            if(val > 0 && val < SUM_PLANET_WEIGHTS) {
                weights[1] = val;
                weights[0] = SUM_PLANET_WEIGHTS - val;
                initCanvasHider(true);
            }
        } catch(NumberFormatException e) {
        }
        tfWeight1.setText("" + weights[0]);
        tfWeight2.setText("" + weights[1]);
    }
    
    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();
        
        if(obj == butStart)
            onClickStart();
        else if(obj == butShoot)
            onClickShoot();
        else if(obj == butRestart)
            onClickRestart();
        else if(obj == tfLocX1)
            onEditLocX1();
        else if(obj == tfLocY1)
            onEditLocY1();
        else if(obj == tfWeight1)
            onEditWeight1();
        else if(obj == tfLocX2)
            onEditLocX2();
        else if(obj == tfLocY2)
            onEditLocY2();
        else if(obj == tfWeight2)
            onEditWeight2();
    }
    
    public void mouseReleased(MouseEvent e) {
        Object obj = e.getSource();
        
        if(obj == cbMode)
            onClickMode();
    }

    public void mouseClicked(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    
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
