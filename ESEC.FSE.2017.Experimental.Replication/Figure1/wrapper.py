'''
Wrapper script to call each of the subject system depending on the input arguments
'''
import sys
import commands

'''
Usage : 
argv[1] : Name of the subjecct system
argv[2] : The dataset to train the classifier
argv[3] : Type of discrimination (Group/Causal)
argv[4] : The sensitive argument to train the classfier
argv[5] : Tyep of attribute to test discrimination against
'''
subjectSystem  = sys.argv[1]
data = sys.argv[2].strip()
discType = (sys.argv[3])
row = (sys.argv[4])
column = (sys.argv[5])


if discType == "group":
	group=1
else:
	group = 0

if data=="credit":
	dataset = "Suites/Credit"
else:
	dataset = "Suites/Census"

if row=="race":
	r=8
else:
	r=9

if column=="race":
	c=8
else:
	c=9

if subjectSystem=='A':
	if "Credit" in  dataset:
		if group==1:
			cmd1="cd A/"
			cmd2 = "python AGroupCredit.py ../"+dataset
		else:
			cmd1="cd A/"
			cmd2 = "python ACausalCredit.py ../"+dataset
	else:
		if group==1:
			cmd1="cd A/"
			cmd2="python AGroup.py ../"+dataset+" "+str(r)+" "+str(c)
		else:
			if c==8:
				cmd1="cd A/"
				cmd2="python ACausalRace.py ../"+dataset+" "+str(r)+" "+str(c)
			else:
				cmd1="cd A/"
				cmd2="python ACausalGender.py ../"+dataset+" "+str(r)+" "+str(c)

	print cmd1+";"+cmd2
	out = commands.getoutput(cmd1+";"+cmd2)
	print out

if subjectSystem=='B':
	if "Credit" in  dataset:
		if group==1:
			cmd1="cd B/BGroupCredit; make clean; make"
			cmd2="./dtree ../../"+dataset
			cmd3="cd ../.."
		else:
			cmd1="cd B/BCausalCredit; make clean; make"
			cmd2="./dtree ../../"+dataset
			cmd3="cd ../.."
	else:
		if group==1 and r==8:
			cmd1="cd B/Brace; make clean; make"
			cmd2="./dtree ../../"+dataset+" "+str(c)
			cmd3="cd ../.."
		elif group==1 and r==9:
			cmd1="cd B/Bgender; make clean; make"
			cmd2="./dtree ../../"+dataset+" "+str(c)
			cmd3="cd ../.."
		elif  r==8:
			cmd1="cd B/Bracecausal; make clean; make"
			cmd2="./dtree ../../"+dataset+" "+str(c)
			cmd3="cd ../.."
		elif  r==9:
			cmd1="cd B/Bgendercausal; make clean; make"
			cmd2="./dtree ../../"+dataset+" "+str(c)
			cmd3="cd ../.."
	print cmd1+";"+cmd2+";"+cmd3
	out = commands.getoutput(cmd1+";"+cmd2+";"+cmd3)
	print out

if subjectSystem =='C':
	if "Credit" in  dataset:
		dataset = "Suites/CCreditdata"
		cmd1="cd C/Ccredit"
		if group==1:
			cmd2="python Ccreditgroup.py ../../"+dataset
		else:
			cmd2="python Ccreditcausal.py ../../"+dataset

	else:
		if r==8:
			dataset = "Suites/Cracedata"
			if group==1:
				cmd1="cd C/Crace"
				cmd2="python Cgroup.py ../../"+dataset+" "+str(c)
			else:
				cmd1="cd C/Crace"
				cmd2="python Ccausal.py ../../"+dataset+" "+str(c)
		elif  r==9:
			dataset = "Suites/Cgenderdata"
			if group==1:
				cmd1="cd C/Cgender"
				cmd2="python Cgroup.py ../../"+dataset+" "+str(c)
			else:
				cmd1="cd C/Cgender"
				cmd2="python Ccausal.py ../../"+dataset+" "+str(c)
	print cmd1+";"+cmd2
	out = commands.getoutput(cmd1+";"+cmd2)
	print out		
