def untangle(route):
    hasChanged = True
    while hasChanged:
        hasChanged = False
        for i in range(1, len(route)):
            city2, city3 = route[i - 1], route[i]
            dist = 0
            if i > 2:
                city1 = route[i - 1]
                dist += getDist(city1, city3)
                dist -= getDist(city1, city2)
            if i < len(route) - 1:
                city4 = route[i + 1]
                dist += getDist(city2, city4)
                dist -= getDist(city3, city4)
            if dist < 0:
                route[i - 1], route[i] = route[i], route[i - 1]
                hasChanged = True
    '''
    hasChanged = True
    while hasChanged:
        hasChanged = False
        for i in range(1, len(route)):
            city2, city3 = route[
    '''

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
        for city in citiesConnectedToCity1:
            if len(citiesConnected[city]) > 2:
                dist = roadVisited.pop((city1, city))
                roadVisited.pop((city, city1))
                citiesConnected[city1].remove(city)
                citiesConnected[city].remove(city1)
                flag = True
                newCitiesWithOneRoad.append(city1)
            elif city not in citiesVisited:
                city3 = city
        if flag == True:
            return dist
        for city in citiesConnectedToCity2:
            if len(citiesConnected[city]) > 2:
                dist = roadVisited.pop((city2, city))
                roadVisited.pop((city, city2))
                citiesConnected[city2].remove(city)
                citiesConnected[city].remove(city2)
                flag = True
                newCitiesWithOneRoad.append(city2)
            elif city not in citiesVisited:
                city4 = city
        if flag == True:
            return dist
        city1, city2 = city3, city4

def minimalSpanningTree(citiesConnected, roadVisited):
    sectionNumForCities = [0 for i in range(1001)]
    citySections = [[]]#group cities if connected
    sectionCount = 0#if it's 1 then all the cities are connected
    citiesVisitedCount = 0
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
        roadVisited[(city2, city1)] = road[0]
        citiesConnected[city1].append(city2)
        citiesConnected[city2].append(city1)
        if citiesVisitedCount == 1000 and sectionCount == 1:
            break;

def getDist(i, j):#city# = i, city# = j
    sumDist = 0
    for k in range(1, 4):
        sumDist += (cities[i - 1][k] - cities[j - 1][k]) ** 2
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
        dist = getDist(i + 1, j + 1)
        distBetweenCitiesList.append((dist, j + 1, i + 1))
        distBetweenCitiesDict[(j + 1, i + 1)] = dist
distBetweenCitiesList.sort()

roadVisited = dict()#for further use, [city1, city2] -> dist
citiesConnected = dict()#for saving cities which are directly connected, city -> [cities]
minimalSpanningTree(citiesConnected, roadVisited)

citiesWithOneRoad = []#[city]
for city in citiesConnected.keys():
    if len(citiesConnected[city]) == 1:
        citiesWithOneRoad.append(city)
while len(citiesWithOneRoad) > 2:
    distBetweenCitiesWithOneRoad = []#[(dist, city1, city2)]
    citiesWithOneRoad.sort()
    for i in range(len(citiesWithOneRoad)):
        for j in range(i):
            city1, city2 = citiesWithOneRoad[j], citiesWithOneRoad[i]#city1 < city2
            distBetweenCitiesWithOneRoad.append((distBetweenCitiesDict[city1, city2], city1, city2))
    distBetweenCitiesWithOneRoad.sort()
    newCitiesVisited = set()
    newCitiesWithOneRoad = []
    cityCount = 0
    maxIndex = 0
    cityVisitCount = dict()
    for i in range(len(distBetweenCitiesWithOneRoad)):
        road = distBetweenCitiesWithOneRoad[i]
        city1, city2 = road[1], road[2]
        if city1 not in cityVisitCount.keys():
            cityCount += 1
            cityVisitCount[city1] = 1
        else:
            cityVisitCount[city1] += 1
        if city2 not in cityVisitCount.keys():
            cityCount += 1
            cityVisitCount[city2] = 1
        else:
            cityVisitCount[city2] += 1
        if cityCount == len(citiesWithOneRoad):
            maxIndex = i
            break
    hasChanged = True
    while hasChanged:
        hasChanged = False
        for i in range(0, maxIndex + 1):
            road = distBetweenCitiesWithOneRoad[i]
            city1, city2 = road[1], road[2]
            if city1 not in newCitiesVisited and city2 not in newCitiesVisited:
                if cityVisitCount[city1] == 1 or cityVisitCount[city2] == 1:
                    citiesWithOneRoad.remove(city1)
                    citiesWithOneRoad.remove(city2)
                    newCitiesVisited.add(city1)
                    newCitiesVisited.add(city2)
                    dist = getLongestRoad(roadVisited, citiesConnected, city1, city2, newCitiesWithOneRoad)
                    roadVisited[(city1, city2)] = road[0]
                    roadVisited[(city2, city1)] = road[0]
                    citiesConnected[city1].append(city2)
                    citiesConnected[city2].append(city1)
                    cityVisitCount[city1] -= 1
                    cityVisitCount[city2] -= 1
                    hasChanged = True

    for i in range(0, maxIndex + 1):
        road = distBetweenCitiesWithOneRoad[i]
        city1, city2 = road[1], road[2]
        if city1 not in newCitiesVisited and city2 not in newCitiesVisited:
            citiesWithOneRoad.remove(city1)
            citiesWithOneRoad.remove(city2)
            newCitiesVisited.add(city1)
            newCitiesVisited.add(city2)
            dist = getLongestRoad(roadVisited, citiesConnected, city1, city2, newCitiesWithOneRoad)
            roadVisited[(city1, city2)] = road[0]
            roadVisited[(city2, city1)] = road[0]
            citiesConnected[city1].append(city2)
            citiesConnected[city2].append(city1)
    citiesWithOneRoad += newCitiesWithOneRoad

firstCity = citiesWithOneRoad[0]
route = [firstCity]
nextCity = firstCity
citiesVisited = set()
citiesVisited.add(firstCity)
while nextCity != citiesWithOneRoad[1]:
    length = len(citiesConnected[nextCity])
    for thisCity in citiesConnected[nextCity]:
        if thisCity not in citiesVisited:
            citiesVisited.add(thisCity)
            '''
            if len(citiesConnected[nextCity]) > 2:
                print "no"
                citiesConnectedToNextCity = citiesConnected[nextCity]
                for thatCity in citiesConnectedToNextCity:
                    if thatCity in citiesVisited:
                        citiesConnectedToNextCity.remove(thatCity)
                for i in range(1, len(citiesConnectedToNextCity), 2):
                    print "yes"
                    city1, city2 = min(citiesConnectedToNextCity[i - 1], citiesConnectedToNextCity[i]), max(citiesConnectedToNextCity[i - 1], citiesConnectedToNextCity[i])
                    roadVisited.pop((city1, nextCity))
                    roadVisited.pop((nextCity, city1))
                    roadVisited.pop((city2, nextCity))
                    roadVisited.pop((nextCity, city2))
                    key = (city1, city2)
                    dist = distBetweenCitiesDict[key]
                    roadVisited[key] = dist
                    key = (city2, city1)
                    roadVisited[key] = dist
                    citiesConnected[city1].append(city2)
                    citiesConnected[city2].append(city1)
                    citiesConnected[nextCity].remove(city1)
                    citiesConnected[nextCity].remove(city2)
            '''
            nextCity = thisCity
            break
    route.append(nextCity)

untangle(route)
print route
dist = 0
for i in range(len(route) - 1):
    dist += getDist(route[i], route[i + 1])
print dist
