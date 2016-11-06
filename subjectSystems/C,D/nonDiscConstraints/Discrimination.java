/*
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

/*
 *    Evaluation.java
 *   Author xxxxx  Feb,2010
 *
 */

package weka.nonDiscConstraints;

import java.util.*;
import java.io.*;
import weka.core.*;
import java.lang.Object.*;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import weka.classifiers.Evaluation;
import java.util.Hashtable;
import java.util.Enumeration;
//import weka.estimators.*;
//import java.util.zip.GZIPInputStream;
//import java.util.zip.GZIPOutputStream;
//import weka.classifiers.bayes.*;


public class Discrimination {
    //change to accuracy std
     public static double []acc=new double[10];
    public static boolean compareFlag=true;
    public double [][][] m_Counts;
    public double [] m_Priors;
    public static int totalFolds=10,count=0;
    public static double []disc=new double[totalFolds];
    public static double []discCond=new double[totalFolds];
   
    //    public static Instances trainInstsWithoutsa=null;
    public static Instances trainInstsWithsa=null,testWithSA=null, data=null;
    public static Vector v ;
    public static Vector<Vector<String>> result =new  Vector<Vector<String>>();
    
    //   set this flag to ignore SA from classification
    private static boolean o_CLICheck=false,saAbsent=false;
   
    // Flag to set default Dependency  parameters
   private static boolean setSaDepfault=true,setSaFavDefault=true;
    // it stores the value of SA when SA is excluded from classification
    public static String saName="?",saValue=null,classifierName=null,filterName=null,rankerName=null,o_FileName="outputFile.txt",trainSetName=null;
    
     /**    Discrimination relate38.04
d variables*/
    public static double savCount=0,favCount=0,savPos=0,favPos=0;
    public static double spCount=0,fpCount=0,spPos=0,fpPos=0; //for the calculation of Disc for Each Fold
    public static  double discrimination=0;
    // to add DC probability into output file
   public static double dcProb=0;

//ICs PARAMETERS FOR CENSUS INCOME DATASET

    public static String sa,sa_Deprived="Female",sa_Favored="Male";
    public static int sa_Index=0,dc=1,ndc=0;

//ICs PARAMETERS FOR COMMUNITIES AND CRIMES DATASET
   //public static String sa,sa_Deprived="0",sa_Favored="1";
   //public static int sa_Index=0,dc=1,ndc=0;


 //ICs PARAMETERS FOR GERMAN CREDIT DATASET
   // public static String sa,sa_Deprived="A131",sa_Favored="A132";    //sa= sensitive attribute   sa_deprived=deprived community    sa_favored=favord community of sa
   // public static int sa_Index=12,dc=1,ndc=0;
   
/*public Discrimination(Instances data,String  sa,String sa_deprived) throws Exception {
  }
  */
  public static void setDepParameters(Instances insts){
      trainInstsWithsa=insts;
      if(setSaDepfault)
      sa_Deprived=trainInstsWithsa.attribute(sa_Index).value(0);
      if(setSaFavDefault)
      sa_Favored=trainInstsWithsa.attribute(sa_Index).value(1);
       }

 public static  void setSaIndex(int saIndex){
     sa_Index=saIndex;
 }
 
 public static int getSaIndex(){
     return sa_Index;
 }
 public static void setSaDep(String  saDep){
     sa_Deprived=saDep;
     setSaDepfault=false; // dont set this valsue to default
 }
 
 public  static String getSaDep(){
     return sa_Deprived;
 }
 public static void setSaFav(String saFav){
     sa_Favored=saFav;
     setSaFavDefault=false;
 }
 
 public static String getSaFav(){
     return sa_Favored;
 }
 public static void setDC(int desiredClass){
     dc=desiredClass;
     if(dc==1)
         ndc=0;
     else
         ndc=1;
 }
 
 public static int getDC(){
     return dc;
 }

 public static int getNDC(){
  return ndc;   
 }
  public static void set_O_FileName(String outPutFileName){
     o_FileName=outPutFileName;
 }
  public static void set_CLICheck(boolean cli){
     o_CLICheck=cli;
 }
  public static boolean get_CLICheck(){
     return o_CLICheck;
 }
   
