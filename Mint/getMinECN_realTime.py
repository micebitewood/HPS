def getScore(denominations, right, exactChangeNumberArr, score):
    for n in range(denominations[-1] + 1, right):
        for i in denominations:
            exactChangeNumberArr[n] = min(exactChangeNumberArr[n], exactChangeNumberArr[n - i] + 1)
        if exactChangeNumberArr[n] > maxNum:
            return 0
        if n % 5 == 0:
            score += N * exactChangeNumberArr[n]
        else:
            score += exactChangeNumberArr[n]
    return score

def getMinScore(denominations, exactChangeNumberArr, left, right, score, minScore):
    if len(denominations) == denominationsNum:
        newArr = exactChangeNumberArr[:]
        newScore = getScore(denominations, 100, newArr, score)
        if newScore == 0:
            return minScore
        if newScore < minScore:
            minScore = newScore
            print denominations, newScore
    else:
        for i in range(left, right):
            if i > maxNum * denominations[-1]:
                return minScore
            newArr = exactChangeNumberArr[:]
            newArr[i] = 1
            newScore = getScore(denominations, i + 1, newArr, score)
            if newScore == 0:
                return minScore
            newDenominations = denominations + [i]
            minScore = getMinScore(newDenominations, newArr, i + 2, right + 2, newScore, minScore)
    return minScore

import time
tic = time.clock()
import sys
N = int(sys.argv[1])
denominationsNum = 5
maxNum = 7
minScore = 4951
getMinScore([1], [0, 1] + [100 for i in range(98)], 3, 94, 1, minScore)
print time.clock() - tic
