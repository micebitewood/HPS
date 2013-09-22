#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <time.h>
#include <pthread.h>

const int N = 1000;
const char FILENAME_IN[] = "input";
const char FILENAME_OUT[] = "output";

const int NUM_THREADS = 8;

const int NUM_TRIALS = 3;
const int NUM_ITERATIONS_UNTANGLE[NUM_THREADS] = {30000, 30000, 30000, 30000, 28000, 28000, 25000, 25000};
const int NUM_ITERATIONS_INSERT[NUM_THREADS] = {10000, 10000, 8000, 8000, 8000, 8000, 7000, 7000};
const int NUM_STEPS = 10000;
const int CUT = 6;
const int TIME_LIMIT = 117;

const double TEMP_INIT = 0.005;
const double TEMP_FINAL = 0.000001;
const double TEMP_MULT = pow(TEMP_FINAL/TEMP_INIT, 1.0/double(NUM_STEPS));

const int INDEX_INVALID = N*N;
const double INF = 99999999.9;

typedef struct {
	int x;
	int y;
	int z;
} pos;

typedef struct {
	int* p;
	int tID;
	unsigned int seed;
} thread_data;

double d[N*N];			// Distance between two cities
int mst[N-1];			// MST edges
struct timespec start;	// Timer start

inline int randRange(int r, unsigned int* seedp) { return rand_r(seedp) % r; }
inline void swap(int &a, int &b) { int tmp = a; a = b; b = tmp; }
int getElapsedTime() {
	struct timespec now;
	clock_gettime(CLOCK_MONOTONIC, &now);
	return now.tv_sec - start.tv_sec;
}

void readData() {
	pos p[N];
	FILE* in = fopen(FILENAME_IN, "r");
	int index;
	
	for(int i=0; i<N; ++i) {
		fscanf(in, "%d %d %d %d", &index, &p[i].x, &p[i].y, &p[i].z);
//		if(index != i+1)
//			printf("This is not cool...\n");
	}
	
	fclose(in);
	
	// Pre-calculate Euclidean distances
	for(int i=0; i<N-1; ++i)
		for(int j=i+1; j<N; ++j)
			d[i*N+j] = d[j*N+i] = sqrt(pow(double(p[i].x-p[j].x), 2.0) + pow(double(p[i].y-p[j].y), 2.0) + pow(double(p[i].z-p[j].z), 2.0));
	
	printf("Done calculating distances! - %d\n", getElapsedTime());
}

void calculateMST() {
	bool a[N];	// Indicates whether each city is added
	
	for(int i=1; i<N; ++i)
		a[i] = false;
	
	a[0] = true;
	for(int i=0; i<N-1; ++i) {
		double m = INF;
		int e;
		
		for(int j=0; j<N; ++j) {
			if(!a[j]) continue;
			
			for(int k=1; k<N; ++k) {
				if(a[k]) continue;
				
				if(d[j*N+k] < m) {
					e = j*N+k;
					m = d[e];
				}
			}
		}
		
		mst[i] = e;
		a[e%N] = true;
	}
	
	printf("Done calculating MST! - %d\n", getElapsedTime());
}

void dfs(int node, int* count, bool f[N], int p[N]) {
	f[node] = true;
	p[(*count)++] = node;
	
	for(int i=0; i<N-1; ++i)
		if(mst[i]/N == node && !f[mst[i]%N] || mst[i]%N == node && !f[mst[i]/N])
			dfs(mst[i]/N+mst[i]%N-node, count, f, p);
}

double calculateTravelDist(int p[N]) {
	double s = 0.0;
	
	for(int i=0; i<N-1; ++i)
		s += d[p[i]*N+p[i+1]];
	
	s += d[p[N-1]*N+p[0]];
	
	return s;
}

