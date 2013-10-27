public class Evasion {
    
    private final static double MIN_DIST = 4;
    private Hunter hunter;
    private Prey prey;
    int[][] board;
    int wallTime;
    int maxNumWalls;
    int wallNum;
    boolean preyMovable;
    int minDuration = 10;
    
    public Evasion() {
        this.hunter = new Hunter(this);
        this.prey = new Prey(this);
        board = new int[500][500];
        preyMovable = false;
        wallTime = 30;
        maxNumWalls = 5;
        wallNum = 1;
    }
    
    private double getDist() {
        double dist = Math.sqrt(Math.pow(hunter.position[0] - prey.position[0], 2)
                                + Math.pow(hunter.position[1] - prey.position[1], 2));
        return dist;
    }
    
    public void start() {
        int count = 0;
        int duration = 0;
        while (getDist() > MIN_DIST) {
            HunterMove hunterMove = hunter.play(count, prey.position);
            if (hunterMove.buildWall) {
                System.out.println(String.format(
                                                 "Time %d: Build wall %d: (%3d, %3d) to (%3d, %3d), H(%3d, %3d), P(%3d, %3d)",
                                                 count, wallNum,
                                                 hunterMove.wall[0], hunterMove.wall[1], hunterMove.wall[2], hunterMove.wall[3],
                                                 hunter.position[0], hunter.position[1], prey.position[0], prey.position[1]));
                System.out.println(String.format("    new bounds: %3d by %3d: (%3d, %3d) to (%3d, %3d)",
                                                 hunter.bounds[2] - hunter.bounds[0], hunter.bounds[3] - hunter.bounds[1],
                                                 hunter.bounds[0], hunter.bounds[1], hunter.bounds[2], hunter.bounds[3]));
                
                prey.setBoundaries(hunter.bounds[0], hunter.bounds[2], hunter.bounds[1], hunter.bounds[3]);
                hunter.buildWall(hunterMove.wall[2] != hunterMove.wall[0]);
            }
            if (hunterMove.destroyWall > 0) {
                System.out.println(String.format("Time %d: Destroy wall %d", count, hunterMove.destroyWall));
                
                hunter.destroyWall(hunterMove.destroyWall);
            }
            
            if (preyMovable) {
                if (!prey.hasTarget()) {
                    prey.getTarget(hunter.position, hunter.direction);
                }
                prey.moveTowardsTarget(hunter.direction);
                System.out.println("position of prey: " + prey.position[0] + ", " + prey.position[1]);
                System.out.println("direction of prey: " + prey.direction[0] + ", " + prey.direction[1]);
            } else {
                preyMovable = true;
            }
            hunter.move();
            System.out.println("position of hunter: " + hunter.position[0] + ", " + hunter.position[1]);
            System.out.println("direction of hunter: " + hunter.direction[0] + ", " + hunter.direction[1]);
            duration++;
            count++;
            if (count % 10 == 0) {
                System.out.println();
            }
        }
        System.out.println(count);
    }
    
    public static void main(String[] args) {
        Evasion game = new Evasion();
        game.start();
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
    int wallTimer = 0;
    int wallCount = 0;
    int[] bounds = {0, 0, 499, 499 };
    int wallToDestroy = 0;
    Evasion game;
    
    public void move() {
        int[][] board = game.board;
        int x = position[0] + direction[0];
        int y = position[1] + direction[1];
        /* x is a wall */
        if (x == -1 || x == 500) {
            direction[0] = -direction[0];
            /* y is also a wall */
            if (y == -1 || y == 500) {
                direction[1] = -direction[1];
                return;
            }
            position[1] = y;
            return;
        }
        /* y is a wall */
        if (y == -1 || y == 500) {
            direction[1] = -direction[1];
            position[0] = x;
            return;
        }
        if (board[x][y] == 0) {
            position[0] = x;
            position[1] = y;
            return;
        }
        if (board[x][position[1]] != 0) {
            if (board[position[0]][y] != 0) {
                direction[0] = -direction[0];
                direction[1] = -direction[1];
            } else {
                position[1] = y;
                direction[0] = -direction[0];
            }
        } else if (board[position[0]][y] != 0) {
            position[0] = x;
            direction[1] = -direction[1];
        } else {
            int xx = x + direction[0];
            int yy = y + direction[1];
            if (yy == -1 || yy == 500) {
                position[0] = x;
                direction[1] = -direction[1];
            } else if (xx == -1 || xx == 500 || board[x][yy] != 0) {
                position[1] = y;
                direction[0] = -direction[0];
            } else if (board[xx][y] != 0) {
                position[0] = x;
                direction[1] = -direction[1];
            }
        }
        
    }
    
    public boolean buildWall(boolean horizontal) {
        int[][] board = game.board;
        int wallNum = game.wallNum;
        int x = position[0];
        int y = position[1];
        if (board[x][y] != 0)
            return false;
        board[x][y] = wallNum;
        if (horizontal) {
            int left = x - 1;
            int right = x + 1;
            while (left >= 0) {
                if (board[left][y] != 0)
                    break;
                board[left--][y] = wallNum;
            }
            while (right < 500) {
                if (board[right][y] != 0)
                    break;
                board[right++][y] = wallNum;
            }
        } else {
            int up = y + 1;
            int down = y - 1;
            while (up < 500) {
                if (board[x][up] != 0)
                    break;
                board[x][up++] = wallNum;
            }
            while (down >= 0) {
                if (board[x][down] != 0)
                    break;
                board[x][down--] = wallNum;
            }
        }
        game.wallNum++;
        return true;
    }
    
    public void destroyWall(int id) {
        int[][] board = game.board;
        for (int x = 0; x < 500; ++x)
            for (int y = 0; y < 500; ++y)
                if (board[x][y] == id)
                    board[x][y] = 0;
    }
    
    public HunterMove play(int count, int[] preyPosition) {
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
                    for (int i = 0; i < bounds[0]; ++i)
                        if (game.board[i][position[1]] > 0) {
                            wallToDestroy = game.board[i][position[1]];
                            break;
                        }
                } else {
                    for (int i = bounds[2] + 1; i < 500; ++i)
                        if (game.board[i][position[1]] > 0) {
                            wallToDestroy = game.board[i][position[1]];
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
                    for (int i = 0; i < bounds[1]; ++i)
                        if (game.board[position[0]][i] > 0) {
                            wallToDestroy = game.board[position[0]][i];
                            break;
                        }
                } else {
                    for (int i = bounds[3] + 1; i < 500; ++i)
                        if (game.board[position[0]][i] > 0) {
                            wallToDestroy = game.board[position[0]][i];
                            break;
                        }
                }
            }
            
            if (vWall || hWall) {
                buildWall = true;
                ++wallCount;
                wallTimer = game.wallTime;
            }
        }
        
        if (!buildWall && wallToDestroy > 0) {
            // See if there's any useless wall to destroy.
            // NB: IF the architecture is changed that we can build and destroy in a same
            // round, just remove "!buildWall &&".
            
            destroyWall = wallToDestroy;
            wallToDestroy = 0;
            --wallCount;
        }
        
        if (wallTimer > 0)
            --wallTimer;
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
    int[] boundaries;// minX, maxX, minY, maxY
    
    /*
     * public void move() { int x = position[0] + direction[0]; int y = position[1] + direction[1]; int[][] board =
     * game.board; game.preyMovable = false; if (x < boundaries[0] || x > boundaries[1]) { hasTarget = false; if (y <
     * boundaries[2] || y > boundaries[3]) { return; } position[1] = y; return; } if (y < boundaries[2] || y >
     * boundaries[3]) { position[0] = x; hasTarget = false; return; } if (game.board[x][y] == 0) { position[0] = x;
     * position[1] = y; return; } hasTarget = false; if (board[x][position[1]] != 0) { if (board[position[0]][y] == 0) {
     * position[1] = y; } } else if (board[position[0]][y] != 0) { position[0] = x; } else { int xx = x + direction[0];
     * int yy = y + direction[1]; if (yy < boundaries[2] || yy > boundaries[3]) { position[0] = x; } else if (xx <
     * boundaries[0] || xx > boundaries[1] || board[x][yy] != 0) { position[1] = y; } else if (board[xx][y] != 0) {
     * position[0] = x; } } }
     */
    
    public void move() {
        int x = position[0] + direction[0];
        int y = position[1] + direction[1];
        int[][] board = game.board;
        if (x >= boundaries[0] && x <= boundaries[1] && y >= boundaries[2] && y <= boundaries[3] && board[x][y] == 0) {
            position[0] = x;
            position[1] = y;
        } else {
            hasTarget = false;
            if (x < 0 || x > 499) {
                if (y < 0 || y > 499)
                    return;
                position[1] = y;
                return;
            }
            if (y < 0 || y > 499) {
                position[0] = x;
                return;
            }
            if (board[x][position[1]] != 0) {
                if (board[position[0]][y] == 0) {
                    position[1] = y;
                }
            } else if (board[position[0]][y] != 0) {
                position[0] = x;
            } else {
                int xx = x + direction[0];
                int yy = y + direction[1];
                if (yy < boundaries[2] || yy > boundaries[3]) {
                    position[0] = x;
                } else if (xx < boundaries[0] || xx > boundaries[1] || board[x][yy] != 0) {
                    position[1] = y;
                } else if (board[xx][y] != 0) {
                    position[0] = x;
                }
            }
        }
    }
    
    public void setBoundaries(int minX, int maxX, int minY, int maxY) {
        this.boundaries[0] = minX;
        this.boundaries[1] = maxX;
        this.boundaries[2] = minY;
        this.boundaries[3] = maxY;
        hasTarget = false;
    }
    
    /**
     * set direction before setting target
     */
    private boolean setTarget(int x, int y) {
        int[][] board = game.board;
        int xx = position[0] + direction[0];
        int yy = position[1] + direction[1];
        this.hasTarget = true;
        while (xx != x && xx >= boundaries[0] && xx <= boundaries[1] && yy >= boundaries[2] && yy <= boundaries[3]) {
            if (board[xx][yy] != 0) {
                hasTarget = false;
                target[0] = xx - direction[0];
                target[1] = yy - direction[1];
                return false;
            }
            xx += direction[0];
            yy += direction[1];
        }
        if (yy != y) {
            target[0] = position[0] + direction[0];
            target[1] = position[1] + direction[1];
            return false;
        }
        target[0] = x;
        target[1] = y;
        return true;
    }
    
    public void getTarget(int[] position, int[] direction) {
        if (gettingCloser(position, direction)) {
            int[] futurePosition =
            {this.position[0], position[1] + (this.position[0] - position[0]) * direction[1] / direction[0] };
            System.out.println("future position for hunter: " + futurePosition[0] + ", " + futurePosition[1]);
            int dist = Math.abs(Math.abs(futurePosition[1]) - Math.abs(this.position[1]));
            if (dist < 6) {
                if (Math.abs(futurePosition[1]) > Math.abs(this.position[1])) {
                    this.direction[0] = 0;
                    this.direction[1] = -direction[1];
                    if (!setTarget(this.position[0], this.position[1] + this.direction[1] * (6 - dist))) {
                        this.direction[0] = direction[0];
                        if (!setTarget(this.position[0] + this.direction[0] * (6 - dist), this.position[1]
                                       + this.direction[1] * (6 - dist))) {
                            this.direction[1] = 0;
                            if (!setTarget(this.position[0] + this.direction[0] * (6 - dist), this.position[1])) {
                                this.direction[0] = -direction[0];
                                this.direction[0] = direction[1];
                                if (!setTarget(this.position[0] + dist * this.direction[0], this.position[1]
                                               + dist * this.direction[1])) {
                                    this.direction[0] = 0;
                                    this.direction[1] = direction[1];
                                    if (!setTarget(this.position[0], this.position[1] + (dist + 6) * this.direction[1])) {
                                        this.direction[0] = -direction[0];
                                        this.direction[1] = 0;
                                        setTarget(this.position[0] + (dist + 6) * this.direction[0], this.position[1]);
                                    }
                                }
                            }
                        }
                    }
                } else {
                    this.direction[0] = -direction[0];
                    this.direction[1] = 0;
                    if (!setTarget(this.position[0] + this.direction[0] * (6 - dist), this.position[1])) {
                        this.direction[1] = direction[1];
                        if (!setTarget(this.position[0] + this.direction[0] * (6 - dist), this.position[1]
                                       + this.direction[1] * (6 - dist))) {
                            this.direction[0] = 0;
                            if (!setTarget(this.position[0], this.position[1] + this.direction[1] * (6 - dist))) {
                                this.direction[0] = direction[0];
                                this.direction[0] = -direction[1];
                                if (!setTarget(this.position[0] + dist * this.direction[0], this.position[1] + dist
                                               * this.direction[1])) {
                                    this.direction[0] = direction[0];
                                    this.direction[1] = 0;
                                    if (!setTarget(this.position[0] + (dist + 6) * this.direction[0], this.position[1])) {
                                        this.direction[0] = 0;
                                        this.direction[1] = -direction[1];
                                        setTarget(this.position[0], this.position[1] + (dist + 6) * this.direction[1]);
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                this.direction[0] = -direction[0];
                this.direction[1] = -direction[1];
                if (dist > 40) {
                    if (this.position[1] == boundaries[2] + 1 || this.position[1] == boundaries[3] - 1)
                        this.direction[1] = direction[1];
                    if (this.position[0] == boundaries[0] + 1 || this.position[0] == boundaries[1] - 1)
                        this.direction[0] = direction[0];
                }
                int maxDist = Math.abs(position[0] - this.position[0]) / 3;
                int tempDist = Math.abs(position[1] - this.position[1]) / 3;
                if (tempDist > maxDist)
                    maxDist = tempDist;
                int targetX = this.position[0] + maxDist * this.direction[0];
                int targetY = this.position[1] + maxDist * this.direction[1];
                setTarget(targetX, targetY);
            }
        } else {
            this.direction[0] = direction[0];
            this.direction[1] = direction[1];
            int xx = position[0];
            int yy = position[1];
            int[][] board = game.board;
            int dist = 0;
            while (xx >= boundaries[0] && xx <= boundaries[1] && yy >= boundaries[2] && yy <= boundaries[3]
                   && board[xx][yy] == 0) {
                xx += direction[0];
                yy += direction[1];
                dist++;
            }
            setTarget(this.position[0] + dist * this.direction[0], this.position[1] + this.direction[1] * dist);
            
        }
        System.out.println("target set: " + target[0] + ", " + target[1]);
        opponentDirection[0] = direction[0];
        opponentDirection[1] = direction[1];
    }
    
    private boolean gettingCloser(int[] position, int[] direction) {
        int x1 = position[0];
        int y1 = position[1];
        int x2 = this.position[0];
        int y2 = this.position[1];
        if ((x2 - x1) / direction[0] > 0)
            return true;
        if ((y2 - y1) / direction[1] > 0)
            return true;
        return false;
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
        game.preyMovable = false;
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
        boundaries = new int[4];
        boundaries[0] = 0;
        boundaries[1] = 499;
        boundaries[2] = 0;
        boundaries[3] = 499;
        this.game = game;
    }
}
