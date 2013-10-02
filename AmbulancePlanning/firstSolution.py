def getDist(location1, location2):
    return abs(location1[0] - location2[0]) + abs(location1[1] - location2[1])

def getFinishTime(ambulance, victims): #ambulance=(time, locX, locY), victims=[(time, (locX, locY, num))]
    travelTime = ambulance[0]
    travelTime += getDist(ambulance[1:], victims[0][1]) + 1
    for i in range(1, len(victims)):
        travelTime += getDist(victims[i - 1][1], victims[i][1]) + 1
    (hospital, dist) = getClosestHospital(victims[-1])
    travelTime += dist + 1
    
    for victim in victims:
        if travelTime > victim[0]:
            return 0
    return travelTime

def getClosestHospital(victim): #victim = (time, (locX, locY, num))
    minDist = getDist(hospitals[0], victim[1]) + 1
    closestHospital = hospitals[0]
    for i in range(1, len(hospitals)):
        dist = getDist(hospitals[i], victim[1]) + 1
        if dist < minDist:
            minDist = dist
            closestHospital = hospitals[i]
    return (closestHospital, minDist)

hospitals = [] #(locX, locY, #ambulances)
ambulances = [] #[time, locX, locY]
with open('hospital', 'r') as f:
    for line in f:
        temp = map(int, line.split(','))
        hospitals.append((temp[0], temp[1], temp[2]))
        ambulances += [[0, temp[0], temp[1]] for i in range(temp[2])]
distBetweenVictims = dict() #(locX, locY, num) -> [(dist, (time, (locX, locY, num)))]
visitedVictims = dict() #(locX, locY, num) -> boolean False means unvisited
victims = [] #(time, (locX, locY, num))
num = 0
with open('victims', 'r') as f:
    for line in f:
        temp = map(int, line.split(','))
        victims.append((temp[2], (temp[0], temp[1], num)))
        distBetweenVictims[(temp[0], temp[1], num)] = []
        visitedVictims[(temp[0], temp[1], num)] = False
        num += 1
numOfVictims = len(victims)
for i in range(len(victims)):
    victim = victims[i]
    key = victim[1]
    for j in range(i):
        victim2 = victims[j]
        key2 = victim2[1]
        dist = getDist(key, key2)
        distBetweenVictims[key].append((dist, victim2))
        distBetweenVictims[key2].append((dist, victim))
victims.sort()
visitedVictimsCount = 0
savedVictimsCount = 0
while visitedVictimsCount != numOfVictims:
    ambulances.sort()
    for ambulance in ambulances:
        victimsInAmbulance = []
        for victim in victims:
            key = victim[1]
            if not visitedVictims[key]:
                if victim[0] > ambulance[0] and getFinishTime(ambulance, [victim]) != 0:
                    currVictim = victim
                    victimsInAmbulance.append(victim) #(time, (locX, locY, num))
                    visitedVictims[victim[1]] = True
                    savedVictimsCount += 1
                    visitedVictimsCount += 1
                    while len(victimsInAmbulance) != 4:
                        hasNextVictim = False
                        otherVictims = distBetweenVictims[currVictim[1]]
                        otherVictims.sort()
                        for otherVictim in otherVictims:
                            timeAndLoc = otherVictim[1]
                            if visitedVictims[timeAndLoc[1]]:
                                continue
                            if getFinishTime(ambulance, victimsInAmbulance + [timeAndLoc]) != 0:
                                visitedVictimsCount += 1
                                visitedVictims[timeAndLoc[1]] = True
                                victimsInAmbulance.append(timeAndLoc)
                                savedVictimsCount += 1
                                hasNextVictim = True
                                currVictim = timeAndLoc
                                break
                        if not hasNextVictim:
                            break
                    if len(victimsInAmbulance) == 0:
                        break
                    (returnHospital, dist) = getClosestHospital(victimsInAmbulance[-1])
                    finishTime = getFinishTime(ambulance, victimsInAmbulance)
                    ambulance[0] = finishTime
                    ambulance[1] = returnHospital[0]
                    ambulance[2] = returnHospital[1]
                    break
                else:
                    visitedVictimsCount += 1
                    visitedVictims[key] = True
print visitedVictimsCount, savedVictimsCount
