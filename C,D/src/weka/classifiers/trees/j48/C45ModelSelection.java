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
 *    C45ModelSelection.java
 *    Copyright (C) 1999 Eibe Frank
 *
  *  Modified by xxxx on February, 15, 2009.
 * Please search for  word "change" to changes in in this class
 */

//change to exclude SA from the splitting attribute list
package weka.classifiers.trees.j48;

import java.util.*;
import weka.core.*;
import weka.nonDiscConstraints.Discrimination;

/**
 * Class for selecting a C4.5-type split for a given dataset.
 *
 * @author Eibe Frank (eibe@cs.waikato.ac.nz)
 * @version $Revision: 1.8 $y
 */
public class C45ModelSelection extends ModelSelection {

  /** Minimum number of objects in interval. */
  private int m_minNoObj;               

  /** All the training data */
  private Instances m_allData; // 
  public static boolean noSA=false;
  /** Model for Sensitive Attribute*/
  // public  C45Split discModel = null;
  

  /**
   * Initializes the split selection method with the given parameters.
   *
   * @param minNoObj minimum number of instances that have to occur in at least two
   * subsets induced by split
   * @param allData FULL training dataset (necessary for
   * selection of split points).
   */
  public C45ModelSelection(int minNoObj, Instances allData) {
    m_minNoObj =minNoObj;
    m_allData = allData;
  }
  public C45ModelSelection() {
   
  }

  /**
   * Sets reference to training data to null.
   */
  public void cleanup() {

    m_allData = null;
  }
/**
   * Sets reference to training data to null.
   */
 /* public final C45Split getDiscModel() {

        return discModel;
  }*/
  /**
   * Selects C4.5-type split for the given dataset.
   */
  boolean flag=true;
  public final ClassifierSplitModel selectModel(Instances data){

    double minResult;
    double currentResult;
    C45Split [] currentModel;
    C45Split bestModel = null;
    
    NoSplit noSplitModel = null;
    double averageInfoGain = 0;
    int validModels = 0;
    boolean multiVal = true;
    Distribution checkDistribution;
    Attribute attribute;
    double sumOfWeights;
    int i;
    
    try{

      // Check if all Instances belong to one class or if not
      // enough Instances to split.
      checkDistribution = new Distribution(data);
      noSplitModel = new NoSplit(checkDistribution);
      if (Utils.sm(checkDistribution.total(),2*m_minNoObj) ||
	  Utils.eq(checkDistribution.total(),
		   checkDistribution.perClass(checkDistribution.maxClass())))
                    return noSplitModel;

      // Check if all attributes are nominal and have a 
      // lot of values.
      if (m_allData != null) {
	Enumeration enu = data.enumerateAttributes();
	while (enu.hasMoreElements()) {
	  attribute = (Attribute) enu.nextElement();
	  if ((attribute.isNumeric()) ||
	      (Utils.sm((double)attribute.numValues(),
			(0.3*(double)m_allData.numInstances())))){
	    multiVal = false;
	    break;
	  }
	}
      } 

      currentModel = new C45Split[data.numAttributes()];
      sumOfWeights = data.sumOfWeights();
 //change to exclude SA from the splitting attribute list
   int c;
  if(noSA)
         c=   weka.nonDiscConstraints.Discrimination.getSaIndex();////122;//8;//
   else
       c=data.classIndex();
      // For each attribute.
   //System.out.println("\n\n============New turn============\n\n");
      for (i = 0; i < data.numAttributes(); i++){
	
	// Apart from class attribute.
       
	if (i != (data).classIndex() && i!=c){//
	  
	  // Get models for current attribute.
	  currentModel[i] = new C45Split(i,m_minNoObj,sumOfWeights);
	  currentModel[i].buildClassifier(data);
	   //System.out.println(" \n model no    "+i+"  \t :"+currentModel[i].m_distribution.m_sa[1]);
	  // Check if useful split for current attribute
	  // exists and check for enumerated attributes with 
	  // a lot of values.
	  if (currentModel[i].checkModel())
	    if (m_allData != null) {
	      if ((data.attribute(i).isNumeric()) ||
		  (multiVal || Utils.sm((double)data.attribute(i).numValues(),
					(0.3*(double)m_allData.numInstances())))){
		averageInfoGain = averageInfoGain+currentModel[i].infoGain();
		validModels++;
	      } 
	    } else {
	      averageInfoGain = averageInfoGain+currentModel[i].infoGain();
	      validModels++;
	    }
	}else
	  currentModel[i] = null;
      }
      
      // Check if any useful split was found.
      if (validModels == 0)
	return noSplitModel;
      averageInfoGain = averageInfoGain/(double)validModels;

      // Find "best" attribute to split on.
      minResult = 0;
      for (i=0;i<data.numAttributes();i++){
	if (((i != (data).classIndex()) && i!=c ) &&//|| weka.classifiers.discrimination.Discrimination.getSaIndex()
	    (currentModel[i].checkModel()))
	  
	  // Use 1E-3 here to get a closer approximation to the original
	  // implementation.
            if((currentModel[i].infoGain() >= (averageInfoGain-1E-3)) && Utils.gr(currentModel[i].gainRatio(),minResult)){ 
	    bestModel = currentModel[i];
	    minResult = currentModel[i].gainRatio();
	  } 
        
      }
      //if(i==Discrimination.getSaIndex())
     // discModel = new C45Split(Discrimination.getSaIndex(),m_minNoObj,sumOfWeights);
     // discModel.buildClassifier(data);
     // bestModel = new C45Split(Discrimination.getSaIndex(),m_minNoObj,sumOfWeights);
     
      
     // System.out.println("male"+discModel.m_distribution.m_sa[1]+"female"+discModel.m_distribution.m_sa[0]);
           //    discModel=cur+rentModel[Discrimination.getSaIndex()];
   
      // Check if useful split was found.
      if (Utils.eq(minResult,0))
	return noSplitModel;
      
      // Add all Instances with unknown values for the corresponding
      // attribute to the distribution for the model, so that
      // the complete distribution is stored with the model. 
      bestModel.distribution().
	  addInstWithUnknown(data,bestModel.attIndex());
      
     // discModel.distribution().
	// addInstWithUnknown(data,bestModel.attIndex());
   
      // Set the split point analogue to C45 if attribute numeric.
      if (m_allData != null)
	bestModel.setSplitPoint(m_allData);
     // System.out.println(" \n normal in c45   "+bestModel.m_distribution.total()+"   Disc total :"+discModel.m_distribution.total());
    //System.out.println(" \n norma   "+bestModel.m_distribution.m_sa[1]+"   Disc total :"+discModel.m_distribution.m_sa[1]);
      return bestModel;
    }catch(Exception e){
      e.printStackTrace();
    }
    return null;
  }
 
  /**
   * Selects C4.5-type split for the given dataset.
   */
  public final ClassifierSplitModel selectModel(Instances train, Instances test) {

    return selectModel(train);
  }
 
}



