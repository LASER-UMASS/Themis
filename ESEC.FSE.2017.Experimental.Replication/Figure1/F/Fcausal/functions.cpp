#include "header.h"

/* 
 * Parses a string and stores data
 * into a vector of vector of strings
 */
void parse(string& someString, vvs &attributeTable)
{
	int attributeCount = 0;
	vs vectorOfStrings;
	while (someString.length() != 0 && someString.find(',') != string::npos)
	{
		size_t pos;
		string singleAttribute;
		pos = someString.find_first_of(',');
		singleAttribute = someString.substr(0, pos);
		vectorOfStrings.push_back(singleAttribute);
		someString.erase(0, pos+1);
	}
	vectorOfStrings.push_back(someString);
	attributeTable.push_back(vectorOfStrings);
	vectorOfStrings.clear();
}

/*
 * Prints a vector of vector of strings
 * For debugging purposes only.
 */
void printAttributeTable(vvs &attributeTable)
{
	int inner, outer;
	for (outer = 0; outer < attributeTable.size(); outer++) {
		for (inner = 0; inner < attributeTable[outer].size(); inner++) {
			cout << attributeTable[outer][inner] << "\t";
		}
		cout << endl;
	}
}

/*
 * Prunes a table based on a column/attribute's name
 * and value of that attribute. Removes that column
 * and all rows that have that value for that column.
 */
vvs pruneTable(vvs &attributeTable, string &colName, string value)
{
	int iii, jjj;
	vvs prunedTable;
	int column = -1;
	vs headerRow;
	for (iii = 0; iii < attributeTable[0].size(); iii++) {
		if (attributeTable[0][iii] == colName) {
			column = iii;
			break;
		}
	}
	for (iii = 0; iii < attributeTable[0].size(); iii++) {
		 if (iii != column) {
		 	headerRow.push_back(attributeTable[0][iii]);
		 }
	}
	prunedTable.push_back(headerRow);
	for (iii = 0; iii < attributeTable.size(); iii++) {
		vs auxRow;
		if (attributeTable[iii][column] == value) {
			for (jjj = 0; jjj < attributeTable[iii].size(); jjj++) {
				if(jjj != column) {
					auxRow.push_back(attributeTable[iii][jjj]);
				}
			}
			prunedTable.push_back(auxRow);
		}
	}
	return prunedTable;
}

/*
 * Recursively builds the decision tree based on
 * the data that it is passed and tha table info.
 */
node* buildDecisionTree(vvs &table, node* nodePtr, vvs &tableInfo)
{
	if (tableIsEmpty(table)) {
		return NULL;
	}
    if(table[0].size()==0)
        return NULL;
//    cout<<"Building tree"<<table.size()<<" "<<table[0].size()<<" "<<table[1].size()<<endl;
	if (isHomogeneous(table)) {
		nodePtr->isLeaf = true;
		nodePtr->label = table[1][table[1].size()-1];
		return nodePtr;
	} else {
		string splittingCol = decideSplittingColumn(table);
		nodePtr->splitOn = splittingCol;
		int colIndex = returnColumnIndex(splittingCol, tableInfo);
		int iii;
		for (iii = 1; iii < tableInfo[colIndex].size(); iii++) {
			node* newNode = (node*) new node;
			newNode->label = tableInfo[colIndex][iii];
			nodePtr->childrenValues.push_back(tableInfo[colIndex][iii]);
			newNode->isLeaf = false;
			newNode->splitOn = splittingCol;
			vvs auxTable = pruneTable(table, splittingCol, tableInfo[colIndex][iii]);
			nodePtr->children.push_back(buildDecisionTree(auxTable, newNode, tableInfo));
		}
	}
	return nodePtr;
}

/*
 * Returns true if all rows in a subtable
 * have the same class label.
 * This means that that node's class label
 * has been decided.
 */
bool isHomogeneous(vvs &table)
{

	int iii;
    if(table.size()<=1){
        return false;
    }

	int lastCol = table[0].size() - 1;
    if(lastCol==-1){
        return false;
    }

	string firstValue = table[1][lastCol];

	for (iii = 1; iii < table.size(); iii++) {
		if (firstValue != table[iii][lastCol]) {
                //cout<<"exit"<<endl;
			return false;
		}
	}
   // cout<<"exit"<<endl;
	return true;
}

/*
 * Returns a vector of integers containing the counts
 * of all the various values of an attribute/column.
 */