double calculateMinDFS(int mp[N]) {
	bool f[N];
	double m = INF;
	
	for(int i=0; i<N; ++i) {
		for(int j=0; j<N; ++j)
			f[j] = false;
		
		int count = 0;
		int p[N];
		dfs(i, &count, f, p);
		double tmp = calculateTravelDist(p);
		
		if(tmp < m) {
			m = tmp;
			for(int j=0; j<N; ++j)
				mp[j] = p[j];
		}
	}
	
	return m;
}

void displayPath(int p[N]) {
//	for(int i=0; i<N; ++i)
//		printf("Travel from %d to %d: %f\n", p[i]+1, p[(i+1)%N]+1, d[p[i]*N+p[(i+1)%N]]);
	
	printf("Total travel distance: %f - %d\n", calculateTravelDist(p), getElapsedTime());
}

void writePath2(int p[N]) {
	FILE* out = fopen(FILENAME_OUT, "w");
	
	fprintf(out, "[");
	
	for(int i=0; i<N-1; ++i)
		fprintf(out, "%d, ", p[i]+1);
	
	fprintf(out, "%d]\n", p[N-1]+1);
	fclose(out);
}

void writePath(int p[N]) {
	FILE* out = fopen(FILENAME_OUT, "w");
	
	fprintf(out, "%d", p[0]+1);
	for(int i=0; i<N-1; ++i)
		fprintf(out, " %d", p[i]+1);
	
	fclose(out);
}

double untangle1(int p[N], double dist) {
	double diff;
	
	for(int i=0; i<N; ++i) {
		diff = d[p[i]*N+p[(i+2)%N]] + d[p[(i+1)%N]*N+p[(i+3)%N]] - d[p[i]*N+p[(i+1)%N]] - d[p[(i+2)%N]*N+p[(i+3)%N]];
		
		if(diff < 0.0) {
			swap(p[(i+1)%N], p[(i+2)%N]);
			dist += diff;
		}
	}
	
	return dist;
}

double untangle2(int p[N], double dist) {
	double diff;
	
	for(int i=0; i<N; ++i) {
		diff = d[p[i]*N+p[(i+3)%N]] + d[p[(i+1)%N]*N+p[(i+4)%N]] - d[p[i]*N+p[(i+1)%N]] - d[p[(i+3)%N]*N+p[(i+4)%N]];
		
		if(diff < 0.0) {
			swap(p[(i+1)%N], p[(i+3)%N]);
			dist += diff;
		}
	}
	
	return dist;
}

