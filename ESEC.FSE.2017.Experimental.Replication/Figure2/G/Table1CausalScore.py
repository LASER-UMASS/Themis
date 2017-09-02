'''
This script calculates the Causal discrimination score for the particular input file towards race or gender.
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

pos = 0
neg = 1

rowNum = 0
num=0
den=0
posFound = 0
negFound = 0
iter = 0
lst = []
lines =[]
for line in f:
    line = line.strip()
    l1 = line
    lines.append(l1)
    line =line.split(',')
    
    if(float(line[-1])>0):
        posFound=1
    if(float(line[-1])<=0):
        negFound=1

    rowNum+=1
    if(rowNum==6):
        rowNum=0
        if(posFound==1 and negFound==1):
            num+=1
            lst.append(iter/5*5)
        #print l1,iter
        den+=1
        posFound = 0
        negFound = 0
    iter +=1
val = num*100.0/den
if(val < 0.01):
    val=0.01
print("%.2f"%val)
