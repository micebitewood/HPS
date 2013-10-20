import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Voronoi {
    private static final int LENGTH = 1000;
    private static final int STONES = 15;
    private static final int THREAD_COUNT = 1;

    public static void main(String[] args) {
        // NB: Making 2 games just for test. We will need only one game
        Game game1 = new Game(LENGTH, STONES);
        Game game2 = new Game(LENGTH, STONES);
        List<Move> moves = new ArrayList<Move>();
        
        // NB: Test
        for (int i = 0; i < STONES; ++i) {
            Move move = game1.play(1, moves, 120.0);
            System.out.println("Player 1: (" + move.x + ", " + move.y + ")");
            moves.add(move);
            
            move = game2.play(2, moves, 120.0);
            System.out.println("Player 2: (" + move.x + ", " + move.y + ")");
            moves.add(move);
        }
    }
}

class BestPosition {
    public int x;
    public int y;
    public int[] score;

    public BestPosition(int x, int y, int[] score) {
        this.x = x;
        this.y = y;
        this.score = score;
    }
}

class Move {
    public int player;
    public int x;
    public int y;
    
    public Move(int player, int x, int y) {
        this.player = player;
        this.x = x;
        this.y = y;
    }
}

class ClosestMultiRun implements Callable<BestPosition> {
    double[][] scores;
    int[][] board;
    int x;
    int y;
    int length;
    boolean red;

    private int[] getScore() {
        int[] score = new int[2];
        for (int i = 0; i < length; i++)
            for (int j = 0; j < length; j++) {
                double pull = scores[i][j];
                if (pull > 0)
                    score[0] += 1;
                else if (pull < 0)
                    score[1] += 1;
            }
        return score;
    }

    private BestPosition compare(List<BestPosition> same) {
        BestPosition bestPosition = same.get(0);
        Random random = new Random(System.currentTimeMillis());
        int lastScore = Integer.MAX_VALUE;
        for (BestPosition bestPos : same) {
            addStone(bestPos.x, bestPos.y);
            int[] score1 = getScore();
            red = !red;
            // TODO
            int maxScore = 0;
            for (int i = -1; i < 2; i++) {
                if (bestPos.x + i >= 0 && bestPos.x + i < length) {
                    for (int j = -1; j < 2; j++) {
                        if (i == 0 && j == 0)
                            continue;
                        if (bestPos.y + j >= 0 && bestPos.y + j < length && board[bestPos.x + i][bestPos.y + j] == 0) {
                            int[] score = getScore(bestPos.x + i, bestPos.y + j);
                            if (red) {
                                if (score[0] >= maxScore) {
                                    maxScore = score[0];
                                }
                            } else {
                                if (score[1] >= maxScore) {
                                    maxScore = score[1];
                                }
                            }
                        }
                    }
                }
            }
            // TODO
            for (int i = 0; i < 10; i++) {
                boolean isValid = false;
                while (!isValid) {
                    int x = random.nextInt(length);
                    int y = random.nextInt(length);
                    if (board[x][y] == 0) {
                        isValid = true;
                        int[] score = getScore(x, y);
                        if (red && score[0] > maxScore) {
                            maxScore = score[0];
                        } else if (!red && score[1] > maxScore) {
                            maxScore = score[1];
                        }
                    }
                }
            }
            if (maxScore < lastScore) {
                bestPosition = bestPos;
                lastScore = maxScore;
            }
            red = !red;
            removeStone(bestPos.x, bestPos.y);
        }
        return bestPosition;
    }

    public ClosestMultiRun(int[][] board, double[][] scores, int x, int y, boolean red) {
        length = scores.length;
        this.scores = new double[length][];
        for (int i = 0; i < length; i++) {
            this.scores[i] = Arrays.copyOf(scores[i], length);
        }
        this.x = x;
        this.y = y;
        this.red = red;
        this.board = board;
    }

