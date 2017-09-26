import sys
import itertools
import commands
import random
import math

class soft:
    conf_zValue = {80:1.28,90:1.645,95:1.96, 98:2.33, 99:2.58}

    MaxSamples=50
    SamplingThreshold = 10
    cache = {}

    def __init__(self, names, values, num, command, type):
        self.attr_names = names
        self.values = values
        self.num = num
        self.type = type
        self.command = command


    def getValues(self):
        return self.values

    def getComand(self):
        return self.command

    def getAttributeNames(self):
        return self.attr_names

    def getRange(self, attr_name):
        for index,att_name in self.attr_names.iteritems():
            if(att_name == attr_name):
                return self.num[index]

    def getValues(self, attr_name):
        for index,att_name in self.attr_names.iteritems():
            if(att_name == attr_name):
                return self.values[index]

    def printSoftwareDetails(self):
        print "Number of attributes are ", len(self.attr_names),"\n"
        for attr_name in self.attr_names:
            print "Attribute name is ",attr_name
            print "Number of values taken by this attribute =",self.getRange(attr_name)
            print "The different values taken are ",self.getValues(attr_name),"\n"


    def randomInput (self, I, X, attr):
        print I
        print X

        exit()

        i=0
        inp = []
        while i < len(I):
            if i in X:
                inp.append(attr[X.index(i)])
            else:
                inp.append(random.randint(0,I[i]-1))
            i+=1
        return inp

    def SoftwareTest(self, inp,num, values):
        i=0
        actual_inp = []
        running_command = self.command

        while i<len(inp):
            actual_inp.append(values[i][inp[i]])
            running_command += " "
            running_command += str(values[i][inp[i]])
            i+=1
        return (commands.getstatusoutput(running_command)[1] == "1")


    def decodeValues(self, index, num, X):
        attr=[]
        copy = index
        for x in X:
            a = num[x]
            attr.append(copy%a)
            copy -= (copy%a)
            copy = copy/a
        return attr


    def discriminationSearch(self,theta, confidence, epsilon, type):
	Scausal=[]
	if("causal" in type and "group" in type):
		Scausal = self.discriminationSearch(theta, confidence, epsilon, "causal")
	i=0
        lst = []
        while i<len(self.attr_names):
            lst.append(i)
            i+=1
        i=1
        D = []
        while i<= len(self.attr_names):
            subsets = list(itertools.combinations(tuple(lst), i))
            for X in subsets:
                found = False
                for d in D:
                    if(set(d)< set(list(X))):
                        found = True
                        break
                if(found):
                    continue
                if("group" in type):
                    score = self.groupDiscrimination(list(X),confidence,epsilon)
                elif("causal" in type):
                    score = self.causalDiscrimination(list(X),confidence,epsilon)
                if(score > theta):
                    D.append(list(X))
            #print self.group_discrimination(list(X),90,0.1)
            i+=1
        S=[]
        for d in D:
            s  = []
            for att in d:
                s.append(self.attr_names[att])
            S.append(s)
	if("group" in type and "causal" in type):
		dict={"group":S, "causal":Scausal["causal"]}
        elif("group" in type):
		dict={"group":S}
	else:
		dict={"causal":S}
	return dict


    def getTestSuite(self):
        inp_lst = []
        print self.values
        for inp,out in self.cache.iteritems():

            curr=[]
            i=0
            while i<len(inp):
                curr.append(self.values[i][inp[i]])
                i+=1
            inp_lst.append(curr)

        return inp_lst

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

    def merge(self, inp, attr, X):
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
            found_in = self.ProcessCacheCausal(inp, X)
            #Process cache to find atleast one with not out as output
            
            if r > self.SamplingThreshold:
                p = count*1.0/r
                if (self.conf_zValue[int(100*confidence)] * math.sqrt(p*(1-p)*1.0/r) < epsilon):
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
                new_inp = self.merge(inp,attr,X)

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
        i=0

        while i < numValues:
            attr = self.decodeValues(i,self.num,X)
            tr = 0
            fal=0
            r = tr+fal
            count = tr
            p=1
            added_now=[]
            while r <= self.MaxSamples:
                inp = self.randomInput(self.num,X,attr)
                if(tuple(inp) in self.cache.keys()):
                    out = self.cache[tuple(inp)]
                    if(r==maxPossible):
                        p = count*1.0/r
                        break
                    if(inp in added_now):
                        continue
                else:
                    out = self.SoftwareTest(inp,self.num, self.values)
                    self.cache[tuple(inp)] = out
                added_now.append(inp)
                r+=1
                if out:
                    count += 1
                p = count*1.0/r
                if r > self.SamplingThreshold:
                    if (self.conf_zValue[int(100*confidence)] * math.sqrt(p*(1-p)*1.0/r) < epsilon):
                        break
            if(maxGroup < p):
                maxGroup = p
            if(minGroup > p):
                minGroup = p
            i+=1
        return maxGroup - minGroup



