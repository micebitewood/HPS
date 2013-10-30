import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.swing.JApplet;

public class NanomunchersServer extends JApplet {
    static final int SIZE_X = 20; // TODO: Make sure of this later
    static final int SIZE_Y = 10; // TODO: Make sure of this later
    static final int MAX_NUM_NODES = SIZE_X * SIZE_Y;
    
    static final int CELL_SIZE = 40;
    static final int BOARD_WIDTH = SIZE_X * CELL_SIZE;
    static final int BOARD_HEIGHT = SIZE_Y * CELL_SIZE;
    static final int APPLET_WIDTH = BOARD_WIDTH + 200;
    static final int APPLET_HEIGHT = BOARD_HEIGHT;
    static final Color[] PLAYER_COLOR = {Color.GREEN, Color.RED };
    static final int[] DIR_ANGLE = {90, 180, 270, 0 };
    
    private Set<Integer> edges = new HashSet<Integer>();
    private Map<Integer, Integer> locs = new HashMap<Integer, Integer>();
    
    private Set<Integer> nodes = new HashSet<Integer>();
    private int[] board = new int[SIZE_X * SIZE_Y];
    private List<Nanomuncher> munchers1 = new ArrayList<Nanomuncher>();
    private List<Nanomuncher> munchers2 = new ArrayList<Nanomuncher>();
    private int[] scores = {0, 0 };
    
    private ServerSocket server;
    private Socket socket;
    
    private NanomunchersCanvas m_canvas;
    
    // public static void main(String[] args) throws IOException {
    // }
    
    public void parseInput(String input) throws IOException {
        boolean startNodes = false;
        boolean startEdges = false;
        int numNodes = 0;
        
        BufferedReader br = new BufferedReader(new FileReader(input));
        String line;
        while ((line = br.readLine()) != null) {
            String content = line.trim().toLowerCase();
            if (content.equals(""))
                continue;
            if (content.equals("nodeid,xloc,yloc")) {
                startNodes = true;
                startEdges = false;
            } else if (content.equals("nodeid1,nodeid2")) {
                startEdges = true;
                startNodes = false;
            } else if (startEdges) {
                String[] edgeItems = content.split(",");
                int node1 = Integer.parseInt(edgeItems[0]);
                int node2 = Integer.parseInt(edgeItems[1]);
                edges.add(node1 * MAX_NUM_NODES + node2);
                edges.add(node2 * MAX_NUM_NODES + node1);
            } else if (startNodes) {
                String[] nodeItems = content.split(",");
                int nodeid = Integer.parseInt(nodeItems[0]);
                int xloc = Integer.parseInt(nodeItems[1]);
                int yloc = Integer.parseInt(nodeItems[2]);
                if (nodeid != numNodes) {
                    // TODO: Throw?
                }
                locs.put(nodeid, xloc * SIZE_Y + yloc);
                nodes.add(nodeid);
                board[xloc * SIZE_Y + yloc] = nodeid + 1;
                ++numNodes;
            }
        }
        br.close();
    }
    
    public void init() {
        try {
            server = new ServerSocket(3443);
            socket = server.accept();
        } catch (IOException e) {
            System.out.println("port is unavailable");
        }
        m_canvas = new NanomunchersCanvas(BOARD_WIDTH, BOARD_HEIGHT);
        m_canvas.setBackground(Color.BLACK);
    }
    
