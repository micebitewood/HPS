def calc(denominations, left, right, minScore):
    if right != 100:
        for i in range(left, right + 1):
            minScore = calc(denominations + [i], i + 1, right + 1, minScore)
    else:
        arr = [100 for i in range(99)]
        for i in denominations:
            arr[i - 1] = 1
        score = 0
        for n in range(1, 100):
            if arr[n - 1] != 1:
                for i in denominations:
                    if n - i > 0:
                        arr[n - 1] = min(arr[n - 1], arr[n - i - 1] + 1)
            if n % 5 == 0:
                score += N * arr[n - 1]
            else:
                score += arr[n - 1]
        minScore = min(score, minScore)
        return minScore

N = 4
denominationsNum = 5
minScore = 4951
calc([1], 2, 100 - denominationsNum + 1, minScore)
print minScore
