def assign(arr, ind, value, changedNumbers):
    arr[ind] = value
    changedNumbers.add(ind)
    arr[100 - ind] = value
    changedNumbers.add(100 - ind)

import sys
arg = [0 for i in range(5)]
for i in range(1, 6):
    arg[i - 1] = min(int(sys.argv[i]), 100 - int(sys.argv[i]))
arg.sort()
denominations = arg
arr = [0] + [100 for i in range(99)] + [0]
assign(arr, denominations[0], 1, set())
N = 1
for i in range(1, 5):
    n = denominations[i]
    assign(arr, n, 1, set())
    m = denominations[i - 1]
    changedNumbers = set()
    for num in range(m + 1, n):
         for j in range(1, num / 2 + 1):
            temp = arr[j] +  arr[num - j]
            if temp < arr[num]:
                assign(arr, num, temp, changedNumbers)
         if arr[n - num] + 1 < arr[num]:
            assign(arr, num, arr[n - num] + 1, changedNumbers)
    while len(changedNumbers) != 0:
        newChangedNumbers = changedNumbers
        changedNumbers = set()
        for num in range(1, n):
            for j in newChangedNumbers:
                if j < num:
                    temp = arr[j] + arr[num - j]
                elif j > num:
                    temp = arr[j] + arr[j - num]
                else:
                    temp = arr[num]
                if temp < arr[num]:
                    assign(arr, num, temp, changedNumbers)
                if j + num <= n:
                    temp = arr[j] + arr[j + num]
                    if temp < arr[num]:
                        assign(arr, num, temp, changedNumbers)
changedNumbers = set()
changedNumbers.add(0)
for num in range(denominations[-1] + 1, 100):
    for i in range(1, num / 2 + 1):
        temp = arr[i] + arr[num - i]
        if temp < arr[num]:
            assign(arr, num, temp, changedNumbers)
while len(changedNumbers) != 0:
    newChangedNumbers = changedNumbers
    changedNumbers = set()
    for num in range(1, 100):
        for i in newChangedNumbers:
            if i < num:
                temp = arr[i] + arr[num - i]
            elif i > num:
                temp = arr[i] + arr[i - num]
            else:
                temp = arr[num]
            if temp < arr[num]:
                assign(arr, num, temp, changedNumbers)
            if i + num <= 100:
                temp = arr[i] + arr[i + num]
                if temp < arr[num]:
                    assign(arr, num, temp, changedNumbers)
score = 0
for n in range(1, 100):
    if n % 5 == 0:
        score += N * arr[n]
    else:
        score += arr[n]
print score
print denominations
print arr
