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

oddCities = []
for city in citiesConnected.keys():
    if len(citiesConnected[city]) % 2 != 0:
        oddCities.append(city)
distBetweenOddCities = []#[(dist, city1, city2)]
for i in range(len(oddCities)):
    for j in range(i):
        city1, city2 = min(oddCities[i], oddCities[j]), max(oddCities[i], oddCities[j])
        #city1 < city2
        if city1 not in citiesConnected[city2]:
            distBetweenOddCities.append((distBetweenCitiesDict[city1, city2], city1, city2))
distBetweenOddCities.sort()
newCitiesVisited = set()
for road in distBetweenOddCities:
    city1, city2 = road[1], road[2]
    if city1 not in newCitiesVisited and city2 not in newCitiesVisited:
        newCitiesVisited.add(city1)
        newCitiesVisited.add(city2)
        maxDist = 0
        for (tempDist, city3) in citiesConnected[city1]:
            if tempDist > maxDist:
                maxDist = tempDist
        for (tempDist, city3) in citiesConnected[city2]:
            if tempDist > maxDist:
                maxDist = tempDist
        if 
        distSum += road[0]
        distSum -= maxDist
print distSum
