'''
Causal discrimination testing for Subject System C
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
from sklearn.naive_bayes import GaussianNB
import commands
import itertools

import sys

#Maximum number of inputs to test against
max_inp = 50000

#set prinsuite to 0 to not print the test suite
printsuite = 1

#Minimum number of inputs tos tart applying the confidence check optimization
mininp=50000


random.seed(2)

#Output file for the test suite
outputfile = "../../Suites/freshCcausalrg.txt"
fout = open(outputfile,"w")

option=4
num_test=0

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
clf = GaussianNB()
clf.fit(X, Y)

num_atr=[10,8,70,16,7,14,6,5,2,100,40,100,40]
map={}
already = "../../Suites/Ccausalrg.txt"
def check_ratio(fixed,clf):
    num_test = 0
    if option==3 or option==4:
        fin = open(already,"r")
        requeried={}
        num=0
        den=0
        for line in fin:
            line = line.strip()
            line = line.split(',')
            line = line[:-1]
            i=0
            pos=0
            neg=0
            fixval=9#int(sys.argv[2])
            if fixval ==9:
                numval=10
            else:
                numval=2
            while i<numval:
                #print i,den
                line[fixval-1] = str(int(i/5))
                line[8-1] = str(int(i%5))
                strinp = ','.join(line)
                inpstr = ' '.join(line)
                if(strinp in requeried.keys()):
                    i+=1
                    continue
                else:
                    #query here
                    inp=[]
                    for a in  line:
                        inp.append(int(a))
                    if option ==4 :   
                        out = float(commands.getoutput("python Ctest.py "+sys.argv[1]+" 8 "+inpstr))
                    else:
                        out = clf.predict([inp])[0]
                    if printsuite==1:
                        fout.write(strinp+", "+str(out)+"\n")
                    if out>0:
                        pos+=1
                    else:
                        neg+=1
                    requeried[strinp] = 1
                i+=1
            if pos>0 and neg > 0:
                num+=1
            if pos>0 or neg>0:
                #print pos,neg,line,num
                den+=1
        print "Score is ",num*100.0/den
        return
    fix_atr = []
    num=1
    for i in range(0,len(fixed)):
        if(fixed[i]==1):
            num = num*num_atr[i]
            fix_atr.append(i)

    #print fix_atr, num
    max = -1
    min = 100
    #print num
    val = 0

    while val< num:
        inp_fix=['','','','','','','','','','','','','']
        i=len(fix_atr)-1
        tmp_val = val
            #if(val%10000==0):
            #print val
        while i>=0:
            inp_fix[fix_atr[i]] = int(tmp_val%num_atr[fix_atr[i]])
            tmp_val = int((tmp_val - tmp_val%num_atr[fix_atr[i]])/num_atr[fix_atr[i]])
            i-=1
        #print inp_fix
        val+=1
    inp=['','','','','','','','','','','','','']
    num_inp = 0
    pos = 0
    neg = 0
    curr_map={}
    inp=['','','','','','','','','','','','','']
    num_inp = 0
    pos = 0
    neg = 0
    curr_map={}

    for i3 in range(0, max_inp) :
        #print i3
        if(num_inp>=max_inp):
            break;
        j=0
        while j<len(num_atr):
            if inp_fix[j]=='':
               inp[j] = (random.randint(0,num_atr[j]-1))
            else:
                inp[j]=inp_fix[j]
            j+=1
        val = 0
        pos_found = 0
        neg_found = 0
        while val< num:
            inp_fix=['','','','','','','','','','','','','']
            i=len(fix_atr)-1
            tmp_val = val
            #print val
            while i>=0:
                inp[fix_atr[i]] = int(tmp_val%num_atr[fix_atr[i]])
                tmp_val = (tmp_val - tmp_val%num_atr[fix_atr[i]])/num_atr[fix_atr[i]]
                i-=1
            val+=1
            num_test+=1
            out = clf.predict([inp])[0]
            str1=""
            strinp=""
            for x in inp:
                str1 += str(x)+","
                strinp+=str(x)+" "
            if option ==1 :   
                out = commands.getoutput("python Ctest.py "+sys.argv[1]+" "+sys.argv[2]+" "+strinp)
            else:
                out = clf.predict([inp])[0]
            if printsuite==1:
                fout.write(str1+" "+str(out)+"\n")
                #f.write(str1+" "+str(out)+"\n")
                #print str1,out
            
            if(out>0):
                pos_found=1
                curr_map[','.join(str(inp))] = 1
            else:
                neg_found = 1
                curr_map[','.join(str(inp))] = 0
        if(pos_found==1 and neg_found==1):
            pos+=1
        else:
            neg+=1
        frac=pos*1.0/(pos+neg)
        if 2.5*math.sqrt(frac*(1-frac)*1.0/(pos+neg)<0.05) and pos+neg>mininp:
            #print pos,neg
            break
        num_inp+=1
    print "output is in ",outputfile
    print  pos*1.0/(neg+pos),num_test
def findsubsets(S,m):
    return set(itertools.combinations(S, m))

fixedatr = 9#int(sys.argv[2])
fixed = [0,0,0,0,0,0,0,0,0,0,0,0,0]
fixed[fixedatr-1]=1
fixed[8-1]=1

check_ratio(fixed,clf)
fout.close()
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