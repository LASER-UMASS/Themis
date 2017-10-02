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
    _, p = t.group_discrimination(i_fields=["Sex", "Race"])
    print "Sex and Race: ", p

    _, p = t.group_discrimination(i_fields=["Race"])
    print "Race: ", p

    _, p = t.group_discrimination(i_fields=["Sex"])
    print "Sex: ", p

def test_causal_discrimination():
    t = themis.Themis(xml_fname="settings.xml")
    _, p = t.causal_discrimination(i_fields=["Sex", "Race"])
    print "Sex and Race: ", p

    _, p = t.causal_discrimination(i_fields=["Race"])
    print "Race: ", p

    _, p = t.causal_discrimination(i_fields=["Sex"])
    print "Sex: ", p