    private void addStone(int x, int y) {
        double flag;
        if (red)
            flag = 1;
        else
            flag = -1;
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < length; j++) {
                if (x == i && y == j)
                    scores[i][j] += flag * 1000000;
                else
                    scores[i][j] += flag / ((x - i) * (x - i) + (y - j) * (y - j));
            }
        }
    }

    private void removeStone(int x, int y) {
        double flag;
        if (red)
            flag = 1;
        else
            flag = -1;
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < length; j++) {
                if (x == i && y == j)
                    scores[i][j] -= flag * 1000000;
                else
                    scores[i][j] -= flag / ((x - i) * (x - i) + (y - j) * (y - j));
            }
        }
    }

    private int[] getScore(int x, int y) {
        int[] score = new int[2];
        double flag;
        if (red)
            flag = 1;
        else
            flag = -1;
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < length; j++) {
                double pull;
                if (x == i && y == j)
                    pull = 1000000 * flag;
                else
                    pull = flag / ((x - i) * (x - i) + (y - j) * (y - j));
                if (scores[i][j] + pull > 0) {
                    score[0]++;
                } else if (scores[i][j] + pull < 0) {
                    score[1]++;
                }
            }
        }
        return score;
    }

    @Override
    public BestPosition call() throws Exception {
        int[] maxScore = new int[2];
        if (red) {
            maxScore[0] = 0;
            maxScore[1] = Integer.MAX_VALUE;
        } else {
            maxScore[0] = Integer.MAX_VALUE;
            maxScore[1] = 0;
        }
        BestPosition bestPosition = new BestPosition(x, y, maxScore);
        List<BestPosition> same = new ArrayList<BestPosition>();
        for (int i = -1; i < 2; i++) {
            if (x + i >= 0 && x + i < length) {
                for (int j = -1; j < 2; j++) {
                    if (i == 0 && j == 0)
                        continue;
                    if (y + j >= 0 && y + j < length && board[x + i][y + j] == 0) {
                        int[] score = getScore(x + i, y + j);
                        if (red) {
                            if (score[0] >= maxScore[0] && score[1] <= maxScore[1]) {
                                BestPosition position = new BestPosition(x + i, y + j, score);
                                if (score[0] == maxScore[0] && score[1] == maxScore[1])
                                    same.add(position);
                                else {
                                    maxScore = score;
                                    bestPosition = position;
                                    same.clear();
                                    same.add(position);
                                }
                            }
                        } else {
                            if (score[0] <= maxScore[0] && score[1] >= maxScore[1]) {
                                BestPosition position = new BestPosition(x + i, y + j, score);
                                if (score[0] == maxScore[0] && score[1] == maxScore[1])
                                    same.add(position);
                                else {
                                    maxScore = score;
                                    bestPosition = position;
                                    same.clear();
                                    same.add(position);
                                }
                            }
                        }
                    }
                }
            }
        }
        if (same.size() > 1)
            bestPosition = compare(same);
        return bestPosition;
    }
}

class Game {
    private static Random random;
    private int length;
    private int stones;
    private int count;
    private double[][] scores;
    int[][] board;
    Set<Integer> red;
    Set<Integer> blue;
    List<Move> moves;
    int numMoves;

    public Game(int length, int stones) {
        this.length = length;
        this.stones = stones;
        random = new Random(System.currentTimeMillis());
        count = 0;
        scores = new double[length][length];
        board = new int[length][length];
        red = new HashSet<Integer>();
        blue = new HashSet<Integer>();
        moves = new ArrayList<Move>();
        numMoves = 0;
    }

    private Move randomMove(boolean isRed) {
        boolean isValid = false;
        while (!isValid) {
            int x = random.nextInt(length);
            int y = random.nextInt(length);
            if (board[x][y] == 0) {
                isValid = true;
                board[x][y] = 1;
                if (isRed)
                    red.add(x * length + y);
                else
                    blue.add(x * length + y);
                addStone(x, y, isRed);
                return new Move(isRed?1:2, x, y);
            }
        }
        
        // We shouldn't get here
        return new Move(0, 0, 0);
    }

