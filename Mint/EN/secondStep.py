key = []
sum1 = []
sum2 = []
lst = []
with open('csums', 'r') as f:
    for line in f:
        temp = line.rsplit(' ', 2)
        key.append(eval(temp[0]))
        sum1.append(int(temp[1]))
        sum2.append(int(temp[2]))
for i in range(len(key)):
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
