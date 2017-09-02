from __future__ import division
import random
import math
import os
from collections import defaultdict
from sklearn import linear_model
import numpy as np
import itertools

import sys

max_inp = 30000
printout = 0
minInp=3
random.seed(1997)

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
clf = linear_model.LinearRegression()
clf.fit(X, Y)

i=2
inp=[]
while len(inp)<20:
    inp.append(int(sys.argv[i]))
    i+=1
out = clf.predict([inp])[0]
if out > 0:
    out=1
else:
    out=0
print out