package com.micebitewood.HPS;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class AB {

    private int[] board;

    private class Child {
        public int possibility;
        public int position;

        public Child(int possibility, int position) {
            this.possibility = possibility;
            this.position = position;
        }
    }

    public AB(int[] board) {
        this.board = board;
    }

    private void decScore(int[] score, int position) {
        int weight = board[position];
        score[0] -= weight * (12 - position);
        score[1] -= weight * (14 - position);
    }

    private boolean valid(int[] score, int position) {
        int[] newScore = Arrays.copyOf(score, 2);
        decScore(newScore, position);
        if (newScore[0] < 0 && newScore[1] > 0)
            return true;
        return false;
    }

    public Child traversal(int[] score, Map<Integer, Integer> red, Map<Integer, Integer> total, boolean isRedTurn,
            int lastWeight, int count) {
        boolean option = false;
        if (isRedTurn) {
            if (red.size() != 0) {
                for (int position : red.keySet()) {
                    if (count == 0) {
                        System.out.println("next option for red");
                    }
                    if (valid(score, position)) {
                        int[] newScore = Arrays.copyOf(score, 2);
                        decScore(newScore, position);
                        Map<Integer, Integer> newTotal = new HashMap<Integer, Integer>(total);
                        newTotal.remove(position);
                        Map<Integer, Integer> newRed = new HashMap<Integer, Integer>(red);
                        newRed.remove(position);
                        int weight = board[position];
                        Child child = traversal(newScore, newRed, newTotal, false, weight, count + 1);
                        if (child.possibility == 1) {
                            System.out.println("red will win if it chooses " + position);
                            return child;
                        }
                    }
                }
            }
            else {
                for (int position : total.keySet()) {
                    if (valid(score, position)) {
                        int[] newScore = Arrays.copyOf(score, 2);
                        decScore(newScore, position);
                        Map<Integer, Integer> newTotal = new HashMap<Integer, Integer>(total);
                        newTotal.remove(position);
                        int weight = board[position];
                        Child child = traversal(newScore, red, newTotal, false, weight, count + 1);
                        if (child.possibility == 1) {
                            System.out.println("red will win if it chooses " + position);
                            return child;
                        }
                    }
                }
            }
            System.out.println("if blue chooses " + lastWeight + " in the last step, red will loose");
            return new Child(0, 0);
        }
        else {
            for (int position : total.keySet()) {
                if (valid(score, position)) {
                    int[] newScore = Arrays.copyOf(score, 2);
                    decScore(newScore, position);
                    Map<Integer, Integer> newTotal = new HashMap<Integer, Integer>(total);
                    newTotal.remove(position);
                    Map<Integer, Integer> newRed = new HashMap<Integer, Integer>(red);
                    if (red.keySet().contains(position)) {
                        newRed.remove(position);
                    }
                    int weight = board[position];
                    Child child = traversal(newScore, newRed, newTotal, true, weight, count + 1);
                    if (child.possibility == 0) {
                        System.out.println("if blue chooses " + position + " in move " + count + ", red will lose");
                        return child;
                    }
                }
            }
            System.out.println("if red chooses " + lastWeight + " in the last step, it will win!");
            return new Child(1, 0);
        }
    }

    public static void main(String[] args) {
        int[] board = {1, 2, 3, 4, 5, 8, 9, 0, 0, 7, 0, 3, 8, 6, 10, 10, 3, 0, 9, 0, 4, 2, 0, 0, 0, 0, 0, 5, 6, 7, 1 };
        Map<Integer, Integer> red = new HashMap<Integer, Integer>();
        Map<Integer, Integer> total = new HashMap<Integer, Integer>();
        int[] score = {-215, 17 };

        red.put(11, 3);
        red.put(12, 8);
        red.put(14, 10);
        red.put(16, 3);
        red.put(18, 9);
        red.put(20, 4);
        red.put(21, 2);
        red.put(27, 5);
        red.put(28, 6);
        red.put(29, 7);
        red.put(30, 1);

        total.put(0, 1);
        total.put(1, 2);
        total.put(2, 3);
        total.put(3, 4);
        total.put(4, 5);
        total.put(5, 8);
        total.put(6, 9);
        total.put(9, 7);
        total.put(11, 3);
        total.put(12, 8);
        total.put(13, 6);
        total.put(14, 10);
        total.put(15, 10);
        total.put(16, 3);
        total.put(18, 9);
        total.put(20, 4);
        total.put(21, 2);
        total.put(27, 5);
        total.put(28, 6);
        total.put(29, 7);
        total.put(30, 1);

        AB ab = new AB(board);
        Child child = ab.traversal(score, red, total, true, 0, 0);
        System.out.println(child.position + " " + child.possibility);
    }
}
