'''
Group discrimination testing for Subject System G
Inputs :
    argv[1] : Train file
    argv[2] : Argument to test discriminationa gainst
              8 means race and 9 means gender
'''
from __future__ import division
import random
import math
import os
from collections import defaultdict
from sklearn import linear_model
import numpy as np
import itertools
import commands
import sys

#Maximum number of inputs to test against
max_inp = 30000

#Minimum number of inputs tos tart applying the confidence check optimization
minInp=30000

#set printsuite to 0 to not print the test suite
printsuite = 1
sense_arg = int(sys.argv[2])
random.seed(15)

outputfile = "../Suites/freshG"+str(sense_arg)+".txt"
f = open(outputfile,"w")
option=4
X=[]
Y=[]
i=0
with open(sys.argv[1], "r") as ins:
    for line in ins:
        line = line.strip()
        line1 = line.split(',')
        if(i==0):
            i+=1
            continue
        X.append(map(int,line1[:-1]))
        Y.append(int(line1[-1]))
clf = linear_model.LinearRegression()
clf.fit(X, Y)

num_atr=[10,8,70,16,7,14,6,5,2,100,40,100,40]
map={}
already = "../Suites/G"+sys.argv[2]
def check_ratio(fixed,clf):

    fix_atr = []
    if option==3 or option==4:
        fixval=int(sys.argv[2])
        fpr = open(already,"r")
        done = {}
        for line in fpr:
            line =  line.strip()
            line  =  line.split(',')
            line = ','.join(line[:-1])
            line+=','
            done[line]=1
        if fixval==9:
            pos=[0,0]
            neg=[0,0]
        else:
            pos=[0,0,0,0,0]
            neg=[0,0,0,0,0]
        for inp in done.keys():
            strinp = inp
            inpstr=''
            inp = inp.split(',')
            inp=inp[:-1]
            i=0
            while i<len(inp):
                inpstr+=inp[i]+' '
                inp[i] = int(inp[i])
                i+=1
            if option==3:
                out = clf.predict([inp])[0]
            else:
                out = commands.getoutput("python Gtest.py "+sys.argv[1]+" "+sys.argv[2]+" "+inpstr)
                out = int(out)
            if out>0:
                out=1
            else:
                out=0
            if(out>0):
                pos[inp[fixval-1]]+=1
            else:
                neg[inp[fixval-1]]+=1
            if printsuite==1:
                f.write(strinp+" "+str(out)+"\n")
        i=0
        maxv = 0
        minv = 1
        while i<len(pos):
            #print pos[i],neg[i]
            v = pos[i]*1.0/(pos[i]+neg[i])
            if v > maxv :
                maxv = v
            if v < minv:
                minv = v
            i+=1
        print "score is ",100*(maxv-minv)
        return     
    num=1
    for i in range(0,len(fixed)):
        if(fixed[i]==1):
            num = num*num_atr[i]
            fix_atr.append(i)

    val = 0
    #print fix_atr, num
    max = -1
    min = 100
    #print num
    while val< num:
        inp_fix=['','','','','','','','','','','','','']
        i=len(fix_atr)-1
        tmp_val = val
            #if(val%10000==0):
            #print val
        while i>=0:
            inp_fix[fix_atr[i]] = tmp_val%num_atr[fix_atr[i]]
            tmp_val = (tmp_val - tmp_val%num_atr[fix_atr[i]])/num_atr[fix_atr[i]]
            i-=1
        #print inp_fix
        val+=1
        inp=['','','','','','','','','','','','','']
        num_inp = 0
        pos = 0
        neg = 0
        for i3 in range(0, max_inp) :
            
            j=0
            while j<len(num_atr):
                if inp_fix[j]=='':
                    inp[j] = (random.randint(0,num_atr[j]-1))
                else:
                    inp[j]=inp_fix[j]
                j+=1
           
            
            num_inp+=1
            strinp=""
            str1=""
            for x in inp:
                str1 += str(x)+","
                strinp += str(x)+" "
            if option ==1 :   
                #print "python Gtest.py "+sys.argv[1]+" "+sys.argv[2]+" "+strinp
                out = commands.getoutput("python Gtest.py "+sys.argv[1]+" "+sys.argv[2]+" "+strinp)
                out = int(out)
            else:
                out = clf.predict([inp])[0]
            if out>0:
                out=1
            else:
                out=0
            if printsuite==1:
                f.write(str1+" "+str(out)+"\n")
                #print str1,out

            #if(','.join(str(inp)) in map.keys()):
            #   continue
            
            if(out>0):
            #print inp, out, 1
                map[','.join(str(inp))] = 1
                pos+=1
            else:
                #print inp,out, 0
                map[','.join(str(inp))] = 0
                neg+=1
            frac = pos*1.0/(pos+neg)
            if 2.5*math.sqrt(frac*(1-frac)*1.0/(pos+neg)<0.05) and pos+neg>minInp:
                break
        if(pos*1.0/(pos+neg)>max):
            max = pos*1.0/(pos+neg)
        if(pos*1.0/(pos+neg)<min):
            min = pos*1.0/(pos+neg)
#print fixed,max,min, max-min
def findsubsets(S,m):
    return set(itertools.combinations(S, m))


fixed = [0,0,0,0,0,0,0,0,0,0,0,0,0]
fixed[sense_arg-1] = 1
check_ratio(fixed,clf)
f.close()
'''
for i in range(0,13):
    
    out = findsubsets([0,1,2,3,4,5,6,7,8,9,10,11,12],i+1)
    for a in out:
        fixed = [0,0,0,0,0,0,0,0,0,0,0,0,0]
        for j in range(0,13):
            if j in a:
                fixed[j]=1
    
    #print fixed
        check_ratio(fixed,clf)
'''
print "output is in",outputfile