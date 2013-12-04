import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Voronoi {
    
    private static final String TEAM_NAME = "jj";
    private static final int PORT = 4567;
    private static final String EOM = "<EOM>";
    
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    public int numOfPlayers;
    public int stones;
    public int dimen;
    public int pid;
    public static Object lock = new Object();
    
    private Player[] players;
    Set<Integer> allMoves;
    double[][][] board;
    
    public Voronoi(int port) throws UnknownHostException, IOException {
        socket = new Socket("127.0.0.1", port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }
    
    public void close() throws IOException {
        in.close();
        out.close();
        socket.close();
    }
    
    private void send(String output) {
        out.println(output + EOM);
    }
    
    private String read() throws IOException {
        StringBuffer sb = new StringBuffer();
        String temp;
        while ((temp = in.readLine()) != null) {
            if (!temp.contains(EOM)) {
                sb.append(temp + " ");
            }
            else {
                sb.append(temp.substring(0, temp.indexOf(EOM)));
                break;
            }
        }
        System.out.println("!! " + sb.toString() + " !!");
        return sb.toString();
    }
    
    private Step parseStep(String input) {
        String[] steps = input.split(" ");
        String[] pidAndTime = steps[0].split(",");
        Step step = new Step(Integer.parseInt(pidAndTime[0]), Double.parseDouble(pidAndTime[1]));
        String[] moves = steps[1].split("[)(]");
        for (String move : moves) {
            if (!move.equals("") && !move.equals(",")) {
                String[] moveDetail = move.split(",");
                step.addMove(new Move(Integer.parseInt(moveDetail[0]), Integer.parseInt(moveDetail[1]),
                                      Integer.parseInt(moveDetail[2])));
            }
        }
        String[] areas = steps[2].split("[)(]");
        for (String area : areas) {
            if (!area.equals("") && !area.equals(",")) {
                String[] areaDetail =
                area.split(",");
                step.addArea(new Step.Area(Integer.parseInt(areaDetail[0]),
                                           Integer.parseInt(areaDetail[1])));
            }
        }
        
        return step;
    }
    
    public static void main(String[] args) throws UnknownHostException, IOException {
        int port;
        if (args.length > 0)
            port = Integer.parseInt(args[0]);
        else
            port = PORT;
        Voronoi voronoi = new Voronoi(port);
        String str = voronoi.read();
        if (str.equalsIgnoreCase("Team Name?")) {
            voronoi.send(TEAM_NAME);
        }
        str = voronoi.read();
        String[] params = str.split(","); // num of players, stones, N, pid
        voronoi.numOfPlayers = Integer.parseInt(params[0].trim());
        voronoi.stones = Integer.parseInt(params[1].trim());
        voronoi.dimen = Integer.parseInt(params[2].trim());
        voronoi.pid = Integer.parseInt(params[3].trim());
        
        voronoi.initGame();
        voronoi.play();
        voronoi.close();
    }
    
    private void play() throws IOException {
        for (int i = 0; i < stones; i++) {
            for (int j = 1; j <= numOfPlayers; j++) {
                Step next = parseStep(read());
                int pid = j - 2;
                if (pid < 0)
                    pid = numOfPlayers - 1;
                players[pid].addStep(next);
                if (next.pid == this.pid) {
                    Move move = players[j - 1].move();
                    send(move.toString());
                }
            }
        }
        
    }
    
    private void initGame() {
        players = new Player[numOfPlayers];
        for (int i = 1; i <= numOfPlayers; i++)
            players[i - 1] = new Player(this, i);
        allMoves = new HashSet<Integer>();
        board = new double[numOfPlayers][dimen][dimen];
    }
}

class Player {
    int pid;
    Voronoi game;
    Set<Integer> moves;
    List<Integer> history;
    Random random;
    
    public Player(Voronoi game, int pid) {
        this.game = game;
        this.pid = pid;
        random = new Random(System.currentTimeMillis());
        moves = new HashSet<Integer>();
        history = new ArrayList<Integer>();
    }
    
    private int getPosition(int x, int y) {
        return x * game.dimen + y;
    }
    
    private int getX(int position) {
        return position / game.dimen;
    }
    
    private int getY(int position) {
        return position % game.dimen;
    }
    
    private double getPull(int x1, int y1, int x2, int y2) {
        if (x1 == x2 && y1 == y2)
            return 1000;
        return 1. / ((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }
    
    private void addStone(int x, int y) {
        double[][] board = game.board[pid - 1];
        for (int i = 0; i < game.dimen; i++) {
            for (int j = 0; j < game.dimen; j++) {
                board[i][j] += getPull(i, j, x, y);
            }
        }
    }
    
    public void addStep(Step step) {
        System.out.println("my id is: " + this.pid);
        for (Move move : step.moves) {
            if (move.playerId == this.pid) {
                int position = getPosition(move.x, move.y);
                if (!moves.contains(position)) {
                    moves.add(position);
                    System.out.println("** new pos " + move.x + ", " + move.y + ", "
                                       + step.areas.get(pid - 1).toString()
                                       + " **");
                    addStone(move.x, move.y);
                }
                if (!game.allMoves.contains(position)) {
                    game.allMoves.add(position);
                }
            }
        }
    }
    
    public Move move() {
        return triangleMove();
        // return randomMove();
    }
    
    private Move triangleMove() {
        if (moves.size() % 3 == 0) {
            return randomMove();
        }
        else if (moves.size() % 3 == 1) {
            int lastMove = history.get(history.size() - 1);
            int lastX = getX(lastMove);
            int lastY = getY(lastMove);
            int maxScore = 0;
            int x = 0;
            int y = lastY;
            for (int i = 1; i < 100; i += 4) {
                int score = tryPosition(lastX + i, lastY);
                if (score > maxScore) {
                    x = lastX + i;
                    maxScore = score;
                }
                score = tryPosition(lastX - i, lastY);
                if (score > maxScore) {
                    x = lastX - i;
                    maxScore = score;
                }
            }
            history.add(getPosition(x, y));
            System.out.println("best score: " + maxScore);
            System.out.println(" x, y: " + x + ", " + y);
            return new Move(pid, x, y);
        } else {
            int[] lastTwoMoves = {history.get(history.size() - 1), history.get(history.size() - 2) };
            int x = (getX(lastTwoMoves[0]) + getX(lastTwoMoves[1])) / 2;
            int lastY = getY(lastTwoMoves[0]);
            int y = 0;
            int maxScore = 0;
            for (int i = 1; i < 100; i += 4) {
                int score = tryPosition(x, lastY + i);
                if (score > maxScore) {
                    y = lastY + i;
                    maxScore = score;
                }
                score = tryPosition(x, lastY - i);
                if (score > maxScore) {
                    y = lastY - i;
                    maxScore = score;
                }
            }
            System.out.println("best score: " + maxScore);
            System.out.println(" x, y: " + x + ", " + y);
            history.add(getPosition(x, y));
            return new Move(pid, x, y);
        }
    }
    
    private Move randomMove() {
        int maxScore = 0;
        int x = 0;
        int y = 0;
        for (int i = 0; i < 100; i++) {
            int xx = random.nextInt(game.dimen);
            int yy = random.nextInt(game.dimen);
            if (game.allMoves.contains(getPosition(xx, yy)))
                continue;
            int score = tryPosition(xx, yy);
            if (score > maxScore) {
                x = xx;
                y = yy;
                maxScore = score;
            }
        }
        if (x == 0 && y == 0) {
            while (game.allMoves.contains(getPosition(x, y))) {
                x = random.nextInt(game.dimen);
                y = random.nextInt(game.dimen);
            }
        }
        System.out.println("best score: " + maxScore);
        System.out.println(" x, y: " + x + ", " + y);
        history.add(getPosition(x, y));
        return new Move(pid, x, y);
    }
    
    private int tryPosition(int xx, int yy) {
        if (xx < 0 || xx > game.dimen)
            return 0;
        if (yy < 0 || yy > game.dimen)
            return 0;
        double[][][] board = game.board;
        int score = 0;
        for (int i = 0; i < game.dimen; i++) {
            for (int j = 0; j < game.dimen; j++) {
                double max = 0;
                int maxPid = 0;
                for (int pid = 1; pid <= game.numOfPlayers; pid++) {
                    if (pid == this.pid) {
                        double pull = board[pid - 1][i][j] + getPull(i, j, xx, yy);
                        if (pull > max) {
                            max = pull;
                            maxPid = pid;
                        }
                        continue;
                    }
                    if (board[pid - 1][i][j] > max) {
                        max = board[pid - 1][i][j];
                        maxPid = pid;
                    }
                }
                if (maxPid == this.pid)
                    score++;
            }
        }
        return score;
    }
}

class Step {
    public int pid;
    public double time;
    public List<Move> moves;
    public List<Area> areas;
    
    static class Area {
        public int pid;
        public int area;
        
        public Area(int pid, int area) {
            this.pid = pid;
            this.area = area;
        }
        
        @Override
        public String toString() {
            return "pid: " + pid + ", are: " + area;
        }
    }
    
    public Step(int pid, double time) {
        this.pid = pid;
        this.time = time;
        moves = new ArrayList<Move>();
        areas = new ArrayList<Area>();
    }
    
    public void addMove(Move move) {
        moves.add(move);
    }
    
    public void addArea(Area area) {
        areas.add(area);
    }
}

class Move {
    public int playerId;
    public int x;
    public int y;
    
    public Move(int playerId, int x, int y) {
        this.playerId = playerId;
        this.x = x;
        this.y = y;
    }
    
    @Override
    public String toString() {
        return "(" + playerId + "," + x + "," + y + ")";
    }
}
