import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Evasion {
    
    public static final int[] NN = {0, -1 };
    public static final int[] SS = {0, 1 };
    public static final int[] EE = {1, 0 };
    public static final int[] WW = {-1, 0 };
    public static final int[] NE = {1, -1 };
    public static final int[] NW = {-1, -1 };
    public static final int[] SE = {1, 1 };
    public static final int[] SW = {-1, 1 };
    public static final int[] ZZ = {0, 0 };
    public static Socket socket;
    public static PrintWriter out;
    public static BufferedReader in;
    
    private final static double MIN_DIST = 4;
    private boolean isHunter;
    private Hunter hunter;
    private Prey prey;
    int wallTime;
    int maxNumWalls;
    int wallNum;
    int[][] walls;
    int wallCount;
    int wallTimer;
    int minDuration = 10;
    Map<Integer, WallPair> wallNums;
    Map<Integer, Integer[]> verticalWalls;
    Map<Integer, Integer[]> horizontalWalls;
    
    public Evasion(boolean isHunter, int wallTime, int maxNumWalls) {
        this.hunter = new Hunter(this);
        this.prey = new Prey(this);
        this.isHunter = isHunter;
        this.wallTime = wallTime;
        this.maxNumWalls = maxNumWalls;
        wallNum = 1;
        walls = new int[maxNumWalls][4];
        wallNums = new HashMap<Integer, WallPair>();
        verticalWalls = new HashMap<Integer, Integer[]>();
        horizontalWalls = new HashMap<Integer, Integer[]>();
    }
    
    private void parseSpec(String str) {
    System.out.println(str);
        String[] specs = str.split("\n");
        wallCount = Integer.parseInt(specs[1]);
        Set<Integer> existingWallNums = new HashSet<Integer>();
        for (int i = 0; i < wallCount; i++) {
            String[] wallSpecs = specs[i + 2].split(" ");
            int wallNum = Integer.parseInt(wallSpecs[0]);
            existingWallNums.add(wallNum);
            if (!wallNums.containsKey(wallNum)) {
                String[] coord = wallSpecs[1].split("[,()]");
                int[] wall =
                {Integer.parseInt(coord[1]), Integer.parseInt(coord[2]), Integer.parseInt(coord[5]),
                    Integer.parseInt(coord[6]) };
                if (wall[0] == wall[2]) {
                    verticalWalls.put(wall[0], new Integer[] {wall[1], wall[3] });
                    wallNums.put(wallNum, new WallPair(wall[0], verticalWalls));
                }
                else {
                    horizontalWalls.put(wall[1], new Integer[] {wall[0], wall[2] });
                    wallNums.put(wallNum, new WallPair(wall[1], horizontalWalls));
                }
                walls[wallNum-1] = wall;
            }
        }
        for (int wallNum : wallNums.keySet()) {
            if (!existingWallNums.contains(wallNum)) {
                WallPair pair = wallNums.get(wallNum);
                pair.map.remove(pair.val);
                wallNums.remove(wallNum);
            }
        }
        wallTimer = Integer.parseInt(specs[3 + wallCount]);
        String[] hunterSpecs = specs[4 + wallCount].split(" ");
        if (hunterSpecs[1].equalsIgnoreCase("NE")) {
            hunter.setDir(NE);
        } else if (hunterSpecs[1].equalsIgnoreCase("NW")) {
            hunter.setDir(NW);
        } else if (hunterSpecs[1].equalsIgnoreCase("SE")) {
            hunter.setDir(SE);
        } else if (hunterSpecs[1].equalsIgnoreCase("SW")) {
            hunter.setDir(SW);
        }
        String[] hunterPos = hunterSpecs[2].split("[,()]");
        hunter.setPos(Integer.parseInt(hunterPos[1]), Integer.parseInt(hunterPos[2]));
        String[] preySpecs = specs[5 + wallCount].split(" ");
        String[] preyPos = preySpecs[1].split("[,()]");
        prey.setPos(Integer.parseInt(preyPos[1]), Integer.parseInt(preyPos[2]));
    }
    
    private double getDist() {
        double dist = Math.sqrt(Math.pow(hunter.position[0] - prey.position[0], 2)
                                + Math.pow(hunter.position[1] - prey.position[1], 2));
        return dist;
    }
    
    public void start() throws IOException {
        int count = 0;
        while (getDist() > MIN_DIST) {
            parseSpec(read());
            if (isHunter) {
                HunterMove hunterMove = hunter.play(count, prey.position, wallTimer, wallCount, walls);
                if (hunterMove.buildWall) {
                    System.out.println(String.format(
                                                     "Time %d: Build wall %d: (%3d, %3d) to (%3d, %3d), H(%3d, %3d), P(%3d, %3d)",
                                                     count, wallNum,
                                                     hunterMove.wall[0], hunterMove.wall[1], hunterMove.wall[2], hunterMove.wall[3],
                                                     hunter.position[0], hunter.position[1], prey.position[0], prey.position[1]));
                    System.out.println(String.format("    new bounds: %3d by %3d: (%3d, %3d) to (%3d, %3d)",
                                                     hunter.bounds[2] - hunter.bounds[0], hunter.bounds[3] - hunter.bounds[1],
                                                     hunter.bounds[0], hunter.bounds[1], hunter.bounds[2], hunter.bounds[3]));
                }
                if (hunterMove.destroyWall > 0) {
                    System.out.println(String.format("Time %d: Destroy wall %d", count, hunterMove.destroyWall));
                }
                sendHunterMove(hunterMove);
                System.out.println("position of hunter: " + hunter.position[0] + ", " + hunter.position[1]);
                System.out.println("direction of hunter: " + hunter.direction[0] + ", " + hunter.direction[1]);
            } else {
                if (!prey.hasTarget()) {
                    prey.getTarget(hunter.position, hunter.direction);
                }
                prey.moveTowardsTarget(hunter.direction);
                sendPreyMove();
                System.out.println("position of prey: " + prey.position[0] + ", " + prey.position[1]);
                System.out.println("direction of prey: " + prey.direction[0] + ", " + prey.direction[1]);
            }
            count++;
        }
    }
    
    private void sendPreyMove() {
        String msg;
        int[] direction = prey.direction;
        if (direction[0] == -1) {
            if (direction[1] == -1)
                msg = "NW";
            else if (direction[1] == 0)
                msg = "WW";
            else
                msg = "SW";
        } else if (direction[0] == 0) {
            if (direction[1] == -1)
                msg = "NN";
            else if (direction[1] == 0)
                msg = "ZZ";
            else
                msg = "SS";
        } else {
            if (direction[1] == -1)
                msg = "NE";
            else if (direction[1] == 0)
                msg = "EE";
            else
                msg = "SE";
        }
        send(msg);
    }
    
    private void sendHunterMove(HunterMove hunterMove) {
        if (hunterMove.buildWall) {
            StringBuffer sb = new StringBuffer();
            int[] direction = hunterMove.direction;
            if (direction[0] == 1 && direction[1] == 1) {
                sb.append("SE");
            } else if (direction[0] == 1 && direction[1] == -1) {
                sb.append("NE");
            } else if (direction[0] == -1 && direction[1] == -1) {
                sb.append("NW");
            } else {
                sb.append("SW");
            }
            sb.append("w(");// hunterMove.wall[0], hunterMove.wall[1], hunterMove.wall[2], hunterMove.wall[3],\
            sb.append(hunterMove.wall[0] + "," + hunterMove.wall[1] + "),(" + hunterMove.wall[2] + ","
            + hunterMove.wall[3] + ")");
            send(sb.toString());
        } else if (hunterMove.destroyWall > 0) {
            StringBuffer sb = new StringBuffer();
            int[] direction = hunterMove.direction;
            if (direction[0] == 1 && direction[1] == 1) {
                sb.append("SE");
            } else if (direction[0] == 1 && direction[1] == -1) {
                sb.append("NE");
            } else if (direction[0] == -1 && direction[1] == -1) {
                sb.append("NW");
            } else {
                sb.append("SW");
            }
            sb.append("wx" + hunterMove.destroyWall);
            send(sb.toString());
        } else {
            StringBuffer sb = new StringBuffer();
            int[] direction = hunterMove.direction;
            if (direction[0] == 1 && direction[1] == 1) {
                sb.append("SE");
            } else if (direction[0] == 1 && direction[1] == -1) {
                sb.append("NE");
            } else if (direction[0] == -1 && direction[1] == -1) {
                sb.append("NW");
            } else {
                sb.append("SW");
            }
            send(sb.toString());
        }
        
    }
    
    public static String read() throws IOException {
        StringBuffer sb = new StringBuffer();
        String temp;
        temp = in.readLine();
        if (temp.equalsIgnoreCase("Walls")) {
            sb.append(temp + '\n');
            temp = in.readLine();
            sb.append(temp + '\n');
            int wallCount = Integer.parseInt(temp);
            for (int i = 0; i < wallCount; i++) {
                temp = in.readLine();
                sb.append(temp + '\n');
            }
            for (int i = 0; i < 5; i++) {
                temp = in.readLine();
                sb.append(temp + '\n');
            }
            return sb.toString();
        } else {
            return temp;
        }
    }
    
    public static void send(String str) {
        out.println(str);
    }
    
    public static void main(String[] args) throws UnknownHostException, IOException {
        if (args.length == 2) {
            String player = args[0];
            boolean isHunter = false;
            if (player.equalsIgnoreCase("H")) {
                isHunter = true;
            }
            int port = Integer.parseInt(args[1]);
            socket = new Socket("127.0.0.1", port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            String q = read();
            send("JJ");
            String wallsSpec = read();
            String[] timeAndNum = wallsSpec.split(" ");
            Evasion game = new Evasion(isHunter, Integer.parseInt(timeAndNum[0]), Integer.parseInt(timeAndNum[1]));
            game.start();
            
            in.close();
            out.close();
            socket.close();
        }
    }
}

class WallPair {
    int val;
    Map<Integer, Integer[]> map;
    
    public WallPair(int val, Map<Integer, Integer[]> map) {
        this.val = val;
        this.map = map;
    }
}

class HunterMove {
    int[] direction;
    boolean buildWall;
    int destroyWall; // id of the wall to destroy, 0 for not destroying any walls
    int[] wall; // {x1, y1, x2, y2} of the wall to build
    
    public HunterMove(int[] direction, boolean buildWall, int destroyWall, int[] wall) {
        this.direction = direction;
        this.buildWall = buildWall;
        this.destroyWall = destroyWall;
        this.wall = wall;
    }
}

class Hunter {
    static final int WALL_CONST = 2; // 2 should be minimum, experiment with larger value
    
    // y
    // ^
    // |
    // ---> x
    /* direction[0] is horizontal, ranging from -1 to 1; direction[1] is vertical, and also from -1 to 1. */
    int[] direction;
    int[] position;
    int[] bounds = {0, 0, 499, 499 };
    int wallToDestroy = 0;
    Evasion game;
    
    // TODO new function
    public void setPos(int x, int y) {
        this.position[0] = x;
        this.position[1] = y;
    }
    
    // TODO new function
    public void setDir(int[] dir) {
        this.direction[0] = dir[0];
        this.direction[1] = dir[1];
    }
    
    public HunterMove play(int count, int[] preyPosition, int wallTimer, int wallCount, int[][] walls) {
        boolean buildWall = false;
        int destroyWall = 0;
        int[] wall = null;
        
        if (wallTimer == 0 && wallCount < game.maxNumWalls) {
            // If we are allowed to build a wall
            
            // Turn these on, in case we want to build a wall
            boolean vWall = false;
            boolean hWall = false;
            
            int[] dist = new int[] {Math.abs(preyPosition[0] - position[0]), Math.abs(preyPosition[1] - position[1]) };
            int minDist = (dist[0] < dist[1]) ? dist[0] : dist[1];
            int safetyDist = 3 * game.wallTime + WALL_CONST;
            
            if (direction[0] == 1 && preyPosition[0] - position[0] > count % 2) {
                if (dist[0] <= WALL_CONST || minDist >= safetyDist && dist[1] == minDist)
                    vWall = true;
            } else if (direction[0] == -1 && position[0] - preyPosition[0] > count % 2) {
                if (dist[0] <= WALL_CONST || minDist >= safetyDist && dist[1] == minDist)
                    vWall = true;
            }
            
            if (direction[1] == 1 && preyPosition[1] - position[1] > count % 2) {
                if (dist[1] <= WALL_CONST || minDist >= safetyDist && dist[0] == minDist)
                    hWall = true;
            } else if (direction[1] == -1 && position[1] - preyPosition[1] > count % 2) {
                if (dist[1] <= WALL_CONST || minDist >= safetyDist && dist[0] == minDist)
                    hWall = true;
            }
            
            // Don't bother if our bounds are tight enough
            if (vWall && bounds[2] - bounds[0] <= 5)
                vWall = false;
            if (hWall && bounds[3] - bounds[1] <= 5)
                hWall = false;
            
            if (vWall) {
                buildWall = true;
                wall = new int[] {position[0], bounds[1], position[0], bounds[3] };
                if (direction[0] == 1)
                    bounds[0] = position[0] + 1;
                else
                    bounds[2] = position[0] - 1;
                
                // See if there's a wall that's out of bound and if there is,
                // put it to wallToDestroy
                if (direction[0] == 1) {
                    for (int i = 0; i < wallCount; ++i)
                        if (walls[i][0] < bounds[0] && walls[i][2] < bounds[0]) {
                            wallToDestroy = i+1;
                            break;
                        }
                } else {
                    for (int i = 0; i < wallCount; ++i)
                        if (walls[i][0] > bounds[2] && walls[i][2] > bounds[2]) {
                            wallToDestroy = i+1;
                            break;
                        }
                }
            } else if (hWall) {
                buildWall = true;
                wall = new int[] {bounds[0], position[1], bounds[2], position[1] };
                if (direction[1] == 1)
                    bounds[1] = position[1] + 1;
                else
                    bounds[3] = position[1] - 1;
                
                // See if there's a wall that's out of bound and if there is,
                // put it to wallToDestroy
                if (direction[1] == 1) {
                    for (int i = 0; i < wallCount; ++i)
                        if (walls[i][1] < bounds[1] && walls[i][3] < bounds[1]) {
                            wallToDestroy = i+1;
                            break;
                        }
                } else {
                    for (int i = 0; i < wallCount; ++i)
                        if (walls[i][1] > bounds[3] && walls[i][3] > bounds[3]) {
                            wallToDestroy = i+1;
                            break;
                        }
                }
            }
            
            if (vWall || hWall) {
                buildWall = true;
//                ++wallCount;
//                wallTimer = game.wallTime;
            }
        }
        
        if (!buildWall && wallToDestroy > 0) {
            // See if there's any useless wall to destroy.
            // NB: IF the architecture is changed that we can build and destroy in a same
            // round, just remove "!buildWall &&".
            
            destroyWall = wallToDestroy;
            wallToDestroy = 0;
//            --wallCount;
        }
        
//        if (wallTimer > 0)
//            --wallTimer;
        return new HunterMove(direction, buildWall, destroyWall, wall);
    }
    
    public Hunter(Evasion game) {
        direction = new int[2];
        direction[0] = 1;
        direction[1] = 1;
        position = new int[2];
        position[0] = 0;
        position[1] = 0;
        this.game = game;
    }
}

class Prey {
    int[] position;
    Evasion game;
    int[] target;
    boolean hasTarget;
    int[] direction;
    int[] opponentDirection;
    Map<Integer, Integer[]> verticalWalls;
    Map<Integer, Integer[]> horizontalWalls;
    
    public void setPos(int x, int y) {
        if (this.position[0] != x || this.position[1] != y) {
            System.out.println("prey position unequal");
            this.position[0] = x;
            this.position[1] = y;
        }
    }
    
    public void move() {
        int x = position[0] + direction[0];
        int y = position[1] + direction[1];
        if (x < 0 || x > 499) {
            hasTarget = false;
            return;
        }
        if (y < 0 || y > 499) {
            hasTarget = false;
            return;
        }
        if (verticalWalls.containsKey(x)) {
            Integer[] range = verticalWalls.get(x);
            if (y >= range[0] && y <= range[1]) {
                hasTarget = false;
            }
        }
        else if (horizontalWalls.containsKey(y)) {
            Integer[] range = horizontalWalls.get(y);
            if (x >= range[0] && x <= range[1]) {
                hasTarget = false;
            }
        }
    }
    
    /**
     * set direction before setting target
     */
    private boolean setTarget(int x, int y) {
        int xx = position[0] + direction[0];
        int yy = position[1] + direction[1];
        while (xx != x && xx >= 0 && xx <= 499 && yy >= 0 && yy <= 499) {
            if (verticalWalls.containsKey(xx)) {
                Integer[] range = verticalWalls.get(x);
                if (yy >= range[0] && yy <= range[1]) {
                    hasTarget = false;
                    return false;
                }
            } else if (horizontalWalls.containsKey(yy)) {
                Integer[] range = horizontalWalls.get(y);
                if (xx >= range[0] && xx <= range[1]) {
                    hasTarget = false;
                    return false;
                }
            }
            xx += direction[0];
            yy += direction[1];
        }
        if (yy != y) {
            hasTarget = false;
            return false;
        }
        if (x == position[0] && y == position[1]) {
            hasTarget = false;
            direction[0] = 0;
            direction[1] = 0;
            return false;
        }
        target[0] = x;
        target[1] = y;
        this.hasTarget = true;
        return true;
    }
    
    public void getTarget(int[] position, int[] direction) {
        int[] dist = {Math.abs(position[0] - this.position[0]), Math.abs(position[1] - this.position[1]) };
        if (Math.min(dist[0], dist[1]) < 12) {
            int[] hunterFuturePosition =
            {this.position[0], position[1] + (this.position[0] - position[0]) * direction[1] / direction[0] };
            System.out.println("future position for hunter: " + hunterFuturePosition[0] + ", "
                               + hunterFuturePosition[1]);
            int distY = Math.abs(Math.abs(hunterFuturePosition[1]) - Math.abs(this.position[1]));
            if (distY < 6) {
                if (Math.abs(hunterFuturePosition[1]) > Math.abs(this.position[1])) {
                    this.direction[0] = 0;
                    this.direction[1] = -direction[1];
                    if (!setTarget(this.position[0], this.position[1] + this.direction[1] * (6 - distY))) {
                        this.direction[0] = direction[0];
                        if (!setTarget(this.position[0] + this.direction[0] * (6 - distY), this.position[1]
                                       + this.direction[1] * (6 - distY))) {
                            this.direction[1] = 0;
                            if (!setTarget(this.position[0] + this.direction[0] * (6 - distY), this.position[1])) {
                                this.direction[0] = -direction[0];
                                this.direction[0] = direction[1];
                                if (!setTarget(this.position[0] + distY * this.direction[0], this.position[1]
                                               + distY * this.direction[1])) {
                                    this.direction[0] = 0;
                                    this.direction[1] = direction[1];
                                    if (!setTarget(this.position[0], this.position[1] + (distY + 6) * this.direction[1])) {
                                        this.direction[0] = -direction[0];
                                        this.direction[1] = 0;
                                        setTarget(this.position[0] + (distY + 6) * this.direction[0], this.position[1]);
                                    }
                                }
                            }
                        }
                    }
                } else {
                    this.direction[0] = -direction[0];
                    this.direction[1] = 0;
                    if (!setTarget(this.position[0] + this.direction[0] * (6 - distY), this.position[1])) {
                        this.direction[1] = direction[1];
                        if (!setTarget(this.position[0] + this.direction[0] * (6 - distY), this.position[1]
                                       + this.direction[1] * (6 - distY))) {
                            this.direction[0] = 0;
                            if (!setTarget(this.position[0], this.position[1] + this.direction[1] * (6 - distY))) {
                                this.direction[0] = direction[0];
                                this.direction[0] = -direction[1];
                                if (!setTarget(this.position[0] + distY * this.direction[0], this.position[1] + distY
                                               * this.direction[1])) {
                                    this.direction[0] = direction[0];
                                    this.direction[1] = 0;
                                    if (!setTarget(this.position[0] + (distY + 6) * this.direction[0], this.position[1])) {
                                        this.direction[0] = 0;
                                        this.direction[1] = -direction[1];
                                        setTarget(this.position[0], this.position[1] + (distY + 6) * this.direction[1]);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            if (isBehind(position, direction)) {
                this.direction[0] = direction[0];
                this.direction[1] = direction[1];
            } else {
                this.direction[0] = -direction[0];
                this.direction[1] = -direction[1];
            }
            int targetX = this.position[0] + this.direction[0];
            int targetY = this.position[1] + this.direction[1];
            setTarget(targetX, targetY);
        }
        System.out.println("target set: " + target[0] + ", " + target[1]);
        opponentDirection[0] = direction[0];
        opponentDirection[1] = direction[1];
    }
    
    private boolean isBehind(int[] position, int[] direction) {
        boolean xBehind = false;
        boolean yBehind = false;
        if (this.position[0] <= position[0] && direction[0] > 0) {
            xBehind = true;
        } else if (this.position[0] >= position[0] && direction[0] < 0) {
            xBehind = true;
        }
        if (this.position[1] <= position[1] && direction[1] > 0) {
            yBehind = true;
        } else if (this.position[1] >= position[1] && direction[1] < 0) {
            yBehind = true;
        }
        return xBehind && yBehind;
    }
    
    public void moveTowardsTarget(int[] direction) {
        if (opponentDirection[0] != direction[0] || opponentDirection[1] != direction[1]) {
            hasTarget = false;
            return;
        }
        move();
        if (position[0] == target[0] && position[1] == target[1]) {
            hasTarget = false;
        }
    }
    
    public boolean hasTarget() {
        return this.hasTarget;
    }
    
    public Prey(Evasion game) {
        position = new int[2];
        position[0] = 330;
        position[1] = 200;
        direction = new int[2];
        opponentDirection = new int[2];
        target = new int[2];
        this.game = game;
        this.verticalWalls = game.verticalWalls;
        this.horizontalWalls = game.horizontalWalls;
    }
}
