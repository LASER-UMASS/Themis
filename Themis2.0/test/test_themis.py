from itertools import chain, combinations
import pytest
import themis

def test_init():
    t = themis.Themis(xml_fname="settings.xml")
    assert t.input_order == ["Sex", "Age", "Race", "Income"]
    assert t.command == "python software.py"

def test_tuple():
    t = themis.Themis(xml_fname="settings.xml")
    assign = t.new_random_sub_input(args=t.input_order)
    tupled_args = t._tuple(assign=assign)
    assert assign == t._untuple(tupled_args = tupled_args)

def test_gen_all_sub_inputs():
    t = themis.Themis(xml_fname="settings.xml")
    t.gen_all_sub_inputs(args=t.input_order)

def test_get_test_result():
    t = themis.Themis(xml_fname="settings.xml")
    assign = t.new_random_sub_input(args=t.input_order)
    if assign["Sex"] == "Male" and assign["Race"] == "Red":
        assert t.get_test_result(assign=assign)
    else:
        assert not t.get_test_result(assign=assign)

def test_group_discrimination():
    t = themis.Themis(xml_fname="settings.xml")
    print "\nGroup:"
    for f in t._all_relevant_subs(["Sex", "Race", "Age", "Income"]):
        _, p = t.group_discrimination(i_fields=f)
        print f, "--> ", p

def test_causal_discrimination():
    t = themis.Themis(xml_fname="settings.xml")
    print "\nCausal:"
    for f in t._all_relevant_subs(["Sex", "Race", "Age", "Income"]):
        _, p = t.causal_discrimination(i_fields=f)
        print f, "--> ", p

def test_discrimination_search():
    t = themis.Themis(xml_fname="settings.xml")
    group_subs, causal_subs = t.discrimination_search(group=True, causal=True)
    print "\n"
    print "Group: ", group_subs
    print "Causal: ", causal_subs


