from __future__ import division
import random
import math
import os
from collections import defaultdict
from sklearn import linear_model
import numpy as np
from sklearn.naive_bayes import GaussianNB

import itertools
import commands
import sys

random.seed(1991)
trainfile =  sys.argv[1]
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

map={}
option =1

i=3
inp=[]
while len(inp)<13:
    inp.append(int(sys.argv[i]))
    i+=1
    
print  str(clf.predict([inp])[0])