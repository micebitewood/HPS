def move(location, victimsInLocation, directions):
    moved = False
    indX = -1
    indY = -1   
    if directions[0] > directions[1] + 4: #move right
        indX = 0
        moved = True
    elif directions[0] < directions[1] - 4: #move left
        indX = 1
        moved = True
    if directions[2] > directions[3] + 4: #move up
        indY = 2
        moved = True
    elif directions[2] < directions[3] - 4: #move down
        indY = 3
        moved = True
    if moved:
        for victim in victimsInLocation:
            if indX != -1:
                if victim[0] == location[0] - indX * 2 + 1:
                    directions[indX] -= 1
                elif victim[0] == location[0]:
                    directions[1 - indX] += 1
            if indY != -1:
                if victim[1] == location[1] - indY * 2 + 5: #if indY == 3, location[1] + 1
                    directions[indY] -= 1
                elif victim[1] == location[1]:
                    directions[5 - indY] += 1
        if indX != -1:
            location[0] = location[0] - indX * 2 + 1
        if indY != -1:
            location[1] = location[1] - indY * 2 + 5
        return True
    return False

def adjust(locations):
    for index in range(len(locations)):
        location = locations[index]
        victimsInLocation = victimLocations[index]
        directions = [0 for i in range(4)] #[right, left, up, down]
        for victim in victimsInLocation:
            if victim[0] > location[0]:
                directions[0] += 1
            elif victim[0] < location[0]:
                directions[1] += 1
            if victim[1] > location[1]:
                directions[2] += 1
            elif victim[1] < location[1]:
                directions[3] += 1
        count = 0
        print "before adjusting", location
        while count < 1000:
            count += 1
            if not move(location, victimsInLocation, directions):
                break
        print "after adjusting", location, count

def getAbsSum(x1, y1, x2, y2):
    return abs(x1 - x2) + abs(y1 - y2)

minX, maxX, minY, maxY = 100, -100, 100, -100
victims = dict() #(locX, locY, num)->time
hospitals = [] #ambulanceNumber
num = 0
startVictims = False
startHospitals = False
with open('input', 'r') as f:
    for line in f:
        line = line.strip().lower()
        if not line:
            continue
        if "person" in line:
            startVictims = True
        elif "hospital" in line:
            startHospitals = True
        elif startHospitals:
            temp = int(line)
            hospitals.append(temp)
        elif startVictims:
            temp = map(int, line.split(','))
            if temp[0] < minX:
                minX = temp[0]
            elif temp[0] > maxX:
                maxX = temp[0]
            if temp[1] < minY:
                minY = temp[1]
            elif temp[1] > maxY:
                maxY = temp[1]
            victims[(temp[0], temp[1], num)] = temp[2]
            num += 1

numOfHospitals = len(hospitals)
locations = [] #[locX, locY, numOfVictims]

import random
for i in range(numOfHospitals):
    locations.append([random.randint(minX, maxX), random.randint(minY, maxY), 0])
needClustering = True
edges = []#(minX, maxX, minY, maxY)
victimLocations = [] #[(locX, locY, num)]
while needClustering:
    needClustering = False
    sumXY = [[0, 0, 0] for i in range(numOfHospitals)] #(sumX, sumY, #victims)
    edges = [[maxX, minX, maxY, minY] for i in range(numOfHospitals)] 
    victimLocations = [[] for i in range(numOfHospitals)]
    for victim in victims.keys(): #victim = (locX, locY, num)
        minDist = 100000
        closestPoint = 0
        for ind in range(numOfHospitals):
            location = locations[ind]
            dist = getAbsSum(victim[0], victim[1], location[0], location[1])
            if dist < minDist:
                minDist = dist
                closestPoint = ind
        sumXY[closestPoint][0] += victim[0]
        sumXY[closestPoint][1] += victim[1]
        sumXY[closestPoint][2] += 1
        victimLocations[closestPoint].append(victim)
        if victim[0] < edges[closestPoint][0]:
            edges[closestPoint][0] = victim[0]
        elif victim[0] > edges[closestPoint][1]:
            edges[closestPoint][1] = victim[0]
        if victim[1] < edges[closestPoint][2]:
            edges[closestPoint][2] = victim[1]
        elif victim[1] > edges[closestPoint][3]:
            edges[closestPoint][3] = victim[1]
    for ind in range(numOfHospitals):
        if sumXY[ind][2] != 0:
            sumXY[ind][0] /= sumXY[ind][2]
            sumXY[ind][1] /= sumXY[ind][2]
        if sumXY[ind][0] != locations[ind][0]:
            needClustering = True
        if sumXY[ind][1] != locations[ind][1]:
            needClustering = True
        locations[ind] = sumXY[ind]

adjust(locations)
numFirstLocations = []
for location in locations:
    numFirstLocations.append((location[2], location[0], location[1]))
numFirstLocations.sort()
newHospitals = hospitals[:]
newHospitals.sort()
result = []

for i in range(numOfHospitals):
    result.append((newHospitals[i], numFirstLocations[i][1], numFirstLocations[i][2]))
used = [False for i in range(numOfHospitals)]
for i in hospitals:
    for j in range(numOfHospitals):
        if not used[j]:
            res = result[j]
            if res[0] == i:
                #print res[1], res[2], res[0]
                used[j] = True
'''
import matplotlib.pyplot as plot
for victim in victims.keys():
    plot.plot(victim[0], victim[1], 'ro')
for location in locations:
    plot.plot(location[0], location[1], 'bo')
plot.show()
'''
