# Themis

Themis is a testing-based approach for measuring discrimination in a software
system. For the best explanation of the underlying problem Themis solves,
Themis algorithms, and an evaluation of Themis, read our paper [Fairness
Testing: Testing Software for
Discrimination](http://people.cs.umass.edu/~brun/pubs/pubs/Galhotra17fse.pdf).
 This work won an ACM Distinguished Paper Award at the ESEC/FSE 2017
conference.

This repository contains Themis (in `Themis1.0/`), instructions for using
Themis (this `README.md`), and subject systems on which Themis has been
evaluated (`subjectSystems/`).

## Measuring Discrimination 

The Themis implementation measures two kinds of discrimination: group
discrimination and causal discrimination. 

Imagine you have a piece of software that determines if an applicant should
be given a loan, and you want to know if this software discriminates with
respect to race. (And let's say in this scenario, there are two races, green
and purple.) One way to measure discrimination is to ask "do the same
fractions of green and purple people get loans?" This measure is called
*group* discrimination, and has been identified in prior work, e.g., [2, 3,
4, 5], among others. Specifically, the group discrimination score is the
difference between the largest and smallest fraction of people who get a loan
in a set of groups of people (e.g., one group per race). If 40% of purple
people get a loan but only 30% of green people get a loan, the discrimination
score with respect to race is 40%-30%=10%.

However, group discrimination has two limitations. First, it may fail to
observe some discrimination. Suppose the software discriminates against half
the purple people, e.g., those who live in Greenville, but discriminates for
the other half of the purple people, e.g., those who live in Purpleville.
These two kinds of discrimination can cancel out and be hidden by the group
discrimination score. Second, software may circumvent discrimination
detection. For example, suppose the software recommends loans for a random
30% of the purple people, and the 30% of the green people who have the most
savings. Then the group discrimination score with respect to race will deem
the software perfectly fair, despite a clear discrepancy in how the
applications are processed based on race.

The *causal* discrimination score [1] addresses these shortcomings. Software
testing enables a unique opportunity to conduct causal experiments to
determine statistical causation between inputs and outputs. For example, it
is possible to execute the software on two individuals identical in every way
except race, and verify if changing the race causes a change in the output.
Causal discrimination says that to be fair with respect to a set of
characteristics, the software must produce the same output for every two
individuals who differ only in those characteristics. For example, the
software is fair with respect to race if for all pairs of individuals who are
identical in every way except for race, the software either gives both of
them or neither of them the loan. The fraction of people for whom software
causally discriminates is a measure of causal discrimination.

Measuring causal and group discrimination scores each have three input
parameters:

- set of input characteristics whose discrimination against to measure,
- the desired confidence,
- the allowable error bound for that confidence.

The `discriminationSearch` function has four inputs:

- theta: the desired discrimination threshold
- confidence: the desired confidence,
- epsilon: the allowable error bound for that confidence,
- discrimination type: "group" or "causal".

## Running Themis:

The main entry point for Themis is `main.py`. It allows the user to run
Themis via a configuration file (`settings.txt`). `main.py` uses `Themis.py`,
which implements most of the Themis functionality.

To run Themis, you need to do two things. You need to specify an input schema
file (`settings.txt`) and modify `main.py` to specify which discrimination
type to compute.

### Input schema

Themis needs to know how to run the software being tested and the input
schema for the legal inputs to the software. The `settings.txt` consists of
the number of input characteristics, a description for each characteristic,
and the command to run the software. Because Themis requires a particular
command-line format, it is likely to require writing a simple wrapper around
the software under test.

Refer to this example `settings.txt` file:
```
2
1 race categorical green purple
2 age continuousInt 18,120
command: python loan.py
name: LoanSoftware
```

The first line of `settings.txt` is the number of input characteristics the
software requires. It is followed by one attribute per line, each containing
space-separated number, name, type, and finally the set of possible
valuations.

The attribute number and name must be unique. There are two supported
characteristic types: categorical and continuousInt. Categorical types list
all possible characteristic valuations, whereas continuousInt types list the
(comma-separated) minimum and maximum integer values the characteristic can
take on.

Next, `settings.txt` includes the executable part of the command to run
the software, preceded by `command:`.

Finally, `settings.txt` includes a name for the software, preceded by
`name:`. Note that the name is not used in executing the software, but only
to refer to the software when reporting results.

Themis expects the software to run using the command executable, followed by
a combination of characteristic name and value pairs. For example, for the
above `settings.txt`, Themis may execute the following command:
```
python loan.py race green age 18
```
and expects a binary output in the form of 0 or 1 printed to standard output.  

### Specifying what to measure

There are two ways to use Themis. First, given a set of input characteristics
and an acceptable error bound, Themis can compute the *group* and the
*causal* discrimination scores for those input characteristics. Second, given
a discrimination threshold and an acceptable error bound, Themis can compute
all characteristics with respect to which the software discriminates at least
as much as the threshold. To make these computations, Themis generates test
suites.

To use Themis, one creates a `main.py` script  out of a combination of the following give commands:
```
causalDiscrimination(..)
groupDiscrimination(..)
discriminationSearch(..)
printSoftwareDetails()
getTestSuite
```
---

We now describe the five commands. 

* `causalDiscrimination(characteristics, confidence, errorBound)`

The `characteristics` argument is a list of comma-separated names of the
input characteristics whose causal discrimination is to be measured. 

The `confidence` and `errorBound` arguments are each a number between 0 and 1.

Example use: `causalDiscrimination({race, age}, 0.99, 0.01})` returns the
causal discrimination score with respect to race and age that is within 1% of
the true causal discrimination score with confidence at least 99%.

* `groupDiscrimination(characteristics, confidence, errorBound)`

The `characteristics` argument is a list of comma-separated names of the
input characteristics whose group discrimination is to be measured. 

The `confidence` and `errorBound` arguments are each a number between 0 and 1.

Example use: `groupDiscrimination({race, age}, 0.99, 0.01})` returns the
group discrimination score with respect to race and age that is within 1% of
the true group discrimination score with confidence at least 99%.

* `discriminationSearch(discriminationThreshold, confidence, errorBound, discriminationType)`

The `discriminationThreshold`, `confidence`, and `errorBound` arguments are
each a number between 0 and 1.

The `discriminationType` argument can be one of "causal", "group", or "causalandgroup".

Example use: `discriminationSearch(0.1, 0.99, 0.01, causalandgroup)` returns
all characteristics such that either the causal or the group discrimination
score is between 9% and 11% (10% \pm 1%) with confidence of at least 99%.

* `printSoftwareDetails()`

Prints the input scema for the software.

* `getTestSuite()`

Must be called after a discrimination-computing function
(`causalDiscrimination`, `groupDiscrimination`, or `discriminationSearch`).
Prints the test suite used for the last discrimination-computing function.  

## Subject systems

The evaluation in [1] applied Themis to eight subject software systems (named
A-H), trained in different ways, for a total of twenty different software
system instances. The `subjectSystems/` folder contains the code for these
subject systems.

`A/` Discrimination-aware logistic regression [2] and Themis wrapper scripts for executing it.

`B/` Discrimination-aware decision tree classifier [3] and Themis wrapper scripts for executing it.

`C/` Discrimination-aware naive Bayes classifier [4] and Themis wrapper scripts for executing it.

`D/` Discrimination-aware decision tree classifier [5] and Themis wrapper scripts for executing it.

`fairness_unaware/` Four standard discrimination-unaware classifiers,
described as systems E (naive Bayes), F (decision tree), G (logistic
regression), and H (support vector machines) in [1].

## Collaborators
<table border:none;>
<tr>
<td><a href='http://people.cs.umass.edu/~sainyam/'><img src='http://people.cs.umass.edu/~sainyam/sainyam.jpg' alt='Sainyam Galhotra' height='100' /><br/>Sainyam Galhotra</a></td>
<td><a href='http://people.cs.umass.edu/~brun/'><img src='http://people.cs.umass.edu/~brun/images/yuriy.jpg' alt='Yuriy Brun' height='100' /><br/>Yuriy Brun</a></td>
<td><center><a href='http://people.cs.umass.edu/~ameli/'><img src='http://people.cs.umass.edu/~ameli/images/alexandra.jpg' alt='Alexandra Meliou' height='100' /></center><br/><center>Alexandra Meliou</center></a></td>
</tr><!-- <tr>
<td></td>
<td>[Yuriy Brun](http://people.cs.umass.edu/~brun/)</td>
<td>[Alexandra Meliou](http://people.cs.umass.edu/~ameli/)</td>
</tr> --></table>

## Funding
This work is supported by the National Science Foundation under grants no.
CCF-1453474, IIS-1453543, and CNS-1744471.

## References 

[1] Sainyam Galhotra, Yuriy Brun, and Alexandra Meliou, Fairness Testing:
Testing Software for Discrimination, in European Software Engineering
Conference and ACM SIGSOFT Symposium on the Foundations of Software
Engineering (ESEC/FSE), pages 498-510, Paderborn, Germany, September 2017.

[2] Muhammad Bilal Zafar, Isabel Valera, Manuel Gomez Rodriguez, and Krishna
P. Gummadi. Learning fair classifiers. CoRR, abs/1507.05259, 2015.

[3] Faisal Kamiran, Toon Calders, and Mykola Pechenizkiy. Discrimination
aware decision tree learning. In International Conference on Data Mining
(ICDM), pages 869-874, Sydney, Australia, December 2010.

[4] Toon Calders, Faisal Kamiran, and Mykola Pechenizkiy. Building
classifiers with independency constraints. In Proceedings of the 2009 IEEE
International Conference on Data Mining (ICDM) Workshops, pages 13-18, Miami,
FL, USA, December 2009.

[5] Indre Zliobaite, Faisal Kamiran, and Toon Calders. Handling conditional
discrimination. In International Conference on Data Mining (ICDM), pages
992-1001, Vancouver, BC, Canada, December 2011.


