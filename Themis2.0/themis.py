# Themis2.0 main class
#
# By: Rico Angell

from __future__ import division

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

        random.seed(int(root.find("seed").text))

        self.max_samples = 200
        self.min_samples = 10
        self.software_name = root.find("name").text
        self.command = root.find("command").text.strip()
        self._build_input_space(args=root.find("inputs"))
        self.cache = {}
        # TODO: Add parsing to run functions user requests

    def _build_input_space(self, args=None):
        assert args != None
        self.inputs = {}
        self.input_order = []
        for uid, obj in enumerate(args.findall("input")):
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

    def _tuple(self, assign=None):
        assert assign != None
        return tuple(str(assign[name]) for name in self.input_order)

    def _untuple(self, tupled_args=None):
        assert tupled_args != None
        listed_args = list(tupled_args)
        return {name : listed_args[idx] \
                    for idx, name in enumerate(self.input_order)}

    def run(self):
        """
        Run Themis given the configuration.
        """
        #TODO: make this general
        group_discrimination(field=["Race"])

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
        if tupled_args in self.cache:
            return self.cache[tupled_args]

        cmd = self.command + " " + " ".join(tupled_args)
        output = commands.getoutput(cmd).strip()
        self.cache[tupled_args] = (commands.getoutput(cmd).strip() == "1")
        return self.cache[tupled_args]

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

    def _all_relevant_subs(self, xs):
        return chain.from_iterable(combinations(xs, n) \
                                            for n in range(1, len(xs)))

    def _is_subset(self, small, big):
        for x in small:
            if x not in big:
                return False
        return True

    def group_discrimination(self, i_fields=None, conf=0.99, margin=0.01):
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

    def causal_discrimination(self, i_fields=None, conf=0.99, margin=0.01):
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
                fixed_assign.update(dyn_sub_assign)
                self._add_assignment(test_suite, assign)
                if self.get_test_result(assign=assign):
                    count += 1
                    break

            p, end = self._end_condition(count, num_sampled, conf, margin)
            if end:
                break

        return test_suite, p

    def discrimination_search(self, threshold=0.3, conf=0.99, margin=0.01):
        pass


if __name__ == '__main__':
    pass
