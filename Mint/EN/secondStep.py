lst = []
sumDict = dict()
minSumFive = 9999
sumOtherOfMinSumFive = 0
with open('sums', 'r') as f:
    for line in f:
        temp = line.rsplit(' ', 2)
        denominations = eval(temp[0])
        sumOther = int(temp[1])
        sumFive = int(temp[2])
        sumAll = sumOther + sumFive
        if (sumAll, sumFive) not in sumDict:
            sumDict[(sumAll, sumFive)] = len(lst)
            lst += [[]]
        ind = sumDict[(sumAll, sumFive)]
        lst[ind] += [denominations]
        if sumFive < minSumFive:
            minSumFive = sumFive
            sumAllOfMinSumFive = sumAll
keys = sumDict.keys()
keys.sort()
newKeys = []
i = 0
newKeys += [keys[0]]
for j in range(1, len(keys)):
    if keys[i][1] >= keys[j][1]:
        newKeys += [keys[j]] 
        i = j
for key in newKeys:
    print key[0], key[1], lst[sumDict[key]]
