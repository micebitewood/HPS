#!/usr/bin/env python

#  validator.py (Python2.3 or higher required) [oct. 14 2004]

#  [Original] by Yusuke Shinyama (yusuke at cs dot nyu dot edu)

#  modified to accomodate new format and rules, fall 2013
#  by Akshay Kumar [oct. 3 2013] (ak4126 at nyu dot edu)

import sys, re, fileinput



# Exception class
class ValidationError(ValueError): pass
class FormatSyntaxError(ValidationError): pass
class DataMismatchError(ValidationError): pass
class IllegalPlanError(ValidationError): pass


##  Person object
##
PID = 0
AMBCAPACITY = 4
class Person:

  def __init__(self, x, y, st):
    global PID
    self.pid = PID
    self.x = x
    self.y = y
    self.st = st
    self.rescued = False
    self.rescTime = -1
    self.rescAmbid = -1
    self.rescLoc = (-1,-1)
    PID += 1
    return
  
  def rescue(self,time,ambid,loc):
    self.rescued = True
    self.rescTime = time
    self.rescLoc = loc
    self.rescAmbid = ambid
    return
  
  def __repr__(self):
    return '%d: (%d,%d,%d)' % (self.pid, self.x, self.y, self.st)


## Ambulance object
AMBID = 0
class Ambulance:
  def __init__(self,hid,x,y):
    global AMBID
    self.id = AMBID
    self.hid = hid
    self.x = x
    self.y = y
    self.time = 0
    self.path = []
    self.pers = []
    
    AMBID += 1
    return

  def updateLocation(self,x,y):
    self.x = x
    self.y = y
    return
  def pick(self,pid):
    if len(self.pers) == AMBCAPACITY:
      raise IllegalPlanError('Cannot rescue more than %d people at once: %d' % (AMBCAPACITY, pid))
    else:
      self.pers.append(pid)
    return
      
  def drop(self):
    self.pers = []
    return  
  def inctime(self,t):
    self.time += t
    return
  def __repr__(self):
    return '%d %d: (%d,%d)' % (self.hid,self.id,self.x,self.y)
    
##  Hostpital object
##
HID = 0
class Hospital:
  
  def __init__(self, x, y, namb):
    global HID
    self.hid = HID
    self.x = x
    self.y = y
    self.ambids = []
    self.setLoc = False
    HID += 1
    return
    
  def __repr__(self):
    return '%d: (%d,%d) %d' % (self.hid, self.x, self.y, len(self.ambtime))
  
 
  def updateLocation(self,x,y):
    self.x = x
    self.y = y
    self.setLoc = True
    return
  
  def addAmbu(self,ambid):
    self.ambids.append(ambid)
    return
  

# readdata
def readdata(fname):
  print >>sys.stderr, 'Reading data:', fname
  persons = []
  hospitals = []
  ambulances = []
  mode = 0
  for line in file(fname):
    line = line.strip().lower()
    if line.startswith("person") or line.startswith("people"):
      mode = 1
    elif line.startswith("hospital"):
      mode = 2
    elif line:
      (a,b,c) = (0,0,0)
      if mode == 1:
        (a,b,c) = map(int, line.split(","))
        persons.append(Person(a,b,c))
      elif mode == 2:
        a = int(line)
        hospitals.append(Hospital(0,0,a))
        for i in range(a):
          ambulances.append(Ambulance(HID-1,0,0))
          hospitals[HID-1].addAmbu(AMBID-1)
          
      #print a," ",b," ",c
  return (persons, hospitals, ambulances)


# read_results
def readresults(persons, hospitals, ambulances):
  print >>sys.stderr, 'Reading results...'
  hp1 = re.compile(r'\d+\s*\(\s*\d+\s*,\s*\d+\s*\)')
  hp2 = re.compile('\d+')
  
  for line in fileinput.input():
    line = line.strip().lower()
    if not line: continue
    if line.startswith('hospital'):
      line = line.strip('hospital').strip()
      hos = hp1.findall(line)
      
      for i in range(len(hos)):
        (hid,x,y) = map(int,hp2.findall(hos[i]))
        if hid<0 or hid>len(hospitals):
          print "ERROR: invalid hospital id"
          return
        else:
          h = hospitals[hid]
          h.updateLocation(x,y)
          for j in h.ambids:
            ambulances[j].updateLocation(x,y)
        