 public static void setSAA(boolean f){
    saAbsent = f;
 }
 public static boolean getSAA(){
    return saAbsent;
 }
   /** After printing the results in Weka window, this function intitialize all the static variablle used to
    *record discrimination.
    * this is called from following function and class
    * public String toSummaryString(String title,boolean printComplexityStatistics)
    * weka.classifiers.Evaluation.java
    */
public static void init_param(){
    // System.out.println("   SA with + Class =: "+savPos+"/"+savCount + "  Non Sensitive with + class =   " +favPos+"/"+favCount+" %\n");
    favPos=favCount=savPos=savCount=totalFolds=0;
    fpPos=spPos=fpCount=spCount=0;
    countCond=new double[caValues][2][2];
    //caIndex=4;caValues=7;
    countCond=new double[caValues][2][2];

}
  static int inst_number=0;
  static int missingValues=0;
   /** continue to calculate discrimination with the output of each instace
    * this is called from following function and class
    * public double evaluateModelOnce(Classifier classifier, Instance instance)
    * weka.classifiers.Evaluation.java
    */
  
 public static int caIndex=1,caValues=5;//insts.attribute(caIndex).numValues();
public static  void setCaIndex(int ca){
     caIndex=ca;
 }

 public static int getCaIndex(){
     return caIndex;
 }

 public static  void setCaValues(int caVals){
       caValues=caVals;
       countCond=new double[caValues][2][2];
 }
 public static int getCaValues(){
     return caValues;
 }
  public static   double [][][] countCond=new double[caValues][2][2];
// Method to calculate discrimination from a give data
  public static double discCalculation(Instances insts){
      double d=0;
      double s=0,sp=0,f=0,fp=0;
      Enumeration enumInsts = insts.enumerateInstances();
      int classValue=0;
      String saValue;
      while (enumInsts.hasMoreElements()) {
          Instance instance = (Instance) enumInsts.nextElement();
          if (!instance.classIsMissing()) {
              saValue= instance.toString(sa_Index);
              classValue=(int) instance.classValue();
              if(saValue.equals(sa_Deprived)){
                  s++;
                  if( classValue==dc)//sav.equals(sa_Deprived) &&
                      sp++;
              }
              else if(!saValue.equals(sa_Deprived)){
                  f++;
                  if(classValue==dc)
                      fp++;
              }
        }
    }
      if(s==0)s=1;if(f==0)f=1;
      d=((fp/f)-(sp/s))*100;
      return d;
  }
public static double mtp=0,mtn=0,ftp=0,ftn=0,actMP=0,actFP=0;
  public static void discCalculation(Instance instance,Double pred) {
      //System.out.println(instance);
      double actPred=instance.classValue();
     // pred=actPred; // To calculate the actaul disc in data
       int saVal=0;
           String sav=instance.toString(sa_Index);
           
           if(instance.numAttributes()<trainInstsWithsa.numAttributes())
          //if(!sav.equals(sa_Deprived)&&!sav.equals(sa_Favored))
                sav=saValue;
                //System.out.println(++inst_number+" sav = "+sav);
                // save the output to the vector result
                 prepareResult(instance,sav,pred);
          if(sav.equals(sa_Deprived)){
              saVal=0;
                 savCount++;            
                 if( pred==dc)//sav.equals(sa_Deprived) &&
                        savPos++;
              if(pred==actPred)// code for FP% and FN%
                  if(pred==dc) ftp++;
                  else ftn++;
               if(actPred==dc)actFP++;
             }
            //else if(sav.equals(sa_Favored)){
              else if(!sav.equals(sa_Deprived)){
                  saVal=1;
                favCount++;
                if(pred==dc)//sav.equals(sa_Favored) && 
                        favPos++;
                if(pred==actPred) // code for FP% and FN%
                  if(pred==dc) mtp++;
                  else mtn++;
                if(actPred==dc)actMP++;
          }
//make saVal to more general, independent of assumption
                
// if(saAbsent)
 //      countCond[(int)instance.value(caIndex-1)][saVal][Integer.parseInt(Utils.doubleToString(pred, 0))]+=1;
 //else
 //      countCond[(int)instance.value(caIndex)][(int)instance.value(0)][Integer.parseInt(Utils.doubleToString(pred, 0))]+=1;
 
 }


