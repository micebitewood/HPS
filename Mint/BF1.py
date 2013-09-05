def getScore(denominations, right, exchangeNumberArr, score):
    for n in range(denominations[-1] + 1, right):
        for i in denominations:
            exchangeNumberArr[n] = min(exchangeNumberArr[n], exchangeNumberArr[n - i] + 1)
        if exchangeNumberArr[n] > maxNum:
            return 0
        if n % 5 == 0:
            score += N * exchangeNumberArr[n]
        else:
            score += exchangeNumberArr[n]
    return score

def getMinScore(denominations, exchangeNumberArr, left, right, score, minScore):
    if len(denominations) == denominationsNum:
        newArr = exchangeNumberArr[:]
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
            newArr = exchangeNumberArr[:]
            newArr[i] = 1
            newScore = getScore(denominations, i + 1, newArr, score)
            if newScore == 0:
                return minScore
            newDenominations = denominations + [i]
            minScore = getMinScore(newDenominations, newArr, i + 2, right + 2, newScore, minScore)
    return minScore

import time
tic = time.clock()
N = 4
denominationsNum = 5
maxNum = 5
minScore = 4951
getMinScore([1], [0, 1] + [100 for i in range(98)], 3, 100 - denominationsNum * 2 + 4, 1, minScore)
print time.clock() - tic
