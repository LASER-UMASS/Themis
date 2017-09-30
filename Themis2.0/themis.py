# Themis2.0 main class
#
# By: Rico Angell

import commands
import itertools
import random
import scipy.stats as st


class Input:
    """
    """

    def __init__(name=name, values=[]):
        self.name = name
        self.values = values
        self.num_values = len(values)

    def get_random_input():
        """
        Return a random value from self.values
        """
        return random.choice(self.values)


class Themis:
    """
    """

    def __init__(xml_fname=""):
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

        self.max_samples = 50
        self.min_samples = 10
        self.software_name = root.find("name").text
        self.command = root.find("command").text.strip
        self._build_input_space(args=root.find("inputs"))
        self.cache = {}
        # TODO: Add parsing to run functions user requests

    def _build_input_space(self, args=None):
        assert args != None
        self.inputs = {}
        self.input_order = []
        args =
        for uid, obj in enumerate(args.findall("input")):
            name = obj.find("name").text
            values = []
            t = obj.find("type").text
            if t == "categorical":
                values = [elt.text for elt in obj.find("values").findall("value")]
            elif types[uid] == "continuousInt":
                values = range(int(obj.find("bounds").find("lowerbound").text),
                               int(obj.find("bounds").find("upperbound").text))
            else:
                assert false
            self.inputs[name] = Input(name=name, values=values)
            self.input_order.append(name)

    def _tuple(self, assignment=None):
        assert assignment != None
        return tuple(str(assignment[name]) for name in self.input_order)

    def _untuple(self, tupled_args=None):
        assert tupled_args != None
        listed_args = list(tupled_args)
        return {name : listed_args[idx] for idx, name in self.input_order}

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
            the assignments to each of the attributes.
        """
        assert args
        return {name : self.input[name].get_random_input() for name in args}

    def gen_all_sub_inputs(self, args=[]):
        """
        Produces a list of all possible assignments.

        Parameters
        ----------
        attributes : list of str
            list of attribute names that we want random assignments to.

        Returns
        -------
        list of dict
            list of the assignments.
        """
        assert args
        vals_of_args = [self.inputs[arg].values for arg in args]
        combos = [list(elt) for elt in list(itertools.product(*vals_of_args))]
        return [{arg : elt[idx] for idx, arg in enumerate(args)} \
                                                 for elt in combos]

    def get_test_result(self, assignment=None):
        """
        Returns the output of the software with input `assignment`.

        Checks the cache to see if the output for `assignment` has already
        been computed. If it has, return the cached results. Otherwise, run
        the software and cache and return the result.

        Parameters
        ----------
        assignment : dict
            dictionary of all input assignments.

        Returns
        -------
        str
            "0" or "1", the output of the software.
        """
        assert assignment != None
        tupled_args = self._tuple(assignment)
        if tupled_args in self.cache:
            return self.cache[tupled_args]

        cmd = self.command + " " + " ".join(tupled_args)
        self.cache[tupled_args] = commands.getoutput(cmd).strip
        return output

    def group_discrimination(self, fields=None, conf=0.99, margin=0.01):
        """
        """
        pass


if __name__ == '__main__':
    pass
