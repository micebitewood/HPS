#-15 -14 -13 -12 -11 -10 -9 -8 -7 -6 -5 -4 -3 -2 -1  0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15
# 0   1   2   3   4   5   6  7  8  9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30
def remove(board, red, blue, leftScore, rightScore, nextColor, count):
    global wins, loses
    if nextColor:
        if count == 0:
            for j in range(len(board)):
                if board[j] != 0:
                    if blue[board[j]] == j:
                        newBlue = blue[:]
                        newBoard = board[:]
                        i = board[j]
                        newBoard[j] = 0
                        newBlue[i] = -2
                        newLeftScore = leftScore - i * (support1 - j)
                        if newLeftScore > 0:
                            loses.append((red[:], newBlue[:]))
                            print "lose: {0} {1} {2}".format(newBoard, red, newBlue)
                            continue
                        newRightScore = rightScore - i * (support2 - j)
                        if newRightScore < 0:
                            loses.append((red[:], newBlue[:]))
                            print "lose: {0} {1} {2}".format(newBoard, red, newBlue)
                            continue
                        remove(newBoard, red, newBlue, newLeftScore, newRightScore, False, count)
                        
        else:
            for i in range(1, 13):
                if red[i] != -2:
                    newRed = red[:]
                    newBoard = board[:]
                    j = newRed[i]
                    newBoard[j] = 0
                    newRed[i] = -2
                    newLeftScore = leftScore - i * (support1 - j)
                    if newLeftScore > 0:
                        loses.append((newRed[:], blue[:]))
                        print "lose: {0} {1} {2}".format(newBoard, newRed, blue)
                        continue
                    newRightScore = rightScore - i * (support2 - j)
                    if newRightScore < 0:
                        loses.append((newRed[:], blue[:]))
                        print "lose: {0} {1} {2}".format(newBoard, newRed, blue)
                        continue
                    remove(newBoard, newRed, blue, newLeftScore, newRightScore, False, count - 1)
    else:
        for j in range(len(board)):
            if board[j] != 0:
                if red[board[j]] == j:
                    newRed = red[:]
                    newBoard = board[:]
                    i = board[j]
                    newLeftScore = leftScore - i * (support1 - j)
                    if newLeftScore > 0:
                        wins.append((newRed[:], blue[:]))
                        print "win: {0} {1} {2}".format(newBoard, newRed, blue)
                        continue
                    newRightScore = rightScore - i * (support2 - j)
                    if newRightScore < 0:
                        wins.append((newRed[:], blue[:]))
                        print "win: {0} {1} {2}".format(newBoard, newRed, blue)
                        continue
                    newBoard[j] = 0
                    newRed[i] = -2
                    remove(newBoard, newRed, blue, newLeftScore, newRightScore, True, count - 1)
                elif blue[board[j]] == j:
                    newBlue = blue[:]
                    newBoard = board[:]
                    i = board[j]
                    newLeftScore = leftScore - i * (support1 - j)
                    if newLeftScore > 0:
                        wins.append((red[:], newBlue[:]))
                        print "win: {0} {1} {2}".format(newBoard, red, newBlue)
                        continue
                    newRightScore = rightScore - i * (support2 - j)
                    if newRightScore < 0:
                        wins.append((red[:], newBlue[:]))
                        print "win: {0} {1} {2}".format(newBoard, red, newBlue)
                        continue
                    newBoard[j] = 0
                    newBlue[i] = -2
                    remove(newBoard, red, newBlue, newLeftScore, newRightScore, True, count)
                    

def getProbabilities(board, red, blue, leftScore, rightScore, nextColor, count):
    global wins, loses
    if count  == 12:
        remove(board, red, blue, leftScore, rightScore, nextColor, 12)
    if nextColor:# red move
        for i in range(1, 13):
            if red[i] == -1:
                for j in range(31):
                    if board[j] == 0:
                        newBoard = board[:]
                        newRed = red[:]
                        newBoard[j] = i
                        newRed[i] = j
                        newLeftScore = leftScore + i * (support1 - j)
                        if newLeftScore > 0:
                            loses.append((newRed[:], blue[:]))
                            print "lose: {0} {1} {2}".format(newBoard, newRed, blue)
                            continue
                        newRightScore = rightScore + i * (support2 - j)
                        if newRightScore < 0:
                            loses.append((newRed[:], blue[:]))
                            print "lose: {0} {1} {2}".format(newBoard, newRed, blue)
                            continue
                        getProbabilities(newBoard, newRed, blue, newLeftScore, newRightScore, False, count)
    else:
        for i in range(1, 13):
            if blue[i] == -1:
                for j in range(31):
                    if board[j] == 0:
                        newBoard = board[:]
                        newBlue = blue[:]
                        newBoard[j] = i
                        newBlue[i] = j
                        newLeftScore = leftScore + i * (support1 - j)
                        if newLeftScore > 0:
                            wins.append((red[:], newBlue[:]))
                            print "win: {0} {1} {2}".format(newBoard, red, newBlue)
                            continue
                        newRightScore = rightScore + i * (support2 - j)
                        if newRightScore < 0:
                            wins.append((red[:], newBlue[:]))
                            print "win: {0} {1} {2}".format(newBoard, red, newBlue)
                            continue
                        getProbabilities(newBoard, red, newBlue, newLeftScore, newRightScore, True, count + 1)

support1 = 12
support2 = 14
red = [-1 for i in range(13)]
blue = [-1 for i in range(13)]
board = [0 for i in range(31)]
board[11] = 3
wins = []
loses = []
leftScore = board[11] * (support1 - 11) + 3 * (support1 - 15)
rightScore = board[11] * (support2 - 11) + 3 * (support2 - 15)

choices = dict()
getProbabilities(board, red, blue, leftScore, rightScore, True, 0)
print wins
print loses
