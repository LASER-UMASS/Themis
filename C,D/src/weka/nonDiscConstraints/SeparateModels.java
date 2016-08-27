/*SeparateModels
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package weka.nonDiscConstraints;
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

public class SeparateModels extends MultipleClassifiersCombiner {

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






//================================Two models concept===================
  /**
   * Buildclassifier selects a classifier from the set of classifiers
   * by minimising error on the training data.
   *
   * @param data the training data to be used for generating the
   * boosted classifier.
   * @exception Exception if the classifier could not be built successfully
   */
 public void buildClassifier(Instances data) throws Exception {

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
   // System.out.println("Male:  "+newDataMale.numInstances()+ "   Female: "+newDataFemale.numInstances());
    //getClassifier(0).buildClassifier(newDataMale);
    //    getClassifier(1).buildClassifier(newDataFemale);
    //    getClassifier(2).buildClassifier(data);
    //    newData.deleteWithMissingClass();

    for (int i = 0; i < m_Classifiers.length; i++) {
      getClassifier(i).buildClassifier(data);
    }
  }


  public double[] distributionForInstance(Instance instance) throws Exception {
      double[] probs=new double[2];
      double []scores=new double[2];
      String sav="null";
      if(Discrimination.getSAA())
          sav=Discrimination.saValue;
      else
            sav=instance.toString(0);

       for(int j=0;j<m_Classifiers.length;j++){
            probs = getClassifier(j).distributionForInstance(instance);
              scores[0]+=probs[0];
               scores[1]+=probs[1];
          }//end of inner for with J counter
       scores[0]=scores[0]/m_Classifiers.length;
       scores[1]=scores[1]/m_Classifiers.length;
//System.out.println(instance.value(instance.attribute(Discrimination.getSaIndex()))+"\t"+instance.value(13)+"\t"+scores[1]);
    
      if(sav.equals("Male")){
             //probs = getClassifier(0).distributionForInstance(instance);
             if(scores[1]>=.5)
             probs[1]=1;
             else
                 probs[0]=1;
      }
        else{
            //probs = getClassifier(1).distributionForInstance(instance);
            if(scores[1]>=.5)
             probs[1]=1;
             else
                 probs[0]=1;
      }
  /* double[] probs = getClassifier(0).distributionForInstance(instance);
   double[] dist = getClassifier(1).distributionForInstance(instance);
   for (int j = 0; j < dist.length; j++)
	probs[j] += dist[j];
   dist = getClassifier(2).distributionForInstance(instance);
   for (int j = 0; j < dist.length; j++)
	probs[j] += dist[j];

    for (int j = 0; j < probs.length; j++) {
      probs[j] /= (double)m_Classifiers.length;
    }*/
       probs=scores;
    return probs;
  }

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
      System.out.println(Evaluation.evaluateModel(new SeparateModels(), argv));
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }
  }

}
