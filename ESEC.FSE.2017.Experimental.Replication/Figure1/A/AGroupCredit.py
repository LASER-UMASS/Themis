'''
Group discrimination testing for Subject System A for Credit dataset
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
import itertools
import loss_funcs as lf # loss funcs that can be optimized subject to various constraints
import commands

#Minimum number of inputs tos tart applying the confidence check optimization
minInp = 15000

#Maximum number of inputs to test against
max_inp=15000

#set prinsuite to 0 to not print the test suite
printsuite = 1

#Training file
trainfile =  sys.argv[1]

random.seed(2)
#fixed seed to get the same test suite each time

sens_arg=9
fixval=9

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
outputfile = "../Suites/freshAcredit"+str(sens_arg)+str(fixval)+".txt"
option=4
already = "../Suites/Acredit"

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

#print "software has been trained"

f = open(already,"r")
done = {}
for line in f:
    line =  line.strip()
    line  =  line.split(',')
    line = ','.join(line[:-1])
    line+=','
    done[line]=1

num_atr=[4,80,5,11,200,5,5,4,2,3,4,4,75,3,3,4,4,3,2,2]
map={}
f = open(outputfile,"w")

def check_ratio(fixed):
    if option==3 or option==4:
    	pos0=0
    	pos1=0
    	neg0=0
    	neg1=0
    	for inp in done.keys():
    		strinp = inp
    		inp = inp.split(',')
    		inp=inp[:-1]
    		i=0
		inpstr=''
    		while i<len(inp):
			inpstr +=inp[i]+' '
			inp[i] = int(inp[i])
			i+=1
        	if option==3:
			out = np.sign(np.dot(w, inp))
        	else:
        		out = commands.getoutput("python Atestcredit.py "+sys.argv[1]+" "+inpstr)  
		if(out>0):
			if((inp[8]) ==1):
				pos1+=1
			else:
				pos0+=1
		else:
			if((inp[8]) ==1):
				neg1+=1
			else:
				neg0+=1
		if printsuite==1:
        	        f.write(strinp+" "+str(out)+"\n")
        print "score is ",100*(pos1*1.0/(pos1+neg1) - pos0*1.0/(pos0+neg0))
	return 
    fix_atr = []
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
            inpstr=""
            str1=""
            for x in inp:
                str1 += str(x)+","
                inpstr+=str(x)+" "
            if option ==1 :   
                out = commands.getoutput("python Atestcredit.py "+sys.argv[1]+" "+inpstr)
            elif option==2:
                out = np.sign(np.dot(w, inp))

            if printsuite==1:
                f.write(str1+" "+str(out)+"\n")

            
            
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
#    print fixed,max,min, max-min

def findsubsets(S,m):
    return set(itertools.combinations(S, m))




fixed = [0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0]
check_ratio(fixed)
f.close()
print "output is in ",outputfile
