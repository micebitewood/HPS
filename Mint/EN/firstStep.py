def assign(arr, ind, value, changedNumbers):
    arr[ind] = value
    changedNumbers.add(ind)
    arr[100 - ind] = value
    changedNumbers.add(100 - ind)

def exchangeNumber(arr):
    sumFive = 0
    sumOther = 0
    for n in range(1, 100):
        if arr[n] == 100:
            return (0, 0)
        if n % 5 == 0:
            sumFive += arr[n]
        else:
            sumOther += arr[n]
    return (sumOther, sumFive)

def calc(denominations, right, exchangeNumberArr):
    n = denominations[-1]
    changedNumbers = set()
    if right != 100:
        assign(exchangeNumberArr, right, 1, changedNumbers)
    for num in range(n + 1, right):
        for i in range(1, num / 2 + 1):
            temp = exchangeNumberArr[i] + exchangeNumberArr[num - i]
            if temp < exchangeNumberArr[num]:
                assign(exchangeNumberArr, num, temp, changedNumbers)
        if exchangeNumberArr[right - num] + 1 < exchangeNumberArr[num]:
            assign(exchangeNumberArr, num, exchangeNumberArr[right - num] + 1, changedNumbers)
    if right == 100:
        changedNumbers.add(0)
    while len(changedNumbers) != 0:
        newChangedNumbers = changedNumbers.copy()
        changedNumbers = set()
        for num in range(1, right):
            for i in newChangedNumbers:
                if i < num:
                    temp = exchangeNumberArr[i] + exchangeNumberArr[num - i]
                    if temp < exchangeNumberArr[num]:
                        assign(exchangeNumberArr, num, temp, changedNumbers)
                elif i > num:
                    temp = exchangeNumberArr[i] + exchangeNumberArr[i - num]
                    if temp < exchangeNumberArr[num]:
                        assign(exchangeNumberArr, num, temp, changedNumbers)
                if i + num < right + 1 :
                    temp = exchangeNumberArr[i] + exchangeNumberArr[i + num]
                    if temp < exchangeNumberArr[num]:
                        assign(exchangeNumberArr, num, temp, changedNumbers)

def preCalc(denominations, exchangeNumberArr):
    if len(denominations) == numDenominations:
        newArr = exchangeNumberArr[:]
        calc(denominations, 100, newArr)
        (sumOther, sumFive) = exchangeNumber(newArr)
        if sumOther != 0 and sumFive != 0:
            print denominations, sumOther, sumFive
    else:
        if len(denominations) == 0:
            for i in range(1, 47):
                newArr = exchangeNumberArr[:]
                assign(newArr, i, 1, set())
                newDenominations = denominations + [i]
                preCalc(newDenominations, newArr)
        else:
            for i in range(denominations[-1] + 1, 51):
                newArr = exchangeNumberArr[:]
                calc(denominations, i, newArr)
                newDenominations = denominations + [i]
                preCalc(newDenominations, newArr)

numDenominations = 5
preCalc([], [0] + [100 for i in range(99)] + [0])
