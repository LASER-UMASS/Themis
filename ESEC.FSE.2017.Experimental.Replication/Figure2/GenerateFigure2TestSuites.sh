#!/bin/bash
#Generate the test suite for each of the Subject systems. 
# The parameters taken by wrapper script are the subject system,
# type of dataset, type of discrimintation to test, sensitive
# attribute to train the classifier for and the discriminating
#attribute to test discrimination against.

cd A/; python ACausal.py ../Suites/Census; cd ..
mv Suites/freshAcausal88 Suites/Acausal88

cd B/Bgendercausalmrg; make clean; make
./dtree ../../Suites/Census
cd ../..
mv Suites/freshBcausalmrg.txt Suites/Bcausalmrg.txt

cp ../Fig1/Suites/B99causal Suites/B99causal
cp ../Fig1/Suites/B98causal Suites/B98causal

cd B/Bgendercausalrg; make clean; make
./dtree ../../Suites/Census
cd ../..
mv Suites/freshBcausalrg.txt Suites/Bcausalrg.txt

cd B/Bgendercausalm; make clean; make
./dtree ../../Suites/Census
cd ../..
mv Suites/freshBcausalmarital.txt Suites/Bcausalmarital.txt

#cd B/Bracecausal; make clean; make
#./dtree ../../Suites/Census
#cd ../..
#mv Suites/freshB89causal Suites/B89causal
cp ../Fig1/Suites/B89causal Suites/B89causal
cp ../Fig1/Suites/C89causal Suites/C89causal

cd C/Crace; 
python Ccausala.py ../../Suites/Cracedata

cd ../..
mv Suites/freshCcausalage.txt Suites/Ccausalage.txt

cd C/Cracerg; 
python Ccausal.py ../../Suites/Cracedata
cd ../..
mv Suites/freshCcausalrg.txt Suites/Ccausalrg.txt



cd C/Cgender; 
python Ccausalm.py ../../Suites/Cgenderdata
cd ../..
mv Suites/freshCcausalmarital.txt Suites/Ccausalmarital.txt


cd D/Dgenderm; make clean; make
./dtree Dgenderdata
cd ../..
mv Suites/freshDcausalmarital.txt Suites/Dcausalmarital.txt


cd D/Dgenderc; make clean; make
./dtree Dgenderdata
cd ../..
mv Suites/freshDcausalrc.txt Suites/Dcausalrc.txt

cd D/Dracem; make clean; make
./dtree Dracedata
cd ../..
mv Suites/freshDcausalrmarital.txt Suites/Dcausalrmarital.txt

cd D/Dracemr; make clean; make
./dtree Dracedata
cd ../..
mv Suites/freshDcausalmr.txt Suites/Dcausalmr.txt.txt

cd E/;
python Ecausalmarital.py ../Suites/Census
cd ..
mv Suites/freshEcausalmarital Suites/Ecausalmarital

cd F/Frelationrace; make clean; make
./dtree ../../Suites/Census
cd ../..
mv Suites/freshFcausalrr.txt Suites/Fcausalrr.txt

cd F/Fcountryrace; make clean; make
./dtree ../../Suites/Census
cd ../..
mv Suites/freshFcausalcr.txt Suites/Fcausalcr.txt

cd G/
python Gcausale.py ../Suites/Census
cd ..
mv Suites/freshGcausalrelation.txt Suites/Gcausalrelation.txt


cd C/Ccredit; 
python Ccreditcausal.py ../../Suites/Ccreditdata
cd ../..
mv Suites/freshCcreditcausal99.txt  Suites/Ccreditcausal99.txt 
