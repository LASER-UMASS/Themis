import pytest
import themis

def test_init():
    _input = themis.Input(name="Sex", values=["Male", "Female"])
    assert _input.name == "Sex"
    assert _input.values == ["Male", "Female"]
    assert _input.num_values == 2

def test_get_random_input():
    _input = themis.Input(name="Sex", values=["Male", "Female"])
    for i in range(10):
        r_input = _input.get_random_input()
        assert r_input == "Male" or r_input == "Female"
