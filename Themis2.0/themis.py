# Themis2.0 main class
#
# By: Rico Angell

import scipy.stats as st


class Input:
    """
    """
    def __init__(name=name, values=[]):
        self.name = name
        self.values = values
        self.num_values = len(values)


class Themis:
    """
    """

    def __init__(xml_fname=""):
        """
        """
        pass

    def run():
        """
        Run Themis given the configuration.
        """
        pass

    def run_discrimination_test():
        pass

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

