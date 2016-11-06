

/*
 *    ClassiferWithNDCs.java
 *    Author xxxx  Feb,2010
 *
 */

package weka.nonDiscConstraints;
import weka.classifiers.*;
import weka.classifiers.lazy.IBk;
import weka.classifiers.Evaluation;
import weka.classifiers.SingleClassifierEnhancer;
import weka.core.Drawable;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Utils;
import weka.filters.Filter;


import java.util.Enumeration;
import java.util.Vector;

/**
 * Class for running an arbitrary classifier on data that has been passed
 * through an arbitrary filter.<p>
 *
 * Valid options from the command line are:<p>
 *
 * -W classifierstring <br>
 * Classifierstring should contain the full class name of a classifier
 * (options are specified after a --). <p>
 *
 * -F filterstring <br>
 * Filterstring should contain the full class name of a filter
 * followed by options to the filter. <p>
 * @author Len Trigg (trigg@cs.waikato.ac.nz)
 * @version $Revision: 1.20.2.3 $
 *
 * -C= desired class index
 *
 * -S=sensitive attribite Index
 *
 * -D=deprived community
 *
 * -G=favored community
 *
 * -K= value of k for KNN
 *
 * -Z is true when SA is not used
 *
 */
public class ClassiferWithNDCs extends SingleClassifierEnhancer implements Drawable {

  /** The filter */
  protected Filter m_Filter = new weka.nonDiscConstraints.MassagingFilter();//  .supervised.attribute.AttributeSelection();

  /** The instance structure of the filtered instances */
  protected Instances m_FilteredInstances;
 /**  Flag to avoid preprocessing on the wole (train+test) dataset          */
        public static boolean preprocess=true;
  /**
   * Returns a string describing this classifier
   * @return a description of the classifier suitable for
   * displaying in the explorer/experimenter gui
   */
  public String globalInfo() {
    return   "Class for running an arbitrary classifier on data that has been passed "
      + "through an arbitrary filter specially the filters with Independency Constraints (ICs). Like the classifier, the structure of the filter "
      + "is based exclusively on the training data and test instances will be processed "
      + "by the filter without changing their structure.";
  }

  /**
   * String describing default classifier.
   */
  protected String defaultClassifierString() {
    
   //return "weka.classifiers.bayes.NaiveBayesSimple";
    //  return "weka.classifiers.meta.AdaBoostM1"; 
     return "weka.classifiers.lazy.IBk"; 
  }

  /**
   * Default constructor.
   */
  public ClassiferWithNDCs() {

    
    m_Classifier = new weka.classifiers.lazy.IBk(7);  
    m_Filter = new weka.nonDiscConstraints.MassagingFilter();
            
  }

  /**
   *  Returns the type of graph this classifier
   *  represents.
   */   
 public int graphType() {
    
    if (m_Classifier instanceof Drawable)
      return ((Drawable)m_Classifier).graphType();
    else 
      return Drawable.NOT_DRAWABLE;
  }

  /**
   * Returns graph describing the classifier (if possible).
   *
   * @return the graph of the classifier in dotty format
   * @exception Exception if the classifier cannot be graphed
   */
  public String graph() throws Exception {
    
    if (m_Classifier instanceof Drawable)
      return ((Drawable)m_Classifier).graph();
    else throw new Exception("Classifier: " + getClassifierSpec()
			     + " cannot be graphed");
  }