void* runSimulatedAnnealing(void* data) {
	int* mp = ((thread_data*)data)->p;
	int tID = ((thread_data*)data)->tID;
	unsigned int seed = ((thread_data*)data)->seed;
	srand(seed);
	
	double m = INF;
	bool f[N];
	int p[N];
	double dist;
	int count;
	double diff;
	bool update;
	int noUpdate;
	int n1, n2;
	
	for(int trial=0; trial<NUM_TRIALS && getElapsedTime()<TIME_LIMIT; ++trial) {
		count = 0;
		noUpdate = 0;
		for(int i=0; i<N; ++i)
			f[i] = false;
		
		dfs(randRange(N, &seed), &count, f, p);
		dist = calculateTravelDist(p);
		dist = untangle1(p, dist);
		dist = untangle2(p, dist);
		dist = untangle1(p, dist);
		dist = untangle2(p, dist);
		
		if(dist < m) {
			m = dist;
			for(int j=0; j<N; ++j)
				mp[j] = p[j];
		}
		
		double t = TEMP_INIT;
		
		while(t > TEMP_FINAL) {
			update = false;
			
			for(int i=0; i<NUM_ITERATIONS_UNTANGLE[tID]; ++i) {
				// Untangle phase
				n1 = randRange(N, &seed);
				n2 = (n1+randRange(N-5, &seed)+3)%N;
				
				if(n1 > n2)
					swap(n1, n2);
				
				diff = d[p[n2]*N+p[(n1+N-1)%N]] + d[p[n1]*N+p[(n2+1)%N]]
					 - d[p[n1]*N+p[(n1+N-1)%N]] - d[p[n2]*N+p[(n2+1)%N]];
				
				if(diff < 0.0 || diff/m < 0.2 && exp(-diff/m/t)*double(RAND_MAX) > double(rand_r(&seed))) {
					for(int j=0; j<(n2-n1+1)/2; ++j)
						swap(p[n1+j], p[n2-j]);
					
					dist += diff;
					
					if(dist < m) {
						m = dist;
						for(int j=0; j<N; ++j)
							mp[j] = p[j];
					}
					
					update = true;
				}
			}
			
			for(int i=0; i<NUM_ITERATIONS_INSERT[tID]; ++i) {
				// Insert phase
				n1 = randRange(N, &seed);
				n2 = (n1+randRange(N-3, &seed)+2)%N;
				
				diff = d[p[(n1+N-1)%N]*N+p[(n1+1)%N]] + d[p[n2]*N+p[n1]] + d[p[n1]*N+p[(n2+1)%N]]
					 - d[p[(n1+N-1)%N]*N+p[n1]] - d[p[n1]*N+p[(n1+1)%N]] - d[p[n2]*N+p[(n2+1)%N]];
				
				if(diff < 0.0 || diff/m < 0.2 && exp(-diff/m/t)*double(RAND_MAX) > double(rand_r(&seed))) {
					if(n1 > n2)
						n2 += N;
					
					int tmp = p[n1];
					for(int j=n1; j<n2; ++j)
						p[j%N] = p[(j+1)%N];
					p[n2%N] = tmp;
					
					dist += diff;
					
					if(dist < m) {
						m = dist;
						for(int j=0; j<N; ++j)
							mp[j] = p[j];
					}
					
					update = true;
				}
			}
			
			if(update) {
				noUpdate = 0;
				dist = untangle1(p, dist);
				dist = untangle2(p, dist);
				
				if(dist < m) {
					m = dist;
					for(int j=0; j<N; ++j)
						mp[j] = p[j];
				}
			}
			else
				if(++noUpdate > CUT)
					break;
			
			if(getElapsedTime() > TIME_LIMIT)
				break;
			
			t *= TEMP_MULT;
		}
		
		printf("T%d Trial %d: %f (min: %f), temp: %f - %d\n", tID, trial, dist, m, -log10(t), getElapsedTime());
//		displayPath(p);
	}
	
	pthread_exit(NULL);
}

void launchThreads(int p[NUM_THREADS][N]) {
	pthread_t thr[NUM_THREADS];
	thread_data data[NUM_THREADS];
	
	for(int i=0; i<NUM_THREADS; ++i) {
		data[i].p = p[i];
		data[i].tID = i;
		data[i].seed = time(NULL)+i;
		
		int rc = pthread_create(&thr[i], NULL, runSimulatedAnnealing, (void*)&data[i]);
		
		if(rc > 0)
			printf("Error creating thread %d\n", i);
	}
	
	for(int i=0; i<NUM_THREADS; ++i) {
		int rc = pthread_join(thr[i], NULL);
		if(rc > 0)
			printf("Error joining thread %d\n", i);
	}
}

void runMultiSimulatedAnnealing(int mp[N]) {
	int p[NUM_THREADS][N];
	double dist[NUM_THREADS];
	double m = INF;
	int mi;
	
	launchThreads(p);
	
	for(int i=0; i<NUM_THREADS; ++i) {
		dist[i] = calculateTravelDist(p[i]);
		
		if(dist[i] < m) {
			m = dist[i];
			mi = i;
		}
	}
	
	for(int i=0; i<N; ++i)
		mp[i] = p[mi][i];
}

int main() {
	clock_gettime(CLOCK_MONOTONIC, &start);
//	srand(time(NULL));
	readData();
	calculateMST();
	
	int p[N];
	runMultiSimulatedAnnealing(p);
	displayPath(p);
	writePath(p);
	
	return 0;
}
