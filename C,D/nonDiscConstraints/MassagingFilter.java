
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
public class MassagingFilter extends Filter{  //implements OptionHandler
  /**
   * Returns a string describing this filter
   * @return a description of the filter suitable for
   * displaying in the explorer/experimenter gui
   */
   /** holds the classifier to use for error estimates */
  private Classifier m_Ranker = new NaiveBayesSimple();
  private double   m_epsilon=0;
  public String globalInfo() {

    return "Produces a dataset after the application of massaging. The dataset "
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


  
  private void applyMassaging()throws Exception {
           Massaging cnd=new Massaging();
           Instances insts=getInputFormat();
          Instances massagedData=cnd.cndApplication(insts);
          //BalancedSampling bal=new BalancedSampling();
          //Instances massagedData=bal.makeBalance(getInputFormat());
          //massagedData=cnd.cndApplication(insts);
          //massagedData=cnd.cndApplication1(insts, massagedData);
          // massagedData=cnd.cndApplication(insts, massagedData);
   
        Enumeration enumInsts = massagedData.enumerateInstances();
        while (enumInsts.hasMoreElements()) {
                 Instance instance = (Instance) enumInsts.nextElement();
                 push(instance);
            }
}

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
 	Filter.batchFilterFile(new MassagingFilter(), argv);
      } else {
	Filter.filterFile(new MassagingFilter(), argv);
      }
    } catch (Exception ex) {
		ex.printStackTrace();
      System.out.println(ex.getMessage());
    }
  }
}
