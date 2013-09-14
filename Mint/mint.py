sumAll = []
sumFive = []
denominations = []
n = 0
import sys
problemID = int(sys.argv[2])
if problemID == 1:
    filename = 'selectedSumsECN'
elif problemID == 2:
    filename = 'selectedSumsEN'
with open(filename, 'r') as f:
    for line in f:
        temp = line.split(' ', 2)
        sumAll.append(int(temp[0]))
        sumFive.append(int(temp[1]))
        denominations.append(eval(temp[2]))
        n += 1
N = float(sys.argv[1])
minScore = sumAll[0] + (N - 1) * sumFive[0]
minDenominations = denominations[0]
for i in range(1, n):
    score = sumAll[i] + (N - 1) * sumFive[i]
    if score < minScore:
        minScore = score
        minDenominations = denominations[i]
print '<TeamName>'
print minDenominations[0], minDenominations[1], minDenominations[2], minDenominations[3], minDenominations[4]
