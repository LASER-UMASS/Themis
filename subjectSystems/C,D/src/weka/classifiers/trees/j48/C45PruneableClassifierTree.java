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
 *    C45PruneableClassifierTree.java
 *    Copyright (C) 1999 Eibe Frank
 *
 *  Modified by xxxxx on February, 15, 2009.
 * Please search for  word "change" to changes in in this class
 */

package weka.classifiers.trees.j48;

import weka.core.*;
import weka.classifiers.trees.J48;
import weka.nonDiscConstraints.Discrimination;
import weka.nonDiscConstraints.*;

/**
 * Class for handling a tree structure that can
 * be pruned using C4.5 procedures.
 *
 * @author Eibe Frank (eibe@cs.waikato.ac.nz)
 * @version $Revision: 1.11 $
 */

public class C45PruneableClassifierTree extends ClassifierTree{

  /** True if the tree is to be pruned. */
  boolean m_pruneTheTree = false;

  /** The confidence factor for pruning. */
  float m_CF = 0.25f;

  /** Is subtree raising to be performed? */
  boolean m_subtreeRaising = true;

  /** Cleanup after the tree has been built. */
  boolean m_cleanup = true;
public static double [][]disc=new double[2][2];
//public static double unprunedTreeDisc[][]=new double[2][2];
//public static double prunedTreeDisc[][]=new double[2][2];
public static double [][] leafRank=new double[10000][2];
public static int leafID=0,topLeaf;
boolean stop=false;
public static double errors=0,total=0, total_changes=0;
protected int dcIndex=Discrimination.getDC(),ndcIndex=Discrimination.getNDC();
double epsilon=J48WithNDCs.m_epsilon;
public static int saDepIndex=0,saFavIndex=0;


  /**
   * Constructor for pruneable tree structure. Stores reference
   * to associated training data at each node.
   *
   * @param toSelectLocModel selection method for local splitting model
   * @param pruneTree true if the tree is to be pruned
   * @param cf the confidence factor for pruning
   * @exception Exception if something goes wrong
   */
  public C45PruneableClassifierTree(ModelSelection toSelectLocModel,
				    boolean pruneTree,float cf,
				    boolean raiseTree,
				    boolean cleanup)
       throws Exception {

    super(toSelectLocModel);

    m_pruneTheTree = pruneTree;
    m_CF = cf;
    m_subtreeRaising = raiseTree;
    m_cleanup = cleanup;
  }

  /**
   * Method for building a pruneable classifier tree.
   *
   * @exception Exception if something goes wrong
   */
  public void buildClassifier(Instances data) throws Exception {
 //total_changes=0;
   if (data.classAttribute().isNumeric())
     throw new UnsupportedClassTypeException("Class is numeric!");
   if (data.checkForStringAttributes()) {
     throw new UnsupportedAttributeTypeException("Cannot handle string attributes!");
   }

  
   data = new Instances(data);
   data.deleteWithMissingClass();
   buildTree(data, m_subtreeRaising);
  collapse();

   if (m_pruneTheTree) {
       
            prune();// Pruning using only class Attribute
   }

  if(J48WithNDCs.m_relabel)
        relabeling();
   if (m_cleanup) {
     cleanup(new Instances(data, 0));
 }
  }
 public void setDiscParam(Instances data){
      int saIndex=Discrimination.getSaIndex();
     for(int i=0;i<data.attribute(saIndex).numValues();i++)
         if(data.attribute(saIndex).value(i).equals(Discrimination.getSaDep()))
             saDepIndex=i;
         else if(data.attribute(saIndex).value(i).equals(Discrimination.getSaFav()))
             saFavIndex=i;
  }
  
  //change
  /**
   * method to remove dependency
   * by changing the labels of tree leaves.
   */
  public void relabeling(){
      int i=0,changes=0;
      total=errors=0;
      disc=init(disc);
       getDiscForTree();
      leafRank=init(leafRank);
      //System.out.println(" 1  CC =: "+cc);
        //relabelingTree();
      leafID=0;
      leafRanking();
        //System.out.println(leafRank.length+" 2   CC =: "+cc);
      leafRank=Massaging.sorting(leafRank,10000,1);
        
        while(getDisc(disc)>epsilon){
            changes++;
            topLeaf=(int)leafRank[i++][0];
            leafID=0;
            stop=false;
        changeLeaf();
        }
       System.out.println("Total insts changed=: \t"+total_changes+"\t Number of leaves changed=:\t"+changes);
      // total_changes=0;
  }