vi countDistinct(vvs &table, int column)
{
	vs vectorOfStrings;
	vi counts;
	bool found = false;
	int foundIndex;
	for (int iii = 1; iii < table.size(); iii++) {
		for (int jjj = 0; jjj < vectorOfStrings.size(); jjj++) {
			if (vectorOfStrings[jjj] == table[iii][column]) {
				found = true;
				foundIndex = jjj;
				break;
			} else {
				found = false;
			}
		}
		if (!found) {
			counts.push_back(1);
			vectorOfStrings.push_back(table[iii][column]);
		} else {
			counts[foundIndex]++;
		}
	}
	int sum = 0;
	for (int iii = 0; iii < counts.size(); iii++) {
		sum += counts[iii];
	}
	counts.push_back(sum);
	return counts;
}

/*
 * Decides which column to split on
 * based on entropy. Returns the column
 * with the least entropy.
 */
string decideSplittingColumn(vvs &table)
{
	int column, iii;
	double minEntropy = DBL_MAX;
	int splittingColumn = 0;
	vi entropies;
	bool tie = false;
	for (column = 0; column < table[0].size() - 1; column++) {
		string colName = table[0][column];
		msi tempMap;
		vi counts = countDistinct(table, column);
		vd attributeEntropy;
		double columnEntropy = 0.0;
		for (iii = 1; iii < table.size()-1; iii++) {
			double entropy = 0.0;
			if (tempMap.find(table[iii][column]) != tempMap.end()) { 	// IF ATTRIBUTE IS ALREADY FOUND IN A COLUMN, UPDATE IT'S FREQUENCY
				tempMap[table[iii][column]]++;
			} else { 							// IF ATTRIBUTE IS FOUND FOR THE FIRST TIME IN A COLUMN, THEN PROCESS IT AND CALCULATE IT'S ENTROPY
				tempMap[table[iii][column]] = 1;
				vvs tempTable = pruneTable(table, colName, table[iii][column]);
                int num;
                //cout<<tempTable[0].size()<<endl;
                std::string race ("i");
                if(tempTable[0].size()>0){
                 num = -1;
                    for(int i=0;i<tempTable[0].size();i++){
                        if(tempTable[0][i].compare(race)==0){
                            //num = i;
                            break;
                        }
                            
                        //cout<<"ith is "<<i<<" "<<tempTable[0][i]<<endl;
                    }
                    if(num==-1)
                        num = 0;
                    //cout<<"value "<<tempTable[0][7]<<endl;
                }
                else
                    num =tempTable[0].size()-1;
				vi classCounts = countDistinct(tempTable, num);
				int jjj, kkk;
				for (jjj = 0; jjj < classCounts.size(); jjj++) {
					double temp = (double) classCounts[jjj];
					entropy -= (temp/classCounts[classCounts.size()-1])*(log(temp/classCounts[classCounts.size()-1]) / log(2));
				}
				if(tie){
					attributeEntropy.push_back((double) rand() / (RAND_MAX));	
					continue;
				}
				attributeEntropy.push_back(entropy);
				entropy = 0.0;
			}
		}
		
		for (iii = 0; iii < counts.size() - 1; iii++) {
			columnEntropy += ((double) counts[iii] * (double) attributeEntropy[iii]);
		}
		columnEntropy = columnEntropy / ((double) counts[counts.size() - 1]);
		if (columnEntropy <= minEntropy) {
			minEntropy = columnEntropy;
			splittingColumn = column;
		}
	}
	return table[0][splittingColumn];
}

/*
 * Returns an integer which is the
 * index of a column passed as a string
 */
int returnColumnIndex(string &columnName, vvs &tableInfo)
{
	int iii;
	for (iii = 0; iii < tableInfo.size(); iii++) {
		if (tableInfo[iii][0] == columnName) {
			return iii;
		}
	}
	return -1;
}

/*
 * Returns true if the table is empty
 * returns false otherwise
 */
bool tableIsEmpty(vvs &table)
{
	return (table.size() == 1);
}

/*
 * Recursively prints the decision tree
 * For debugging purposes only
 */
void printDecisionTree(node* nodePtr)
{
	if(nodePtr == NULL) {
		return;
	}
	if (!nodePtr->children.empty()) {
		cout << " Value: " << nodePtr->label << endl;
		cout << "Split on: " << nodePtr->splitOn;
		int iii;
		for (iii = 0; iii < nodePtr->children.size(); iii++) {   
			cout << "\t";
			printDecisionTree(nodePtr->children[iii]);
		}
		return;
        } else {
		cout << "Predicted class = " << nodePtr->label;
		return;
	}
}

