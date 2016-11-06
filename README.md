# Themis

The code for Themis is present in reporsitory names Themis1.0/

##Structure of folder :

The Themis.py file has the class file defined for Themis and the different functions which can be used to test the software.

The main.py file is a helper which reads all the attributes and the command used to run the software from the settings file. It then parses the settings file to create an instance of Themis. Once this instance is created, any function called will test the given software.
——————————————————————————————————————————————————————

##The functions discussed in paper : 

Causal Discrimination : 
Takes the characteristic subset to be tested, the confidence and epsilon as input and outputs the causal discrimination score


Group Discrimination :
Takes the characteristic subset to be tested, the confidence and epsilon as input and outputs the group discrimination score


discriminationSearch :
Takes theta, confidence, epsilon and type as input.
    Theta is the threshold to test the discrimination of software i.e. if discriminations score > theta then the software discriminates.
    type = 0 means to test group discrimination
    type = 1 means to test causal discrimination

——————————————————————————————————————————————————————

##How to run :

1) Change the settings.txt file according to the input attributes according to the description of the file given below this section.

2) Add the function calls in the file main.py as per the need described below :

a) <i>Search the discriminating set of attributes</i> : The function discriminationSearch tests the software for discrimination and returns all characteristic subsets which have discrimination score more than the threshold.

Example input : software.discriminationSearch(0.1,99,0.1,0)
Here the threshold = 0.1, confidence needed = 99%, epsilon/error tolerated = 0.1 = 10%, type = 0 means to test group fairness

b) <i>Group discrimination </i>: The function to be called is groupDiscrimination which takes the following arguments :

A list X : which contains the attribute indices against which discrimination is tested, confidence and epsilon/error.

eg. X=[0,1] means the first and the second attributes are to be used. 
groupDiscrimination(X,99,0.1) will call this function with confidence = 99% and epsilon = 0.1

c) <i>Causal discrimination</i> : The function to be called is groupDiscrimination which takes the following arguments :

A list X : which contains the attribute indices against which discrimination is tested, confidence and epsilon/error.

eg. X=[0,1] means the first and the second attribute are to be used.

causalDiscrimination(X,99,0.1) will call this function with confidence = 99% and epsilon = 0.1

d) <i>Print the software details</i> : The function printSoftwareDetails prints the software details, the set of input attributes and the values it can take. It takes no arguments.

e) <i> Get the test suite</i> : The function getTestSuite can be called after running discrimination functions listes in (a) or (b) or (c) above to print the test suite which has been generated in the current of the code. It returns a list of various inputs which were used to test the software.


Note : Example usages have been commented in main.py for reference. Confidence can take values 80, 90, 95, 98 and 99%

——————————————————————————————————————————————————————

##Settings file (Input schema) description : 

It should contain the input schema which consists of the number of attributes, attribute description, software name and the command to run the software.

The first line contains the number of attributes the software takes.
It is followed by one attribute per line.
Each line for attribute contains the attribute number, followed by attribute name, followed by type of attribute followed by the values it can take. All these are separated by space.

The type of attribute will be categorical for the categorical variables eg. sex which takes values male or female.
If a variable can take continuous integers then the type will be continuousInt. It is represented by the start and end of the range of values 
eg. a line  "2 Age continuousInt 2,6" (ignore the ") means that Age can take continuous values starting from 2 till 6 i.e. {2,3,4,5,6}

The command should be given in a new line starting with "command:"

Themis assumes that a software can be run as "`<Command>` `<space separated input attributes>`"
eg. In the demo Themis calls the software as "python software.py Male 3 Red 4" which means that the input sex = Male, Age = 3, Race = Red and Income = 4 as per the specifications of settings file.

The name should also be given in a new line starting with "name:"



——————————————————————————————————————————————————————

##SAMPLE DEMO : 

You can run python main.py with settings.txt and software.py .

It will output the following :


Software Name is   SexistSoftware 

Number of attributes are  4 

Attribute name is  Sex
Number of values taken by this attribute = 2

The different values taken are  ['Male', 'Female'] 

Attribute name is  Age
Number of values taken by this attribute = 5

The different values taken are  [2, 3, 4, 5, 6] 

Attribute name is  Race
Number of values taken by this attribute = 4

The different values taken are  ['Red', 'Green', 'Yellow', 'Purple'] 

Attribute name is  Income
Number of values taken by this attribute = 4

The different values taken are  [2, 3, 4, 5] 




Themis has completed 

Software discriminates against  [['Sex'], ['Race']] 

——————————————————————————————————————————————————————

The output above shows the different attributes and the discriminating ones.


——————————————————————————————————————————————————————

##Code in subjectSystems folders : 

Different folders are : 

A = Code for [1] along with themis scripts running over it.

B = Folder contains the decision tree classifier code for [2] 

C,D  = Folder containing the code for [3] and [4] respectively.

fairness_unaware = Folder containing the code for fairness unaware algorithms. These algorithms correspond to systems E,G,H described in our paper.

    Here E = Naive Bayes
         F = Decision Tree
         G = Logistic Regression
         H = Support Vector Machine


##References 

[1] Muhammad Bilal Zafar, Isabel Valera, Manuel Gomez Rodriguez, and Krishna P Gummadi. Learning fair classifiers. CoRR, abs/1507.05259, 2015.

[2] Faisal Kamiran, Toon Calders, and Mykola Pechenizkiy. Discrimination aware decision tree learning. In International Conference on Data Mining (ICDM), pages 869–874, Sydney, Australia, December 2010.

[3] Toon Calders, Faisal Kamiran, and Mykola Pechenizkiy. Building classifiers with independency constraints. In Proceedings of the 2009 IEEE International Conference on Data Mining (ICDM) Workshops, pages 13–18, Miami, FL, USA, December 2009.

[4] Indre Zliobaite, Faisal Kamiran, and Toon Calders. Handling conditional discrimination. In International Conference on Data Mining (ICDM), pages 992–1001, Vancouver, BC, Canada, December 2011.