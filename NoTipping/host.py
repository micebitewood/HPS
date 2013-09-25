sizeBoard = 31
offset = 15
supports = [-3, -1]

pressEnterToProceed = 1

import playRandom

# Plug in algorithms here
playerRed = playRandom.playRandom
playerBlue = playRandom.playRandom

# Input arguments: (board, color, torque, blocks, turn)
#   board: 31-element array containing weight blocks of current board state
#   color: 31-element array containing color of each block (0: red, 1: blue, -1: original)
#   torque: 2-element array containing current torques of each support
#   blocks: list of blocks available for the current player
#   turn: flag denoting the current player (0: red, 1: blue)

# Output arguments
#   add phase: [position, block]
#   remove phase: position

# Displays current game status
def displayStatus(board, color, torque, blocksRed, blocksBlue):
	print
	print '============================================================= (A:10, B:11, C:12)'
	
	for block in board:
		if block == 0:
			print ' ',
		else:
			print '%X' % block,
	print ': Blocks'
	
	for i in range(sizeBoard):
		if board[i] == 0:
			print ' ',
		elif color[i] == 0:
			print 'R',
		elif color[i] == 1:
			print 'B',
		else:
			print 'O',
	print ': Colors'
	
	print '+-----+-----+-----+-----+---+-+-----+-----+-----+-----+-----+'
	print 'F     C     9     6     |   | 0     3     6     9     C     F'
	print '                     %+4d  %+4d                               : Torques' % (torque[0], torque[1])
	print 'Remaining blocks for red :', blocksRed
	print 'Remaining blocks for blue:', blocksBlue

# Start game and return the loser
def startGame():
	board = [0 for i in range(sizeBoard)]
	board[offset-4] = 3
	color = [0 for i in range(sizeBoard)]
	color[offset-4] = -1
	torqueCur = [6, -6]
	
	blocksRed = range(1, 13)
	blocksBlue = range(1, 13)
	
	# 0 for red, 1 for blue
	turn = 0
	
	displayStatus(board, color, torqueCur, blocksRed, blocksBlue)
	
	print 'Starting add phase!'
	while len(blocksRed) + len(blocksBlue) > 0:
		if pressEnterToProceed:
			if turn == 0:
				print "Red's turn: ",
			else:
				print "Blue's turn: ",
			raw_input('Press enter to proceed')
		
		if turn == 0:
			move = playerRed(board, color, torqueCur, blocksRed, turn)
			blocksRed = [block for block in blocksRed if block != move[1]]
		else:
			move = playerBlue(board, color, torqueCur, blocksBlue, turn)
			blocksBlue = [block for block in blocksBlue if block != move[1]]
		
		if turn == 0:
			print 'Red',
		else:
			print 'Blue',
		print 'places', move[1], 'at', move[0]
		
		torqueCur[0] += (move[0]-supports[0])*move[1]
		torqueCur[1] += (move[0]-supports[1])*move[1]
		board[offset+move[0]] = move[1]
		color[offset+move[0]] = turn
		
		displayStatus(board, color, torqueCur, blocksRed, blocksBlue)
		
		if torqueCur[0] * torqueCur[1] > 0:
			if turn == 0:
				print 'Tipped, blue wins!'
			else:
				print 'Tipped, red wins!'
			return turn
		
		turn = 1-turn
	
	print 'Add phase finished. Now starting remove phase!'
	while sum(board) > 0:
		if pressEnterToProceed:
			if turn == 0:
				print "Red's turn: ",
			else:
				print "Blue's turn: ",
			raw_input('Press enter to proceed')
		
		if turn == 0:
			move = playerRed(board, color, torqueCur, [], turn)
		else:
			move = playerBlue(board, color, torqueCur, [], turn)
		
		if turn == 0:
			print 'Red',
		else:
			print 'Blue',
		print 'removes', board[offset+move], 'at', move
		
		torqueCur[0] -= (move-supports[0])*board[offset+move]
		torqueCur[1] -= (move-supports[1])*board[offset+move]
		board[offset+move] = 0;
		
		displayStatus(board, color, torqueCur, blocksRed, blocksBlue)
		
		if torqueCur[0] * torqueCur[1] > 0:
			if turn == 0:
				print 'Tipped, blue wins!'
			else:
				print 'Tipped, red wins!'
			return turn
		
		turn = 1-turn
	
	print 'Remove phase finished. Draw!'
	
	return -1

# Play the game
loser = startGame()
