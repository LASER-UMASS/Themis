/*
Causal discrimination testing for Subject System B
Inputs :
    argv[1] : Train file
    argv[2] : Argument to test discriminationa gainst
              8 means race and 9 means gender
*/
#include <math.h>
#include <stdexcept>
#include <sstream>
#include "header.h"
#include <algorithm>
#include <stdlib.h>
string trainfile;
string fix;

vector< vector<int> > findsubsets(vector<int> set, int len)
{
    vector< vector<int> > subset, subset_out;
    vector<int> empty;
    subset.push_back( empty );
    
    for (int i = 0; i < set.size(); i++)
    {
        vector< vector<int> > subsetTemp = subset;
        
        for (int j = 0; j < subsetTemp.size(); j++)
        subsetTemp[j].push_back( set[i] );
        
        for (int j = 0; j < subsetTemp.size(); j++){
        subset.push_back( subsetTemp[j] );
        if(subsetTemp[j].size()==len)
            subset_out.push_back(subsetTemp[j]);
        }
        
    }
    return subset_out;
}
void check_ratio(vector<int> fixed, node* root,vvs tableInfo, string defaultClass ){
    
    //Minimum number of inputs tos tart applying the confidence check optimization
    int mininp=50000;

    //Maximum number of inputs to test against
    int maxinp = 50000;

    //set printout to 0 to not print the test suite
    int printout = 1;
    int tmp[] = { 10,8,70,16,7,14,6,5,2,100,40,100,40 };
    std::vector<int> num_atr( tmp, tmp+13 );

    ofstream fout;
    int option=4;
    //Output file
    string outfile="../../Suites/freshBcausalmarital.txt";
    fout.open(outfile,ios::out);
       string decision;
       string already = "../../Suites/Bcausalmarital.txt";
    if (option==3 || option==4){
        int fixval=atoi(fix.c_str());
        ifstream fin;
        fin.open(already,ios::in);
        string line;
        int maxrows;
        int num=0,den=0;
        if (fixval==5){
            maxrows=7;
        }
        else{
            maxrows=5;
        }
        int pos=0,neg=0;
        int iter=0;
        while (fin>>line){
                stringstream ss(line); // Insert the string into a stream
                string token; // Create vector to hold our words
            std::vector<string> inp;
            string testinp;
            string printinp;
            int currval = -1;
            while(std::getline(ss, token, ',')) {
                //std::cout << token << endl;
                inp.push_back(token);
                printinp+=token+",";
                testinp +=token+" ";
                if(inp.size()==13)
                    break;
            }

            if(option==3){
                decision =testDataOnDecisionTree(inp, root, tableInfo, defaultClass);
            }else{
                string cmd = "./test "+trainfile+" "+testinp;
                FILE *in;
                char buff[512];
                in = popen(cmd.c_str(), "r");
                while(fgets(buff, sizeof(buff), in)!=NULL){
                    decision = buff;
                    
                }
            }

            printinp+=","+decision;
            if (printout==1)
                fout<<printinp<<endl;
            currval = atoi(inp[fixval-1].c_str());
            if (decision.compare("1")==0)
                pos++;
            else{
                neg++;
            }

           iter++; 
           if(iter%maxrows==0){
            if(pos>0 && neg>0)
                num++;
            den++;
            pos=0;
            neg=0;
           }
        }
        
        cout<<"Score is  "<<num*100.0/den<<endl;
        
        return;
            
    }
    
    int num_test = 0;
    vector<int> fix_atr;
    int num=1;
    for (int i=0;i<fixed.size();i++){//i in range(0,len(fixed)):
        if(fixed[i]==1){
            num = num*num_atr[i];
            fix_atr.push_back(i);
        }
    }
    int max = -1;
    int min = 100;
    
    int val = 0;
    string inp_fix[]={"","","","","","","","","","","","",""};
    while (val< num){
         
        int i=(fix_atr.size())-1;
        int tmp_val = val;
        
        while (i>=0){
            inp_fix[fix_atr[i]] = tmp_val%num_atr[fix_atr[i]];
            tmp_val = (tmp_val - tmp_val%num_atr[fix_atr[i]])/num_atr[fix_atr[i]];
            i-=1;
        }
        val+=1;
    }
        
        
        string inp[]={"","","","","","","","","","","","",""};
        int num_inp = 0;
        double pos = 0;
        double neg = 0;

    

    for (int i3=0;i3<maxinp;i3++){
        if(num_inp>=maxinp)
            break;
        int j=0;
        
        while (j<(num_atr.size())){
            if (inp_fix[j].compare("")==0)
               inp[j] = to_string((int)rand()%num_atr[j]);
            else
                inp[j]=inp_fix[j];
            j+=1;
            
        }
        int val = 0;
        double pos_found = 0;
        double neg_found = 0;
        while (val< num){
           // string inp_fix1[]=["","","","","","","","","","","","",""];

            int i=(fix_atr.size())-1;
            int tmp_val = val;
            
            while (i>=0){
                inp[fix_atr[i]] = to_string(tmp_val%num_atr[fix_atr[i]]);
                tmp_val = (tmp_val - tmp_val%num_atr[fix_atr[i]])/num_atr[fix_atr[i]];
                i-=1;
            }
            val+=1;
            /*if(','.join(str(inp)) in curr_map.keys()):
                pos_found=-1
                #print "here",inp
                break
            */

            num_test+=1;
            std::vector<string> inpstr( inp, inp+13 );

              j=0;
              string printinp;
            string testinp;
            while (j<inpstr.size()){
                printinp+=inpstr[j]+",";
                testinp+=inpstr[j]+" ";
                j++;
            }
            //out = np.sign(np.dot(w, inp))
            string cmd = "./test "+trainfile+" "+testinp;
            //cout<<cmd<<endl;
            if(option==1){
                FILE *in;
                char buff[512];
                in = popen(cmd.c_str(), "r");
                while(fgets(buff, sizeof(buff), in)!=NULL){
                    decision = buff;
                    
                }

                //decision = system (cmd.c_str());
                //cout<<decision<<endl;
            }
            else
                decision =testDataOnDecisionTree(inpstr, root, tableInfo, defaultClass);
// /cout<<decision<<endl;
            


            if (printout ==1)
                fout<<printinp<<decision<<endl;
            if(decision.compare("1")==0)
                pos_found=1;
            else
                neg_found = 1;
            
        }
        if(pos_found==1 and neg_found==1)
            pos+=1;
        else
            neg+=1;
        double frac = pos*1.0/(pos+neg);
             if (2.5*sqrt(frac*(1-frac)*1.0/(pos+neg)<0.05) && pos+neg>mininp)
            {

               break;
            }
        num_inp+=1;
    }
    //cout<<pos*1.0/(pos+neg)<<endl;
   cout<<"output is in "<<outfile<<endl;

}
int main(int argc, const char *argv[])
{
	ifstream inputFile;												// Input file stream
	string singleInstance;											// Single line read from the input file 
	vvs dataTable;													// Input data in the form of a vector of vector of strings
    srand (1992);
        trainfile = argv[1];

	inputFile.open(argv[1]);
	if (!inputFile)													// If input file does not exist, print error and exit
	{
		cerr << "Error: Training data file not found!" << endl;
		exit(-1);
	}

	/* 
	 * Decision tree training phase
	 * In this phase, the training data is read
	 * from the file and stored into a vvs using
	 * the parse() function. The generateTableInfo()
	 * function extracts the attribute (column) names
	 * and also the values that each column can take.
	 * This information is also stored in a vvs.
	 * buildDecisionTree() function recursively
	 * builds trains the decision tree.
	 */
	while (getline(inputFile, singleInstance))						// Read from file, parse and store data
	{
		parse(singleInstance, dataTable);
	}
	inputFile.close(); 												// Close input file
	vvs tableInfo = generateTableInfo(dataTable);					// Stores all the attributes and their values in a vector of vector of strings named tableInfo
	node* root = new node;											// Declare and assign memory for the root node of the Decision Tree
    
    root = buildDecisionTree(dataTable, root, tableInfo);			// Recursively build and train decision tree
    
    string defaultClass = returnMostFrequentClass(dataTable);		// Stores the most frequent class in the training data. This is used as the default class label
	dataTable.clear(); 												// clear dataTable of training data to store testing data

    
    
    
    
    //Generate a set of inputs for which we get 1 as the output
    //Then check a set of inputs which have 
    
    
    
  
    
    
//    Input :
    
    string decision;
    
    vector<int> attr;
    vector<string> val;
    
    vector<int> list;
    for(int i=0;i<13;i++)
        list.push_back(i);
    
      
    int fixedAtr = 5;//stoi(argv[2]);
    fix = to_string(5);//argv[2];
    static const int arr[] = {0,0,0,0,0,0,0,0,0,0,0,0,0};

    vector<int> fixed (arr, arr + sizeof(arr) / sizeof(arr[0]) );
    fixed[fixedAtr - 1] = 1;
  
    //vector<int> fixed = {0,0,0,0,0,0,0,0,1,0,0,0,0};
    check_ratio(fixed,root, tableInfo, defaultClass);
    
    /*
    int i=0;
    while (i <13){

        vector< vector<int> > out = findsubsets(list,i+1);
        for (vector<int>a:out){
            vector<int> fixed;// = [0,0,0,0,0,0,0,0,0,0,0,0,0]
            for (int j = 0;j<13;j++){
                if (std::find(a.begin(), a.end(),j)!=a.end())
                    fixed.push_back(1);
                else
                    fixed.push_back(0);
            }
    
            check_ratio(fixed,root, tableInfo, defaultClass);
        }
        i++;
    }
    */
    
    
    
    
    //Call check XX function and check adamant function
    //XX will change everything in race and see if the output changes
    //adamant will change everything other than the race and check of output changes
    //inp is an array of strings so convert int to strings...
    
    
    	return 0;
}



