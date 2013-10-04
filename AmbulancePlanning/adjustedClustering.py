def move(location, victimsInLocation, directions):
    moved = False
    indX = -1
    indY = -1   
    if directions[0] > directions[1] + 4: #move left
        indX = 1
        moved = True
    elif directions[0] < directions[1] - 4: #move right
        indX = 0
        moved = True
    if directions[2] > directions[3] + 4: #move up
        indY = 3
        moved = True
    elif directions[2] < directions[3] - 4: #move down
        indY = 2
        moved = True
    if moved:
        for victim in victimsInLocation:
            if indX != -1:
                if victim[0] == location[0] - indX * 2 + 1:
                    directions[1 - indX] -= 1
                elif victim[0] == location[0]:
                    directions[indX] += 1
            if indY != -1:
                if victim[1] == location[1] + indY * 2 - 5: #if indY == 3, location[1] + 1
                    directions[5 - indY] -= 1
                elif victim[1] == location[0]:
                    directions[indY] += 1
        return True
    return False

def adjust(locations):
    for index in range(len(locations)):
        location = locations[index]
        victimsInLocation = victimLocations[index]
        directions = [0 for i in range(4)] #[left, right, up, down]
        for victim in victimsInLocation:
            if victim[0] < location[0]:
                directions[0] += 1
            elif victim[0] > location[0]:
                directions[1] += 1
            if victim[1] > location[1]:
                directions[2] += 1
            elif victim[1] < location[1]:
                directions[3] += 1
        while True:
            if not move(location, victimsInLocation, directions):
                break

def getAbsSum(x1, y1, x2, y2):
    return abs(x1 - x2) + abs(y1 - y2)

minX, maxX, minY, maxY = 100, -100, 100, -100
victims = dict() #(locX, locY, num)->time
num = 0
with open('victims', 'r') as f:
    for line in f:
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

hospitals = [] #ambulanceNumber
with open('hospital', 'r') as f:
    for line in f:
        temp = map(int, line.split(','))
        hospitals.append(temp[2])
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
print "hospitals[locX, locY, #victims]:", locations
print "ambulances in hospitals:", hospitals

import matplotlib.pyplot as plot
for victim in victims.keys():
    plot.plot(victim[0], victim[1], 'ro')
for location in locations:
    plot.plot(location[0], location[1], 'bo')
plot.show()

