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
 *    Vote.java
 *    Copyright (C) 2000 Alexander K. Seewald
 *
 */

package weka.classifiers.meta;

import java.io.*;
import java.util.*;
import weka.core.*;
import weka.classifiers.*;
import weka.classifiers.rules.ZeroR;
import weka.nonDiscConstraints.*;

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

public class Vote1 extends MultipleClassifiersCombiner {
  
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
 public void buildClassifier1(Instances data) throws Exception {
     
    Instances newData = new Instances(data);

    Instances trainData = new Instances(data);
    Instances valData = new Instances(data);
    trainData.delete();
    valData.delete();
    trainData=data.trainCV(3, 2,random);
    valData=data.testCV(3, 2);
   /*
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
    System.out.println("Male:  "+newDataMale.numInstances()+ "   Female: "+newDataFemale.numInstances());
    getClassifier(0).buildClassifier(newDataMale);
       getClassifier(1).buildClassifier(newDataFemale);
       getClassifier(2).buildClassifier(data);*/
     trainData.deleteWithMissingClass();

    for (int i = 0; i < m_Classifiers.length; i++) {
      getClassifier(i).buildClassifier(trainData);
    }
     // Vector<Vector<Double>> leaves =new  Vector<Vector<Double>>();
     Vector [] leaves=new Vector[8];
     for (int i = 0; i < 8; i++){
        leaves[i] = new Vector();
        }
      int [][] pred=new int[valData.numInstances()][m_Classifiers.length];
      double[] probs;
      for (int i=0; i<valData.numInstances();i++){
          for(int j=0;j<m_Classifiers.length;j++){
           probs= getClassifier(j).distributionForInstance(valData.instance(i));
               if(probs[1]>probs[0])
                   pred[i][j]=1;
               else
                   pred[i][j]=0;
          }//end of inner for with J counter
      }
      int saInfo[][]=new int[8][3];
      double leaf=0;
      double totalLeaves=Math.pow(2,m_Classifiers.length)/2;
      double half=totalLeaves/2;
      for (int i=0; i<valData.numInstances();i++){
           String sav=valData.instance(i).toString(0);
           
       if(pred[i][0]==1 && pred[i][1]==1 && pred[i][2]==1){
           leaves[0].add(i);
           if(sav.equals("Female"))
               saInfo[0][0]++;
           else
               saInfo[0][1]++;
           if(valData.instance(i).classValue()==1)
               saInfo[0][2]++;
       }
       else if(pred[i][0]==1 && pred[i][1]==1 && pred[i][2]==0){
           leaves[1].add(i);
           if(sav.equals("Female"))
               saInfo[1][0]++;
           else
               saInfo[1][1]++;
           if(valData.instance(i).classValue()==1)
               saInfo[1][2]++;
       }
       else if(pred[i][0]==1 && pred[i][1]==0 && pred[i][2]==1){
           leaves[2].add(i);
           if(sav.equals("Female"))
               saInfo[2][0]++;
           else
               saInfo[2][1]++;
           if(valData.instance(i).classValue()==1)
               saInfo[2][2]++;
       }
       else if(pred[i][0]==1 && pred[i][1]==0 && pred[i][2]==0){
           leaves[3].add(i);
           if(sav.equals("Female"))
               saInfo[3][0]++;
           else
               saInfo[3][1]++;
           if(valData.instance(i).classValue()==1)
               saInfo[3][2]++;
       }
       else if(pred[i][0]==0 && pred[i][1]==1 && pred[i][2]==1){
           leaves[4].add(i);
           if(sav.equals("Female"))
               saInfo[4][0]++;
           else
               saInfo[4][1]++;
           if(valData.instance(i).classValue()==1)
               saInfo[4][2]++;
       }
       else if(pred[i][0]==0 && pred[i][1]==1 && pred[i][2]==0){
           leaves[5].add(i);
           if(sav.equals("Female"))
               saInfo[5][0]++;
           else
               saInfo[5][1]++;
           if(valData.instance(i).classValue()==1)
               saInfo[5][2]++;
       }
       else if(pred[i][0]==0 && pred[i][1]==0 && pred[i][2]==1){
           leaves[6].add(i);
           if(sav.equals("Female"))
               saInfo[6][0]++;
           else
               saInfo[6][1]++;
           if(valData.instance(i).classValue()==1)
               saInfo[6][2]++;
       }
       else if(pred[i][0]==0 && pred[i][1]==0 && pred[i][2]==0){
          leaves[7].add(i);
          if(sav.equals("Female"))
               saInfo[7][0]++;
           else
               saInfo[7][1]++;
          if(valData.instance(i).classValue()==1)
               saInfo[7][2]++;
          
       }
       
           //System.out.print("\n"+i+"\t");
          /*for(int j=0;j<m_Classifiers.length;j++){
              if(classPred[i][j]==1)
              {
               totalLeaves=totalLeaves-half;
                half=totalLeaves/2;
              }
             else
             {
                 totalLeaves=totalLeaves/2+half;
                half=totalLeaves/2;
             }
       System.out.print("\t"+classPred[i][j]);
          }//end of inner for with J counter*/
      }
      for(int i =0;i<8; i++){
          if(leaves[i].size()/2.0<saInfo[i][2])
              relabel[i]=true;
          else
              relabel[i]=false;
          System.out.println("length of leaf \t"+i+":\t"+leaves[i].size()+"\t Famle: \t"+saInfo[i][0]+"\t Male:\t"+saInfo[i][1]+"\t positive:\t"+saInfo[i][2]+"\t flag\t"+relabel[i]);
  }
 }