if subjectSystem=='D':
	if "Credit" in  dataset:
		dataset = "Suites/DCreditdata"
		if group==1:
			cmd1="cd D/DGroupCredit; make clean; make;"
			cmd2="./dtree ../../"+dataset
			cmd3="cd ../.."
		else:
			cmd1="cd D/DCausalCredit; make clean; make;"
			cmd2="./dtree ../../"+dataset
			cmd3="cd ../.."
	else:
		if group==1 and r==8:
			dataset = "Suites/Dracedata"
			cmd1="cd D/Drace; make clean; make;"
			cmd2="./dtree ../../"+dataset+" "+str(c)
			cmd3="cd ../.."
		elif group==1 and r==9:
			dataset = "Suites/Dgenderdata"
			cmd1="cd D/Dgender; make clean; make;"
			cmd2="./dtree ../../"+dataset+" "+str(c)
			cmd3="cd ../.."
		elif  r==8:
			dataset = "Suites/Dracedata"
			cmd1="cd D/Dracecausal; make clean; make;"
			cmd2="./dtree ../../"+dataset+" "+str(c)
			cmd3="cd ../.."
		elif  r==9:
			dataset = "Suites/Dgenderdata"
			cmd1="cd D/Dgendercausal; make clean; make;"
			cmd2="./dtree ../../"+dataset+" "+str(c)
			cmd3="cd ../.."
	print cmd1+";"+cmd2+";"+cmd3
	out = commands.getoutput(cmd1+cmd2+";"+cmd3)
	print out	

if subjectSystem =='E':
	if "Credit" in  dataset:
		cmd1="cd E/"
		if group==1:
			cmd2="python Ecreditgroup.py ../"+dataset
		else:
			cmd2="python Ecreditcausal.py ./../"+dataset

	else:
		if group==1:
			cmd1="cd E/"
			cmd2="python Egroup.py ./../"+dataset+" "+str(r)+" "+str(c)
		else:
			cmd1="cd E/"
			cmd2="python Ecausal.py ./../"+dataset+" "+str(r)+" "+str(c)
		
	print cmd1+";"+cmd2
	out = commands.getoutput(cmd1+";"+cmd2)
	print out	

if subjectSystem=='F':
	if "Credit" in  dataset:
		if group==1:
			cmd1="cd F/FGroupCredit; make clean; make;"
			cmd2="./dtree ../../"+dataset
			cmd3="cd ../.."
		else:
			cmd1="cd F/FCausalCredit; make clean; make;"
			cmd2="./dtree ../../"+dataset
			cmd3="cd ../.."
	else:
		if group==1 :
			cmd1="cd F/Fgroup; make clean; make;"
			cmd2="./dtree ../../"+dataset+" "+str(c)
			cmd3="cd ../.."
		else:
			cmd1="cd F/Fcausal; make clean; make;"
			cmd2="./dtree ../../"+dataset+" "+str(c)
			cmd3="cd ../.."
	print cmd1+";"+cmd2+";"+cmd3
	out = commands.getoutput(cmd1+cmd2+";"+cmd3)
	print out

if subjectSystem =='G':
	if "Credit" in  dataset:
		cmd1="cd G/"
		if group==1:
			cmd2="python Gcreditgroup.py ./../"+dataset
		else:
			cmd2="python Gcreditcausal.py ./../"+dataset

	else:
		if group==1:
			cmd1="cd G/"
			cmd2="python Ggroup.py ./../"+dataset+" "+str(r)+" "+str(c)
		else:
			cmd1="cd G/"
			cmd2="python Gcausal.py ./../"+dataset+" "+str(r)+" "+str(c)

	print cmd1+";"+cmd2
	out = commands.getoutput(cmd1+";"+cmd2)
	print out			
if subjectSystem =='H':
	if "Credit" in  dataset:
		cmd1="cd H/"
		if group==1:
			cmd2="python Hcreditgroup.py ./../"+dataset
		else:
			cmd2="python Hcreditcausal.py ./../"+dataset

	else:
		if group==1:
			cmd1="cd H/"
			cmd2="python Hgroup.py ./../"+dataset+" "+str(r)+" "+str(c)
		else:
			cmd1="cd H/"
			cmd2="python Hcausal.py ./../"+dataset+" "+str(r)+" "+str(c)
		
	print cmd1+";"+cmd2
	out = commands.getoutput(cmd1+";"+cmd2)
	print out	
