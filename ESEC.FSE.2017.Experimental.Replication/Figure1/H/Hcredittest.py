from __future__ import division
import random
import math
import os
from collections import defaultdict
from sklearn import linear_model
import numpy as np
from sklearn.naive_bayes import GaussianNB
from sklearn import svm

import itertools

import sys



random.seed(1)

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
clf = svm.SVC()
clf.fit(X, Y)

num_atr=[4,80,5,11,200,5,5,4,2,3,4,4,75,3,3,4,4,3,2,2]
map={}

i=2
inp=[]
while len(inp)<20:
    inp.append(int(sys.argv[i]))
    i+=1


out = clf.predict([inp])[0]
print out