  /** Method to calculate conditional discrimination
   *
  */

 public static double getCondDepForFold(){
   double dq=0;//[] dep=new double[caValues];
   double fTot=0,fposTot=0,mTot=0,mposTot=0;
   double []total=new double [5];
   double n=0,d=0,di;
     for (int i=0; i<caValues;i++){
         fTot+=countCond[i][0][0]+countCond[i][0][1];
         fposTot+=countCond[i][0][1];
         mTot+=countCond[i][1][0]+countCond[i][1][1];
         mposTot+=countCond[i][1][1];
     }
        n=fTot+mTot;
     for (int i=0; i<caValues;i++){
         double f=countCond[i][0][0]+countCond[i][0][1];
         double fpos=countCond[i][0][1];
         double m=countCond[i][1][0]+countCond[i][1][1];
         double mpos=countCond[i][1][1];
        // if(f!=0)f=1;if(fpos==0)fpos=1;if(m==0)m=1;if(mpos==0)fpos=1;
         if(f==0)f=1;if(m==0)m=1;
         if(fTot==0)fTot=1;if(mTot==0)mTot=1;


         di=(m/mTot-f/fTot)
                 *((0.5*(mpos/m))+(0.5*(fpos/f)))*100;
        
      //p*(+|ei) = p(m)pc(+|m,ei) + p(f)pc(+|f,ei), which is weighted average of the acceptance threshold of male and female.   
         // di=(m/mTot-f/fTot)*(((mTot/n)*(mpos/m))+((fTot/n)*(fpos/f)))*100;

         // Direct method to calculate conditional discrimination
         // if(f==0)f=1;if(m==0)m=1;
        //di=(((f+m)/n)*(mpos/m-fpos/f))*100;
        //if(di<0)
        //    di=di*-1;
//System.out.println("disc for CA_Attribute value"+i+":=\t"+di+"\t f="+f+"\t fpos="+fpos+"\t m="+m+"  mpos:=\t"+mpos);
d+=di;
     }
        dq=(mposTot/mTot-fposTot/fTot)*100;
    // System.out.println(dq+"cond disc for current fold:\t"+d+"\tTotal insts:\t"+n);
     return (dq-d);
 /*        fTot+=f; mTot+=m;
         fposTot+=fpos;mposTot+=mpos;
//total[0]= disc(G)*size(G)
         subgroupDisc=(mpos/m-fpos/f)*100;
        if(subgroupDisc>=-100){
  //to calculate avg disc, max disc and min disc

         if(subgroupDisc>max)
             max=subgroupDisc;
         if(subgroupDisc<min)
             min=subgroupDisc;
         total[0]+=subgroupDisc;
  
  //to calculate absolute avg disc, max disc and min disc
         if(subgroupDisc<0)
            dep=subgroupDisc*-1;
         else
             dep=subgroupDisc;
         if(dep>maxAbs) 
             maxAbs=dep;
         if(dep<minAbs) 
             minAbs=dep;
         total[1]+=dep;
         
  //to calculate weighter avg disc, weighted max disc and weighted min disc
          subgroupDisc=subgroupDisc*(f+m);
         if(subgroupDisc>wMax)
             wMax=subgroupDisc;
         if(subgroupDisc<wMin)
             wMin=subgroupDisc;
         total[2]+=subgroupDisc;
         
    //to calculate weighter avg disc, weighted max disc and weighted min disc
          if(subgroupDisc<0)
            dep=subgroupDisc*-1;
         else
             dep=subgroupDisc;
         if(dep>wMaxAbs)
             wMaxAbs=dep;
         if(dep<wMinAbs)
             wMinAbs=dep;
         total[3]+=dep;
      //   
        }

//System.out.println("Occupation:\t"+(i+1)+"\tFemale:\t"+f+"\tmale:\t"+m+"\tDiscrimination:\t"+dep[i]*100);
System.out.println("\tMax:\t"+max+"\tMin:\t"+min+"\tAvg:\t"+total[0]+"\tAbs Max:\t"+maxAbs+"\tAbs Min:\t"+minAbs+"\tAbs Avg:\t"+total[1]);
    System.out.println("\t WeithedMax:\t"+wMax+"\tMin:\t"+wMin+"\tAvg:\t"+total[2]+"\tAbs Max:\t"+wMaxAbs+"\tAbs Min:\t"+wMinAbs+"\tAbs Avg:\t"+total[3]);
     }
        total[0]=total[0]/caValues;
        total[1]=total[1]/caValues;
         //total[1]= (disc(G)*size(G))/total size
        total[2]=total[2]/((mTot+fTot)*caValues);
        total[3]=total[3]/((mTot+fTot)*caValues);
    // System.out.println("\t\tConditional Independence\t\t");
     //System.out.println("\tMax:\t"+max+"\tMin:\t"+min+"\tAvg:\t"+total[0]+"\tAbs Max:\t"+maxAbs+"\tAbs Min:\t"+minAbs+"\tAbs Avg:\t"+total[1]);
    // System.out.println("\t WeithedMax:\t"+wMax+"\tMin:\t"+wMin+"\tAvg:\t"+total[2]+"\tAbs Max:\t"+wMaxAbs+"\tAbs Min:\t"+wMinAbs+"\tAbs Avg:\t"+total[3]);
     return total[0];*/
  }
 public static double getCondDisc(int folds){
double total=0;
     for (int i=0; i<folds;i++){
         total+=discCond[i];
         }
     return (total/folds);
  }

