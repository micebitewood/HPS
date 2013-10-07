'''
@author: akshay
'''
import socket, sys

teamName = "jj"
eom = "<EOM>"
port = 5555
maxlen = 999999

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

s.connect(('127.0.0.1', port))

inpData=''

def getData(sock):
  inpData=''
  s.send(teamName)
  while True:
      chunk = sock.recv(maxlen)
      if not chunk: break
      if chunk == '':
          raise RuntimeError("socket connection broken")
      inpData = inpData + chunk
      if eom in inpData:
          break
  return inpData
  
def sendResult(sock,result):
  result += eom
  totalsent=0
  MSGLEN = len(result) 
  while totalsent < MSGLEN:
  
      sent = sock.send(result[totalsent:])
      if sent == 0:
          raise RuntimeError("socket connection broken")
      totalsent = totalsent + sent
  

inpData = getData(s)
inpData = inpData[:-6]

f = open('input', 'w')
f.write(inpData)
f.close()

import os
os.system("java ClusterMaximization > output")

f = open('output', 'r')
result = f.read()
f.close()

sendResult(s,result)

reply = getData(s)
print reply


s.close()

