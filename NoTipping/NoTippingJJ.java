
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class NoTippingJJ implements Callable<Boolean> {

    static final long TIME_LIMIT = 80;

    private Set<Integer> blueWeights;
    private Set<Integer> redWeights;

    int[] score;
    Map<Integer, Integer> red;
    Map<Integer, Integer> total;
    boolean isRedTurn;
    long endTime;

    public NoTippingJJ(Set<Integer> blueWeights, Set<Integer> redWeights) {
        this.blueWeights = blueWeights;
        this.redWeights = redWeights;
    }

    public NoTippingJJ(int[] score, Map<Integer, Integer> red, Map<Integer, Integer> total, boolean isRedTurn, long endTime) {
        this.score = score;
        this.red = red;
        this.total = total;
        this.isRedTurn = isRedTurn;
        this.endTime = endTime;
    }

    private static void incScore(int[] score, int position, int weight) {
        score[0] -= weight * (3 + position);
        score[1] -= weight * (1 + position);
    }

    private static void decScore(int[] score, int position, int weight) {
        score[0] += weight * (3 + position);
        score[1] += weight * (1 + position);
    }

    private static boolean removeValid(int[] score, int position, int weight) {
        int[] newScore = Arrays.copyOf(score, 2);
        decScore(newScore, position, weight);
        if (newScore[0] <= 0 && newScore[1] >= 0)
            return true;
        return false;
    }

    private static boolean moveValid(int[] score, int position, int weight) {
        int[] newScore = Arrays.copyOf(score, 2);
        incScore(newScore, position, weight);
        if (newScore[0] <= 0 && newScore[1] >= 0)
            return true;
        return false;
    }

    public static boolean traversal(int[] score, Map<Integer, Integer> red, Map<Integer, Integer> total,
            boolean isRedTurn, long endTime) {
        
        if (System.currentTimeMillis() > endTime)
            return true;
        
        if (isRedTurn) {
            if (red.size() != 0) {
                for (int position : red.keySet()) {
                    int weight = red.get(position);
                    if (removeValid(score, position, weight)) {
                        int[] newScore = Arrays.copyOf(score, 2);
                        decScore(newScore, position, weight);
                        Map<Integer, Integer> newTotal = new HashMap<Integer, Integer>(total);
                        newTotal.remove(position);
                        Map<Integer, Integer> newRed = new HashMap<Integer, Integer>(red);
                        newRed.remove(position);
                        boolean redWins = traversal(newScore, newRed, newTotal, false, endTime);
                        if (redWins) {
                            return false;
                        }
                    }
                }
            } else {
                for (int position : total.keySet()) {
                    int weight = total.get(position);
                    if (removeValid(score, position, weight)) {
                        int[] newScore = Arrays.copyOf(score, 2);
                        decScore(newScore, position, weight);
                        Map<Integer, Integer> newTotal = new HashMap<Integer, Integer>(total);
                        newTotal.remove(position);
                        boolean redWins = traversal(newScore, red, newTotal, false, endTime);
                        if (redWins) {
                            return false;
                        }
                    }
                }
            }
            return true;
        } else {
            for (int position : total.keySet()) {
                int weight = total.get(position);
                if (removeValid(score, position, weight)) {
                    int[] newScore = Arrays.copyOf(score, 2);
                    decScore(newScore, position, weight);
                    Map<Integer, Integer> newTotal = new HashMap<Integer, Integer>(total);
                    newTotal.remove(position);
                    Map<Integer, Integer> newRed = new HashMap<Integer, Integer>(red);
                    if (red.keySet().contains(position)) {
                        newRed.remove(position);
                    }
                    boolean blueWins = traversal(newScore, newRed, newTotal, true, endTime);
                    if (blueWins) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    @Override
    public Boolean call() throws Exception {
        return traversal(score, red, total, isRedTurn, endTime);
    }

    public static boolean createThreads(int[] score, Map<Integer, Integer> red, Map<Integer, Integer> total,
            boolean isRedTurn, long endTime) throws InterruptedException, ExecutionException {
        List<Callable<Boolean>> lst = new ArrayList<Callable<Boolean>>();
        if (isRedTurn) {
            if (red.size() != 0) {
                for (int position : red.keySet()) {
                    int weight = red.get(position);
                    if (removeValid(score, position, weight)) {
                        int[] newScore = Arrays.copyOf(score, 2);
                        decScore(newScore, position, weight);
                        Map<Integer, Integer> newTotal = new HashMap<Integer, Integer>(total);
                        newTotal.remove(position);
                        Map<Integer, Integer> newRed = new HashMap<Integer, Integer>(red);
                        newRed.remove(position);
                        lst.add(new NoTippingJJ(newScore, newRed, newTotal, false, endTime));
                    }
                }
            } else {
                for (int position : total.keySet()) {
                    int weight = total.get(position);
                    if (removeValid(score, position, weight)) {
                        int[] newScore = Arrays.copyOf(score, 2);
                        decScore(newScore, position, weight);
                        Map<Integer, Integer> newTotal = new HashMap<Integer, Integer>(total);
                        newTotal.remove(position);
                        lst.add(new NoTippingJJ(newScore, red, newTotal, false, endTime));
                    }
                }
            }
        } else {
            for (int position : total.keySet()) {
                int weight = total.get(position);
                if (removeValid(score, position, weight)) {
                    int[] newScore = Arrays.copyOf(score, 2);
                    decScore(newScore, position, weight);
                    Map<Integer, Integer> newTotal = new HashMap<Integer, Integer>(total);
                    newTotal.remove(position);
                    Map<Integer, Integer> newRed = new HashMap<Integer, Integer>(red);
                    if (red.keySet().contains(position)) {
                        newRed.remove(position);
                    }
                    lst.add(new NoTippingJJ(newScore, newRed, newTotal, false, endTime));
                }
            }
        }

        if (lst.size() == 0) {
            return true;
        }
        ExecutorService executorService = Executors.newFixedThreadPool(lst.size());
        List<Future<Boolean>> tasks = executorService.invokeAll(lst);
        executorService.shutdown();

        for (Future<Boolean> task : tasks) {
            if (task.get()) {
                return false;
            }
        }
        return true;
    }

    public static int[] getNextRemoveForBlue(Map<Integer, Integer> red, Map<Integer, Integer> total, long endTime)
            throws InterruptedException, ExecutionException {
        int[] score = {0, 0 };
        for (int position : total.keySet()) {
            incScore(score, position, total.get(position));
        }
        incScore(score, 0, 3);
        for (int position : total.keySet()) {
            int weight = total.get(position);
            if (removeValid(score, position, weight)) {
                int[] newScore = Arrays.copyOf(score, 2);
                decScore(newScore, position, weight);
                Map<Integer, Integer> newTotal = new HashMap<Integer, Integer>(total);
                newTotal.remove(position);
                Map<Integer, Integer> newRed = new HashMap<Integer, Integer>(red);
                if (red.keySet().contains(position)) {
                    newRed.remove(position);
                }
                boolean blueWins = createThreads(newScore, newRed, newTotal, true, endTime);
                if (blueWins) {
                    int[] ret = {position, weight };
                    return ret;
                }
            }
        }
        for (int position : total.keySet())
            if(removeValid(score, position, total.get(position))) {
                int[] ret = {position, total.get(position) };
                return ret;
            }
        for (int position : total.keySet()) {
            int[] ret = {position, total.get(position) };
            return ret;
        }
        int[] ret = {0, 0 };
        return ret;
    }

    public static int[] getNextRemoveForRed(Map<Integer, Integer> red, Map<Integer, Integer> total, long endTime)
            throws InterruptedException, ExecutionException {
        int[] score = {0, 0 };
        for (int position : total.keySet()) {
            incScore(score, position, total.get(position));
        }
        incScore(score, 0, 3);

        if (red.size() != 0) {
            for (int position : red.keySet()) {
                int weight = red.get(position);
                if (removeValid(score, position, weight)) {
                    int[] newScore = Arrays.copyOf(score, 2);
                    decScore(newScore, position, weight);
                    Map<Integer, Integer> newTotal = new HashMap<Integer, Integer>(total);
                    newTotal.remove(position);
                    Map<Integer, Integer> newRed = new HashMap<Integer, Integer>(red);
                    newRed.remove(position);
                    boolean redWins = createThreads(newScore, newRed, newTotal, false, endTime);
                    if (redWins) {
                        int[] ret = {position, weight };
                        return ret;
                    }
                }
            }
            for (int position : red.keySet())
                if(removeValid(score, position, red.get(position))) {
                    int[] ret = {position, red.get(position) };
                    return ret;
                }
            for (int position : red.keySet()) {
                int[] ret = {position, red.get(position) };
                return ret;
            }
        } else {
            for (int position : total.keySet()) {
                int weight = total.get(position);
                if (removeValid(score, position, weight)) {
                    int[] newScore = Arrays.copyOf(score, 2);
                    decScore(newScore, position, weight);
                    Map<Integer, Integer> newTotal = new HashMap<Integer, Integer>(total);
                    newTotal.remove(position);
                    boolean redWins = createThreads(newScore, red, newTotal, false, endTime);
                    if (redWins) {
                        int[] ret = {position, weight };
                        return ret;
                    }
                }
            }
            for (int position : total.keySet())
                if(removeValid(score, position, total.get(position))) {
                    int[] ret = {position, total.get(position) };
                    return ret;
                }
            for (int position : total.keySet()) {
                int[] ret = {position, total.get(position) };
                return ret;
            }
        }
        int[] ret = {0, 0 };
        return ret;
    }

    public int[] getNextMoveForBlue(Map<Integer, Integer> total, int blueSum) {
        int[] score = {0, 0 };
        for (int position : total.keySet())
            incScore(score, position, total.get(position));
        incScore(score, 0, 3);
        if (!total.containsKey(-3)) {
            for (int weight = 12; weight > 0; weight--)
                if (!blueWeights.contains(weight)) {
                    int[] ret = {-3, weight };
                    return ret;
                }
        } else if (!total.containsKey(-2)) {
            for (int weight = 12; weight > 0; weight--)
                if (!blueWeights.contains(weight)) {
                    int[] ret = {-2, weight };
                    return ret;
                }
        } else if (!total.containsKey(-1)) {
            for (int weight = 12; weight > 0; weight--)
                if (!blueWeights.contains(weight)) {
                    int[] ret = {-1, weight };
                    return ret;
                }
        } else if (blueSum > -2) {
            for (int weight = 12; weight > 0; weight--)
                if (!blueWeights.contains(weight))
                    for (int position = -15; position < 16; position++) {
                        if (!total.containsKey(position) && moveValid(score, position, weight)) {
                            int[] ret = {position, weight };
                            return ret;
                        }
                    }
        } else {
            for (int weight = 12; weight > 0; weight--)
                if (!blueWeights.contains(weight))
                    for (int position = 15; position >= -15; position--) {
                        if (!total.containsKey(position) && moveValid(score, position, weight)) {
                            int[] ret = {position, weight };
                            return ret;
                        }
                    }
        }
        for (int weight = 12; weight > 0; weight--)
            if (!blueWeights.contains(weight))
                for (int position = 15; position >= -15; position--) {
                    if (!total.containsKey(position)) {
                        int[] ret = {position, weight };
                        return ret;
                    }
                }
        int[] ret = {0, 0 };
        return ret;
    }

    public int[] getNextMoveForRed(Map<Integer, Integer> total, int redSum) {
        int[] score = {0, 0 };
        for (int position : total.keySet())
            incScore(score, position, total.get(position));
        incScore(score, 0, 3);
        if (!total.containsKey(-3)) {
            for (int weight = 12; weight > 0; weight--)
                if (!redWeights.contains(weight)) {
                    int[] ret = {-3, weight };
                    return ret;
                }
        } else if (!total.containsKey(-2)) {
            for (int weight = 12; weight > 0; weight--)
                if (!redWeights.contains(weight)) {
                    int[] ret = {-2, weight };
                    return ret;
                }
        } else if (!total.containsKey(-1)) {
            for (int weight = 12; weight > 0; weight--)
                if (!redWeights.contains(weight)) {
                    int[] ret = {-1, weight };
                    return ret;
                }
        } else if (redSum > -2) {
            for (int weight = 1; weight < 13; weight++)
                if (!redWeights.contains(weight))
                    for (int position = -15; position < 16; position++) {
                        if (!total.containsKey(position) && moveValid(score, position, weight)) {
                            int[] ret = {position, weight };
                            return ret;
                        }
                    }
        } else {
            for (int weight = 1; weight < 13; weight++)
                if (!redWeights.contains(weight))
                    for (int position = 15; position >= -15; position--) {
                        if (!total.containsKey(position) && moveValid(score, position, weight)) {
                            int[] ret = {position, weight };
                            return ret;
                        }
                    }
        }
        for (int weight = 1; weight < 13; weight++)
            if (!redWeights.contains(weight))
                for (int position = 15; position >= -15; position--) {
                    if (!total.containsKey(position)) {
                        int[] ret = {position, weight };
                        return ret;
                    }
                }
        int[] ret = {0, 0 };
        return ret;
    }

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        long startTime = System.currentTimeMillis();
        int mode = Integer.parseInt(args[0]);
        int playerNum = Integer.parseInt(args[1]);
        // double remainingTime = Double.parseDouble(args[2]);

        Map<Integer, Integer> red = new HashMap<Integer, Integer>();
        Map<Integer, Integer> total = new HashMap<Integer, Integer>();
        Set<Integer> redWeights = new HashSet<Integer>();
        Set<Integer> blueWeights = new HashSet<Integer>();
        BufferedReader br = new BufferedReader(new FileReader("board.txt"));
        String line;
        int redSum = 0;
        int blueSum = 0;
        while ((line = br.readLine()) != null) {
            String[] temp = line.split(" ");
            int position = Integer.parseInt(temp[0]);
            int weight = Integer.parseInt(temp[1]);
            int player = Integer.parseInt(temp[2]);
            if (weight > 0) {
                total.put(position, weight);
                if (player < 2) {
                    red.put(position, weight);
                    if (player == 1) {
                        redWeights.add(weight);
                        redSum += position;
                    }
                } else {
                    blueWeights.add(weight);
                    blueSum += position;
                }

            }
        }
        br.close();
        if (mode == 1) {
            NoTippingJJ game = new NoTippingJJ(blueWeights, redWeights);
            if (playerNum == 1) {
                if (!redWeights.isEmpty())
                    redSum /= redWeights.size();
                int[] res = game.getNextMoveForRed(total, redSum);
                System.out.println(res[0] + " " + res[1]);
            } else {
                if (!blueWeights.isEmpty())
                    blueSum /= blueWeights.size();
                int[] res = game.getNextMoveForBlue(total, blueSum);
                System.out.println(res[0] + " " + res[1]);
            }
        } else {
            if (playerNum == 1) {
                int[] res = getNextRemoveForRed(red, total, startTime + TIME_LIMIT*1000);
                System.out.println(res[0] + " " + res[1]);
            } else {
                int[] res = getNextRemoveForBlue(red, total, startTime + TIME_LIMIT*1000);
                System.out.println(res[0] + " " + res[1]);
            }
        }
//        System.out.println(System.currentTimeMillis() / 1000 - startTime);
    }

}