    private Move balance(boolean isRed) {
        int minDist = 0;
        int[] bestPos = {0, 0 };
        for (int i = 0; i < 100; i++) {
            boolean isValid = false;
            while (!isValid) {
                int x = random.nextInt(length);
                int y = random.nextInt(length);
                if (board[x][y] == 0) {
                    isValid = true;
                    int minMinDist = Integer.MAX_VALUE;
                    Set<Integer> color;
                    if (isRed)
                        color = red;
                    else
                        color = blue;
                    for (int pos : color) {
                        int xx = pos / length;
                        int yy = pos % length;
                        int dist = (xx - x) * (xx - x) + (yy - y) * (yy - y);
                        if (dist < minMinDist)
                            minMinDist = dist;
                    }
                    int[] dist =
                    {
                            x * x,
                            y * y,
                            (length - x - 1) * (length - x - 1),
                            (length - y - 1) * (length - y - 1)
                    };
                    for (int d : dist) {
                        if (d < minMinDist)
                            minMinDist = d;
                    }
                    if (minMinDist > minDist) {
                        minDist = minMinDist;
                        bestPos[0] = x;
                        bestPos[1] = y;
                    }
                }
            }
        }
        board[bestPos[0]][bestPos[1]] = 1;
        addStone(bestPos[0], bestPos[1], isRed);
        if (isRed)
            red.add(bestPos[0] * length + bestPos[1]);
        else
            blue.add(bestPos[0] * length + bestPos[1]);
        
        return new Move(isRed?1:2, bestPos[0], bestPos[1]);
    }

