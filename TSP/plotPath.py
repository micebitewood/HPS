import sys
datafile = sys.argv[1]
cities = []
path = []
with open('input', 'r') as f:
    for line in f:
        cities.append(map(int, line.split()))
with open(datafile, 'r') as f:
    for line in f:
        path = eval(line)

import matplotlib.pyplot as plot
for i in range(1, len(path)):
    city1, city2 = path[i - 1], path[i]
    x1, y1 = cities[city1 - 1][1], cities[city1 - 1][3]
    x2, y2 = cities[city2 - 1][1], cities[city2 - 1][3]
    plot.plot([x1, x2], [y1, y2], 'r-')
plot.axis([-100, 30100, -100, 30100])
plot.show()
