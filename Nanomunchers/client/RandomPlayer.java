import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class RandomPlayer {
    static Socket client;
    static PrintWriter out;
    static BufferedReader in;
    
    private static final String[] programs = {"dlru", "dlur", "drlu", "drul", "dulr", "durl", "ldru", "ldur", "lrdu",
    "lrud", "ludr", "lurd", "rdlu", "rdul", "rldu", "rlud", "rudl", "ruld", "udlr", "udrl", "uldr", "ulrd",
    "urdl", "urld" };
    
    private Random random;
    private Map<Integer, Integer[]> locations;
    private Map<Integer, Map<Character, Integer>> edges;
    private Set<Integer> remainingNodes;
    private List<Nanomuncher> myNanomunchers;
    private List<Integer> otherNanomunchers;
    private int myScore;
    private int opponentScore;
    private int remainingMunchers;
    private int opponentRemainingMunchers;
    private long remainingTime;
    
    private void parseData(String data) {
        String[] specs = data.split("\n");
        boolean startNodes = false;
        boolean startEdges = false;
        for (String line : specs) {
            String content = line.trim().toLowerCase();
            if (content.equals(""))
                continue;
            if (content.contains("xloc")) {
                startNodes = true;
            } else if (content.contains("nodeid1")) {
                startEdges = true;
            } else if (startEdges) {
                String[] edgeSpecs = line.split(",");
                int node1 = Integer.parseInt(edgeSpecs[0]);
                int node2 = Integer.parseInt(edgeSpecs[1]);
                if (!edges.containsKey(node1)) {
                    edges.put(node1, new HashMap<Character, Integer>());
                }
                if (!edges.containsKey(node2)) {
                    edges.put(node2, new HashMap<Character, Integer>());
                }
                Integer[] loc1 = locations.get(node1);
                Integer[] loc2 = locations.get(node2);
                if (loc1[0].equals(loc2[0])) {
                    if (loc1[1] - loc2[1] == 1) {
                        edges.get(node1).put('u', node2);
                        edges.get(node2).put('d', node1);
                    } else {
                        edges.get(node1).put('d', node2);
                        edges.get(node2).put('u', node1);
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
            } else if (startNodes) {
                String[] nodeSpecs = line.split(",");
                Integer[] locs = {Integer.parseInt(nodeSpecs[1]), Integer.parseInt(nodeSpecs[2]) };
                locations.put(Integer.parseInt(nodeSpecs[0]), locs);
                remainingNodes.add(Integer.parseInt(nodeSpecs[0]));
            }
        }
    }
    
    public boolean parseStat(String str) {
        if (str.equals("0")) {
            return false;
        }
        String[] stats = str.split("\n");
        String[] munched = stats[0].split(":");
        if (Integer.parseInt(munched[0]) > 0) {
            String[] nodes = munched[1].split("[,/]");
            for (int i = 0; i < Integer.parseInt(munched[0]); i++) {
                remainingNodes.remove(Integer.parseInt(nodes[i]));
            }
        }
        myNanomunchers = new ArrayList<Nanomuncher>();
        String[] myMunchers = stats[1].split(":");
        if (Integer.parseInt(myMunchers[0]) > 0) {
            String[] myMuncherDetails = myMunchers[1].split(",");
            for (int i = 0; i < Integer.parseInt(myMunchers[0]); i++) {
                String[] muncher = myMuncherDetails[i].split("/");
                myNanomunchers.add(new Nanomuncher(muncher[1], Integer.parseInt(muncher[0]), Integer
                                                   .parseInt(muncher[2])));
            }
        }
        otherNanomunchers = new ArrayList<Integer>();
        String[] otherMunchers = stats[2].split(":");
        if (Integer.parseInt(otherMunchers[0]) > 0) {
            String[] otherMuncherDetails = otherMunchers[1].split(",");
            for (int i = 0; i < Integer.parseInt(otherMunchers[0]); i++) {
                otherNanomunchers.add(Integer.parseInt(otherMuncherDetails[i]));
            }
        }
        String[] scores = stats[3].split(",");
        myScore = Integer.parseInt(scores[0]);
        opponentScore = Integer.parseInt(scores[1]);
        String[] remainingInfo = stats[4].split(",");
        remainingMunchers = Integer.parseInt(remainingInfo[0]);
        opponentRemainingMunchers = Integer.parseInt(remainingInfo[1]);
        remainingTime = Long.parseLong(remainingInfo[2]);
        return true;
    }
    
    public RandomPlayer(int port) throws UnknownHostException, IOException {
        client = new Socket("127.0.0.1", port);
        out = new PrintWriter(client.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        locations = new HashMap<Integer, Integer[]>();
        edges = new HashMap<Integer, Map<Character, Integer>>();
        remainingNodes = new HashSet<Integer>();
        random = new Random(System.currentTimeMillis());
        send( new String[] {"Jaded", "Jaunty", "Jealous", "Jerky", "Jolly", "Joyful", "Juicy", "Jumpy", "Justifiable", "Juvenile"}[random.nextInt(10)]
            + new String[] {"Jam", "Janitor", "Jelly", "Jerk", "Jet", "Jitterbug", "Journalist", "Judge", "Juice", "Juxtaposition"}[random.nextInt(10)]);
        parseData(receive());
    }
    
    public String receive() throws IOException {
        StringBuffer sb = new StringBuffer();
        String temp;
        while (!(temp = in.readLine()).equalsIgnoreCase("<EOM>")) {
            sb.append(temp + "\n");
        }
        sb.deleteCharAt(sb.length() - 1);
        System.out.println("receive:");
        System.out.println(sb.toString());
        return sb.toString();
    }
    
    public void send(String str) {
        System.out.println("send:");
        out.println(str);
        System.out.println(str);
        out.println("<EOM>");
        System.out.println("<EOM>");
    }
    
    public void startGame() throws IOException, InterruptedException {
        while (parseStat(receive())) {
            System.out.println("remaining munchers: " + remainingMunchers);
            Thread.sleep(500);
            randomeMove();
        }
    }
    
    private void randomeMove() {
        int numOfMunchers = Math.min(random.nextInt(remainingMunchers + 1), remainingNodes.size());
        StringBuffer sb = new StringBuffer();
        sb.append(numOfMunchers + ":");
        int count = 0;
        Set<Integer> usedNodes = new HashSet<Integer>();
        while (count < numOfMunchers) {
            for (int id : remainingNodes) {
                if (random.nextBoolean() && !usedNodes.contains(id)) {
                    String program = programs[random.nextInt(programs.length)];
                    usedNodes.add(id);
                    sb.append(id + "/" + program + ",");
                    count++;
                    if (count == numOfMunchers) {
                        break;
                    }
                }
            }
        }
        send(sb.toString().substring(0, sb.length() - 1));
    }
    
    public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
        if (args.length != 1) {
            System.out.println("java RandomPlayer <port>");
            System.exit(0);
        }
        int port = Integer.parseInt(args[0]);
        RandomPlayer player = new RandomPlayer(port);
        player.startGame();
    }
}

class Nanomuncher {
    String program;
    int programCounter;
    int position;// nodeid
    
    public Nanomuncher(String program, int position, int programCounter) {
        this.program = program;
        this.position = position;
        this.programCounter = programCounter;
    }
}
