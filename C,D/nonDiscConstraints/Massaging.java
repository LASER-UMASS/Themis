

//Author Faisal Kamiran  Feb,2010

package weka.nonDiscConstraints;


import java.util.*;
import weka.core.*;
import weka.classifiers.*;
import weka.classifiers.bayes.*;
import weka.classifiers.lazy.*;
import weka.classifiers.trees.*;
import weka.classifiers.meta.*;

/**
 * Class for building and using a Classyfing without Discriminating classifier.
 * Numeric attributes are modelled by a normal distribution. For more
 * Discrimination has been calculated by taking the difference of confidence of SA values for decired class(DC)
 * Discrimination is removed by using the reweighing technique of CND
 * Author Toon Calders, Faisal Kamran
*/
    public class Massaging{
    /** To count the total number of changes in the train set by massaging*/
    public static int totalChanges=0;
    public static double epsilon=0;
/** The data set after tapplying massaging*/
    protected Instances massagedInst;
  
   /** All the counts for sensitive attribute value for positve and negative classes.  
    * saPos=favoured community with positve class   savPos=sav&+ */
    protected static double savPos=0,favNeg=0;//saPos=0,saNeg=0,
  
  /** array for sorted promotion list*/
    protected double [][] sortedPromotionList;
    
    /** array for sorted demotion list*/
    protected  double [][] sortedDemotionList;
  
    /** Confidence of deprived community for desired class(dc)*/
    protected static double confDep=0;
    
    /** Confidence of favoured community for desired class(dc)*/
    protected static double confFav=0;
    
    /**cpCount=number of candidates for promotion   cdCount=numeber of candidates for demotion*/
    protected  static int cpCount=0,cdCount=0; 
    
    
    /** To store a desired class(dc) index and Not desired class index(ndc) from  weka.classifiers.discrimination.java*/
    protected int dcIndex=Discrimination.getDC(),ndcIndex=Discrimination.getNDC();
   
 /**  Classifier used as Ranker*/
   //IBk classifier=new IBk(7);                               
    //RandomForest classifier=new RandomForest();
    //AdaBoostM1 classifier=new AdaBoostM1();
    public static  Classifier classifier=new NaiveBayesSimple();//NaiveBayesSimple
    //public static Classifier classifier=new J48();

    /**  Sensitive attribute related paramters like index,
     * name(SA),deprived communitity and favoured  corresponding values of The Discrimination class*/
    int sa_Index=Discrimination.getSaIndex();      
    String sa_Deprived=Discrimination.getSaDep()
          ,sa_Favored=Discrimination.getSaFav();    //sa= sensitive attribute   sa_deprived=deprived community   
    
   // public static  Classifier classifier;
     public static void setRanker(Classifier classifierName){
        classifier=classifierName;
    }
 
    /**
    * 
    * @param instances dataset for massa
    * @return
    * @throws java.lang.Exception
    */
      public Instances cndApplication(Instances instances,double promote,double demote )throws Exception{
        if(Discrimination.getSAA()){
            classifier.buildClassifier(Discrimination.trainInstsWithsa);
            ranker(Discrimination.trainInstsWithsa);
        }
        else{
            classifier.buildClassifier(instances);
            ranker(instances);
        }

     int cp=0,cd=0;
     int i=0;
     for(i=0;i<promote;i++)
     {
            cp=(int)sortedPromotionList[i][0];
            instances.instance(cp).setClassValue(dcIndex);
           
     }
     for(i=0;i<demote;i++)
     {
           
            cd=(int)sortedDemotionList[i][0];
            instances.instance(cd).setClassValue(ndcIndex);

     }
     totalChanges+=i;
     System.out.println("Changes during massaging for Current Fold := "+i+"  Total Changes =: "+ 2*totalChanges);
     return(instances);
 }
    public Instances cndApplication(Instances instances)throws Exception{
        if(Discrimination.getSAA()){
            classifier.buildClassifier(Discrimination.trainInstsWithsa);
            ranker(Discrimination.trainInstsWithsa);
        }
        else{
            classifier.buildClassifier(instances);
            ranker(instances);
        }
        
     int cp=0,cd=0;
     int i=0;
     while(confidenceDiff()>epsilon)
     {
            cp=(int)sortedPromotionList[i][0];
            cd=(int)sortedDemotionList[i][0];
            //System.out.println(i+" <----i         cp =: "+cp+"  cd =: "+cd);
            instances.instance(cp).setClassValue(dcIndex);
            instances.instance(cd).setClassValue(ndcIndex);
            savPos++;
            cpCount--;
            cdCount--;
            favNeg++;
       
        i++; 
     }
     totalChanges+=i;
     System.out.println("Changes during massaging for Current Fold := "+i+"  Total Changes =: "+ 2*totalChanges);
     return(instances);
 }

 //end of massaging function

    /**
    * ranker method produces sorted promotion and demotion list for CND
    * @param instances
    * @throws java.lang.Exception
    */
    public void ranker(Instances instances)throws Exception{
     
    // double [][] promotionList=new double[instances.numInstances()][2];
     // double[][] demotionList=new double[instances.numInstances()][2];
         double [][] promotionList=new double[100000][2];
      double[][] demotionList=new double[100000][2];
      double []prob=new double[instances.numClasses()]; //array to store probabilty distribution
      int instIndex=0;
     cdCount=cpCount=0;
     savPos=favNeg=0;
    Enumeration enumInsts = instances.enumerateInstances();
    int classValue=0;
    String saValue;   
  int gill=0;
    while (enumInsts.hasMoreElements()) {
      Instance instance = (Instance) enumInsts.nextElement();
        // System.out.println(instance);
      if (!instance.classIsMissing()) {
	           saValue= instance.toString(sa_Index);
                   classValue=(int) instance.classValue();
                   if(saValue.equals(sa_Deprived) && classValue==ndcIndex ){
                      promotionList[cpCount][0]=instIndex;
                      prob=classifier.distributionForInstance(instance);
                     promotionList[cpCount++][1]=prob[dcIndex]*100;
               }
               else if(!saValue.equals(sa_Deprived) && classValue==dcIndex){
                        demotionList[cdCount][0]=instIndex;
                        prob=classifier.distributionForInstance(instance);
                        demotionList[cdCount++][1]=prob[dcIndex]*100;
              }
                    else if(saValue.equals(sa_Deprived)&& classValue==dcIndex){
                        savPos++;
                    }
                    else if (!saValue.equals(sa_Deprived) && classValue==ndcIndex){
                        favNeg++;
                    }
              // System.out.println(instance.value(0)+" prob "+prob[dcIndex]*100);//instance.attribute(0).value(sa_Index)
      }
      instIndex++;
    }//end of outer while loop for the enumeration of instances 
 
  sortedPromotionList=sorting(promotionList,cpCount,1);
   sortedDemotionList=sorting(demotionList,cdCount,2);
/*   
 for(int i=0;i<sortedDemotionList.length;i++){
     //  System.out.println("this is a sorted list  i =:"+i+"  instance no =: "+sortedDemotionList[i][0]+"  DC Prob =: "+sortedDemotionList[i][1]);
       System.out.println(cdCount+" sorted demotion  list after sorting i =:"+i+"  instance no =: "+sortedDemotionList[i][0]+"  DC Prob =: "+sortedDemotionList[i][1]);}
   
     for(int i=0;i<sortedPromotionList.length;i++){
     //  System.out.println("this is a sorted list  i =:"+i+"  instance no =: "+sortedDemotionList[i][0]+"  DC Prob =: "+sortedDemotionList[i][1]);
       System.out.println(cpCount+" sorted promotion list after sorting i =:"+i+"  instance no =: "+sortedPromotionList[i][0]+"  DC Prob =: "+sortedPromotionList[i][1]);}
  */
 }
 
 /**
  * 
  * @return confidence difference of "sav" and "fav" for "dc"  
  */
 public static double confidenceDiff(){
     
     confDep=savPos*100/(savPos+cpCount); //savPos=sensitive attribute with DC, cpCount= SA witn out DC 
     confFav=cdCount*100/(favNeg+cdCount);
  //System.out.println("cof of Dep+ "+confDep+"  conf fav= "+confFav+"   sav Pos=  "+savPos+"  sav Neg  ="+cpCount+" fav Post=  "+cdCount+" fav neg= "+favNeg);
     return(confFav-confDep);
 }
 /**
  * method to sort the 2-D arrays
  * @param arrayToSort A 2-D array which we want to sort
  * @param type 1 is descending order and type 2 is for ascending order
  * @return sorted array
  */
  public static double[][] sorting(double [][] arrayToSort,int length,int type){
             int max=length;
             double val1=0,val2=0;
             double [][]sortedArray=new double[length][2];
             double [][] temp=new double[1][2];
           for(int index=0;index<length;index++)  
             for(int i=0;i<max-1;i++){
               try{  
                 val1=arrayToSort[i][1];
                 val2=arrayToSort[i+1][1];
        
                        if(val1<val2 && type==1){  //swapping for sort descending
                                 System.arraycopy(arrayToSort[i],0,temp[0],0,2);
                                 System.arraycopy(arrayToSort[i+1],0,arrayToSort[i],0,2);
                                 System.arraycopy(temp[0],0,arrayToSort[i+1],0,2);//System.out.println("val1 = "+val1+" new value of rec[] "+rec[i+1][20]+" i = "+i);
                          }     //end of  if
                            else if(val1>val2 && type==2){  //swapping for sort ascending
                                 System.arraycopy(arrayToSort[i],0,temp[0],0,2);
                                 System.arraycopy(arrayToSort[i+1],0,arrayToSort[i],0,2);
                                 System.arraycopy(temp[0],0,arrayToSort[i+1],0,2);//System.out.println("val1 = "+val1+" new value of rec[] "+rec[i+1][20]+" i = "+i);
                          }     //end of else if 
             
             } catch (NumberFormatException e){
                 System.out.println(" Probelme with sorting during Massaging");
                 }
               
                 }//end of out for-i loop
             for(int i=0;i<length;i++)
             System.arraycopy(arrayToSort[i],0, sortedArray[i],0, 2);
             return sortedArray;
         }   // End of sorting function


  // Extra code:  cndApplication1 function was used for bootstrappin
  /*
    public Instances cndApplication1(Instances instances, Instances impInsts)throws Exception{
        if(Discrimination.getSAA()){
               classifier.buildClassifier(Discrimination.trainInstsWithsa);
                ranker(Discrimination.trainInstsWithsa);
        }
        else{
            classifier.buildClassifier(impInsts);

             //classifier1.buildClassifier(instances);
             ranker(impInsts);
        }

     int cp=0,cd=0;

     int i=0;
     System.out.println("savpos  "+savPos+" ");
     while(confidenceDiff()>0) //
     {
         //cp=Integer.parseInt(Utils.doubleToString(sortedPromotionList[i][0],1));
         //cd=Integer.parseInt(Utils.doubleToString(sortedDemotionList[i][0],1));
         cp=(int)sortedPromotionList[i][0];
         cd=(int)sortedDemotionList[i][0];
       //System.out.println(i+" <----i         cp =: "+cp+"  cd =: "+cd);

         instances.instance(cp).setClassValue(dcIndex);
         instances.instance(cd).setClassValue(ndcIndex);


        //instances.delete(cp);//instance(cp).setClassValue(dcIndex);
        //instances.delete(cd);//.instance(cd).setClassValue(ndcIndex);

         savPos++;
         cpCount--;
         cdCount--;
         favNeg++;

        i++;
     }
     totalChanges+=i;
     System.out.println("Changes during massaging for Current Fold := "+i+"  Total Changes =: "+ 2*totalChanges);
     return(instances);
 }*/
 

 
}



