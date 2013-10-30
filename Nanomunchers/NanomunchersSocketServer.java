import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class NanomunchersSocketServer {
    
    int numOfMunchers;
    List<String> nodeStrs;
    List<String> edgeStrs;
    Map<Integer, Integer[]> locations;
    Map<Integer, Map<Character, Integer>> edges;
    Set<Integer> munched;
    Set<Integer> newlyMunched;
    Map<Integer, Player> newMunchers;
    Set<Integer> dups;
    
    private ServerSocket server;
    private ServerSocket animation;
    private Player player1;
    private Player player2;
    private boolean isAdversarial;
    private Random random;
    
    public NanomunchersSocketServer(String input, int numOfMunchers, int port1, int port2) {
        this(input, numOfMunchers, port1);
        isAdversarial = true;
        try {
            player2 = new Player(port2, this);
            player1.setOpponent(player2);
            player2.setOpponent(player1);
        } catch (IOException e) {
            System.out.println("cannot connect to client");
            System.exit(0);
        }
    }
    
    public NanomunchersSocketServer(String input, int numOfMunchers, int port) {
        this.numOfMunchers = numOfMunchers;
        locations = new HashMap<Integer, Integer[]>();
        nodeStrs = new ArrayList<String>();
        edgeStrs = new ArrayList<String>();
        munched = new HashSet<Integer>();
        newlyMunched = new HashSet<Integer>();
        dups = new HashSet<Integer>();
        newMunchers = new HashMap<Integer, Player>();
        edges = new HashMap<Integer, Map<Character, Integer>>();
        random = new Random(System.currentTimeMillis());
        isAdversarial = false;
        
        BufferedReader br;
        boolean startNodes = false;
        boolean startEdges = false;
        try {
            br = new BufferedReader(new FileReader(input));
            String line;
            while ((line = br.readLine()) != null) {
                String content = line.trim().toLowerCase();
                if (content.equals(""))
                    continue;
                if (content.contains("xloc")) {
                    startNodes = true;
                } else if (content.contains("nodeid1")) {
                    startEdges = true;
                } else if (startEdges) {
                    edgeStrs.add(content);
                } else if (startNodes) {
                    nodeStrs.add(content);
                }
            }
            parseData(nodeStrs, edgeStrs);
        } catch (IOException e) {
            System.out.println("cannot open input file, please check again");
            System.exit(0);
        }
        try {
            player1 = new Player(port, this);
            player1.setOpponent(new Player());
        } catch (IOException e) {
            System.out.println("cannot connect to client");
            System.exit(0);
        }
    }
    
    public void startGame() throws IOException {
        if (!isAdversarial) {
            while (!player1.gameOver()) {
                newlyMunched.clear();
                player1.move();
                player1.getNextMove();
            }
            player1.close();
        } else {
            while (!player1.gameOver() || !player2.gameOver()) {
                newlyMunched.clear();
                Map<Integer, Character> move1 = player1.move();
                Map<Integer, Character> move2 = player2.move();
                solveConflicts(move1, move2);
                dups.clear();
                player1.getNextMove();
                player2.getNextMove();
                solveConflicts();
            }
            player1.close();
            player2.close();
        }
    }
    
    private void solveConflicts(Map<Integer, Character> move1, Map<Integer, Character> move2) {
        for (int id : move1.keySet()) {
            if (move2.containsKey(id)) {
                char dir1 = move1.get(id);
                char dir2 = move2.get(id);
                if (isLarger(dir1, dir2)) {
                    player2.idToMuncher.remove(id);
                    player2.score--;
                } else {
                    player1.idToMuncher.remove(id);
                    player1.score--;
                }
            }
        }
    }
    
    private void solveConflicts() {
        if (dups.size() != 0) {
            for (int id : dups) {
                if (random.nextBoolean() == true) {
                    newMunchers.put(id, player1);
                    player2.score--;
                } else {
                    newMunchers.put(id, player2);
                    player1.score--;
                }
            }
        }
    }
    
    private boolean isLarger(char dir1, char dir2) {
        if (dir1 == 'u')
            return true;
        if (dir1 == 'l' && dir2 != 'u')
            return true;
        if (dir1 == 'd' && dir2 == 'r')
            return true;
        return false;
    }
    
    private void parseData(List<String> nodes, List<String> edges) {
        for (String node : nodes) {
            String[] nodeSpecs = node.split(",");
            Integer[] locs = {Integer.parseInt(nodeSpecs[1]), Integer.parseInt(nodeSpecs[2]) };
            locations.put(Integer.parseInt(nodeSpecs[0]), locs);
        }
        for (String edge : edges) {
            String[] edgeSpecs = edge.split(",");
            int node1 = Integer.parseInt(edgeSpecs[0]);
            int node2 = Integer.parseInt(edgeSpecs[1]);
            if (!this.edges.containsKey(node1)) {
                this.edges.put(node1, new HashMap<Character, Integer>());
            }
            if (!this.edges.containsKey(node2)) {
                this.edges.put(node2, new HashMap<Character, Integer>());
            }
            Integer[] loc1 = locations.get(node1);
            Integer[] loc2 = locations.get(node2);
            if (loc1[0].equals(loc2[0])) {
                if (loc1[1] - loc2[1] == 1) {
                    this.edges.get(node1).put('u', node2);
                    this.edges.get(node2).put('d', node1);
                } else {
                    this.edges.get(node1).put('d', node2);
                    this.edges.get(node2).put('u', node1);
                }
            } else {
                if (loc1[0] - loc2[0] == 1) {
                    this.edges.get(node1).put('l', node2);
                    this.edges.get(node2).put('r', node1);
                } else {
                    this.edges.get(node1).put('r', node2);
                    this.edges.get(node2).put('l', node1);
                }
            }
        }
    }
    
    public static void main(String[] args) throws IOException {
        // args: input numOfMunchers port port
        if (args.length != 3 && args.length != 4) {
            System.out.println("java SocketServer <input> <numOfMunchers> <port> <port>");
        }
        NanomunchersSocketServer server;
        String inputFile = args[0];
        int numOfMunchers = Integer.parseInt(args[1]);
        int port1 = Integer.parseInt(args[2]);
        if (args.length == 4) {
            int port2 = Integer.parseInt(args[3]);
            server = new NanomunchersSocketServer(inputFile, numOfMunchers, port1, port2);
        } else {
            server = new NanomunchersSocketServer(inputFile, numOfMunchers, port1);
        }
        server.startGame();
    }
}

