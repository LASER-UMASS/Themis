'''
Test the Subject System A on Census dataset to generate the output for the input given as argv arguments.
All the inputs are assumed to be space separated.
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
trainfile =  sys.argv[1]

random.seed(12)
#fixed seed to get the same test suite each time


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
option =1

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

i=3
inp=[]
while len(inp)<13:
    inp.append(int(sys.argv[i]))
    i+=1
print  np.sign(np.dot(w, inp)) 

