# README

Welcome to Themis&trade; 2.0 

### Installation

Make sure you have Anaconda 2 up and running. You will need the libraries that come with Anaconda (matplotlib, scikit-learn and numpy specifically). You will face issues if you don't have this. 

Once this is completed, simply run Themis&trade; by using the command:
python Themis.py settings.xml


### Explanation of Settings File

<name>Software</name>
    <command>python software.py</command>
    <seed>42</seed>
    <max_samples>200</max_samples>
    <min_samples>10</min_samples>

So here the name tag is the name of the software.
The command is the exact command that is used to execute the software. Please note that the software must be able to accept command line input and should print 0 and 1 as this is what Themis&trade; uses for evaluation. 


     <input>
            <name>Sex</name>
            <type>categorical</type>
            <values>
                <value>Male</value>
                <value>Female</value>
            </values>
        </input>

Here you specify the name of the input, its type and the values that you want it to take. Here we have used the data type categorical. Another input data type in continuousInt (very much like Integer in Java). Likewise, you can create as many input variables as you want, however, with more values, the more time Themis&trade; will take to complete.

### Confidence and Threshold values

Please note that extremely high confidence values wil lead to very high runtimes. As a matter of fact, a small run may take an entire day or two to   terminate if the confidence value is incredibly high.

Likewise, generous threshold values (i.e. not too high nor too low), will help give you a better idea about the extent of discrimination associated with the software under test. 




   [express]: <http://expressjs.com>
   [AngularJS]: <http://angularjs.org>
   [Gulp]: <http://gulpjs.com>

   [PlDb]: <https://github.com/joemccann/dillinger/tree/master/plugins/dropbox/README.md>
   [PlGh]: <https://github.com/joemccann/dillinger/tree/master/plugins/github/README.md>
   [PlGd]: <https://github.com/joemccann/dillinger/tree/master/plugins/googledrive/README.md>
   [PlOd]: <https://github.com/joemccann/dillinger/tree/master/plugins/onedrive/README.md>
   [PlMe]: <https://github.com/joemccann/dillinger/tree/master/plugins/medium/README.md>
   [PlGa]: <https://github.com/RahulHP/dillinger/blob/master/plugins/googleanalytics/README.md>
