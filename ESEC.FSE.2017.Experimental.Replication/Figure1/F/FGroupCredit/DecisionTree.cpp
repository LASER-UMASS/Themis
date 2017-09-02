/*
Group discrimination testing for Subject System F for Credit dataset
Inputs :
    argv[1] : Train file
    argv[2] : Attribute to test discriminationa against
*/
#include "header.h"
#include <stdexcept>
#include <stdio.h>
#include <algorithm>
#include <sstream>
#include <stdlib.h>
string trainfile;

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
    
    //set printout to 0 to not print the test suite
    int printout = 1;

    //Minimum number of inputs tos tart applying the confidence check optimization
    int mininp=50000;

    //Maximum number of inputs to test against
    int maxinp=50000;
    string decision;
    int tmp[] = { 4,80,5,11,200,5,5,4,2,3,4,4,75,3,3,4,4,3,2,2 };
    std::vector<int> num_atr( tmp, tmp+20 );
    ofstream fout;
    
    string outfile="../../Suites/freshFcredit99.txt";
    fout.open(outfile,ios::out);
    int option=4;
    string already = "../../Suites/Fcredit";
    if (option==3 || option==4){
        string decision;
        int fixval=9;//atoi(fixedatr.c_str());
        ifstream fin;
        fin.open(already,ios::in);
        string line;
        vector<int> pos;
        vector<int> neg;
        if (fixval==9){
            for(int i=0;i<2;i++){
                pos.push_back(0);
                neg.push_back(0);
            }
           
        }
        else{
            for(int i=0;i<4;i++){
                pos.push_back(0);
                neg.push_back(0);
            }
        }
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
                if(inp.size()==20)
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
                pclose(in);
            }
            printinp+=decision;
            if (printout==1)
                fout<<printinp<<endl;
            currval = atoi(inp[fixval-1].c_str());
            if (decision.compare("1")==0)
                pos[currval] = pos[currval]+1;
            else{
                neg[currval] = neg[currval]+1;
            }
            
        }
        double min=1,max=0;
        for(int i=0;i<pos.size();i++){
            double v = pos[i]*1.0/(pos[i]+neg[i]);
            //cout<<v<<" "<<pos[i]<<" "<<neg[i]<<endl;
            if (v > max)
                max = v;
            if (v<min)
                min = v;
        }
        cout<<"Score is"<<100*(max-min)<<endl;
        
        return;
            
    }
   
    vector<int> fix_atr;// = []
    int num=1;
    for (int i=0;i< fixed.size();i++){
        if(fixed[i]==1){
            //cout<<i<<endl;
            num*= num_atr[i];
            fix_atr.push_back(i);
        }
    }//i in range(0,len(fixed)):
        
    int val = 0;
    
    double max = -1;
    double min = 100;

    while (val< num){
        string inp_fix[]={"","","","","","","","","","","","","","","","","","","",""};
        int i=(fix_atr.size())-1;
        int tmp_val = val;
        while(i>=0){
            inp_fix[fix_atr[i]] = to_string(tmp_val%num_atr[fix_atr[i]]);
            tmp_val = (tmp_val - tmp_val%num_atr[fix_atr[i]])/num_atr[fix_atr[i]];
            i-=1;
        }
        
        val+=1;
        string inp[]={"","","","","","","","","","","","","","","","","","","",""};
        int num_inp = 0;
        double pos = 0;
        double neg = 0;
        for (int i3=0;i3<maxinp;i3++){
            
            int j=0;
            while (j<(num_atr.size())){
                if (inp_fix[j]=="")
                    inp[j] = to_string((int)rand()%num_atr[j]);
                else
                    inp[j]=inp_fix[j];
                j+=1;
            }
            std::vector<string> inpstr( inp, inp+20 );
            string printinp;

             j=0;
             string testinp;
            while (j<inpstr.size()){
                printinp+=inpstr[j]+",";
                testinp+=inpstr[j]+" ";
                j++;
            }
            string cmd = "./test "+trainfile+" "+testinp;
            if(option==1){
                FILE *in;
                char buff[512];
                in = popen(cmd.c_str(), "r");
                while(fgets(buff, sizeof(buff), in)!=NULL){
                    decision = buff;
                    
                }
                pclose(in);

                //decision = system (cmd.c_str());
                //cout<<decision<<endl;
            }
            else
                decision =testDataOnDecisionTree(inpstr, root, tableInfo, defaultClass);

            if (printout==1)
                fout<<printinp<<decision<<endl;
            if (decision.compare("1")==0)
                pos++;
            else{
            //    cout<<decision<<endl;
                    neg++;
            }
            
            num_inp+=1;
           double frac = pos*1.0/(pos+neg);
        if (2.5*sqrt(frac*(1-frac)*1.0/(pos+neg)<0.05) && pos+neg>mininp)
        {
               break;
            }

        }
        if(pos*1.0/(pos+neg)>max)
            max = pos*1.0/(pos+neg);
        if(pos*1.0/(pos+neg)<min)
            min = pos*1.0/(pos+neg);
         
        
       // cout<<pos*1.0/(pos+neg)<<endl;
       
    }
     //cout<<(max-min)*100<<endl;
cout<<"output is in "<<outfile<<endl;
    

}
int main(int argc, const char *argv[])
{

    srand (14);
    trainfile = argv[1];    
	ifstream inputFile;												// Input file stream
	string singleInstance;											// Single line read from the input file 
	vvs dataTable;													// Input data in the form of a vector of vector of strings

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

    //cout<<"Learned"<<endl;
    
    
    
    //Generate a set of inputs for which we get 1 as the output
    //Then check a set of inputs which have 
    
    
    
  
    
    
//    Input :
    vector<string> inp,master_inp;
    //parse(singleInstance, dataTable);
    
    vector<int> list;
    for(int i=0;i<20;i++)
        list.push_back(i);
    
    //int fixedAtr = stoi(argv[2]);
    
    static const int arr[] = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};

    vector<int> fixed (arr, arr + sizeof(arr) / sizeof(arr[0]) );
    fixed[8] = 1;
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



