#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <vector>
#include <list>
#include <algorithm>

using namespace std;

const int NUM_TRIALS = 10;
const bool VERBOSE = NUM_TRIALS == 1;

const int NUM_HOSPITALS = 5;
const int NUM_VICTIMS = 300;

//const char FILENAME_HOSPITAL[] = "hospitals";
const char FILENAME_HOSPITAL[] = "cluster_output";
//const char FILENAME_HOSPITAL[] = "eval_in";
const char FILENAME_VICTIMS[] = "victims";

const int TIME_LOAD = 1;
const int TIME_UNLOAD = 1;
const int AMB_CAPACITY = 4;
const int PLAN_LENGTH = 8;
const int NUM_PLANS = 6;
const int PLAN[NUM_PLANS][PLAN_LENGTH] = {
	{30, 20, 10, 5, 3, 2, 1, 0},
	{50, 30, 20, 10, 3, 2, 1, 0},
	{70, 60, 50, 40, 30, 10, 1, 0},
	{50, 30, 20, 10, 3, 2, 1, 0},
	{70, 50, 30, 20, 10, 2, 1, 0},
	{50, 30, 20, 10, 3, 2, 1, 0},
};

const int INF = 99999999;

typedef struct {
	int x;
	int y;
} location;

typedef struct {
	location loc;
	int time;         // Rescue time
	int minHospDist;  // Distance to the closest hospital
	location minHospLoc;
} victim;

typedef struct {
	location loc;
	int time;  // Time that this ambulance becomes available
	int rand;
} ambulance;

typedef struct {
	int id;
	int pr;  // Priority score
	int slack;
} vicdata;

location h[NUM_HOSPITALS];
vector<victim> v;
vector<ambulance> ambInit;  // Initial state that we will copy over to a
vector<ambulance> a;
list<vicdata> q;
int numSaved;

inline int getDist(location a, location b) {
	return abs(a.x-b.x)+abs(a.y-b.y);
}

inline int min(int a, int b) {
	return a<b?a:b;
}

void getData() {
	FILE* in;
	int x, y, z;
	
	in = fopen(FILENAME_HOSPITAL, "r");
	for(int i=0; i<NUM_HOSPITALS; ++i) {
		fscanf(in, "%d , %d , %d", &x, &y, &z);
		
		h[i].x = x;
		h[i].y = y;
		
		for(int j=0; j<z; ++j) {
			ambulance amb;
			amb.loc.x = x;
			amb.loc.y = y;
			amb.time = 0;
			ambInit.push_back(amb);
		}
	}
	fclose(in);
	
	in = fopen(FILENAME_VICTIMS, "r");
	for(int i=0; i<NUM_VICTIMS; ++i) {
		fscanf(in, "%d,%d,%d", &x, &y, &z);
		
		victim vic;
		vic.loc.x = x;
		vic.loc.y = y;
		vic.time = z;
		vic.minHospDist = getDist(vic.loc, h[0]);
		vic.minHospLoc = h[0];
		for(int j=1; j<NUM_HOSPITALS; ++j) {
			int tmpDist = getDist(vic.loc, h[j]);
			if(tmpDist < vic.minHospDist) {
				vic.minHospDist = tmpDist;
				vic.minHospLoc = h[j];
			}
		}
		v.push_back(vic);
	}
	fclose(in);
}

bool compareAmbs(ambulance a1, ambulance a2) {
	return a1.rand < a2.rand;
}

void setup(int seed) {
	srand(seed);
	
	a = ambInit;
	
	for(int i=0; i<a.size(); ++i)
		a[i].rand = rand();
	sort(a.begin(), a.end(), compareAmbs);
	
	q.clear();
	for(int i=0; i<NUM_VICTIMS; ++i) {
		vicdata data;
		data.id = i;
		q.push_back(data);
	}
	
	numSaved = 0;
}

