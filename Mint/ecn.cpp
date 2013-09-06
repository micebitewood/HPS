#include <stdio.h>

int calculateEcnSum(int* denom, int numDenom, int maxChange, int N) {
	int ecn[100] = {0};
	int ecnSum = 0;
	int tmp;
	
	for(int i=1; i<=maxChange; ++i) {
		ecn[i] = 999999;
		
		for(int j=0; j<numDenom; ++j)
			if(i-denom[j] >= 0) {
				tmp = ecn[i-denom[j]]+1;
				
				if(tmp < ecn[i])
					ecn[i] = tmp;
			}
		
		if(i%5 == 0)
			ecnSum = ecnSum + ecn[i]*N;
		else
			ecnSum = ecnSum + ecn[i];
	}
	
	return ecnSum;
}



int main() {
	int N = 4;
	int numDenom = 5;
	int denom[5] = {1};
	int maxChange = 99;
	
	int ecnSum;
	int ecnSumMin = 999999;
	
	for(int i=2; i<=maxChange-3; ++i)
		for(int j=i+1; j<=maxChange-2; ++j)
			for(int k=j+1; k<=maxChange-1; ++k)
				for(int l=k+1; l<=maxChange; ++l) {
					denom[1] = i;
					denom[2] = j;
					denom[3] = k;
					denom[4] = l;
					ecnSum = calculateEcnSum(denom, numDenom, maxChange, N);
					
					if(ecnSum < ecnSumMin) {
						ecnSumMin = ecnSum;
						printf("%d: %d %d %d %d %d\n", ecnSum, denom[0], denom[1], denom[2], denom[3], denom[4]);
					}
			}
	
	return 0;
}
