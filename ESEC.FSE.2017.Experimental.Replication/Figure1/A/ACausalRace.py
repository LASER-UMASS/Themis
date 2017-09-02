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
import loss_funcs as lf # loss funcs that can be optimized subject to various constraints
import commands

random.seed(1996)

#Maximum number of inputs to test against
max_inp = 50000

#Minimum number of inputs tos tart applying the confidence check optimization
minInp = 50000

#set prinsuite to 0 to not print the test suite
printout=1
sens_arg = int(sys.argv[2])


fixval=8

outputfile = "../Suites/freshAcausal"+str(sens_arg)+str(fixval)


#print sens_arg
if(sens_arg== 9):
    name = 'sex'
    cov=0
else:
    name = 'race'
    cov = [0.2,0.2,0.2,0.2,0.2,0.2]

X=[]
Y=[]
i=0
sensitive = {}
sens = []
with open(sys.argv[1], "r") as ins:
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

X = np.array(X, dtype=float)
Y = np.array(Y, dtype = float)
sensitive[name] = np.array(sens, dtype = float)
loss_function = lf._logistic_loss
sep_constraint = 0
sensitive_attrs = [name]
sensitive_attrs_to_cov_thresh = {name:cov}

gamma=None

w = ut.train_model(X, Y, sensitive, loss_function, 1, 0, sep_constraint, sensitive_attrs, sensitive_attrs_to_cov_thresh, gamma)


option =4
f = open(outputfile,"w")
already = "../Suites/A"+str(sens_arg)+str(fixval)+"causal"


num_atr=[10,8,70,16,7,14,6,5,2,100,40,100,40]
map={}
def check_ratio(fixed):
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
            if fixval ==8:
                numval=5
            else:
                numval=2
            while i<numval:
                #print i,den
                line[fixval-1] = str(i)
                strinp = ','.join(line)
                if(strinp in requeried.keys()):
                    i+=1
                    continue
                else:
                    #query here
                    inp=[]
                    inpstr=''
                    for a in  line:
                        inp.append(int(a))
                        inpstr+=a+' '
                    if option ==4 :   
                        out = commands.getoutput("python Atestcensus.py "+sys.argv[1]+" "+sys.argv[2]+" "+inpstr)
                    else:
                        out = np.sign(np.dot(w, inp))
                    if printout==1:
                        f.write(strinp+", "+str(out)+"\n")
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
    num_test = 0
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
            inp_fix[fix_atr[i]] = tmp_val%num_atr[fix_atr[i]]
            tmp_val = (tmp_val - tmp_val%num_atr[fix_atr[i]])/num_atr[fix_atr[i]]
            i-=1
        #print inp_fix
        val+=1
    inp=['','','','','','','','','','','','','']
    num_inp = 0
    pos = 0
    neg = 0
    curr_map={}

    for i3 in range(0, 10000) :
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
        val = 0
        pos_found = 0
        neg_found = 0
        while val< num:
            inp_fix=['','','','','','','','','','','','','']
            i=len(fix_atr)-1
            tmp_val = val
            #print val
            while i>=0:
                inp[fix_atr[i]] = tmp_val%num_atr[fix_atr[i]]
                tmp_val = (tmp_val - tmp_val%num_atr[fix_atr[i]])/num_atr[fix_atr[i]]
                i-=1
            val+=1
            
            #print inp,num_inp,max_inp
            num_test+=1
            #out = np.sign(np.dot(w, inp))
            str1=""
            inpstr=""
            for x in inp:
                str1 += str(x)+","
                inpstr+=str(x)+" "
            
            if option ==1 :   
                out = commands.getoutput("python Atestcensus.py "+sys.argv[1]+" "+sys.argv[2]+" "+inpstr)
            else:
                out = np.sign(np.dot(w, inp))
            if printout:
                f.write(str1+" "+str(out)+"\n")
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
        if 2.5*math.sqrt(frac*(1-frac)*1.0/(pos+neg)<0.05) and pos+neg>minInp:
                break
        num_inp+=1
    #print pos*100.0/(neg+pos)

                                                             
def findsubsets(S,m):
    return set(itertools.combinations(S, m))

#fixed_atr = 9
fixed = [0,0,0,0,0,0,0,1,0,0,0,0,0]
#fixed[fixed_str-1]=1
check_ratio(fixed)
f.close()
print "output is in ",outputfile
