'''
Causal discrimination testing for Subject System A on Credit dataset
Inputs :
    argv[1] : Train file
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

random.seed(22)
max_inp = 4000
minInp=4000
printout=1
sens_arg = 9
if(sens_arg== 9):
    name = 'sex'
    cov=0
else:
    name = 'race'
    cov = [0.2,0.2,0.2,0.2,0.2,0.2]

X=[]
Y=[]
i=0
fixval=9
outputfile = "../Suites/freshAcausalcredit"+str(sens_arg)+str(fixval)
option=4
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



already= "../Suites/Acausalcredit"
num_atr=[4,80,5,11,200,5,5,4,2,3,4,4,75,3,3,4,4,3,2,2]
map={}
f = open(outputfile,"w")

def check_ratio(fixed):
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
            while i<2:
                line[8] = str(i)
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
                        out = commands.getoutput("python Atestcredit.py "+sys.argv[1]+" "+inpstr)
                    else:
                        out = np.sign(np.dot(w, inp))
                    if printout==1:
                        f.write(strinp+","+str(out)+"\n")
                    if out>0:
                        pos+=1
                    else:
                        neg+=1
                    requeried[strinp] = 1
                i+=1
            if pos>0 and neg > 0:
                num+=1
            if pos>0 or neg>0:
                den+=1
        print "Score is ",num*100.0/den
        return
    while val< num:
        inp_fix=['','','','','','','','','','','','','','','','','','','','']
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
    inp=['','','','','','','','','','','','','','','','','','','','']
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
            inp_fix=['','','','','','','','','','','','','','','','','','','','']
            i=len(fix_atr)-1
            tmp_val = val
            #print val
            while i>=0:
                inp[fix_atr[i]] = tmp_val%num_atr[fix_atr[i]]
                tmp_val = (tmp_val - tmp_val%num_atr[fix_atr[i]])/num_atr[fix_atr[i]]
                i-=1
            val+=1
            num_test+=1
            out = np.sign(np.dot(w, inp))
            str1=""
            inpstr=""
            for x in inp:
                str1 += str(x)+","
                inpstr += str(x)+" "
            if option ==1 :   
                out = commands.getoutput("python Atestcredit.py "+sys.argv[1]+" "+inpstr)
            else:
                out = np.sign(np.dot(w, inp))

            if printout==1:
                f.write(str1+" "+str(out)+"\n")
            
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
        frac = pos*1.0/(pos+neg)
        if 2.5*math.sqrt(frac*(1-frac)*1.0/(pos+neg)<0.05) and pos+neg>minInp:
                break
        num_inp+=1

                                                             
def findsubsets(S,m):
    return set(itertools.combinations(S, m))

#fixed_atr = 9
fixed = [0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0]
#fixed[fixed_str-1]=1
check_ratio(fixed)
f.close()
print "output is in ",outputfile
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
