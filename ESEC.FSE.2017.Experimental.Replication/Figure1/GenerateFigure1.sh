#!/bin/bash
#Processes the test suites to generate the discrimination values as present in Table 1


Acr="$(python Table1GroupScore.py Suites/Acredit 1)"
Acrcausal="$(python Table1CausalScore.py Suites/Acausalcredit 1)"


echo "-   -   "$Acr"   "$Acrcausal

ASR="$(python Table1GroupScore.py Suites/A98 0)"
ASRCausal="$(python Table1CausalScore.py Suites/A98causal 0)"
ASS="$(python Table1GroupScore.py Suites/A99 1)"
ASSCausal="$(python Table1CausalScore.py Suites/A99causal 1)"
echo $ASR"   "$ASRCausal"   "$ASS"   "$ASSCausal


ARR="$(python Table1GroupScore.py Suites/A88 0)"
ARRCausal="$(python Table1CausalScore.py Suites/A88causal 0)"
ARS="$(python Table1GroupScore.py Suites/A89 1)"
ARSCausal="$(python Table1CausalScore.py Suites/A89causal 1)"
echo $ARR"   "$ARRCausal"   "$ARS"   "$ARSCausal


Bcr="$(python Table1GroupScore.py Suites/Bcredit 1)"
Bcrcausal="$(python Table1CausalScore.py Suites/Bcausalcredit 1)"
echo "-   -   "$Bcr"   "$Bcrcausal

BSR="$(python Table1GroupScore.py Suites/B98 0)"
BSRCausal="$(python Table1CausalScore.py Suites/B98causal 0)"
BSS="$(python Table1GroupScore.py Suites/B99 1)"
BSSCausal="$(python Table1CausalScore.py Suites/B99causal 1)"
echo $BSR"   "$BSRCausal"   "$BSS"   "$BSSCausal


BRR="$(python Table1GroupScore.py Suites/B88 0)"
BRRCausal="$(python Table1CausalScore.py Suites/B88causal 0)"
BRS="$(python Table1GroupScore.py Suites/B89 1)"
BRSCausal="$(python Table1CausalScore.py Suites/B89causal 1)"
echo $BRR"   "$BRRCausal"   "$BRS"   "$BRSCausal



Ccr="$(python Table1GroupScore.py Suites/Ccredit 1)"
Ccrcausal="$(python Table1CausalScore.py Suites/Ccausalcredit 1)"
echo "-   -   "$Ccr"   "$Ccrcausal

CSR="$(python Table1GroupScore.py Suites/C98 0)"
CSRCausal="$(python Table1CausalScore.py Suites/C98causal 0)"
CSS="$(python Table1GroupScore.py Suites/C99 1)"
CSSCausal="$(python Table1CausalScore.py Suites/C99causal 1)"
echo $CSR"   "$CSRCausal"   "$CSS"   "$CSSCausal


CRR="$(python Table1GroupScore.py Suites/C88 0)"
CRRCausal="$(python Table1CausalScore.py Suites/C88causal 0)"
CRS="$(python Table1GroupScore.py Suites/C89 1)"
CRSCausal="$(python Table1CausalScore.py Suites/C89causal 1)"
echo $CRR"   "$CRRCausal"   "$CRS"   "$CRSCausal


Dcr="$(python Table1GroupScore.py Suites/Dcredit 1)"
Dcrcausal="$(python Table1CausalScore.py Suites/Dcausalcredit 1)"
echo "-   -   "$Dcr"   "$Dcrcausal

DSR="$(python Table1GroupScore.py Suites/D98 0)"
DSRCausal="$(python Table1CausalScore.py Suites/D98causal 0)"
DSS="$(python Table1GroupScore.py Suites/D99 1)"
DSSCausal="$(python Table1CausalScore.py Suites/D99causal 1)"
echo $DSR"   "$DSRCausal"   "$DSS"   "$DSSCausal


DRR="$(python Table1GroupScore.py Suites/D88 0)"
DRRCausal="$(python Table1CausalScore.py Suites/D88causal 0)"
DRS="$(python Table1GroupScore.py Suites/D89 1)"
DRSCausal="$(python Table1CausalScore.py Suites/D89causal 1)"
echo $DRR"   "$DRRCausal"   "$DRS"   "$DRSCausal


Ecr="$(python Table1GroupScore.py Suites/Ecredit 1)"
Ecrcausal="$(python Table1CausalScore.py Suites/Ecausalcredit 1)"
echo "-   -   "$Ecr"   "$Ecrcausal

ER="$(python Table1GroupScore.py Suites/E8 0)"
ERCausal="$(python Table1CausalScore.py Suites/E8causal 0)"
ES="$(python Table1GroupScore.py Suites/E9 1)"
ESCausal="$(python Table1CausalScore.py Suites/E9causal 1)"
echo $ER"   "$ERCausal"   "$ES"   "$ESCausal



Fcr="$(python Table1GroupScore.py Suites/Fcredit 1)"
Fcrcausal="$(python Table1CausalScore.py Suites/Fcausalcredit 1)"
echo "-   -   "$Fcr"   "$Fcrcausal

FR="$(python Table1GroupScore.py Suites/F8 0)"
FRCausal="$(python Table1CausalScore.py Suites/F8causal 0)"
FS="$(python Table1GroupScore.py Suites/F9 1)"
FSCausal="$(python Table1CausalScore.py Suites/F9causal 1)"
echo $FR"   "$FRCausal"   "$FS"   "$FSCausal




Gcr="$(python Table1GroupScore.py Suites/Gcredit 1)"
Gcrcausal="$(python Table1CausalScore.py Suites/Gcausalcredit 1)"
echo "-   -   "$Gcr"   "$Gcrcausal

GR="$(python Table1GroupScore.py Suites/G8 0)"
GRCausal="$(python Table1CausalScore.py Suites/G8causal 0)"
GS="$(python Table1GroupScore.py Suites/G9 1)"
GSCausal="$(python Table1CausalScore.py Suites/G9causal 1)"
echo $GR"   "$GRCausal"   "$GS"   "$GSCausal




Hcr="$(python Table1GroupScore.py Suites/Hcredit 1)"
Hcrcausal="$(python Table1CausalScore.py Suites/Hcausalcredit 1)"
echo "-   -   "$Hcr"   "$Hcrcausal

HR="$(python Table1GroupScore.py Suites/H8 0)"
HRCausal="$(python Table1CausalScore.py Suites/H8causal 0)"
HS="$(python Table1GroupScore.py Suites/H9 1)"
HSCausal="$(python Table1CausalScore.py Suites/H9causal 1)"

echo $HR"   "$HRCausal"   "$HS"   "$HSCausal

