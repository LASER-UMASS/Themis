/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 *
 *
 * Author xxxx  Feb,2010
 */

package weka.nonDiscConstraints;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.classifiers.*;
import weka.classifiers.bayes.*;
import weka.filters.*;
import weka.core.*;

import java.util.Random;
import java.util.Enumeration;

public class LocalPreferentialSampling{
     /** All the counts for sensitive attribute value for positve and negative classes.
    * saPos=favoured community with positve class   savPos=sav&+ */
  protected double saPos=0,saNeg=0,savPos=0,savNeg=0;
   int sp=0,sn=0,fp=0,fn=0,restCount=0;
   double [][]spList; // list of instances with SA=dep   class=dc
   double [][]snList;// list of instances with SA=dep   class=ndc
   double [][]fpList;// list of instances with SA=fav   class=dc
   double [][]fnList;   // list of instances with SA=fav   class=ndc
   double [][]restList;   // list of instances with SA other than Dep and fav  i.e. when SA has more than two values

  //intitializatin of sensitive varialbles with the corresponding values of The Discrimination class
  int sa_Index=Discrimination.getSaIndex(),dc=Discrimination.getDC(),ndc=Discrimination.getNDC();
  public String sa_Deprived=Discrimination.getSaDep()
          ,sa_Favored=Discrimination.getSaFav();    //sa= sensitive attribute   sa_deprived=deprived community
                                                    //sa_favored=favord community of sa

 public static  Classifier classifier=new NaiveBayesSimple();//NaiveBayesSimple







 public void initParameters(){
     sa_Index=Discrimination.getSaIndex();
     dc=Discrimination.getDC();
     ndc=Discrimination.getNDC();
     sa_Deprived=Discrimination.getSaDep();
     sa_Favored=Discrimination.getSaFav();

 }

  /**
   * Creates a subsample of the current set of input instances. The output
   * instances are pushed onto the output queue for collection.
   */
   public Instances createSubsample(Instances instances,double femNeg,double femPos, double maleNeg,double malePos)throws Exception {
      initParameters();
     // if(instances.numInstances()<50)
      //    return instances;

    Instances newInsts=new Instances(instances);
    newInsts.delete();
    int i,top=0;

     Random generator = new Random();

     if(Discrimination.getSAA()){
         classifier.buildClassifier(Discrimination.trainInstsWithsa);
         weightCalculation(Discrimination.trainInstsWithsa);
     }
     else{
         classifier.buildClassifier(instances);
          weightCalculation(instances);
      }

   for( i=0;i<femPos;i++)
           if(sp!=0){
        if(i<sp)
            newInsts.add(instances.instance(Integer.parseInt(Utils.doubleToString(spList[i][0],1))));//Utils.doubleToString(spList[i][0],1)
        else if(sp==1)
            newInsts.add(instances.instance((int)spList[0][0]));
        else{
            if(top==sp-1) top=0;
                newInsts.add(instances.instance((int)spList[top++][0]));
       }
}
     System.out.println("Hello1");
 top=0;
    for( i=0;i<femNeg;i++)
       if(sn!=0)
        //System.out.println("Hello");
        if(i<sn)
             newInsts.add(instances.instance((int)snList[i][0]));
        else
            newInsts.add(instances.instance((int)snList[top++][0]));
     top=0;

    for(i=0;i<malePos-1;i++)//saPos means favored community with positve or desired class
      if(fp!=0)
        if(i<fp)
            newInsts.add(instances.instance((int)fpList[i][0]));
        else
           newInsts.add(instances.instance((int)fpList[top++][0]));
      top=0;
    for( i=0;i<maleNeg-1;i++)       //saNeg means favored community with negative or non desired class
       if(fn!=0)
        if(i<fn)
            newInsts.add(instances.instance((int)fnList[i][0]));
        else{
        if(top==fn-1) top=0;
            newInsts.add(instances.instance((int)fnList[top++][0]));
        }
  top=0;
     for( i=0;i<restCount-1;i++)
        newInsts.add(instances.instance((int)restList[generator.nextInt(restCount)][0]));

        
     return newInsts;

  }

