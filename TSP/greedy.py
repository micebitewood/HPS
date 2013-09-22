def getNewRoute(i, newRoute):
    for index in range(1, len(newRoute) + 1 - i):
        city1, city2 = newRoute[index - 1], newRoute[index - 1 + i]
        dist = 0
        if index > 1:
            city3 = newRoute[index - 2]
            dist += getDist(city2, city3)
            dist -= getDist(city1, city3)
        if index < len(newRoute) - i:
            city4 = newRoute[index + i]
            dist += getDist(city1, city4)
            dist -= getDist(city2, city4)
        if dist < 0:
            if index != 1:
                newRoute[index - 1: index + i] = newRoute[index + i - 1: index - 2: -1]
            else:
                newRoute[index - 1: index + i] = newRoute[index + i - 1::-1]
    return newRoute

def untangle(newRoute):
    for i in range(500, 0, -1):
        newRoute = getNewRoute(i, newRoute)
    for i in range(1, 500):
        newRoute = getNewRoute(i, newRoute)
    return newRoute

def reFactor(route):
    MINDIST = 4000
    MAXDIST = 2000
    maxIndex = 1
    for index in range(1, len(route)):
        city1, city2 = route[index - 1], route[index]
        if getDist(city1, city2) > MINDIST:
            maxIndex = index
            break
    newRoute = route[:maxIndex]
    for index in range(maxIndex, len(route)):
        city = route[index]
        hasInserted = False
        for i in range(len(newRoute)):
            city1 = newRoute[i]
            if getDist(city, city1) < MAXDIST:
                newRoute.insert(i + 1, city)
                hasInserted = True
                break
        if not hasInserted:
            newRoute.append(city)
    return newRoute

def getDist(i, j):
    sumDist = 0
    for k in range(1, 4):
        sumDist += (cities[i - 1][k] - cities[j - 1][k]) ** 2
    return math.sqrt(sumDist)

import math
cities = []
with open('input', 'r') as f:
    for line in f:
        temp = map(int, line.split())
        cities.append(temp)

citiesNum = len(cities)
distBetweenCities = []
for i in range(len(cities)):
    for j in range(i):# j < i
        dist = getDist(i + 1, j + 1)
        distBetweenCities.append((dist, j + 1, i + 1))
distBetweenCities.sort()

citiesVisited = set()
fistCity = 0
citiesCount = 0
finalDist = (0, 0, 0)
roadVisited = set()
citiesConnected = dict()#[cities]
for (dist, city1, city2) in distBetweenCities:
    lastCity = city1
    if city1 not in citiesVisited:
        citiesVisited.add(city1)
        citiesConnected[city1] = []
        citiesCount += 1
    citiesConnected[city1].append(city2)
    if city2 not in citiesVisited:
        citiesVisited.add(city2)
        citiesConnected[city2] = []
        citiesCount += 1
        lastCity = city2
    citiesConnected[city2].append(city1)
    roadVisited.add((city1, city2))
    if citiesCount == citiesNum:
        firstCity = lastCity
        finalDist = (dist, city1, city2)
        break

#firstCity, citiesConnected, roadVisited
citiesVisited = set()
citiesCount = 1
nextCity = firstCity
route = [firstCity]
while citiesCount < citiesNum:
    citiesConnectedToNextCity = citiesConnected[nextCity]
    closestCity = nextCity
    minDist = 999999
    for city in citiesConnectedToNextCity:
        if city not in citiesVisited:
            dist = getDist(city, nextCity)
            if dist < minDist:
                minDist = dist
                closestCity = city
    if minDist == 999999:
        for cityInCities in cities:
            city = cityInCities[0]
            if city not in citiesVisited:
                dist = getDist(city, nextCity)
                if dist < minDist:
                    minDist = dist
                    closestCity = city
    citiesVisited.add(closestCity)
    citiesCount += 1
    nextCity = closestCity
    route.append(nextCity)
    
route = reFactor(route)
route = untangle(route)

import matplotlib.pyplot as plot
dist = 0
for i in range(1, citiesNum):
    city1, city2 = route[i - 1], route[i]
    dist += getDist(city1, city2)
    x1, y1 = cities[city1 - 1][1], cities[city1 - 1][3]
    x2, y2 = cities[city2 - 1][1], cities[city2 - 1][3]
    plot.plot([x1, x2], [y1, y2], '-')
print dist
plot.axis([-100, 30100, -100, 30100])
plot.show()
