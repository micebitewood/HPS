def getEN(denom, maxChange, N):
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
	
	en = ecn[:maxChange+1]
	
	for i in range(1, maxChange+1):
		for j in range(1, maxChange+1):
			tmp = ecn[i+j]+ecn[j]
			if tmp < en[i]:
				en[i] = tmp
	
	print sum(en)+(N-1)*sum(en[::5])
	print en

import sys
denom = [int(sys.argv[1]), int(sys.argv[2]), int(sys.argv[3]), int(sys.argv[4]), int(sys.argv[5])]
N = int(sys.argv[6])

getEN(denom, 99, N)
