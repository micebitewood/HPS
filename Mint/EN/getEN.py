def getEN(denom, maxChange, N):
	numDenom = len(denom)
	maxDenom = max(denom)
	
	ecn = [999999 for i in range(maxChange+1)]
	ecn[0] = 0
	lastUpdate = 0
	i = 1
	
	while i <= lastUpdate+maxDenom:
		for j in range(numDenom):
			tmp = ecn[(i-denom[j])%100]+1
			if tmp < ecn[i%100]:
				ecn[i%100] = tmp
				lastUpdate = i
		i = i+1
				
	
	en = ecn[:maxChange+1]
	
	for i in range(1, maxChange+1):
		for j in range(1, maxChange+1):
			tmp = ecn[(i+j)%100]+ecn[j]
			if tmp < en[i]:
				en[i] = tmp
	
	print sum(en)+(N-1)*sum(en[::5])
	print en

import sys
denom = [int(sys.argv[1]), int(sys.argv[2]), int(sys.argv[3]), int(sys.argv[4]), int(sys.argv[5])]
N = int(sys.argv[6])

getEN(denom, 99, N)
