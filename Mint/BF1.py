def calc(denominations, left, right):
    global minScore
    if right != 100:
        for i in range(left, right + 1):
            calc(denominations + [i], i + 1, right + 1)
    else:
        arr = [100 for i in range(99)]
        for i in denominations:
            arr[i - 1] = 1
        score = 0
        for n in range(1, 100):
            if arr[n - 1] != 1:
                for i in denominations[::-1]:
                    if n - i > 0:
                        coinNum = arr[n - i - 1] + arr[i - 1]
                        if arr[n - 1] > coinNum:
                            arr[n - 1] = coinNum
            if n % 5 == 0:
                score += N * arr[n - 1]
            else:
                score += arr[n - 1]
        print denominations, score
        if score < minScore:
            minScore = score

N = 1
denominationsNum = 3
minScore = 1000
calc([1], 2, 100 - denominationsNum + 1)
print minScore
