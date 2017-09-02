'''
This script calculates the Group discrimination score for the particular input file towards race or gender.
USAGE : 
    argv[1] : Input test suite
    argv[2] : 0/1
                0 for tace
                1 for gender
'''
import sys

f = open(sys.argv[1],"r")

type = int(sys.argv[2])
#type = 0 means race
#type = 1 means gender

if(type==0):
    pos = [0,0,0,0,0]
    neg = [0,0,0,0,0]
else:
    pos =[0,0]
    neg =[0,0]

for line in f:
    line = line.strip()
    line =line.split(',')
    if(float(line[-1])>0):
        if(type==1):
	    if(int(line[8])<len(pos)):
	    	pos[int(line[8])]+=1
        else:
            pos[int(line[7])]+=1

    if(float(line[-1])<=0):
        if(type==1):
	    if(int(line[8])<len(neg)):
            	neg[int(line[8])]+=1
        else:
            neg[int(line[7])]+=1


i =0
max = 0
min = 1
while i<len(pos):
    ratio = pos[i]*1.0/(pos[i]+neg[i])
    if(ratio  >=  max):
        max = ratio
    if(ratio < min):
        min = ratio
    i+=1
val = 100*(max-min)
if(val < 0.01):
    val=0.01
print("%.2f" %val)
#print 100*(max-min)