   public static void DiscForCV(int currentFold){
      // fold=currentFold;
      disc[currentFold]=(((favPos-fpPos)/(favCount-fpCount))-((savPos-spPos)/(savCount-spCount)))*100;
    System.out.println(" \n\n Favored + = "+(favPos-fpPos)+"  Favored_Total ="+(favCount-fpCount)+"  Deprived + = "+(savPos-spPos)+" Deprived_Total = "+(savCount-spCount));
      fpPos=favPos;   // to get (favPos-fpPos) which shows the value of favPos,i.e., favored with diesired class for next fold
      fpCount=favCount;
      spPos=savPos;
      spCount=savCount;
     //  Accuracy for each fold
     discCond[currentFold]=getCondDepForFold();
    countCond=new double[caValues][2][2];
      System.out.println(" Discrimination for fold ="+(currentFold+1)+"   = "+disc[currentFold]+
              "\tConditional Disc = "+discCond[currentFold]);

 }
  /** It prints the resultant discrimination into weka
    * this is called from following function and class
    * public String toSummaryString(String title,boolean printComplexityStatistics)
    * weka.classifiers.Evaluation.java
    */
/* public static double getDisc() {
   //System.out.println(" favored + = "+favPos+"  f ="+favCount+"  Dep + = "+savPos+" dep = "+savCount);
   double pos=favPos+savPos, favNeg=favCount-favPos,savNeg=savCount-savPos;
   double neg=favNeg+savNeg, total=favCount+savCount;
   if (favCount == 0 && savCount == 0){
        discrimination= 0;}
   else{
    if (savCount == 0){
        discrimination= (favPos/favCount)*100;
   }else
       if (favCount == 0){
           discrimination= -(savPos/savCount)*100;
   }else       
      discrimination=(favPos/pos)*(Math.log((favPos/pos)/((pos/total)*(favCount/total))))+
                     (savPos/pos)*(Math.log((savPos/pos)/((pos/total)*(savCount/total))))+
                     (favNeg/neg)*(Math.log((favNeg/neg)/((neg/total)*(favCount/total))))+
                     (savNeg/neg)*(Math.log((savNeg/neg)/((neg/total)*(savCount/total))));

              
   }
       return discrimination;
 }
 */
  public static double getDisc() {

   if (favCount == 0 && savCount == 0){
        discrimination= 0;}
   else{
    if (savCount == 0){
        discrimination= (favPos/favCount)*100;
   }else
       if (favCount == 0){
           discrimination= -(savPos/savCount)*100;
   }else
      discrimination=((favPos/favCount)-(savPos/savCount))*100;
   }
  // System.out.println("Error rate =: male"+((favCount-(mtp+mtn))/favCount)*100+"\tFemale=:\t"+((savCount-(ftp+ftn))/savCount)*100);
    System.out.println("Error rate =: male"+(((mtp+mtn))/favCount)*100+"\tFemale=:\t"+(((ftp+ftn))/savCount)*100); //True Positive rate
System.out.println(mtp+"\t"+ ftp +"\t"+mtn +"\t"+ftn);
   System.out.println(Utils.doubleToString((mtp/actMP*100), 0, 0)+"%/"+Utils.doubleToString((ftp/actFP*100), 0, 0)
           +"%\t"+Utils.doubleToString((mtn/(favCount-actMP)*100), 0, 0)+"%/"+Utils.doubleToString((ftn/(savCount-actFP)*100), 0, 0)+"%");
       return discrimination;

  }

