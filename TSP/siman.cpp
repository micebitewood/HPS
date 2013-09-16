#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <time.h>

const int N = 1000;
const char FILENAME_IN[] = "tsp1000";
const char FILENAME_OUT[] = "output";

const int NUM_TRIALS = 2;
const int NUM_ITERATIONS = 10000; // 30000;
const int NUM_STEPS = 10000;

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

double d[N*N];	// Distance between two cities
int mst[N-1];	// MST edges

inline int randRange(int r) { return rand() % r; }
inline void swap(int &a, int &b) { int tmp = a; a = b; b = tmp; }

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
	for(int i=0; i<N; ++i)
		for(int j=0; j<N; ++j)
			d[i*N+j] = sqrt(pow(double(p[i].x-p[j].x), 2.0) + pow(double(p[i].y-p[j].y), 2.0) + pow(double(p[i].z-p[j].z), 2.0));
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
	
	printf("Total travel distance: %f\n", calculateTravelDist(p));
}

void writePath(int p[N]) {
	FILE* out = fopen(FILENAME_OUT, "w");
	
	fprintf(out, "[");
	
	for(int i=0; i<N-1; ++i)
		fprintf(out, "%d, ", p[i]+1);
	
	fprintf(out, "%d]\n", p[N-1]+1);
	fclose(out);
}

double runSimulatedAnnealing(int mp[N]) {
	double m = INF;
	bool f[N];
	int p[N];
	double tmp;
	
	for(int trial=0; trial<NUM_TRIALS; ++trial) {
		int count = 0;
		for(int i=0; i<N; ++i)
			f[i] = false;
		
		dfs(randRange(N), &count, f, p);
		tmp = calculateTravelDist(p);
		
		if(tmp < m) {
			m = tmp;
			for(int j=0; j<N; ++j)
				mp[j] = p[j];
		}
		
		double t = TEMP_INIT;
		
		while(t > TEMP_FINAL) {
			for(int i=0; i<NUM_ITERATIONS; ++i) {
				int n1 = randRange(N);
				int n2 = (n1+randRange(N-3)+2)%N;
				
				if(n1 > n2)
					swap(n1, n2);
				
				double diff = d[p[n2]*N+p[(n1+N-1)%N]] + d[p[n1]*N+p[(n2+1)%N]]
						  - d[p[n1]*N+p[(n1+N-1)%N]] - d[p[n2]*N+p[(n2+1)%N]];
				
//				printf("%f\n", diff);
				if(diff < 0.0 || diff/m < 0.2 && exp(-diff/m/t)*double(RAND_MAX) > double(rand())) {
					for(int j=0; j<(n2-n1+1)/2; ++j)
						swap(p[n1+j], p[n2-j]);
					
					tmp += diff;
					
					if(tmp < m) {
						m = tmp;
						for(int j=0; j<N; ++j)
							mp[j] = p[j];
					}
				}
			}
			
			t *= TEMP_MULT;
		}
		
		printf("Trial %d: %f (min: %f)\n", trial, tmp, m);
//		displayPath(p);
	}
	
	return m;
}

int main() {
	srand(time(NULL));
	readData();
	calculateMST();
	
	int p[N];
//	double m = calculateMinDFS(p);
	double m = runSimulatedAnnealing(p);
//	displayPath(p);
	writePath(p);
	
	return 0;
}
