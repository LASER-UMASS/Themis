import math


def ProcessCacheCausal (self,inp,X):
    tr=0
    fl=0
    
    for cache_tuple in self.cache.items():
        #print cache_tuple
        i=0
        match =  True
        j=0
        while j<len(inp):
            if(j in X):
                j+=1
                continue
            if(not (cache_tuple[0][j] == inp[j])):
                match = False
                break
            i+=1
            j+=1
        if(match==True and not cache_tuple[1]==self.cache[tuple(inp)]):
            return True
    return False

def merge(inp, attr,X):
    #print inp, attr, X
    i=0
    while i<len(inp):
        if(i in X):
            inp[i] = attr[X.index(i)]
        i+=1
    return inp

def causalDiscrimination (self, X, confidence, epsilon):
    count = 0
    r = 0

    numValues = 1
    for x in X:
        numValues *= self.num[x]

    while r < self.MaxSamples :
        inp = self.randomInput(self.num,[],[])
        if(tuple(inp) in self.cache.keys()):
            out = self.cache[tuple(inp)]
        else:
            out = self.SoftwareTest( inp,self.num, self.values)
            self.cache[tuple(inp)] = out
        found_in = ProcessCacheCausal(self, inp,X)
        #Process cache to find atleast one with not out as output
        
        if r > self.SamplingThreshold:
            p = count*1.0/r
            if (self.conf_zValue[int(confidence)] * math.sqrt(p*(1-p)*1.0/r) < epsilon):
                break
        if(found_in):
            count+=1
            r+=1
            continue
        i=0
        found = False
        while i < numValues:
            #print "here",i, numValues
            attr=self.decodeValues(i,self.num,X)
            new_inp = merge(inp,attr,X)

            if(tuple(new_inp) in self.cache.keys()):
                tmpout = self.cache[tuple(new_inp)]
            else:
                tmpout = self.SoftwareTest(new_inp,self.num, self.values)
                self.cache[tuple(new_inp)] = tmpout
            if(not tmpout==out):
                found = True
                break
            i+=1
        if(found):
            count+=1
                    


        r += 1
    
    return p


