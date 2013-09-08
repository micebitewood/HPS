key = []
sum1 = []
sum2 = []
lst = []
minSum2 = 9999
minInd = 0
with open('sums', 'r') as f:
    for line in f:
        temp = line.rsplit(' ', 2)
        key.append(eval(temp[0]))
        tempSum1 = int(temp[1])
        tempSum2 = int(temp[2])
        sum1.append(tempSum1 + tempSum2)
        sum2.append(tempSum2)
        if tempSum2 < minSum2:
            minSum2 = tempSum2
            minInd = len(sum2) - 1
for i in range(len(key)):
    if sum1[i] <= sum1[minInd]:
        lst.append([sum1[i], sum2[i], key[i]])
key = []
sum1 = []
sum2 = []
lst.sort()
i = 0
for j in range(1, len(lst)):
    if lst[i][1] >= lst[j][1]:
        print lst[j][2], lst[j][0], lst[j][1]
        i = j
