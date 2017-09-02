#!/bin/bash
#Generate the test suite for each of the Subject systems. 
# The parameters taken by wrapper script are the subject system,
# type of dataset, type of discrimintation to test, sensitive
# attribute to train the classifier for and the discriminating
#attribute to test discrimination against.
python wrapper.py A credit group race race
mv Suites/freshAcredit99.txt Suites/Acredit
python wrapper.py A credit causal race race
mv Suites/freshAcausalcredit99 Suites/Acausalcredit
python wrapper.py A census group gender race
mv Suites/freshA98 Suites/A98
python wrapper.py A census causal gender race
mv Suites/freshAcausal98 Suites/A98causal
python wrapper.py A census group gender gender
mv Suites/freshA99 Suites/A99
python wrapper.py A census causal gender gender
mv Suites/freshAcausal99 Suites/A99causal
python wrapper.py A census group race race
mv Suites/freshA88 Suites/A88
python wrapper.py A census causal race race
mv Suites/freshAcausal88 Suites/A88causal
python wrapper.py A census group race gender
mv Suites/freshA89 Suites/A89
python wrapper.py A census causal race gender
mv Suites/freshAcausal89 Suites/A89causal

python wrapper.py B credit group race race
mv Suites/freshBcredit99.txt Suites/Bcredit
python wrapper.py B credit causal race race
mv Suites/freshBcausalcredit99.txt Suites/Bcausalcredit
python wrapper.py B census group gender race
mv Suites/freshB98.txt Suites/B98
python wrapper.py B census causal gender race
mv Suites/freshBcausal98.txt Suites/B98causal
python wrapper.py B census group gender gender
mv Suites/freshB99.txt Suites/B99
python wrapper.py B census causal gender gender
mv Suites/freshBcausal99.txt Suites/B99causal
python wrapper.py B census group race race
mv Suites/freshB88.txt Suites/B88
python wrapper.py B census causal race race
mv Suites/freshBcausal88.txt Suites/B88causal
python wrapper.py B census group race gender
mv Suites/freshB89.txt Suites/B89
python wrapper.py B census causal race gender
mv Suites/freshBcausal89.txt Suites/B89causal


python wrapper.py C credit group race race
mv Suites/freshCcredit99.txt Suites/Ccredit
python wrapper.py C credit causal race race
mv Suites/freshCcreditcausal99.txt Suites/Ccausal
python wrapper.py C census group gender race
mv Suites/freshC98.txt Suites/C98
python wrapper.py C census causal gender race
mv Suites/freshCcausal98.txt Suites/C98causal
python wrapper.py C census group gender gender
mv Suites/freshC99.txt Suites/C99
python wrapper.py C census causal gender gender
mv Suites/freshCcausal98.txt Suites/C99causal
python wrapper.py C census group race race
mv Suites/freshC88.txt Suites/C88
python wrapper.py C census causal race race
mv Suites/freshCcausal88.txt Suites/C88causal
python wrapper.py C census group race gender
mv Suites/freshC89.txt Suites/C89
python wrapper.py C census causal race gender
mv Suites/freshCcausal89.txt Suites/C89causal

python wrapper.py D credit group race race
mv Suites/freshDcredit99.txt Suites/Dcredit
python wrapper.py D credit causal race race
mv Suites/freshDcausalcredit99.txt Suites/Dcreditcausal
python wrapper.py D census group gender race
mv Suites/freshD98.txt Suites/D98
python wrapper.py D census causal gender race
mv Suites/freshDcausal98.txt Suites/D98causal
python wrapper.py D census group gender gender
mv Suites/freshD99.txt Suites/D99
python wrapper.py D census causal gender gender
mv Suites/freshDcausal99.txt Suites/D99causal
python wrapper.py D census group race race
mv Suites/freshD88.txt Suites/D88
python wrapper.py D census causal race race
mv Suites/freshDcausal88.txt Suites/D88causal
python wrapper.py D census group race gender
mv Suites/freshD89.txt Suites/D89
python wrapper.py D census causal race gender
mv Suites/freshDcausal89.txt Suites/D89causal



python wrapper.py E credit group race race
mv Suites/freshEcredit9.txt Suites/Ecredit
python wrapper.py E credit causal race race
mv Suites/freshEcreditcausal9.txt Suites/Ecreditcausal

python wrapper.py E census group race race
mv Suites/freshE8.txt Suites/E8
python wrapper.py E census causal race race
mv Suites/freshEcausal98 Suites/E8causal

python wrapper.py E census group gender gender
mv Suites/freshE9.txt Suites/E9
python wrapper.py E census causal gender gender
mv Suites/freshEcausal99 Suites/E9causal

python wrapper.py F credit group race race
mv Suites/freshFcredit99.txt Suites/Fcredit
python wrapper.py F credit causal race race
mv Suites/freshFcausalcredit99.txt Suites/Fcreditcausal

python wrapper.py F census group race race
mv Suites/freshF98.txt Suites/F8
python wrapper.py F census causal race race
mv Suites/freshFcausal8.txt Suites/F8causal
python wrapper.py F census group gender gender
mv Suites/freshF99.txt Suites/F9
 mv Suites/freshFcausal9.txt Suites/F9causal

python wrapper.py G credit group race race
mv Suites/freshGcredit9.txt Suites/Gcredit
python wrapper.py G credit causal race race
mv Suites/freshGcreditcausal9.txt Suites/Gcreditcausal
python wrapper.py G census group race race
mv Suites/freshG8.txt Suites/G8
python wrapper.py G census causal race race
mv Suites/freshGcausal8.txt Suites/G8causal
python wrapper.py G census group gender gender
mv Suites/freshG9.txt Suites/G9
python wrapper.py G census causal gender gender
mv Suites/freshGcausal9.txt Suites/G9causal

python wrapper.py H credit group race race
mv Suites/freshHcredit9.txt Suites/Hcredit
python wrapper.py H credit causal race race
mv Suites/freshHcreditcausal9.txt Suites/Hcreditcausal

python wrapper.py H census group race race
mv Suites/freshH8.txt Suites/H8
python wrapper.py H census causal race race
mv Suites/freshHcausal8.txt Suites/H8causal

python wrapper.py H census group gender gender
mv Suites/freshH9.txt Suites/H9
python wrapper.py H census causal gender gender
mv Suites/freshHcausal9.txt Suites/H9causal