class Player {
    ServerSocket server;
    Socket socket;
    PrintWriter out;
    BufferedReader in;
    NanomunchersSocketServer game;
    String teamName;
    Player opponent;
    int score;
    Nanomuncher[] munchers;
    Map<Integer, Nanomuncher> idToMuncher;
    long timeRemaining;
    
    int totalMunchers;
    int currMuncherNum;
    
    private List<String> genData(List<String> nodes, List<String> edges) {
        List<String> data = new ArrayList<String>();
        data.add("nodeid,xloc,yloc");
        data.addAll(nodes);
        data.add("nodeid1,nodeid2");
        data.addAll(edges);
        return data;
    }
    
    public void setOpponent(Player opponent) {
        this.opponent = opponent;
    }
    
    public Map<Integer, Character> move() {
        Map<Integer, Character> moves = new HashMap<Integer, Character>();
        for (int i = 0; i < munchers.length; i++) {
            Nanomuncher muncher = munchers[i];
            if (muncher != null) {
                String program = muncher.program;
                int programCounter = muncher.programCounter;
                Map<Character, Integer> edges = game.edges.get(muncher.position);
                idToMuncher.remove(muncher.position);
                do {
                    char direction = program.charAt(programCounter);
                    if (edges.containsKey(direction)) {
                        int id = edges.get(direction);
                        moves.put(id, direction);
                        muncher.programCounter = programCounter;
                        game.newlyMunched.add(id);
                        idToMuncher.put(id, muncher);
                        score++;
                        break;
                    }
                    programCounter++;
                } while (programCounter != muncher.programCounter);
                if (programCounter == muncher.programCounter) {
                    munchers[i] = null;
                }
            }
        }
        game.munched.addAll(game.newlyMunched);
        return moves;
    }
    
    private void getName() throws IOException {
        send("TeamName?");
        this.teamName = receive();
    }
    
    private void send(String str) {
        send(Arrays.asList(str));
    }
    