  /**
   * Returns an enumeration describing the available options.
   *
   * @return an enumeration of all the available options.
   */
  public Enumeration listOptions() {

    Vector newVector = new Vector(2);
    newVector.addElement(new Option(
	      "\tFull class name of filter to use, followed\n"
	      + "\tby filter options.\n"
	      + "\teg: \"weka.filters.AttributeFilter -V -R 1,2\"",
	      "F", 1, "-F <filter specification>"));
newVector.addElement(new Option(
              "\t Desired class index\n"
              + "\t(default: second value of class attribute, i.e., at index 1 of class).).\n",
              "C", 1, "-C <Desired class index>"));
newVector.addElement(new Option(
              "\t Sensitive attribute (SA) index (default: first attribute)\n",
              "S", 1, "-S <Sensitve Attribute index>"));
newVector.addElement(new Option(
              "\t Value of deprived community of sensitive attribute (SA)\n"
               + "\t(default: first value of SA, i.e., at index 0 of SA).\n",
              "D", 1, "-D <Deprived community>"));
newVector.addElement(new Option(
              "\t Value of favored community of sensitive attribute (SA)\n"
               + "\t(default: second value of SA, i.e., at index 1 of SA).\n",
              "G", 1, "-G <Favored community>"));
newVector.addElement(new Option(
              "\tIf flage is true, the SA is only used\n"
               + "\t for dependency calculation, otherwise SA is a part of\n"
               + "\twhole learning and testing process (default: false).\n",
              "Z", 1, "-Z <SAabsent>"));
newVector.addElement(new Option(
              "\t Complete name of the ranker used with Massaging filter\n"
               + "\t(default: weka.classifiers.bayes.NaiveBayesSimple).\n",
              "R", 1, "-R <Ranker>"));
    Enumeration enu = super.listOptions();
    while (enu.hasMoreElements()) {
      newVector.addElement(enu.nextElement());
    }

    return newVector.elements();
  }

  /**
   * Parses a given list of options. Valid options are:<p>
   *
   * -W classifierstring <br>
   * Classifierstring should contain the full class name of a classifier
   * (options are specified after a --). <p>
   *
   * -F filterstring <br>
   * Filterstring should contain the full class name of a filter
   * followed by options to the filter.<p>
   *
   * @param options the list of options as an array of strings
   * @exception Exception if an option is not supported
   */
  public void setOptions(String[] options) throws Exception {
     Discrimination.set_CLICheck(true);
    
     
    // Same for filter
    String filterString = Utils.getOption('F', options);
    if (filterString.length() > 0) {
        Discrimination.filterName=filterString;
      String [] filterSpec = Utils.splitOptions(filterString);
      if (filterSpec.length == 0) {
	throw new IllegalArgumentException("Invalid filter specification string");
      }
      String filterName = filterSpec[0];
      filterSpec[0] = "";
      setFilter((Filter) Utils.forName(Filter.class, filterName, filterSpec));
    } else {
      setFilter(new weka.filters.supervised.attribute.Discretize());
    }

    String rankerString = Utils.getOption('R', options);
    if (rankerString.length() > 0) {
        Discrimination.rankerName=rankerString;
        System.out.println(" \n \n Your ranker string is : "+rankerString);
      String [] rankerSpec = Utils.splitOptions(rankerString);
      if (rankerSpec.length == 0) {
	throw new IllegalArgumentException("Invalid ranker specification string");
      }
      String rankerName = rankerSpec[0];
      //System.out.println(" \n \n Your ranker string is : "+rankerName);
      
      Massaging.setRanker(Classifier.forName(rankerName, null));
      Massaging.setRanker(Classifier.forName(rankerName,Utils.partitionOptions(options)));
      
    } else {
        Massaging.setRanker(new weka.classifiers.bayes.NaiveBayesSimple());
    }

    String saIndex = Utils.getOption('S', options);
    if(saIndex.length()>0){
        Discrimination.setSaIndex(Integer.parseInt(saIndex));
        System.out.println(" \n \n sa Index is: "+Discrimination.getSaIndex());
    }
    else{
        Discrimination.setSaIndex(0);
    }
    String caIndex = Utils.getOption('E', options);
    if(caIndex.length()>0){
        Discrimination.setCaIndex(Integer.parseInt(caIndex));
        System.out.println(" \n \n ca Index is: "+Discrimination.getCaIndex());
    }
    else{
        Discrimination.setCaIndex(1);
    }
    String dcIndex = Utils.getOption('C', options);
    if(dcIndex.length()>0){
        Discrimination.setDC(Integer.parseInt(dcIndex));
        System.out.println(" \n \n DC Index is: "+Discrimination.getDC());
        
    }
    else{
        Discrimination.setDC(1);
    }


    String k_Value = Utils.getOption('K', options);
    if(k_Value.length()>0){
        m_Classifier = new IBk(Integer.parseInt(k_Value));
        Discrimination.classifierName="weka.classifiers.lazy.IBk-"+k_Value;
        System.out.println(" \n \n Value of K for default classifier of Class SingleClassifierEnhacer: "+k_Value);
        
    }
    else{
        m_Classifier = new IBk(7);
        Discrimination.classifierName="weka.classifiers.lazy.IBk-"+7;
    }


    String saAbsent = Utils.getOption('Z', options);
    if(saAbsent.length()>0){
        Discrimination.setSAA(Boolean.parseBoolean(saAbsent));
        System.out.println(" \n \nValue of saAbsent : "+Discrimination.getSAA());
    }
    else{
        Discrimination.setSAA(false);
    }
    

    String saDep = Utils.getOption('D', options);
    if(saDep.length()>0){
        Discrimination.setSaDep(saDep);
        System.out.println(" \n \n sa dep is: "+Discrimination.getSaDep());
    }
    else{
        System.out.println("The default value of SaFav, i.e., att(0).val(1) will used");
    }
    

    String saFav = Utils.getOption('G', options);
    if(saFav.length()>0){
        Discrimination.setSaFav(saFav);
        System.out.println(" \n \n sa fav is: "+Discrimination.getSaFav());
    }
    else{
         System.out.println("The default value of SaFav, i.e., att(0).val(1) will used");
    }
    
    String o_FileName = Utils.getOption('O', options);
    if(o_FileName.length()>0){
        Discrimination.set_O_FileName(o_FileName);
        System.out.println(" \n \n out put file name is : "+o_FileName);
    }
    else{
         Discrimination.set_O_FileName("results.txt");
        System.out.println(" \n \n out put file name is :  results.txt");
    }

    super.setOptions(options);
  }

