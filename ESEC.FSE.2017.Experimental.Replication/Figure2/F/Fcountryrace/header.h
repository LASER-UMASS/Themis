#include <iostream>
#include <string>
#include <vector>
#include <fstream>
#include <map>
#include <math.h>
#include <float.h>
#include <cstdlib>
#include <iomanip>

using namespace std;

typedef vector<string> vs;
typedef vector<vs> vvs;
typedef vector<int> vi;
typedef map<string, int> msi;
typedef vector<double> vd;

struct node													// struct node defines the structure of a node of the decision tree
{
	string splitOn;											// Stores which attribute to split on at a particular node
	string label;											// Stores the class label for leaf nodes. For nodes that are not leaf nodes, it stores the value of the attribute of the parent's' split 
	bool isLeaf;											// boolean flag for leaf nodes
	vector<string> childrenValues;							// Stores the values of the childrens' attributes
	vector<node*> children;									// Stores pointers to the children of a node
};

void parse(string&, vvs&);									// Parses a single line from the input file and stores the information into a vector of vector of strings 
void printAttributeTable(vvs&);								// For debugging purposes only. Prints a data table
vvs pruneTable(vvs&, string&, string);						// Prunes a table based on a column/attribute's name and the value of that attribute. Removes that column and all instances that have that value for that column
node* buildDecisionTree(vvs&, node*, vvs&);					// Builds the decision tree based on the table it is passed
bool isHomogeneous(vvs&);									// Returns true if all instances in a subtable at a node have the same class label
vi countDistinct(vvs&, int);								// Returns a vector of integers containing the counts of all the various values of an attribute/column
string decideSplittingColumn(vvs&);							// Returns the column on which to split on. Decision of column is based on entropy
int returnColumnIndex(string&, vvs&);						// Returns the index of a column in a subtable
bool tableIsEmpty(vvs&);									// Returns true if a subtable is empty
void printDecisionTree(node*);								// For degubbing purposes only. Recursively prints decision tree
string testDataOnDecisionTree(vs&, node*, vvs&, string);	// Runs a single instance of the test data through the decision tree. Returns the predicted class label
int returnIndexOfVector(vs&, string);						// Returns the index of a string in a vector of strings
double printPredictionsAndCalculateAccuracy(vs&, vs&);		// Outputs the predictions to file and returns the accuracy of the classification
vvs generateTableInfo(vvs &dataTable);						// Generates information about the table in a vector of vector of stings
string returnMostFrequentClass(vvs &dataTable);				// Returns the most frequent class from the training data. This class is used as the default class during the testing phase