    private Move closest(boolean isRed) {
        Set<Integer> color1;
        Set<Integer> color2;
        if (isRed) {
            if (red.size() == 0) {
                red.add(length / 2 * length + length / 2 - 1);
                board[length / 2][length / 2 - 1] = 1;
                addStone(length / 2, length / 2 - 1, true);
                return new Move(isRed?1:2, length / 2, length / 2 - 1);
            }
            color1 = red;
            color2 = blue;
        } else {
            color1 = blue;
            color2 = red;
        }

        try {
            List<Callable<BestPosition>> lst = new ArrayList<Callable<BestPosition>>();
            for (int stone : color2) {
                int x = stone / length;
                int y = stone % length;
                lst.add(new ClosestMultiRun(board, scores, x, y, isRed));
            }
            ExecutorService executorService = Executors.newFixedThreadPool(lst.size());
            List<Future<BestPosition>> tasks = executorService.invokeAll(lst);
            executorService.shutdown();

            int[] maxScore = new int[2];
            if (isRed) {
                maxScore[0] = 0;
                maxScore[1] = Integer.MAX_VALUE;
            } else {
                maxScore[0] = Integer.MAX_VALUE;
                maxScore[1] = 0;
            }
            BestPosition bestPosition = new BestPosition(0, 0, maxScore);

            List<BestPosition> same = new ArrayList<BestPosition>();
            for (Future<BestPosition> task : tasks) {
                BestPosition position = task.get();
                int[] score = position.score;
                if (isRed) {
                    if (score[0] >= maxScore[0] && score[1] <= maxScore[1]) {
                        if (score[0] == maxScore[0] && score[1] == maxScore[1])
                            same.add(position);
                        else {
                            maxScore = score;
                            bestPosition = position;
                            same.clear();
                            same.add(bestPosition);
                        }
                    }
                } else {
                    if (score[0] <= maxScore[0] && score[1] >= maxScore[1]) {
                        if (score[0] == maxScore[0] && score[1] == maxScore[1])
                            same.add(position);
                        else {
                            maxScore = score;
                            bestPosition = position;
                            same.clear();
                            same.add(position);
                        }
                    }
                }
            }
            if (same.size() > 1)
                bestPosition = compare(board, same, isRed);
            color1.add(bestPosition.x * length + bestPosition.y);
            addStone(bestPosition.x, bestPosition.y, isRed);
            board[bestPosition.x][bestPosition.y] = 1;
            
            return new Move(isRed?1:2, bestPosition.x, bestPosition.y);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        
        // We shouldn't get here
        return new Move(0, 0, 0);
    }

    private BestPosition compare(int[][] board, List<BestPosition> same, boolean isRed) {
        BestPosition bestPosition = same.get(0);
        int lastScore = 0;
        for (BestPosition bestPos : same) {
            board[bestPos.x][bestPos.y] = 1;
            addStone(bestPos.x, bestPos.y, isRed);
            int minScore = Integer.MAX_VALUE;
            for (int i = 0; i < 100; i++) {
                boolean isValid = false;
                while (!isValid) {
                    int x = random.nextInt(length);
                    int y = random.nextInt(length);
                    if (board[x][y] == 0) {
                        isValid = true;
                        int[] score = getScore(x, y, !isRed);
                        if (isRed && score[0] < minScore) {
                            minScore = score[0];
                        } else if (!isRed && score[1] < minScore) {
                            minScore = score[1];
                        }
                    }
                }
            }
            if (isRed && minScore > lastScore) {
                bestPosition = bestPos;
                lastScore = minScore;
            }
            else if (!isRed && minScore > lastScore) {
                bestPosition = bestPos;
                lastScore = minScore;
            }
            addStone(bestPos.x, bestPos.y, !isRed);
            board[bestPos.x][bestPos.y] = 0;
        }
        return bestPosition;
    }

    private BestPosition sampleSearch(boolean isRed) {
        int[] maxScore = new int[2];
        if (isRed) {
            maxScore[0] = 0;
            maxScore[1] = Integer.MAX_VALUE;
        } else {
            maxScore[0] = Integer.MAX_VALUE;
            maxScore[1] = 0;
        }
        int bestX = -1;
        int bestY = -1;
        
        for (int i = 0; i < 100; ++i) {
            int x = random.nextInt(length);
            int y = random.nextInt(length);
            if (board[x][y] != 0) continue;
            
            int[] score = getScore(x, y, isRed);
            if (isRed) {
                if (score[0]-score[1] > maxScore[0]-maxScore[1]) {
                    maxScore = score;
                    bestX = x;
                    bestY = y;
                }
            } else {
                if (score[1]-score[0] > maxScore[1]-maxScore[0]) {
                    maxScore = score;
                    bestX = x;
                    bestY = y;
                }
            }
        }
        
//        System.out.println("Random sampling: " + maxScore[0] + " " + maxScore[1]);
        
//         int[] maxScore1 = maxScore;
//         
//         for (int i = -9; i < 10; ++i) {
//             int x = bestX + i;
//             if (x < 0 || x >= length) continue;
//             for (int j = -9; j < 10; ++j) {
//                 int y = bestY + j;
//                 if (y < 0 || y >= length) continue;
//                 if (board[x][y] != 0) continue;
//                 
//                 int[] score = getScore(x, y, isRed);
//                 if (isRed) {
//                     if (score[0]-score[1] > maxScore1[0]-maxScore1[1]) {
//                         maxScore1 = score;
//                     }
//                 } else {
//                     if (score[1]-score[0] > maxScore1[1]-maxScore1[0]) {
//                         maxScore1 = score;
//                     }
//                 }
//             }
//         }
//         
//         System.out.println("Region search: " + maxScore1[0] + " " + maxScore1[1]);
        
        int[] curScore = maxScore;
        int curX = bestX;
        int curY = bestY;
        for (double t = 0.01; t > 0.003; t *= 0.99) {
        	int x = curX + random.nextInt(39) - 19;
        	int y = curY + random.nextInt(39) - 19;
        	x = (x < 0) ? 0 : (x >= length) ? length-1 : x;
        	y = (y < 0) ? 0 : (y >= length) ? length-1 : y;
        	if (board[x][y] != 0) continue;
        	
        	int[] score = getScore(x, y, isRed);
        	if (isRed) {
        	    if (Math.exp((double)(score[0]-score[1]-curScore[0]+curScore[1])/length/length/t) > random.nextDouble()) {
        	        curScore = score;
        	        curX = x;
        	        curY = y;
        	        
        	        if (score[0]-score[1] > maxScore[0]-maxScore[1]) {
        	            maxScore = score;
        	            bestX = x;
        	            bestY = y;
        	        }
        	    }
        	} else {
        	    if (Math.exp((double)(score[1]-score[0]-curScore[1]+curScore[0])/length/length/t) > random.nextDouble()) {
        	        curScore = score;
        	        curX = x;
        	        curY = y;
        	        
        	        if (score[1]-score[0] > maxScore[1]-maxScore[0]) {
        	            maxScore = score;
        	            bestX = x;
        	            bestY = y;
        	        }
        	    }
        	}
        }
        
//        System.out.println("Simulated annealing: " + maxScore[0] + " " + maxScore[1]);
        
        return new BestPosition(bestX, bestY, maxScore);
    }
    
    private Move lastMove(boolean isRed) {
        BestPosition pos = null;
        if (isRed) {
            int[] maxScore = {0, Integer.MAX_VALUE};
            
            for (int i = 0; i < 10; ++i) {
                int x = random.nextInt(length);
                int y = random.nextInt(length);
                if (board[x][y] != 0) continue;
                board[x][y] = 1;
                addStone(x, y, isRed);
                
                BestPosition worstPos = sampleSearch(!isRed);
                if (worstPos.score[0]-worstPos.score[1] > maxScore[0]-maxScore[1]) {
                    maxScore = worstPos.score;
                    pos = worstPos;
                }
                board[x][y] = 0;
                addStone(x, y, !isRed);
            }
//            System.out.println("guess: " + maxScore[0] + " " + maxScore[1]);
        } else {
            pos = sampleSearch(isRed);
        }
        if (isRed)
	        red.add(pos.x * length + pos.y);
	    else
	        blue.add(pos.x * length + pos.y);
        addStone(pos.x, pos.y, isRed);
        board[pos.x][pos.y] = 1;
        return new Move(isRed?1:2, pos.x, pos.y);
    }

    private void addStone(int i, int j, boolean red) {
        double flag;
        if (red)
            flag = 1;
        else
            flag = -1;
        for (int x = 0; x < length; x++)
            for (int y = 0; y < length; y++)
                if (x == i && y == j)
                    scores[x][y] += 1000000 * flag;
                else
                    scores[x][y] += flag / ((x - i) * (x - i) + (y - j) * (y - j));
    }

    private int[] getScore(int x, int y, boolean isRed) {
        int[] score = new int[2];
        double flag;
        if (isRed)
            flag = 1;
        else
            flag = -1;
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < length; j++) {
                double pull;
                if (x == i && y == j)
                    pull = flag * 1000000;
                else
                    pull = flag / ((x - i) * (x - i) + (y - j) * (y - j));
                if (scores[i][j] + pull > 0) {
                    score[0]++;
                } else if (scores[i][j] + pull < 0) {
                    score[1]++;
                }
            }
        }
        return score;
    }

    private int[] getScore() {
        int[] score = new int[2];
        for (int i = 0; i < length; i++)
            for (int j = 0; j < length; j++) {
                double pull = scores[i][j];
                if (pull > 0)
                    score[0] += 1;
                else if (pull < 0)
                    score[1] += 1;
            }
        return score;
    }
    
    public Move play(int player, List<Move> moves, double timeRemaining) {
        if (numMoves < moves.size()) {
            for (int i = numMoves; i < moves.size(); ++i) {
                Move move = moves.get(i);
                if (move.player == 1)
                    red.add(move.x * length + move.y);
                else
                    blue.add(move.x * length + move.y);
                board[move.x][move.y] = 1;
                addStone(move.x, move.y, move.player == 1);
            }
            numMoves = moves.size() + 1;
        }
        
        if (player == 1) {
            if (count++ < stones - 1) {
                return closest(true);
            } else {
                return lastMove(true);
            }
        } else {
            if (count++ < stones - 1) {
                return closest(false);
            } else {
                return lastMove(false);
            }
        }
    }
}
