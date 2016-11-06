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
 *    PruneableClassifierTree.java
 *    Copyright (C) 1999 Eibe Frank
 *
 */

package weka.classifiers.trees.j48;

import weka.core.*;
import java.util.*;


import weka.nonDiscConstraints.*;
import weka.nonDiscConstraints.PrefrentialSamplingFilter;
import weka.filters.Filter;
import weka.classifiers.trees.J48;
        

/**
 * Class for handling a tree structure that can
 * be pruned using a pruning set. 
 *
 * @author Eibe Frank (eibe@cs.waikato.ac.nz)
 * @version $Revision: 1.8 $
 */
public class PruneableClassifierTree extends ClassifierTree{

  /** True if the tree is to be pruned. */
  private boolean pruneTheTree = false;

  /** How many subsets of equal size? One used for pruning, the rest for training. */
  private int numSets = 3;

  /** Cleanup after the tree has been built. */
  private boolean m_cleanup = true;

  /** The random number seed. */
  private int m_seed = 1;
  

public static double [][]disc=new double[2][2];
public static double unprunedTreeDisc[][]=new double[2][2];
public static double prunedTreeDisc[][]=new double[2][2];
public static double [][] leafRank=new double[10000][2];
public static int  leafID=0,topLeaf;
boolean stop=false;
public static double errors=0,total=0, total_changes=0;
protected int dcIndex=Discrimination.getDC(),ndcIndex=Discrimination.getNDC();
public static int saDepIndex=0,saFavIndex=0;
double epsilon=J48WithNDCs.m_epsilon;
  /**
   * Constructor for pruneable tree structure. Stores reference
   * to associated training data at each node.
   *
   * @param toSelectLocModel selection method for local splitting model
   * @param pruneTree true if the tree is to be pruned
   * @param num number of subsets of equal size
   * @exception Exception if something goes wrong
   */
  public PruneableClassifierTree(ModelSelection toSelectLocModel,
				 boolean pruneTree, int num, boolean cleanup,
				 int seed)
       throws Exception {

    super(toSelectLocModel);

    pruneTheTree = pruneTree;
    numSets = num;
    m_cleanup = cleanup;
    m_seed = seed;
  }

  /**
   * Method for building a pruneable classifier tree.
   *
   * @exception Exception if tree can't be built suleafIDessfully
   */
  public void buildClassifier(Instances data) 
       throws Exception {

   if (data.classAttribute().isNumeric())
      throw new Exception("Class is numeric!");
   
   data = new Instances(data);
   Random random = new Random(m_seed);
   data.deleteWithMissingClass();
   data.stratify(numSets);
   Massaging msg=new Massaging();
  PrefrentialSamplingFilter filt=new PrefrentialSamplingFilter();
   //UniformSamplingFilter filt=new UniformSamplingFilter();
   filt.setInputFormat(data);
   float opt=J48WithNDCs.t_v_comb;
  if(opt>0 && opt<9 ){
      /* if(opt==1)   buildTree(Filter.useFilter(data.trainCV(numSets, numSets - 1, random), filt),
                                data.testCV(numSets, numSets - 1), false);
              else if(opt==3)   buildTree(Filter.useFilter(data.trainCV(numSets, numSets - 1, random), filt),
                                Filter.useFilter(data.testCV(numSets, numSets - 1), filt), false);
              else if(opt==2){   buildTree(data.trainCV(numSets, numSets - 1, random),
                                msg.cndApplication(data.testCV(numSets, numSets - 1)), false);
              System.out.println("massaging is applid");
              }*/
              if(opt==1)   buildTree(msg.cndApplication(data.trainCV(numSets, numSets - 1, random)),
                                data.testCV(numSets, numSets - 1), false);
              else if(opt==2)   buildTree(Filter.useFilter(data.trainCV(numSets, numSets - 1, random), filt),
                                data.testCV(numSets, numSets - 1), false);
              else if(opt==3){   buildTree(data.trainCV(numSets, numSets - 1, random),
                                msg.cndApplication(data.testCV(numSets, numSets - 1)), false);
              System.out.println("massaging is applid");
              }
             else if(opt==4)   buildTree(msg.cndApplication(data.trainCV(numSets, numSets - 1, random)),
                                msg.cndApplication(data.testCV(numSets, numSets - 1)), false);
              else if(opt==5)   buildTree(Filter.useFilter(data.trainCV(numSets, numSets - 1, random), filt),
                                msg.cndApplication(data.testCV(numSets, numSets - 1)), false);
              else if(opt==6)   buildTree(data.trainCV(numSets, numSets - 1, random),
                                Filter.useFilter(data.testCV(numSets, numSets - 1),filt), false);
              else if(opt==7)   buildTree(msg.cndApplication(data.trainCV(numSets, numSets - 1, random)),
                                Filter.useFilter(data.testCV(numSets, numSets - 1),filt), false);
              else if(opt==8)   buildTree(Filter.useFilter(data.trainCV(numSets, numSets - 1, random), filt),
                                Filter.useFilter(data.testCV(numSets, numSets - 1),filt), false);
             
  }
  else
   buildTree(data.trainCV(numSets, numSets - 1, random),
	     data.testCV(numSets, numSets - 1), false);
  
  if (pruneTheTree) {
      /* if(J48WithNDCs.csa_prune)//
       {
        disc=init(disc);
         getDiscForTree();
        errors=total=0;
            removeDiscByPruning(); //pruning by using both c and SA
       }else if(J48WithNDCs.s_prune){
             disc=init(disc);
            getDiscForTree();
            errors=total=0;
            sPruning();//pruning by using only SA
       }
       else*/
            prune();//pruning by using only c
   }
  if(J48WithNDCs.m_relabel){
      setDiscParam(data);
           removeDisc();
        
  }
   if (m_cleanup) {
     cleanup(new Instances(data, 0));
 }
   

  }

