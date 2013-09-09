#include <stdio.h>
#include <stdlib.h>
#include <string.h>

const int INF = 999999;
const int ECN_ARRAY_SIZE = sizeof(int)*100;
inline int min(int a, int b) { return (a<b)?a:b; }
inline int max(int a, int b) { return (a>b)?a:b; }

void calcEcnArr(int* pDenom, int nNumDenoms, int nBound, int* pEcnArr) {
	for(int n=pDenom[nNumDenoms-1]+1; n<nBound; ++n)
		for(int i=0; i<nNumDenoms; ++i)
			pEcnArr[n] = min(pEcnArr[n], pEcnArr[n-pDenom[i]]+1);
}

void getSums(int* pDenom, int* pEcnArr, int* pSum1, int* pSum2) {
	int nMaxDenom = pDenom[0];
	for(int i=1; i<5; ++i)
		nMaxDenom = max(nMaxDenom, pDenom[i]);
	
	int nTmp;
	int iLastUpdate = 0;
	int i = 1;
	while(i<iLastUpdate+nMaxDenom) {
		for(int j=0; j<5; ++j) {
			nTmp = pEcnArr[(i-pDenom[j]+100)%100]+1;
			
			if(nTmp < pEcnArr[i%100]) {
				pEcnArr[i%100] = nTmp;
				iLastUpdate = i;
			}
		}
		
		++i;
	}
	
	int nSum1 = 0;
	int nSum2 = 0;
	int nEn;
	
	*pSum1 = *pSum2 = INF;
	
	for(int n=1; n<100; ++n) {
		nEn = pEcnArr[n];
		
		for(int i=1; i<100; ++i)
			nEn = min(nEn, pEcnArr[(n+i)%100]+pEcnArr[i]);
		
		if(nEn >= INF)
			return;
		
		if(n%5 == 0)
			nSum2 += nEn;
		else
			nSum1 += nEn;
	}
	
	*pSum1 = nSum1;
	*pSum2 = nSum2;
}

void preCalc(int* pDenom, int nNumDenoms, int* pEcnArr, FILE* pOut) {
	if(nNumDenoms == 5) {
		int pNewArr[100];
		memcpy(pNewArr, pEcnArr, ECN_ARRAY_SIZE);
		
		calcEcnArr(pDenom, nNumDenoms, 100, pNewArr);
		
		int nSum1, nSum2;
		getSums(pDenom, pNewArr, &nSum1, &nSum2);
		
		if(nSum1 < INF) {
			fprintf(pOut, "[%d, %d, %d, %d, %d] %d %d\n", pDenom[0], pDenom[1], pDenom[2],
				pDenom[3], pDenom[4], nSum1, nSum2);
		}
	} else {
		if(nNumDenoms == 2) {
			for(int i=0; i<nNumDenoms; ++i)
				printf("%d ", pDenom[i]);
			printf("\n");
		}
		
		for(int i=pDenom[nNumDenoms-1]+1; i<=46+nNumDenoms; ++i) {
			int pNewArr[100];
			memcpy(pNewArr, pEcnArr, ECN_ARRAY_SIZE);
			
			pNewArr[i] = 1;
			calcEcnArr(pDenom, nNumDenoms, i+1, pNewArr);
			
			pDenom[nNumDenoms] = i;
			preCalc(pDenom, nNumDenoms+1, pNewArr, pOut);
		}
	}
}

int main() {
	const int nNumDenoms = 5;
	int pDenom[5];
	int pEcnArr[100];
	
	FILE* pOut = fopen("csums", "w");
	
	for(int n=1; n<=46; ++n) {
		int nMinDenom = n;
		
		pDenom[0] = nMinDenom;
		
		for(int i=1; i<100; ++i)
			pEcnArr[i] = 999999;
		
		pEcnArr[0] = 0;
		pEcnArr[nMinDenom] = 1;
		
		preCalc(pDenom, 1, pEcnArr, pOut);
	}
	
	fclose(pOut);
	return 0;
}
