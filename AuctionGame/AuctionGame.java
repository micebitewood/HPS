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
        String identity = (args.length > 1) ? args[1] : "";
        Game game = new Game(port, identity);
        while (true) {
            game.bid(identity);
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
    private int[] maxBid; // Max bid amount for each item
    
    public Game(int port, String identity) throws UnknownHostException, IOException {
        
        client = new Socket("127.0.0.1", port);
        out = new PrintWriter(client.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        System.out.println("connected");
        receive();
        send("JJ" + identity);
        init(receive());
        random = new Random(System.currentTimeMillis());
    }
    
    public void bid(String identity) throws IOException {
        // int bid = randomBid();
        int bid = 0;
        System.out.println("********** round " + round + " **********");
        System.out.println("My ID is: " + myId);
        System.out.println("My budget is: " + players.get(myId).budget);
        if (identity.equals("r"))
            bid = randomBid();
        else if (identity.equals("p"))
            bid = patientBid();
        else if (identity.equals("c"))
            bid = cheapBid();
        else if (identity.equals("a"))
            bid = ambitiousBid();
        else
            bid = patientBid();
        send(bid + "");
        parseLastBid(receive());
        passedCounts[items.get(round).num]++;
        round++;
    }
    
    private int cheapBid() {
        // The goal of cheap bidder is collecting 3 (or so) types of items that he can get
        // as cheap as possible, so that he has as much money as possible at the endgame
        // when he wants to finish off the game or intercepts the opponent's final bid.
        final int maxNumTypes = 2; // Number of item types that we will be collecting
        int type = items.get(round).num;
        for (Entry<Integer, Integer> entry : players.get(myId).counts.entrySet()) {
            System.out.println("I have " + entry.getValue() + " of item " + entry.getKey());
        }
        System.out.println(" next item: " + type);
        for (Player player : players) {
            // See if anyone can finish the game and bid more than they can afford
            // If it's me, bid all I have
            if (player.isDangerous(type)) {
                if (player.id == myId) {
                    System.out.println("I'm about to win with item " + type);
                    return player.budget;
                } else {
                    // Consider letting someone else do it
                    System.out.println("Player " + player.id + " is about to win with item " + type
                                       + ", so I'm bidding");
                    return player.budget + 1;
                }
            }
        }
        if (players.get(myId).counts.keySet().size() < maxNumTypes) {
            // In case we are still sampling different types of items
            int[] rank = new int[itemTypes];
            for (int i = 0; i < itemTypes; ++i)
                for (int j = 0; j < itemTypes; ++j)
                    if (maxBid[i] > maxBid[j])
                        ++rank[i];
            // Bid only if no one has ever won this item with expensive bid
            if (rank[type] < maxNumTypes) {
                System.out.println("Item " + type + " seems cheap (" + maxBid[type] + "), so I'm bidding");
                // TODO john did this
                return maxBid[type] + random.nextInt(2) + 1;
                // return maxBid[type] + 1;
            } else {
                System.out.println("Item " + type + " seems expensive (" + maxBid[type] + "), so I'm not bidding");
            }
        } else {
            // In case we already have maxNumTypes types of items
            // If we are not collecting this type, let it go
            if (!players.get(myId).counts.containsKey(type))
                return 0;
            int[] rank = new int[itemTypes];
            for (int i = 0; i < itemTypes; ++i)
                for (int j = 0; j < itemTypes; ++j)
                    if (maxBid[i] > maxBid[j])
                        ++rank[i];
                    else if (maxBid[i] == maxBid[j] && i != j) {
                        int ci = players.get(myId).counts.containsKey(i) ? players.get(myId).counts.get(i) : 0;
                        int cj = players.get(myId).counts.containsKey(j) ? players.get(myId).counts.get(j) : 0;
                        if (ci < cj)
                            ++rank[i];
                    }
            // Still make sure that this is not something that someone is eagerly looking for
            if (rank[type] < maxNumTypes) {// || rank[type] < itemTypes-totalPlayers+1) {
                System.out.println("Item " + type + " is in my collections and is still cheap (" + maxBid[type]
                                   + "), so I'm bidding");
                return maxBid[type] + 1;
            } else {
                System.out.println("Item " + type + " is in my collections but is too expensive (" + maxBid[type]
                                   + "), so I'm not bidding");
            }
        }
        // We are not interested in this item
        return 0;
    }
    
    private int getWinningPosition(Player player, int currType) {
        Map<Integer, Integer> counts = player.counts;
        int minPosition = items.size();
        for (Entry<Integer, Integer> entry : counts.entrySet()) {
            int type = entry.getKey();
            System.out.println(" *** I have " + entry.getValue() + " of " + type + " ***");
            int position = positions.get(type).get(passedCounts[type] + winningNum - entry.getValue() - 1);
            if (position < minPosition) {
                System.out.println("  I'll win in position " + position + " with " + type);
                minPosition = position;
            }
        }
        return minPosition;
    }
    
    private int patientBid() {
        int type = items.get(round).num;
        System.out.println(" next item: " + type);
        if (isStillEmpty()) {
            int[] order = new int[itemTypes];
            System.out.println("the orders of No. " + winningNum + ": ");
            for (int i = 0; i < itemTypes; i++) {
                order[i] = positions.get(i).get(passedCounts[i] + winningNum - 1);
                System.out.print(" " + order[i]);
            }
            System.out.println();
            Arrays.sort(order);
            int[] secondOrder = new int[itemTypes];
            System.out.println("the orders of No. " + (winningNum + totalPlayers) + ": ");
            for (int i = 0; i < itemTypes; i++) {
                secondOrder[i] = positions.get(i).get(passedCounts[i] + winningNum + totalPlayers - 1);
                System.out.print(" " + secondOrder[i]);
            }
            System.out.println();
            Arrays.sort(secondOrder);
            for (int i = 0; i < 2; i++) {
                if (items.get(order[i]).num == type || items.get(secondOrder[i]).num == type) {
                    return 100 / winningNum;
                }
            }
            if (items.get(order[totalPlayers]).num == type) {
                return 1;
            }
        } else {
            Player myPlayer = players.get(myId);
            int myWinningPosition = getWinningPosition(myPlayer, type);
            int maxPrice = 0;
            int minWinningPosition = 0;
            for (Player player : players) {
                if (player.id != myId) {
                    if (player.counts.containsKey(type)) {
                        int count = player.counts.get(type);
                        System.out.println("!! Player " + player.id + " has " + count + " of " + type + " !!");
                        int winningPosition = positions.get(type).get(passedCounts[type] + winningNum - count - 1);
                        if (winningPosition < myWinningPosition) {
                            System.out.println(player.id + " is gonna win before me! " + winningPosition);
                            int estimatePrice = player.budget / Math.max(1, (winningNum - count)) / 2 + 1;
                            if (winningNum - count == 2)
                                estimatePrice = player.budget / (winningNum - count);
                            if (winningNum - count == 1)
                                estimatePrice = player.budget / (winningNum - count) + 1;
                            System.out.println(" estimate price is: " + estimatePrice);
                            if (estimatePrice > maxPrice) {
                                maxPrice = estimatePrice;
                            }
                            if (winningPosition < minWinningPosition) {
                                minWinningPosition = winningPosition;
                            }
                        }
                    }
                }
            }
            if (type == items.get(myWinningPosition).num && maxPrice == 0) {
                int average = myPlayer.budget / Math.max(1, (winningNum - myPlayer.counts.get(type)));
                if (myPlayer.counts.get(type) < winningNum - 1)
                    return random.nextInt(3) + 3;
                return average;
            }
            if (maxPrice != 0) {
                if (minWinningPosition <= round + 1) {
                    return Math.max(0, Math.min(maxPrice,
                                                myPlayer.budget - myPlayer.counts.get(items.get(myWinningPosition).num)));
                }
                if (players.get(myId).budget > 2 * (maxPrice + 1))
                    return maxPrice;
            }
        }
        if (players.get(myId).counts.containsKey(type) && players.get(myId).budget > 100 / winningNum + 15) {
            return 1;
        }
        // if (winningNum < 5)
        if (players.get(myId).budget > 30)
            return players.get(myId).budget > 0 ? 1 : 0;
        return 0;
        // else
        // return 0;
    }
    
    private int ambitiousBid() {
        int type = items.get(round).num;
        System.out.println(" next item: " + type);
        if (isStillEmpty()) {
            int[] order = new int[itemTypes];
            System.out.println("the orders of No. " + winningNum + ": ");
            for (int i = 0; i < itemTypes; i++) {
                order[i] = positions.get(i).get(passedCounts[i] + winningNum - 1);
                System.out.print(" " + order[i]);
            }
            System.out.println();
            Arrays.sort(order);
            int[] secondOrder = new int[itemTypes];
            System.out.println("the orders of No. " + (winningNum + totalPlayers) + ": ");
            for (int i = 0; i < itemTypes; i++) {
                secondOrder[i] = positions.get(i).get(passedCounts[i] + winningNum + totalPlayers - 1);
                System.out.print(" " + secondOrder[i]);
            }
            System.out.println();
            Arrays.sort(secondOrder);
            if (items.get(order[0]).num == type || items.get(secondOrder[0]).num == type) {
                return 100 / winningNum;
            }
        } else {
            Player myPlayer = players.get(myId);
            int myWinningPosition = getWinningPosition(myPlayer, type);
            if (type == items.get(myWinningPosition).num) {
                int average = myPlayer.budget / Math.max(1, (winningNum - myPlayer.counts.get(type)));
                return average;
            }
            int maxPrice = 0;
            int minWinningPosition = 0;
            for (Player player : players) {
                if (player.id != myId) {
                    if (player.counts.containsKey(type)) {
                        int count = player.counts.get(type);
                        System.out.println("!! Player " + player.id + " has " + count + " of " + type + " !!");
                        int winningPosition = positions.get(type).get(passedCounts[type] + winningNum - count - 1);
                        if (winningPosition < myWinningPosition) {
                            System.out.println(player.id + " is gonna win before me! " + winningPosition);
                            int estimatePrice = player.budget / Math.max(1, (winningNum - count)) / 2 + 1;
                            if (winningNum - count == 2)
                                estimatePrice = player.budget / (winningNum - count);
                            if (winningNum - count == 1)
                                estimatePrice = player.budget / (winningNum - count) + 1;
                            System.out.println(" estimate price is: " + estimatePrice);
                            if (estimatePrice > maxPrice) {
                                maxPrice = estimatePrice;
                            }
                            if (winningPosition < minWinningPosition) {
                                minWinningPosition = winningPosition;
                            }
                        }
                    }
                }
            }
            if (maxPrice != 0) {
                if (minWinningPosition <= round + 1) {
                    return Math.max(0, Math.min(maxPrice,
                                                myPlayer.budget - myPlayer.counts.get(items.get(myWinningPosition).num)));
                }
                if (players.get(myId).budget > 2 * (maxPrice + 1))
                    return maxPrice;
            }
        }
        return 0;
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
    }
    
    private void init(String str) {
        String[] specs = str.split(" ");
        myId = Integer.parseInt(specs[0]);
        System.out.println(" ** my id " + myId + " **");
        totalPlayers = Integer.parseInt(specs[1]);
        itemTypes = Integer.parseInt(specs[2]);
        winningNum = Integer.parseInt(specs[3]);
        passedCounts = new int[itemTypes];
        maxBid = new int[itemTypes]; // Cheap bid
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
            sb.append(cbuf[0]);
            if (sb.toString().contains("<EOM>")) {
                break;
            }
        }
        String res = sb.toString().substring(0, sb.length() - 5);
        System.out.println(res + '\n');
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
