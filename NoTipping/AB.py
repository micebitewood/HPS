def decScore(score, position, weight):
    score[0] -= weight * (12 - position)
    score[1] -= weight * (14 - position)

def incScore(score, position, weight):
    score[0] += weight * (12 - position)
    score[1] += weight * (14 - position)

def valid(score, position, weight):
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
                if valid(score, position, weight):
                    newScore = score[:]
                    decScore(newScore, position, weight)
                    newTotal = total.copy()
                    newTotal.pop(position)
                    newRed = red.copy()
                    newRed.pop(position)
                    (redWins, newPosition) = traversal(newScore, newRed, newTotal, False, position)
                    if redWins:
                        return (True, newPosition)
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
                        return (True, newPosition)
        return (False, 0)
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
                    return (False, newPosition)
        return (True, 0)
    

#red = {6:1, 7:2, 2:3, 1:4, 8:5, 11:3}#position:weight
#total = {1:4, 2:3, 6:1, 7:2, 8:5, 11:3, 14:10, 16:3, 19:4, 21:5}
#board = [0, 4, 3, 0, 0, 0, 1, 2, 5, 0, 0, 3, 0, 0, 10, 0, 3, 0, 0, 4, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0]
#score = [-1, 85]
red = {11:3, 12:3, 13:5, 14:8, 16:7, 18:9, 19:10, 20:1, 23:6, 25:4, 27:2} #including the green 3 in position -4(which is 11 here, because the board starts from -15 and our list starts from 0)
total = {0:1, 1:2, 2:3, 3:4, 4:5, 5:6, 6:7, 7:8, 8:9, 11:3, 12:3, 13:5, 14:8, 16:7, 18:9, 19:10, 20:1, 23:6, 25:4, 27:2, 30:10} #including red, green, and blue
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
print traversal(score, red, total, True, 0)
