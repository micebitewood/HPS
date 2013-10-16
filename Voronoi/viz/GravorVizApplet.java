import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JApplet;


public class GravorVizApplet extends JApplet implements MouseListener, MouseMotionListener {
	final static int N = 50;
	final static int CELL_SIZE = 8;
	final static int CURSOR_SIZE = CELL_SIZE/8;
	final static int BOARD_SIZE = N*CELL_SIZE+1;
	final static int APPLET_WIDTH = BOARD_SIZE*2;
	final static int APPLET_HEIGHT = BOARD_SIZE;
	final static int NUM_PLAYERS = 2;
	final static Color PLAYER_COLOR[] = {Color.RED, Color.BLUE};
	final static double INF = 10000.0;
	
	private double[][] grav;
	private int[] color;
	private int[] scores;
	
	private int curPlayer = 0;
	
	private GravorCanvas m_canvas;
	private Label m_labelTurn;
	private Label[] m_labelScores;
	private Label m_debug;
	
	public void init() {
		grav = new double[NUM_PLAYERS][N*N];
		color = new int[N*N];
		scores = new int[NUM_PLAYERS];
		
		for(int i=0; i<NUM_PLAYERS; ++i)
			for(int j=0; j<N*N; ++j)
				grav[i][j] = 0.0;
		
		for(int i=0; i<N*N; ++i)
			color[i] = -1;
		
		for(int i=0; i<NUM_PLAYERS; ++i)
			scores[i] = 0;
		
		m_canvas = new GravorCanvas(BOARD_SIZE, BOARD_SIZE);
		m_canvas.setBackground(Color.WHITE);
		m_canvas.addMouseListener(this);
		m_canvas.addMouseMotionListener(this);
		
		m_labelTurn = new Label();
		m_labelScores = new Label[NUM_PLAYERS];
		for(int i=0; i<NUM_PLAYERS; ++i)
			m_labelScores[i] = new Label();
		m_debug = new Label();
	}
	
	public void start() {
		// Create layout
		setLayout(new GridLayout(1, 2));
		setSize(APPLET_WIDTH, APPLET_HEIGHT);
		add(m_canvas);
		
		Panel panel = new Panel();
		panel.setLayout(new GridLayout(NUM_PLAYERS+2, 1));
		panel.add(m_labelTurn);
		panel.add(m_debug);
		for(int i=0; i<NUM_PLAYERS; ++i)
			panel.add(m_labelScores[i]);
		add(panel);
		
		updateLabels();
		drawGrid();
		updateCanvas(grav, color);
	}
	
	public void stop() {
		
	}
	
	private String getTurnString(int player) {
		return String.format("Player %d's Turn", player+1);
	}
	
	private String getScoreString(int player, int score) {
		return String.format("Player %d's Score: %d", player+1, score);
	}
	
	private void drawGrid() {
		Graphics g= m_canvas.getOffscreenGraphics();
		if(CELL_SIZE > 4)
			g.setColor(Color.LIGHT_GRAY);
		else
			g.setColor(Color.BLACK);
		
		for(int i=0; i<N; ++i)
			for(int j=0; j<N; ++j)
				g.drawRect(i*CELL_SIZE, j*CELL_SIZE, CELL_SIZE, CELL_SIZE);
	}
	
	private Color mapColor(Color c, double grav) {
		double r = (double)c.getRed() / 255.0;
		double g = (double)c.getGreen() / 255.0;
		double b = (double)c.getBlue() / 255.0;
		
//		double m = Math.pow(grav, 0.3);
		double m = Math.log(grav)*25.0/N+1.5;
		if(m > 1.0)
			m = 1.0;
		if(m < 0.0)
			m = 0.0;
		
		m = 0.5 + 0.5*m;
		
		r = r*m;
		g = g*m;
		b = b*m;
		
		return new Color((int)(r*255.0), (int)(g*255.0), (int)(b*255.0));
	}
	
	private void updateCanvas(double[][] grav, int[]color) {
		Graphics g= m_canvas.getOffscreenGraphics();
		
		for(int i=0; i<N; ++i)
			for(int j=0; j<N; ++j) {
				if(color[i*N+j] == -1)
					g.setColor(Color.WHITE);
				else
					g.setColor(mapColor(PLAYER_COLOR[color[i*N+j]], grav[color[i*N+j]][i*N+j]));
				
				g.fillRect(i*CELL_SIZE+1, j*CELL_SIZE+1, CELL_SIZE-1, CELL_SIZE-1);
				
				for(int k=0; k<NUM_PLAYERS; ++k)
					if(grav[k][i*N+j] >= INF) {
						g.setColor(Color.BLACK);
						g.fillRect(i*CELL_SIZE+1+CURSOR_SIZE, j*CELL_SIZE+1+CURSOR_SIZE, CELL_SIZE-1-2*CURSOR_SIZE, CELL_SIZE-1-2*CURSOR_SIZE);
					}
			}
		
		m_canvas.repaint();
	}
	
	private void updateLabels() {
		m_labelTurn.setText(getTurnString(curPlayer));
		for(int i=0; i<NUM_PLAYERS; ++i)
			m_labelScores[i].setText(getScoreString(i, scores[i]));
	}
	
	private double getGravity(int x1, int y1, int x2, int y2) {
		return (x1==x2 && y1==y2) ? INF : 1.0/((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2));
	}
	
