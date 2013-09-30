'''
Created on Sep 20, 2013

@author: shaqal
'''
import socket

<<<<<<< HEAD
=======
Path_to_tsp_so = '/home/jhl580/hps/tsp/t/tmp/jj/tsp.so'

>>>>>>> master
s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

s.connect(('127.0.0.1', 5006))
ques1=s.recv(1024)

if ques1=="Team_Name?":
    s.send("jj")

input_data=''

while True:
    chunk = s.recv(1000000)
    if not chunk: break
    if chunk == '':
        raise RuntimeError("socket connection broken")
    input_data = input_data + chunk
    if ';' in input_data:
        break

input_data = input_data[:-1]
with open('input', 'w') as f:
    f.write(input_data)
import ctypes
<<<<<<< HEAD
tsp = ctypes.CDLL('/home/jm4911/HPS/TSP/tsp.so')
=======
tsp = ctypes.CDLL(Path_to_tsp_so)
>>>>>>> master
tsp.main()
path = ""
with open('output', 'r') as f:
    path = f.read()
#calculate your solution is less than 2 minutes
<<<<<<< HEAD
solution = path
=======
solution = path + ";"
>>>>>>> master

totalsent=0
MSGLEN = len(solution) 
while totalsent < MSGLEN:

    sent = s.send(solution[totalsent:])
    if sent == 0:
        raise RuntimeError("socket connection broken")
    totalsent = totalsent + sent

s.close()
