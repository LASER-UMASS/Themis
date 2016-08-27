////Author xxxx  Feb,2010
package weka.nonDiscConstraints;



import weka.filters.*;
import weka.core.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Random;
import java.util.Enumeration;
import java.util.Vector;


public class UniformSamplingFilter  extends Filter  {
     /** All the counts for sensitive attribute value for positve and negative classes.  
    * saPos=favoured community with positve class   savPos=sav&+ */
  protected double saPos=0,saNeg=0,savPos=0,savNeg=0;
   int sp=0,sn=0,fp=0,fn=0,restCount=0;
   int []spList; // list of instances with SA=dep   class=dc
   int []snList;// list of instances with SA=dep   class=ndc
   int []fpList;// list of instances with SA=fav   class=dc
   int []fnList;   // list of instances with SA=fav   class=ndc
   int []restList;   // list of instances with SA other than Dep and fav  i.e. when SA has more than two values 
  
  //intitializatin of sensitive varialbles with the corresponding values of The Discrimination class
  int sa_Index=Discrimination.sa_Index; 
  int dc=Discrimination.dc; 
  int ndc=Discrimination.ndc;
  public String sa=Discrimination.sa,sa_Deprived=Discrimination.sa_Deprived
          ,sa_Favored=Discrimination.sa_Favored;    //sa= sensitive attribute   sa_deprived=deprived community    
                                                    //sa_favored=favord community of sa

 
  
  
  /**
   * Returns a string describing this filter
   *
   * @return a description of the filter suitable for
   * displaying in the explorer/experimenter gui
   */
  public String globalInfo() {

    return "Produces a reweighed subsample of a dataset using sampling with replacement."
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
   * method to initilize the disc parameters explicitly
   */
 public void initParameters(){
     sa_Index=Discrimination.getSaIndex(); 
     dc=Discrimination.getDC(); 
     ndc=Discrimination.getNDC();
     sa_Deprived=Discrimination.getSaDep();
     sa_Favored=Discrimination.getSaFav(); 
                                                   
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

 
  public boolean batchFinished() {

    if (getInputFormat() == null) {
      throw new IllegalStateException("No input instance format defined");
    }

    if (!m_FirstBatchDone) {
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
  private void createSubsample() {

    int i;

     Random generator = new Random();
    Instances instances=getInputFormat();
     if(Discrimination.getSAA())
          weightCalculation(Discrimination.trainInstsWithsa);
     else
          weightCalculation(getInputFormat());
  
    for( i=0;i<savPos;i++)
        if(i<sp)
            push(instances.instance(spList[i]));
       else
            push(instances.instance(spList[generator.nextInt(sp)]));
 
    for( i=0;i<savNeg;i++)
        if(i<sn)
             push(instances.instance(snList[i]));
        else
            push(instances.instance(snList[generator.nextInt(sn)]));
           
    for(i=0;i<saPos-1;i++)//saPos means favored community with positve or desired class
        if(i<fp)
            push(instances.instance(fpList[i]));
        else
            push(instances.instance(fpList[generator.nextInt(fp)]));
    for( i=0;i<saNeg-1;i++)       //saNeg means favored community with negative or non desired class
        if(i<fn)
            push(instances.instance(fnList[i]));
        else
            push(instances.instance(fnList[generator.nextInt(fn)]));
    
     for( i=0;i<restCount-1;i++)       
        push(instances.instance(restList[generator.nextInt(restCount)]));

   
  }
  
  
  public void weightCalculation(Instances instances){
    spList=new int[instances.numInstances()];
    snList=new int[instances.numInstances()];
    fpList=new int[instances.numInstances()];
    fnList=new int[instances.numInstances()];
    restList=new int[instances.numInstances()];
   
       Enumeration enumInsts = instances.enumerateInstances();
    int classValue=0;
    int instIndex=0;//sp=sav&positive fn=favored&negative
    sp=sn=fp=fn=restCount=0;
    String saValue;
    
    while (enumInsts.hasMoreElements()) {
      Instance instance = (Instance) enumInsts.nextElement();
    
                    saValue=instance.toString(sa_Index);
                    classValue=(int)instance.classValue();
                                     
                    //System.out.println("class"+classValue);
                    if(saValue.equals(sa_Deprived) && classValue==dc )
                            spList[sp++]=instIndex++;
                                          
                    else if(saValue.equals(sa_Deprived) && classValue==ndc)
                            snList[sn++]=instIndex++;
                    else if(!saValue.equals(sa_Deprived) && classValue==dc)
                            fpList[fp++]=instIndex++;
                    else if(!saValue.equals(sa_Deprived) && classValue==ndc)
                            fnList[fn++]=instIndex++;
                    //if(!saValue.equals(sa_Favored)&& !saValue.equals(sa_Deprived))
                    else
                         restList[restCount++]=instIndex++;
                
    }
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
 	Filter.batchFilterFile(new UniformSamplingFilter(), argv);
      } else {
	Filter.filterFile(new PrefrentialSamplingFilter(), argv);
      }
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
    }
  }

}
















