#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <time.h>

const int N = 5;
const char FILENAME_IN[] = "cluster_output";
const char FILENAME_EVAL_IN[] = "eval_in";
const char FILENAME_EVAL_OUT[] = "eval_out";

const int NUM_TRIALS = 1;
const int NUM_ITERATIONS = 10;
const int NUM_STEPS = 10;

const int JUMP_RANGE = 2;

const double TEMP_INIT = 0.03;
const double TEMP_FINAL = 0.0003;
const double TEMP_MULT = pow(TEMP_FINAL/TEMP_INIT, 1.0/double(NUM_STEPS));

int sol[N*3];

inline int randRange(int r) { return rand() % r; }

void readData() {
	FILE* in = fopen(FILENAME_IN, "r");
	
	for(int i=0; i<N; ++i)
		fscanf(in, "%d , %d , %d", &sol[i*3], &sol[i*3+1], &sol[i*3+2]);
	
	fclose(in);
}

int eval(int s[N*3]) {
	int result;
	
	FILE* out = fopen(FILENAME_EVAL_IN, "w");
	for(int i=0; i<N; ++i)
		fprintf(out, "%d , %d , %d\n", s[i*3], s[i*3+1], s[i*3+2]);
	fclose(out);
	
	system("./greedy > eval_out");
	
	FILE* in = fopen(FILENAME_EVAL_OUT, "r");
	fscanf(in, "%d", &result);
	
	return result;
}

void copySol(int dest[N*3], int src[N*3]) {
	for(int i=0; i<N*3; ++i)
		dest[i] = src[i];
}

void printSol(int s[N*3]) {
	for(int i=0; i<N*3; ++i)
		printf("%d ", s[i]);
	printf("\n");
}

double runSimulatedAnnealing() {
	int saves;
	int maxSaves;
	int cur[N*3];           // Current solution
	int var[N*3];           // Random variation
	int diff;
	
	copySol(cur, sol);
	saves = eval(cur);
	maxSaves = saves;
	
	for(int trial=0; trial<NUM_TRIALS /*&& getElapsedTime()<TIME_LIMIT*/; ++trial) {
		
		double t = TEMP_INIT;
		
		while(t > TEMP_FINAL) {
			
			for(int i=0; i<NUM_ITERATIONS; ++i) {
				int j = randRange(N);
				copySol(var, cur);
				var[j*3] = cur[j*3] + randRange(JUMP_RANGE*2+1) - JUMP_RANGE;
				var[j*3+1] = cur[j*3+1] + randRange(JUMP_RANGE*2+1) - JUMP_RANGE;
				var[j*3+2] = cur[j*3+2];
				
				if(var[j*3] < 1) var[j*3] = 1;
				if(var[j*3] > 100) var[j*3] = 100;
				if(var[j*3+1] < 1) var[j*3+1] = 1;
				if(var[j*3+1] > 100) var[j*3+1] = 100;
				
//				printSol(cur);
//				printSol(var);
				diff = eval(var) - saves;
				
				if(exp((double)(diff-1)/saves/t)*double(RAND_MAX) > double(rand())) {
					printf("%3.1f: max: %3d, cur: %3d, diff: %3d: updated\n", -log10(t), maxSaves, saves, diff);
					copySol(cur, var);
					saves += diff;
					
					if(saves > maxSaves) {
						maxSaves = saves;
						copySol(sol, cur);
					}
				} else {
					printf("%3.1f: max: %3d, cur: %3d, diff: %3d: rejected\n", -log10(t), maxSaves, saves, diff);
				}
			}
			
			t *= TEMP_MULT;
		}
	}
	
	return maxSaves;
}

int main() {
//	srand(0);
	srand(time(NULL));
	readData();
	
	int m = runSimulatedAnnealing();
	printf("%d\n", m);
	
	return 0;
}
