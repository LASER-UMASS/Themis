
//Author xxx  Feb,2010


package weka.nonDiscConstraints;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.classifiers.*;
import weka.classifiers.bayes.*;
import weka.filters.*;
import weka.core.*;

import java.util.Enumeration;

public class RWwithActualWtsFilter extends Filter  {
     /** All the counts for sensitive attribute value for positve and negative classes.  
    * saPos=favoured community with positve class   savPos=sav&+ */
  protected double favPos=0,favNeg=0,savPos=0,savNeg=0;
   double sp=0,sn=0,fp=0,fn=0,restCount=0;
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
   *
   * @return a description of the filter suitable for
   * displaying in the explorer/experimenter gui
   */
  public String globalInfo() {

    return "Produces a reweighed subsample of a dataset using sampling with new weights."
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
    Instances instances=getInputFormat();
    classifier.buildClassifier(instances);
    Enumeration enumInstsWithSA=null;
     if(Discrimination.getSAA()){
          weightCalculation(Discrimination.trainInstsWithsa);
          enumInstsWithSA=Discrimination.trainInstsWithsa.enumerateInstances();
     }
     else
          weightCalculation(instances);
   Enumeration enumInsts=instances.enumerateInstances();
   String saValue;
   Instance instanceWithSA;
   while(enumInsts.hasMoreElements()){
         Instance instance = (Instance) enumInsts.nextElement();
         // to get the value of SA, if SA is absent
         if(Discrimination.getSAA()){
                 instanceWithSA = (Instance) enumInstsWithSA.nextElement();
                 saValue=instanceWithSA.toString(sa_Index);
         }
         else
                 saValue=instance.toString(sa_Index);
         
    int classValue=(int)instance.classValue();
    if(saValue.equals(sa_Deprived) && classValue==dc ){
              instance.setWeight(savPos); 
              push(instance);
       }
    else if(saValue.equals(sa_Deprived) && classValue==ndc){
              instance.setWeight(savNeg); 
              push(instance);
    }
    else if(!saValue.equals(sa_Deprived) && classValue==dc){
              instance.setWeight(favPos); 
              push(instance);
    }
    else if(!saValue.equals(sa_Deprived) && classValue==ndc){
              instance.setWeight(favNeg); 
              push(instance);
    }
     else{
               instance.setWeight(1); 
               push(instance);
    }
  }
}  
  
  public void weightCalculation(Instances instances)throws Exception{
    spList=new double[instances.numInstances()][2];
    snList=new double[instances.numInstances()][2];
    fpList=new double[instances.numInstances()][2];
    fnList=new double[instances.numInstances()][2];
    restList=new double[instances.numInstances()][2];
    Enumeration enumInsts = instances.enumerateInstances();
    int classValue=0;
    sp=sn=fp=fn=restCount=0;
    String saValue;
    while (enumInsts.hasMoreElements()) {
                    Instance instance = (Instance) enumInsts.nextElement();
                    saValue=instance.toString(sa_Index);
                    classValue=(int)instance.classValue();
                    if(saValue.equals(sa_Deprived) && classValue==dc ){
                           sp++;
                    }                  
                    else if(saValue.equals(sa_Deprived) && classValue==ndc){
                            sn++;
                    }
                    else if(saValue.equals(sa_Favored) && classValue==dc){
                           fp++;
                    }
                    else if(saValue.equals(sa_Favored) && classValue==ndc){
                           fn++;
                    }
                    else{
                         restCount++;
                    }
	  //instIndex++;
    }
        double total=instances.numInstances();
        //System.out.println("SavPos=: "+savPos+"  SavNeg =: "+savNeg+"   saPos =: "+favPos+"  saNeg =: "+favNeg);
        savPos=((sp+sn)/total)*((sp+fp)/sp);
        savNeg=((sp+sn)/total)*((sn+fn)/sn);
        favPos=((fp+fn)/total)*((sp+fp)/fp);
        favNeg=((fp+fn)/total)*((sn+fn)/fn);
        System.out.println("Weights: SavPos=: "+savPos+"  SavNeg =: "+savNeg+"   saPos =: "+favPos+"  saNeg =: "+favNeg);
        // System.out.println("SavPos=: "+savPos*sp+"  SavNeg =: "+savNeg*sn+"   saPos =: "+saPos*fp+"  saNeg =: "+saNeg*fn);
        System.out.println("SP=: "+sp+"  SN =: "+sn+"   Fp =: "+fp+"  fn =: "+fn);
       // System.out.println("SP=: "+(sp*savPos+sn*savNeg+fp*favPos+fn*favNeg));
    // System.out.println("     Total number of instances in traing data=:  f "+instances.numInstances());
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
 	Filter.batchFilterFile(new RWwithActualWtsFilter(), argv);
      } else {
	Filter.filterFile(new PrefrentialSamplingFilter(), argv);
      }
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
    }
}

}
