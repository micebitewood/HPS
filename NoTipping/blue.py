def decScore(score, position, weight):
    score[0] += weight * (3 + position)
    score[1] += weight * (1 + position)

def incScore(score, position, weight):
    score[0] -= weight * (3 + position)
    score[1] -= weight * (1 + position)

def valid(score, position, weight):
    newScore = score[:]
    decScore(newScore, position, weight)
    if newScore[0] < 0 and newScore[1] > 0:
        return True
    return False

def traversal(score, total, red, isRedTurn, lastPosition):
    if isRedTurn:
        if len(red) != 0:
            for position in red.keys():
                weight = red[position]
                if valid(score, position, weight):
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
                if valid(score, position, weight):
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
            if valid(score, position, weight):
                newScore = score[:]
                decScore(newScore, position, weight)
                newTotal = total.copy()
                newTotal.pop(position)
                newRed = red.copy()
                if position in red.keys():
                    newRed.pop(position)
                (blueLoses, newPosition) = traversal(newScore, newRed, newTotal, True, position)
                if not blueLoses:
                    return (False, position)
        return (True, 0)
        
def getNextMove(total, red):
    score = [0, 0]
    for position in total.keys():
        incScore(score, position, total[position])
    for position in total.keys():
        weight = total[position]
        if valid(score, position, weight):
            newScore = score[:]
            decScore(newScore, position, weight)
            newBoard = total.copy()
            newBoard[position] = 0
            newRed = red.copy()
            if position in red.keys():
                newRed.pop(position)
            (blueWins, newPosition) = traversal(newScore, newBoard, newRed, True, position)
            if blueWins:
                return (position, total[position])
    for position in total.keys():
        if total[position] != 0:
            return (position, total[position])

import sys
mode = eval(sys.argv[1])
player = eval(sys.argv[2])
remainingTime = eval(sys.argv[3])

total = dict()
red = dict()
with open('board.txt', 'r') as f:
    for line in f:
        temp = map(int, line.split())
        if temp[1] > 0:
            total[temp[0]] = temp[1]
            if temp[2] < 2:
                red[temp[0]] = temp[1]
(position, weight) = getNextMove(total, red)
print position, weight
