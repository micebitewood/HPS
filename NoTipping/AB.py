def decScore(score, position):
    weight = board[position]
    score[0] -= weight * (12 - position)
    score[1] -= weight * (14 - position)

def incScore(score, position):
    weight = board[position]
    score[0] += weight * (12 - position)
    score[1] += weight * (14 - position)

def valid(score, position):
    newScore = score[:]
    decScore(newScore, position)
    if newScore[0] < 0 and newScore[1] > 0:
        return True
    return False

def traversal(score, red, total, isRedTurn, doNotTraverse, count):
    children = dict()#position:(possibility, position)
    option = False
    if isRedTurn:
        if len(red) != 0:
            for position in red.keys():
                if count == 0:
                    print "next option"
                if not valid(score, position):
                    children[position] = (0, position)
                else:
                    newScore = score[:]
                    decScore(newScore, position)
                    newTotal = total.copy()
                    newTotal.pop(position)
                    newRed = red.copy()
                    newRed.pop(position)
                    children[position] = traversal(newScore, newRed, newTotal, False, option, count + 1)
                    if children[position][0] == 0:
                        print "red choosing {0} is terrible in {1}".format(position, count)
                    if not option and children[position][0] == 1:
                        option = True
        else:
            for position in total.keys():
                if not valid(score, position):
                    children[position] = (0, position)
                else:
                    newScore = score[:]
                    decScore(newScore, position)
                    newTotal = total.copy()
                    newTotal.pop(position)
                    children[position] = traversal(newScore, red, newTotal, False, option, count + 1)
                    if children[position][0] == 0:
                        print "red choosing {0} is terrible in {1}".format(position, count)
                    if not option and children[position][0] == 1:
                        option = True
        isValid = False
        validPosition = 0
        for position in children.keys():
            if children[position][0] != 0:
                isValid = True
                validPosition = position
                if children[position][0] == 2:
                    return (2, position)
        if not isValid:
            return (0, 0)
        return (1, validPosition)
    else:
        for position in total.keys():
            if not valid(score, position):
                children[position] = (0, position)
            else:
                newScore = score[:]
                decScore(newScore, position)
                newTotal = total.copy()
                newTotal.pop(position)
                newRed = red.copy()
                if position in red.keys():
                    newRed.pop(position) 
                children[position] = traversal(newScore, newRed, newTotal, True, False, count + 1)
                if children[position][0] == 0:
                    print "if blue chooses {0} in {1}, red will lose".format(position, count)
                    return (0, position)
                elif doNotTraverse and children[position][0] == 1:
                    return (1, position)
        isValid = False
        for position in children.keys():
            if children[position][0] != 0:
                return (1, position)
        return (2, 0)
    

def getPath(red, total, score):
    print traversal(score, red, total, True, False, 0)

#red = {6:1, 7:2, 2:3, 1:4, 8:5, 11:3}#position:weight
#total = {1:4, 2:3, 6:1, 7:2, 8:5, 11:3, 14:10, 16:3, 19:4, 21:5}
#board = [0, 4, 3, 0, 0, 0, 1, 2, 5, 0, 0, 3, 0, 0, 10, 0, 3, 0, 0, 4, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0]
#score = [-1, 85]
red = {11:3, 12:3, 13:5, 14:8, 16:7, 18:9, 19:10, 20:1, 23:6, 25:4, 27:2} #including the green 3 in position -4(which is 11 here, because the board starts from -15 and our list starts from 0)
total = {0:1, 1:2, 2:3, 3:4, 4:5, 5:6, 6:7, 7:8, 8:9, 11:3, 12:3, 13:5, 14:8, 16:7, 18:9, 19:10, 20:1, 23:6, 25:4, 27:2, 30:10} #including red, green, and blue
board = [1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 0, 3, 3, 5, 8, 0, 7, 0, 9, 10, 1, 0, 0, 6, 0, 4, 0, 2, 0, 0, 10] #the board starts from -15 and the list starts from 0
score = [-215, 17]
#red = {11:3, 16:3, 17:8, 18:9, 19:10, 20:7, 21:2, 23:6, 26:5}
#total = {0:1, 1:2, 2:3, 3:4, 4:5, 5:6, 6:7, 7:8, 11:3, 16:3, 17:8, 18:9, 19:10, 20:7, 21:2, 23:6, 26:5}
#board = [1, 2, 3, 4, 5, 6, 7, 8, 0, 0, 0, 3, 0, 0, 0, 0, 3, 8, 9, 10, 7, 2, 0, 6, 0, 0, 5, 0, 0, 0, 0]
#score = [-128, 56]
#red = {11:3, 15:6}
#total = {9:3, 11:3, 15:6}
#board = [0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 3, 0, 0, 0, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
#score = [-15, 15]
#red = {11:3, 15:6, 18:3}
#total = {6:6, 10:3, 11:3, 15:6, 18:3}
#board = [0, 0, 0, 0, 0, 0, 6, 0, 0, 0, 3, 3, 0, 0, 0, 6, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
#score = [0, 48]

getPath(red, total, score)