  //change
  public void changeLeaf(){
      double s=0,f=0,temp;
       
        int i,c=0;
        if (m_isLeaf){
           if(leafID++==topLeaf){
            s=localModel().distribution().m_sa[0];
            f=localModel().distribution().m_sa[1];
            c=localModel().distribution().maxClass();
           // newErrors=localModel().distribution().numCorrect()-localModel().distribution().numIncorrect();
            //tot=m_localModel.m_distribution.total();
            disc[0][c]-=s;
            disc[1][c]-=f;
            if(c==0){
            disc[0][1]+=s;
            disc[1][1]+=f;
            }
            else{
                disc[0][0]+=s;
            disc[1][0]+=f;
            }
       total_changes+=s+f;
           temp=localModel().distribution().m_perClass[0];
              localModel().distribution().m_perClass[0]=localModel().distribution().m_perClass[1];
              localModel().distribution().m_perClass[1]=temp;
              stop=true;
                }
            }
        else{
        for (i=0;i<m_sons.length;i++){
            if(stop==true)
              break;
               son(i).changeLeaf();
      }
    } // end of else        
}

  //change
  // Method to change labels of weak but discriminatory laves Global measures of Accuracy and Discrimination
    public void leafRanking(){
       double newErrors,s=0,f=0,discGain=0,alpha=1;
       double [][] temp=new double[2][2];

        int i,c=0;
        if (m_isLeaf){

            s=localModel().distribution().m_sa[0];
            f=localModel().distribution().m_sa[1];
            c=localModel().distribution().maxClass();
            newErrors=(localModel().distribution().numCorrect()-localModel().distribution().numIncorrect());
            temp[0][c]=disc[0][c]-s;
            temp[1][c]=disc[1][c]-f;
             if(c==Discrimination.getDC()){

                    temp[0][Discrimination.getNDC()]=disc[0][Discrimination.getNDC()]+s;
                    temp[1][Discrimination.getNDC()]=disc[1][Discrimination.getNDC()]+f;
             }
             else{
                    temp[0][Discrimination.getDC()]=disc[0][Discrimination.getDC()]+s;
                    temp[1][Discrimination.getDC()]=disc[1][Discrimination.getDC()]+f;
             }

                leafRank[leafID][0]=(double)leafID;
                if(((newErrors+errors)/total-errors/total)==0){
                     leafRank[leafID][1]=0;//(getDisc(disc)-getDisc(temp))+10000;
                    // System.out.println(s+" Male: \t"+f+" new erroros =: "+newErrors+" label:  "+c);

                }
                else
                     leafRank[leafID][1]=(getDisc(disc)-getDisc(temp))/((newErrors+errors)/total-errors/total);

               // leafRank[cc][1]=(getDisc(disc)-getDisc(temp))-((newErrors+errors)/total-errors/total);
               // System.out.println(" CC # =: "+cc+"\t"+leafRank[cc][0]+"\t"+leafRank[cc][1]);
                leafID++;
    }
    else
      for (i=0;i<m_sons.length;i++)
               son(i).leafRanking();
 }//end of method

    //change
  private double[][] init(double temp[][]){
          //for (int i=0;i)
     temp[0][0]=temp[0][1]=temp[1][0]=temp[1][1]=0;
     return temp;

  }

