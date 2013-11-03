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
    List<String> nodeStrs;
    List<String> edgeStrs;
    Map<Integer, Integer[]> locations;
    Map<Integer, Map<Character, Integer>> edges;
    Set<Integer> munched;
    Set<Integer> newlyMunched;
    Map<Integer, Player> newMunchers;
    Set<Integer> dups;
    Map<String, String> vizUpdate;
    
    private Player player1;
    private Player player2;
    private Visualizer viz;
    private Random random;
    private boolean hasViz;
    
    private NanomunchersSocketServer(String input, int numOfMunchers) {
        this.numOfMunchers = numOfMunchers;
        locations = new HashMap<Integer, Integer[]>();
        nodeStrs = new ArrayList<String>();
        edgeStrs = new ArrayList<String>();
        munched = new HashSet<Integer>();
        newlyMunched = new HashSet<Integer>();
        dups = new HashSet<Integer>();
        newMunchers = new HashMap<Integer, Player>();
        edges = new HashMap<Integer, Map<Character, Integer>>();
        vizUpdate = new HashMap<String, String>();
        random = new Random(System.currentTimeMillis());
        hasViz = false;
        
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
    
    public NanomunchersSocketServer(String input, int numOfMunchers, int time1, int port1, int time2, int port2,
                                    int delay, int portViz)
    throws InterruptedException {
        this(input, numOfMunchers);
        hasViz = true;
        try {
            if (time2 != 0 || port2 != 0) {
                player1 = new Player(time1, port1, this, 0);
                player2 = new Player(time2, port2, this, 1);
                player1.start();
                player2.start();
                player1.join();
                player2.join();
                player1.setOpponent(player2);
                player2.setOpponent(player1);
                viz = new Visualizer(portViz, this, delay, new String[] {player1.teamName, player2.teamName });
            } else {
                player1 = new Player(time1, port1, this, 0);
                player1.start();
                player1.join();
                player2 = new Player();
                player2.setOpponent(player1);
                player1.setOpponent(player2);
                viz = new Visualizer(portViz, this, delay, new String[] {player1.teamName, "" });
            }
        } catch (IOException e) {
            System.out.println("cannot connect to client");
            System.exit(0);
        }
    }
    
    public NanomunchersSocketServer(String input, int numOfMunchers, int time1, int port1, int time2, int port2)
    throws InterruptedException {
        this(input, numOfMunchers);
        try {
            player1 = new Player(time1, port1, this, 0);
            player2 = new Player(time2, port2, this, 1);
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
    
    public NanomunchersSocketServer(String input, int numOfMunchers, int time, int port) throws InterruptedException {
        this(input, numOfMunchers);
        try {
            player1 = new Player(time, port, this, 0);
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
        int count = 0;
        boolean isOver = false;
        while (!player1.isGameOver && !player2.isGameOver && !isOver) {
            System.out.println("========== Starting round " + count + " with " + (locations.size() - munched.size())
                               + " remaining nodes");
            player1.getStatus();
            player2.getStatus();
            newlyMunched.clear();
            if (player1.currMuncherNum < player1.totalMunchers && !player1.isDone) {
                System.out.println("  ** Receiving moves from " + player1.teamName);
                player1.getNextMove();
            } else {
                if (!player1.isDone) {
                    player1.isDone = true;
                    player1.getNextMove();
                }
            }
            if (player2.currMuncherNum < player2.totalMunchers && !player2.isDone) {
                System.out.println("  ** Receiving moves from " + player2.teamName);
                player2.getNextMove();
            } else {
                if (!player2.isDone) {
                    player2.isDone = true;
                    player2.getNextMove();
                }
            }
            solveConflicts();
            munched.addAll(newlyMunched);
            if (!player1.idToMunchers.isEmpty())
                System.out.println("  ** " + player1.teamName + "'s munchers are moving");
            Map<Integer, Nanomuncher> move1 = player1.move();
            if (!player2.idToMunchers.isEmpty())
                System.out.println("  ** " + player2.teamName + "'s munchers are moving");
            Map<Integer, Nanomuncher> move2 = player2.move();
            solveConflicts(move1, move2);
            munched.addAll(newlyMunched);
            System.out.println("  ** Current scores are: " + player1.teamName + ": " + player1.score + ", "
                               + player2.teamName + ": " + player2.score);
            if (hasViz)
                viz.update();
            count++;
            if (newlyMunched.isEmpty())
                isOver = true;
        }
        System.out.println("==================== Final scores ====================");
        System.out.println(player1.teamName + ": " + player1.score + ", " + player2.teamName + ": " + player2.score);
        if (hasViz)
            viz.finish();
    }
    
    private void solveConflicts(Map<Integer, Nanomuncher> move1, Map<Integer, Nanomuncher> move2) {
        for (int id : move1.keySet()) {
            if (move2.containsKey(id)) {
                Nanomuncher muncher1 = move1.get(id);
                Nanomuncher muncher2 = move2.get(id);
                char dir1 = muncher1.program.charAt(muncher1.programCounter);
                char dir2 = muncher2.program.charAt(muncher2.programCounter);
                System.out.println("     - Resolving confliction at node " + id + ", moving directions: "
                                   + dir1 + ", " + dir2);
                if (isLarger(dir1, dir2)) {
                    vizUpdate.remove("1," + id + "," + dir2);
                    vizUpdate.put("1," + id + ",c", "x");
                    System.out.println("       " + player1.teamName + "'s muncher wins and lives on");
                    player2.idToMunchers.remove(id);
                    player2.score--;
                } else {
                    vizUpdate.remove("0," + id + "," + dir1);
                    vizUpdate.put("0," + id + ",c", "x");
                    System.out.println("       " + player2.teamName + "'s muncher wins and lives on");
                    player1.idToMunchers.remove(id);
                    player1.score--;
                }
            }
        }
    }
    
    private void solveConflicts() {
        if (dups.size() != 0) {
            for (int id : dups) {
                System.out.println("     - Resolving confliction at node " + id);
                if (random.nextBoolean() == true) {
                    System.out.println("       " + player1.teamName + "'s muncher wins and lives on");
                    newMunchers.put(id, player1);
                    vizUpdate.put("0," + id + ",n", player1.idToMunchers.get(id).program);
                    vizUpdate.remove("1," + id + ",n");
                    vizUpdate.put("1," + id + ",c", "x");
                    player2.idToMunchers.remove(id);
                    player2.score--;
                } else {
                    System.out.println("       " + player2.teamName + "'s muncher wins and lives on");
                    newMunchers.put(id, player2);
                    vizUpdate.put("1," + id + ",n", player2.idToMunchers.get(id).program);
                    vizUpdate.remove("0," + id + ",n");
                    vizUpdate.put("0," + id + ",c", "x");
                    player1.idToMunchers.remove(id);
                    player1.score--;
                }
            }
        }
        dups.clear();
    }
    
    public boolean isLarger(char dir1, char dir2) {
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
        if (args.length != 4 && args.length != 6 && args.length != 8) {
            System.out
            .println("adversarial mode with visualization:");
            System.out
            .println("    java -jar SocketServerNanomunchers.jar <input> <numOfMunchers> <time1> <port1> <time2> <port2> <delayVizInMillis> <portViz>");
            System.out
            .println("non-adversarial mode with visualization:");
            System.out
            .println("    java -jar SocketServerNanomunchers.jar <input> <numOfMunchers> <time> <port> 0 0 <delayVizInMillis> <portViz>");
            System.out
            .println("adversarial mode without visualization:");
            System.out
            .println("    java -jar SocketServerNanomunchers.jar <input> <numOfMunchers> <time1> <port1> <time2> <port2>");
            System.out
            .println("non-adversarial mode without visualization:");
            System.out
            .println("    java -jar SocketServerNanomunchers.jar <input> <numOfMunchers> <time> <port>");
        } else {
            while (true) {
                NanomunchersSocketServer server;
                String inputFile = args[0];
                int numOfMunchers = Integer.parseInt(args[1]);
                int time1 = Integer.parseInt(args[2]);
                int port1 = Integer.parseInt(args[3]);
                if (args.length > 6) {
                    int time2 = Integer.parseInt(args[4]);
                    int port2 = Integer.parseInt(args[5]);
                    int delay = Integer.parseInt(args[6]);
                    int portViz = Integer.parseInt(args[7]);
                    server =
                    new NanomunchersSocketServer(inputFile, numOfMunchers, time1, port1, time2, port2, delay,
                                                 portViz);
                } else if (args.length > 4) {
                    int time2 = Integer.parseInt(args[4]);
                    int port2 = Integer.parseInt(args[5]);
                    server = new NanomunchersSocketServer(inputFile, numOfMunchers, time1, port1, time2, port2);
                } else {
                    server = new NanomunchersSocketServer(inputFile, numOfMunchers, time1, port1);
                }
                server.startGame();
            }
        }
    }
}

class Player extends Thread {
    ServerSocket server;
    Socket socket;
    PrintWriter out;
    BufferedReader in;
    NanomunchersSocketServer game;
    int playerid;
    String teamName;
    Player opponent;
    int score;
    Map<Integer, Nanomuncher> idToMunchers;
    long timeRemaining;
    List<String> status;
    int totalMunchers;
    int currMuncherNum;
    boolean isGameOver;
    boolean isDone;
    
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
                System.out.println("     - Muncher at node " + muncher.position + " ("
                                   + game.locations.get(muncher.position)[0] + ", "
                                   + game.locations.get(muncher.position)[1] + ")/" + muncher.program
                                   + "/" + (muncher.programCounter + 1) % 4 + " tries to move");
                String program = muncher.program;
                int programCounter = muncher.programCounter;
                Map<Character, Integer> edges = game.edges.get(muncher.position);
                if (edges == null) {
                    game.vizUpdate.put(playerid + "," + muncher.position + ",x", "x");
                    System.out.println("       Nowhere to go, starved at node " + muncher.position);
                    continue;
                }
                int count = 0;
                do {
                    programCounter++;
                    programCounter %= 4;
                    char direction = program.charAt(programCounter);
                    if (edges.containsKey(direction) && !game.munched.contains(edges.get(direction))) {
                        int id = edges.get(direction);
                        if (moves.containsKey(id)) {
                            System.out.println("       Moved " + direction + " to node " + id + " ("
                                               + game.locations.get(id)[0] + ", " + game.locations.get(id)[1] + ")");
                            char prevDirection = moves.get(id).program.charAt(moves.get(id).programCounter);
                            if (game.isLarger(prevDirection, direction)) {
                                game.vizUpdate.put(playerid + "," + muncher.position + "," + direction, "x");
                                System.out.println("       Confliction detected at " + id + ", this muncher dies");
                                break;
                            } else {
                                game.vizUpdate.remove(playerid + "," + id + "," + prevDirection);
                                System.out.println("       Confliction detected at " + id + ", this muncher lives on");
                                score--;
                            }
                        }
                        moves.put(id, muncher);
                        muncher.position = id;
                        muncher.programCounter = programCounter;
                        game.newlyMunched.add(id);
                        game.vizUpdate.put(playerid + "," + id + "," + program.charAt(programCounter),
                                           program.substring((programCounter + 1) % 4));
                        System.out.println("       Moved " + direction + " to node " + id + " ("
                                           + game.locations.get(id)[0] + ", " + game.locations.get(id)[1] + ")");
                        score++;
                        break;
                    }
                    count++;
                } while (count < 4);
                if (count == 4) {
                    game.vizUpdate.put(playerid + "," + muncher.position + ",x", "x");
                    System.out.println("       Starved at node " + muncher.position);
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
        if (strs.size() == 0) {
            try {
                close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
        if (isDone) {
            send(new ArrayList<String>());
            return;
        }
        send(status);
        if (gameOver())
            return;
        Move nextMove = new Move(this);
        long startTime = System.currentTimeMillis();
        nextMove.start();
        try {
            nextMove.join(timeRemaining);
        } catch (InterruptedException e) {
            System.out.println("interrupted");
        }
        timeRemaining -= System.currentTimeMillis() - startTime;
        if (timeRemaining <= 0) {
            isDone = true;
            System.out.println("  ** " + teamName + " timed out");
            return;
        }
        Map<Integer, String> idAndPrograms = nextMove.moves;
        if (idAndPrograms != null && idAndPrograms.size() != 0) {
            for (int id : idAndPrograms.keySet()) {
                boolean isDup = false;
                if (game.newMunchers.containsKey(id)) {
                    game.dups.add(id);
                }
                if (!game.munched.contains(id)) {
                    System.out.println("     - New muncher deployed at node " + id + " ("
                                       + game.locations.get(id)[0] + ", "
                                       + game.locations.get(id)[1] + ")" + "/" + idAndPrograms.get(id) + "/0");
                    idToMunchers.put(id, new Nanomuncher(idAndPrograms.get(id), id));
                    game.newlyMunched.add(id);
                    game.newMunchers.put(id, this);
                    game.vizUpdate.put(playerid + "," + id + ",n", idAndPrograms.get(id));
                    score++;
                } else {
                    System.out.println("     - New muncher starves at node " + id);
                }
                if (isDup) {
                    System.out.println("       Confliction detected at node " + id + ", to be resolved");
                }
                currMuncherNum++;
                if (currMuncherNum == totalMunchers) {
                    System.out.println("  ** All of " + teamName + "'s munchers have been deployed");
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
            sb.append(muncher.position + "/" + muncher.program + "/" + (muncher.programCounter + 1) % 4 + ",");
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
        isDone = true;
    }
    
    public Player(int time, int port, NanomunchersSocketServer game, int playerid) throws IOException {
        this.game = game;
        this.playerid = playerid;
        totalMunchers = game.numOfMunchers;
        currMuncherNum = 0;
        isGameOver = false;
        isDone = false;
        timeRemaining = time * 1000;
        server = new ServerSocket(port);
    }
    
    private void close() throws IOException {
        in.close();
        out.close();
        socket.close();
        server.close();
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

class Visualizer {
    ServerSocket server;
    Socket socket;
    PrintWriter out;
    BufferedReader in;
    NanomunchersSocketServer game;
    int delay;
    String[] teamNames;
    
    private List<String> genData(List<String> nodes, List<String> edges) {
        List<String> data = new ArrayList<String>();
        data.add(teamNames[0]);
        data.add(teamNames[1]);
        data.add(delay + "");
        data.add("nodeid,xloc,yloc");
        data.addAll(nodes);
        data.add("nodeid1,nodeid2");
        data.addAll(edges);
        return data;
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
    
    public void update() {
        Map<String, String> vizUpdate = game.vizUpdate;
        if (vizUpdate.size() == 0)
            return;
        for (Entry<String, String> entry : vizUpdate.entrySet()) {
            String str = entry.getKey() + "," + entry.getValue();
            out.println(str);
        }
        out.println("<EOM>");
        try {
            receive();
        } catch (IOException e) {
        }
        game.vizUpdate.clear();
    }
    
    public void finish() {
        send(new ArrayList<String>());
        try {
            close();
        } catch (IOException e) {
        }
    }
    
    public Visualizer(int port, NanomunchersSocketServer game, int delay, String[] teamNames) throws IOException {
        this.game = game;
        this.delay = delay;
        this.teamNames = teamNames;
        server = new ServerSocket(port);
        this.socket = server.accept();
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        
        send(genData(game.nodeStrs, game.edgeStrs));
        receive();
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
        this.programCounter = 3;
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
