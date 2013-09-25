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

def blueMovable():
    for i in range(31):
        weight = board[i]
        if weight == 0:
            continue
        if valid(score, i):
            return True
    return False

def redMovable():
    for weight in range(1, 11):
        position = red[weight]
        if position != 100:
            if valid(score, position):
                return True
    return False

def redMove(score, red, blue, board):
    redExists = False
    for weight in red:
        if weight != 100:
            redExists = True
            break

    if redExists:
        if redMovable():
            #move
            success = False
            weight = 0
            position = 0
            while not success:
                weight = random.randint(1, 11)
                if weight == 11:
                    position = 11
                    weight = 3
                    if board[position] == 3:
                        success = valid(score, position)
                else:
                    position = red[weight]
                    if position == 100:
                        continue
                    success = valid(score, position)
            decScore(score, position)
            print "removing {0} of red from {1} in board".format(weight, position)
            print score
            red[weight] = 100
            board[position] = 0
            return False
        else:
            return True
    else:
        return blueMove(score, red, blue, board)

def blueMove(score, red, blue, board):
    if blueMovable():
        success = False
        rand = 0
        while not success:
            rand = random.randint(0, 30)
            if board[rand] == 0:
                continue
            success = valid(score, rand)
        weight = board[rand]
        color = red
        if blue[weight] == rand:
            color = blue
        decScore(score, rand)
        print "removing {0} from {1} in board".format(weight, rand)
        print score
        color[weight] = 100
        board[rand] = 0
        return False
    return True

import random
red = [100, 23, 20, 16, 19, 22, 7, 10, 12, 13, 14]
blue = [100, 0, 1, 2, 4, 15, 17, 18, 3, 21, 24]
board = [1, 2, 3, 8, 4, 0, 0, 6, 0, 0, 7, 3, 8, 9, 10, 5, 3, 6, 7, 4, 2, 9, 5, 1, 10, 0, 0, 0, 0, 0, 0]
score = [-9, -3]
for i in range(31):
    incScore(score, i)
redLose = False
blueLose = False
for i in range(10):
    redLose = False
    blueLose = False
    print 'red:'
    redLose = redMove(score, red, blue, board)
    if redLose:
        print "red loses"
        break
    print 'blue:'
    blueLose = blueMove(score, red, blue, board)
    if blueLose:
        print "blue loses"
        break
