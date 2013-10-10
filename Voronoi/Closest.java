import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Closest {
    private static final int LENGTH = 100;
    private static final int STONES = 5;
    private static final int COUNT = 100;
    private static final int THREAD_COUNT = 16;

    public static void main(String[] args) {

        for (int i = 0; i < THREAD_COUNT; i++) {
            MultiRun multiRun = new MultiRun(LENGTH, STONES, COUNT);
            multiRun.start();
        }
    }
}

class MultiRun extends Thread {
    private static Random random;
    private int length;
    private int stones;
    private int count;
    private static final Object lock = new Object();

    public MultiRun(int length, int stones, int count) {
        this.length = length;
        this.stones = stones;
        random = new Random(System.currentTimeMillis());
        this.count = count;
    }

    private void move(int[][] board, Set<Integer> color) {
        boolean isValid = false;
        while (!isValid) {
            int x = random.nextInt(length);
            int y = random.nextInt(length);
            if (board[x][y] == 0) {
                isValid = true;
                board[x][y] = 1;
                color.add(x * length + y);
            }
        }
    }

    private void closest(int[][] board, Set<Integer> color1, Set<Integer> color2) {
        if (color2.size() == 0)
            color1.add(length / 2 * length + length / 2);
        else {
            int maxScore = 0;
            int maxPosition = 0;
            for (int stone : color2) {
                int x = stone / length;
                int y = stone % length;
                for (int i = -1; i < 2; i++) {
                    if (x + i < 0 || x + i >= length)
                        continue;
                    for (int j = -1; j < 2; j++) {
                        if (y + j < 0 || y + j >= length)
                            continue;
                        if (board[x + i][y + j] == 0) {
                            Set<Integer> newColor1 = new HashSet<Integer>(color1);
                            newColor1.add((x + i) * length + y + j);
                            int[] score = getScore(newColor1, color2);
                            if (score[0] > maxScore) {
                                maxScore = score[0];
                                maxPosition = (x + i) * length + y + j;
                            }
                        }
                    }
                }
            }
            color1.add(maxPosition);
        }
    }

    private double getPull(int i, int j, Set<Integer> color) {
        double pull = 0;
        for (int stone : color) {
            int x = stone / length;
            int y = stone % length;
            if (x == i && y == j) {
                return 9999999;
            }
            pull += 1 / (Math.sqrt((x - i) * (x - i) + (y - j) * (y - j)));
        }
        return pull;
    }

    private void redMove(int[][] board, Set<Integer> red, Set<Integer> blue) {
        move(board, red);
    }

    private void blueMove(int[][] board, Set<Integer> red, Set<Integer> blue) {
        // move(board, blue);
        closest(board, blue, red);
    }

    private int[] getScore(Set<Integer> color1, Set<Integer> color2) {
        int[] score = new int[2];
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < length; j++) {
                double pull1 = getPull(i, j, color1);
                double pull2 = getPull(i, j, color2);
                if (pull1 > pull2)
                    score[0] += 1;
                else if (pull1 < pull2)
                    score[1] += 1;
            }
        }
        return score;
    }

    @Override
    public void run() {

        int redCount = 0;
        int blueCount = 0;
        int tieCount = 0;
        Set<Integer> red = new HashSet<Integer>();// x * LENGTH + y
        Set<Integer> blue = new HashSet<Integer>();
        for (int i = 0; i < count; i++) {
            int[][] board = new int[length][length];
            red.clear();
            blue.clear();

            for (int n = 0; n < stones; n++) {
                redMove(board, red, blue);
                blueMove(board, red, blue);
            }
            int[] score = getScore(red, blue);
            if (score[0] > score[1])
                redCount += 1;
            else if (score[0] < score[1])
                blueCount += 1;
            else
                tieCount += 1;
        }
        synchronized (lock) {
            System.out.println(redCount + " " + blueCount + " " + tieCount);
        }
    }
}
