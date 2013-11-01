import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JApplet;

public class NanomunchersVisualizer extends JApplet {
    
    static final int SIZE_X = 20;
    static final int SIZE_Y = 10;
    static final int MAX_NUM_NODES = SIZE_X * SIZE_Y;
    
    static final int CELL_SIZE = 40;
    static final int BOARD_WIDTH = SIZE_X * CELL_SIZE;
    static final int BOARD_HEIGHT = SIZE_Y * CELL_SIZE;
    static final int TEXT_WIDTH = 200;
    static final int APPLET_WIDTH = BOARD_WIDTH + TEXT_WIDTH;
    static final int APPLET_HEIGHT = BOARD_HEIGHT;
    static final Color[] PLAYER_COLOR = {Color.YELLOW, Color.RED };
    static final Color[] PLAYER_COLOR_DARKER = {Color.YELLOW.darker().darker(), Color.RED.darker() };
    static final int[] DIR_ANGLE = {90, 180, 270, 0 };
    
    static final int DELAY = 100;
    
    static final int PORT = 9394;
    
    static Socket client;
    static PrintWriter out;
    static BufferedReader in;
    
    private Set<Integer> edges = new HashSet<Integer>();
    private Map<Integer, Integer> locs = new HashMap<Integer, Integer>();
    
    private Set<Integer> nodes = new HashSet<Integer>();
    private int[] board = new int[SIZE_X * SIZE_Y];
    
    private String[] teamNames = new String[2];
    private int[] scores = new int[2];
    
    private List<VizData> vizUpdate = new ArrayList<VizData>();
    
    private NanomunchersCanvas m_canvas;
    
    private void parseData(String data) {
        String[] specs = data.split("\n");
        int count = 0;
        boolean startNodes = false;
        boolean startEdges = false;
        for (String line : specs) {
            if (count < 2) {
                teamNames[count++] = line;
                continue;
            }
            String content = line.trim().toLowerCase();
            if (content.equals(""))
                continue;
            if (content.contains("xloc")) {
                startNodes = true;
            } else if (content.contains("nodeid1")) {
                startEdges = true;
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
                locs.put(nodeid, xloc * SIZE_Y + yloc);
                nodes.add(nodeid);
                board[xloc * SIZE_Y + yloc] = nodeid + 1;
            }
        }
    }
    
    private void parseTeamNames(String str) {
        String[] teamNames = str.split("\n");
        this.teamNames[0] = teamNames[0];
        this.teamNames[1] = teamNames[1];
    }
    
    public boolean parseStat(String str) {
        if (str.equals("0")) {
            return false;
        }
        String[] stats = str.split("\n");
        
        vizUpdate.clear();
        for (String stat : stats) {
            String[] items = stat.split(",");
            vizUpdate.add(new VizData(Integer.parseInt(items[0]), Integer.parseInt(items[1]), items[2].charAt(0), items[3].charAt(0)));
        }
        
        return true;
    }
    
    public void init() {
        int port = PORT;
        try {
            client = new Socket("127.0.0.1", port);
            out = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        } catch (IOException e) {
            System.out.println("port is unavailable");
        }
        m_canvas = new NanomunchersCanvas(APPLET_WIDTH, APPLET_HEIGHT);
        m_canvas.setBackground(Color.BLACK);
    }
    