#        print hid," ",x," ",y
      continue

    if line.startswith('ambulance'):
      line = line.strip('ambulance').strip()
      (ambid,path) = line.split(None,1)
      ambid = int(ambid)
      for s in path.split(';'):
        move = map(int,hp2.findall(s))
        if len(move) == 0:
          continue # blank move, or line
        if len(move) == 4:
          # pick up a person (id, x, y, time)
          ambulances[ambid].path.append((move[0],move[1],move[2],move[3]))
        if len(move) == 2:
          # drop off at hospital  (-1,x,y) , -1 means its drop off move
          ambulances[ambid].path.append((-1,move[0],move[1]))
#      print >>sys.stderr, '!!! Ignored: %r' % line
      continue

  for i in range(len(hospitals)):
    if hospitals[i].setLoc == False:
      print "WARNING: Hospital location not provided, id = ",hospitals[i].hid," Defaulting to (0,0)" 
      
  return

def dist(a,b):
 return abs(a[0]-b[0])+abs(a[1]-b[1])

def isValidDropLocation(loc):
  flag = False
  (x,y) = loc
  for i in range(len(hospitals)):
    h = hospitals[i]
    if (h.x == x) and (h.y == y):
      flag = True
      break
  return flag
 
def validateAndScore(persons, hospitals, ambulances):
  print >>sys.stderr, 'Validating and scoring results...'
  score = 0
  for i in range(len(ambulances)):
    amb = ambulances[i]    
    plen = len(amb.path)
    if plen > 0:
      path = amb.path
      prev = loc = (amb.x,amb.y)
      pers = []
      print "\nAmbulance id = ",amb.id
      print "Time",0,"@", loc,": Starting"
      for j in range(plen):
        move = path[j]
        pid = move[0]

        #Pick up
        if pid >= 0 and pid < len(persons):
          if len(pers) == AMBCAPACITY:
            raise IllegalPlanError('Cannot carry more than %d people at once: %d' % (AMBCAPACITY, move[0]))
          else:
            p = persons[pid]
            loc = (p.x,p.y)
            if p.rescued == False:
              pers.append(pid)
              amb.inctime(1+dist(prev,loc))
              print "Time",amb.time, "@",loc,": Picked ", pid
              prev = loc
              
            else:
              p = persons[pid]
              print 'Time', amb.time,"@",loc,": ERROR: Person gets rescued on other path (persId,time,ambuId,loc)=",pid,p.rescTime,p.rescAmbid,p.rescLoc
              raise IllegalPlanError('Cannot pick rescued person')
              
        
        # Drop    
        elif pid == -1:
          loc = (move[1],move[2])
          if isValidDropLocation(loc):
            
            amb.inctime(1+dist(prev,loc))
            for k in range(len(pers)):
              tmpid = pers[k]
              if persons[tmpid].st >= amb.time:
                if persons[tmpid].rescued == False:
                  print 'Time', amb.time,"@",loc,": Rescued person (id,life)", tmpid,persons[tmpid].st
                  persons[tmpid].rescue(amb.time,i,loc)
                  score += 1
                else:
                  p = persons[tmpid]
                  print 'Time', amb.time,"@",loc,": ERROR: Person gets rescued on other path (persId,time,ambId,loc)=",tmpid,p.rescTime,p.rescAmbid,p.rescLoc
                  raise IllegalPlanError('Cannot rescue already rescued person')
              else:
                print 'Time', amb.time,"@",loc,": Person died in ambulance, (id,life) = ", tmpid,persons[tmpid].st
                
            pers = []
            prev = loc           
            continue
          else:
            raise IllegalPlanError('Invalid Drop Location = (%d,%d)' % (loc[0],loc[1]))
            
        else:
          raise IllegalPlanError('Invalid Person id = %d' % pid)
          
  
  print '\nTotal persons saved:', score
  return

# main
if __name__ == "__main__":
  if len(sys.argv) < 3:
    print 'usage: validator.py datafile resultfile'
    sys.exit(2)
  (persons, hospitals, ambulances) = readdata(sys.argv[1])
  del sys.argv[1]
  readresults(persons, hospitals, ambulances)
#  for i in range(len(hospitals)):
#    print hospitals[i], " ", hospitals[i].ambids
#  for i in range(len(ambulances)):
#    a = ambulances[i]
#    if len(a.path) > 0:
#      print "Ambulance",i, "Path =",a.path
  validateAndScore(persons,hospitals,ambulances)
    
