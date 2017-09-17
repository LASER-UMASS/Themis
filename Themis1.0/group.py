import sys
import random
import math
import causal

def ProcessCache (self, X, attr):
    tr=0
    fl=0

    for cache_tuple in self.cache.items():
        #print cache_tuple
        i=0
        match =  True
        for x in X:
            if(not (cache_tuple[0][x] == attr[i])):
                match = False
                break
            i+=1
        if(match==True and int(cache_tuple[1])==1):
            tr+=1
        elif (match):
            fl+=1

    return (tr,fl)


def groupDiscrimination (self, X, confidence, epsilon):
    minGroup = float("inf")
    maxGroup = 0
    
    #first find the maximum number of assignments possible
    #iterate
    numValues = 1
    for x in X:
        numValues *= self.num[x]

    maxPossible =1
    i=0
    while i<len(self.num):
        if i in X:
            i+=1
            continue
        maxPossible*=self.num[i]
        i+=1
    #print maxPossible
    i=0

    while i < numValues:
        #print i
        #Convert i to the values for X
        attr = self.decodeValues(i,self.num,X)
        #print "this is ",X, self.num,attr
        #TODO: Check cache elements first
        #        (tr,fal) = self.ProcessCache(X,attr)
        #print tr,fal,attr,X
        tr = 0
        fal=0
        r = tr+fal
        count = tr
        #print "cahce told ",tr,fal
        p=1
        added_now=[]
        while r <= self.MaxSamples:
            #print r, maxPossible
            
            inp = self.randomInput(self.num,X,attr)
            
            if(tuple(inp) in self.cache.keys()):
                
                out = self.cache[tuple(inp)]
                
                if(r==maxPossible):
                    p = count*1.0/r
                    break
                if(inp in added_now):
                    continue
                        #                print inp,out

            else:
        #print "inp",attr,inp,r,self.MaxSamples,p
            #Compute S(inp)
                out = self.SoftwareTest(inp,self.num, self.values)
                self.cache[tuple(inp)] = out
            added_now.append(inp)
            r+=1
            #print inp,out
            #

            if out:
                count += 1
            p = count*1.0/r
            
            if r > self.SamplingThreshold:
                #print confidence, self.conf_zValue
                if (self.conf_zValue[int(100*confidence)] * math.sqrt(p*(1-p)*1.0/r) < epsilon):
                    break
            #Add to cache

            #we need to check if the new inp is not generated in this loop. In that case we need to re generate the random input
            #print p, maxGroup,minGroup
        
        if(maxGroup < p):
            maxGroup = p
        if(minGroup > p):
            minGroup = p
        i+=1
    #print maxGroup, minGroup
    return maxGroup - minGroup