  /** it prints standard deviation of discrimination in to weka
    * this is called from following function and class
    * public String toSummaryString(String title,boolean printComplexityStatistics)
    * weka.classifiers.Evaluation.java
    */ 
public static double getStdvDisc() {
    return calculateStdv(disc);
   /* double stdDev=0;
    double mean=0;
    if(totalFolds>1){
             for(int fold=0;fold<totalFolds;fold++)
                 mean+=disc[fold];
             mean=mean/totalFolds;
             //System.out.println("mean=: "+mean);
             for(int fold=0;fold<totalFolds;fold++){
                    stdDev+=(disc[fold]-mean)*(disc[fold]-mean);
             }
                    stdDev=stdDev/(totalFolds-1);
                stdDev = (float) Math.sqrt(stdDev);
    }
             return stdDev;*/
}

public static double getStdvDiscCond(){
    return calculateStdv(discCond);
}

//change to accuracy std
private static double prevCorrect=0, prevTotal=0;
public static void accuracyStd(double correct,double incorrect,int i){
    acc[i]=((correct-prevCorrect)/(correct+incorrect-prevTotal))*100;
       //System.out.println(" correct   is =:\t"+correct()+" \t for this fold \t"+(correct()-acc[0][1])+"\t total=:\t"+(correct()+incorrect()-acc[0][2]));
      System.out.println("Accuracy for the fold \t"+i+"\t is =:\t"+acc[i]);
       prevCorrect=correct;
       prevTotal=correct+incorrect;
}

//change to accuracy std
  public static  double getAccuracyStd() {

             return calculateStdv(acc);
}
public static double calculateStdv(double array[]) {
    int folds=array.length;
    double stdDev=0;
    double mean=0;
    if(folds>1){
             for(int fold=0;fold<folds;fold++)
                 mean+=array[fold];
             mean=mean/folds;
             //System.out.println("mean=: "+mean);
             for(int fold=0;fold<folds;fold++){
                    stdDev+=(array[fold]-mean)*(array[fold]-mean);
             }
                    stdDev=stdDev/(folds-1);
                stdDev = (float) Math.sqrt(stdDev);
    }
             return stdDev;
}

