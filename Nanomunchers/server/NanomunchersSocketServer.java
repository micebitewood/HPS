import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

public class NanomunchersSocketServer {
    
    int numOfMunchers;
    int noMoveCount;
    List<String> nodeStrs;
    List<String> edgeStrs;
    Map<Integer, Integer[]> locations;
    Map<Integer, Map<Character, Integer>> edges;
    Set<Integer> munched;
    Set<Integer> newlyMunched;
    Map<Integer, Player> newMunchers;
    Set<Integer> dups;
    
    private Player player1;
    private Player player2;
    private Random random;
    
    private NanomunchersSocketServer(String input, int numOfMunchers) {
        this.numOfMunchers = numOfMunchers;
        noMoveCount = 0;
        locations = new HashMap<Integer, Integer[]>();
        nodeStrs = new ArrayList<String>();
        edgeStrs = new ArrayList<String>();
        munched = new HashSet<Integer>();
        newlyMunched = new HashSet<Integer>();
        dups = new HashSet<Integer>();
        newMunchers = new HashMap<Integer, Player>();
        edges = new HashMap<Integer, Map<Character, Integer>>();
        random = new Random(System.currentTimeMillis());
        
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
    }
    
    public NanomunchersSocketServer(String input, int numOfMunchers, int port1, int port2) throws InterruptedException {
        this(input, numOfMunchers);
        try {
            player1 = new Player(port1, this);
            player2 = new Player(port2, this);
            player1.start();
            player2.start();
            player1.join();
            player2.join();
            player1.setOpponent(player2);
            player2.setOpponent(player1);
        } catch (IOException e) {
            System.out.println("cannot connect to client");
            System.exit(0);
        }
    }
    
    public NanomunchersSocketServer(String input, int numOfMunchers, int port) throws InterruptedException {
        this(input, numOfMunchers);
        try {
            player1 = new Player(port, this);
            player1.start();
            player1.join();
            player2 = new Player();
            player2.setOpponent(player1);
            player1.setOpponent(player2);
        } catch (IOException e) {
            System.out.println("cannot connect to client");
            System.exit(0);
        }
    }
    
    public void startGame() throws IOException {
        while (!player1.isGameOver && !player2.isGameOver) {
            if (noMoveCount >= 8) {
                System.out.println("=================final score=================");
                System.out.println(0 + " : " + 0);
                System.exit(0);
            } else if (noMoveCount != -1) {
                System.out.println("current noMoveCount: " + noMoveCount);
            }
            System.out.println("**** Remaining nodes: " + (locations.size() - munched.size()) + " ****");
            System.out.println("player1 move");
            Map<Integer, Nanomuncher> move1 = player1.move();
            System.out.println("player2 move");
            Map<Integer, Nanomuncher> move2 = player2.move();
            solveConflicts(move1, move2);
            munched.addAll(newlyMunched);
            player1.getStatus();
            player2.getStatus();
            newlyMunched.clear();
            System.out.println("player1 get move");
            player1.getNextMove();
            System.out.println("player2 get move");
            player2.getNextMove();
            solveConflicts();
            munched.addAll(newlyMunched);
        }
        Player remainingPlayer;
        if (player1.isGameOver) {
            System.out.println("Player2 remains");
            remainingPlayer = player2;
        } else {
            System.out.println("Player1 remains");
            remainingPlayer = player1;
        }
        while (!remainingPlayer.isGameOver) {
            System.out.println("**** Remaining nodes: " + (locations.size() - munched.size()) + " ****");
            System.out.println("remaining player move");
            remainingPlayer.move();
            munched.addAll(newlyMunched);
            remainingPlayer.getStatus();
            newlyMunched.clear();
            System.out.println("remaining player get move");
            remainingPlayer.getNextMove();
            munched.addAll(newlyMunched);
        }
        // }
        System.out.println("=================final score=================");
        System.out.println(player1.score + " : " + player2.score);
    }
    
