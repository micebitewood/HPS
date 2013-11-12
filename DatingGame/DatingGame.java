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
    
    // 40
    Random random;
    int numPosWeights;
    int numNegWeights;
    int avgPosWeight;
    int avgNegWeight;
    List<Integer> weightMap;
    
    private void randomAverageWeights40() {
        random = new Random(System.currentTimeMillis());
        if (numFeatures >= 40) {
            numPosWeights = numNegWeights = 20;
            avgPosWeight = avgNegWeight = 5;
        } else {
            numPosWeights = numFeatures / 2;
            numNegWeights = numFeatures - numPosWeights;
            avgPosWeight = 100 / numPosWeights;
            avgNegWeight = 100 / numNegWeights;
        }
        weightMap = new ArrayList<Integer>();
        for (int i = 0; i < numFeatures; ++i)
            weightMap.add(i);
        Collections.shuffle(weightMap);
        for (int i = 0; i < numFeatures; ++i)
            weights.add(0);
        int remainingPos = 100 - numPosWeights * avgPosWeight;
        for (int i = 0; i < numPosWeights; ++i)
            if (i < remainingPos)
                weights.set(weightMap.get(i), avgPosWeight+1);
            else
                weights.set(weightMap.get(i), avgPosWeight);
        int remainingNeg = 100 - numNegWeights * avgNegWeight;
        for (int i = 0; i < numNegWeights; ++i)
            if (i < remainingNeg)
                weights.set(weightMap.get(numPosWeights+i), -avgNegWeight-1);
            else
                weights.set(weightMap.get(numPosWeights+i), -avgNegWeight);
    }
    
    private void perturb40() {
        if (numFeatures < 40) return;
        // Recover original weights
        int remainingPos = 100 - numPosWeights * avgPosWeight;
        for (int i = 0; i < numPosWeights; ++i)
            if (i < remainingPos)
                weights.set(weightMap.get(i), avgPosWeight+1);
            else
                weights.set(weightMap.get(i), avgPosWeight);
        int remainingNeg = 100 - numNegWeights * avgNegWeight;
        for (int i = 0; i < numNegWeights; ++i)
            if (i < remainingNeg)
                weights.set(weightMap.get(numPosWeights+i), -avgNegWeight-1);
            else
                weights.set(weightMap.get(numPosWeights+i), -avgNegWeight);
        // Perturb
        int numMods = numFeatures/40;
        int numPosMods = random.nextInt(numMods+1);
        int numNegMods = numMods - numPosMods;
        if (numPosMods > 0) {
            List<Integer> perturbPos = new ArrayList<Integer>();
            for (int i = 0; i < numPosWeights; ++i)
                perturbPos.add(i);
            Collections.shuffle(perturbPos);
            int modPos = avgPosWeight / 5;
            for (int i = 0; i < numPosMods; ++i) {
                weights.set(weightMap.get(perturbPos.get(i)), weights.get(weightMap.get(perturbPos.get(i)))+modPos);
                weights.set(weightMap.get(perturbPos.get(numPosWeights-i-1)), weights.get(weightMap.get(perturbPos.get(numPosWeights-i-1)))-modPos);
            }
        }
        if (numNegMods > 0) {
            List<Integer> perturbNeg = new ArrayList<Integer>();
            for (int i = 0; i < numNegWeights; ++i)
                perturbNeg.add(i);
            Collections.shuffle(perturbNeg);
            int modNeg = avgNegWeight / 5;
            for (int i = 0; i < numNegMods; ++i) {
                weights.set(weightMap.get(numPosWeights+perturbNeg.get(i)), weights.get(weightMap.get(numPosWeights+perturbNeg.get(i)))+modNeg);
                weights.set(weightMap.get(numPosWeights+perturbNeg.get(numNegWeights-i-1)), weights.get(weightMap.get(numPosWeights+perturbNeg.get(numNegWeights-i-1)))-modNeg);
            }
        }
    }
    
    private void moreEqual() {
        int posMax = 0;
        int posMin = 100;
        int posMaxPosition = 0;
        int posMinPosition = 0;
        int negMax = -100;
        int negMin = 0;
        int negMaxPosition = 0;
        int negMinPosition = 0;
        for (int i = 0; i < numFeatures; i++) {
            int feature = weights.get(i);
            if (feature > posMax) {
                posMax = feature;
                posMaxPosition = i;
            }
            if (feature > 0 && feature < posMin) {
                posMin = feature;
                posMinPosition = i;
            }
            if (feature < negMin) {
                negMin = feature;
                negMinPosition = i;
            }
            if (feature < 0 && feature > negMax) {
                negMax = feature;
                negMaxPosition = i;
            }
        }
        if (numFeatures >= 80) {
            int modification = (int) Math.floor(-negMax * 0.2);
            System.out.println("modificationNeg: " + modification);
            weights.set(negMaxPosition, negMax - modification);
            weights.set(negMinPosition, negMin + modification);
        }
        if (numFeatures >= 40) {
            int modification = (int) Math.floor(posMin * 0.2);
            System.out.println("modificationPos: " + modification);
            weights.set(posMaxPosition, posMax - modification);
            weights.set(posMinPosition, posMin + modification);
        }
    }
    
    private void randomAverageWeights() {
        Random random = new Random(System.currentTimeMillis());
        int posNum = numFeatures / 2;
        int averagePos = 100 / posNum;
        int remainingPos = 100 - averagePos * (posNum - 1);
        System.out.println(averagePos + " * " + (posNum - 1) + " " + remainingPos);
        for (int i = 0; i < posNum - 1; i++) {
            if (remainingPos > averagePos) {
                weights.add(averagePos + 1);
                remainingPos--;
            }
            else
                weights.add(averagePos);
            System.out.println("pos: " + i + " sum: " + weights.get(weights.size() - 1));
        }
        weights.add(remainingPos);
        System.out.println("pos: " + (posNum - 1) + " sum: " + weights.get(weights.size() - 1));
        int averageNeg = -100 / (numFeatures - posNum);
        int remainingNeg = -100 - averageNeg * (numFeatures - posNum - 1);
        System.out.println(averageNeg + " * " + (numFeatures - posNum - 1) + " " + remainingNeg);
        for (int i = posNum; i < numFeatures - 1; i++) {
            if (remainingNeg < averageNeg) {
                weights.add(averageNeg - 1);
                remainingNeg++;
            } else
                weights.add(averageNeg);
            System.out.println("pos: " + i + " sum: " + weights.get(weights.size() - 1));
        }
        if (posNum != numFeatures) {
            weights.add(remainingNeg);
            System.out.println("pos: " + (numFeatures - 1) + " sum: " + weights.get(weights.size() - 1));
        }
        Collections.shuffle(weights);
    }
    
    private void randomWeights() {
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
    }
    
    public String genWeights(int numFeatures) {
        weights = new ArrayList<Integer>();
        this.numFeatures = numFeatures;
        // randomWeights();
        randomAverageWeights();
        // randomAverageWeights40();
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
        moreEqual();
        // perturb40();
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
    boolean repeat = false;
    
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
        if (!repeat) {
            lastFeatures = new double[numFeatures];
            if (numFeatures < 40) {
                
                solveEquation();
            } else {
                if (round == 0) {
                    for (Candidate candidate : candidates)
                        System.out.println(candidate.toString());
                    group = new ArrayList<Integer>();
                    initGroup();
                }
                groupStrategy();
            }
        }
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
    
    private void solveEquation() {
        if (round == 0) {
            candidates = gaussianElimination();
            System.out.println(" **after elimination**");
            for (Candidate candidate : candidates)
                System.out.println(candidate.toString());
        }
        if (round + 20 < numFeatures) {
            lastFeatures[round + 20] = 1;
        } else {
            double[] weights = new double[numFeatures];
            for (int i = round + 20; i >= 0; i--) {
                if (i >= candidates.size())
                    continue;
                Candidate candidate = candidates.get(i);
                if (i >= 20) {
                    weights[i] = candidate.score;
                } else {
                    double score = candidate.score;
                    for (int j = numFeatures - 1; j > i; j--) {
                        score -= candidate.features[j] * weights[j];
                    }
                    if (candidate.features[i] != 0)
                        weights[i] = score / candidate.features[i];
                }
                if (weights[i] > 0)
                    lastFeatures[i] = 1;
            }
            repeat = true;
        }
    }
    
    private List<Candidate> gaussianElimination() {
        List<Candidate> eliminatedCandidates = new ArrayList<Candidate>();
        double[][] features = new double[20][numFeatures];
        double[] scores = new double[20];
        for (int i = 0; i < 20; i++) {
            Candidate candidate = candidates.get(i);
            features[i] = Arrays.copyOf(candidate.features, numFeatures);
            scores[i] = candidate.score;
            System.out.println(candidate.toString());
        }
        eliminatedCandidates.add(new Candidate(scores[0], features[0]));
        for (int i = 0; i < Math.min(19, numFeatures - 1); i++) {
            if (Math.abs(features[i][i]) < 0.001) {
                for (int j = i + 1; j < Math.min(20, numFeatures); j++) {
                    if (Math.abs(features[j][i]) >= 0.001) {
                        double[] temp = Arrays.copyOf(features[j], numFeatures);
                        features[j] = features[i];
                        features[i] = temp;
                        break;
                    }
                }
            }
            if (Math.abs(features[i][i]) < 0.00000000000001) {
                continue;
            }
            for (int j = i + 1; j < Math.min(20, numFeatures); j++) {
                double factor = features[j][i] / features[i][i];
                features[j][i] = 0;
                for (int k = i + 1; k < numFeatures; k++) {
                    features[j][k] -= features[i][k] * factor;
                }
                scores[j] -= scores[i] * factor;
            }
            eliminatedCandidates.add(new Candidate(scores[i + 1], features[i + 1]));
        }
        return eliminatedCandidates;
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
            // for (int i = 0; i < numFeatures; ++i) {
            // if (lastFeatures[i] == 1 && group.get(i) == -1)
            // group.set(i, ++maxInGroup % 18 + 1);
            // }
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
        System.out.println(candidates.get(candidates.size() - 1).toString());
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