void goSavePeople() {
	while(true) {
		int minAmbTime = INF;
		int iAmb;
		
		for(int i=0; i<a.size(); ++i) {
			if(a[i].time < minAmbTime) {
				iAmb = i;
				minAmbTime = a[i].time;
			}
		}
		
		// For this ambulance
		list<vicdata>::iterator it;
		
		// Remove victims who are too late from the list
		for(it=q.begin(); it!=q.end();)
			if(v[it->id].time - minAmbTime - TIME_LOAD - v[it->id].minHospDist - TIME_UNLOAD < 0) {
				if(VERBOSE)
					printf("Victim %3d at (%2d, %2d) cannot be saved by time %3d\n", it->id, v[it->id].loc.x, v[it->id].loc.y, v[it->id].time);
				it = q.erase(it);
			}
			else
				++it;
		
		if(q.empty())
			break;
		
		int time = minAmbTime;
		location loc = a[iAmb].loc;
		int slack = INF;
		int lastId = 0;
		int saved = 0;
		if(VERBOSE)
			printf("[Amb %2d] time %3d: Leaves (%2d, %2d)\n", iAmb, time, loc.x, loc.y);
		for(int i=0; i<PLAN_LENGTH; ++i) {
			// Calculate priority score
			for(it=q.begin(); it!=q.end(); ++it) {
				switch(iAmb%15) {
					case 10:
					it->pr = v[it->id].time - getDist(v[it->id].loc, a[iAmb].loc) - v[it->id].minHospDist;
					break;
					
					default:
					it->pr = v[it->id].time - v[it->id].minHospDist;
					break;
				}
				it->slack = v[it->id].time - time - getDist(v[it->id].loc, a[iAmb].loc) - TIME_LOAD
						- v[it->id].minHospDist - TIME_UNLOAD;
			}
			
			list<vicdata>::iterator target = q.end();
			int minPr = INF;
			
			for(it=q.begin(); it!=q.end(); ++it) {
				if(it->slack >= PLAN[iAmb%NUM_PLANS][i] && v[it->id].minHospDist + 18 >= getDist(loc, v[it->id].loc) && it->pr < minPr) {
					minPr = it->pr;
					target = it;
				}
			}
			
			it = target;
			
			if(it == q.end()) {
				continue;
			}
			
			if(slack + v[lastId].minHospDist - getDist(loc, v[it->id].loc) - v[it->id].minHospDist - TIME_LOAD - TIME_UNLOAD < 0) {
				continue;
			}
			slack = min(slack + v[lastId].minHospDist, v[it->id].time - time - TIME_UNLOAD) - getDist(loc, v[it->id].loc) - TIME_LOAD - v[it->id].minHospDist;
			time += getDist(loc, v[it->id].loc) + TIME_LOAD;
			loc = v[it->id].loc;
			a[iAmb].loc = loc;
			lastId = it->id;
			if(VERBOSE)
				printf("[Amb %2d] time %3d: Visits (%2d, %2d) and loads victim %3d (rescue time: %d)\n", iAmb, time, loc.x, loc.y, it->id, v[it->id].time);
			it = q.erase(it);
			++numSaved;
			
			if(++saved == AMB_CAPACITY)
				break;
		}
		
		if(slack == INF) {
			a[iAmb].time = INF;
			continue;
		}
		a[iAmb].time = time + v[lastId].minHospDist + TIME_UNLOAD;
		a[iAmb].loc = v[lastId].minHospLoc;
		if(VERBOSE)
			printf("[Amb %2d] time %3d: Arrives(%2d, %2d) and unloads %d victims\n", iAmb, a[iAmb].time, a[iAmb].loc.x, a[iAmb].loc.y, saved);
	}
}

int main(int argc, char** argv) {
	int maxNumSaved = 0;
	int bestSeed;
	int seed = 0;
	int sum = 0;
	
	if(argc > 1)
		seed = atoi(argv[1]);
	
	getData();
	
	for(int i=0; i<NUM_TRIALS; ++i) {
		setup(seed+i);
		goSavePeople();
		
		if(VERBOSE)
			printf("Trial %d: Saved %d victims\n", i, numSaved);
		
		if(numSaved > maxNumSaved) {
			maxNumSaved = numSaved;
			bestSeed = seed+i;
		}
		
		sum += numSaved;
	}
	
	printf("%d\n", maxNumSaved);
	
//	if(VERBOSE)
		printf("Average: %f\n", (double)sum / (double)NUM_TRIALS);
}