/*
 * Takes a row and traverses that row through
 * the decision tree to find out the 
 * predicted class label. If none is found
 * returns the default class label which is
 * the class label with the highest frequency.
 */
string testDataOnDecisionTree(vs &singleLine, node* nodePtr, vvs &tableInfo, string defaultClass)
{
	string prediction;
	while (!nodePtr->isLeaf && !nodePtr->children.empty()) {
		int index = returnColumnIndex(nodePtr->splitOn, tableInfo);
		string value = singleLine[index];
     
		int childIndex = returnIndexOfVector(nodePtr->childrenValues, value);
     
		nodePtr = nodePtr->children[childIndex];
        //cout<<childIndex<<"FDSF"<<defaultClass<<endl;
		if (nodePtr == NULL || childIndex==-1) {
			prediction = defaultClass;
            //cout<<"HERE   vdf"<<defaultClass<<endl;
            return defaultClass;
            break;
		}
		prediction = nodePtr->label;
	}
	return prediction;
}

/*
 * Returns an integer which is the index
 * of a string in a vector of strings
 */
int returnIndexOfVector(vs &stringVector, string value)
{
	int iii;
	for (iii = 0; iii < stringVector.size(); iii++) {
		if (stringVector[iii] == value)	{
			return iii;
		}
	}
	return -1;
}

/*
 * Outputs the predictions to file
 * and returns the accuracy of the classification
 */
double printPredictionsAndCalculateAccuracy(vs &givenData, vs &predictions)
{
	ofstream outputFile;
	outputFile.open("decisionTreeOutput.txt");
	int correct = 0;
	outputFile << setw(3) << "#" << setw(16) << "Given Class" << setw(31) << right << "Predicted Class" << endl;
	outputFile << "--------------------------------------------------" << endl;
	for (int iii = 0; iii < givenData.size(); iii++) {
		outputFile << setw(3) << iii+1 << setw(16) << givenData[iii];
		if (givenData[iii] == predictions[iii]) {
			correct++;
			outputFile << "  ------------  ";
		} else {
			outputFile << "  xxxxxxxxxxxx  ";
		}
		outputFile << predictions[iii] << endl;
	}
	outputFile << "--------------------------------------------------" << endl;
	outputFile << "Total number of instances in test data = " << givenData.size() << endl;
	outputFile << "Number of correctly predicted instances = " << correct << endl;
	outputFile.close();
	return (double) correct/16281 * 100;
}

/*
 * Returns a vvs which contains information about
 * the data table. The vvs contains the names of
 * all the columns and the values that each
 * column can take
 */
vvs generateTableInfo(vvs &dataTable)
{
	vvs tableInfo;
	for (int iii = 0; iii < dataTable[0].size(); iii++) {
		vs tempInfo;
		msi tempMap;
		for (int jjj = 0; jjj < dataTable.size(); jjj++) {
			if (tempMap.count(dataTable[jjj][iii]) == 0) {
				tempMap[dataTable[jjj][iii]] = 1;
				tempInfo.push_back(dataTable[jjj][iii]);
			} else	{
				tempMap[dataTable[jjj][iii]]++;
			}
		}
		tableInfo.push_back(tempInfo);
	}
	return tableInfo;
}

/*
 * Returns the most frequent class from the training data
 * This class will be used as the default class label
 */
string returnMostFrequentClass(vvs &dataTable)
{
	msi trainingClasses;           													 // Stores the classlabels and their frequency
	for (int iii = 1; iii < dataTable.size(); iii++) {
		if (trainingClasses.count(dataTable[iii][dataTable[0].size()-1]) == 0) {
			trainingClasses[dataTable[iii][dataTable[0].size()-1]] = 1;
		} else {
			trainingClasses[dataTable[iii][dataTable[0].size()-1]]++;
		}
	}   
	msi::iterator mapIter;
	int highestClassCount = 0;
	string mostFrequentClass;
	for (mapIter = trainingClasses.begin(); mapIter != trainingClasses.end(); mapIter++) {
		if (mapIter->second >= highestClassCount) {
			highestClassCount = mapIter->second;
			mostFrequentClass = mapIter->first;
		}   
	}
	return mostFrequentClass;
}
