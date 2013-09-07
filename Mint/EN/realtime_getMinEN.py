def calculateEnSum(denom, maxChange, N, ecnLim, enLim):
	numDenom = len(denom)
	
	ecn = [999999 for i in range(maxChange*2+1)]
	ecn[0] = 0
	
	for i in range(1, maxChange*2+1):
		if i > maxChange:
			ecn[i] = ecn[i-maxChange-1]
		else:
			for j in range(numDenom):
				if i-denom[j] >= 0:
					tmp = ecn[i-denom[j]]+1
					if tmp < ecn[i]:
						ecn[i] = tmp
			
			if ecn[i] > ecnLim:
				return 999999
	
	en = ecn[:maxChange+1]
	
	for i in range(1, maxChange+1):
		for j in range(1, maxChange+1):
			tmp = ecn[i+j]+ecn[j]
			if tmp < en[i]:
				en[i] = tmp
		
		if en[i] > enLim:
			return 999999
	
	return sum(en)+(N-1)*sum(en[::5])

def getBestDenomEn(maxChange, N, ecnLim, enLim):
	enSumMin = 999999
	
	for i in range(2, maxChange-2):
		for j in range(i+1, maxChange-1):
			for k in range(j+1, maxChange):
				for l in range(k+1, maxChange+1):
					denom = [1, i, j, k, l]
					enSum = calculateEnSum(denom, maxChange, N, ecnLim, enLim)
					
					if enSum < enSumMin:
						enSumMin = enSum
						denomMin = denom
						print enSumMin, denomMin

	print enSumMin, denomMin

import time
tic = time.clock()
maxChange = 99
N = 4
ecnLim = 10
enLim = 10
getBestDenomEn(maxChange, N, ecnLim, enLim)
print time.clock() - tic