	private void play(int x, int y) {
		for(int i=0; i<NUM_PLAYERS; ++i)
			if(grav[i][x*N+y] >= INF) return;
		
		for(int i=0; i<N; ++i)
			for(int j=0; j<N; ++j)
				grav[curPlayer][i*N+j] += getGravity(x, y, i, j);
		
		for(int i=0; i<NUM_PLAYERS; ++i)
			scores[i] = 0;
		
		for(int i=0; i<N; ++i)
			for(int j=0; j<N; ++j) {
				double max = 0;
				int maxPlayer = -1;
				
				for(int k=0; k<NUM_PLAYERS; ++k)
					if(grav[k][i*N+j] > max) {
						max = grav[k][i*N+j];
						maxPlayer = k;
					} else if(grav[k][i*N+j] == max) {
						maxPlayer = -1;
					}
				color[i*N+j] = maxPlayer;
				if(maxPlayer != -1)
					++scores[maxPlayer];
			}
		
		curPlayer = (curPlayer+1)%NUM_PLAYERS;
		updateLabels();
		updateCanvas(grav, color);
	}
	
	private void projectPlay(int x, int y) {
		for(int i=0; i<NUM_PLAYERS; ++i)
			if(grav[i][x*N+y] >= INF) {
				updateCanvas(grav, color);
				return;
			}
		
		double[][] projGrav = new double[NUM_PLAYERS][N*N];
		for(int i=0; i<N; ++i)
			for(int j=0; j<N; ++j)
				for(int k=0; k<NUM_PLAYERS; ++k)
					if(k == curPlayer)
						projGrav[k][i*N+j] = grav[k][i*N+j] + getGravity(x, y, i, j);
					else
						projGrav[k][i*N+j] = grav[k][i*N+j];
		
		int[] projScores = new int[NUM_PLAYERS];
		for(int i=0; i<NUM_PLAYERS; ++i)
			projScores[i] = 0;
		
		int[] projColor = new int[N*N];
		
		for(int i=0; i<N; ++i)
			for(int j=0; j<N; ++j) {
				double max = 0;
				int maxPlayer = -1;
				
				for(int k=0; k<NUM_PLAYERS; ++k)
					if(projGrav[k][i*N+j] > max) {
						max = projGrav[k][i*N+j];
						maxPlayer = k;
					} else if(projGrav[k][i*N+j] == max) {
						maxPlayer = -1;
					}
				projColor[i*N+j] = maxPlayer;
				if(maxPlayer != -1)
					++projScores[maxPlayer];
			}
		updateCanvas(projGrav, projColor);
		m_debug.setText(String.format("Projected scores for (%2d, %2d): %d vs %d", x, y, projScores[0], projScores[1]));
	}
	
	public class GravorCanvas extends Canvas {
		Image m_imageOffscreen;
		Graphics m_graphOffscreen;
		int m_uWidth;
		int m_uHeight;
		
		public GravorCanvas( int uWidth, int uHeight ) {
			super();
			
			m_uWidth= uWidth;
			m_uHeight= uHeight;
			setSize( m_uWidth, m_uHeight );
			
			m_imageOffscreen= null;
			m_graphOffscreen= null;
		} // end ConwayMusicCanvas()
		
		public Graphics getOffscreenGraphics() {
			if( m_imageOffscreen == null || m_graphOffscreen == null ) {
				m_imageOffscreen= this.createImage( m_uWidth, m_uHeight );
				m_graphOffscreen= m_imageOffscreen.getGraphics();
			} // end if image is null
			
			return m_graphOffscreen;
		} // end Graphics getOffscreenGraphics()
		
		public void update( Graphics g ) {
			paint( g );
		} // end void update()
		
		public void paint( Graphics g ) {
			if( m_imageOffscreen == null || m_graphOffscreen == null ) {
				m_imageOffscreen= this.createImage( m_uWidth, m_uHeight );
				m_graphOffscreen= m_imageOffscreen.getGraphics();
			} // end if image is null
			
			if( m_imageOffscreen != null ) {
				g.drawImage( m_imageOffscreen, 0, 0, m_uWidth, m_uHeight, this );
			} else {
				// Couldn't create image properly
			} // end if image is not null
		} // end void paint()
	} // end class ConwayMusicCanvas
	
	public void mouseClicked(MouseEvent e) {
		Point p = e.getPoint();
		if(CELL_SIZE > 8 && (p.x%CELL_SIZE == 1 || p.y%CELL_SIZE == 3)) return;
		
		int x = (p.x-1)/CELL_SIZE;
		int y = (p.y-3)/CELL_SIZE;
		
		play(x, y);
	}

	public void mouseMoved(MouseEvent e) {
		Point p = e.getPoint();
		if(CELL_SIZE > 8 && (p.x%CELL_SIZE == 1 || p.y%CELL_SIZE == 3)) {
//			updateCanvas(grav, color);
			m_debug.setText("");
			return;
		}
		
		int x = (p.x-1)/CELL_SIZE;
		int y = (p.y-3)/CELL_SIZE;
		
		projectPlay(x, y);
//		m_debug.setText(String.format("Gravity at (%2d %2d): %f vs %f", x, y, grav[0][x*N+y], grav[1][x*N+y]));
	}
	
	public void mouseExited(MouseEvent e) {
		updateCanvas(grav, color);
		m_debug.setText("");
	}
	
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseDragged(MouseEvent e) {}
}

