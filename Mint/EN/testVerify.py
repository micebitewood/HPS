def assign(arr, ind, value, changedNumbers):
    arr[ind] = value
    changedNumbers.add(ind)
    arr[100 - ind] = value
    changedNumbers.add(100 - ind)

import sys
denominations = [int(sys.argv[1]), int(sys.argv[2]), int(sys.argv[3]), int(sys.argv[4]), int(sys.argv[5])]
arr = [100 for i in range(100)]
arr[0] = 0
assign(arr, denominations[0], 1, set())
N = int(sys.argv[6])
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
        newChangedNumbers = changedNumbers.copy()
        changedNumbers = set()
        for num in range(1, n):
            if num not in newChangedNumbers:
                for j in range(1, num):
                    temp = arr[j] + arr[num - j]
                    if temp < arr[num]:
                        assign(arr, num, temp, changedNumbers)
                for j in range(1, n - num + 1):
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
    newChangedNumbers = changedNumbers.copy()
    changedNumbers = set()
    for num in range(1, 100):
        for j in range(1, num):
            temp = arr[j] + arr[num - j]
            if temp < arr[num]:
                assign(arr, num, temp, changedNumbers)
        for j in range(1, 100 - num):
            temp = arr[j] + arr[j + num]
            if temp < arr[num]:
                assign(arr, num, temp, changedNumbers)
score = 0
for n in range(1, 100):
    if n % 5 == 0:
        score += N * arr[n]
    else:
        score += arr[n]
print score
print arr
