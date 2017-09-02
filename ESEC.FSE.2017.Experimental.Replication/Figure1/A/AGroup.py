'''
Group discrimination testing for Subject System A
Inputs :
    argv[1] : Train file
    argv[2] : Sensitive argument
    argv[3] : Argument to test discriminationa gainst
    For argv[2] and argv[3] : 8 means race and 9 means gender
'''
from __future__ import division
from random import seed, shuffle
import random
import math
import os
from collections import defaultdict
from sklearn import svm
import os,sys
import urllib2
sys.path.insert(0, './fair_classification/') # the code for fair classification is in this directory
import utils as ut
import numpy as np
import itertools
import loss_funcs as lf # loss funcs that can be optimized subject to various constraints
import commands

sens_arg = int(sys.argv[2])
fixval = int(sys.argv[3])

#set prinsuite to 0 to not print the test suite
printsuite = 1

#Minimum number of inputs tos tart applying the confidence check optimization
minInp = 50000

#Maximum number of inputs to test against
max_inp=50000

#Training file
trainfile =  sys.argv[1]

#Output file for the test suite
outputfile = "../Suites/freshA"+str(sens_arg)+str(fixval)

random.seed(12)



if(sens_arg== 9):
    name = 'sex'
    cov=0
else:
    name = 'race'
    cov  = [0.2,0.2,0.2,0.2,0.2,0.2]

X=[]
Y=[]
i=0
sensitive = {};
sens = []

option=4

with open(trainfile, "r") as ins:
    for line in ins:
        line = line.strip()
        line1 = line.split(',')
        if(i==0):
            i+=1
            continue
        L = map(int,line1[:-1])
        sens.append(L[sens_arg-1])
        #L[sens_arg-1]=-1
        X.append(L)
        
        if(int(line1[-1])==0):
            Y.append(-1)
        else:
            Y.append(1)

X = np.array(X, dtype=float);
Y = np.array(Y, dtype = float);
sensitive[name] = np.array(sens, dtype = float);
loss_function = lf._logistic_loss;
sep_constraint = 0;
sensitive_attrs = [name];
sensitive_attrs_to_cov_thresh = {name:cov};

gamma=None

w = ut.train_model(X, Y, sensitive, loss_function, 1, 0, sep_constraint, sensitive_attrs, sensitive_attrs_to_cov_thresh, gamma);


num_atr=[10,8,70,16,7,14,6,5,2,100,40,100,40]
map={}
f = open(outputfile,"w")
already = "../Suites/A"+str(sens_arg)+str(fixval)
def check_ratio(fixed):
    fix_atr = []

    if option==3 or option==4:
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
            inp = inp.split(',')
            inp=inp[:-1]
            i=0
            inpstr=''
            while i<len(inp):
                inpstr+=inp[i]+' '
                inp[i] = int(inp[i])
                i+=1
            if option==3:
                out = np.sign(np.dot(w, inp))
            else:
                out = commands.getoutput("python Atestcensus.py "+sys.argv[1]+" "+sys.argv[2]+" "+inpstr)
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

    max = -1
    min = 100
    #print num
    while val< num:
        inp_fix=['','','','','','','','','','','','','']
        i=len(fix_atr)-1
        tmp_val = val
       
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
            #print inp_fix
            if(num_inp>=max_inp):
                break;
            j=0
            while j<len(num_atr):
                if inp_fix[j]=='':
                    inp[j] = (random.randint(0,num_atr[j]-1))
                else:
                    inp[j]=inp_fix[j]
                j+=1
                                

            num_inp+=1
            str1=""
            strinp=""
            for x in inp:
                str1 += str(x)+","
                strinp+=str(x)+" "
            if option ==1 :   
                out = commands.getoutput("python Atestcensus.py "+sys.argv[1]+" "+sys.argv[2]+" "+strinp)
            else:
                out = np.sign(np.dot(w, inp))

            if printsuite==1:
            	f.write(str1+" "+str(out)+"\n")
                #print str1,out
            if(out>0):
            #print inp, out, 1
                map[','.join(str(inp))] = 1
                pos+=1
            else:
                #print inp,out, 0
                map[','.join(str(inp))] = 0
                neg+=1
            frac=pos*1.0/(pos+neg)
            if 2.5*math.sqrt(frac*(1-frac)*1.0/(pos+neg)<0.05) and pos+neg>minInp:
                break
        if(pos*1.0/(pos+neg)>max):
            max = pos*1.0/(pos+neg)
        if(pos*1.0/(pos+neg)<min):
            min = pos*1.0/(pos+neg)
    #print (max-min)*100
def findsubsets(S,m):
    return set(itertools.combinations(S, m))




if(sens_arg==9 and fixval==8):
    fixed = [0,0,0,0,0,0,0,1,0,0,0,0,0]
elif (sens_arg==9 and fixval==9):
    fixed = [0,0,0,0,0,0,0,0,1,0,0,0,0]
elif (sens_arg==8 and fixval==9):
    fixed = [0,0,0,0,0,0,0,0,1,0,0,0,0]
else:
    fixed = [0,0,0,0,0,0,0,1,0,0,0,0,0]

check_ratio(fixed)
print "output written in ", outputfile
f.close()
