#10x10 board, two players, 5 stones for each player
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

def blueMove(board, blue):
    move(board, blue)

def getPull(i, j, color):
    pull = 0
    import math
    for location in color:
        if location[0] == i and location[1] == j:
            return 9999999999
        pull += 1/(math.sqrt((location[0] - i) ** 2 + (location[1] - j) ** 2))
    return pull

def getScore():
    redScore = 0
    blueScore = 0
    for i in range(10):
        for j in range(10):
            redPull = getPull(i, j, red)
            bluePull = getPull(i, j, blue)
            if redPull > bluePull:
                redScore += 1
            elif redPull < bluePull:
                blueScore += 1
    return (redScore, blueScore)

redCount = 0
blueCount = 0
tieCount = 0
for i in range(10000):
    board = [[0 for i in range(10)] for j in range(10)]
    red = []
    blue = []
    for n in range(5):
        redMove(board, red)
        blueMove(board, blue)
    score = getScore()
    if score[0] > score[1]:
        redCount += 1
    elif score[0] < score[1]:
        blueCount += 1
    else:
        tieCount += 1
print redCount, blueCount, tieCount