  //change
  // method to calcualte disccrimination
  private double getDisc(double temp[][]){
          return ((temp[1][dcIndex]/(temp[1][dcIndex]+temp[1][ndcIndex]))-(temp[0][dcIndex]/(temp[0][dcIndex]+temp[0][ndcIndex])));
  }
  //change
   // method to get Disc parameters from the whole tree
 protected void getDiscForTree(){
 //double [][] temp=new double[2][2];
      double s=0,f=0,temp;
 int i,c=0;
        if (m_isLeaf){

      //temp=discModel().distribution().getDisc();
            s=localModel().distribution().m_sa[0];
            f=localModel().distribution().m_sa[1];
            c=localModel().distribution().maxClass();
            errors+=localModel().distribution().numIncorrect();
            total+=m_localModel.m_distribution.total();
            disc[0][c]+=s;
            disc[1][c]+=f;
      //System.out.println(" \n disc no   "+cc+"   Total:"+total);
      //System.out.println(" Errors: "+errors+" Fmale:   "+m_localModel.m_distribution.m_sa[0]+"  Male: \t "+m_localModel.m_distribution.m_sa[1]+"\n disc:" +
      //        disc[1][1]+"/"+(disc[1][0]+disc[1][1])+"\t Black: "+disc[0][1]+"/"+(disc[0][0]+disc[0][1]));
       }
    else{
      for (i=0;i<m_sons.length;i++){
         // System.out.println(m_sons.length+":\t"+i+"   Male  "+son(i).m_localModel.m_distribution.m_sa[1]+"   Female:   "+son(i).m_localModel.m_distribution.m_sa[0]+"   Total:"+son(i).m_localModel.m_distribution.total());

          son(i).getDiscForTree();

      }
    }


  }

// ============================original Weka methods====================================


  public final void collapse(){

    double errorsOfSubtree;
    double errorsOfTree;
    int i;

    if (!m_isLeaf){
      errorsOfSubtree = getTrainingErrors();
      errorsOfTree = localModel().distribution().numIncorrect();
      if (errorsOfSubtree >= errorsOfTree-1E-3){

	// Free adjacent trees
	m_sons = null;
	m_isLeaf = true;

	// Get NoSplit Model for tree.
	m_localModel = new NoSplit(localModel().distribution());
      }else
	for (i=0;i<m_sons.length;i++)
	  son(i).collapse();
    }

  }

/**
   * Prunes a tree using C4.5's pruning procedure.
   *
   * @exception Exception if something goes wrong
   */
  public void prune() throws Exception {

    double errorsLargestBranch;
    double errorsLeaf;
    double errorsTree;
    int indexOfLargestBranch;
    C45PruneableClassifierTree largestBranch;
    int i;

    if (!m_isLeaf){

      // Prune all subtrees.
      for (i=0;i<m_sons.length;i++)
	son(i).prune();

      // Compute error for largest branch
      indexOfLargestBranch = localModel().distribution().maxBag();
      if (m_subtreeRaising) {
	errorsLargestBranch = son(indexOfLargestBranch).
	  getEstimatedErrorsForBranch((Instances)m_train);
      } else {
	errorsLargestBranch = Double.MAX_VALUE;
      }

      // Compute error if this Tree would be leaf
      errorsLeaf =
	getEstimatedErrorsForDistribution(localModel().distribution());

      // Compute error for the whole subtree
      errorsTree = getEstimatedErrors();

      // Decide if leaf is best choice.
      if (Utils.smOrEq(errorsLeaf,errorsTree+0.1) &&
	  Utils.smOrEq(errorsLeaf,errorsLargestBranch+0.1)){

	// Free son Trees
	m_sons = null;
	m_isLeaf = true;

	// Get NoSplit Model for node.
	m_localModel = new NoSplit(localModel().distribution());
	return;
      }

      // Decide if largest branch is better choice
      // than whole subtree.
      if (Utils.smOrEq(errorsLargestBranch,errorsTree+0.1)){
	largestBranch = son(indexOfLargestBranch);
	m_sons = largestBranch.m_sons;
	m_localModel = largestBranch.localModel();
	m_isLeaf = largestBranch.m_isLeaf;
	newDistribution(m_train);
	prune();
      }
    }
  }


  /**
   * Returns a newly created tree.
   *
   * @exception Exception if something goes wrong
   */
  protected ClassifierTree getNewTree(Instances data) throws Exception {

    C45PruneableClassifierTree newTree =
      new C45PruneableClassifierTree(m_toSelectModel, m_pruneTheTree, m_CF,
				     m_subtreeRaising, m_cleanup);
    newTree.buildTree((Instances)data, m_subtreeRaising);

    return newTree;
  }


