denominations = [1, 5, 7, 25, 40]
arr = [100 for i in range(100)]
arr[0] = 0
score = 0
N = 2
for i in denominations:
    arr[i] = 1
for n in range(1, 100):
    if arr[n] != 1:
        for i in denominations:
            if n - i > 0:
                temp = arr[n - i] + arr[i]
                if arr[n] > temp:
                    arr[n] = temp
    if n % 5 == 0:
        score += N * arr[n]
    else:
        score += arr[n]
print arr
print score