  public double[] distributionForInstance1(Instance instance) throws Exception {
  /* double[] probs = getClassifier(0).distributionForInstance(instance);
    for (int i = 1; i < m_Classifiers.length; i++) {
      double[] dist = getClassifier(i).distributionForInstance(instance);
      for (int j = 0; j < dist.length; j++) {
	probs[j] += dist[j];
      }
    }
    for (int j = 0; j < probs.length; j++) {
      probs[j] /= (double)m_Classifiers.length;
    }*/
      double [] dist=new double[2];
      int [] pred=new int[m_Classifiers.length];
      double[] probs;
     for(int j=0;j<m_Classifiers.length;j++){
           probs= getClassifier(j).distributionForInstance(instance);
               if(probs[1]>probs[0])
                   pred[j]=1;
               else
                   pred[j]=0;
          }//end of inner for with J counter
      String sav=instance.toString(0);
    if(pred[0]==1 && pred[1]==1 && pred[2]==1){
       if(relabel[0])
           dist[1]=1;
        else
            dist[1]=0;
        //dist[0]=1;

       }
       else if(pred[0]==1 && pred[1]==1 && pred[2]==0){
          if(relabel[1])
           dist[1]=1;
        else
            dist[1]=0;
       }
       else if(pred[0]==1 && pred[1]==0 && pred[2]==1){
          //if(relabel[2])
           if(sav.equals("Female"))
           dist[1]=1;
        else
            dist[0]=1;
       }
      else if(pred[0]==1 && pred[1]==0 && pred[2]==0){
        // if(relabel[3])
          if(sav.equals("Female"))
           dist[1]=1;
        else
            dist[0]=1;
       }
      else if(pred[0]==0 && pred[1]==1 && pred[2]==1){
          //if(relabel[4])
          if(sav.equals("Female"))
           dist[1]=1;
        else
            dist[0]=1;
       }
      else if(pred[0]==0 && pred[1]==1 && pred[2]==0){
          //if(relabel[5])
          if(sav.equals("Female"))
           dist[1]=1;
        else
            dist[0]=1;
       }
      else if(pred[0]==0 && pred[1]==0 && pred[2]==1){
          //if(relabel[6])
          if(sav.equals("Female"))
           dist[1]=1;
        else
            dist[0]=1;
       }
      else if(pred[0]==0 && pred[1]==0 && pred[2]==0){
          if(relabel[7])
           dist[1]=1;
        else
            dist[0]=1;
       }
      return dist;
  }
 public void buildClassifier(Instances data) throws Exception {

    Instances newData = new Instances(data);
    newData.deleteWithMissingClass();

    for (int i = 0; i < m_Classifiers.length; i++) {
      getClassifier(i).buildClassifier(data);
    }
    }
/*    public double[] distributionForInstance(Instance instance) throws Exception {

    double [] dist=new double[2];
      int [] pred=new int[m_Classifiers.length];
      double[] probs;
       int pos=0,neg=0;
     for(int j=0;j<m_Classifiers.length;j++){
           probs= getClassifier(j).distributionForInstance(instance);
               if(probs[1]>probs[0]){
                   pred[j]=1;
                   pos++;
               }
               else{
                   pred[j]=0;
                   neg++;
               }
          }//end of inner for with J counter
      String sav=instance.toString(0);

      if(pos==m_Classifiers.length)
          dist[1]=1;
      else if (neg==m_Classifiers.length)
          dist[0]=1;
      else if (sav.equals("Female"))
          dist[1]=1;
      else
          dist[0]=1;

    return dist;
  }*/
      public double[] distributionForInstance(Instance instance) throws Exception {

    double [] dist=new double[2];
      //int [] pred=new int[m_Classifiers.length];
      double[] probs=new double[2];
      double []scores=new double[2];
       int pos=0,neg=0;
     for(int j=0;j<m_Classifiers.length;j++){
           probs= getClassifier(j).distributionForInstance(instance);
            if(probs[1]>probs[0]){
  //                 pred[j]=1;
                   pos++;
               }
               else{
   //                pred[j]=0;
                   neg++;
               }
           //System.out.println(probs[0]+"\t"+probs[1]);
               scores[0]+=probs[0];
               scores[1]+=probs[1];
          }//end of inner for with J counter
       scores[0]=scores[0]/m_Classifiers.length;
       scores[1]=scores[1]/m_Classifiers.length;

      
      String sav=instance.toString(0);
      if(pos==m_Classifiers.length){
      dist[1]=1;
      //dist[0]=0;

     }
     else if (neg==m_Classifiers.length){
         dist[0]=1;
         //dist[1]=0;
     //System.out.println("Sex:=\t"+sav+"\t scores:=\t"+scores[0]+"\t"+scores[1]);
      /*   if(scores[1]<=0.8 || sav.equals("Male"))
                dist[0]=1;
          else{
             //System.out.println(pos+"\t"+neg+"\t Sex:=\t"+sav+"\t scores:=\t"+scores[0]+"\t"+scores[1]);
             dist[1]=1;
          }*/
 }
 else if (sav.equals("Female"))
 {
          dist[1]=1;
          System.out.println("famle");
 }
 else          dist[1]=0;
 



      /*if(pos==m_Classifiers.length){
     dist[1]=1;
          
    }
 else if (neg==m_Classifiers.length){
     //System.out.println("Sex:=\t"+sav+"\t scores:=\t"+scores[0]+"\t"+scores[1]);
         if(scores[1]<=0.5 || sav.equals("Male"))
                dist[0]=1;
          else{
             System.out.println(pos+"\t"+neg+"\t Sex:=\t"+sav+"\t scores:=\t"+scores[0]+"\t"+scores[1]);
             dist[1]=1;
          }
 }
 else if (sav.equals("Female"))
          dist[1]=1;
 else          dist[0]=1;
   
*/
    return dist;
   //   return scores;
    }
//================================Two models concept===================
  /**
   * Buildclassifier selects a classifier from the set of classifiers
   * by minimising error on the training data.
   *
   * @param data the training data to be used for generating the
   * boosted classifier.
   * @exception Exception if the classifier could not be built successfully
   */
/* public void buildClassifier(Instances data) throws Exception {

    Instances newData = new Instances(data);
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
    System.out.println("Male:  "+newDataMale.numInstances()+ "   Female: "+newDataFemale.numInstances());
    getClassifier(0).buildClassifier(newDataMale);
        getClassifier(1).buildClassifier(newDataFemale);
        getClassifier(2).buildClassifier(data);
    //    newData.deleteWithMissingClass();

    //for (int i = 0; i < m_Classifiers.length; i++) {
      //getClassifier(i).buildClassifier(data);
    //}
  }


  public double[] distributionForInstance(Instance instance) throws Exception {
      double[] probs;
      String sav="null";
      if(Discrimination.getSAA())
          sav=Discrimination.saValue;
      else
            sav=instance.toString(0);

      if(sav.equals("Male"))
             probs = getClassifier(1).distributionForInstance(instance);
        else
            probs = getClassifier(0).distributionForInstance(instance);*/
  /* double[] probs = getClassifier(0).distributionForInstance(instance);
   double[] dist = getClassifier(1).distributionForInstance(instance);
   for (int j = 0; j < dist.length; j++)
	probs[j] += dist[j];
   dist = getClassifier(2).distributionForInstance(instance);
   for (int j = 0; j < dist.length; j++)
	probs[j] += dist[j];

    for (int j = 0; j < probs.length; j++) {
      probs[j] /= (double)m_Classifiers.length;
    }
    return probs;
  }*/

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
      System.out.println(Evaluation.evaluateModel(new Vote1(), argv));
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }
  }

}
