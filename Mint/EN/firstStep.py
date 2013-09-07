def calcExactChangeNumberArr(denominations, right, exactChangeNumberArr):
    for n in range(denominations[-1] + 1, right):
        for i in denominations:
            exactChangeNumberArr[n] = min(exactChangeNumberArr[n], exactChangeNumberArr[n - i] + 1)

def getSums(denominations, exactChangeNumberArr):
    sum1 = 0
    sum2 = 0
    
    for n in range(1, 100):
        exchangeNumber = exactChangeNumberArr[n]
        for i in range(1, 100):
            exchangeNumber = min(exchangeNumber, exactChangeNumberArr[(n+i)%100]+exactChangeNumberArr[i])
        
        if n % 5 == 0:
            sum2 += exchangeNumber
        else:
            sum1 += exchangeNumber

    return (sum1, sum2)

def preCalc(denominations, exactChangeNumberArr):
    if len(denominations) == numDenominations:
        newArr = exactChangeNumberArr[:]
        calcExactChangeNumberArr(denominations, 100, newArr)
        (sum1, sum2) = getSums(denominations, newArr)
        print denominations, sum1, sum2
    else:
        for i in range(denominations[-1] + 1, 96 + len(denominations)):
            newArr = exactChangeNumberArr[:]
            newArr[i] = 1
            calcExactChangeNumberArr(denominations, i + 1, newArr)
            newDenominations = denominations + [i]
            preCalc(newDenominations, newArr)

numDenominations = 5
preCalc([1], [0, 1] + [100 for i in range(98)])
