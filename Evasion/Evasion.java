public class Evasion {
    
    private final static int[] N = {0, 1 };
    private final static int[] NE = {1, 1 };
    private final static int[] E = {1, 0 };
    private final static int[] SE = {1, -1 };
    private final static int[] S = {0, -1 };
    private final static int[] SW = {-1, -1 };
    private final static int[] W = {-1, 0 };
    private final static int[] NW = {-1, 1 };
    private final static double MIN_DIST = 4;
    private Hunter hunter;
    private Prey prey;
    int[][] board;
    int wallNum;
    boolean preyMovable;
    
    public Evasion() {
        this.hunter = new Hunter(this);
        this.prey = new Prey(this);
        board = new int[500][500];
        preyMovable = true;
        wallNum = 1;
    }
    
    private double getDist() {
        double dist = Math.sqrt(Math.pow(hunter.position[0] - prey.position[0], 2)
                                + Math.pow(hunter.position[1] - prey.position[1], 2));
        return dist;
    }
    
    public void start() {
        int count = 0;
        while (getDist() > MIN_DIST) {
            if (hunter.position[0] == 10)
                hunter.buildWall(false);
            hunter.move();
            if (preyMovable) {
                prey.move(W);
            } else {
                preyMovable = true;
            }
            count++;
        }
        System.out.println(count);
    }
    
    public static void main(String[] args) {
        Evasion game = new Evasion();
        game.start();
    }
}

class Hunter {
    // y
    // ^
    // |
    // ---> x
    /* direction[0] is horizontal, ranging from -1 to 1; direction[1] is vertical, and also from -1 to 1. */
    int[] direction;
    int[] position;
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
        if (y == -1 || y == 500) {// || ) {
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
    
    public void move(int[] direction) {
        int x = position[0] + direction[0];
        int y = position[1] + direction[1];
        int[][] board = game.board;
        game.preyMovable = false;
        if (x == -1 || x == 500) {
            if (y == -1 || y == 500)
                return;
            position[1] = y;
            return;
        }
        if (y == -1 || y == 500) {
            position[0] = x;
            return;
        }
        if (game.board[x][y] == 0) {
            position[0] = x;
            position[1] = y;
            return;
        }
        if (board[x][position[1]] != 0) {
            if (board[position[0]][y] == 0)
                position[1] = y;
        } else if (board[position[0]][y] != 0) {
            position[0] = x;
        } else {
            int xx = x + direction[0];
            int yy = y + direction[1];
            if (yy == -1 || yy == 500) {
                position[0] = x;
            } else if (xx == -1 || xx == 500 || board[x][yy] != 0) {
                position[1] = y;
            } else if (board[xx][y] != 0) {
                position[0] = x;
            }
        }
    }
    
    public Prey(Evasion game) {
        position = new int[2];
        position[0] = 330;
        position[1] = 200;
        this.game = game;
    }
}
