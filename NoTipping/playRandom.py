sizeBoard = 31
offset = 15
supports = [-3, -1]

import random

def playRandomAdd(board, torque, blocks):
	possibleMoves = []
	
	for pos in range(-offset, sizeBoard-offset):
		if board[offset+pos] > 0:
			continue
		
		for block in blocks:
			if (torque[0]+(pos-supports[0])*block) * (torque[1]+(pos-supports[1])*block) <= 0:
				possibleMoves += [[pos, block]]
	
	if len(possibleMoves) == 0:
		for pos in range(-offset, sizeBoard-offset):
			if(board[offset+pos] == 0):
				return [pos, block[0]]
	
	idx = random.randrange(len(possibleMoves))
	return possibleMoves[idx]

def playRandomRemove(board, color, torque, turn):
	possibleMoves = []
	
	for pos in range(-offset, sizeBoard-offset):
		if board[offset+pos] == 0:
			continue
		
		if turn == 0 and color[offset+pos] == 1:
			continue
		
		if (torque[0]-(pos-supports[0])*board[offset+pos]) * (torque[1]-(pos-supports[1])*board[offset+pos]) <= 0:
			possibleMoves += [pos]
	
	if len(possibleMoves) == 0:
		if turn == 0:
			return playRandomRemove(board, color, torque, 1)
		
		for pos in range(-offset, sizeBoard-offset):
			if(board[offset+pos] > 0):
				return pos
	
	idx = random.randrange(len(possibleMoves))
	return possibleMoves[idx]

def playRandom(board, color, torque, blocks, turn):
	if blocks != []:
		move = playRandomAdd(board, torque, blocks)
	else:
		move = playRandomRemove(board, color, torque, turn)
	
	return move
