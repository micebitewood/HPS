lst = []
sumDict = dict()
minSumFive = 999999
sumOtherOfMinSumFive = 0
with open('sums', 'r') as f:
    for line in f:
        temp = line.rsplit(' ', 2)
        denominations = eval(temp[0])
        sumOther = int(temp[1])
        sumFive = int(temp[2])
        sumAll = sumOther + sumFive
        if (sumAll, sumFive) not in sumDict:
            sumDict[(sumAll, sumFive)] = denominations
keys = sumDict.keys()
keys.sort()
newKeys = []
i = 0
newKeys += [keys[0]]
for j in range(1, len(keys)):
    if keys[i][1] > keys[j][1]:
        newKeys += [keys[j]] 
        i = j
for key in newKeys:
    print key[0], key[1], sumDict[key]