   /**
   * Method just exists to make program easier to read.
   */
  private ClassifierSplitModel localModel(){

    return (ClassifierSplitModel)m_localModel;
  }

  /**
   * Computes new distributions of instances for nodes
   * in tree.
   *
   * @exception Exception if something goes wrong
   */
  private void newDistribution(Instances data) throws Exception {

    Instances [] localInstances;

    localModel().resetDistribution(data);
    m_train = data;
    if (!m_isLeaf){
      localInstances =
	(Instances [])localModel().split(data);
      for (int i = 0; i < m_sons.length; i++)
	son(i).newDistribution(localInstances[i]);
    } else {

      // Check whether there are some instances at the leaf now!
      if (!Utils.eq(data.sumOfWeights(), 0)) {
	m_isEmpty = false;
      }
    }
  }

  /**
   * Method just exists to make program easier to read.
   */
  private C45PruneableClassifierTree son(int index){
      return (C45PruneableClassifierTree)m_sons[index];
  }

  /**
   * Computes estimated errors for one branch.
   *
   * @exception Exception if something goes wrong
   */
  private double getEstimatedErrorsForBranch(Instances data)
       throws Exception {

    Instances [] localInstances;
    double errors = 0;
    int i;

    if (m_isLeaf)
      return getEstimatedErrorsForDistribution(new Distribution(data));
    else{
      Distribution savedDist = localModel().m_distribution;
      localModel().resetDistribution(data);
      localInstances = (Instances[])localModel().split(data);
      localModel().m_distribution = savedDist;
      for (i=0;i<m_sons.length;i++)
	errors = errors+
	  son(i).getEstimatedErrorsForBranch(localInstances[i]);
      return errors;
    }
  }

  /**
   * Computes estimated errors for leaf.
   */

  private double getEstimatedErrorsForDistribution(Distribution
						   theDistribution){

   /*   System.out.print("\n "+theDistribution.total());
for(int i=0;i<theDistribution.m_perBag.length;i++)
    System.out.print(theDistribution.m_perBag.length+" value \t "+theDistribution.m_perBag[i]);*/
      //System.out.println(" \n disc no   "+cc+"   Total:"+theDistribution.total());

    if (Utils.eq(theDistribution.total(),0))
      return 0;
    else
      return theDistribution.numIncorrect()+
	Stats.addErrs(theDistribution.total(),
		      theDistribution.numIncorrect(),m_CF);
  }
   /**
   * Computes estimated errors for tree.
   */

  private double getEstimatedErrors(){
    double errors = 0;
    int i;
    if (m_isLeaf){
      return getEstimatedErrorsForDistribution(localModel().distribution());
     }
    else{
      for (i=0;i<m_sons.length;i++){
	errors = errors+son(i).getEstimatedErrors();
      }
      return errors;
    }
  }
   /**
   * Computes errors of tree on training data.
   */
  private double getTrainingErrors(){

    double errors = 0;
    int i;

    if (m_isLeaf)
      return localModel().distribution().numIncorrect();
    else{
      for (i=0;i<m_sons.length;i++)
	errors = errors+son(i).getTrainingErrors();
      return errors;
    }
  }

}

