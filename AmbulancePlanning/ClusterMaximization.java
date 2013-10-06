import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ClusterMaximization {
    private Map<Victim, Integer> victims;
    private List<Integer> hospitals;
    private int minX = 100;
    private int maxX = -100;
    private int minY = 100;
    private int maxY = -100;

    public Map<Victim, Integer> getVictims() {
        return victims;
    }

    public List<Integer> getHospitals() {
        return hospitals;
    }

    public int[] getRange() {
        int[] range = new int[4];
        range[0] = minX;
        range[1] = maxX;
        range[2] = minY;
        range[3] = maxY;
        return range;
    }

    public void parseFile(String input) throws IOException {

        victims = new HashMap<Victim, Integer>();
        hospitals = new ArrayList<Integer>();
        int num = 0;
        boolean startVictims = false;
        boolean startHospitals = false;

        BufferedReader br = new BufferedReader(new FileReader(input));
        String line;
        while ((line = br.readLine()) != null) {
            String content = line.trim().toLowerCase();
            if (content.equals(""))
                continue;
            if (content.contains("person"))
                startVictims = true;
            else if (content.contains("hospital"))
                startHospitals = true;
            else if (startHospitals)
                hospitals.add(Integer.parseInt(content));
            else if (startVictims) {
                String[] person = content.split(",");
                int locX = Integer.parseInt(person[0]);
                int locY = Integer.parseInt(person[1]);
                int time = Integer.parseInt(person[2]);
                if (locX < minX)
                    minX = locX;
                else if (locX > maxX)
                    maxX = locX;
                if (locY < minY)
                    minY = locY;
                else if (locY > maxY)
                    maxY = locY;
                victims.put(new Victim(locX, locY, num), time);
                num++;
            }
        }
    }

    public void print() {
        for (Entry<Victim, Integer> entry : victims.entrySet()) {
            System.out.println(entry.getKey() + ":" + entry.getValue());
        }
        for (int number : hospitals) {
            System.out.println(number);
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        ClusterMaximization cm = new ClusterMaximization();
        cm.parseFile("input");
        List<Callable<Paths>> lst = new ArrayList<Callable<Paths>>();
        for (int i = 0; i < 1; i++) {
            lst.add(new MultiRun(cm));
        }
        ExecutorService executorService = Executors.newFixedThreadPool(lst.size());
        List<Future<Paths>> tasks = executorService.invokeAll(lst);
        executorService.shutdown();
    }
}

class MultiRun implements Callable<Paths> {
    private Location[] locations;// used for clustering
    private List<Integer> ambulanceNumbers;// used for clustering
    private Ambulance[] ambulances;// used for locating
    private Map<Victim, Integer> victims;// Victim->time, used for both parts
    private int minX;
    private int maxX;
    private int minY;
    private int maxY;
    private List<Victim>[] victimsInLocations;// used for clustering

    private int getAbsSum(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    private void pairUp() {
        // locations and ambulanceNumbers
        Integer[] ambulanceNumbersArray = new Integer[ambulanceNumbers.size()];
        ambulanceNumbers.toArray(ambulanceNumbersArray);
        for (int i = 0; i < ambulanceNumbersArray.length; i++) {
            for (int j = 0; j < ambulanceNumbersArray.length; j++) {
                if (i == j)
                    continue;
                if (ambulanceNumbersArray[j] < ambulanceNumbersArray[i]) {
                    if (locations[j].numOfPeople > locations[i].numOfPeople) {
                        Location location = locations[j];
                        locations[j] = locations[i];
                        locations[i] = location;
                    }
                } else if (ambulanceNumbersArray[j] > ambulanceNumbersArray[i]) {
                    if (locations[j].numOfPeople < locations[i].numOfPeople) {
                        Location location = locations[j];
                        locations[j] = locations[i];
                        locations[i] = location;
                    }
                }
            }
        }
        for (int i = 0; i < locations.length; i++) {
            System.out.println("hospitals: " + locations[i].locX + ", " + locations[i].locY + " " +
                    ambulanceNumbersArray[i]);
        }
        int num = 0;
        for (int i = 0; i < ambulanceNumbersArray.length; i++) {
            num += ambulanceNumbersArray[i];
        }
        ambulances = new Ambulance[num];
        num = 0;
        for (int i = 0; i < ambulanceNumbersArray.length; i++) {
            for (int j = 0; j < ambulanceNumbersArray[i]; j++) {
                ambulances[num] = new Ambulance(locations[i].locX, locations[i].locY, 0, num);
                num++;
            }
        }
    }

    private boolean move(Location location, List<Victim> victimsInLocation, int[] directions) {
        boolean moved = false;
        int indX = -1;
        int indY = -1;

        if (directions[0] > directions[1] + 4) {// right
            indX = 0;
            moved = true;
        } else if (directions[0] < directions[1] - 4) {// left
            indX = 1;
            moved = true;
        }
        if (directions[2] > directions[3] + 4) {// up
            indY = 2;
            moved = true;
        } else if (directions[2] < directions[3] - 4) {// down
            indY = 3;
            moved = true;
        }
        if (moved) {
            for (Victim victim : victimsInLocation) {
                if (indX != -1) {
                    if (victim.locX == location.locX - 2 * indX + 1)
                        directions[indX] -= 1;
                    else if (victim.locX == location.locX)
                        directions[1 - indX] += 1;
                }
                if (indY != -1) {
                    if (victim.locY == location.locY - indY * 2 + 5)
                        directions[indY] -= 1;
                    else if (victim.locY == location.locY)
                        directions[5 - indY] += 1;
                }
            }
            if (indX != -1)
                location.locX = location.locX - 2 * indX + 1;
            if (indY != -1)
                location.locY = location.locY - indY * 2 + 5;
            return true;
        }
        return false;
    }

    private void adjust() {
        for (int i = 0; i < ambulanceNumbers.size(); i++) {
            Location location = locations[i];
            List<Victim> victimsInLocation = victimsInLocations[i];
            int[] directions = new int[4];// right, left, up, down
            for (Victim victim : victimsInLocation) {
                if (victim.locX > location.locX)
                    directions[0]++;
                else if (victim.locX < location.locX)
                    directions[1]++;
                if (victim.locY > location.locY)
                    directions[2]++;
                else if (victim.locY < location.locY)
                    directions[3]++;
            }
            int count = 0;

            while (count < 200) {
                count += 1;
                if (!move(location, victimsInLocation, directions))
                    break;
            }
        }
    }

    private void cluster() {
        Random random = new Random();
        random.setSeed(System.currentTimeMillis());
        locations = new Location[ambulanceNumbers.size()];
        victimsInLocations = new ArrayList[ambulanceNumbers.size()];
        for (int i = 0; i < ambulanceNumbers.size(); i++) {
            locations[i] = new Location(random.nextInt(maxX - minX) + minX, random.nextInt(maxY - minY) + minY, 0);
        }
        boolean needClustering = true;
        while (needClustering) {
            needClustering = false;
            int[][] sumXY = new int[ambulanceNumbers.size()][3]; // sumX, sumY, #victims
            for (int i = 0; i < ambulanceNumbers.size(); i++) {
                victimsInLocations[i] = new ArrayList<Victim>();
            }
            for (Victim victim : victims.keySet()) {
                int minDist = 100000;
                int closestPoint = 0;
                for (int i = 0; i < ambulanceNumbers.size(); i++) {
                    Location location = locations[i];
                    int dist = getAbsSum(victim.locX, victim.locY, location.locX, location.locY);
                    if (dist < minDist) {
                        minDist = dist;
                        closestPoint = i;
                    }
                }
                sumXY[closestPoint][0] += victim.locX;
                sumXY[closestPoint][1] += victim.locY;
                sumXY[closestPoint][2] += 1;
                victimsInLocations[closestPoint].add(victim);
            }
            for (int i = 0; i < ambulanceNumbers.size(); i++) {
                if (sumXY[i][2] != 0) {
                    sumXY[i][0] /= sumXY[i][2];
                    sumXY[i][1] /= sumXY[i][2];
                }
                if (sumXY[i][0] != locations[i].locX) {
                    needClustering = true;
                }
                if (sumXY[i][1] != locations[i].locY) {
                    needClustering = true;
                }
                locations[i] = new Location(sumXY[i][0], sumXY[i][1], sumXY[i][2]);
            }
        }
        adjust();
        pairUp();

    }

    public MultiRun(ClusterMaximization cm) {
        ambulanceNumbers = new ArrayList<Integer>();
        ambulanceNumbers.addAll(cm.getHospitals());
        victims = new HashMap<Victim, Integer>();
        victims.putAll(cm.getVictims());

        int[] range = cm.getRange();
        minX = range[0];
        maxX = range[1];
        minY = range[2];
        maxY = range[3];
    }

    @Override
    public Paths call() throws Exception {
        cluster();
        Paths paths = pathSearch();
        return paths;
    }

    private Paths pathSearch() {
        // TODO Auto-generated method stub
        return null;
    }

}

class Ambulance {
    public int locX;
    public int locY;
    public int time;
    public int num;

    public Ambulance(int locX, int locY, int time, int num) {
        this.locX = locX;
        this.locY = locY;
        this.time = time;
        this.num = num;
    }
}

class Paths {
    public Map<Integer, List<Path>> paths;// numOfAmbulance->List<Path>
    public int savedCount;

    class Path {
        public int[] victim;// (numOfvictim)
        public int[] hospital;// (locX, locY)

    }
}

class Victim {
    public int locX;
    public int locY;
    public int num;

    public Victim(int locX, int locY, int num) {
        this.locX = locX;
        this.locY = locY;
        this.num = num;
    }

    @Override
    public String toString() {
        return "(" + locX + ", " + locY + ", " + num + ")";
    }
}

class Location {
    public int locX;
    public int locY;
    public int numOfPeople;

    public Location(int locX, int locY, int numOfPeople) {
        this.locX = locX;
        this.locY = locY;
        this.numOfPeople = numOfPeople;
    }
}
