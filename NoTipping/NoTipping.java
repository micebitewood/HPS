import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NoTipping extends Thread {

    private Set<Integer> blueWeights;
    private Set<Integer> redWeights;

    public NoTipping(Set<Integer> blueWeights, Set<Integer> redWeights) {
        this.blueWeights = blueWeights;
        this.redWeights = redWeights;
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
        if (newScore[0] < 0 && newScore[1] > 0)
            return true;
        return false;
    }

    private static boolean moveValid(int[] score, int position, int weight) {
        int[] newScore = Arrays.copyOf(score, 2);
        incScore(newScore, position, weight);
        if (newScore[0] < 0 && newScore[1] > 0)
            return true;
        return false;
    }

    @Override
    public void run() {

    }

    public static boolean traversal(int[] score, Map<Integer, Integer> red, Map<Integer, Integer> total,
            boolean isRedTurn) {
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
                        boolean redWins = traversal(newScore, newRed, newTotal, false);
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
                        boolean redWins = traversal(newScore, red, newTotal, false);
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
                    boolean blueWins = traversal(newScore, newRed, newTotal, true);
                    if (blueWins) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    public static void createThreads(int[] score, Map<Integer, Integer> red, Map<Integer, Integer> total,
            boolean isRedTurn, int lastPosition) {
        if (isRedTurn) {

        } else {

        }
    }

    public static int[] getNextRemoveForBlue(Map<Integer, Integer> red, Map<Integer, Integer> total) {
        int[] score = {0, 0 };
        for (int position : total.keySet()) {
            incScore(score, position, total.get(position));
        }
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
                boolean blueWins = traversal(newScore, newRed, newTotal, true);
                if (blueWins) {
                    int[] ret = {position, weight };
                    return ret;
                }
            }
        }
        for (int position : total.keySet()) {
            int[] ret = {position, total.get(position) };
            return ret;
        }
        int[] ret = {0, 0 };
        return ret;
    }

    public static int[] getNextRemoveForRed(Map<Integer, Integer> red, Map<Integer, Integer> total) {
        int[] score = {0, 0 };
        for (int position : total.keySet()) {
            incScore(score, position, total.get(position));
        }

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
                    boolean redWins = traversal(newScore, newRed, newTotal, false);
                    if (redWins) {
                        int[] ret = {position, weight };
                        return ret;
                    }
                }
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
                    boolean redWins = traversal(newScore, red, newTotal, false);
                    if (redWins) {
                        int[] ret = {position, weight };
                        return ret;
                    }
                }
            }
            for (int position : total.keySet()) {
                int[] ret = {position, total.get(position) };
                return ret;
            }
        }
        int[] ret = {0, 0 };
        return ret;
    }

    public int[] getNextMoveForBlue(Map<Integer, Integer> total) {
        int[] score = {0, 0 };
        for (int position : total.keySet())
            incScore(score, position, total.get(position));
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
        } else if (-score[0] > score[1]) {
            for (int weight = 1; weight < 13; weight++)
                if (!blueWeights.contains(weight))
                    for (int position = -15; position < 16; position++) {
                        if (!total.containsKey(position) && moveValid(score, position, weight)) {
                            int[] ret = {position, weight };
                            return ret;
                        }
                    }
        } else {
            for (int weight = 1; weight < 13; weight++)
                if (!blueWeights.contains(weight))
                    for (int position = 15; position >= -15; position--) {
                        if (!total.containsKey(position) && moveValid(score, position, weight)) {
                            int[] ret = {position, weight };
                            return ret;
                        }
                    }
        }
        int[] ret = {0, 0 };
        return ret;
    }

    public int[] getNextMoveForRed(Map<Integer, Integer> total, int redSum) {
        int[] score = {0, 0 };
        for (int position : total.keySet())
            incScore(score, position, total.get(position));
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
        int[] ret = {0, 0 };
        return ret;
    }

    public static void main(String[] args) throws IOException {
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
                }

            }
        }
        br.close();
        if (mode == 1) {
            NoTipping game = new NoTipping(blueWeights, redWeights);
            if (playerNum == 1) {
                redSum /= redWeights.size();
                int[] res = game.getNextMoveForRed(total, redSum);
                System.out.println(res[0] + " " + res[1]);
            } else {
                int[] res = game.getNextMoveForBlue(total);
                System.out.println(res[0] + " " + res[1]);
            }
        } else {
            if (playerNum == 1) {
                int[] res = getNextRemoveForRed(red, total);
                System.out.println(res[0] + " " + res[1]);
            } else {
                int[] res = getNextRemoveForBlue(red, total);
                System.out.println(res[0] + " " + res[1]);
            }
        }
    }
}
