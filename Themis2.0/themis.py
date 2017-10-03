# Themis 2.0
#
# By: Rico Angell

from __future__ import division

import argparse
import commands
from itertools import chain, combinations, product
import math
import random
import scipy.stats as st
import xml.etree.ElementTree as ET


class Input:
    """
    """
    def __init__(self, name="", values=[]):
        self.name = name
        self.values = [str(v) for v in values]
        self.num_values = len(values)

    def get_random_input(self):
        """
        Return a random value from self.values
        """
        return random.choice(self.values)

    def __str__(self):
        s = "\nInput\n"
        s += "-----\n"
        s += "Name: " + self.name + "\n"
        s += "Values: " + ", ".join(self.values)
        return s

    __repr__ = __str__


class Test:
    """
    """
    def __init__(self, function="", i_fields=[], conf=0.999, margin=0.0001,
                    group=False, causal=False, threshold=0.2):
        self.function = function
        self.i_fields = i_fields
        self.conf = conf
        self.margin = margin
        self.group = group
        self.causal = causal
        self.threshold = threshold

    def __str__(self):
        s = "Test: " + self.function + "\n"
        s += "Characteristics: " + ", ".join(self.i_fields) + "\n"
        s += "Confidence: " + str(self.conf) + "\n"
        s += "Margin: " + str(self.margin) + "\n"
        s += "Threshold: " + str(self.threshold) + "\n"
        s += "Group: " + str(self.group) + "\n"
        s += "Causal: " + str(self.causal) + "\n"
        return s

    __repr__ = __str__


