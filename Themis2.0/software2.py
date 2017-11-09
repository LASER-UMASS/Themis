import sys
sex = sys.argv[1]
race = sys.argv[2]
income = sys.argv[3]


if race != "Green":
	if income == "1":
		print ("0")
	else:
		print ("1")
elif sex == "Male":
	print ("1")
else:
	if income == "1" or income == "2":
		print ("0")
	else:
		print ("1")
