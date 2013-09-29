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

def traversal(score, red, total, isRedTurn, lastPosition):
    print score
    print red
    print total
    print isRedTurn
    print lastPosition
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
                    print "red chooses {0}".format(position)
                    (redWins, newPosition) = traversal(newScore, newRed, newTotal, False, position)
                    if redWins:
                        print "red wins by choosing {0}".format(position)
                        return (False, position)
        else:
            for position in total.keys():
                weight = total[position]
                if valid(score, position, weight):
                    newScore = score[:]
                    decScore(newScore, position, weight)
                    newTotal = total.copy()
                    newTotal.pop(position)
                    print "red chooses {0}".format(position)
                    (redWins, newPosition) = traversal(newScore, red, newTotal, False, position)
                    if redWins:
                        print "red wins by choosing {0}".format(position)
                        return (False, position)
        print "if blue chooses {0}, red will lose".format(lastPosition)
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
                print "blue chooses {0}".format(position)
                (blueWins, newPosition) = traversal(newScore, newRed, newTotal, True, position)
                if blueWins:
                    print "blue wins by choosing {0}".format(position)
                    return (False, position)
        print "if red chooses {0}, blue will lose".format(lastPosition)
        return (True, 0)
        
def getNextMove(total, red):
    score = [0, 0]
    for position in total.keys():
        incScore(score, position, total[position])
        
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
                print "red chooses {0}".format(position)
                (redWins, newPosition) = traversal(newScore, newRed, newTotal, False, position)
                if redWins:
                    return (position, total[position])
        for position in red.keys():
            print "red will lose"
            return (position, total[position])
    else:
        for position in total.keys():
            weight = total[position]
            if valid(score, position, weight):
                newScore = score[:]
                decScore(newScore, position, weight)
                newTotal = total.copy()
                newTotal.pop(position)
                print "red chooses {0}".format(position)
                (redWins, newPosition) = traversal(newScore, red, newTotal, False, position)
                if redWins:
                    return (position, total[position])
        for position in total.keys():
            print "red will lose"
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

