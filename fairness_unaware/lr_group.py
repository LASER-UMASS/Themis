from __future__ import division
import random
import math
import os
from collections import defaultdict
from sklearn import linear_model
import numpy as np
import itertools

import sys

max_inp = 1000

random.seed()

X=[]
Y=[]
i=0
with open("cleaned_train", "r") as ins:
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
def check_ratio(fixed,clf):

    fix_atr = []
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
            if(num_inp>=max_inp):
                break;
            if(inp_fix[0]==''):
                inp[0]=(random.randint(0,9));
            else:
                inp[0]=inp_fix[0]
            if(inp_fix[1]==''):
                inp[1]=(random.randint(0,7));
            else:
                inp[1]=inp_fix[1]
            if(inp_fix[2]==''):
                inp[2]=(random.randint(0,39));
            else:
                inp[2]=inp_fix[2]
            if(inp_fix[3]==''):
                inp[3]=(random.randint(0,15));
            else:
                inp[3]=inp_fix[3]
            if(inp_fix[4]==''):
                inp[4]=(random.randint(0,6));
            else:
                inp[4]=inp_fix[4]
            if(inp_fix[5]==''):
                inp[5]=(random.randint(0,13));
            else:
                inp[5]=inp_fix[5]
            if(inp_fix[6]==''):
                inp[6]=(random.randint(0,5));
            else:
                inp[6]=inp_fix[6]
            if(inp_fix[7]==''):
                inp[7]=(random.randint(0,5));
            else:
                inp[7]=inp_fix[7]
            if(inp_fix[8]==''):
                inp[8]=(random.randint(0,5));
            else:
                inp[8]=inp_fix[8]
            if(inp_fix[9]==''):
                inp[9]=(random.randint(0,99));
            else:
                inp[9]=inp_fix[9]
            if(inp_fix[10]==''):
                inp[10]=(random.randint(0,39));
            else:
                inp[10]=inp_fix[10]
            if(inp_fix[11]==''):
                inp[11]=(random.randint(0,99));
            else:
                inp[11]=inp_fix[11]
            if(inp_fix[12]==''):
                inp[12]=(random.randint(0,39));
            else:
                inp[12]=inp_fix[12]
            out = clf.predict([inp])[0]
            num_inp+=1
            if(out>0):
            #print inp, out, 1
                map[','.join(str(inp))] = 1
                pos+=1
            else:
                #print inp,out, 0
                map[','.join(str(inp))] = 0
                neg+=1
        if(pos*1.0/(pos+neg)>max):
            max = pos*1.0/(pos+neg)
        if(pos*1.0/(pos+neg)<min):
            min = pos*1.0/(pos+neg)
    print fixed,max,min, max-min

def findsubsets(S,m):
    return set(itertools.combinations(S, m))


for i in range(0,13):
    
    out = findsubsets([0,1,2,3,4,5,6,7,8,9,10,11,12],i+1)
    for a in out:
        fixed = [0,0,0,0,0,0,0,0,0,0,0,0,0]
        for j in range(0,13):
            if j in a:
                fixed[j]=1
    
    #print fixed
        check_ratio(fixed,clf)
