def printResult(score, denominations):
    print
    print "score:", score
    print "denominations sets:", denominations
    print

sumAll = []
sumFive = []
denominations = []
n = 0
with open('selectedSums', 'r') as f:
    for line in f:
        temp = line.split(' ', 2)
        sumAll.append(int(temp[0]))
        sumFive.append(int(temp[1]))
        denominations.append(eval(temp[2]))
        n += 1
import sys
N = int(sys.argv[1])
minScore = sumAll[0] + (N - 1) * sumFive[0]
printResult(minScore, denominations[0])
for i in range(1, n):
    score = sumAll[i] + (N - 1) * sumFive[i]
    if score <= minScore:
        minScore = score
        printResult(minScore, denominations[i])
