N = 1
denominations = [1 for i in range(5)]
arr = [0 for i in range(100)]
for denominations[1] in range(2, 97):
    for denominations[2] in range(denominations[1] + 1, 98):
        for denominations[3] in range(denominations[2] + 1, 99):
            for denominations[4] in range(denominations[3] + 1, 100):
                arr = [100 for i in range(100)]
                for i in denominations:
                    arr[i - 1] = 1
                score = 0
                for n in range(1, 100):
                    if arr[n - 1] != 1:
                        for i in denominations[::-1]:
                            if n - i > 0:
                                if arr[n - 1] > arr[n - i - 1] + arr[i - 1]:
                                    arr[n - 1] = arr[n - i - 1] + arr[i - 1]
                    if n % 5 == 0:
                        score += N * arr[n - 1]
                    else:
                        score += arr[n - 1]
                print denominations, score
