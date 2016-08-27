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

if(len(sys.argv)!=4):
    print "USAGE python svm.py 8/9 SEX AGE"
    print "Curently working with 9 only as it doesnt converge with 8"
    exit(-1)

random.seed()

sens_arg = int(sys.argv[1])
print sens_arg
if(sens_arg== 9):
    name = 'sex'
    cov=0
else:
    name = 'race'
    cov = [0,0.6,0.4,0,0,0]

X=[]
Y=[]
i=0
sensitive = {}
sens = []
with open("cleaned_train", "r") as ins:
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


x = [ 5,1,10,3,0,4,2,0,1,0,0,45,0]

predicted_labels = np.sign(np.dot(w, x))
print predicted_labels

print "DONE"




num_atr=[10,8,70,16,7,14,6,5,2,100,40,100,40]
map={}
def check_ratio(fixed,clf):
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
    for i3 in range(0, 70) :
        if(num_inp>=max_inp):
            break;
        for i12 in range(0,100):
            if(num_inp>=max_inp):
                break;
            for i10 in range(0,100):
                if(num_inp>=max_inp):
                    break;
                for i11 in range(0,40):
                    if(num_inp>=max_inp):
                        break;
                    for i6 in range(0,14):
                        if(num_inp>=max_inp):
                            break;
                        for i5 in range(0,7):
                            if(num_inp>=max_inp):
                                break;
                            for i7 in range(0,6):
                                if(num_inp>=max_inp):
                                    break;
                                for i4 in range(0,16):
                                    if(num_inp>=max_inp):
                                        break;
                                    for i13 in range(0,40):
                                        if(num_inp>=max_inp):
                                            break;
                                        for i1 in range(0,10):
                                            if(num_inp>=max_inp):
                                                break;
                                            for  i2 in range(0,8):
                                                if(num_inp>=max_inp):
                                                    break;
                                                for i9 in range(0,2):
                                                    if(num_inp>=max_inp):
                                                        break;
                                                    for i8 in range(0,5):
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
                                                            if(','.join(str(inp)) in curr_map.keys()):
                                                                pos_found=-1
                                                                print "here",inp
                                                                break
                                                            #print inp,num_inp,max_inp
                                                            num_test+=1
                                                            inp = str(inp)
                                                            out = np.sign(np.dot(w, inp))
                                                            if(out>0):
                                                                pos_found=1
                                                                curr_map[','.join(str(inp))] = 1
                                                            else:
                                                                neg_found = 1
                                                                curr_map[','.join(str(inp))] = 0
                                                            if(pos_found==1 and neg_found==1):
                                                                break
                                                        if(pos_found==-1):
                                                            continue
                                                        if(pos_found==1 and neg_found==1):
                                                            pos+=1
                                                        else:
                                                            neg+=1
                                                        num_inp+=1
    print fixed,pos,neg, pos*1.0/(neg+pos),num_test

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