  public static void setDiscParam(Instances data){
      int saIndex=Discrimination.getSaIndex();
     for(int i=0;i<data.attribute(saIndex).numValues();i++)
         if(data.attribute(saIndex).value(i).equals(Discrimination.getSaDep()))
             saDepIndex=i;
         else if(data.attribute(saIndex).value(i).equals(Discrimination.getSaFav()))
             saFavIndex=i;
  }
   public void removeDisc(){

      int i=0,changes=0;
      leafID=0;total=errors=0;
      disc=init(disc);
      getDiscForTree();
      leafRank=init(leafRank);
      leafID=0;
      relabelingTreeGlobally();
        //System.out.println(leafRank.length+" 2   CC =: "+leafID);
      leafRank=Massaging.sorting(leafRank,10000,1);
        
        while(getDisc(disc)>epsilon){
            //System.out.println(" massaged test disc in the loop"+getDisc(disc));
            changes++;
            topLeaf=(int)leafRank[i++][0];
            leafID=0;
            stop=false;
        changeLeaf();
        }
        System.out.println("Total insts changed=: \t"+total_changes+"\t Number of leaves changed=:\t"+changes);
  }
  public void changeLeaf(){
      double s=0,f=0,temp;
       
        int i,c=0;
        if (m_isLeaf){
           if(leafID++==topLeaf){
            s=m_test.m_sa[saDepIndex];
            f=m_test.m_sa[saFavIndex];
            c=localModel().distribution().maxClass();
           // newErrors=localModel().distribution().numCorrect()-localModel().distribution().numIncorrect();
            //tot=m_localModel.m_distribution.total();
            disc[saDepIndex][c]-=s;
            disc[saFavIndex][c]-=f;
            if(c==Discrimination.getNDC()){
            disc[saDepIndex][Discrimination.getDC()]+=s;
            disc[saFavIndex][Discrimination.getDC()]+=f;
            }
            else{
                disc[saDepIndex][Discrimination.getNDC()]+=s;
            disc[saFavIndex][Discrimination.getNDC()]+=f;
            }
      total_changes+=s+f;
            temp=localModel().distribution().m_perClass[saDepIndex];
            localModel().distribution().m_perClass[saDepIndex]=localModel().distribution().m_perClass[saFavIndex];
            localModel().distribution().m_perClass[saFavIndex]=temp;
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

 // Method to change labels of weak but discriminatory laves Global measures of AleafIDuracy and Discrimination
    public void relabelingTreeGlobally(){
       double newErrors,s=0,f=0;
       double [][] temp=new double[2][2];
       int i,c=0;
        if (m_isLeaf){
            s=m_test.m_sa[saDepIndex];
            f=m_test.m_sa[saFavIndex];
            c=localModel().distribution().maxClass();
            newErrors=m_test.perClass(localModel().distribution().maxClass())-(m_test.total()-m_test.perClass(localModel().distribution().maxClass()));
            //(m_test.total()-m_test.perClass(localModel().distribution().maxClass());
            temp[saDepIndex][c]=disc[saDepIndex][c]-s;
            temp[saFavIndex][c]=disc[saFavIndex][c]-f;
             if(c==Discrimination.getDC()){
                    
                    temp[saDepIndex][Discrimination.getNDC()]=disc[saDepIndex][Discrimination.getNDC()]+s;
                    temp[saFavIndex][Discrimination.getNDC()]=disc[saFavIndex][Discrimination.getNDC()]+f;
             }
             else{
                    temp[saDepIndex][Discrimination.getDC()]=disc[saDepIndex][Discrimination.getDC()]+s;
                    temp[saFavIndex][Discrimination.getDC()]=disc[saFavIndex][Discrimination.getDC()]+f;
             }
             
                leafRank[leafID][0]=(double)leafID;
                if(((newErrors+errors)/total-errors/total)==0)
                     leafRank[leafID][1]=0;//(getDisc(disc)-getDisc(temp))+10000;
                else
                     leafRank[leafID][1]=(getDisc(disc)-getDisc(temp))/((newErrors+errors)/total-errors/total);
               
               // leafRank[leafID][1]=(getDisc(disc)-getDisc(temp))-((newErrors+errors)/total-errors/total);
               // System.out.println(" CC # =: "+leafID+"\t"+leafRank[leafID][0]+"\t"+leafRank[leafID][1]);
                leafID++;
             }
            else
                for (i=0;i<m_sons.length;i++)
                    son(i).relabelingTreeGlobally();
    }//end of method
     // method to intialize any array
  private double[][] init(double temp[][]){
          //for (int i=0;i) 
     temp[0][0]=temp[0][1]=temp[1][0]=temp[1][1]=0;
     return temp;
    
  }
  // method to calcualte disleafIDrimination
  private double getDisc(double temp[][]){
      //int dc=Discrimination.getDC(),ndc=Discrimination.getNDC();
      //   return ((temp[saDepIndex][dcIndex]/(temp[saDepIndex][dcIndex]+temp[saDepIndex][ndcIndex]))
      //           -(temp[saFavIndex][dcIndex]/(temp[saFavIndex][dcIndex]+temp[saFavIndex][ndcIndex])));
     // ((temp[1][dcIndex]/(temp[1][dcIndex]+temp[1][ndcIndex]))-(temp[0][dcIndex]/(temp[0][dcIndex]+temp[0][ndcIndex])));
        return ((temp[1][1]/(temp[1][1]+temp[1][0]))-
                  (temp[0][1]/(temp[0][1]+temp[0][0])));

  }
  


  //int counter=0;
  // method to get Disc parameters from the whole tree
  //It calculate number of dep and fav in velidation set for each class wrt tree built over train
 protected void getDiscForTree(){
 //double [][] temp=new double[2][2];
      double s=0,f=0;
 int i,c=0;
        if (m_isLeaf){
            s=m_test.m_sa[saDepIndex];
            f=m_test.m_sa[saFavIndex];
            c=localModel().distribution().maxClass();
            errors+=m_test.total()-m_test.perClass(localModel().distribution().maxClass());
            total+=m_test.total();
            disc[saDepIndex][c]+=s;
            disc[saFavIndex][c]+=f;
            
     // System.out.println(" \n disc no   "+leafID+"   Total:"+tot);
     // System.out.println(" Errors: "+errors+" Fmale:   "+m_localModel.m_distribution.m_sa[0]+"  Male: \t "+m_localModel.m_distribution.m_sa[1]+"\n disc:" +
      //        disc[1][1]+"/"+(disc[1][0]+disc[1][1])+"\t Black: "+disc[0][1]+"/"+(disc[0][0]+disc[0][1]));
       }
    else{
      for (i=0;i<m_sons.length;i++){
               son(i).getDiscForTree();
      }
    }
  
 }
    
     
    
    
    

  /**
   * Prunes a tree.
   *
   * @exception Exception if tree can't be pruned suleafIDessfully
   */
  public void prune() throws Exception {
  
    if (!m_isLeaf) {
      
      // Prune all subtrees.
      for (int i = 0; i < m_sons.length; i++)
	son(i).prune();
      
      // Decide if leaf is best choice.
      if (Utils.smOrEq(errorsForLeaf(),errorsForTree())) {
	
	// Free son Trees
	m_sons = null;
	m_isLeaf = true;
	
	// Get NoSplit Model for node.
	m_localModel = new NoSplit(localModel().distribution());
      }
    }
  }

  /**
   * Returns a newly created tree.
   *
   * @param data and selection method for local models.
   */
  protected ClassifierTree getNewTree(Instances train, Instances test) 
       throws Exception {

    PruneableClassifierTree newTree = 
      new PruneableClassifierTree(m_toSelectModel, pruneTheTree, numSets, m_cleanup,
				  m_seed);
    newTree.buildTree(train, test, false);
    return newTree;
  }

  /**
   * Computes estimated errors for tree.
   *
   * @exception Exception if error estimate can't be computed
   */
  private double errorsForTree() throws Exception {

    double errors = 0;

    if (m_isLeaf)
      return errorsForLeaf();
    else{
      for (int i = 0; i < m_sons.length; i++)
	if (Utils.eq(localModel().distribution().perBag(i), 0)) {
	  errors += m_test.perBag(i)-
	    m_test.perClassPerBag(i,localModel().distribution().
				maxClass());
          //System.out.println("errors \t "+(m_test.perBag(i)- m_test.perClassPerBag(i,localModel().distribution().maxClass())));
	} else
	  errors += son(i).errorsForTree();

      return errors;
    }
  }
  
 

  /**
   * Computes estimated errors for leaf.
   *
   * @exception Exception if error estimate can't be computed
   */
  private double errorsForLeaf() throws Exception {
            //System.out.println("validation set Female: "+m_test.m_sa[0]+"  male:  "+m_test.m_sa[1]);
            //System.out.println("training set Female: "+localModel().distribution().m_sa[0]+" male: "+localModel().distribution().m_sa[1]);
    return m_test.total()-
      m_test.perClass(localModel().distribution().maxClass());
    
  }

  /**
   * Method just exists to make program easier to read.
   */
  private ClassifierSplitModel localModel() {
    
    return (ClassifierSplitModel)m_localModel;
  }

  /**
   * Method just exists to make program easier to read.
   */
  private PruneableClassifierTree son(int index) {

    return (PruneableClassifierTree)m_sons[index];
  }





  /*



  // Computes disc parameters  for Subtree to be pruned.


  private void getUnprunedTreeDisc(){
       double tot=0,s=0,f=0;
        int i,c=0;
        if (m_isLeaf){
            s=m_test.m_sa[0];
            f=m_test.m_sa[1];
            c=localModel().distribution().maxClass();
            tot=m_test.total();
            unprunedTreeDisc[0][c]+=s;
            unprunedTreeDisc[1][c]+=f;
            //System.out.println(" leaves of subtree: "+subTreeDisc[0][0]+"\t"+subTreeDisc[0][1]+"\t"+subTreeDisc[1][0]+"\t"+subTreeDisc[1][1]);
           }
           else{
                for (i=0;i<m_sons.length;i++)
                      son(i).getUnprunedTreeDisc();
         }
  }
   // mehtod to calculate the disc after prunign the sutree
   private double getPrunedDiscValue(){
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
  //  Method to get the dsic parameters, if sub tree is a leaf
   private void getPrunedTreeDisc(Distribution theDistribution){

            double tot=0,s=0,f=0;
                int i,c=0;
                s=m_test.m_sa[0];
                f=m_test.m_sa[1];
                c=theDistribution.maxClass();
                //tot=m_localModel.m_distribution.total();
                prunedTreeDisc[0][c]+=s;
                prunedTreeDisc[1][c]+=f;

   }
     public void removeDiscByPruning()throws Exception{

      double unprunedTreeDiscValue=0,prunedTreeDiscValue=0;
    double errorsLeaf,errorsTree;
    int i;
     if (!m_isLeaf){
      // Prune all subtrees.
      for (i=0;i<m_sons.length;i++)
	son(i).removeDiscByPruning();
      // Compute error if this Tree would be leaf
      errorsLeaf =errorsForLeaf();
      // Compute error for the whole subtree
       errorsTree = errorsForTree();

       //initialize treeDisc[][] subtree[][] and compute disc value for tree as leaf and subtree
      prunedTreeDisc=init(prunedTreeDisc);
      unprunedTreeDisc=init(unprunedTreeDisc);
      getPrunedTreeDisc(localModel().distribution());
       getUnprunedTreeDisc();

       //compute the disc if nor pruning is applied
       unprunedTreeDiscValue=getDisc(disc);
       //compute the disc vlaue if the tree would be a leaf i.e. after being pruned
       prunedTreeDiscValue=getPrunedDiscValue();
       //System.out.println(" tree disc: "+treeDiscValue+"\t Subtree Disc: "+subTreeDiscValue);
       //if (Utils.smOrEq(getDisc(disc),getPruneDisc()) ){   Utils.smOrEq(errorsForLeaf(),errorsForTree())
       if (((unprunedTreeDiscValue)>prunedTreeDiscValue && Utils.smOrEq(errorsLeaf,errorsTree)) ){//
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
 }

    public void sPruning()throws Exception{

      double unprunedTreeDiscValue=0,prunedTreeDiscValue=0;
     int i;
     if (!m_isLeaf){
      // Prune all subtrees.
      for (i=0;i<m_sons.length;i++)
	son(i).sPruning();
       //initialize treeDisc[][] subtree[][] and compute disc value for tree as leaf and subtree
      prunedTreeDisc=init(prunedTreeDisc);
      unprunedTreeDisc=init(unprunedTreeDisc);
      getPrunedTreeDisc(localModel().distribution());
       getUnprunedTreeDisc();

       //compute the disc if nor pruning is applied
       unprunedTreeDiscValue=getDisc(disc);
       //compute the disc vlaue if the tree would be a leaf i.e. after being pruned
       prunedTreeDiscValue=getPrunedDiscValue();
       //System.out.println(" tree disc: "+treeDiscValue+"\t Subtree Disc: "+subTreeDiscValue);
       //if (Utils.smOrEq(getDisc(disc),getPruneDisc()) ){
       if ((unprunedTreeDiscValue>prunedTreeDiscValue+0.1) ){//
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
 }

   protected void getDiscForTree2(){
 //double [][] temp=new double[2][2];
      double s=0,f=0;
 int i,c=0;
        if (m_isLeaf){
            s=m_test.m_sa[0];
            f=m_test.m_sa[1];
            c=localModel().distribution().maxClass();
            errors+=m_test.total()-m_test.perClass(localModel().distribution().maxClass());
            total+=m_test.total();
            disc[0][c]+=s;
            disc[1][c]+=f;

     // System.out.println(" \n disc no   "+leafID+"   Total:"+tot);
     // System.out.println(" Errors: "+errors+" Fmale:   "+m_localModel.m_distribution.m_sa[0]+"  Male: \t "+m_localModel.m_distribution.m_sa[1]+"\n disc:" +
      //        disc[1][1]+"/"+(disc[1][0]+disc[1][1])+"\t Black: "+disc[0][1]+"/"+(disc[0][0]+disc[0][1]));
       }
    else{
      for (i=0;i<m_sons.length;i++)
            if (Utils.eq(localModel().distribution().perBag(i), 0)) {
                 s=m_test.m_sa[0];
                f=m_test.m_sa[1];
                c=localModel().distribution().maxClass();
                total+=m_test.perBag(i);
                disc[0][c]+=s;
                disc[1][c]+=f;
                errors += m_test.perBag(i)-
                        m_test.perClassPerBag(i,localModel().distribution().
				maxClass());
                //System.out.println("femal\t "+s+"\t male \t"+f);
	} else
               son(i).getDiscForTree2();
}
}


   private double errorsForTree2() throws Exception {

    double errors = 0;

    if (m_isLeaf)
      return errorsForLeaf();
    else{
      for (int i = 0; i < m_sons.length; i++)
	if (Utils.eq(localModel().distribution().perBag(i), 0)) {
	  errors += m_test.perBag(i)-
	    m_test.perClassPerBag(i,localModel().distribution().
				maxClass());
          System.out.println("errors \t "+(m_test.perBag(i)- m_test.perClassPerBag(i,localModel().distribution().maxClass())));
	} else
	  errors += son(i).errorsForTree();

      return errors;
    }
  } */


}







