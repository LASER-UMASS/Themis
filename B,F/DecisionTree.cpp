#include "header.h"
#include <algorithm>

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
    vector<string> inp;
    string decision;
    for(int i3=0;i3<70;i3++){
        for(int i5=0;i5<7;i5++){
            for(int i6=0;i6<14;i6++){
                for(int i10=0;i10<100;i10++){
                    for(int i11=0;i11<40;i11++){
                        for(int i12=0;i12<100;i12++){
                            for(int i13=0;i13<41;i13++)  {
                                for(int i7=0;i7<6;i7++){
                                    for(int i4=0;i4<16;i4++){
                                        for(int i1=0;i1<10;i1++){
                                            for(int i2=0;i2<8;i2++){
                                                  for(int i9=0;i9<2;i9++){
                                                    for(int i8=0;i8<5;i8++) {
                                                        inp[0]=to_string(i1);
                                                        inp[1]=to_string(i2);
                                                        inp[2]=to_string(i3);
                                                        inp[3]=to_string(i4);
                                                        inp[4]=to_string(i5);
                                                        inp[5]=to_string(i6);
                                                        inp[6]=to_string(i7);
                                                        inp[7]=to_string(i8);//master_inp[7];
                                                        inp[8]=to_string(i9);
                                                        inp[9]=to_string(i10);
                                                        inp[10]=to_string(i11);
                                                        inp[11]=to_string(i12);
                                                        inp[12]=to_string(i13);
                                                        // if(inp!=master_inp)
                                                        {
                                                            decision =testDataOnDecisionTree(inp, root, tableInfo, defaultClass);
                                                            //if(decision.compare("1")==0)
                                                            {
                                                                //cout<<"Decision is "<<decision<<endl;
                                                                for(int j=0;j<(inp.size());j++)
                                                                cout<<inp[j]<<" ";
                                                                cout<<decision<<endl;
                                                            }
                                                            
                                                            
                                                        }
                                                        
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    

}
int main(int argc, const char *argv[])
{
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

    cout<<"Learned"<<endl;
    
    
    
    //Generate a set of inputs for which we get 1 as the output
    //Then check a set of inputs which have 
    
    
    
  
    
    
//    Input :
    vector<string> inp,master_inp;
    //parse(singleInstance, dataTable);
    for(int i=0;i<13;i++){
        //dataTable[1][1]="1";
        //cout<<dataTable[1][i]<<endl;
        inp.push_back(argv[i+2]);
    }
    cout<<"Testing"<<endl;
    string master_decision =testDataOnDecisionTree(inp, root, tableInfo, defaultClass);
    cout<<"Decision = "<<master_decision<<endl;
    
    //Now we need to check for discrimination against this input
    master_inp= inp;
    
    string decision;
    
    vector<int> attr;
    vector<string> val;
    for(int i=0;i<5;i++){
        inp[7]=to_string(i);
        if(inp[7]!=master_inp[7]){
            decision =testDataOnDecisionTree(inp, root, tableInfo, defaultClass);
            cout<<"Decision is "<<decision<<"Race = "<<inp[7]<<endl;
            
        }
        
    }
    vector<int> list;
    for(int i=0;i<13;i++)
        list.push_back(i);
    
    int i=0;
    while (i <13){

        vector< vector<int> > out = findsubsets(list,i+1);
        for (vector<int>a:out){
            vector<int> fixed;// = [0,0,0,0,0,0,0,0,0,0,0,0,0]
            for (int j = 0;j<13;j++){
                if (std::find(out.begin(), out.end(),j)!=out.end())
                    fixed.push_back(1);
                else
                    fixed.push_back(0);
            }
    
            check_ratio(fixed,root, tableInfo, defaultClass);
        }
        i++;
    }
    
    
    
    
    
    //Call check XX function and check adamant function
    //XX will change everything in race and see if the output changes
    //adamant will change everything other than the race and check of output changes
    //inp is an array of strings so convert int to strings...
    
    
    
   return 0;
	/*
	 * Decision tree testing phase
	 * In this phase, the testing is read
	 * from the file, parsed and stored.
	 * Each row in the table is made to
	 * traverse down the decision tree
	 * till a class label is found.
	 */
	inputFile.clear();
	inputFile.open(argv[2]); 										// Open test file
	if (!inputFile) 												// Exit if test file is not found
	{
		cerr << "Error: Testing data file not found!" << endl;
		exit(-1);
	}
	while (getline(inputFile, singleInstance)) 						// Store test data in a table
	{
		parse(singleInstance, dataTable);
	}
	vs predictedClassLabels;										// Stores the predicted class labels for each row
	vs givenClassLabels;											// Stores the given class labels in the test data
	for (int iii = 1; iii < dataTable.size(); iii++)				// Store given class labels in vector of strings named givenClassLabels
	{
		string data = dataTable[iii][dataTable[0].size()-1];
		givenClassLabels.push_back(data);
	}
    
    //vector<string> inp;
    for(int i=0;i<dataTable[1].size();i++){
        //dataTable[1][1]="1";
        cout<<dataTable[1][i]<<endl;
        inp.push_back(dataTable[1][i]);
    }
    cout<<testDataOnDecisionTree(inp, root, tableInfo, defaultClass);

    
	for (int iii = 1; iii < dataTable.size(); iii++)				// Predict class labels based on the decision tree
	{
        //cout<<"before"<<iii<<" "<<dataTable[iii][2]<<" " <<defaultClass<<endl;
		string someString = testDataOnDecisionTree(dataTable[iii], root, tableInfo, defaultClass);
        //cout<<"after"<<iii<<" "<<dataTable[iii][2]<<" " <<defaultClass<<endl;
        predictedClassLabels.push_back(someString);
	}
	dataTable.clear();

	/* Print output */
	ofstream outputFile;
	outputFile.open("decisionTreeOutput.txt", ios::app);
	outputFile << endl << "--------------------------------------------------" << endl;
	double accuracy = printPredictionsAndCalculateAccuracy(givenClassLabels, predictedClassLabels);			// calculate accuracy of classification
	outputFile << "Accuracy of decision tree classifier = " << accuracy << "%"; 							// Print out accuracy to console
	return 0;
}



float check_XX(){
    
}
