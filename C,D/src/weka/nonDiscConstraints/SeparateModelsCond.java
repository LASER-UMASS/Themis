/*SeparateModels
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package weka.nonDiscConstraints;
import java.io.*;
import java.util.*;
import weka.core.*;
import weka.classifiers.*;
import weka.classifiers.rules.ZeroR;
import weka.nonDiscConstraints.*;
import  java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import weka.classifiers.bayes.NaiveBayesSimple;
import weka.classifiers.trees.J48;
/**
 * Class for combining classifiers using unweighted average of
 * probability estimates (classification) or numeric predictions
 * (regression).
 *
 * Valid options from the command line are:<p>
 *
 * -B classifierstring <br>
 * Classifierstring should contain the full class name of a scheme
 * included for selection followed by options to the classifier
 * (required, option should be used once for each classifier).<p>
 *
 * @author Alexander K. Seewald (alex@seewald.at)
 * @author Eibe Frank (eibe@cs.waikato.ac.nz)
 * @version $Revision: 1.7 $
 */

public class SeparateModelsCond extends MultipleClassifiersCombiner {

  /**
   * Returns a string describing classifier
   * @return a description suitable for
   * displaying in the explorer/experimenter gui
   */
  public String globalInfo() {

    return "Class for combining classifiers using unweighted average of "
      + "probability estimates (classification) or numeric predictions "
      + "(regression).";
  }

    /**
   * Buildclassifier selects a classifier from the set of classifiers
   * by minimising error on the training data.
   *
   * @param data the training data to be used for generating the
   * boosted classifier.
   * @exception Exception if the classifier could not be built successfully
   */
    Random random = new Random(1);

    boolean [] relabel=new boolean[8];






//================================Two models concept===================
  /**
   * Buildclassifier selects a classifier from the set of classifiers
   * by minimising error on the training data.
   *
   * @param data the training data to be used for generating the
   * boosted classifier.
   * @exception Exception if the classifier could not be built successfully
   */

    static double binThresholds [][];
   // public static  Classifier rankerM=new NaiveBayesSimple();
   // public static  Classifier rankerF=new NaiveBayesSimple();
    public static  Classifier rankerM=new J48();
   public static  Classifier rankerF=new J48();

    
 public void buildClassifier(Instances data) throws Exception {
     
    Instances newDataMale = new Instances(data);
    Instances newDataFemale = new Instances(data);
    newDataMale.delete();
    newDataFemale.delete();
    int j;
    String sav="";

    for(j=0; j<data.numInstances();j++){
        if(Discrimination.getSAA())
            sav=Discrimination.trainInstsWithsa.instance(j).toString(0);
        else
            sav=data.instance(j).toString(0);

        if(sav.equals("Male"))
            newDataMale.add(data.instance(j));
        else
            newDataFemale.add(data.instance(j));
    }
    getClassifier(0).buildClassifier(newDataMale);
    getClassifier(1).buildClassifier(newDataFemale);
    rankerM.buildClassifier(newDataMale);
    rankerF.buildClassifier(newDataFemale);

    //classifierM.buildClassifier(data);
    //classifierF.buildClassifier(data);

        int f=0,m=0;
        double mp=0,fp=0;
        int caIndex=Discrimination.getCaIndex();
        int caValues=Discrimination.getCaValues();
        double [][][] biasStat=new double[caValues][2][2];
        Instances insts=data;
        Instances instsC []=new Instances[caValues];
        double n=insts.numInstances();
        for(int i=0;i<instsC.length;i++){
               instsC[i]=new Instances(insts);
               instsC[i].delete();
            }
            for(int i=0;i<insts.numInstances();i++){
                if(insts.instance(i).toString(0).equals(Discrimination.getSaDep()))
                    f++;
                else
                     m++;
                biasStat[(int)insts.instance(i).value(caIndex)][(int)insts.instance(i).value(0)][Integer.parseInt(Utils.doubleToString(insts.instance(i).classValue(), 0))]+=1;
                instsC[(int)insts.instance(i).value(Discrimination.getCaIndex())].add(insts.instance(i));
        }

        //For loop to construct an array of thresholds
         binThresholds=new double[instsC.length][2];
         double total=0;
         for(int i=0; i<instsC.length;i++){
                     total=0;
             double pos=0;
             double probs[]=new double[2];
             double posProbsM []=new double[instsC[i].numInstances()];
             double posProbsF []=new double[instsC[i].numInstances()];
             f=m=0;
             fp=mp=0;
             for(int k=0; k<instsC[i].numInstances();k++){
                 Instance inst=instsC[i].instance(k);
                 total=instsC[i].numInstances();
                 if(inst.classValue()==Discrimination.getDC())
                     pos++;
                 sav=inst.toString(0);
             if(sav.equals("Male")){
                      probs=rankerM.distributionForInstance(inst);
                      posProbsM[m++]=probs[1];
                      if(inst.classValue()==Discrimination.getDC())
                          mp++;
                 }
                 else{
                       probs=rankerF.distributionForInstance(inst);
                       posProbsF[f++]=probs[1];
                       if(inst.classValue()==Discrimination.getDC())
                          fp++;
                 }

             }// end of inner for loop
            posProbsF=sorting(posProbsF,f);
            posProbsM=sorting(posProbsM,m);
             //System.out.println("Before sorting: \t"+Arrays.toString(posProbsF));
             //Arrays.s.sort(posProbsM);
            // System.out.println("After sorting: \t"+Arrays.toString(posProbsF));
             //System.out.println("After sorting: \t"+Arrays.toString(posProbsM));
             
            // double posPercent=pos/total;
            double posPercent=((newDataMale.numInstances()/n)*mp/m)+((newDataFemale.numInstances()/n)*fp/f);
            //System.out.println(mp+fp+"\t "+pos);
            //double posPercent=((m/total)*mp/m)+((f/total)*fp/f);
             System.out.println("========="+i+"=========");
             int fPos=(int) (f * posPercent);
             
             binThresholds[i][0]=posProbsF[fPos];
             int mPos=(int) (m * posPercent);

             System.out.println("Size of Bin:=\t"+total+"\t Male:=\t "+m+"\tFemale:=\t"+f);
             System.out.println("Biased statistics:=\t\t Male Pos:=\t"+biasStat[i][1][1]+"\tFemale Pos:=\t"+biasStat[i][0][1]+"\tPos Class percent in bin=: \t"+posPercent);
             System.out.println("Unbiased statistics:=\t Male Pos:=\t"+mPos+"\t threshold \t"+posProbsM[mPos]);
             System.out.println("\t\t\t\t\t\tFemale Pos:=\t"+fPos+"\t threshold \t"+posProbsF[fPos]);
             binThresholds[i][1]=posProbsM[mPos];
             
             //System.out.println("HRW #"+i+"\t number of instances=\t"+instsC[i].numInstances()+" +ve ratio \t"+(pos/instsC[i].numInstances())*100);
         }//end of outer for loop
 }