    private void send(List<String> strs) {
        for (String str : strs) {
            out.println(str);
        }
        if (strs.size() == 0)
            out.println(0 + "");
        out.println("<EOM>");
    }
    
    private String receive() throws IOException {
        StringBuffer sb = new StringBuffer();
        String temp;
        while (!(temp = in.readLine()).equalsIgnoreCase("<EOM>")) {
            sb.append(temp + "@");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }
    
    public void getNextMove() {
        try {
            send(getStatus());
            game.newlyMunched.clear();
            Move nextMove = new Move(receive());
            Map<Integer, String> idAndPrograms = nextMove.moves;
            if (idAndPrograms != null) {
                for (int id : idAndPrograms.keySet()) {
                    if (game.newMunchers.containsKey(id)) {
                        game.dups.add(id);
                    } else if (!game.munched.contains(id)) {
                        munchers[currMuncherNum] = new Nanomuncher(idAndPrograms.get(id), id);
                        idToMuncher.put(id, munchers[currMuncherNum]);
                        game.newMunchers.put(id, this);
                        score++;
                    }
                    currMuncherNum++;
                    if (currMuncherNum == totalMunchers) {
                        break;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("error in receiving messages from clients, skip this round");
        }
    }
    
    private List<String> getStatus() {
        
        List<String> status = new ArrayList<String>();
        if (gameOver()) {
            return status;
        }
        // newly munched nodes
        StringBuffer sb = new StringBuffer();
        Set<Integer> newlyMunched = game.newlyMunched;
        sb.append(newlyMunched.size() + ":");
        for (int id : newlyMunched) {
            sb.append(id + ",");
        }
        status.add(sb.toString().substring(0, sb.length() - 1));
        // player's munchers
        sb = new StringBuffer();
        sb.append(idToMuncher.size() + ":");
        for (int id : idToMuncher.keySet()) {
            sb.append(id + ",");
        }
        status.add(sb.toString().substring(0, sb.length() - 1));
        // opponent's munchers
        sb = new StringBuffer();
        sb.append(opponent.idToMuncher.size() + ":");
        for (int id : opponent.idToMuncher.keySet()) {
            sb.append(id + ",");
        }
        status.add(sb.toString().substring(0, sb.length() - 1));
        // scores
        sb = new StringBuffer();
        sb.append(score + "," + opponent.score);
        status.add(sb.toString());
        // remaining munchers
        sb = new StringBuffer();
        sb.append((totalMunchers - currMuncherNum) + "," + timeRemaining);
        status.add(sb.toString());
        return status;
    }
    
    public boolean gameOver() {
        return currMuncherNum == totalMunchers;
    }
    
    public Player() {
        munchers = new Nanomuncher[0];
        idToMuncher = new HashMap<Integer, Nanomuncher>();
        
        totalMunchers = 0;
        currMuncherNum = 0;
    }
    
    public Player(int port, NanomunchersSocketServer game) throws IOException {
        this.game = game;
        totalMunchers = game.numOfMunchers;
        currMuncherNum = 0;
        server = new ServerSocket(port);
        this.socket = server.accept();
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        munchers = new Nanomuncher[totalMunchers];
        idToMuncher = new HashMap<Integer, Nanomuncher>();
        
        getName();
        send(genData(game.nodeStrs, game.edgeStrs));
    }
    
    public void close() throws IOException {
        in.close();
        out.close();
        socket.close();
    }
}

class Nanomuncher {
    String program;
    int programCounter;
    int position;// nodeid
    
    public Nanomuncher(String program, int position) {
        this.program = program;
        this.position = position;
        this.programCounter = 0;
    }
}

class Move {
    public Map<Integer, String> moves;
    
    public Move(String str) {
        String[] specs = str.split(":");
        int num = Integer.parseInt(specs[0]);
        String[] moveStrs = specs[1].split(",");
        if (num != moveStrs.length) {
            moves = null;
        } else {
            moves = new HashMap<Integer, String>();
            for (int i = 0; i < num; i++) {
                String[] idAndProgram = moveStrs[i].split("/");
                int id = Integer.parseInt(idAndProgram[0]);
                if (moves.containsKey(id)) {
                    moves = null;
                    return;
                }
                moves.put(id, idAndProgram[1]);
            }
        }
    }
}
