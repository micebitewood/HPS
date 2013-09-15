import sys
import math

if len(sys.argv) > 1:
    input = str(sys.argv[1])
else:
    input = 'input'

if len(sys.argv) > 2:
    output = str(sys.argv[2])
else:
    output = 'output'

cities = []
with open(input, 'r') as f:
    for line in f:
        temp = map(int, line.split())
        cities.append(temp)

with open(output, 'r') as f:
    path = eval(f.readline())

numCities = len(cities)

if numCities != len(path):
    print 'Error: output length mismatch'

distSum = 0
path = path + [path[0]]

for i in range(numCities):
    distSum = distSum + math.sqrt((cities[path[i]-1][1]-cities[path[i+1]-1][1])**2 + (cities[path[i]-1][2]-cities[path[i+1]-1][2])**2 + (cities[path[i]-1][3]-cities[path[i+1]-1][3])**2)

print distSum
