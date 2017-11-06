# Themis 2.0
#
# By: Rico Angell

from __future__ import division

import argparse
import subprocess
from itertools import chain, combinations, product
import math
import random
import scipy.stats as st
import xml.etree.ElementTree as ET


class Input:
    """
    Class to define an input characteristic to the software.

    Attributes
    ----------
    name : str
        Name of the input.
    values : list
        List of the possible values for this input.

    Methods
    -------
    get_random_input()
        Returns a random element from the values list.
    """
    def __init__(self, name="", values=[], kind="", ub=None, lb=None):
        try:
            self.name = name
            self.values = [str(v) for v in values]
            self.kind = kind
            if (ub != None and lb != None):
                self.lb = lb
                self.ub = ub
        except:
            print("Themis input initialization corrupted!")

    def get_random_input(self):
        """
        Return a random value from self.values
        """
        try:
            return random.choice(self.values)
        except:
            print("Error in get_random_input")

    def __str__(self):

        try:
            s = "\nInput\n"
            s += "-----\n"
            s += "Name: " + self.name + "\n"
            s += "Values: " + ", ".join(self.values)
            return s
        except:
            print("Issue with returning a string representation of the input")
    __repr__ = __str__


class Test:
    """
    Data structure for storing tests.

    Attributes
    ----------
    function : str
        Name of the function to call
    i_fields : list of `Input.name`
        The inputs of interest, i.e. compute the casual discrimination wrt
        these fields.
    threshold : float in [0,1]
        At least this level of discrimination to be considered.
    conf : float in [0, 1]
        The z* confidence level (percentage of normal distribution.
    margin : float in [0, 1]
        The margin of error for the confidence.
    group : bool
        Search for group discrimination if `True`.
    causal : bool
        Search for causal discrimination if `True`.
    """
    def __init__(self, function="", i_fields=[], conf=0.999, margin=0.0001,
                    group=False, causal=False, threshold=0.2):
        try:
            self.function = function
            self.i_fields = i_fields
            self.conf = conf
            self.margin = margin
            self.group = group
            self.causal = causal
            self.threshold = threshold
        except:
            print("Themis test initialization input")

    def __str__(self):
        try:

            s = "\n\n"

            # Alters output based on the test that was ran
            if self.function == "discrimination_search":
                s += "Ran discrimination search for: \n"
                if (self.group == True):
                    s += "Group Discrimination\n"
                if (self.causal == True):
                    s += "Causal Discrimination\n\n"
            elif self.function == "causal_discrimination":
                s += "Calculated causal discrimination for the following sets of inputs: \n (" + ", ".join(self.i_fields) + ") \n\n"
            elif self.function == "group_discrimination":
                s += "Calculated group discrimination for the following sets of inputs: \n (" + ", ".join(self.i_fields) + ") \n\n"

            s += "Discrimination result(s): \n"

                # This would be in extra window so may may separate function for this
                
##                s += "Threshold: " + str(self.threshold) + "\n"
##                s += "Group: " + str(self.group) + "\n"
##                s += "Causal: " + str(self.causal) + "\n"
##            s += "Confidence: " + str(self.conf) + "\n"
##            s += "Margin: " + str(self.margin) + "\n"
            return s
        except:
            print("Issue with returning a string version of the test details")
    __repr__ = __str__


class Themis:
    """
    Compute discrimination for a piece of software.

    Attributes
    ----------

    Methods
    -------
    """
    def __init__(self, xml_fname=""):
        """
        Initialize Themis from xml file.

        Parameters
        ----------
        xml_fname : string
            name of the xml file we want to import settings from.
        """

        assert xml_fname != ""
        try:
            tree = ET.parse(xml_fname)
            root = tree.getroot()

            self.max_samples = int(root.find("max_samples").text)
            self.min_samples = int(root.find("min_samples").text)
            self.rand_seed = int(root.find("seed").text)
            self.software_name = root.find("name").text
            self.command = root.find("command").text.strip()
            self._build_input_space(args=root.find("inputs"))
            self._load_tests(args=root.find("tests"))
            self._cache = {}
            self.extended_output = ""
            
        except:
            print("issue with reading xml file and initializing Themis")
    def run(self):
        """
        Run Themis tests specified in the configuration file.
        """
        try:
 
            # move these somewhere else - will not go in main output
            
