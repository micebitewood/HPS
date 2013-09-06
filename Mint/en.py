def calculateEn(denom, maxChange):
	numDenom = len(denom)
	
	ecn = [999999 for i in range(maxChange+101)]
	ecn[0] = 0
	
	for i in range(1, maxChange+101):
		if i >= 100:
			ecn[i] = ecn[i-100]
		else:
			for j in range(numDenom):
				if i-denom[j] >= 0:
					tmp = ecn[i-denom[j]]+1
					if tmp < ecn[i]:
						ecn[i] = tmp
	
	en = ecn[:maxChange+1]
	
	for i in range(1, maxChange+1):
		for j in range(1, maxChange+1):
			tmp = ecn[i+j]+ecn[j]
			if tmp < en[i]:
				en[i] = tmp
	
	return en

def calculateSum(en, N):
	return sum(en)+(N-1)*sum(en[::5])

def getBestDenomEn(maxChange, N):
	enSumMin = 999999
	
	for i in range(2, maxChange/2-2):
		for j in range(i+1, maxChange/2-1):
			for k in range(j+1, maxChange/2):
#				for l in range(k+1, maxChange/2+1):
#					denom = [1, i, j, k, l]
					denom = [1, 5, i, j, k]
					en = calculateEn(denom, maxChange)
					enSum = calculateSum(en, N)
					
					if enSum < enSumMin:
						enSumMin = enSum
						denomMin = denom
						print enSumMin, denomMin

	print enSumMin, denomMin

import time
tic = time.clock()
maxChange = 99
N = 4
getBestDenomEn(maxChange, N)
print time.clock() - tic
