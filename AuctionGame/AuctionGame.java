import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
    private int[] passedCounts;
    
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
        passedCounts[items.get(round).num]++;
        round++;
    }
    
    private int getWinningPosition(Player player, int currType) {
        Map<Integer, Integer> counts = player.counts;
        int minPosition = items.size();
        int minType = -1;
        for (Entry<Integer, Integer> entry : counts.entrySet()) {
            int type = entry.getKey();
            int position = positions.get(type).get(passedCounts[type] + winningNum - entry.getValue() - 1);
            if (position < minPosition) {
                System.out.println("  I'll win in position " + position + " with " + type + ", passedCount "
                                   + passedCounts[type]);
                minPosition = position;
                minType = type;
            }
        }
        if (minType == currType) {
            return 0;
        }
        return minPosition;
    }
    
    private int patientBid() {
        int type = items.get(round).num;
        System.out.println(" next item: " + type);
        if (isStillEmpty()) {
            int[] order = new int[itemTypes];
            System.out.println("orders: ");
            for (int i = 0; i < itemTypes; i++) {
                order[i] = positions.get(i).get(passedCounts[i] + winningNum - 1);
                System.out.print(" " + order[i]);
            }
            System.out.println();
            Arrays.sort(order);
            for (int i = 0; i < totalPlayers; i++) {
                if (items.get(order[i]).num == type) {
                    return 100 / winningNum;
                }
            }
            if (items.get(order[totalPlayers]).num == type) {
                return 1;
            }
        } else {
            int myWinningPosition = getWinningPosition(players.get(myId), type);
            if (myWinningPosition == 0) {
                return players.get(myId).budget / (winningNum - players.get(myId).counts.get(type));
            }
            int maxPrice = 0;
            for (Player player : players) {
                if (player.id != myId) {
                    if (player.counts.containsKey(type)) {
                        int count = player.counts.get(type);
                        int winningPosition = positions.get(type).get(passedCounts[type] + winningNum - count - 1);
                        if (winningPosition < myWinningPosition) {
                            System.out.println(player.id + " is gonna win before me! " + winningPosition);
                            int estimatePrice = player.budget / (winningNum - count);
                            System.out.println(" estimate price is: " + estimatePrice);
                            if (estimatePrice > maxPrice) {
                                maxPrice = estimatePrice;
                            }
                        }
                    }
                }
            }
            if (maxPrice != 0) {
                return maxPrice + 1;
            }
        }
        return 1;
    }
    
    private boolean isStillEmpty() {
        return players.get(myId).budget == 100;
    }
    
    private int randomBid() {
        return random.nextInt(players.get(myId).budget);
    }
    
    private void parseLastBid(String str) {
        Item lastItem = items.get(round);
        String[] details = str.split(" ");
        int winnerId = Integer.parseInt(details[0]);
        Player winner = players.get(winnerId);
        int bestBid = Integer.parseInt(details[1]);
        lastItem.setBid(bestBid);
        lastItem.setPlayer(winner);
        winner.addItem(lastItem);
        System.out.println("My budget is: " + players.get(myId).budget);
    }
    
    private void init(String str) {
        String[] specs = str.split(" ");
        myId = Integer.parseInt(specs[0]);
        System.out.println(" ** my id " + myId + " **");
        totalPlayers = Integer.parseInt(specs[1]);
        itemTypes = Integer.parseInt(specs[2]);
        winningNum = Integer.parseInt(specs[3]);
        passedCounts = new int[itemTypes];
        
        players = new ArrayList<Player>();
        for (int i = 0; i < totalPlayers; i++) {
            players.add(new Player(i, winningNum));
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
    int id;
    Map<Integer, Integer> counts;
    int budget;
    int winningNum;
    
    public Player(int id, int winningNum) {
        this.id = id;
        counts = new HashMap<Integer, Integer>();
        budget = 100;
        this.winningNum = winningNum;
    }
    
    public boolean isDangerous(int type) {
        if (!counts.containsKey(type)) {
            return false;
        }
        return counts.get(type) == winningNum - 1;
    }
    
    public void addItem(Item item) {
        int type = item.num;
        if (!counts.containsKey(type)) {
            counts.put(type, 0);
        }
        counts.put(type, counts.get(type) + 1);
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
