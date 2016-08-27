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
 *    J48.java
 *    Copyright (C) 1999 Eibe Frank

 *  Modified by xxxxxx on February, 15, 2009.
 * Please search for  word "change" to changes in in this class
 */
// change ICs parameters


package weka.nonDiscConstraints;

import weka.classifiers.trees.*;
import weka.classifiers.trees.j48.*;
import java.util.*;
import weka.core.*;
import weka.classifiers.*;
import weka.nonDiscConstraints.Discrimination;

/**
 * Class for generating an unpruned or a pruned C4.5 decision tree.
 * For more information, see<p>
 *
 * Ross Quinlan (1993). <i>C4.5: Programs for Machine Learning</i>, 
 * Morgan Kaufmann Publishers, San Mateo, CA. </p>
 *
 * Valid options are: <p>
 *
 * -U <br>
 * Use unpruned tree.<p>
 *
 * -C confidence <br>
 * Set confidence threshold for pruning. (Default: 0.25) <p>
 *
 * -M number <br>
 * Set minimum number of instances per leaf. (Default: 2) <p>
 *
 * -R <br>
 * Use reduced error pruning. No subtree raising is performed. <p>
 *
 * -N number <br>
 * Set number of folds for reduced error pruning. One fold is
 * used as the pruning set. (Default: 3) <p>
 *
 * -B <br>
 * Use binary splits for nominal attributes. <p>
 *
 * -S <br>
 * Don't perform subtree raising. <p>
 *
 * -L <br>
 * Do not clean up after the tree has been built. <p>
 *
 * -A <br>
 * If set, Laplace smoothing is used for predicted probabilites. <p>
 *
 * -Q <br>
 * The seed for reduced-error pruning. <p>
 *
 * @author Eibe Frank (eibe@cs.waikato.ac.nz)
 * @version $Revision: 1.2 $
 */