 public static void prepareResult(Instance instance,String sav, Double pred){
       v=new Vector();
       v.add(sav);
       v.add(Utils.doubleToString(instance.classValue(),0));
       v.add(Utils.doubleToString(pred,0));
       v.add(Utils.doubleToString(dcProb,10));
       result.add(v);

  }
/**
 * Functions to save output for each test  dorectly into file
 * it is called from classifiers.Evaluation.java  #find: change to display disc
 *
 */
  public static void saveResultsToFile() {

    try {
        //double correct=0,total=0,savp=0,savn=0,favp=0,favn=0;
         Vector <String> current=new  Vector<String>();
         System.out.println("Saving to file...");
         File outputFile = new File(o_FileName);
         FileWriter w = new FileWriter(outputFile);

         w.write("#Classifier:\t"+classifierName+"\n #Filter:\t"+filterName+"\n #Ranker:\t"+rankerName+"\n #Dataset:\t"+trainSetName +"\n");
        w.write("#SA Name:\t"+saName+"\n#Deprived:\t"+sa_Deprived+"\n#Class_Attribute:\t"+trainInstsWithsa.classAttribute().name()+
                 "\n #DC:\t"+trainInstsWithsa.classAttribute().value(dc)+"\n #DC_Index:\t"+dc+"\n #Date:\t"+getDateNow()+"\n");
         w.write("#SA_value\tActual_Class\tPred_Class\tDCProb\n");
     for(int i=0;i<result.size();i++){//i<result.size()
          current=result.get(i);


          w.write("'"+current.get(0)+"'"+"\t"+current.get(1)+"\t"+current.get(2)+"\t"+current.get(3));//
          w.write("\n");
      }

      w.close();

    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }
  /**
   * functions to write Accuracy and discrimination directly into file
   * It also writes std dev for acc and disc
   * input disc, disc_stdDev, accuracy, acc_stdDev
   * writes the output into a file with name "results" in the working directory
   * it is called from classifiers.Evaluation.java  #find: change to display disc
   */

  public static void saveResultsToFile(String disc, String std, String acc, String accStd) {

    try {
        //File outputFile = new File("results.txt");
         //FileWriter w = new FileWriter(outputFile);
         BufferedWriter writer = new BufferedWriter(new FileWriter(o_FileName,true)) ;
        writer.write(disc+"\t"+std+"\t"+acc+"\t"+accStd+"\n") ;
        writer.close() ;

    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }
  //please reoder the place of cond-dic and accuracy score in the final version
  public static void saveResultsToFile(String disc, String std, String acc, String stdAcc,String discCond, String stdCond) {

    try {
        //File outputFile = new File("results.txt");
         //FileWriter w = new FileWriter(outputFile);
         BufferedWriter writer = new BufferedWriter(new FileWriter(o_FileName,true)) ;
        writer.write(disc+"\t"+std+"\t"+acc+"\t"+stdAcc+"\t"+discCond+"\t"+stdCond+"\n") ;
        writer.close() ;

    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }

   public static void saveResultsToFile(String disc, String discCond,String acc) {

    try {
        //File outputFile = new File("results.txt");
         //FileWriter w = new FileWriter(outputFile);
         BufferedWriter writer = new BufferedWriter(new FileWriter(o_FileName,true)) ;
        writer.write(disc+"\t"+discCond+"\t"+acc+"\n") ;
        writer.close() ;

    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }
 /**
  *Function to get the current date and time
 */

public static String getDateNow() {

  Calendar currentDate = Calendar.getInstance();
  SimpleDateFormat formatter= new SimpleDateFormat("yyyy/MMM/dd HH:mm:ss");
  String dateNow = formatter.format(currentDate.getTime());
  return(dateNow);

  }

  /**
   * sets the value of SA when SA is not a part of test set
   * called from ClassifierPanel.java
   */
  public static void checkSAValue(Instance inst){
                saValue=inst.toString(sa_Index);

     }

/*** used to find the discrimnatory attribut from the inout dataset
 * 
 */

 public  void discAttributeSelection(Instances instances)throws Exception{
     instances.setClassIndex(instances.numAttributes()-1);
      int attIndex=0,maxIndex=0,minIndex=0;
      double [] conf;
      double []count;
      datasetScan(instances);
      Enumeration enumInsts = instances.enumerateInstances();
        //while (enumInsts.hasMoreElements()) 
        {
        Instance instance = (Instance) enumInsts.nextElement();
      if (!instance.classIsMissing()) {
	Enumeration enumAtts = instances.enumerateAttributes();
	attIndex = 0;
	while (enumAtts.hasMoreElements()) {
	  Attribute attribute = (Attribute) enumAtts.nextElement();
          conf=new double[attribute.numValues()];
           count=new double[attribute.numValues()];
	  //if (!instance.isMissing(attribute)) {
	    if (attribute.isNominal()) {
                for(int i=0;i<attribute.numValues();i++){
	     conf[i]=m_Counts[dc][attIndex][i];
            // System.out.print(conf[i]+"    \t value: "+attribute.value(i));
                }
                for(int j=0; j<instances.numClasses();j++){
                    if(j!= dc){
                        for(int i=0; i<attribute.numValues();i++){
                            count[i]+=m_Counts[j][attIndex][i];
                        }
                    }
                }
	     
              //System.out.print("\n");
              double maxDisc=0, averageConf=0;
              int threshold=(instances.numInstances()*8/100);
           for(int i=0; i<attribute.numValues();i++){
                count[i]=conf[i]+count[i];
                conf[i]=(conf[i]/count[i])*100;
                //System.out.print(" conf =:"+conf[i]+"  count =: "+count[i]);
                 averageConf+=conf[i];
         
     
           }
              double minDisc=0;
              maxDisc=0;
              for(int i=0; i<attribute.numValues();i++){
                  if(conf[i]>=maxDisc && count[i]>threshold ){//
                      maxDisc=conf[i];
                      maxIndex=i;
                  }
                  if((minDisc>conf[i] || minDisc==0) && count[i]>threshold){//&& count[i]>threshold
                      minDisc=conf[i];
                      minIndex=i;
                  }
              }
              averageConf=averageConf/attribute.numValues();
          //System.out.print("\n Attribute =: "+attribute.name()+"  avg_con =:"+averageConf+" \t max_disc =: "+(maxDisc-minDisc)+" favored =:"+attribute.value(maxIndex)+"  Favored confidence =: "+maxDisc
                 // +" Deprived =:"+attribute.value(minIndex)+"  deprived confidence =: "+minDisc);
          //System.out.print("\n avg_con =:"+averageConf+" \t max_disc =: "+(maxDisc-minDisc)+" favored =:"+attribute.value(maxIndex)+" Deprived =:"+attribute.value(minIndex));
            }
          attIndex++;
	}
	
      }
    }
    
  }
 /*
  *  A single scan of the data set in order to find SA
  **/
 public void datasetScan(Instances instances){
      
       int attIndex = 0;
    double sum;   
    
    // Reserve space
    m_Counts = new double[instances.numClasses()]
      [instances.numAttributes() - 1][0];
   Enumeration enu = instances.enumerateAttributes();
     while (enu.hasMoreElements()) {
          Attribute attribute = (Attribute) enu.nextElement();
        if (attribute.isNominal()) {
	for (int j = 0; j < instances.numClasses(); j++) {
	  m_Counts[j][attIndex] = new double[attribute.numValues()];
	}
      } else {
	for (int j = 0; j < instances.numClasses(); j++) {
	  m_Counts[j][attIndex] = new double[1];
	}
      }
      attIndex++;
    }
    
    // Compute counts and sums
    Enumeration enumInsts = instances.enumerateInstances();
    while (enumInsts.hasMoreElements()) {
      Instance instance = (Instance) enumInsts.nextElement();
      if (!instance.classIsMissing()) {
	Enumeration enumAtts = instances.enumerateAttributes();
	attIndex = 0;
	while (enumAtts.hasMoreElements()) {
	  Attribute attribute = (Attribute) enumAtts.nextElement();
	  if (!instance.isMissing(attribute)) {
	    if (attribute.isNominal()) {
	      m_Counts[(int)instance.classValue()][attIndex]
		[(int)instance.value(attribute)]++;
	    } else {
                                        
	      m_Counts[(int)instance.classValue()][attIndex][0]++;
	    }
	  }
	  attIndex++;
	}
	//m_Priors[(int)instance.classValue()]++;
      }
    }
    
  }
 

 
}//end of class 


 
  // Exra but reusable code
 /*        
  *         double correct=0,total=0,savp=0,savn=0,favp=0,favn=0; 
           //code to calculate statistics internally
          if(current.get(1).equals(current.get(2))) correct++;
          if(current.get(0).equals("0")) 
                if(current.get(2).equals("1") ) savp++;
                else  savn++;
          if(current.get(0).equals("1")) 
                if(current.get(2).equals("1") ) favp++;
                else  favn++;
          
          total=i;
   
          
          
   System.out.println(" Accuracy  "+((correct/--total)*100 )+"  Disc  "+ (((favp/(favp+favn))-(savp/(savp+savn))))*100);

*/