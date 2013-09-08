key = []
sum1 = []
sum2 = []
with open('selectedSums', 'r') as f:
    for line in f:
        temp = line.rsplit(' ', 2)
        key.append(eval(temp[0]))
        sum1.append(int(temp[1]))
        sum2.append(int(temp[2]))
import sys
N = int(sys.argv[1])
minScore = 9999
for i in range(len(key)):
    score = sum1[i] + (N - 1) * sum2[i]
    if score <= minScore:
        minScore = score
        print key[i], score
