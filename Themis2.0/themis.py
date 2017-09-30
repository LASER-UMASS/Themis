# Themis2.0 main class
#
# By: Rico Angell

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

        self.software_name = root.find("name").text
        self.command = root.find("command").text

        random.seed(int(root.find("seed").text))

        inputs = []
        args = root.find("inputs")
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
            inputs.append(Input(name=name, values=values)

        # TODO: Add parsing to run functions user requests

    def run():
        """
        Run Themis given the configuration.
        """
        #TODO: make this general
        group_discrimination(field=["Race"])

    def construct_inputs_and_run(assignment=None):
        """
        Run the software on the input `assignment`.

        Parameters
        ----------
        assignment : dict
            dictionary of all input assignments.

        Returns
        -------
        str
            "0" or "1", the output of the software.
        """
        pass

    def new_random_sub_input(attributes=[]):
        """
        Produce a random input for the each of the elements of `attributes`.

        Parameters
        ----------
        attributes : list of str
            list of attribute names that we want random assignments to

        Returns
        -------
        dict
            the assignments to each of the attributes.
        """
        pass

    def gen_all_sub_inputs(attributes=[]):
        """
        """
        pass

    def get_test_result(assignment=None):
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
        pass

    def group_discrimination(fields=None, conf=0.99, margin=0.01):
        """
        """
        pass


if __name__ == '__main__':
    pass
