
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class AB {

    private class Child {
        public boolean possibility;
        public int position;

        public Child(boolean possibility, int position) {
            this.possibility = possibility;
            this.position = position;
        }
    }

    private void decScore(int[] score, int position, int weight) {
        score[0] -= weight * (12 - position);
        score[1] -= weight * (14 - position);
    }

    private boolean valid(int[] score, int position, int weight) {
        int[] newScore = Arrays.copyOf(score, 2);
        decScore(newScore, position, weight);
        if (newScore[0] < 0 && newScore[1] > 0)
            return true;
        return false;
    }

    public Child traversal(int[] score, Map<Integer, Integer> red, Map<Integer, Integer> total, boolean isRedTurn,
            int lastPosition) {
        if (isRedTurn) {
            if (red.size() != 0) {
                for (int position : red.keySet()) {
                    int weight = red.get(position);
                    if (valid(score, position, weight)) {
                        int[] newScore = Arrays.copyOf(score, 2);
                        decScore(newScore, position, weight);
                        Map<Integer, Integer> newTotal = new HashMap<Integer, Integer>(total);
                        newTotal.remove(position);
                        Map<Integer, Integer> newRed = new HashMap<Integer, Integer>(red);
                        newRed.remove(position);
                        Child child = traversal(newScore, newRed, newTotal, false, position);
                        if (child.possibility) {
                            return new Child(true, position);
                        }
                    }
                }
            }
            else {
                for (int position : total.keySet()) {
                    int weight = total.get(position);
                    if (valid(score, position, weight)) {
                        int[] newScore = Arrays.copyOf(score, 2);
                        decScore(newScore, position, weight);
                        Map<Integer, Integer> newTotal = new HashMap<Integer, Integer>(total);
                        newTotal.remove(position);
                        Child child = traversal(newScore, red, newTotal, false, position);
                        if (child.possibility) {
                            return new Child(true, position);
                        }
                    }
                }
            }
            return new Child(false, 0);
        }
        else {
            for (int position : total.keySet()) {
                int weight = total.get(position);
                if (valid(score, position, weight)) {
                    int[] newScore = Arrays.copyOf(score, 2);
                    decScore(newScore, position, weight);
                    Map<Integer, Integer> newTotal = new HashMap<Integer, Integer>(total);
                    newTotal.remove(position);
                    Map<Integer, Integer> newRed = new HashMap<Integer, Integer>(red);
                    if (red.keySet().contains(position)) {
                        newRed.remove(position);
                    }
                    Child child = traversal(newScore, newRed, newTotal, true, position);
                    if (!child.possibility) {
                        return new Child(false, position);
                    }
                }
            }
            return new Child(true, 0);
        }
    }

    public static void main(String[] args) {
        Map<Integer, Integer> red = new HashMap<Integer, Integer>();
        Map<Integer, Integer> total = new HashMap<Integer, Integer>();
        for (int i = 0; i < 13; i++)
        {
            int key = Integer.parseInt(args[2 * i]);
            int value = Integer.parseInt(args[2 * i + 1]);
            System.out.print(key + ":" + value + " ");
            red.put(key, value);
        }
        System.out.println();
        for (int i = 0; i < 25; i++) {
            int key = Integer.parseInt(args[2 * i + 26]);
            int value = Integer.parseInt(args[2 * i + 27]);
            System.out.print(key + ":" + value + " ");
            total.put(key, value);
        }
        System.out.println();
        int[] score = {Integer.parseInt(args[76]), Integer.parseInt(args[77]) };
        System.out.println(score[0] + ", " + score[1]);
        
        /*
         * red.put(10, 7); red.put(11, 3); red.put(12, 5); red.put(13, 12); red.put(18, 3); red.put(19, 11); red.put(20,
         * 8); red.put(21, 2); red.put(22, 4); red.put(24, 6); red.put(25, 10); red.put(26, 9); red.put(28, 1);
         * 
         * total.put(1, 1); total.put(3, 11); total.put(4, 8); total.put(5, 5); total.put(6, 12); total.put(7, 10);
         * total.put(8, 6); total.put(10, 7); total.put(11, 3); total.put(12, 5); total.put(13, 12); total.put(14, 7);
         * total.put(15, 3); total.put(16, 9); total.put(17, 2); total.put(18, 3); total.put(19, 11); total.put(20, 8);
         * total.put(21, 2); total.put(22, 4); total.put(23, 4); total.put(24, 6); total.put(25, 10); total.put(26, 9);
         * total.put(28, 1);
         */
        AB ab = new AB();
        Child child = ab.traversal(score, red, total, true, 0);
        System.out.println(child.possibility + " " + child.position);
    }
}
