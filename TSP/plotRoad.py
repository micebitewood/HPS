import sys
datafile = sys.argv[1]
cities = []
temp = []
with open('input', 'r') as f:
    for line in f:
        cities.append(map(int, line.split()))
with open(datafile, 'r') as f:
    for line in f:
        temp += eval(line)

import matplotlib.pyplot as plt
roadVisited = set()
count = 0
for road in temp:
    city1, city2 = int(road[0]) - 1, int(road[1]) - 1
    if (city2, city1) in roadVisited:
        continue
    else:
        roadVisited.add((city1, city2))
    x1, y1 = cities[city1][1], cities[city1][3]
    x2, y2 = cities[city2][1], cities[city2][3]
    plt.plot([x1, y1], [x2, y2], 'r-')
    count += 1
    if count > 1:
        break
plt.axis([-100, 30100, -100, 30100])
plt.show()
