import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JApplet;

public class NanomunchersServer extends JApplet {
    static final int SIZE_X = 20; // TODO: Make sure of this later
    static final int SIZE_Y = 10; // TODO: Make sure of this later
    static final int MAX_NUM_NODES = SIZE_X * SIZE_Y;

    final static int CELL_SIZE = 40;
    final static int BOARD_WIDTH = SIZE_X * CELL_SIZE;
    final static int BOARD_HEIGHT = SIZE_Y * CELL_SIZE;
    final static int APPLET_WIDTH = BOARD_WIDTH + 200;
    final static int APPLET_HEIGHT = BOARD_HEIGHT;

    private Set<Integer> edges = new HashSet<Integer>();
    private Map<Integer, Integer> locs = new HashMap<Integer, Integer>();

    private Set<Integer> nodes = new HashSet<Integer>();
    private int[] board = new int[SIZE_X * SIZE_Y];
    private List<Nanomuncher> munchers1 = new ArrayList<Nanomuncher>();
    private List<Nanomuncher> munchers2 = new ArrayList<Nanomuncher>();
    private int[] scores = { 0, 0 };

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
        m_canvas = new NanomunchersCanvas(BOARD_WIDTH, BOARD_HEIGHT);
        m_canvas.setBackground(Color.BLACK);
    }

    public void start() {
        String input = "graph1.txt";

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

        Nanomuncher test = new Nanomuncher(10, "urld");
        System.out.println(test);
        int[] dir = test.getDirVec();
        System.out.println(dir[0] + " " + dir[1]);
        System.out.println(test);
        dir = test.getDirVec();
        System.out.println(dir[0] + " " + dir[1]);
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
                        g.fillRect(x * CELL_SIZE + CELL_SIZE - 2, y * CELL_SIZE
                                - 1, 4, CELL_SIZE + 2);
                    }
                    if (y < SIZE_Y - 1
                            && (board[pos + 1] == 0 || !edges
                                    .contains((board[pos] - 1) * MAX_NUM_NODES
                                            + board[pos + 1] - 1))) {
                        g.setColor(Color.BLUE);
                        g.fillRect(x * CELL_SIZE - 1, y * CELL_SIZE + CELL_SIZE
                                - 2, CELL_SIZE + 2, 4);
                    }
                } else {
                    if (x < SIZE_X - 1 && board[pos + SIZE_Y] != 0) {
                        g.setColor(Color.BLUE);
                        g.fillRect(x * CELL_SIZE + CELL_SIZE - 2, y * CELL_SIZE
                                - 1, 4, CELL_SIZE + 2);
                    }
                    if (y < SIZE_Y - 1 && board[pos + 1] != 0) {
                        g.setColor(Color.BLUE);
                        g.fillRect(x * CELL_SIZE - 1, y * CELL_SIZE + CELL_SIZE
                                - 2, CELL_SIZE + 2, 4);
                    }
                }
            }
        }

        m_canvas.repaint();
    }

    private void updateCanvas() {
        // Update canvas with the current state
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

    private int dirCharToNum(char c) {
        return (c == 'u') ? 0 : (c == 'l') ? 1 : (c == 'd') ? 2
                : (c == 'r') ? 3 : -1;
    }

    private int[] parseProgram(String str) {
        return new int[] { dirCharToNum(str.charAt(0)),
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
        ++counter;
        return (curProgram == 0) ? new int[] { 0, -1 }
                : (curProgram == 1) ? new int[] { -1, 0 }
                        : (curProgram == 2) ? new int[] { 0, 1 } : new int[] {
                                1, 0 };
    }

    public Nanomuncher(int pos, String program) {
        this.pos = pos;
        this.program = parseProgram(program);
    }
}
