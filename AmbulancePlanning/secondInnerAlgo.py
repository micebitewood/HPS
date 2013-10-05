def isValid(ambulance, visitedVictims):
    for victim in timeList:
        time = victim[0]
        key = victim[1]
        if time <= ambulance[2]:
            return False
        if not visitedVictims[key]:
            (time1, time2) = getFinishTime(ambulance, key)
            if time1 + time2 < time:
                return True

def getFinishTime(ambulance, victim):
    timeTillLoadVictim = getDist(ambulance, victim) + 1 + ambulance[2]
    timeBackToHospital = victims[victim][2] + 1 + ambulance[2]
    return (timeTillLoadVictim, timeBackToHospital)

def getSavedCount(ambulances, originalVisitedVictims):
    print "start:"
    visitedVictims = originalVisitedVictims.copy()
    newAmbulances = ambulances[:]
    random.shuffle(newAmbulances)
    for originalAmbulance in newAmbulances:
        print "new ambulance", originalAmbulance
        print "will there be victims saved?"
        for victim in visitedVictims.keys():
            if visitedVictims[victim]:
                print victim,
        print "Y or N?"
        maxCount = 0
        mostSavedVictims = []
        for i in range(100): #try to get the maximum number of victims this ambulance can save
            print i
            ambulance = originalAmbulance[:]
            newVisitedVictims = visitedVictims.copy()
            savedVictims = [] #(locX, locY, num)
            savedVictimsCount = 0
            while True:
                if not isValid(ambulance, newVisitedVictims):
                    break
                ind = random.randint(0, len(victimKeys) - 1)
                victim = victimKeys[ind] #(locX, locY, num)
                (timeTillLoadVictim, timeBackToHospital) = getFinishTime(ambulance, victim)
                if timeTillLoadVictim + timeBackToHospital < victims[victim][0]:
                    print "saved victim", victim
                    lastTimeTillLoadVictim = timeTillLoadVictim
                    lastTimeBackToHospital = timeBackToHospital
                    print "total time", lastTimeTillLoadVictim, lastTimeBackToHospital
                    victimsInAmbulance = [] #(locX, locY, num)
                    currVictim = victim
                    victimsInAmbulance.append(victim)
                    newVisitedVictims[victim] = True
                    savedVictimsCount += 1
                    while len(victimsInAmbulance) != 4:
                        hasNextVictim = False
                        otherVictims = distBetweenVictims[currVictim]
                        otherVictims.sort()
                        for otherVictim in otherVictims:
                            key = otherVictim[1]
                            if newVisitedVictims[key]:
                                continue
                            timeTillLoadVictim = lastTimeTillLoadVictim + otherVictim[0] + 1
                            timeBackToHospital = victims[key][2] + 1
                            skip = False
                            for victimInAmbulance in victimsInAmbulance:
                                if timeTillLoadVictim + timeBackToHospital > victims[victimInAmbulance][0]:
                                    skip = True
                                    break
                            if skip:
                                continue
                            newVisitedVictims[key] = True
                            print "saved", otherVictim
                            lastTimeTillLoadVictim = timeTillLoadVictim
                            lastTimeBackToHospital = timeBackToHospital
                            print "total time", lastTimeTillLoadVictim, lastTimeBackToHospital
                            victimsInAmbulance.append(key)
                            savedVictimsCount += 1
                            hasNextVictim = True
                            currVictim = key
                            break
                        if not hasNextVictim:
                            break
                    if len(victimsInAmbulance) == 0:
                        break
                    returnHospital = hospitals[victims[victimsInAmbulance[-1]][1]]
                    finishTime = lastTimeTillLoadVictim + lastTimeBackToHospital
                    savedVictims += victimsInAmbulance
                    print "savedVictims in by this ambulance:", savedVictims
                    ambulance[0] = returnHospital[0]
                    ambulance[1] = returnHospital[1]
                    ambulance[2] += finishTime
                    print "ambulance:", ambulance
            print "savedVictimsCount", savedVictimsCount
            if savedVictimsCount > maxCount:
                maxCount = savedVictimsCount
                mostSavedVictims = savedVictims
        for victim in mostSavedVictims:
            visitedVictims[victim] = True
    count = 0
    for victim in visitedVictims.keys():
        if visitedVictims[victim]:
            count += 1
    return count

def getDist(location1, location2):
    return abs(location1[0] - location2[0]) + abs(location1[1] - location2[1])

def getNearestHospital(x, y):
    minTime = 10000
    ret = 0
    for i in range(len(hospitals)):
        hospital = hospitals[i]
        time = abs(x - hospital[0]) + abs(y - hospital[1])
        if time < minTime:
            minTime = time
            ret = i
    return (ret, minTime)

hospitals = [] #(locX, locY)
ambulances = [] #(locX, locY, time)
with open('cluster_output', 'r') as f:
    for line in f:
        temp = map(int, line.split(','))
        hospitals.append((temp[0], temp[1]))
        for i in range(temp[2]):
            ambulances.append([temp[0], temp[1], 0])
distBetweenVictims = dict() #(locX, locY, num) -> [(dist, (locX, locY, num))]
visitedVictims = dict() #(locX, locY, num) -> boolean False means unvisited
victims = dict() #(locX, locY, num)->(time, nearestHospital, timeToNearestHospital)
timeList = [] #(time, (locX, locY, num))
num = 0
with open('victims', 'r') as f:
    for line in f:
        temp = map(int, line.split(','))
        timeList.append((temp[2], (temp[0], temp[1], num)))
        (nearestHospital, time) = getNearestHospital(temp[0], temp[1])
        key = (temp[0], temp[1], num)
        victims[key] = (temp[2], nearestHospital, time)
        distBetweenVictims[key] = []
        visitedVictims[key] = False
        num += 1
timeList.sort()
timeList.reverse()
victimKeys = victims.keys()
numOfVictims = len(victimKeys)
for i in range(numOfVictims):
    key = victimKeys[i]
    for j in range(i):
        key2 = victimKeys[j]
        dist = getDist(key, key2)
        distBetweenVictims[key].append((dist, key2))
        distBetweenVictims[key2].append((dist, key))

import random
maxPeopleSaved = 0
for i in range(10):
    count = getSavedCount(ambulances, visitedVictims)
    print "trial", i, "saved", count, "people"
    print count
