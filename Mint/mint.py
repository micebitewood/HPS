#!/usr/bin/env python
ECN = dict()
ECN[(329, 65)] = [1, 5, 16, 23, 33]
ECN[(331, 60)] = [1, 5, 12, 20, 37]
ECN[(332, 57)] = [1, 3, 10, 24, 30]
ECN[(333, 54)] = [1, 5, 11, 19, 40]
ECN[(335, 48)] = [1, 5, 7, 25, 40]
ECN[(336, 47)] = [1, 5, 18, 25, 40]
ECN[(340, 45)] = [1, 5, 19, 25, 40]
ECN[(400, 40)] = [1, 5, 10, 30, 45]
EN = dict()
EN[(227, 39)] = [1, 4, 10, 23, 35]
EN[(228, 36)] = [1, 4, 10, 25, 41]
EN[(232, 32)] = [1, 5, 8, 25, 40]
EN[(268, 30)] = [1, 10, 15, 20, 25]

import sys
problemID = int(sys.argv[2])
data = ECN
if problemID == 2:
    data = EN
elif problemID != 1:
    print "the problem number should be either 1 or 2"
    sys.exit(0)
N = float(sys.argv[1])
minScore = 400 + 65 * N
minDenominations = []
for (sumAll, sumFive) in data.keys():
    score = sumAll + (N - 1) * sumFive
    if score < minScore:
        minScore = score
        minDenominations = data[(sumAll, sumFive)]
print 'jj'
print minDenominations[0], minDenominations[1], minDenominations[2], minDenominations[3], minDenominations[4]
