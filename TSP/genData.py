import sys
import random

numCities = int(sys.argv[1])

for i in range(0, numCities):
    print i+1, random.randrange(30000), random.randrange(30), random.randrange(30000)