##            print("SOFTWARE NAME: " + self.software_name)
##            print("MAX SAMPLES: " + str(self.max_samples))
##            print("MIN SAMPLES: " + str(self.min_samples))
##            print("COMMAND: " + self.command)
##            print ("RANDOM SEED: " + str(self.rand_seed))

            #key = inputs tuple
            # value = percentage from test execution
            self.group_tests = {}
            self.causal_tests = {}

            self.simple_discrim_output = ""
            self.detailed_discrim_output = ""

            
            for test in self.tests:
                random.seed(self.rand_seed)
                #print ("--------------------------------------------------")
                if test.function == "causal_discrimination":
                    suite, p = self.causal_discrimination(i_fields=test.i_fields,
                                                          conf=test.conf,
                                                          margin=test.margin)
                    # store tests for output strings
                    causal_key = tuple(test.i_fields)
                    self.causal_tests [causal_key] = "{:.1%}".format(p)
                    
##                    self.output += str(test)
##                    op = 'Your software discriminates on the above inputs ' + "{:.1%}".format(p) +  ' of the time.'
##                    self.output += op
                    
                elif test.function == "group_discrimination":
                    suite, p = self.group_discrimination(i_fields=test.i_fields,
                                                         conf=test.conf,
                                                         margin=test.margin)

                    # store tests for output strings
                    group_key = tuple(test.i_fields)
                    self.group_tests [group_key] = "{:.1%}".format(p)

                    #save min_group and max_group 
                                       
                elif test.function == "discrimination_search":
                    g, c = self.discrimination_search(threshold=test.threshold,
                                                      conf=test.conf,
                                                      margin=test.margin,
                                                      group=test.group,
                                                      causal=test.causal)