  public Instances createSubsample(Instances instances)throws Exception {
      initParameters();
      //if(instances.numInstances()<243)
      //    return instances;
      double d=Discrimination.discCalculation(instances);
     // System.out.println("discrimination\t"+d);
      if(d<0){
          String t=sa_Deprived;
          sa_Deprived=sa_Favored;
          sa_Favored=t;
      }
    
    Instances newInsts=new Instances(instances);
    newInsts.delete();
    int i,top=0;

     Random generator = new Random();

     if(Discrimination.getSAA()){
         classifier.buildClassifier(Discrimination.trainInstsWithsa);
         weightCalculation(Discrimination.trainInstsWithsa);
     }
     else{
         classifier.buildClassifier(instances);
          weightCalculation(instances);
      }

   for( i=0;i<savPos;i++)
           if(sp!=0){
        if(i<sp)
            newInsts.add(instances.instance(Integer.parseInt(Utils.doubleToString(spList[i][0],1))));//Utils.doubleToString(spList[i][0],1)
        else if(sp==1)
            newInsts.add(instances.instance((int)spList[0][0]));
        else{
            if(top==sp-1) top=0;
                newInsts.add(instances.instance((int)spList[top++][0]));
       }
}
 top=0;
    for( i=0;i<savNeg;i++)
        if(sn!=0)
        //System.out.println("Hello");
        if(i<sn)
             newInsts.add(instances.instance((int)snList[i][0]));
        else
            newInsts.add(instances.instance((int)snList[top++][0]));
     top=0;

    for(i=0;i<saPos-1;i++)//saPos means favored community with positve or desired class
       if(fp!=0)
        if(i<fp)
            newInsts.add(instances.instance((int)fpList[i][0]));
        else
           newInsts.add(instances.instance((int)fpList[top++][0]));
      top=0;
    for( i=0;i<saNeg-1;i++)       //saNeg means favored community with negative or non desired class
        if(fn!=0)
        if(i<fn)
            newInsts.add(instances.instance((int)fnList[i][0]));
        else{
        if(top==sp-1) top=0;
            newInsts.add(instances.instance((int)fnList[top++][0]));
        }
  top=0;
     for( i=0;i<restCount-1;i++)
        newInsts.add(instances.instance((int)restList[generator.nextInt(restCount)][0]));

        System.out.println("Hello1");
     return newInsts;

  }


  // weightCalculation method in this class calculates the expected sample sizes for
  // all class and SA values combinations
  public void weightCalculation(Instances instances)throws Exception{
    spList=new double[instances.numInstances()][2];
    snList=new double[instances.numInstances()][2];
    fpList=new double[instances.numInstances()][2];
    fnList=new double[instances.numInstances()][2];
    restList=new double[instances.numInstances()][2];
    double []prob=new double[instances.numClasses()]; //array to store probabilty distribution
    Enumeration enumInsts = instances.enumerateInstances();
    int classValue=0,instIndex=0;
    sp=sn=fp=fn=restCount=0;
    String saValue;
    while (enumInsts.hasMoreElements()) {
                    Instance instance = (Instance) enumInsts.nextElement();
                    saValue=instance.toString(sa_Index);
                    classValue=(int)instance.classValue();
                    if(saValue.equals(sa_Deprived) && classValue==dc ){
                            spList[sp][0]=instIndex;
                            prob=classifier.distributionForInstance(instance);
                            spList[sp++][1]=prob[dc]*100;
                    }
                    else if(saValue.equals(sa_Deprived) && classValue==ndc){
                            snList[sn][0]=instIndex;
                            prob=classifier.distributionForInstance(instance);
                            snList[sn++][1]=prob[dc]*100;
                    }
                    else if(!saValue.equals(sa_Deprived) && classValue==dc){
                            fpList[fp][0]=instIndex;
                            prob=classifier.distributionForInstance(instance);
                            fpList[fp++][1]=prob[dc]*100;
                    }
                    else if (!saValue.equals(sa_Deprived) && classValue==ndc){
                            fnList[fn][0]=instIndex;
                            prob=classifier.distributionForInstance(instance);
                            fnList[fn++][1]=prob[dc]*100;
                    }
                    else{
                            restList[restCount][0]=instIndex;
                            prob=classifier.distributionForInstance(instance);
                            restList[restCount++][1]=prob[dc]*100;
                    }
	  instIndex++;
    }
     spList=Massaging.sorting(spList,sp,2);
     snList=Massaging.sorting(snList,sn,2);
     fpList=Massaging.sorting(fpList,fp,1);
     fnList=Massaging.sorting(fnList,fn,1);
     restList=Massaging.sorting(restList,restCount,1);

    double total=instances.numInstances();
    savPos=((sp+sn)/total)*((sp+fp));
    savNeg=((sp+sn)/total)*((sn+fn));
    saPos=((fp+fn)/total)*((sp+fp));
    saNeg=((fp+fn)/total)*((sn+fn));
    System.out.println(" Ratios: SavPos=: "+savPos+"  SavNeg =: "+savNeg+"   saPos =: "+saPos+"  saNeg =: "+saNeg);
    // System.out.println("SavPos=: "+savPos*sp+"  SavNeg =: "+savNeg*sn+"   saPos =: "+saPos*fp+"  saNeg =: "+saNeg*fn);
    System.out.println("SavPos=: "+sp+"  SavNeg =: "+sn+"   saPos =: "+fp+"  saNeg =: "+fn);
    // System.out.println("     Total number of instances in traing data=:   "+instances.numInstances());
}
  /**
   * Main method for testing this class.
   *
   * @param argv should contain arguments to the filter:
   * use -h for help
   */


}