// method to intialize any array

 /*
 public void sPruning()throws Exception{
     //System.out.println(" this is S pruning");
     double unprunedTreeDiscValue=0,prunedTreeDiscValue=0;
     int i;
     if (!m_isLeaf){
      // Prune all subtrees.
      for (i=0;i<m_sons.length;i++)
	son(i).sPruning();           
             
       //initialize treeDisc[][] subtree[][] and compute disc value for tree as leaf and subtree
      prunedTreeDisc=init(prunedTreeDisc);
      unprunedTreeDisc=init(unprunedTreeDisc);
      
       getUnprunedTreeDisc();
       getPrunedTreeDisc(localModel().distribution());
       //compute the disc if nor pruning is applied
       unprunedTreeDiscValue=getDisc(disc);
       //compute the disc vlaue if the tree would be a leaf i.e. after being pruned
       prunedTreeDiscValue=getPrunedDiscValue();
       //System.out.println(" tree disc: "+treeDiscValue+"\t Subtree Disc: "+subTreeDiscValue);
       //if (Utils.smOrEq(getDisc(disc),getPruneDisc()) ){
       if (((unprunedTreeDiscValue)>prunedTreeDiscValue+0.1)  ){//&& Utils.smOrEq(errorsLeaf,errorsTree+0.1)
                //System.out.println(" tree is biing pruned");
                disc[0][0]=prunedTreeDisc[0][0];
                disc[0][1]=prunedTreeDisc[0][1];
                disc[1][0]=prunedTreeDisc[1][0];
                disc[1][1]=prunedTreeDisc[1][1];
                // Free son Trees
                m_sons = null;
                m_isLeaf = true;
		// Get NoSplit Model for node.
                m_localModel = new NoSplit(localModel().distribution());
                return;
      }
     
    }       
 }*/
 
 



  // mehtod to calculate the disc value after prunign the sutree
/*  private double getPrunedDiscValue(){
        //System.out.println("\n\n DISC: "+disc[0][0]+"\t"+disc[0][1]+"\t"+disc[1][0]+"\t"+disc[1][1]);
        //System.out.println("Tree as a leaf DISC: "+treeDisc[0][0]+"\t"+treeDisc[0][1]+"\t"+treeDisc[1][0]+"\t"+treeDisc[1][1]);
        //System.out.println("Sub tree DISC: "+subTreeDisc[0][0]+"\t"+subTreeDisc[0][1]+"\t"+subTreeDisc[1][0]+"\t"+subTreeDisc[1][1]);
      prunedTreeDisc[0][0]=disc[0][0]+(prunedTreeDisc[0][0]-unprunedTreeDisc[0][0]);
      prunedTreeDisc[0][1]=disc[0][1]+(prunedTreeDisc[0][1]-unprunedTreeDisc[0][1]);
      prunedTreeDisc[1][0]=disc[1][0]+(prunedTreeDisc[1][0]-unprunedTreeDisc[1][0]);
      prunedTreeDisc[1][1]=disc[1][1]+(prunedTreeDisc[1][1]-unprunedTreeDisc[1][1]);
     // System.out.println("valuses after pruning: "+subTreeDisc[0][0]+"\t"+subTreeDisc[0][1]+"\t"+subTreeDisc[1][0]+"\t"+subTreeDisc[1][1]);
      return getDisc(prunedTreeDisc);

  }
  */
  //  Method to get the dsic parameters (values of SA at this node, if all its sons are prunes), if sub tree is a leaf
  /* private void getPrunedTreeDisc(Distribution theDistribution){

            double tot=0,s=0,f=0;
                int i,c=0;
                s=theDistribution.m_sa[0];
                f=theDistribution.m_sa[1];
                c=theDistribution.maxClass();
                //tot=m_localModel.m_distribution.total();
                prunedTreeDisc[0][c]+=s;
                prunedTreeDisc[1][c]+=f;

   }*/
  /**
   * Computes disc parameters (sum of values of SA over all nodes of subtree to be pruned)  for Subtree to be pruned.
   */
/*
  private void getUnprunedTreeDisc(){
       double s=0,f=0;
        int i,c=0;
        if (m_isLeaf){
            s=localModel().distribution().m_sa[0];
            f=localModel().distribution().m_sa[1];
            c=localModel().distribution().maxClass();
            //tot=m_localModel.m_distribution.total();
            unprunedTreeDisc[0][c]+=s;
            unprunedTreeDisc[1][c]+=f;
            //System.out.println(" leaves of subtree: "+subTreeDisc[0][0]+"\t"+subTreeDisc[0][1]+"\t"+subTreeDisc[1][0]+"\t"+subTreeDisc[1][1]);
           }
           else{
                for (i=0;i<m_sons.length;i++)
                      son(i).getUnprunedTreeDisc();
         }
  }

*/
 






