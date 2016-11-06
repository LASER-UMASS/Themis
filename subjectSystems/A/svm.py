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

sex = sys.argv[2]
race = sys.argv[3]

inp=[0,0,0,0,0,0,0,0,0,0,0,0,0]
inp = np.array(inp, dtype = float)
for i3 in range(0, 70) :
    for i12 in range(0,100):
        for i10 in range(0,100):
            for i11 in range(0,40):
                for i6 in range(0,14):
                    for i5 in range(0,7):
                        for i7 in range(0,6):
                            for i4 in range(0,16):
                                for i13 in range(0,40):
                                    for i1 in range(0,10):
                                        for  i2 in range(0,8):
                                            for i9 in range(0,1):
                                                for i8 in range(0,1):
                                                    i9 = sex
                                                    i8 = race
                                                    inp[0]=(random.randint(0,9));
                                                    inp[1]=(random.randint(0,7));
                                                    inp[2]=(random.randint(0,39));
                                                    inp[3]=(random.randint(0,15));
                                                    inp[4]=(random.randint(0,6));
                                                    inp[5]=(random.randint(0,13));
                                                    inp[6]=(random.randint(0,5));
                                                    inp[7]=(i8);
                                                    inp[8]=(i9);
                                                    inp[9]=(random.randint(0,99));
                                                    inp[10]=(random.randint(0,39));
                                                    inp[11]=(random.randint(0,99));
                                                    inp[12]=(random.randint(0,39));
                                                    out = np.sign(np.dot(w, inp))
                                                    if(out>0):
                                                        print inp,  1
                                                    else:
                                                        print inp, 0