  /**
   * Gets the current settings of the Classifier.
   *
   * @return an array of strings suitable for passing to setOptions
   */
  public String [] getOptions() {

    String [] superOptions = super.getOptions();
    String [] options = new String [superOptions.length + 2];
    int current = 0;

    options[current++] = "-F";
    options[current++] = "" + getFilterSpec();

    System.arraycopy(superOptions, 0, options, current, 
		     superOptions.length);
    return options;
  }
  
  /**
   * Returns the tip text for this property
   * @return tip text for this property suitable for
   * displaying in the explorer/experimenter gui
   */
  public String filterTipText() {
    return "The filter to be used.";
  }

  /**
   * Sets the filter
   *
   * @param filter the filter with all options set.
   */
  public void setFilter(Filter filter) {

    m_Filter = filter;
  }

  /**
   * Gets the filter used.
   *
   * @return the filter
   */
  public Filter getFilter() {

    return m_Filter;
  }
  
  /**
   * Gets the filter specification string, which contains the class name of
   * the filter and any options to the filter
   *
   * @return the filter string.
   */
  protected String getFilterSpec() {
    
    Filter c = getFilter();
    if (c instanceof OptionHandler) {
      return c.getClass().getName() + " "
	+ Utils.joinOptions(((OptionHandler)c).getOptions());
    }
    return c.getClass().getName();
  }

  /**
   * Build the classifier on the filtered data.
   *
   * @param data the training data
   * @exception Exception if the classifier could not be built successfully
   */
  public void buildClassifier(Instances data) throws Exception {

     if (m_Classifier == null) {
      throw new Exception("No base classifiers have been set!");
    }
   Discrimination.setCaValues(data.attribute(Discrimination.getCaIndex()).numValues());
    m_Filter.setInputFormat(data);
    //This flag will be false when the sample model is built ouer the whole training set
    if(preprocess)
    data = Filter.useFilter(data, m_Filter);
     m_FilteredInstances = data.stringFreeStructure();
    m_Classifier.buildClassifier(data);
  
   
  }

  /**
   * Classifies a given instance after filtering.
   *
   * @param instance the instance to be classified
   * @exception Exception if instance could not be classified
   * successfully
   */
  public double [] distributionForInstance(Instance instance)
    throws Exception {

   
   
    return m_Classifier.distributionForInstance(instance);
  }

  /**
   * Output a representation of this classifier
   */
  public String toString() {

    if (m_FilteredInstances == null) {
      return "FilteredClassifier: No model built yet.";
    }

    String result = "FilteredClassifier using "
      + getClassifierSpec()
      + " on data filtered through "
      + getFilterSpec()
      + "\n\nFiltered Header\n"
      + m_FilteredInstances.toString()
      + "\n\nClassifier Model\n"
      + m_Classifier.toString();
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
      System.out.println(Evaluation.evaluateModel(new ClassiferWithNDCs(),
						  argv));
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }
  }

}
