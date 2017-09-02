#!/bin/bash
#Processes the test suites to generate the discrimination values as present in Table 1

cd A/
Acr="$(python Table1CausalScore.py ../Suites/Acausal88 1)"
echo "Acensus,race : g,r "$Acr
cd ..

cd B/Bgendercausalmrg
Bmrg="$(python Table1CausalScore.py ../../Suites/Bcausalmrg.txt 1)"
echo "Bcensus, gender: m,r,g "$Bmrg
cd ../..


cd B/Bgendercausalrg
Brg="$(python Table1CausalScore.py ../../Suites/Bcausalrg.txt 1)"
echo "Bcensus, gender: r,g "$Brg
cd ../..


Brg="$(python Table1CausalScore.py Suites/B99causal 1)"
echo "Bcensus, gender: g "$Brg

Brg="$(python Table1CausalScore.py Suites/B98causal 0)"
echo "Bcensus, gender: r "$Brg



cd B/Bgendercausalm
Bm="$(python Table1CausalScore.py ../../Suites/Bcausalmarital.txt 1)"
echo "Bcensus, gender: m "$Bm
cd ../..

cd B/Bracecausal
Br="$(python Table1CausalScore.py ../../Suites/B89causal 1)"
echo "Bcensus, race: g "$Br
cd ../..


cd C/Ccredit
Cr="$(python Table1CausalScore.py ../../Suites/Ccreditcausal99.txt 1)"
echo "Ccredit, gender: a "$Cr
cd ../..

cd C/Crace
Cr="$(python Table1CausalScore.py ../../Suites/Ccausalage.txt 1)"
echo "Ccensus, race: a "$Cr
cd ../..

cd C/Cracerg
Cr="$(python Table1CausalScore.py ../../Suites/Ccausalrg.txt 1)"
echo "Ccensus, race: r,g "$Cr
cd ../..

Crg="$(python Table1CausalScore.py Suites/C89causal 1)"
echo "Ccensus, race: g "$Crg


cd C/Cgender
Cg="$(python Table1CausalScore.py ../../Suites/Ccausalmarital 1)"
echo "Ccensus, gender: m "$Cg
cd ../..



cd D/Dgenderm
Dm="$(python Table1CausalScore.py ../../Suites/Dcausalmarital.txt 1)"
echo "Dcensus, gender: m "$Dm
cd ../..

cd D/Dgenderc
Dm="$(python Table1CausalScore.py ../../Suites/Dcausalrc.txt 1)"
echo "Dcensus, gender: c "$Dm
cd ../..


cd D/Dracem
Dm="$(python Table1CausalScore.py ../../Suites/Dcausalrmarital.txt 1)"
echo "Dcensus, race: m "$Dm
cd ../..

cd D/Dracemr
Dm="$(python Table1CausalScore.py ../../Suites/Dcausalmr.txt 1)"
echo "Dcensus, race: m,r "$Dm
cd ../..

cd E/
Ec="$(python Table1CausalScore.py ../Suites/Ecausalmarital 1)"
echo "Ecensus: m "$Ec
cd ../

cd F/Fcountryrace
Fcr="$(python Table1CausalScore.py ../../Suites/Fcausalcr.txt 1)"
echo "Fcensus: r,c "$Fcr
cd ../..

cd F/Frelationrace
Frr="$(python Table1CausalScore.py ../../Suites/Fcausalrr.txt 1)"
echo "Fcensus: r,e "$Frr
cd ../..

cd G/
Gc="$(python Table1CausalScore.py ../Suites/Gcausalrelation.txt 1)"
echo "Gcensus: e "$Gc
cd ..
