def getAbsSum(x1, y1, x2, y2):
    return abs(x1 - x2) + abs(y1 - y2)
minX, maxX, minY, maxY = 100, -100, 100, -100
people = dict() #(x, y)->time
with open('people', 'r') as f:
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
        people[(temp[0], temp[1])] = temp[2]

hospitals = [] #ambulanceNumber
with open('hospital', 'r') as f:
    for line in f:
        temp = map(int, line.split(','))
        hospitals.append(temp[2])
numOfHospitals = len(hospitals)
locations = []
import random
for i in range(numOfHospitals):
    locations.append([random.randint(minX, maxX), random.randint(minY, maxY)])
needClustering = True
while needClustering:
    needClustering = False
    sumXY = [[0, 0] for i in range(numOfHospitals)]
    numOfPeople = [0 for i in range(numOfHospitals)]
    for person in people.keys():
        minDist = 100000
        closestPoint = 0
        for ind in range(numOfHospitals):
            location = locations[ind]
            dist = getAbsSum(person[0], person[1], location[0], location[1])
            if dist < minDist:
                minDist = dist
                closestPoint = ind
        sumXY[closestPoint][0] += person[0]
        sumXY[closestPoint][1] += person[1]
        numOfPeople[closestPoint] += 1
    for ind in range(numOfHospitals):
        if numOfPeople[ind] != 0:
            sumXY[ind][0] /= numOfPeople[ind]
            sumXY[ind][1] /= numOfPeople[ind]
        if sumXY[ind][0] != locations[ind][0]:
            needClustering = True
        if sumXY[ind][1] != locations[ind][1]:
            needClustering = True
        locations[ind] = sumXY[ind]
print locations

import matplotlib.pyplot as plot
for person in people.keys():
    plot.plot(person[0], person[1], 'ro')
for location in locations:
    plot.plot(location[0], location[1], 'bo')
plot.show()
