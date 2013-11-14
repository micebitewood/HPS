import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
    private List<Player> players;
    private int round;
    private Random random;
    
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
        int bid = randomBid();
        send(bid + "");
        parseLastBid(receive());
        round++;
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
        int budget = Integer.parseInt(details[2]);
        players.get(winnerId).addItem(lastItem);
    }
    
    private void init(String str) {
        String[] specs = str.split(" ");
        myId = Integer.parseInt(specs[0]);
        totalPlayers = Integer.parseInt(specs[1]);
        players = new ArrayList<Player>();
        for (int i = 0; i < totalPlayers; i++) {
            players.add(new Player());
        }
        itemTypes = Integer.parseInt(specs[2]);
        winningNum = Integer.parseInt(specs[3]);
        items = new ArrayList<Item>();
        for (int i = 4; i < specs.length; i++) {
            int num = Integer.parseInt(specs[i]);
            items.add(new Item(num));
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
    List<Item> items;
    int budget;
    
    public Player() {
        items = new ArrayList<Item>();
        budget = 100;
    }
    
    public void addItem(Item item) {
        items.add(item);
        budget -= item.bid;
    }
}

class Item {
    int bid;
    int num;
    
    public Item(int num) {
        this.num = num;
    }
    
    public void setBid(int bid) {
        this.bid = bid;
    }
}