class Themis:
    """
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

        tree = ET.parse(xml_fname)
        root = tree.getroot()

        self.max_samples = int(root.find("max_samples").text)
        self.min_samples = int(root.find("min_samples").text)
        self.rand_seed = int(root.find("seed").text)
        self.software_name = root.find("name").text
        self.command = root.find("command").text.strip()
        self._build_input_space(args=root.find("inputs"))
        self._load_tests(args=root.find("tests"))
        self.cache = {}

    def _build_input_space(self, args=None):
        assert args != None
        self.inputs = {}
        self.input_order = []
        for obj in args.findall("input"):
            name = obj.find("name").text
            values = []
            t = obj.find("type").text
            if t == "categorical":
                values = [elt.text for elt in obj.find("values").findall("value")]
            elif t == "continuousInt":
                values = range(int(obj.find("bounds").find("lowerbound").text),
                               int(obj.find("bounds").find("upperbound").text)+1)
            else:
                assert false
            self.inputs[name] = Input(name=name, values=values)
            self.input_order.append(name)

    def _load_tests(self, args=None):
        assert args != None
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

    def run(self):
        """
        Run Themis given the configuration.
        """
        for test in self.tests:
            random.seed(self.rand_seed)
            print "--------------------------------------------------"
            if test.function == "causal_discrimination":
                suite, p = self.causal_discrimination(i_fields=test.i_fields,
                                                      conf=test.conf,
                                                      margin=test.margin)
                print test
                print "Score: ", p
            elif test.function == "group_discrimination":
                suite, p = self.group_discrimination(i_fields=test.i_fields,
                                                     conf=test.conf,
                                                     margin=test.margin)
                print test
                print "Score: ", p
            elif test.function == "discrimination_search":
                g, c = self.discrimination_search(threshold=test.threshold,
                                                  conf=test.conf,
                                                  margin=test.margin,
                                                  group=test.group,
                                                  causal=test.causal)
                print test
                if g:
                    print "Group"
                    print "-----"
                    for key, value in g.items():
                        print ", ".join(key) + " --> " + str(value)
                print "\n"
                if c:
                    print "Causal"
                    print "------"
                    for key, value in c.items():
                        print ", ".join(key) + " --> " + str(value)
        print "--------------------------------------------------"

    def new_random_sub_input(self, args=[]):
        """
        Produce a random input for the each of the elements of `attributes`.

        Parameters
        ----------
        attributes : list of str
            list of attribute names that we want random assignments to.

        Returns
        -------
        dict
            the assigns to each of the attributes.
        """
        assert args
        return {name : self.inputs[name].get_random_input() for name in args}

    def gen_all_sub_inputs(self, args=[]):
        """
        Produces a list of all possible assignments.

        Parameters
        ----------
        attributes : list of str
            list of attribute names that we want random assignments to.

        Returns
        -------
        generator of dict
            generator for the assignments.
        """
        assert args
        vals_of_args = [self.inputs[arg].values for arg in args]
        combos = [list(elt) for elt in list(product(*vals_of_args))]
        return ({arg : elt[idx] for idx, arg in enumerate(args)} \
                                                 for elt in combos)

    def get_test_result(self, assign=None):
        """
        Returns the output of the software with input `assign`.

        Checks the cache to see if the output for `assign` has already
        been computed. If it has, return the cached results. Otherwise, run
        the software and cache and return the result.

        Parameters
        ----------
        assign : dict
            dictionary of all input assignments.

        Returns
        -------
        bool
            the output of the software.
        """
        assert assign != None
        tupled_args = self._tuple(assign)
        if tupled_args in self.cache.keys():
            return self.cache[tupled_args]

        cmd = self.command + " " + " ".join(tupled_args)
        output = commands.getoutput(cmd).strip()
        self.cache[tupled_args] = (commands.getoutput(cmd).strip() == "1")
        return self.cache[tupled_args]

    def group_discrimination(self, i_fields=None, conf=0.999, margin=0.0001):
        """
        Compute the group discrimination for characteristics `i_fields`.

        Parameters
        ----------
        i_fields : list of `Input.name`
            The inputs of interest, i.e. compute the casual discrimination wrt
            these fiels.
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
        min_group, max_group, test_suite, p = float("inf"), 0, [], 0
        rand_fields = self._all_other_fields(i_fields)
        for fixed_sub_assign in self.gen_all_sub_inputs(args=i_fields):
            count = 0
            for num_sampled in range(1, self.max_samples):
                assign = self.new_random_sub_input(args=rand_fields)
                assign.update(fixed_sub_assign)
                self._add_assignment(test_suite, assign)
                count += self.get_test_result(assign=assign)

                p, end = self._end_condition(count, num_sampled, conf, margin)
                if end:
                    break

            min_group = min(min_group, p)
            max_group = max(max_group, p)

        return test_suite, (max_group - min_group)

    def causal_discrimination(self, i_fields=None, conf=0.999, margin=0.0001):
        """
        Compute the causal discrimination for characteristics `i_fields`.

        Parameters
        ----------
        i_fields : list of `Input.name`
            The inputs of interest, i.e. compute the casual discrimination wrt
            these fiels.
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
        assert i_fields != None
        count, test_suite, p = 0, [], 0
        f_fields = self._all_other_fields(i_fields) # fixed fields
        for num_sampled in range(1, self.max_samples):
            fixed_assign = self.new_random_sub_input(args=f_fields)
            singular_assign = self.new_random_sub_input(args=i_fields)
            assign = self._merge_assignments(fixed_assign, singular_assign)
            self._add_assignment(test_suite, assign)
            result = self.get_test_result(assign=assign)
            for dyn_sub_assign in self.gen_all_sub_inputs(args=i_fields):
                if dyn_sub_assign == singular_assign:
                    continue
                assign.update(dyn_sub_assign)
                self._add_assignment(test_suite, assign)
                if self.get_test_result(assign=assign) != result:
                    count += 1
                    break

            p, end = self._end_condition(count, num_sampled, conf, margin)
            if end:
                break

        return test_suite, p

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
        tuple of list of list - (group_subsets, causal_subsets)
            The lists of subsets of the input characteristics that discriminate.
        """
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

    def _all_relevant_subs(self, xs):
        return chain.from_iterable(combinations(xs, n) \
                                            for n in range(1, len(xs)))

    def _supset(self, list_of_small, big):
        for small in list_of_small:
            next_subset = False
            for x in small:
                if x not in big:
                    next_subset = True
                    break
            if not next_subset:
                return True

    def _add_assignment(self, test_suite, assign):
        if assign not in test_suite:
            test_suite.append(assign)

    def _all_other_fields(self, i_fields):
        return [f for f in self.input_order if f not in i_fields]

    def _end_condition(self, count, num_sampled, conf, margin):
        p = 0
        if num_sampled > self.min_samples:
            p = count / num_sampled
            error = st.norm.ppf(conf)*math.sqrt((p*(1-p))/num_sampled)
            return p, error < margin
        return p, False

    def _merge_assignments(self, assign1, assign2):
        merged = {}
        merged.update(assign1)
        merged.update(assign2)
        return merged
        return False

    def _tuple(self, assign=None):
        assert assign != None
        return tuple(str(assign[name]) for name in self.input_order)

    def _untuple(self, tupled_args=None):
        assert tupled_args != None
        listed_args = list(tupled_args)
        return {name : listed_args[idx] \
                    for idx, name in enumerate(self.input_order)}


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description="Run Themis.")
    parser.add_argument("XML_FILE", type=str, nargs=1,
                            help="XML configuration file")
    args = parser.parse_args()
    t = Themis(xml_fname=args.XML_FILE[0])
    t.run()
