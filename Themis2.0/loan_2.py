import sys
sex = sys.argv[1]
race = sys.argv[2]
income = sys.argv[3]

# second case
if race == "green" or race == "orange":
	if income == "0...50000":
		print ("0")
	else:
		print ("1")
else:
	if income == "50001...100000":
		print ("0")
	else:
		print ("1")

