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

public class PrefrentialSamplingFilter extends Filter  {
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
 /**
   * Returns a string describing this filter
   * @return a description of the filter suitable for
   * displaying in the explorer/experimenter gui
   */
  public String globalInfo() {

    return "Produces a reweighed subsample of a dataset using preferential sampling with replacement."
      + "The original dataset must "
      + "fit entirely in memory. The number of instances in the generated "
      + "dataset may be specified. The dataset must have a nominal class "
      + "attribute. If not, use the unsupervised version. The filter can be "
      + "made to maintain the class distribution in the subsample, or to bias "
      + "the class distribution toward a uniform distribution. When used in batch "
      + "mode (i.e. in the FilteredClassifier), subsequent batches are NOTE resampled.";
  }

 
 /**
   * Sets the format of the input instances.
   *
   * @param instanceInfo an Instances object containing the input 
   * instance structure (any instances contained in the object are 
   * ignored - only the structure is required).
   * @return true if the outputFormat may be collected immediately
   * @exception Exception if the input format can't be set 
   * successfully
   */
  public boolean setInputFormat(Instances instanceInfo) 
       throws Exception {

    if (instanceInfo.classIndex() < 0 || !instanceInfo.classAttribute().isNominal()) {
      throw new IllegalArgumentException("Supervised resample requires nominal class");
    }

    super.setInputFormat(instanceInfo);
    setOutputFormat(instanceInfo);
    return true;
  }

  /**
   * Input an instance for filtering. Filter requires all
   * training instances be read before producing output.
   *
   * @param instance the input instance
   * @return true if the filtered instance may now be
   * collected with output().
   * @exception IllegalStateException if no input structure has been defined
   */
  public boolean input(Instance instance) {

    if (getInputFormat() == null) {
      throw new IllegalStateException("No input instance format defined");
    }
    if (m_NewBatch) {
      resetQueue();
      m_NewBatch = false;
    }
    if (m_FirstBatchDone) {
      push(instance);
      return true;
    } else {
      bufferInput(instance);
      return false;
    }
  }

 public void initParameters(){
     sa_Index=Discrimination.getSaIndex(); 
     dc=Discrimination.getDC(); 
     ndc=Discrimination.getNDC();
     sa_Deprived=Discrimination.getSaDep();
     sa_Favored=Discrimination.getSaFav(); 
                                                   
 }
  public boolean batchFinished() {

    if (getInputFormat() == null) {
      throw new IllegalStateException("No input instance format defined");
}
 if (!m_FirstBatchDone) {
      // Do the subsample, and clear the input instances.
        initParameters();
        System.out.println("DC\t"+dc+"saDep\t"+sa_Deprived);
            try {
                createSubsample();
            } catch (Exception ex) {
                Logger.getLogger(PrefrentialSamplingFilter.class.getName()).log(Level.SEVERE, null, ex);
            }
}
    flushInput();
    m_NewBatch = true;
    m_FirstBatchDone = true;
    return (numPendingOutput() != 0);
}
  /**
   * Creates a subsample of the current set of input instances. The output
   * instances are pushed onto the output queue for collection.
   */
  private void createSubsample()throws Exception {

    int i,total=0,top=0;

     Random generator = new Random();
    Instances instances=getInputFormat();
    
     if(Discrimination.getSAA()){
         classifier.buildClassifier(Discrimination.trainInstsWithsa);
         weightCalculation(Discrimination.trainInstsWithsa);
     }
     else{
         classifier.buildClassifier(instances);
          weightCalculation(instances);
          }
  
    for( i=0;i<savPos;i++)
        if(i<sp)
            push(instances.instance(Integer.parseInt(Utils.doubleToString(spList[i][0],1))));//Utils.doubleToString(spList[i][0],1)
       else{
        if(top==sp-1) top=0;
            push(instances.instance((int)spList[top++][0]));
            
       }
       
    
 top=0;
    for( i=0;i<savNeg;i++)
        if(i<sn)
             push(instances.instance((int)snList[i][0]));
        else
            push(instances.instance((int)snList[top++][0]));
     top=0;       
    for(i=0;i<saPos-1;i++)//saPos means favored community with positve or desired class
        if(i<fp)
            push(instances.instance((int)fpList[i][0]));
        else
           push(instances.instance((int)fpList[top++][0]));
      top=0;
    for( i=0;i<saNeg-1;i++)       //saNeg means favored community with negative or non desired class
        if(i<fn)
            push(instances.instance((int)fnList[i][0]));
        else{
        if(top==sp-1) top=0;
            push(instances.instance((int)fnList[top++][0]));
        }
     top=0;
     for( i=0;i<restCount-1;i++)       
        push(instances.instance((int)restList[generator.nextInt(restCount)][0]));
 
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
    System.out.println("SavPos=: "+savPos+"  SavNeg =: "+savNeg+"   saPos =: "+saPos+"  saNeg =: "+saNeg);
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
public static void main(String [] argv) {

    try {
      if (Utils.getFlag('b', argv)) {
 	Filter.batchFilterFile(new PrefrentialSamplingFilter(), argv);
      } else {
	Filter.filterFile(new PrefrentialSamplingFilter(), argv);
      }
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
    }
}

}