    public void start() {
        try {
            parseData(receive());
            send("OKAY");
        } catch (IOException e) {
        }
        
        // Create layout
        setLayout(null);
        setSize(APPLET_WIDTH, APPLET_HEIGHT);
        
        add(m_canvas);
        m_canvas.setBounds(0, 0, APPLET_WIDTH, APPLET_HEIGHT);
        
        initCanvas();
        
        try {
            while (parseStat(receive())) {
                boolean isNewMuncher = false;
                for (VizData update : vizUpdate) {
                    if (update.dir == 'n') {
                        isNewMuncher = true;
                        break;
                    }
                }
                for (int i=(isNewMuncher?0:2); i<4; ++i) {
                    updateCanvas(i);
                    Thread.sleep(DELAY);
                }
                System.out.println(String.format("[SCORES] %s: %d, %s: %d", teamNames[0], scores[0], teamNames[1], scores[1]));
                send("OKAY");
            }
            vizUpdate.clear();
            updateCanvas(2);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    public String receive() throws IOException {
        StringBuffer sb = new StringBuffer();
        String temp;
        
        while (!(temp = in.readLine()).equalsIgnoreCase("<EOM>")) {
            sb.append(temp + "\n");
        }
        sb.deleteCharAt(sb.length() - 1);
//        System.out.println("receive:");
//        System.out.println(sb.toString());
        return sb.toString();
    }
    
    public void send(String str) {
//        System.out.println("send:");
        out.println(str);
//        System.out.println(str);
        out.println("<EOM>");
//        System.out.println("<EOM>");
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
    
    private void updateCanvas(int scene) {
        Graphics g = m_canvas.getOffscreenGraphics();
        
        if (scene % 2 == 0) {
            for (VizData update : vizUpdate) {
                if (update.nextDir == 'x') continue;
                if (scene == 0 && update.dir == 'n' || scene == 2 && update.dir != 'n')
                    scores[update.player]++;
            }
        }
        
        if (scene == 2) {
            for (int x = 0; x < SIZE_X; ++x) {
                for (int y = 0; y < SIZE_Y; ++y) {
                    if (board[x * SIZE_Y + y] < 0) {
                        g.setColor(Color.BLACK);
                        g.fillOval(x * CELL_SIZE + 5, y * CELL_SIZE + 5, 29, 29);
                        g.setColor(PLAYER_COLOR_DARKER[board[x * SIZE_Y + y]+2]);
                        g.fillOval(x * CELL_SIZE + 16, y * CELL_SIZE + 16, 7, 7);
                    }
                }
            }
        }
        
        for (VizData update : vizUpdate) {
            if (update.dir == 'c' || update.dir != 'x' && update.nextDir == 'x') continue;
            if (scene / 2 == 0 && update.dir != 'n' || scene / 2 == 1 && update.dir == 'n') continue;
            
            if (update.nextDir != 'x') {
                char dir = (scene % 2 == 0) ? update.dir : update.nextDir;
                int angle = DIR_ANGLE[dir == 'u' ? 0 : dir == 'l' ? 1 : dir == 'd' ? 2 : 3];
                int loc = locs.get(update.node);
                int x = loc / SIZE_Y;
                int y = loc % SIZE_Y;
                board[x * SIZE_Y + y] = update.player-2;
                
                g.setColor(Color.BLACK);
                g.fillOval(x * CELL_SIZE + 5, y * CELL_SIZE + 5, 29, 29);
                g.setColor(PLAYER_COLOR[update.player]);
                if (dir != 'n')
                    g.fillArc(x * CELL_SIZE + 5, y * CELL_SIZE + 5, 29, 29, angle + 30, 300);
                else
                    g.fillOval(x * CELL_SIZE + 5, y * CELL_SIZE + 5, 29, 29);
            } else if (update.dir == 'x') {
                int loc = locs.get(update.node);
                int x = loc / SIZE_Y;
                int y = loc % SIZE_Y;
                
                if (scene % 2 == 0) {
                    g.setColor(PLAYER_COLOR[update.player]);
                    g.fillOval(x * CELL_SIZE + 10, y * CELL_SIZE + 10, 19, 19);
                } else {
                    g.setColor(Color.BLACK);
                    g.fillOval(x * CELL_SIZE + 10, y * CELL_SIZE + 10, 19, 19);
                    g.setColor(PLAYER_COLOR[update.player]);
                    g.fillOval(x * CELL_SIZE + 13, y * CELL_SIZE + 13, 13, 13);
                }
            }
        }
        
        updateText();
        
        m_canvas.repaint();
    }
    
    private void updateText() {
        Graphics g = m_canvas.getOffscreenGraphics();
        
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(BOARD_WIDTH, 0, TEXT_WIDTH, BOARD_HEIGHT);
        
        String str = String.format("[SCORES]\n\n%s: %d\n%s: %d", teamNames[0], scores[0], teamNames[1], scores[1]);
        g.setColor(Color.BLACK);
        drawMultilineText(g, str, BOARD_WIDTH + 10, 20);
    }
    
    private void drawMultilineText(Graphics g, String str, int x, int y) {
        String[] lines = str.split("\n");
        for (String line : lines) {
            g.drawChars(line.toCharArray(), 0, line.length(), x, y);
            y += 15;
        }
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
    
    class VizData {
        int player;
        int node;
        char dir;
        char nextDir;
        
        public VizData(int player, int node, char dir, char nextDir) {
            this.player = player;
            this.node = node;
            this.dir = dir;
            this.nextDir = nextDir;
        }
    }
}
