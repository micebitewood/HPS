def getSums(denominations, right, exactChangeNumberArr, sum1, sum2):
    for n in range(denominations[-1] + 1, right):
        for i in denominations:
            exactChangeNumberArr[n] = min(exactChangeNumberArr[n], exactChangeNumberArr[n - i] + 1)
        if n % 5 == 0:
            sum2 += exactChangeNumberArr[n]
        else:
            sum1 += exactChangeNumberArr[n]
    return (sum1, sum2)

def preCalc(denominations, exactChangeNumberArr, sum1, sum2):
    if len(denominations) == numDenominations:
        newArr = exactChangeNumberArr[:]
        (sum1, sum2) = getSums(denominations, 100, newArr, sum1, sum2)
        print denominations, sum1, sum2
    else:
        for i in range(denominations[-1] + 1, 96 + len(denominations)):
            newArr = exactChangeNumberArr[:]
            newArr[i] = 1
            (newSum1, newSum2) = getSums(denominations, i + 1, newArr, sum1, sum2)
            newDenominations = denominations + [i]
            preCalc(newDenominations, newArr, newSum1, newSum2)

numDenominations = 5
preCalc([1], [0, 1] + [100 for i in range(98)], 1, 0)