    public void start() {
        String input = "input";
        
        try {
            parseInput(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // Create layout
        setLayout(new GridLayout(1, 2));
        setSize(APPLET_WIDTH, APPLET_HEIGHT);
        add(m_canvas);
        
        initCanvas();
        
        // NB: Probably the code below should be moved into main()
        // Add munchers for test
        munchers1.add(new Nanomuncher(0, "urld"));
        munchers1.add(new Nanomuncher(4, "ldru"));
        munchers1.add(new Nanomuncher(5, "dulr"));
        munchers1.add(new Nanomuncher(8, "rudl"));
        munchers1.add(new Nanomuncher(2, "lrud"));
        munchers2.add(new Nanomuncher(18, "urdl"));
        munchers2.add(new Nanomuncher(72, "rldu"));
        munchers2.add(new Nanomuncher(51, "durl"));
        
        int noUpdate = 0;
        while (noUpdate < 4) {
            boolean wasUpdated = false;
            for (Nanomuncher muncher : munchers1) {
                int nodeid = muncher.pos;
                int loc = locs.get(nodeid);
                
                if (board[loc] <= 0) {
                    ++muncher.hunger;
                    if (muncher.hunger == 4) {
                        muncher.isDead = true;
                    }
                } else {
                    board[loc] = -1;
                    muncher.hunger = 0;
                    ++scores[0];
                    wasUpdated = true;
                }
            }
            
            for (Nanomuncher muncher : munchers2) {
                int nodeid = muncher.pos;
                int loc = locs.get(nodeid);
                
                if (board[loc] <= 0) {
                    ++muncher.hunger;
                    if (muncher.hunger == 4) {
                        muncher.isDead = true;
                    }
                } else {
                    board[loc] = -2;
                    muncher.hunger = 0;
                    ++scores[1];
                    wasUpdated = true;
                }
            }
            
            int flag;
            do {
                flag = -1;
                for (int i = 0; i < munchers1.size(); ++i)
                    if (munchers1.get(i).isDead) {
                        flag = i;
                        break;
                    }
                
                if (flag >= 0)
                    munchers1.remove(flag);
            } while (flag >= 0);
            
            do {
                flag = -1;
                for (int i = 0; i < munchers2.size(); ++i)
                    if (munchers2.get(i).isDead) {
                        flag = i;
                        break;
                    }
                
                if (flag >= 0)
                    munchers2.remove(flag);
            } while (flag >= 0);
            
            updateCanvas();
            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            for (Nanomuncher muncher : munchers1) {
                int nodeid = muncher.pos;
                int loc = locs.get(nodeid);
                
                int[] dir = muncher.getDirVec();
                
                int x = loc / SIZE_Y + dir[0];
                int y = loc % SIZE_Y + dir[1];
                
                if (x >= 0 && x < SIZE_X && y >= 0 && y < SIZE_Y && board[x * SIZE_Y + y] > 0
                    && edges.contains(nodeid * MAX_NUM_NODES + board[x * SIZE_Y + y] - 1))
                    muncher.pos = board[x * SIZE_Y + y] - 1;
            }
            
            for (Nanomuncher muncher : munchers2) {
                int nodeid = muncher.pos;
                int loc = locs.get(nodeid);
                
                int[] dir = muncher.getDirVec();
                
                int x = loc / SIZE_Y + dir[0];
                int y = loc % SIZE_Y + dir[1];
                
                if (x >= 0 && x < SIZE_X && y >= 0 && y < SIZE_Y && board[x * SIZE_Y + y] > 0
                    && edges.contains(nodeid * MAX_NUM_NODES + board[x * SIZE_Y + y] - 1))
                    muncher.pos = board[x * SIZE_Y + y] - 1;
            }
            
            updateCanvas();
            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            for (Nanomuncher muncher : munchers1)
                muncher.advance();
            for (Nanomuncher muncher : munchers2)
                muncher.advance();
            
            if (!wasUpdated)
                ++noUpdate;
        }
    }
    
    public void stop() {
    }
    
    private void initCanvas() {
        Graphics g = m_canvas.getOffscreenGraphics();
        
        for (int x = 0; x < SIZE_X; ++x) {
            for (int y = 0; y < SIZE_Y; ++y) {
                int pos = x * SIZE_Y + y;
                if (board[pos] != 0) {
                    g.setColor(Color.WHITE);
                    g.fillOval(x * CELL_SIZE + 16, y * CELL_SIZE + 16, 7, 7);
                    
                    if (x < SIZE_X - 1
                        && (board[pos + SIZE_Y] == 0 || !edges
                            .contains((board[pos] - 1) * MAX_NUM_NODES
                                      + board[pos + SIZE_Y] - 1))) {
                            g.setColor(Color.BLUE);
                            g.fillRect(x * CELL_SIZE + CELL_SIZE - 2, y * CELL_SIZE - 1, 4, CELL_SIZE + 2);
                            // g.setColor(Color.BLACK);
                            // g.fillRect(x * CELL_SIZE + CELL_SIZE - 1, y * CELL_SIZE - 1, 2, CELL_SIZE + 2);
                        }
                    if (y < SIZE_Y - 1
                        && (board[pos + 1] == 0 || !edges
                            .contains((board[pos] - 1) * MAX_NUM_NODES
                                      + board[pos + 1] - 1))) {
                            g.setColor(Color.BLUE);
                            g.fillRect(x * CELL_SIZE - 1, y * CELL_SIZE + CELL_SIZE - 2, CELL_SIZE + 2, 4);
                            // g.setColor(Color.BLACK);
                            // g.fillRect(x * CELL_SIZE - 1, y * CELL_SIZE + CELL_SIZE - 1, CELL_SIZE + 2, 2);
                        }
                } else {
                    if (x < SIZE_X - 1 && board[pos + SIZE_Y] != 0) {
                        g.setColor(Color.BLUE);
                        g.fillRect(x * CELL_SIZE + CELL_SIZE - 2, y * CELL_SIZE - 1, 4, CELL_SIZE + 2);
                        // g.setColor(Color.BLACK);
                        // g.fillRect(x * CELL_SIZE + CELL_SIZE - 1, y * CELL_SIZE - 1, 2, CELL_SIZE + 2);
                    }
                    if (y < SIZE_Y - 1 && board[pos + 1] != 0) {
                        g.setColor(Color.BLUE);
                        g.fillRect(x * CELL_SIZE - 1, y * CELL_SIZE + CELL_SIZE - 2, CELL_SIZE + 2, 4);
                        // g.setColor(Color.BLACK);
                        // g.fillRect(x * CELL_SIZE - 1, y * CELL_SIZE + CELL_SIZE - 1, CELL_SIZE + 2, 2);
                    }
                }
            }
        }
        
        m_canvas.repaint();
    }
    
    private void updateCanvas() {
        Graphics g = m_canvas.getOffscreenGraphics();
        
        for (int x = 0; x < SIZE_X; ++x) {
            for (int y = 0; y < SIZE_Y; ++y) {
                if (board[x * SIZE_Y + y] == -1) {
                    g.setColor(Color.BLACK);
                    g.fillOval(x * CELL_SIZE + 5, y * CELL_SIZE + 5, 29, 29);
                    g.setColor(PLAYER_COLOR[0].darker());
                    g.fillOval(x * CELL_SIZE + 16, y * CELL_SIZE + 16, 7, 7);
                } else if (board[x * SIZE_Y + y] == -2) {
                    g.setColor(Color.BLACK);
                    g.fillOval(x * CELL_SIZE + 5, y * CELL_SIZE + 5, 29, 29);
                    g.setColor(PLAYER_COLOR[1].darker());
                    g.fillOval(x * CELL_SIZE + 16, y * CELL_SIZE + 16, 7, 7);
                }
            }
        }
        
        for (Nanomuncher muncher : munchers1) {
            int angle = DIR_ANGLE[muncher.getCurrentProgram()];
            int loc = locs.get(muncher.pos);
            int x = loc / SIZE_Y;
            int y = loc % SIZE_Y;
            
            g.setColor(PLAYER_COLOR[0]);
            g.fillArc(x * CELL_SIZE + 5, y * CELL_SIZE + 5, 29, 29, angle + 30, 300);
        }
        
        for (Nanomuncher muncher : munchers2) {
            int angle = DIR_ANGLE[muncher.getCurrentProgram()];
            int loc = locs.get(muncher.pos);
            int x = loc / SIZE_Y;
            int y = loc % SIZE_Y;
            
            g.setColor(PLAYER_COLOR[1]);
            g.fillArc(x * CELL_SIZE + 5, y * CELL_SIZE + 5, 29, 29, angle + 30, 300);
        }
        
        m_canvas.repaint();
    }
    
    public class NanomunchersCanvas extends Canvas {
        Image m_imageOffscreen;
        Graphics m_graphOffscreen;
        int m_uWidth;
        int m_uHeight;
        
        public NanomunchersCanvas(int uWidth, int uHeight) {
            super();
            
            m_uWidth = uWidth;
            m_uHeight = uHeight;
            setSize(m_uWidth, m_uHeight);
            
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

class Nanomuncher {
    private static final String dir = "uldr";
    
    int pos; // Node ID
    int[] program; // [4], 0: up, 1: left, 2: down, 3: right
    int counter = 0;
    int hunger = 0;
    boolean isDead = false;
    
    private int dirCharToNum(char c) {
        return (c == 'u') ? 0 : (c == 'l') ? 1 : (c == 'd') ? 2
        : (c == 'r') ? 3 : -1;
    }
    
    private int[] parseProgram(String str) {
        return new int[] {dirCharToNum(str.charAt(0)),
            dirCharToNum(str.charAt(1)), dirCharToNum(str.charAt(2)),
            dirCharToNum(str.charAt(3)) };
    }
    
    public String toString() {
        return String.format("%c%c%c%c", dir.charAt(program[counter % 4]),
                             dir.charAt(program[(counter + 1) % 4]),
                             dir.charAt(program[(counter + 2) % 4]),
                             dir.charAt(program[(counter + 3) % 4]));
    }
    
    public int[] getDirVec() {
        int curProgram = program[counter % 4];
        return (curProgram == 0) ? new int[] {0, -1 }
        : (curProgram == 1) ? new int[] {-1, 0 }
        : (curProgram == 2) ? new int[] {0, 1 } : new int[] {
            1, 0 };
    }
    
    public int getCurrentProgram() {
        return program[counter % 4];
    }
    
    public void advance() {
        ++counter;
    }
    
    public Nanomuncher(int pos, String program) {
        this.pos = pos;
        this.program = parseProgram(program);
    }
}