 public double[] distributionForInstance(Instance instance) throws Exception {

      double[] probs=new double[2];
      double []scores=new double[2];
      String sav="null";
      if(Discrimination.getSAA())
          sav=Discrimination.saValue;
      else
            sav=instance.toString(0);

      
      // scores[0]=scores[0]/m_Classifiers.length;
      // scores[1]=scores[1]/m_Classifiers.length;
//System.out.println(instance.value(instance.attribute(Discrimination.getSaIndex()))+"\t"+instance.value(13)+"\t"+scores[1]);
    
      if(sav.equals("Male")){
            scores = getClassifier(0).distributionForInstance(instance);
          //System.out.println("male"+scores[1]+"\t th\t "+binThresholds[(int)instance.value(Discrimination.getCaIndex())][1]);
             if(scores[1]>=binThresholds[(int)instance.value(Discrimination.getCaIndex())][1])
             probs[1]=1;
             else
                 probs[0]=1;
      }
        else{
            scores = getClassifier(1).distributionForInstance(instance);
           //System.out.println("female"+scores[1]+"\t th\t "+binThresholds[(int)instance.value(Discrimination.getCaIndex())][0]);
            if(scores[1]>=binThresholds[(int)instance.value(Discrimination.getCaIndex())][0])
             probs[1]=1;
             else
                 probs[0]=1;
      }
  
      
    return probs;
  }

    /* @param data the training data to be used for generating the
   * boosted classifier.
   * @exception Exception if the classifier could not be built successfully
   */

 // FOr global base line

