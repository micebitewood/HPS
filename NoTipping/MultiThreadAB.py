import threading
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

class Traversal(threading.Thread):
    def __init__(self, score, red, total):
        threading.Thread.__init__(self)
        self.score = score
        self.red = red
        self.total = total

    def run(self):
        with lock:
            print "start: "
            print self.red
            print self.total
            print self.score
        print traversal(self.score, self.red, self.total, False, 0)

red = dict()
total = dict()
score = []
lock = threading.Lock()
with open('input', 'r') as f:
    red = eval(f.readline())
    total = eval(f.readline())
    score = eval(f.readline())
for position in red.keys():
    weight = red[position]
    if valid(score, position, weight):
        newScore = score[:]
        decScore(newScore, position, weight)
        newRed = red.copy()
        newRed.pop(position)
        newTotal = total.copy()
        newTotal.pop(position)
        thread = Traversal(newScore, newRed, newTotal)
        thread.start()

