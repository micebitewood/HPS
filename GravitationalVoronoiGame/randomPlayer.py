#10x10 board, two players, 5 stones for each player
def closest(board, color1, color2):
    if len(color2) == 0:
        move(board, color1)
    else:
        maxScore = 0
        maxPosition = (0, 0)
        for position in color2:
            for i in [-1, 0, 1]:
                x = position[0] + i
                if x < 0 or x > 9:
                    continue
                for j in [-1, 0, 1]:
                    y = position[1] + j
                    if y < 0 or y > 9:
                        continue
                    if board[x][y] == 0:
                        newColor1 = color1[:]
                        newColor1.append((x, y))
                        score = getScore(newColor1, color2)
                        if score[0] > maxScore:
                            print x, y, score
                            print newColor1, color2
                            maxScore = score[0]
                            maxPosition = (x, y)
        print "choose", maxPosition
        color1.append(maxPosition)

def move(board, color):
    import random
    isValid = False
    while not isValid:
        x = random.randint(0, 9)
        y = random.randint(0, 9)
        if board[x][y] == 0:
            isValid = True
            board[x][y] = 1
            color.append((x, y))

def redMove(board, red):
    move(board, red)
    #closest(board, red, blue)

def blueMove(board, blue):
    print "blue first"
    #move(board, blue)
    closest(board, blue, red)

def getPull(i, j, color):
    pull = 0
    import math
    for location in color:
        if location[0] == i and location[1] == j:
            return 9999999999
        pull += 1/(math.sqrt((location[0] - i) ** 2 + (location[1] - j) ** 2))
    return pull

def getScore(color1, color2):
    score1 = 0
    score2 = 0
    for i in range(10):
        for j in range(10):
            pull1 = getPull(i, j, color1)
            pull2 = getPull(i, j, color2)
            if pull1 > pull2:
                score1 += 1
            elif pull1 < pull2:
                score2 += 1
    return (score1, score2)

redCount = 0
blueCount = 0
tieCount = 0
for i in range(1):
    board = [[0 for i in range(10)] for j in range(10)]
    red = []
    blue = []
    for n in range(5):
        redMove(board, red)
        blueMove(board, blue)
    score = getScore(red, blue)
    if score[0] > score[1]:
        redCount += 1
    elif score[0] < score[1]:
        blueCount += 1
    else:
        tieCount += 1
print redCount, blueCount, tieCount
