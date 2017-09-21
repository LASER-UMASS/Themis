import sys
import itertools
import commands
import random
import math
import causal
import group

class soft:
    causalDiscrimination = causal.causalDiscrimination
    groupDiscrimination = group.groupDiscrimination
    ProcessCacheCausal = causal.ProcessCacheCausal
    ProcessCache = group.ProcessCache
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
    
    #Get the number of values that a particular attribute can take
    def getRange(self, attr_name):
        for index,att_name in self.attr_names.iteritems():
            if(att_name == attr_name):
                return self.num[index]

    #Gets the different values a particular attribute can take
    def getValues(self, attr_name):
        for index,att_name in self.attr_names.iteritems():
            if(att_name == attr_name):
                return self.values[index]

    def printSoftwareDetails(self):
        print "Number of attributes are ", len(self.attr_names),"\n"
        i=0
        while i<len(self.attr_names):
            print "Attribute name is ",self.attr_names[i]
            print "Number of values taken by this attribute =",self.getRange(self.attr_names[i])
            print "The different values taken are ",self.getValues(self.attr_names[i]),"\n"
            i+=1

    def randomInput (self, I, X, attr):
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
                #print "actual inp",actual_inp
                #        print self.command
        
        #        print running_command
        #print commands.getstatusoutput(running_command)[1]
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
