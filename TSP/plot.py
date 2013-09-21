import sys
datafile = sys.argv[1]
temp = []
with open(datafile, 'r') as f:
    for line in f:
        temp.append(map(int, line.split()))

import matplotlib.pyplot as plt
plt.plot(temp[0], temp[1], 'r-')
plt.axis([-100, 30100, -100, 30100])
plt.show()
