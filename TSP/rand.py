import sys
import random

if len(sys.argv) > 1:
    input = str(sys.argv[1])
else:
    input = 'input'

cities = []
with open(input, 'r') as f:
    for line in f:
        temp = map(int, line.split())
        cities.append(temp)

numCities = len(cities)

path = []
pool = range(1, numCities+1)

for i in range(numCities):
    path = path + [pool.pop(random.randrange(numCities-i))]

print path