 /*
 public static double thresholds[];
 public void buildClassifier(Instances data) throws Exception {

    Instances newDataMale = new Instances(data);
    Instances newDataFemale = new Instances(data);
    newDataMale.delete();
    newDataFemale.delete();
    int j;
    String sav="";

    for(j=0; j<data.numInstances();j++){
        if(Discrimination.getSAA())
            sav=Discrimination.trainInstsWithsa.instance(j).toString(0);
        else
            sav=data.instance(j).toString(0);

        if(sav.equals("Male"))
            newDataMale.add(data.instance(j));
        else
            newDataFemale.add(data.instance(j));
    }
    getClassifier(0).buildClassifier(newDataMale);
    getClassifier(1).buildClassifier(newDataFemale);
    rankerM.buildClassifier(newDataMale);
    rankerF.buildClassifier(newDataFemale);

    //classifierM.buildClassifier(data);
    //classifierF.buildClassifier(data);

        int f=0,m=0;
        double mp=0,fp=0;

        double [][][] biasStat=new double[1][2][2];
        Instances insts=data;

        double n=insts.numInstances();

        //For loop to construct an array of thresholds
         thresholds=new double[2];
         double total=0;

             double pos=0;
             double probs[]=new double[2];
             double posProbsM []=new double[insts.numInstances()];
             double posProbsF []=new double[insts.numInstances()];
             f=m=0;
             fp=mp=0;
             for(int k=0; k<insts.numInstances();k++){
                 Instance inst=insts.instance(k);
                 total=insts.numInstances();

                 sav=inst.toString(0);
             if(sav.equals("Male")){
                      probs=rankerM.distributionForInstance(inst);
                      posProbsM[m++]=probs[1];
                      if(inst.classValue()==Discrimination.getDC())
                          mp++;
                 }
                 else{
                       probs=rankerF.distributionForInstance(inst);
                       posProbsF[f++]=probs[1];
                       if(inst.classValue()==Discrimination.getDC())
                          fp++;
                 }

             }// end of inner for loop
            posProbsF=sorting(posProbsF,f);
            posProbsM=sorting(posProbsM,m);
             //System.out.println("Before sorting: \t"+Arrays.toString(posProbsF));
             //Arrays.s.sort(posProbsM);
            // System.out.println("After sorting: \t"+Arrays.toString(posProbsF));
             //System.out.println("After sorting: \t"+Arrays.toString(posProbsM));

            // double posPercent=pos/total;
            double posPercent=((newDataMale.numInstances()/n)*mp/m)+((newDataFemale.numInstances()/n)*fp/f);
            //System.out.println(mp+fp+"\t "+pos);
            //double posPercent=((m/total)*mp/m)+((f/total)*fp/f);

             int fPos=(int) (f * posPercent);

             thresholds[0]=posProbsF[fPos];
             int mPos=(int) (m * posPercent);

             System.out.println("Size of Bin:=\t"+total+"\t Male:=\t "+m+"\tFemale:=\t"+f);
             System.out.println("Biased statistics:=\t\t Male Pos:=\t"+biasStat[0][1][1]+"\tFemale Pos:=\t"+biasStat[0][0][1]+"\tPos Class percent in bin=: \t"+posPercent);
             System.out.println("Unbiased statistics:=\t Male Pos:=\t"+mPos+"\t threshold \t"+posProbsM[mPos]);
             System.out.println("\t\t\t\t\t\tFemale Pos:=\t"+fPos+"\t threshold \t"+posProbsF[fPos]);
             thresholds[1]=posProbsM[mPos];

             //System.out.println("HRW #"+i+"\t number of instances=\t"+instsC[i].numInstances()+" +ve ratio \t"+(pos/instsC[i].numInstances())*100);

 }
  public double[] distributionForInstance(Instance instance) throws Exception {

      double[] probs=new double[2];
      double []scores=new double[2];
      String sav="null";
      if(Discrimination.getSAA())
          sav=Discrimination.saValue;
      else
            sav=instance.toString(0);

      if(sav.equals("Male")){
            scores = getClassifier(0).distributionForInstance(instance);
          //System.out.println("male"+scores[1]+"\t th\t "+binThresholds[(int)instance.value(Discrimination.getCaIndex())][1]);
             if(scores[1]>=thresholds[1])
             probs[1]=1;
             else
                 probs[0]=1;
      }
        else{
            scores = getClassifier(1).distributionForInstance(instance);
           //System.out.println("female"+scores[1]+"\t th\t "+binThresholds[(int)instance.value(Discrimination.getCaIndex())][0]);
            if(scores[1]>=thresholds[0])
             probs[1]=1;
             else
                 probs[0]=1;
      }


    return probs;
  }
*/
//function to sort descending
public static double[] sorting(double [] array,int length){
    if(length<=0)
        return array;
    double []arrayToSort=new double[length];
    System.arraycopy(array, 0, arrayToSort, 0, length);
             int max=length;
             double val1=0,val2=0;
             //double []sortedArray=new double[length];
             double temp;
           for(int index=0;index<length;index++)
             for(int i=0;i<max-1;i++){
               try{
                 val1=arrayToSort[i];
                 val2=arrayToSort[i+1];

                        if(val1<val2){  //swapping for sort descending
                                 temp=arrayToSort[i];
                                 arrayToSort[i]=arrayToSort[i+1];
                                 arrayToSort[i+1]=temp;
                          }     //end of  if


             } catch (NumberFormatException e){
                 System.out.println(" Probelme with sorting during Massaging");
                 }

                 }//end of out for-i loop

             return arrayToSort;
 }   // End of sorting function

  //===============================End of code for two models concepts============
  /**
   * Output a representation of this classifier
   */
  public String toString() {

    if (m_Classifiers == null) {
      return "Vote: No model built yet.";
    }

    String result = "Vote combines";
    result += " the probability distributions of these base learners:\n";
    for (int i = 0; i < m_Classifiers.length; i++) {
      result += '\t' + getClassifierSpec(i) + '\n';
    }

    return result;
  }

  /**
   * Main method for testing this class.
   *
   * @param argv should contain the following arguments:
   * -t training file [-T test file] [-c class index]
   */
  public static void main(String [] argv) {

    try {
      System.out.println(Evaluation.evaluateModel(new SeparateModelsCond(), argv));
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }
  }

}
