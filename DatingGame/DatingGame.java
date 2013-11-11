import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class DatingGame {
    
    private static final String EOM = "<EOM>";
    private static final double MAX_PERCENT_MODIFICATION = 0.20;
    private static final double MAX_NUM_MODIFICATIONS = 0.05;
    
    private static PrintWriter out = null;
    private static BufferedReader in = null;
    private static int port;
    private static final String host = "127.0.0.1";
    private static final String teamName = "JJ";
    
    private static int numFeatures;
    
    public static void main(String[] args) throws IOException {
        
        port = Integer.parseInt(args[0]);
        Socket sock = new Socket(host, port);
        out = new PrintWriter(sock.getOutputStream());
        in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        
        String question = readData(in);
        if (!question.equals("Team Name?")) {
            System.err.println("Server not following convention.");
            System.exit(0);
        }
        
        sendSocket(out, teamName);
        String[] startItems = (readData(in).trim()).split("\n");
        String[] modeAndNum = startItems[0].split("\\s+");
        String mode = modeAndNum[0];
        numFeatures = Integer.parseInt(modeAndNum[1]);
        
        if (mode.equals("P")) {
            Person person = new Person();
            sendSocket(out, person.genWeights(numFeatures));
            
            while (!readData(in).equals("end")) {
                sendSocket(out, person.fixWeights());
            }
        } else if (mode.equals("M")) {
            Matchmaker matchmaker = new Matchmaker(startItems, numFeatures);
            do {
                sendSocket(out, matchmaker.getNextCandidates());
            } while (!(matchmaker.getData(readData(in))).equals("end"));
            System.out.println("end");
        }
        in.close();
        out.close();
        sock.close();
    }
    
    /* Socket Functions */
    
    private static String readData(BufferedReader in) throws IOException {
        String chunk = null;
        StringBuilder data = new StringBuilder();
        
        while ((chunk = in.readLine()) == null) {
        }
        
        do {
            data.append(chunk).append("\n");
            
            int lastIndex = data.toString().indexOf(EOM);
            if (lastIndex >= 0) {
                return data.toString().substring(0, lastIndex);
            }
        } while ((chunk = in.readLine()) != null);
        
        return null;
    }
    
    private static void sendSocket(PrintWriter out, String msg) {
        msg += EOM;
        out.println(msg);
        out.flush();
    }
    /* End of Socket Functions */
    
}

class Person {
    List<Integer> weights;
    int numFeatures;
    
    public String genWeights(int numFeatures) {
        weights = new ArrayList<Integer>();
        this.numFeatures = numFeatures;
        Random random = new Random(System.currentTimeMillis());
        int posNum = random.nextInt(numFeatures + 1);
        int sum = 0;
        for (int i = 0; i < posNum - 1; i++) {
            int next = random.nextInt(101 - sum);
            weights.add(next);
            sum += next;
            System.out.println("pos: " + next + " sum: " + sum);
        }
        if (posNum != 0) {
            weights.add(100 - sum);
        }
        sum = 0;
        for (int i = posNum; i < numFeatures - 1; i++) {
            int next = -random.nextInt(101 - sum);
            weights.add(next);
            sum -= next;
            System.out.println("neg: " + next + " sum: " + sum);
        }
        if (posNum != numFeatures) {
            weights.add(sum - 100);
        }
        Collections.shuffle(weights);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numFeatures; i++) {
            if (weights.get(i) >= 0) {
                if (weights.get(i) == 100)
                    sb.append("1.00 ");
                else
                    sb.append(String.format("0.%02d ", weights.get(i)));
            }
            else {
                if (weights.get(i) == -100)
                    sb.append("-1.00 ");
                else
                    sb.append(String.format("-0.%02d ", -weights.get(i)));
            }
        }
        System.out.println();
        return sb.toString().trim();
    }
    
    public String fixWeights() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numFeatures; i++) {
            if (weights.get(i) >= 0) {
                if (weights.get(i) == 100)
                    sb.append("1.00 ");
                else
                    sb.append(String.format("0.%02d ", weights.get(i)));
            }
            else {
                if (weights.get(i) == -100)
                    sb.append("-1.00 ");
                else
                    sb.append(String.format("-0.%02d ", -weights.get(i)));
            }
        }
        return sb.toString().trim();
    }
    
}

class Matchmaker {
    List<Candidate> candidates;
    double[] lastFeatures;
    int numFeatures;
    Random random;
    int round;
    List<Integer> group;
    int maxInGroup;
    
    public Matchmaker(String[] initString, int numFeatures) {
        candidates = new ArrayList<Candidate>();
        lastFeatures = new double[numFeatures];
        random = new Random(System.currentTimeMillis());
        this.numFeatures = numFeatures;
        for (int i = 0; i < 20; i++) {
            String[] candidatesAndScores = initString[i + 1].split("\\s+");
            double[] features = new double[numFeatures];
            for (int j = 0; j < numFeatures; j++) {
                features[j] = Double.parseDouble(candidatesAndScores[j]);
            }
            double score = Double.parseDouble(candidatesAndScores[numFeatures]);
            candidates.add(new Candidate(score, features));
        }
    }
    
    public String getNextCandidates() {
        if (round == 20)
            return "";
        StringBuilder sb = new StringBuilder();
        // TODO implement other strategy
        // getRandomCandidates();
        // if (round < 19)
        // naiveStrategy();
        // else {
        Arrays.fill(lastFeatures, 0);
        if (round == 0) {
            for (Candidate candidate : candidates)
                System.out.println(candidate.toString());
            group = new ArrayList<Integer>();
            initGroup();
            // naiveStrategy();
//            Arrays.fill(lastFeatures, 1);
//            pickNeg();
            // addPosToGroup();
//            correlationCoefficient();
        }
        groupStrategy();
        // }
        round++;
        for (int i = 0; i < numFeatures; i++) {
            sb.append(lastFeatures[i] + " ");
        }
        return sb.toString().trim();
    }
    