##                    self.simple_discrim_output += str(test) 
                    if g:
                        self.simple_discrim_output += "Group\n"
                        self.simple_discrim_output += "-------\n"
                        for key, value in g.items():
                            values = ", ".join(key) + " --> " + "{:.1%}".format(value) + "\n"
                            self.simple_discrim_output += values
                            self.simple_discrim_output += "\n"
                        self.simple_discrim_output += ""

                        print ("Discrimination search ran for group discrimination")
                        print (self.simple_discrim_output)

                        #store threshold in extended output
                        
                    if c:
                        self.simple_discrim_output += "Causal\n"
                        self.simple_discrim_output += "-------\n"
                        for key, value in c.items():
                            values = ", ".join(key) + " --> " + "{:.1%}".format(value) + "\n"
                            self.simple_discrim_output += values

                        print ("Discrimination search (causal discrimination)")
                        print ("Threshold: " + "{:.1%}".format(test.threshold) + "\n")                        
                        print (self.simple_discrim_output)

                    
            
            print ("Group Discrimination Tests: \n")
            for key,value in self.group_tests.items():
                print ('Input(s): ' + str(key) + '--->' + str(value) + "\n")

            print ("Causal Discrimination Tests: \n")
            for key, value in self.causal_tests.items():
                print ('Input(s): ' + str(key) + '--->' + str(value) + "\n")


            
            
                
            
            self.short_output = ""
            self.extended_output = ""
            
        except:
            print ("Issue in main Themis run")        

    def group_discrimination(self, i_fields=None, conf=0.999, margin=0.0001):
        """
        Compute the group discrimination for characteristics `i_fields`.

        Parameters
        ----------
        i_fields : list of `Input.name`
            The inputs of interest, i.e. compute the casual discrimination wrt
            these fields.
        conf : float in [0, 1]
            The z* confidence level (percentage of normal distribution.
        margin : float in [0, 1]
            The margin of error for the confidence.

        Returns
        -------
        tuple
            * list of dict
                The test suite used to compute group discrimination.
            * float
                The percentage of group discrimination
        """
        assert i_fields != None
        try:
            min_group, max_group, test_suite, p = float("inf"), 0, [], 0
            rand_fields = self._all_other_fields(i_fields)
            for fixed_sub_assign in self._gen_all_sub_inputs(args=i_fields):
                count = 0
                for num_sampled in range(1, self.max_samples):
                    assign = self._new_random_sub_input(args=rand_fields)
                    assign.update(fixed_sub_assign)
                    self._add_assignment(test_suite, assign)
                    count += self._get_test_result(assign=assign)

                    p, end = self._end_condition(count, num_sampled, conf, margin)
                    if end:
                        break

                min_group = min(min_group, p)
                max_group = max(max_group, p)

            return test_suite, (max_group - min_group)
        except:
            print("Issue in group_discrimination")

    def causal_discrimination(self, i_fields=None, conf=0.999, margin=0.0001):
        """
        Compute the causal discrimination for characteristics `i_fields`.

        Parameters
        ----------
        i_fields : list of `Input.name`
            The inputs of interest, i.e. compute the casual discrimination wrt
            these fields.
        conf : float in [0, 1]
            The z* confidence level (percentage of normal distribution.
        margin : float in [0, 1]
            The margin of error for the confidence.

        Returns
        -------
        tuple
            * list of dict
                The test suite used to compute causal discrimination.
            * float
                The percentage of causal discrimination.
        """
        try:
            assert i_fields != None
            count, test_suite, p = 0, [], 0
            f_fields = self._all_other_fields(i_fields) # fixed fields
            for num_sampled in range(1, self.max_samples):
                fixed_assign = self._new_random_sub_input(args=f_fields)
                singular_assign = self._new_random_sub_input(args=i_fields)
                assign = self._merge_assignments(fixed_assign, singular_assign)
                self._add_assignment(test_suite, assign)
                result = self._get_test_result(assign=assign)
                for dyn_sub_assign in self._gen_all_sub_inputs(args=i_fields):
                    if dyn_sub_assign == singular_assign:
                        continue
                    assign.update(dyn_sub_assign)
                    self._add_assignment(test_suite, assign)
                    if self._get_test_result(assign=assign) != result:
                        count += 1
                        break

                p, end = self._end_condition(count, num_sampled, conf, margin)
                if end:
                    break

            return test_suite, p
        except:
            print("Issue in causal discrimination")

    def discrimination_search(self, threshold=0.2, conf=0.99, margin=0.01,
                              group=False, causal=False):
        """
        Find all minimall subsets of characteristics that discriminate.

        Choose to search by group or causally and set a threshold for
        discrimination.

        Parameters
        ----------
        threshold : float in [0,1]
            At least this level of discrimination to be considered.
        conf : float in [0, 1]
            The z* confidence level (percentage of normal distribution.
        margin : float in [0, 1]
            The margin of error for the confidence.
        group : bool
            Search for group discrimination if `True`.
        causal : bool
            Search for causal discrimination if `True`.

        Returns
        -------
        tuple of dict
            The lists of subsets of the input characteristics that discriminate.
        """
        try:
            assert group or causal
            group_d_scores, causal_d_scores = {}, {}
            for sub in self._all_relevant_subs(self.input_order):
                if self._supset(list(set(group_d_scores.keys())|
                                     set(causal_d_scores.keys())), sub):
                    continue
                if group:
                    _, p = self.group_discrimination(i_fields=sub, conf=conf,
                                                       margin=margin)
                    if p > threshold:
                        group_d_scores[sub] = p
                if causal:
                    _, p = self.causal_discrimination(i_fields=sub, conf=conf,
                                                       margin=margin)
                    if p > threshold:
                        causal_d_scores[sub] = p

            return group_d_scores, causal_d_scores
        except:
            print("Issue in trying to search for discrimination")

    def _all_relevant_subs(self, xs):
        try:
            return chain.from_iterable(combinations(xs, n) \
                                        for n in range(1, len(xs)))
        except:
            print("Issue in returning reltive ssubsets. Possible a divide by zer error")

    def _supset(self, list_of_small, big):
       try:
            for small in list_of_small:
                next_subset = False
                for x in small:
                    if x not in big:
                        next_subset = True
                        break
                if not next_subset:
                    return True
       except:
           print("Issue in finding superset")

    def _new_random_sub_input(self, args=[]):
        try:
            assert args
            return {name : self.inputs[name].get_random_input() for name in args}
        except:
            print("Issue in getting a random subset")

    def _gen_all_sub_inputs(self, args=[]):
        assert args
        try:
            vals_of_args = [self.inputs[arg].values for arg in args]
            combos = [list(elt) for elt in list(product(*vals_of_args))]
            return ({arg : elt[idx] for idx, arg in enumerate(args)} \
                                                 for elt in combos)
        except:
            print("Issue in generate all")

    def _get_test_result(self, assign=None):
        assert assign != None
        try:
            tupled_args = self._tuple(assign)
            if tupled_args in self._cache.keys():
                return self._cache[tupled_args]

            cmd = self.command + " " + " ".join(tupled_args)
            output = subprocess.getoutput(cmd).strip()
            self._cache[tupled_args] = (subprocess.getoutput(cmd).strip() == "1")
            return self._cache[tupled_args]
        except:
            print("Issue in getting the results of the tests")

    def _add_assignment(self, test_suite, assign):
        try:

            if assign not in test_suite:
                test_suite.append(assign)
        except:
            print("Issue in assigining to the test_suite")

    def _all_other_fields(self, i_fields):
        try:
            return [f for f in self.input_order if f not in i_fields]
        except:
            print("Issue in _all_other_fields")


    def _end_condition(self, count, num_sampled, conf, margin):
        try:
            p = 0
            if num_sampled > self.min_samples:
                p = count / num_sampled
                error = st.norm.ppf(conf)*math.sqrt((p*(1-p))/num_sampled)
                return p, error < margin
            return p, False
        except:
            print("Issue in _end_condition. Possibly a divide by zero error")

    def _merge_assignments(self, assign1, assign2):
        try:
            merged = {}
            merged.update(assign1)
            merged.update(assign2)
            return merged
        except:
            print("Issue in merging assigments")

    def _tuple(self, assign=None):
        try:
            assert assign != None
            return tuple(str(assign[name]) for name in self.input_order)
        except:
            print("Issue in generating tuples for tests")

    def _untuple(self, tupled_args=None):
        assert tupled_args != None
        try:
            listed_args = list(tupled_args)
            return {name : listed_args[idx] \
                        for idx, name in enumerate(self.input_order)}
        except:
            print("Issue in untupling")

    def _build_input_space(self, args=None):
        assert args != None
        try:
            self.inputs = {}
            self.input_order = []
            self.input_names = []
            for obj in args.findall("input"):
                name = obj.find("name").text
                self.input_names.append(name)
                values = []
                t = obj.find("type").text
                if t == "categorical":
                    values = [elt.text
                                for elt in obj.find("values").findall("value")]

                    self.inputs[name] = Input(name=name, values=values, kind="categorical")
                elif t == "continuousInt":
                    lowerbound = int(obj.find("bounds").find("lowerbound").text)
                    upperbound = int(obj.find("bounds").find("upperbound").text)+1
                    
                    values = range(int(obj.find("bounds").find("lowerbound").text),
                                 int(obj.find("bounds").find("upperbound").text)+1)

                    

                    self.inputs[name] = Input(name=name, values=values, kind="continuousInt", lb = str(lowerbound), ub = str(upperbound))
                else:
                    assert False

                self.input_order.append(name)
        except:
            print("Issue in building the input space/scope. Major problem")

    def _load_tests(self, args=None):
        assert args != None
        try:
            self.tests = []
            for obj in args.findall("test"):
                test = Test()
                test.function = obj.find("function").text
                if test.function == "causal_discrimination" or \
                   test.function == "group_discrimination":
                    test.i_fields = [elt.text
                            for elt in obj.find("i_fields").findall("input_name")]
                if test.function == "discrimination_search":
                    test.group = bool(obj.findall("group"))
                    test.causal = bool(obj.findall("causal"))
                    test.threshold = float(obj.find("threshold").text)
                test.conf = float(obj.find("conf").text)
                test.margin = float(obj.find("margin").text)
                self.tests.append(test)
        except:
            print("Issue in loading the tests")

if __name__ == '__main__':
    try:
        parser = argparse.ArgumentParser(description="Run Themis.")
        parser.add_argument("XML_FILE", type=str, nargs=1,
                                help="XML configuration file")
        args = parser.parse_args()
        t = Themis(xml_fname=args.XML_FILE[0])
        t.run()
    except:
        print("Issue in the main call to Themis i.e. Driver")
