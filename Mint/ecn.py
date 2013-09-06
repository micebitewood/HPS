def calculateEcnSum(denom, maxChange, N):
	numDenom = len(denom)
	
	ecn = [999999 for i in range(maxChange+1)]
	ecn[0] = 0
	ecnSum = 0
	
	for i in range(1, maxChange+1):
		for j in range(numDenom):
			if i-denom[j] >= 0:
				tmp = ecn[i-denom[j]]+1
				if tmp < ecn[i]:
					ecn[i] = tmp
		
		if i % 5 == 0:
			ecnSum = ecnSum + ecn[i]*N
		else:
			ecnSum = ecnSum + ecn[i]
	
	return ecnSum

def getBestDenomEcn(maxChange, N):
	ecnSumMin = maxChange*maxChange*N
	
	for i in range(2, maxChange-2):
		for j in range(i+1, maxChange-1):
			for k in range(j+1, maxChange):
				for l in range(k+1, maxChange+1):
					denom = [1, i, j, k, l]
					ecnSum = calculateEcnSum(denom, maxChange, N)
					
					if ecnSum < ecnSumMin:
						ecnSumMin = ecnSum
						denomMin = denom
						print ecnSumMin, denomMin

	print ecnSumMin, denomMin

getBestDenomEcn(99, 4)
