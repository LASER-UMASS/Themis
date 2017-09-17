import sys
import random
import math
import Themis

f = open("settings.txt",'r')
software_name = ""
command = ""
num_attributes = -1
names={}
type={}
values={}
num_values={}
count = 0
nums = -1

#read the settings file
for line in f :
    count = count + 1;	
    line = line.strip()
    if(count==1):
	line = line.strip(':')
        nums = int(line[-1]) 
    if( "command" in line and count==nums+2):
        line = line.split(" ")
        command  = " ".join(line[1:])
        continue
    if(num_attributes==-1):
	line = line.strip(':')
        num_attributes = int(line[-1]) 
    else:
        line = line.split(' ')
        attr_no = int(line[0])-1
	names[attr_no] = line[1]
        type[attr_no] = line[2]
        
        if (line[2]=="categorical"):
            values[attr_no] = line[3].split(",")
            num_values[attr_no] = len(line[3].split(","))
        
        elif (line[2]=="continuousInt"):
            start = int(line[3].split(",")[0])
            end = int(line[3].split(",")[1])
            num_values[attr_no] = end - start + 1
            value_lst  =[]
            while start <=end:
                value_lst.append(start)
                start+=1
            values[attr_no] = value_lst



soft = Themis.soft(names, values, num_values, command, type)




D = soft.discriminationSearch(0.3,0.99,0.1,"group")

soft.printSoftwareDetails()

print  "\n\n\nThemis has completed \n"
print "Software discriminates against ",D,"\n"
#X=[0,2]
#print soft.groupDiscrimination(X,99,0.1)
#print soft.causalDiscrimination(X,99,0.1)
print soft.getTestSuite()
