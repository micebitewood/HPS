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
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

public class AuctionGame {
    public static void main(String[] args) throws UnknownHostException, IOException {
        int port = Integer.parseInt(args[0]);
        Game game = new Game(port);
        while (true) {
            game.bid();
        }
    }
}

class Game {
    Socket client;
    PrintWriter out;
    BufferedReader in;
    
    private int myId;
    private int totalPlayers;
    private int itemTypes;
    private int winningNum;
    private List<Item> items;
    private List<List<Integer>> positions;
    private List<Player> players;
    private int round;
    private Random random;
    private Set<Integer> possibleTypes;
    
    public Game(int port) throws UnknownHostException, IOException {
        
        client = new Socket("127.0.0.1", port);
        out = new PrintWriter(client.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        System.out.println("connected");
        receive();
        send("JJ");
        init(receive());
        random = new Random(System.currentTimeMillis());
    }
    
    public void bid() throws IOException {
        // int bid = randomBid();
        int bid = patientBid();
        send(bid + "");
        parseLastBid(receive());
        round++;
    }
    
    private int patientBid() {
        int type = items.get(round).num;
        if (possibleTypes.contains(type)) {
            Map<Player, Integer> count = new HashMap<Player, Integer>();
            for (int i = 0; i < round; i++) {
                Item item = items.get(i);
                if (item.num == type) {
                    Player player = item.winner;
                    if (!count.containsKey(player)) {
                        count.put(player, 0);
                    }
                    count.put(player, count.get(player) + 1);
                }
            }
            int maxBudget = 0;
            for (Entry<Player, Integer> entry : count.entrySet()) {
                if (entry.getValue() == winningNum - 1) {
                    if (entry.getKey().budget > maxBudget) {
                        maxBudget = entry.getKey().budget;
                    }
                }
            }
            if (maxBudget != 0)
                return maxBudget + 1;
            return 100 / winningNum;
        }
        return 0;
    }
    
    private int randomBid() {
        return random.nextInt(players.get(myId).budget);
    }
    
    private void parseLastBid(String str) {
        Item lastItem = items.get(round);
        String[] details = str.split(" ");
        int winnerId = Integer.parseInt(details[0]);
        int bestBid = Integer.parseInt(details[1]);
        lastItem.setBid(bestBid);
        lastItem.setPlayer(players.get(winnerId));
        players.get(winnerId).addItem(lastItem);
    }
    
    private void init(String str) {
        String[] specs = str.split(" ");
        myId = Integer.parseInt(specs[0]);
        totalPlayers = Integer.parseInt(specs[1]);
        itemTypes = Integer.parseInt(specs[2]);
        winningNum = Integer.parseInt(specs[3]);
        players = new ArrayList<Player>();
        for (int i = 0; i < totalPlayers; i++) {
            players.add(new Player(winningNum));
        }
        items = new ArrayList<Item>();
        positions = new ArrayList<List<Integer>>();
        for (int i = 0; i < itemTypes; i++) {
            positions.add(new ArrayList<Integer>());
        }
        for (int i = 4; i < specs.length; i++) {
            int num = Integer.parseInt(specs[i]);
            items.add(new Item(num));
            positions.get(num).add(i - 4);
        }
        int[] winningCounts = firstAnalyze();
        possibleTypes = new HashSet<Integer>();
        for (int i = 0; i < winningCounts.length; i++) {
            int winningCount = winningCounts[i];
            if (winningCount >= winningNum) {
                possibleTypes.add(i);
            }
        }
    }
    
    private int[] firstAnalyze() {
        int[] counts = new int[itemTypes];
        int winningCount = 0;
        for (Item item : items) {
            counts[item.num]++;
            if (counts[item.num] == winningNum) {
                winningCount++;
                if (winningCount == Math.min(totalPlayers, itemTypes)) {
                    return counts;
                }
            }
        }
        return counts;
    }
    
    private String receive() throws IOException {
        StringBuilder sb = new StringBuilder();
        char[] cbuf = new char[1];
        System.out.print("receive: ");
        while (true) {
            in.read(cbuf);
            System.out.print(cbuf[0]);
            sb.append(cbuf[0]);
            if (sb.toString().contains("<EOM>")) {
                break;
            }
        }
        String res = sb.toString().substring(0, sb.length() - 5);
        System.out.println();
        return res;
    }
    
    private void send(String str) {
        System.out.println("send: " + str);
        out.println(str);
    }
}

class Player {
    List<Item> items;
    int budget;
    int winningNum;
    
    public Player(int winningNum) {
        items = new ArrayList<Item>();
        budget = 100;
        this.winningNum = winningNum;
    }
    
    public boolean isDangerous() {
        if (items.size() < winningNum - 1)
            return false;
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        for (Item item : items) {
            if (!map.containsKey(item.num)) {
                map.put(item.num, 0);
            }
            map.put(item.num, map.get(item.num) + 1);
            if (map.get(item.num) == winningNum - 1)
                return true;
        }
        return false;
    }
    
    public void addItem(Item item) {
        items.add(item);
        budget -= item.bid;
    }
}

class Item {
    int bid;
    int num;
    Player winner;
    
    public Item(int num) {
        this.num = num;
    }
    
    public void setBid(int bid) {
        this.bid = bid;
    }
    
    public void setPlayer(Player player) {
        this.winner = player;
    }
}
