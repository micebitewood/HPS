import sys
denominations = [int(sys.argv[1]), int(sys.argv[2]), int(sys.argv[3]), int(sys.argv[4]), int(sys.argv[5])]
arr = [100 for i in range(100)]
arr[0] = 0
N = int(sys.argv[6])
for n in range(1, 100):
    if arr[n] != 1:
        for i in denominations:
            if n == i:
                arr[i] = 1
                break
            elif n > i:
                temp = arr[n - i] + 1
                if arr[n] > temp:
                    arr[n] = temp
flag = True
while flag:
    flag = False
    for n in range(1, 100):
        if 1 + arr[100 - n] < arr[n]:
            arr[n] = 1 + arr[100 - n]
            flag = True
    for n in range(1, 100):
        if n < 50 and 2 * arr[n] < arr[2 * n]:
            arr[2 * n] = 2 * arr[n]
            flag = True
        for m in range(1, n):
            if n + m < 100 and arr[n] + arr[m] < arr[n + m]:
                arr[n + m] = arr[n] + arr[m]
                flag = True
            if arr[n] + arr[m] < arr[n - m]:
                arr[n - m] = arr[n] + arr[m]
                flag = True

score = 0
for n in range(1, 100):
    if n % 5 == 0:
        score += N * arr[n]
    else:
        score += arr[n]
print score
print arr