public class J48WithNDCs extends Classifier implements OptionHandler,
  Drawable, Matchable, Sourcable, WeightedInstancesHandler, Summarizable,
  AdditionalMeasureProducer {

  // To maintain the same version number after adding m_ClassAttribute
  static final long serialVersionUID = -217733168393644444L;

  /** The decision tree */
  private ClassifierTree m_root;
  
  /** Unpruned tree? */
  private boolean m_unpruned = false;

  /** Confidence level */
  private float m_CF = 0.25f;

  /** Minimum number of instances */
  private int m_minNumObj =2;

  /** Determines whether probabilities are smoothed using
      Laplace correction when predictions are generated */
  private boolean m_useLaplace = false;

  /** Use reduced error pruning? */
  private boolean m_reducedErrorPruning = false;

  /** Number of folds for reduced error pruning. */
  private int m_numFolds = 3;

  /** Binary splits on nominal attributes? */
  private boolean m_binarySplits = false;

  /** Subtree raising to be performed? */
  private boolean m_subtreeRaising = true;

  /** Cleanup after the tree has been built. */
  private boolean m_noCleanup = false;

  /** Random number seed for reduced-error pruning. */
  private int m_Seed = 1;
  // change
  public static float  t_v_comb=0;  // t_v_comb is to control train and validatio set comb
  public static double m_epsilon=0.01;
  public static boolean csa_prune=false;
  public static boolean m_relabel=false, s_prune=false;
  /* Define possible decision tree splitting  */
  public static int m_splitCriterion=1;
  public static final int IGC = 1;
  public static final int IGCPlusIGS = 2;
  public static final int IGCMinusIGS = 3;
  public static final int IGCDivideIGS = 4;
  public static final Tag [] TAGS_SPLITING = {
    new Tag(IGC, "IGC"),
    new Tag(IGCPlusIGS, "IGC+IGS"),
    new Tag(IGCMinusIGS, "IGC-IGS"),
    new Tag(IGCDivideIGS, "IGC/IGS")
  };

  /**
   * Returns a string describing classifier
   * @return a description suitable for
   * displaying in the explorer/experimenter gui
   */
  public String globalInfo() {

    return  "Class for generating a pruned or unpruned C4.5 decision tree. For more "
      + "information, see\n\n"
      + "Ross Quinlan (1993). \"C4.5: Programs for Machine Learning\", "
      + "Morgan Kaufmann Publishers, San Mateo, CA.\n\n";
  }
  
  /**
   * Generates the classifier.
   *
   * @exception Exception if classifier can't be built successfully
   */
  public void buildClassifier(Instances instances) 
       throws Exception {
 
    ModelSelection modSelection;	 

    if (m_binarySplits)
      modSelection = new BinC45ModelSelection(m_minNumObj, instances);
    else

          modSelection = new C45ModelSelection(m_minNumObj, instances);
    if (!m_reducedErrorPruning)
      m_root = new C45PruneableClassifierTree(modSelection, !m_unpruned, m_CF,
					    m_subtreeRaising, !m_noCleanup);
    else
      m_root = new PruneableClassifierTree(modSelection, !m_unpruned, m_numFolds,
					   !m_noCleanup, m_Seed);
    m_root.buildClassifier(instances);
    if (m_binarySplits) {
      ((BinC45ModelSelection)modSelection).cleanup();
    } else {
      ((C45ModelSelection)modSelection).cleanup();
    }
  }

  /**
   * Classifies an instance.
   *
   * @exception Exception if instance can't be classified successfully
   */
  public double classifyInstance(Instance instance) throws Exception {

    return m_root.classifyInstance(instance);
  }

  /** 
   * Returns class probabilities for an instance.
   *
   * @exception Exception if distribution can't be computed successfully
   */
  public final double [] distributionForInstance(Instance instance) 
       throws Exception {

    return m_root.distributionForInstance(instance, m_useLaplace);
  }

  /**
   *  Returns the type of graph this classifier
   *  represents.
   *  @return Drawable.TREE
   */   
  public int graphType() {
      return Drawable.TREE;
  }

  /**
   * Returns graph describing the tree.
   *
   * @exception Exception if graph can't be computed
   */
  public String graph() throws Exception {

    return m_root.graph();
  }

  /**
   * Returns tree in prefix order.
   *
   * @exception Exception if something goes wrong
   */
  public String prefix() throws Exception {
    
    return m_root.prefix();
  }


  /**
   * Returns tree as an if-then statement.
   *
   * @return the tree as a Java if-then type statement
   * @exception Exception if something goes wrong
   */
  public String toSource(String className) throws Exception {

    StringBuffer [] source = m_root.toSource(className);
    return 
    "class " + className + " {\n\n"
    +"  public static double classify(Object [] i)\n"
    +"    throws Exception {\n\n"
    +"    double p = Double.NaN;\n"
    + source[0]  // Assignment code
    +"    return p;\n"
    +"  }\n"
    + source[1]  // Support code
    +"}\n";
  }

  /**
   * Returns an enumeration describing the available options.
   *
   * Valid options are: <p>
   *
   * -U <br>
   * Use unpruned tree.<p>
   *
   * -C confidence <br>
   * Set confidence threshold for pruning. (Default: 0.25) <p>
   *
   * -M number <br>
   * Set minimum number of instances per leaf. (Default: 2) <p>
   *
   * -R <br>
   * Use reduced error pruning. No subtree raising is performed. <p>
   *
   * -N number <br>
   * Set number of folds for reduced error pruning. One fold is
   * used as the pruning set. (Default: 3) <p>
   *
   * -B <br>
   * Use binary splits for nominal attributes. <p>
   *
   * -S <br>
   * Don't perform subtree raising. <p>
   *
   * -L <br>
   * Do not clean up after the tree has been built.
   *
   * -A <br>
   * If set, Laplace smoothing is used for predicted probabilites. <p>
   *
   * -Q <br>
   * The seed for reduced-error pruning. <p>
   *
   * @return an enumeration of all the available options.
   */

  //change
  public Enumeration listOptions() {

    Vector newVector = new Vector(9);

    newVector.
	addElement(new Option("\tUse unpruned tree.",
			      "U", 0, "-U"));
    newVector.
	addElement(new Option("\tApply leaf relabeling on tree leaves to reduce "+
			      "\tthe dependency to epsilon.(default false)",
			      "Y", 1, "-Y"));
    newVector.
	addElement(new Option("\tEpsilon: Threshold to reduce the dependency"+
			      "\tby relabeling of tree leaves.(default 0.2)",
			      "P", 1, "-P"));
    newVector.
	addElement(new Option("\tValue to specify the splitting criterion."+
			      "\t 1 \t IGC"+
                              "\t 2\t IGC+IGS"+
                              "\t 3\t IGC-IGS"+
                              "\t 4\t IGC/IGS.(default: 1 (IGC))",
			      "E", 0, "-E"));
    newVector.
	addElement(new Option("\tSet confidence threshold for pruning.\n" +
			      "\t(default 0.25)",
			      "C", 1, "-C <pruning confidence>"));
    newVector.
	addElement(new Option("\tSet minimum number of instances per leaf.\n" +
			      "\t(default 2)",
			      "M", 1, "-M <minimum number of instances>"));
    newVector.
	addElement(new Option("\tUse reduced error pruning.",
			      "R", 0, "-R"));
    newVector.
	addElement(new Option("\tSet number of folds for reduced error\n" +
			      "\tpruning. One fold is used as pruning set.\n" +
			      "\t(default 3)",
			      "N", 1, "-N <number of folds>"));
    newVector.
	addElement(new Option("\tUse binary splits only.",
			      "B", 0, "-B"));
    newVector.
        addElement(new Option("\tDon't perform subtree raising.",
			      "S", 0, "-S"));
    newVector.
        addElement(new Option("\tDo not clean up after the tree has been built.",
			      "L", 0, "-L"));
   newVector.
        addElement(new Option("\tLaplace smoothing for predicted probabilities.",
			      "A", 0, "-A"));
    newVector.
      addElement(new Option("\tSeed for random data shuffling (default 1).",
			    "Q", 1, "-Q <seed>"));

    return newVector.elements();
  }

  /**
   * Parses a given list of options.
   *
   * @param options the list of options as an array of strings
   * @exception Exception if an option is not supported
   */
  // change
  public void setOptions(String[] options) throws Exception {
    
    // Other options
      m_reducedErrorPruning = Utils.getFlag('R', options);
    String minNumString = Utils.getOption('M', options);
    if (minNumString.length() != 0) {
      m_minNumObj = Integer.parseInt(minNumString);
    } else {
      m_minNumObj = 2;
    }
    
    // change ICs parameters
    String alphaValue=Utils.getOption('E', options);
    if (alphaValue.length() != 0){
       // m_splitCriterion =(new Float(alphaValue )).floatValue();
        m_splitCriterion =Integer.parseInt(alphaValue);
        C45ModelSelection.noSA=true;
        Discrimination.setSAA(false);// To prevent that NoSA and SaAbsent should not set simultaniously
    }
    else
        m_splitCriterion=1;
    String epsilonValue=Utils.getOption('P', options);
    if (epsilonValue.length() != 0){
        m_epsilon =(new Double(epsilonValue )).doubleValue();
    }
    else
        m_epsilon=0.02;

    if(m_reducedErrorPruning){
        String combValue=Utils.getOption('c', options);
        if(combValue.length()!=0)
        t_v_comb =(new Float( combValue)).floatValue();
        else
            t_v_comb=0;
    }
    //csa_prune=Utils.getFlag('P', options);
    s_prune=Utils.getFlag('s', options);
    m_relabel=Utils.getFlag('Y', options);
    if(m_relabel){
     C45ModelSelection.noSA=true;
     Discrimination.setSAA(false);// To prevent that NoSA and SaAbsent should not set simultaniously
    }
    m_binarySplits = Utils.getFlag('B', options);
    m_useLaplace = Utils.getFlag('A', options);

    // Pruning options
    m_unpruned = Utils.getFlag('U', options);
    m_subtreeRaising = !Utils.getFlag('S', options);
    m_noCleanup = Utils.getFlag('L', options);
    if ((m_unpruned) && (!m_subtreeRaising)) {
      throw new Exception("Subtree raising doesn't need to be unset for unpruned tree!");
    }
    
   /* if ((m_unpruned) && (m_reducedErrorPruning)) {
      throw new Exception("Unpruned tree and reduced error pruning can't be selected " +
			  "simultaneously!");
    }*/
      if ((m_relabel) && (m_SaAbsent)) {
      throw new Exception("Relabeling already removes SA");
    }
    String confidenceString = Utils.getOption('C', options);
    if (confidenceString.length() != 0) {
      if (m_reducedErrorPruning) {
	throw new Exception("Setting the confidence doesn't make sense " +
			    "for reduced error pruning.");
      } else if (m_unpruned) {
	throw new Exception("Doesn't make sense to change confidence for unpruned "
			    +"tree!");
      } else {
	m_CF = (new Float(confidenceString)).floatValue();
	if ((m_CF <= 0) || (m_CF >= 1)) {
	  throw new Exception("Confidence has to be greater than zero and smaller " +
			      "than one!");
	}
      }
    } else {
      m_CF = 0.25f;
    }
    String numFoldsString = Utils.getOption('N', options);
    if (numFoldsString.length() != 0) {
      if (!m_reducedErrorPruning) {
	throw new Exception("Setting the number of folds" +
			    " doesn't make sense if" +
			    " reduced error pruning is not selected.");
      } else {
	m_numFolds = Integer.parseInt(numFoldsString);
      }
    } else {
      m_numFolds = 3;
    }
    String seedString = Utils.getOption('Q', options);
    if (seedString.length() != 0) {
      m_Seed = Integer.parseInt(seedString);
    } else {
      m_Seed = 1;
    }
  }

  /**
   * Gets the current settings of the Classifier.
   *
   * @return an array of strings suitable for passing to setOptions
   */

  // change
  public String [] getOptions() {

    String [] options = new String [17];
    int current = 0;

   if (m_noCleanup) {
      options[current++] = "-L";
    }


    // change
    if (m_relabel) {
      options[current++] = "-Y";
    }
    options[current++] = "" + m_splitCriterion;
    options[current++] = "" + m_epsilon;

    if (m_unpruned) {
      options[current++] = "-U";
    } else {
      if (!m_subtreeRaising) {
	options[current++] = "-S";
      }
      if (m_reducedErrorPruning) {
	options[current++] = "-R";
	options[current++] = "-N"; options[current++] = "" + m_numFolds;
	options[current++] = "-Q"; options[current++] = "" + m_Seed;
      } else {
	options[current++] = "-C"; options[current++] = "" + m_CF;
      }
    }
    if (m_binarySplits) {
      options[current++] = "-B";
    }
    options[current++] = "-M"; options[current++] = "" + m_minNumObj;
    if (m_useLaplace) {
      options[current++] = "-A";
    }

    while (current < options.length) {
      options[current++] = "";
    }
    return options;
  }

  /**
   * Returns the tip text for this property
   * @return tip text for this property suitable for
   * displaying in the explorer/experimenter gui
   */
  public String seedTipText() {
    return "The seed used for randomizing the data " +
      "when reduced-error pruning is used.";
  }

  /**
   * Get the value of Seed.
   *
   * @return Value of Seed.
   */
  public int getSeed() {
    
    return m_Seed;
  }
  /**
   * Returns the tip text for this property
   * @return tip text for this property suitable for
   * displaying in the explorer/experimenter gui
   */
  public String relabelTipText() {
    return "if set, tree leaves are relabled to " +
      "reduce the dependency to a given threshold.";
  }

  /**
   * Get the value of Seed.
   *
   * @return Value of Seed.
   */
  // change
  public boolean getrelabel() {

    return m_relabel;
  }
// change
  public void setrelabel(boolean r) {
       
            if (r) {
                    m_SaRemove=false; // To prevent that NoSA and SaAbsent should not set simultaniously
                    setSaAbsent(true);
                    C45ModelSelection.noSA=true; //This flag makes sure that SA is excluded
                }
         m_relabel=r;
 }

/**
   * Returns the tip text for this property
   * @return tip text for this property suitable for
   * displaying in the explorer/experimenter gui
   */
  // change
  public String splitCriterionTipText() {
    return "\tValue to specify the splitting criterion.";
  }

 /**
   * Gets the splitting criterion used. Will be one of
   * IGC, IGC+IGS, IGC-IGS or IGC/IGS
    * @return the splitting criterion used.
   */
  // change
  public SelectedTag getsplitCriterion() {

    return new SelectedTag(m_splitCriterion, TAGS_SPLITING);//
  }
  
  /**
   * Sets the splitting criterion used. Will be one of
   * IGC, IGC+IGS, IGC-IGS or IGC/IGS
   * @param splitting criterion method to use
   */
  // change
  public void setsplitCriterion(SelectedTag newMethod) {
    
    if (newMethod.getTags() == TAGS_SPLITING) {
      m_splitCriterion = newMethod.getSelectedTag().getID();
      if(m_splitCriterion!=1){
           m_SaRemove=false;
         setSaAbsent(true);
       //   if(m_SaAbsent)
          C45ModelSelection.noSA=true; //This flag makes sure that SA is excluded
      }
    }
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

    }

  /**
   * Set the value of Seed.
   *
   * @param newSeed Value to assign to Seed.
   */
  public void setSeed(int newSeed) {
    
    m_Seed = newSeed;
  }

  /**
   * Returns the tip text for this property
   * @return tip text for this property suitable for
   * displaying in the explorer/experimenter gui
   */
  public String useLaplaceTipText() {
    return "Whether counts at leaves are smoothed based on Laplace.";
  }

  /**
   * Get the value of useLaplace.
   *
   * @return Value of useLaplace.
   */
  public boolean getUseLaplace() {
    
    return m_useLaplace;
  }
  
  /**
   * Set the value of useLaplace.
   *
   * @param newuseLaplace Value to assign to useLaplace.
   */
  public void setUseLaplace(boolean newuseLaplace) {
    
    m_useLaplace = newuseLaplace;
  }
  
  /**
   * Returns a description of the classifier.
   */
  public String toString() {

    if (m_root == null) {
      return "No classifier built";
    }
    if (m_unpruned)
      return "J48 unpruned tree\n------------------\n" + m_root.toString();
    else
      return "J48 pruned tree\n------------------\n" + m_root.toString();
  }

  /**
   * Returns a superconcise version of the model
   */
  public String toSummaryString() {

    return "Number of leaves: " + m_root.numLeaves() + "\n"
         + "Size of the tree: " + m_root.numNodes() + "\n";
  }

  /**
   * Returns the size of the tree
   * @return the size of the tree
   */
  public double measureTreeSize() {
    return m_root.numNodes();
  }

  /**
   * Returns the number of leaves
   * @return the number of leaves
   */
  public double measureNumLeaves() {
    return m_root.numLeaves();
  }

  /**
   * Returns the number of rules (same as number of leaves)
   * @return the number of rules
   */
  public double measureNumRules() {
    return m_root.numLeaves();
  }
  
  /**
   * Returns an enumeration of the additional measure names
   * @return an enumeration of the measure names
   */
  public Enumeration enumerateMeasures() {
    Vector newVector = new Vector(3);
    newVector.addElement("measureTreeSize");
    newVector.addElement("measureNumLeaves");
    newVector.addElement("measureNumRules");
    return newVector.elements();
  }

  /**
   * Returns the value of the named measure
   * @param measureName the name of the measure to query for its value
   * @return the value of the named measure
   * @exception IllegalArgumentException if the named measure is not supported
   */
  public double getMeasure(String additionalMeasureName) {
    if (additionalMeasureName.compareToIgnoreCase("measureNumRules") == 0) {
      return measureNumRules();
    } else if (additionalMeasureName.compareToIgnoreCase("measureTreeSize") == 0) {
      return measureTreeSize();
    } else if (additionalMeasureName.compareToIgnoreCase("measureNumLeaves") == 0) {
      return measureNumLeaves();
    } else {
      throw new IllegalArgumentException(additionalMeasureName 
			  + " not supported (j48)");
    }
  }
  
  /**
   * Returns the tip text for this property
   * @return tip text for this property suitable for
   * displaying in the explorer/experimenter gui
   */
  public String unprunedTipText() {
    return "Whether pruning is performed.";
  }

  /**
   * Get the value of unpruned.
   *
   * @return Value of unpruned.
   */
  public boolean getUnpruned() {
    
    return m_unpruned;
  }
  
  /**
   * Set the value of unpruned. Turns reduced-error pruning
   * off if set.
   * @param v  Value to assign to unpruned.
   */
  public void setUnpruned(boolean v) {

    if (v) {
      m_reducedErrorPruning = false;
    }
    m_unpruned = v;
  }

  /**
   * Returns the tip text for this property
   * @return tip text for this property suitable for
   * displaying in the explorer/experimenter gui
   */
  public String confidenceFactorTipText() {
    return "The confidence factor used for pruning (smaller values incur "
      + "more pruning).";
  }
  
  /**
   * Get the value of CF.
   *
   * @return Value of CF.
   */
  public float getConfidenceFactor() {
    
    return m_CF;
  }
  
  /**
   * Set the value of CF.
   *
   * @param v  Value to assign to CF.
   */
  public void setConfidenceFactor(float v) {
    
    m_CF = v;
  }
   
  /**
   * Returns the tip text for this property
   * @return tip text for this property suitable for
   * displaying in the explorer/experimenter gui
   */
  public String minNumObjTipText() {
    return "The minimum number of instances per leaf.";
  }

  /**
   * Get the value of minNumObj.
   *
   * @return Value of minNumObj.
   */
  public int getMinNumObj() {
    
    return m_minNumObj;
  }
  
  /**
   * Set the value of minNumObj.
   *
   * @param v  Value to assign to minNumObj.
   */
  public void setMinNumObj(int v) {
    
    m_minNumObj = v;
  }

  /**
   * Returns the tip text for this property
   * @return tip text for this property suitable for
   * displaying in the explorer/experimenter gui
   */
  public String reducedErrorPruningTipText() {
    return "Whether reduced-error pruning is used instead of C.4.5 pruning.";
  }
 
  /**
   * Get the value of reducedErrorPruning. 
   *
   * @return Value of reducedErrorPruning.
   */
  public boolean getReducedErrorPruning() {
    
    return m_reducedErrorPruning;
  }
  
  /**
   * Set the value of reducedErrorPruning. Turns
   * unpruned trees off if set.
   *
   * @param v  Value to assign to reducedErrorPruning.
   */
  public void setReducedErrorPruning(boolean v) {
    
    if (v) {
      m_unpruned = false;
    }
    m_reducedErrorPruning = v;
  }
  
  /**
   * Returns the tip text for this property
   * @return tip text for this property suitable for
   * displaying in the explorer/experimenter gui
   */
  public String numFoldsTipText() {
    return "Determines the amount of data used for reduced-error pruning. "
      + " One fold is used for pruning, the rest for growing the tree.";
  }

  /**
   * Get the value of numFolds.
   *
   * @return Value of numFolds.
   */
  public int getNumFolds() {
    
    return m_numFolds;
  }
  
  /**
   * Set the value of numFolds.
   *
   * @param v  Value to assign to numFolds.
   */
  public void setNumFolds(int v) {
    
    m_numFolds = v;
  }
 
  /**
   * Returns the tip text for this property
   * @return tip text for this property suitable for
   * displaying in the explorer/experimenter gui
   */
  public String binarySplitsTipText() {
    return "Whether to use binary splits on nominal attributes when "
      + "building the trees.";
  }
  
  /**
   * Get the value of binarySplits.
   *
   * @return Value of binarySplits.
   */
  public boolean getBinarySplits() {
    
    return m_binarySplits;
  }
  
  /**
   * Set the value of binarySplits.
   *
   * @param v  Value to assign to binarySplits.
   */
  public void setBinarySplits(boolean v) {
    
    m_binarySplits = v;
  }
  
  /**
   * Returns the tip text for this property
   * @return tip text for this property suitable for
   * displaying in the explorer/experimenter gui
   */
  public String subtreeRaisingTipText() {
    return "Whether to consider the subtree raising operation when pruning.";
  }
 
  /**
   * Get the value of subtreeRaising.
   *
   * @return Value of subtreeRaising.
   */
  public boolean getSubtreeRaising() {
    
    return m_subtreeRaising;
  }
  
  /**
   * Set the value of subtreeRaising.
   *
   * @param v  Value to assign to subtreeRaising.
   */
  public void setSubtreeRaising(boolean v) {
    
    m_subtreeRaising = v;
  }

  /**
   * Returns the tip text for this property
   * @return tip text for this property suitable for
   * displaying in the explorer/experimenter gui
   */
  public String saveInstanceDataTipText() {
    return "Whether to save the training data for visualization.";
  }

  /**
   * Check whether instance data is to be saved.
   *
   * @return true if instance data is saved
   */
  public boolean getSaveInstanceData() {
    
    return m_noCleanup;
  }
  
  /**
   * Set whether instance data is to be saved.
   * @param v true if instance data is to be saved
   */
  public void setSaveInstanceData(boolean v) {
    
    m_noCleanup = v;
  }
 
  /**
   * Main method for testing this class
   *
   * @param String options 
   */
  public static void main(String [] argv){

    try {
      System.out.println(Evaluation.evaluateModel(new J48WithNDCs(), argv));
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }
  }
}


  






