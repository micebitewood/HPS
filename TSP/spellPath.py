import sys

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

x = [cities[path[i]-1][1] for i in range(numCities)]
y = [cities[path[i]-1][2] for i in range(numCities)]
z = [cities[path[i]-1][3] for i in range(numCities)]

print ' '.join(map(str, x))
print ' '.join(map(str, z))
