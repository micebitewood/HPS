def getLongestRoad(roadVisited, citiesConnected, city1, city2, newCitiesWithOneRoad):
    citiesVisited = set()#set([city])
    while True:
        citiesVisited.add(city1)
        citiesVisited.add(city2)
        flag = False
        citiesConnectedToCity1, citiesConnectedToCity2 = citiesConnected[city1], citiesConnected[city2]
        dist = 0
        city3 = city1
        city4 = city2
        for (t, city) in citiesConnectedToCity1:
            if len(citiesConnected[city]) > 2:
                key = (min(city1, city), max(city1, city))
                dist = roadVisited.pop(key)
                citiesConnected[city1].remove((t, city))
                citiesConnected[city].remove((t, city1))
                flag = True
                newCitiesWithOneRoad.append(city1)
            elif city not in citiesVisited:
                city3 = city
        if flag == True:
            return dist
        for (t, city) in citiesConnectedToCity2:
            if len(citiesConnected[city]) > 2:
                key = (min(city2, city), max(city2, city))
                dist = roadVisited.pop(key)
                citiesConnected[city2].remove((t, city))
                citiesConnected[city].remove((t, city2))
                flag = True
                newCitiesWithOneRoad.append(city2)
            elif city not in citiesVisited:
                city4 = city
        if flag == True:
            return dist
        city1, city2 = city3, city4

def minimalSpanningTree(citiesConnected, roadVisited):
    sectionNumForCities = [0 for i in range(1000)]
    citySections = [[]]#group cities if connected
    sectionCount = 0#if it's 1 then all the cities are connected
    citiesVisitedCount = 0
    distSum = 0
    for road in distBetweenCitiesList:
        city1, city2 = road[1], road[2]
        section1, section2 = sectionNumForCities[city1], sectionNumForCities[city2]
        if section1 != 0 and section2 != 0:
            if section1 == section2:
                continue
            minSection, maxSection = min(section1, section2), max(section1, section2)
            for city in citySections[maxSection]:
                sectionNumForCities[city] = minSection
                citySections[minSection].append(city)
            sectionCount -= 1
        elif section1 == 0 and section2 == 0:
            sectionNum = len(citySections)
            sectionNumForCities[city1], sectionNumForCities[city2] = sectionNum, sectionNum
            citySections.append([city1, city2])
            citiesVisitedCount += 2
            sectionCount += 1
            citiesConnected[city1] = []
            citiesConnected[city2] = []
        else:
            if section1 == 0:
                sectionNumForCities[city1] = section2
                citySections[section2].append(city1)
                citiesConnected[city1] = []
            else:
                sectionNumForCities[city2] = section1
                citySections[section1].append(city2)
                citiesConnected[city2] = []
            citiesVisitedCount += 1
        roadVisited[(city1, city2)] = road[0]
        distSum += road[0]
        citiesConnected[city1].append((road[0], city2))
        citiesConnected[city2].append((road[0], city1))
        if citiesVisitedCount == 1000 and sectionCount == 1:
            break;
    return distSum

def getDist(i, j):
    sumDist = 0
    for k in range(1, 4):
        sumDist += (cities[i][k] - cities[j][k]) ** 2
    import math
    return math.sqrt(sumDist)

cities = []
with open('input', 'r') as f:
    for line in f:
        temp = map(int, line.split())
        cities.append(temp)

distBetweenCitiesList = []
distBetweenCitiesDict = dict()#for searching for dist between cities, [city1, city2] -> dist
for i in range(len(cities)):
    for j in range(i):#j < i
        dist = getDist(i, j)
        distBetweenCitiesList.append((dist, j, i))
        distBetweenCitiesDict[(j, i)] = dist
distBetweenCitiesList.sort()

roadVisited = dict()#for further use, [city1, city2] -> dist
citiesConnected = dict()#for saving cities which are directly connected, city -> [cities]
distSum = minimalSpanningTree(citiesConnected, roadVisited)

citiesWithOneRoad = []#[city]
for city in citiesConnected.keys():
    if len(citiesConnected[city]) == 1:
        citiesWithOneRoad.append(city)
while len(citiesWithOneRoad) > 2:
    distBetweenCitiesWithOneRoad = []
    citiesWithOneRoad.sort()
    for i in range(len(citiesWithOneRoad)):
        for j in range(i):
            city1, city2 = citiesWithOneRoad[j], citiesWithOneRoad[i]#city1 < city2
            distBetweenCitiesWithOneRoad.append((distBetweenCitiesDict[city1, city2], city1, city2))
    distBetweenCitiesWithOneRoad.sort()
    newCitiesVisited = set()
    newCitiesWithOneRoad = []
    for road in distBetweenCitiesWithOneRoad:
        city1, city2 = road[1], road[2]
        if city1 not in newCitiesVisited and city2 not in newCitiesVisited:
            citiesWithOneRoad.remove(city1)
            citiesWithOneRoad.remove(city2)
            distSum += road[0]
            newCitiesVisited.add(city1)
            newCitiesVisited.add(city2)
            dist = getLongestRoad(roadVisited, citiesConnected, city1, city2, newCitiesWithOneRoad)
            roadVisited[city1, city2] = road[0]
            citiesConnected[city1].append((road[0], city2))
            citiesConnected[city2].append((road[0], city1))
            distSum -= dist
    citiesWithOneRoad += newCitiesWithOneRoad
print citiesWithOneRoad
firstCity = citiesWithOneRoad[0]
route = [firstCity]
nextCity = firstCity
citiesVisited = set()
citiesVisited.add(firstCity)
while nextCity != citiesWithOneRoad[1]:
    length = len(citiesConnected[nextCity])
    for (dist, thisCity) in citiesConnected[nextCity]:
        if thisCity not in citiesVisited:
            citiesVisited.add(thisCity)
            if len(citiesConnected[nextCity]) > 2:
                cities = citiesConnected[nextCity]
                for (dist, thatCity) in cities:
                    if thatCity in citiesVisited:
                        cities.remove(dist, thatCity)
                for i in range(1, len(cities), 2):
                    city1, city2 = min(cities[i - 1][1], cities[i][1]), max(cities[i - 1][1], cities[i][1])
                    key = (min(city1, nextCity), max(city1, nextCity))
                    distSum -= roadVisited.pop(key)
                    key = (min(city2, nextCity), max(city2, nextCity))
                    distSum -= roadVisited.pop(key)
                    key = (city1, city2)
                    roadVisited[key] = distBetweenCitiesDict[key]
                    citiesConnected[city1].append(city2)
                    citiesConnected[city2].append(city1)
                    citiesConnected[nextCity].remove(city1)
                    citiesConnected[nextCity].remove(city2)
            nextCity = thisCity
            break
    route.append(nextCity)
print route
print distSum
import matplotlib.pyplot as pyplot
for road in roadVisited.keys():
    city1 = road[0]
    city2 = road[1]
    x1, y1 = cities[city1][1], cities[city1][3]
    x2, y2 = cities[city2][1], cities[city2][3]
    pyplot.plot([x1, x2], [y1, y2], '-')
pyplot.axis([-100, 30100, -100, 30100])
pyplot.show()