    private void solveConflicts(Map<Integer, Nanomuncher> move1, Map<Integer, Nanomuncher> move2) {
        for (int id : move1.keySet()) {
            if (move2.containsKey(id)) {
                Nanomuncher muncher1 = move1.get(id);
                Nanomuncher muncher2 = move2.get(id);
                char dir1 = muncher1.program.charAt(muncher1.programCounter);
                char dir2 = muncher2.program.charAt(muncher2.programCounter);
                System.out.println("conflicts: " + id + " player1: " + dir1 + " player2: " + dir2);
                if (isLarger(dir1, dir2)) {
                    System.out.println("player1 wins");
                    player2.idToMunchers.remove(id);
                    player2.score--;
                } else {
                    System.out.println("player2 wins");
                    player1.idToMunchers.remove(id);
                    player1.score--;
                }
            }
        }
    }
    
    private void solveConflicts() {
        if (dups.size() != 0) {
            for (int id : dups) {
                System.out.print("dup " + id + ": ");
                if (random.nextBoolean() == true) {
                    System.out.println("chooses player1");
                    newMunchers.put(id, player1);
                    player2.idToMunchers.remove(id);
                    player2.score--;
                } else {
                    System.out.println("chooses player2");
                    newMunchers.put(id, player2);
                    player1.idToMunchers.remove(id);
                    player1.score--;
                }
            }
        }
        dups.clear();
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
    
    public static void main(String[] args) throws IOException, InterruptedException {
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

class Player extends Thread {
    ServerSocket server;
    Socket socket;
    PrintWriter out;
    BufferedReader in;
    NanomunchersSocketServer game;
    String teamName;
    Player opponent;
    int score;
    Map<Integer, Nanomuncher> idToMunchers;
    long timeRemaining;
    List<String> status;
    int totalMunchers;
    int currMuncherNum;
    boolean isGameOver;
    
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
    
    public Map<Integer, Nanomuncher> move() {
        Map<Integer, Nanomuncher> moves = new HashMap<Integer, Nanomuncher>();
        Set<Integer> removes = new HashSet<Integer>();
        for (Entry<Integer, Nanomuncher> idToMuncher : idToMunchers.entrySet()) {
            Nanomuncher muncher = idToMuncher.getValue();
            if (muncher != null) {
                removes.add(muncher.position);
                System.out.println("muncher from: " + muncher.position + " " + muncher.program + " "
                                   + muncher.programCounter + " loc: " + game.locations.get(muncher.position)[0] + ", "
                                   + game.locations.get(muncher.position)[1]);
                String program = muncher.program;
                int programCounter = muncher.programCounter;
                Map<Character, Integer> edges = game.edges.get(muncher.position);
                System.out.print("neibours: ");
                if (edges == null) {
                    continue;
                }
                for (Entry<Character, Integer> entry : edges.entrySet()) {
                    System.out.print(entry.getKey() + " " + entry.getValue() + ", ");
                }
                System.out.println();
                int count = 0;
                do {
                    programCounter++;
                    programCounter %= 4;
                    char direction = program.charAt(programCounter);
                    if (edges.containsKey(direction) && !game.munched.contains(edges.get(direction))) {
                        int id = edges.get(direction);
                        moves.put(id, muncher);
                        muncher.position = id;
                        muncher.programCounter = programCounter;
                        game.newlyMunched.add(id);
                        System.out.println("munched: " + id + " " + muncher.program + " " + muncher.programCounter
                                           + " loc: " + game.locations.get(id)[0] + ", "
                                           + game.locations.get(id)[1]);
                        score++;
                        break;
                    }
                    count++;
                } while (count < 4);
                if (count == 4) {
                    System.out.println("this muncher dies in " + muncher.position);
                }
            }
        }
        for (int id : removes) {
            idToMunchers.remove(id);
        }
        idToMunchers.putAll(moves);
        return moves;
    }
    
    private void setName() throws IOException {
        this.teamName = receive();
    }
    
    private void send(List<String> strs) {
        for (String str : strs) {
            out.println(str);
        }
        if (strs.size() == 0) {
            out.println(0 + "");
        }
        out.println("<EOM>");
        if (isGameOver) {
            System.out.println("game is over");
            try {
                close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (gameOver())
            isGameOver = true;
    }
    
    public String receive() throws IOException {
        StringBuffer sb = new StringBuffer();
        String temp;
        while (!(temp = in.readLine()).equalsIgnoreCase("<EOM>")) {
            sb.append(temp + "\n");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }
    
    public void getNextMove() {
        send(status);
        long startTime = System.currentTimeMillis();
        if (gameOver())
            return;
        Move nextMove = new Move(this);
        nextMove.start();
        try {
            nextMove.join(timeRemaining);
        } catch (InterruptedException e) {
            System.out.println("interrupted");
        }
        timeRemaining -= System.currentTimeMillis() - startTime;
        if (timeRemaining <= 0) {
            isGameOver = true;
        }
        Map<Integer, String> idAndPrograms = nextMove.moves;
        if (idAndPrograms.size() == 0 && game.noMoveCount != -1)
            game.noMoveCount++;
        if (idAndPrograms != null && idAndPrograms.size() != 0) {
            game.noMoveCount = -1;
            for (int id : idAndPrograms.keySet()) {
                if (game.newMunchers.containsKey(id)) {
                    System.out.println("dup in: " + id);
                    game.dups.add(id);
                } else if (!game.munched.contains(id)) {
                    System.out.println("new muncher arrives: " + id + " " + idAndPrograms.get(id) + " loc: "
                                       + game.locations.get(id)[0] + ", "
                                       + game.locations.get(id)[1]);
                    idToMunchers.put(id, new Nanomuncher(idAndPrograms.get(id), id));
                    game.newlyMunched.add(id);
                    game.newMunchers.put(id, this);
                    score++;
                }
                currMuncherNum++;
                if (currMuncherNum == totalMunchers) {
                    break;
                }
            }
        }
    }
    
    public void getStatus() {
        
        status = new ArrayList<String>();
        if (gameOver()) {
            return;
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
        sb.append(idToMunchers.size() + ":");
        for (Entry<Integer, Nanomuncher> entry : idToMunchers.entrySet()) {
            Nanomuncher muncher = entry.getValue();
            sb.append(muncher.position + "/" + muncher.program + "/" + muncher.programCounter + ",");
        }
        status.add(sb.toString().substring(0, sb.length() - 1));
        // opponent's munchers
        sb = new StringBuffer();
        sb.append(opponent.idToMunchers.size() + ":");
        for (int id : opponent.idToMunchers.keySet()) {
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
    }
    
    private boolean gameOver() {
        return (idToMunchers.size() == 0 && currMuncherNum == totalMunchers)
        || game.locations.size() - game.munched.size() == 0;
    }
    
    public Player() {
        idToMunchers = new HashMap<Integer, Nanomuncher>();
        
        totalMunchers = 0;
        currMuncherNum = 0;
        score = 0;
        isGameOver = true;
    }
    
    public Player(int port, NanomunchersSocketServer game) throws IOException {
        this.game = game;
        totalMunchers = game.numOfMunchers;
        currMuncherNum = 0;
        isGameOver = false;
        timeRemaining = 120 * 1000;
        server = new ServerSocket(port);
    }
    
    private void close() throws IOException {
        in.close();
        out.close();
        socket.close();
    }
    
    @Override
    public void run() {
        try {
            this.socket = server.accept();
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            idToMunchers = new HashMap<Integer, Nanomuncher>();
            setName();
            send(genData(game.nodeStrs, game.edgeStrs));
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
}

class Nanomuncher {
    String program;
    int programCounter;
    int position;// nodeid
    
    public Nanomuncher(String program, int position) {
        this.program = program;
        this.position = position;
        this.programCounter = -1;
    }
}

class Move extends Thread {
    Player player;
    public Map<Integer, String> moves;
    
    @Override
    public void run() {
        try {
            String str = player.receive();
            String[] specs = str.split(":");
            int num = Integer.parseInt(specs[0]);
            if (num > 0) {
                String[] moveStrs = specs[1].split(",");
                if (num != moveStrs.length) {
                    System.out.println("invalid number");
                    moves = null;
                } else {
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public Move(Player player) {
        this.player = player;
        moves = new HashMap<Integer, String>();
    }
}
