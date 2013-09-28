def incScore(score, position, weight):
    score[0] += weight * (12 - position)
    score[1] += weight * (14 - position)

def valid(score, position, weight):
    newScore = score[:]
    incScore(newScore, position, weight)
    if newScore[0] < 0 and newScore[1] > 0:
        return True
    return False

def getPossibleWeights(weights, total, score):
    possibleWeights = []
    for weight in range(1, 13):
        if weight not in weights:
            for position in range(31):
                if position not in total.keys() and valid(score, position, weight):
                    possibleWeights.append(weight)
                    break
    return possibleWeights

def addPhase(red, redWeights, total, blueWeights, score, isRedTurn, count):
    if count == 25:
        for key in red.keys():
            print key, red[key], 
        for key in total.keys():
            print key, total[key],
        print score[0], score[1], 
        return True
    if isRedTurn:
        flag = True
        possibleWeights = getPossibleWeights(redWeights, total, score)
        if len(possibleWeights) == 0:
            return False
        while flag or not addPhase(newRed, newRedWeights, newTotal, blueWeights, newScore, False, count + 1):
            flag = False
            weightRed = possibleWeights[random.randint(0, len(possibleWeights) - 1)]
            position = random.randint(0, 30)
            while (position in total.keys()) or (not valid(score, position, weightRed)):
                position = random.randint(0, 30)
            newRed = red.copy()
            newRed[position] = weightRed
            newRedWeights = redWeights.copy()
            newRedWeights.add(weightRed)
            newTotal = total.copy()
            newTotal[position] = weightRed
            newScore = score[:]
            incScore(newScore, position, weightRed)
    else:
        flag = True
        possibleWeights = getPossibleWeights(blueWeights, total, score)
        if len(possibleWeights) == 0:
            return False
        while flag or not addPhase(red, redWeights, newTotal, newBlueWeights, newScore, True, count + 1):
            flag = False
            weightBlue = possibleWeights[random.randint(0, len(possibleWeights) - 1)]
            position = random.randint(0, 30)
            while (position in total.keys()) or (not valid(score, position, weightBlue)):
                position = random.randint(0, 30)
            newTotal = total.copy()
            newTotal[position] = weightBlue
            newBlueWeights = blueWeights.copy()
            newBlueWeights.add(weightBlue)
            newScore = score[:]
            incScore(newScore, position, weightBlue)
    return True

red = {11:3}
redWeights = set()
total = {11:3}
blueWeights = set()
score = [-6, 6]
count = 1
import random
addPhase(red, redWeights, total, blueWeights, score, True, count)
