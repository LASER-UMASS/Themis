//Author xxxxx  Feb,2010



package weka.nonDiscConstraints;
//import weka.classifiers.discrimination.*;
import weka.filters.*;
import weka.core.UnsupportedClassTypeException;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import weka.nonDiscConstraints.Massaging;
import weka.classifiers.*;


import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Range;
import weka.core.SparseInstance;

import weka.filters.Filter;
import weka.filters.StreamableFilter;
import weka.filters.UnsupervisedFilter;

import java.util.Enumeration;
import java.util.Vector;


/**
 *
 * @author fkamiran
 */
public class NoFilter extends Filter {
  /**
   * Returns a string describing this filter
   *
   * @return a description of the filter suitable for
   * displaying in the explorer/experimenter gui
   */
   
  public String globalInfo() {

    return "Produces a dataset after the application of massaging. The dataset "
     + "must fit entirely in memory. ";
}

    /**
   * Returns an enumeration describing the available options.
   *
   * @return an enumeration of all the available options.
   */
  public Enumeration listOptions() {

    Vector newVector = new Vector(2);

    newVector.addElement(new Option(
              "\tSpecify list of columns to delete. First and last are valid\n"
	      +"\tindexes. (default none)",
              "R", 1, "-R <index1,index2-index4,...>"));
    newVector.addElement(new Option(
	      "\tInvert matching sense (i.e. only keep specified columns)",
              "V", 0, "-V"));

    return newVector.elements();
  }

  /**
   * Parses a given list of options controlling the behaviour of this object.
   * Valid options are:<p>
   *
   * -R index1,index2-index4,...<br>
   * Specify list of columns to delete. First and last are valid indexes.
   * (default none)<p>
   *
   * -V<br>
   * Invert matching sense (i.e. only keep specified columns)<p>
   *
   * @param options the list of options as an array of strings
   * @exception Exception if an option is not supported
   */
  public void setOptions(String[] options) throws Exception {

    
    setFlag(Utils.getFlag('f', options));
    
    if (getInputFormat() != null) {
      setInputFormat(getInputFormat());
    }
  }
  
  public void setFlag(boolean f){
    System.out.println("Hurra!" + f);
  }
  

  /**
   * Gets the current settings of the filter.
   *
   * @return an array of strings suitable for passing to setOptions
   */
  public String [] getOptions() {

    String [] options = new String [3];
    int current = 0;

    if (getFlag()) {
      options[current++] = "-V";
    }
    /*if (!getAttributeIndices().equals("")) {
      options[current++] = "-R"; options[current++] = getAttributeIndices();
    }*/

    while (current < options.length) {
      options[current++] = "";
    }
    return options;
  }
  
  public boolean getFlag(){
    return Discrimination.getSAA(); 
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
  
  
  //Discrimination d1 = new Discrimination();
  
  /*  public void setOptions(String[] options) throws Exception {
    
    String saString = Utils.getOption('S', options);
    if (saString.length() != 0) {
      d1.setSA(Integer.parseInt(saString));
    } else {
      setSA(1);
    }
    
    String dString = Utils.getOption('D', options);
    if (dString.length() != 0) {
      d1.setD(Integer.parseInt(dString));
    } else {
      d1.setD(1);
    }   
  }
  
  */
  
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
      // Do the subsample, and clear the input instances.
      noFiltering();
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
  private void noFiltering()throws Exception {
                     
      Instances instances=getInputFormat();
   
   Enumeration enumInsts = instances.enumerateInstances();
    while (enumInsts.hasMoreElements()) {
      Instance instance = (Instance) enumInsts.nextElement();
            push(instance);
    }
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
 	Filter.batchFilterFile(new NoFilter(), argv);
      } else {
	Filter.filterFile(new NoFilter(), argv);
      }
    } catch (Exception ex) {
		ex.printStackTrace();
      System.out.println(ex.getMessage());
    }
  }
   
}

