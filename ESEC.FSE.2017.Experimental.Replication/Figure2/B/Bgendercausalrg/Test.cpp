#include "header.h"
#include <algorithm>


int main(int argc, const char *argv[])
{
	ifstream inputFile;												// Input file stream
	string singleInstance;											// Single line read from the input file 
	vvs dataTable;													// Input data in the form of a vector of vector of strings
    	srand (9);
	inputFile.open(argv[1]);
	if (!inputFile)													// If input file does not exist, print error and exit
	{
		cerr << "Error: Training data file not found!" << endl;
		exit(-1);
	}

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
     std::vector<string> inpstr;
     for(int i=2;inpstr.size()<13;i++){
        inpstr.push_back((argv[i]));
     }
    
    cout<<atoi(testDataOnDecisionTree(inpstr, root, tableInfo, defaultClass).c_str());
  
   
    
    	return 0;
}



