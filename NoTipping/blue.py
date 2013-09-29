def decScore(score, position, weight):
    score[0] += weight * (3 + position)
    score[1] += weight * (1 + position)

def incScore(score, position, weight):
    score[0] -= weight * (3 + position)
    score[1] -= weight * (1 + position)

def moveValid(score, position, weight):
    newScore = score[:]
    incScore(newScore, position, weight)
    if newScore[0] < 0 and newScore[1] > 0:
        return True
    return False

def removeValid(score, position, weight):
    newScore = score[:]
    decScore(newScore, position, weight)
    if newScore[0] < 0 and newScore[1] > 0:
        return True
    return False

def traversal(score, red, total, isRedTurn, lastPosition):
    if isRedTurn:
        if len(red) != 0:
            for position in red.keys():
                weight = red[position]
                if removeValid(score, position, weight):
                    newScore = score[:]
                    decScore(newScore, position, weight)
                    newTotal = total.copy()
                    newTotal.pop(position)
                    newRed = red.copy()
                    newRed.pop(position)
                    (redWins, newPosition) = traversal(newScore, newRed, newTotal, False, position)
                    if redWins:
                        return (False, position)
        else:
            for position in total.keys():
                weight = total[position]
                if removeValid(score, position, weight):
                    newScore = score[:]
                    decScore(newScore, position, weight)
                    newTotal = total.copy()
                    newTotal.pop(position)
                    (redWins, newPosition) = traversal(newScore, red, newTotal, False, position)
                    if redWins:
                        return (False, position)
        return (True, 0)
    else:
        for position in total.keys():
            weight = total[position]
            if removeValid(score, position, weight):
                newScore = score[:]
                decScore(newScore, position, weight)
                newTotal = total.copy()
                newTotal.pop(position)
                newRed = red.copy()
                if position in red.keys():
                    newRed.pop(position)
                (blueWins, newPosition) = traversal(newScore, newRed, newTotal, True, position)
                if blueWins:
                    return (False, position)
        return (True, 0)
        
def getNextRemove(total, red):
    score = [0, 0]
    for position in total.keys():
        incScore(score, position, total[position])
    for position in total.keys():
        weight = total[position]
        if removeValid(score, position, weight):
            newScore = score[:]
            decScore(newScore, position, weight)
            newTotal = total.copy()
            newTotal.pop(position)
            newRed = red.copy()
            if position in red.keys():
                newRed.pop(position)
            (blueWins, newPosition) = traversal(newScore, newRed, newTotal, True, position)
            if blueWins:
                return (position, total[position])
    for position in total.keys():
        return (position, total[position])

def getNextMove(total, red):
    score = [0, 0]
    for position in total.keys():
        incScore(score, position, total[position])
    if -3 not in total.keys():
        for weight in range(12, 0, -1):
            if weight not in blueWeights:
                return (-3, weight)
    elif -2 not in total.keys():
        for weight in range(12, 0, -1):
            if weight not in blueWeights:
                return (-2, weight)
    elif -1 not in total.keys():
        for weight in range(12, 0, -1):
            if weight not in blueWeights:
                return (-1, weight)
    elif -score[0] > score[1]:
        for weight in range(1, 13):
            if weight not in blueWeights:
                for position in range(31):
                    position -= 15
                    if position not in total.keys():
                        if moveValid(score, position, weight): 
                            return (position, weight)
    else:
        for weight in range(1, 13):
            if weight not in blueWeights:
                for position in range(31, 0, -1):
                    position -= 16
                    if position not in total.keys():
                        if moveValid(score, position, weight):
                            return (position, weight)
    
import sys
mode = eval(sys.argv[1])
player = eval(sys.argv[2])
remainingTime = eval(sys.argv[3])

total = dict()
red = dict()
redWeights = set()
blueWeights = set()
with open('board.txt', 'r') as f:
    for line in f:
        temp = map(int, line.split())
        if temp[1] > 0:
            total[temp[0]] = temp[1]
            if temp[2] < 2:
                red[temp[0]] = temp[1]
                redWeights.add(temp[1])
            else:
                blueWeights.add(temp[1])
if mode == 1:
    (position, weight) = getNextMove(total, red)
    print position, weight
if mode == 2:
    (position, weight) = getNextRemove(total, red)
    print position, weight
