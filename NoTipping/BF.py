#-15 -14 -13 -12 -11 -10 -9 -8 -7 -6 -5 -4 -3 -2 -1  0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15
# 0   1   2   3   4   5   6  7  8  9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30
def getProbabilities(board, red, blue, leftScore, rightScore, nextColor):
    global wins, loses
    if nextColor:# red move
        for i in range(1, 13):
            if i not in red:
                if (red + [i], blue) in loses:
                    continue
                for j in range(31):
                    if board[j] == 0:
                        newBoard = board[:]
                        newRed = red[:]
                        newLeftScore = leftScore
                        newRightScore = rightScore

                        newBoard[j] = i
                        newRed.append(i)
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
                        getProbabilities(newBoard[:], newRed, blue, newLeftScore, newRightScore, False)
    else:
        for i in range(1, 13):
            if i not in blue:
                if (red, blue + [i]) in wins:
                    continue
                for j in range(31):
                    if board[j] == 0:
                        newBoard = board[:]
                        newBlue = blue[:]
                        newLeftScore = leftScore
                        newRightScore = rightScore
                        newBoard[j] = i
                        newBlue.append(i)
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
                        getProbabilities(newBoard[:], red, newBlue, newLeftScore, newRightScore, True)

support1 = 12
support2 = 14
red = []
blue = []
board = [0 for i in range(31)]
board[11] = 3
wins = []
loses = []
leftScore = board[11] * (support1 - 11) + 3 * (support1 - 15)
rightScore = board[11] * (support2 - 11) + 3 * (support2 - 15)

choices = dict()
getProbabilities(board, red, blue, leftScore, rightScore, True)
print wins
print loses