    private void initGroup() {
        for (int i = 0; i < numFeatures; i++) {
            group.add(-1);
        }
    }
    
    private void addPosToGroup() {
        for (Candidate candidate : candidates) {
            if (candidate.score > 0) {
                for (int i = 0; i < candidate.features.length; i++) {
                    double feature = candidate.features[i];
                    if (feature > 0.9) {
                        group.set(i, 0);
                    }
                }
            }
        }
        
    }
    
    private void pickNeg() {
        for (Candidate candidate : candidates) {
            if (candidate.score < 0) {
                for (int i = 0; i < candidate.features.length; i++) {
                    double feature = candidate.features[i];
                    if (feature > 0.85) {
                        lastFeatures[i] = 0;
                        group.set(i, -1);
                    }
                }
            }
        }
    }
    
    private void groupStrategy() {
        if (round == 0) {
            // Init
            getGroup();
//             for (int i = 0; i < numFeatures; ++i) {
//                 if (lastFeatures[i] == 1 && group.get(i) == -1)
//                     group.set(i, ++maxInGroup % 18 + 1);
//             }
            // Collections.shuffle(group, random);
            
            for (int i = 0; i < group.size(); i++) {
                System.out.println(i + ": " + group.get(i));
            }
        }
        
        if (round == 19) {
            // Final call
            for (int i = 0; i < numFeatures; ++i) {
                if (group.get(i) >= 0 && group.get(i) < 19)
                    lastFeatures[i] = (candidates.get(20 + group.get(i)).score > 0) ? 1 : 0;
                else if (group.get(i) == -1)
                    lastFeatures[i] = 1;
            }
        } else {
            // Test each group
            int curGroup = candidates.size() - 20;
            for (int i = 0; i < numFeatures; ++i)
                lastFeatures[i] = (group.get(i) == curGroup) ? 1 : 0;
        }
    }
    
    private void getGroup() {
        double[] corr = new double[numFeatures];
        int[] rank = new int[numFeatures];
        for (int i = 0; i < numFeatures; ++i) {
            corr[i] = 0.0;
            double sum = 0.0;
            for (Candidate candidate : candidates) {
                corr[i] += candidate.features[i] * candidate.score;
                sum += candidate.features[i];
            }
            corr[i] /= sum;
        }
        for (int i = 0; i < numFeatures; ++i) {
            rank[i] = 0;
            for (int j = 0; j < numFeatures; ++j)
                if (corr[i] < corr[j] || corr[i] == corr[j] && i > j)
                    ++rank[i];
        }
        // Group from -1 to 19, where -1 means it's positive and doesn't need to be tested,
        // 19 means it's negative and doesn't need to be tested.
        for (int i = 0; i < numFeatures; ++i)
            group.set(i, rank[i] * 21 / numFeatures - 1);
    }
    
    private void correlationCoefficient() {
        double sum = 0;
        for (Candidate candidate : candidates) {
            sum += candidate.score;
        }
        sum /= candidates.size();
        for (int i = 0; i < numFeatures; i++) {
            double sumX = 0;
            for (Candidate candidate : candidates) {
                sumX += candidate.features[i];
            }
            sumX /= candidates.size();
            double cov = 0;
            for (Candidate candidate : candidates) {
                cov += (candidate.features[i]) * (candidate.score);
            }
            cov -= candidates.size() * sumX * sum;
            if (cov > 0)
                lastFeatures[i] = 1;
        }
    }
    
    private void naiveStrategy() {
        Collections.sort(candidates);
        Candidate bestCandidate = candidates.get(candidates.size() - 1);
        for (int i = 0; i < numFeatures; i++) {
            double firstPart = 0;
            double secondPart = 0;
            for (int j = 0; j < candidates.size(); j++) {
                Candidate candidate = candidates.get(j);
                if (j < candidates.size() / 2 + 1)
                    firstPart += candidate.features[i];
                else
                    secondPart += candidate.features[i];
            }
            if (firstPart < secondPart) {
                if (random.nextBoolean() || random.nextBoolean() || random.nextBoolean())
                    lastFeatures[i] = 1;
                else
                    lastFeatures[i] = bestCandidate.features[i];
            } else {
                if (random.nextBoolean())
                    lastFeatures[i] = random.nextDouble();
                else
                    lastFeatures[i] = 0;
            }
        }
    }
    
    private void getRandomCandidates() {
        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < numFeatures; i++) {
            lastFeatures[i] = random.nextDouble();
        }
    }
    
    public String getData(String readData) {
        if (readData.equals("end")) {
            return readData;
        }
        String[] lines = readData.split("\n");
        int lastInd = lines.length - 1;
        String[] candidatesAndScores = lines[lastInd].split("\\s+");
        double score = Double.parseDouble(candidatesAndScores[numFeatures]);
        candidates.add(new Candidate(score, lastFeatures));
        return readData;
    }
}

class Candidate implements Comparable<Candidate> {
    double score;
    double[] features;
    
    public Candidate(double score, double[] features) {
        this.score = score;
        this.features = features;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < features.length; i++) {
            sb.append(features[i] + " ");
        }
        sb.append(score);
        return sb.toString();
    }
    
    @Override
    public int compareTo(Candidate candidate) {
        if (score - candidate.score > 0)
            return 1;
        else if (score - candidate.score == 0)
            return 0;
        return -1;
    }
}
