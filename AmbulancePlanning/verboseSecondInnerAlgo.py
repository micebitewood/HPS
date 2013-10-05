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
    timeBackToHospital = victims[victim][2] + 1
    return (timeTillLoadVictim, timeBackToHospital)

def getSavedCount(ambulances, originalVisitedVictims, f):
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
        finalPath = []
        for i in range(100): #try to get the maximum number of victims this ambulance can save
            print i
            ambulance = originalAmbulance[:]
            path = []
            newVisitedVictims = visitedVictims.copy()
            savedVictims = [] #(locX, locY, num)
            savedVictimsCount = 0
            while True:
                if not isValid(ambulance, newVisitedVictims):
                    break
                ind = random.randint(0, len(victimKeys) - 1)
                victim = victimKeys[ind] #(locX, locY, num)
                while newVisitedVictims[victim]:
                    ind = random.randint(0, len(victimKeys) - 1)
                    victim = victimKeys[ind]
                (timeTillLoadVictim, timeBackToHospital) = getFinishTime(ambulance, victim)
                if timeTillLoadVictim + timeBackToHospital < victims[victim][0]:
                    print "saved victim", victim
                    lastTimeTillLoadVictim = timeTillLoadVictim
                    lastTimeBackToHospital = timeBackToHospital
                    print "time, nearestHospital:", victims[victim][0], hospitals[victims[victim][1]]
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
                            if timeTillLoadVictim + timeBackToHospital > victims[key][0]:
                                skip = True
                            if skip:
                                continue
                            newVisitedVictims[key] = True
                            print "saved", otherVictim
                            lastTimeTillLoadVictim = timeTillLoadVictim
                            lastTimeBackToHospital = timeBackToHospital
                            print "time, nearestHospital:", victims[key][0], hospitals[victims[key][1]]
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
                    for victim in victimsInAmbulance:
                        path.append((victim[2], (victim[0], victim[1], victims[victim][0])))
                    savedVictims += victimsInAmbulance
                    print "savedVictims in by this ambulance:", savedVictims
                    ambulance[0] = returnHospital[0]
                    ambulance[1] = returnHospital[1]
                    ambulance[2] = finishTime
                    path.append(returnHospital)
                    print "ambulance:", ambulance
            print "savedVictimsCount", savedVictimsCount
            if savedVictimsCount > maxCount:
                maxCount = savedVictimsCount
                mostSavedVictims = savedVictims
                finalPath = path[:]
        print "path for ambulance: "
        print "ambulance", originalAmbulance
        print "path", finalPath
        f.write("ambulance " + str(ambulance[3]) + " ")
        for path in finalPath:
            if type(path[1]) == int:
                f.write("(" + str(path[0]) + ", " + str(path[1]) + "); ")
            else:
                f.write(str(path[0]) + " " + "(" + str(path[1][0]) + ", " + str(path[1][1]) + ", " + str(path[1][2]) + "); ")
        f.write("\n")
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
ambulances = [] #(locX, locY, time, num)
num = 0
with open('cluster_output', 'r') as f:
    for line in f:
        temp = map(int, line.split())
        hospitals.append((temp[0], temp[1]))
        for i in range(temp[2]):
            ambulances.append([temp[0], temp[1], 0, num])
            num += 1
distBetweenVictims = dict() #(locX, locY, num) -> [(dist, (locX, locY, num))]
visitedVictims = dict() #(locX, locY, num) -> boolean False means unvisited
victims = dict() #(locX, locY, num)->(time, nearestHospital, timeToNearestHospital)
timeList = [] #(time, (locX, locY, num))
num = 0
with open('input', 'r') as f:
    for line in f:
        line = line.strip().lower()
        if not line:
            continue
        if "person" in line:
            startVictims = True
        elif "hospital" in line:
            startVictims = False
        elif startVictims:
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
with open('path', 'w') as f:
    f.write("hospitals ")
    for i in range(len(hospitals)):
        f.write(str(i) + " ")
        f.write("(" + str(hospitals[i][0]) + ", " + str(hospitals[i][1]) + "); ")
    f.write('\n')
    for i in range(1):
        count = getSavedCount(ambulances, visitedVictims, f)
