'''
Created on Sep 20, 2013

@author: shaqal
'''
import socket
import subprocess
import threading
import math
import operator

client = None
Path_to_client_run = "client_run.txt"
Path_to_input = "input.txt"
leaderBoard = {}
citiesVisited = ()
coordChart = [ [0,0,0,0] for i in range(0,1001)]

input_file = open(Path_to_input, 'r')
j=0
for line in input_file :
    coordChart[j]=line.split()
    j+=1


def calculatePathLength(cityId):
    global citiesVisited
    cityIdList = cityId.split()
    citiesVisited = set(cityIdList)
    pathLenth = 0.0
    city1 = cityIdList[0]
    
    for i in range(1,len(cityIdList)) :
        city2 = cityIdList[i]
        pathLenth = pathLenth +\
        math.sqrt(float(((int(coordChart[int(city1)-1][1])-int(coordChart[int(city2)-1][1]))**2)+\
                        ((int(coordChart[int(city1)-1][2])-int(coordChart[int(city2)-1][2]))**2)+\
                  ((int(coordChart[int(city1)-1][3])-int(coordChart[int(city2)-1][3]))**2)))
        city1 = city2
        
    return pathLenth


def killClient():
    '''Called when time is up. 
    
    Kills process if still running'''
    
    if client:
        if client.poll()==None:
            client.kill()

serversocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)   #getting a socket
serversocket.bind(('127.0.0.1', 5006))                             #binding socket to port 5006
commands_file = open(Path_to_client_run, 'r')
serversocket.listen(1)                                             #listening on the port using the socket

for line in commands_file:

    '''This line will start your program in a new process. you need to send me your programs 
    and the command needed to start them from the terminal by 2 hrs before the class starts.
    I will just put them in a file and read() from there on consecutive runs or something.
    You should test your code on energon1 before sending me the command to run it.''' 
    client = subprocess.Popen(line.split())          
    
    #This starts a two minute timer and kills your process if it is still running after that using killclient()
    t = threading.Timer(120.0, killClient)
    t.start()
    
    #Here is where the IPC starts
    (clientsocket, address) = serversocket.accept() 
    print "connected!!"
    #Server will initiate communication with your program using this string
    whoareu = "Team_Name?"  
    clientsocket.send(whoareu)
    
    #On receiving the string "Team_Name?", you will send your team-name.
    TeamName = clientsocket.recv(1024)
    
    '''This reads the input file and passes the whole content to your program. 
    Format will be what is stated in the problem statement except 1 small change. 
    I will add a semi-colon ';' after the last entry in the file to help you know EOF.
    Refer the input file I attached.'''
    
    f = open(Path_to_input, 'r')
    totalsent = 0
    inputCoords = f.read()
    inputCoords = inputCoords + ';'
    MSGLEN = len(inputCoords) 
    while totalsent < MSGLEN:
        sent = clientsocket.send(inputCoords[totalsent:])
        if sent == 0:
            raise RuntimeError("socket connection broken")
        totalsent = totalsent + sent
    
    '''Now your program does its magic and sends me a single string consisting of 
    space separated city Ids in the order that you would like them to be visited.
    You need to add a semi-colon ';' at the end of your list.'''
    solution = ''
    while  True:
        chunk = clientsocket.recv(1024)
        if not chunk: break
        if chunk == '':
            raise RuntimeError("socket connection broken")
        solution = solution + chunk
        if ';' in solution:
            break
    
    #timer is cancelled.
    t.cancel()
    
    print "City travel order :  " + solution
    
    #a evaluate function will calculate the length of your path and keep a leader board
    TSP_path_length = calculatePathLength(solution[:-1])
    print "Number of cities visited by team " + TeamName + " : " + str(len(citiesVisited))
    print "Path length for team  " + TeamName + " :  " + str(TSP_path_length)
    
    f.close()
    clientsocket.close()
    if len(citiesVisited) == 1000 :
        leaderBoard.update({TeamName :TSP_path_length})
        
    wait = raw_input("PRESS ENTER TO CONTINUE TO NEXT TEAM")


for entry in sorted(leaderBoard.iteritems(), key = operator.itemgetter(1)):
    print entry[0] + ' : ' + str(entry[1])
