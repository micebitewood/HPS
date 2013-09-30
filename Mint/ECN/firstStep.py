def getSums(denominations, right, exactChangeNumberArr, sumOther, sumFive):
    for n in range(denominations[-1] + 1, right):
        for i in denominations:
            exactChangeNumberArr[n] = min(exactChangeNumberArr[n], exactChangeNumberArr[n - i] + 1)
        if n % 5 == 0:
            sumFive += exactChangeNumberArr[n]
        else:
            sumOther += exactChangeNumberArr[n]
    return (sumOther, sumFive)

def preCalc(denominations, exactChangeNumberArr, sumOther, sumFive):
    if len(denominations) == numDenominations:
        newArr = exactChangeNumberArr[:]
        (sumOther, sumFive) = getSums(denominations, 100, newArr, sumOther, sumFive)
        print denominations, sumOther, sumFive
    else:
        for i in range(denominations[-1] + 1, 96 + len(denominations)):
            newArr = exactChangeNumberArr[:]
            newArr[i] = 1
            (newSumOther, newSumFive) = getSums(denominations, i + 1, newArr, sumOther, sumFive)
            newDenominations = denominations + [i]
            preCalc(newDenominations, newArr, newSumOther, newSumFive)

numDenominations = 5
preCalc([1], [0, 1] + [100 for i in range(98)], 1, 0)
