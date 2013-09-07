def exchangeNumber(numberArr):
    sortedList = [[]]
    for i in range(1, len(numberArr)):
        n = numberArr[i]
        while len(sortedList) - 1 < n:
            sortedList += [[]]
        sortedList[n] += [i]
    change = 0
    for i in range(1, len(sortedList) - 1):
        for j in range(1, len(sortedList) - i):
            for price in sortedList[i]:
                for change in sortedList[j]:
                    if price > change:
                        if i + j < numberArr[price - change]:
                            change += 1
                            numberArr[price - change] = i + j
    sumFive = 0
    sumOther = 0
    for n in range(1, 100):
        if n % 5 == 0:
            sumFive += numberArr[n]
        else:
            sumOther += numberArr[n]
    return (sumOther, sumFive)

def calc(denominations, right, exactChangeNumberArr):
    for n in range(denominations[-1] + 1, right):
        for i in denominations:
            exactChangeNumberArr[n] = min(exactChangeNumberArr[n], exactChangeNumberArr[n - i] + 1)
    

def preCalc(denominations, exactChangeNumberArr):
    if len(denominations) == numDenominations:
        newArr = exactChangeNumberArr[:]
        calc(denominations, 100, newArr)
        for i in range(1, 100):
            newArr[i] = min(1 + newArr[100 - i], newArr[i])
        (sumOther, sumFive) = exchangeNumber(newArr)
        print denominations, sumOther, sumFive
    else:
        for i in range(denominations[-1] + 1, 96 + len(denominations)):
            newArr = exactChangeNumberArr[:]
            newArr[i] = 1
            calc(denominations, i + 1, newArr)
            newDenominations = denominations + [i]
            preCalc(newDenominations, newArr)

numDenominations = 5
preCalc([1], [0, 1] + [100 for i in range(98)])
