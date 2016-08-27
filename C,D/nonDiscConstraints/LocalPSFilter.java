
//Author Faisal Kamiran  Feb,2010


package weka.nonDiscConstraints;
import weka.filters.*;
import weka.classifiers.SingleClassifierEnhancer.*;
import weka.core.UnsupportedClassTypeException;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import weka.classifiers.*;
import java.util.Enumeration;
import java.util.*;
import weka.core.*;
import weka.classifiers.*;
import weka.classifiers.bayes.*;
/**
 *
 * @author fkamiran
 */
public class LocalPSFilter extends Filter{  //implements OptionHandler
  /**
   * Returns a string describing this filter
   * @return a description of the filter suitable for
   * displaying in the explorer/experimenter gui
   */
   /** holds the classifier to use for error estimates */
  private Classifier m_Ranker = new NaiveBayesSimple();
  private double   m_epsilon=0;
  public String globalInfo() {

    return "Produces a dataset after the application of Local massaging. The dataset "
     + "must fit entirely in memory. ";
}

  /**
   * Sets the format of the input instances.
   *
   * @param instanceInfo an Instances object containing the input
   * instance structure (any instances contained in the object are
   * ignored - only the structure is required).
   * @return true if the outputFormat may be collected immediately
   * @exception UnassignedClassException if no class attribute has been set.
   * @exception UnsupportedClassTypeException if the class attribute
   * is not nominal.
   */
  public boolean setInputFormat(Instances instanceInfo)
       throws Exception {

    super.setInputFormat(instanceInfo);
    if (instanceInfo.classAttribute().isNominal() == false) {
      throw new UnsupportedClassTypeException("The class attribute must be nominal.");
    }
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
//System.out.println(" Input method : MassagingFilter class");
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
   /**
   * Signify that this batch of input to the filter is finished.
   * If the filter requires all instances prior to filtering,
   * output() may now be called to retrieve the filtered instances.
   *
   * @return true if there are instances pending output
   * @exception IllegalStateException if no input structure has been defined
   */
  public boolean batchFinished()throws Exception {

    if (getInputFormat() == null) {
      throw new IllegalStateException("No input instance format defined");
    }
    if (!m_FirstBatchDone) {
        applyMassaging();
    }
    flushInput();
    m_NewBatch = true;
    m_FirstBatchDone = true;
    return (numPendingOutput() != 0);
  }


public  void applyMassaging()throws Exception {
    double f=0,m=0;
    int caIndex=Discrimination.getCaIndex();
    int caValues=Discrimination.getCaValues();
    double [][][] biasStat=new double[caValues][2][2];

         //Massaging cnd=new Massaging();
        LocalPreferentialSampling ps=new LocalPreferentialSampling();
           Instances insts=getInputFormat();
           int n=insts.numInstances();
           //System.out.println("Orig=:\t"+Discrimination.discCalculation(insts));
           Instances instsC []=new Instances[caValues];
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

    Instances massagedData=new Instances(insts);
    massagedData.delete();
double mp=0,mn=0,fp=0,fn=0;
           for(int i=0; i<instsC.length;i++){
               double fLocal=biasStat[i][0][0]+biasStat[i][0][1];
               double mLocal=biasStat[i][1][0]+biasStat[i][1][1];
               /*
                fn=fLocal*(((f/n)*(biasStat[i][0][0]/fLocal))+((m/n)*(biasStat[i][1][0]/mLocal)));
                mn=mLocal*(((f/n)*(biasStat[i][0][0]/fLocal))+((m/n)*(biasStat[i][1][0]/mLocal)));
                fp=fLocal*(((f/n)*(biasStat[i][0][1]/fLocal))+((m/n)*(biasStat[i][1][1]/mLocal)));
                mp=mLocal*(((f/n)*(biasStat[i][0][1]/fLocal))+((m/n)*(biasStat[i][1][1]/mLocal)));
                */
               if(fLocal==0)fLocal=1;  if(mLocal==0)mLocal=1;
               fn=fLocal*((0.5*(biasStat[i][0][0]/fLocal))+(0.5*(biasStat[i][1][0]/mLocal)));
                mn=mLocal*((0.5*(biasStat[i][0][0]/fLocal))+(0.5*(biasStat[i][1][0]/mLocal)));
                fp=fLocal*((0.5*(biasStat[i][0][1]/fLocal))+(0.5*(biasStat[i][1][1]/mLocal)));
                mp=mLocal*((0.5*(biasStat[i][0][1]/fLocal))+(0.5*(biasStat[i][1][1]/mLocal)));
                System.out.println(i+":biased\t fn=: \t"+biasStat[i][0][0]+"\t fp=: \t"+biasStat[i][0][1]+"\t mn=: \t"+biasStat[i][1][0]+"\t mp=: \t"+biasStat[i][1][1]);
                System.out.println(i+":Unbiased\t fn=: \t"+fn+"\t fp=: \t"+fp+"\t mn=: \t"+mn+"\t mp=: \t"+mp);
               // Instances temp=cnd.cndApplication(instsC[i],(fp-biasStat[i][0][1]),(biasStat[i][1][1]-mp)); // For Local Massagin
               Instances temp=ps.createSubsample(instsC[i],fn,fp,mn,mp); // For Local Massagin
               for(int j=0;j<temp.numInstances();j++)
                   massagedData.add(temp.instance(j));
               temp.delete();
          }
           // Instances massagedData=cnd.cndApplication(insts);
            Enumeration enumInsts = massagedData.enumerateInstances();
            while (enumInsts.hasMoreElements()) {
                 Instance instance = (Instance) enumInsts.nextElement();
                 push(instance);
            }

      /*
            System.out.println("f="+f+"  m= "+m);
           f=m=0;
           for(int i=0;i<caValues;i++){
               System.out.println(i+"\tf+="+biasStat[i][0][1]+"\tM+="+biasStat[i][1][1]+"\t total"+(biasStat[i][0][0]+biasStat[i][0][1]+biasStat[i][1][0]+biasStat[i][1][1]));
               f+=biasStat[i][0][0]+biasStat[i][0][1];
               m+=biasStat[i][1][0]+biasStat[i][1][1];

                }
           System.out.println("f="+f+"  m= "+m);*/
}
/*public  void applyMassaging()throws Exception {
          //Massaging cnd=new Massaging();
        LocalPreferentialSampling ps=new LocalPreferentialSampling();
           Instances insts=getInputFormat();
           //System.out.println("Orig=:\t"+Discrimination.discCalculation(insts));
           Instances instsC []=new Instances[Discrimination.getCaValues()];
           for(int i=0;i<instsC.length;i++){
               instsC[i]=new Instances(insts);
               instsC[i].delete();
              // System.out.println(i+":\t"+instsC[i].numInstances()+"\t ints=:\t"+insts.numInstances());
    }
        for(int i=0;i<insts.numInstances();i++){
            instsC[(int)insts.instance(i).value(Discrimination.getCaIndex())].add(insts.instance(i));
        }
    Instances massagedData=new Instances(insts);
     massagedData.delete();

           for(int i=0; i<instsC.length;i++){
               //Instances temp=cnd.cndApplication(instsC[i]); // For Local Massagin
               Instances temp=ps.createSubsample(instsC[i]); // For Local Massagin
               for(int j=0;j<temp.numInstances();j++)
                   massagedData.add(temp.instance(j));
               temp.delete();
          }
           // Instances massagedData=cnd.cndApplication(insts);
            Enumeration enumInsts = massagedData.enumerateInstances();
            while (enumInsts.hasMoreElements()) {
                 Instance instance = (Instance) enumInsts.nextElement();
                 push(instance);
            }
}
*/
   /**
   * Returns an enumeration describing the available options. <p>
   *
   * -B <classifier> <br>
   * Class name of the classifier to use as ranker of Massaging filter.
   * Place any classifier options last on the command line following a
   * "--". Eg  -B weka.classifiers.bayes.NaiveBayes ... -- -K <p>
   *
   * @return an enumeration of all the available options.
   **/
  public Enumeration listOptions () {
    Vector newVector = new Vector(2);
    newVector.addElement(new Option("\tclass name of the classifier to use as"
				    + "\n\tranker of Massaging filter. Place any"
				    + "\n\tclassifier options LAST on the"
				    + "\n\tcommand line following a \"--\"."
				    + "\n\teg. -C weka.classifiers.bayes.NaiveBayes ... "
				    + "-- -K", "B", 1, "-B <classifier>"));

 newVector.addElement(new Option("\tValue of Epsilon: Minimum "
				    +" Threshold to reduce dependency."
				    ,"E",0,"-E"));

    if ((m_Ranker != null) &&
	(m_Ranker instanceof OptionHandler)) {
      newVector.addElement(new Option("", "", 0, "\nOptions specific to "
				      + "scheme "
				      + m_Ranker.getClass().getName()
				      + ":"));
      Enumeration enu = ((OptionHandler)m_Ranker).listOptions();

      while (enu.hasMoreElements()) {
        newVector.addElement(enu.nextElement());
      }
    }

    return  newVector.elements();
  }

  /**
   * Parses a given list of options.
   *
   * Valid options are:<p>
   *
   * -C <classifier> <br>
   * Class name of classifier to use for accuracy estimation.
   * Place any classifier options last on the command line following a
   * "--". Eg  -B weka.classifiers.bayes.NaiveBayes ... -- -K <p>
   *

   * @param options the list of options as an array of strings
   * @exception Exception if an option is not supported
   *
   **/
  public void setOptions (String[] options)
    throws Exception {
    String optionString;
    resetOptions();

    optionString = Utils.getOption('B', options);

    if (optionString.length() == 0) {
      throw new Exception("A classifier must be specified with -B option");
    }

    setRanker(Classifier.forName(optionString,
				     Utils.partitionOptions(options)));
optionString = Utils.getOption('E',options);
    if (optionString.length() != 0) {
      setepsilon(Double.parseDouble(optionString));
    }

  }

    /**
   * Returns the tip text for this property
   * @return tip text for this property suitable for
   * displaying in the explorer/experimenter gui
   */
  public String rankerTipText() {
    return "Classifier to use ranker for Massaging";
  }

  /**
   * Set the classifier to use for accuracy estimation
   *
   * @param newClassifier the Classifier to use.
   */

  public void setRanker (Classifier newClassifier) {
    m_Ranker = newClassifier;
    Massaging.setRanker(m_Ranker);
  }


  /**
   * Get the classifier used as the base learner.
   *
   * @return the classifier used as the classifier
   */
  public Classifier getRanker () {
    return  m_Ranker;
  }
   /**
   * Gets the current settings of ClassifierSubsetEval
   *
   * @return an array of strings suitable for passing to setOptions()
   */
  public String[] getOptions () {
    String[] classifierOptions = new String[0];

    if ((m_Ranker != null) &&
	(m_Ranker instanceof OptionHandler)) {
      classifierOptions = ((OptionHandler)m_Ranker).getOptions();
    }

    String[] options = new String[5 + classifierOptions.length];
    int current = 0;

    if (getRanker() != null) {
      options[current++] = "-B";
      options[current++] = getRanker().getClass().getName();
    }
     options[current++] = "" + m_epsilon;

    options[current++] = "--";
    System.arraycopy(classifierOptions, 0, options, current,
		     classifierOptions.length);
    current += classifierOptions.length;
        while (current < options.length) {
      options[current++] = "";
    }

    return  options;
  }
/**
   * Returns the tip text for this property
   * @return tip text for this property suitable for
   * displaying in the explorer/experimenter gui
   */
  // change
  public String epsilonTipText() {
    return "It specifies the minimum threshold upto which "+
			      "dependendency is redueced";

  }

// change
  public double getepsilon() {

    return m_epsilon;
  }
// change
  public void setepsilon(double a) {

      m_epsilon = a;
      Massaging.epsilon=a;

    }

  /**
   * reset to defaults
   */
  protected void resetOptions () {
      m_Ranker = new NaiveBayesSimple();

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
 	Filter.batchFilterFile(new LocalPSFilter(), argv);
      } else {
	Filter.filterFile(new LocalPSFilter(), argv);
      }
    } catch (Exception ex) {
		ex.printStackTrace();
      System.out.println(ex.getMessage());
    }
  }
}
