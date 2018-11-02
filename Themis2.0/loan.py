import sys
sex = sys.argv[1]
race = sys.argv[2]
income = sys.argv[3]

# first case
if sex == "male":
	print ("1")
elif race != "green":
	if income == "0...50000":
		print ("0")
	else:
		print ("1")
else:
	if income == "0...50000" or income == "50001...100000":
		print ("0")
	else:
		print ("1")


# # second case
# if race == "green" or race == "orange":
# 	if income == "0...50000":
# 		print ("0")
# 	else:
# 		print ("1")
# else:
# 	if income == "50001...100000":
# 		print ("0")
# 	else:
# 		print ("1")

# # third case
# if income == "0...50000":
# 	print ("0")
# else:
# 	print ("1")
