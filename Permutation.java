package enigma;

import static enigma.EnigmaException.*;

/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author Brian Chiang
 */
class Permutation {

    /** Set this Permutation to that specified by CYCLES, a string in the
     *  form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     *  is interpreted as a permutation in cycle notation.  Characters in the
     *  alphabet that are not included in any cycle map to themselves.
     *  Whitespace is ignored. */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        addCycle(cycles);
    }

    /** Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     *  c0c1...cm. */
    private void addCycle(String cycle) {
        int open = 0;
        int close = 0;
        for (int i = 0; i < cycle.length(); i++) {
            char currentChar = cycle.charAt(i);
            if (currentChar == '(') {
                open++;
            } else if (currentChar == ')') {
                close++;
            } else if (!Character.isWhitespace(currentChar)) {
                if (!_alphabet.contains(currentChar)) {
                    throw error("Chara %s not in _ALPHABET", currentChar);
                } else {
                    for (int j = i + 1; j < cycle.length(); j++) {
                        char comparedChar = cycle.charAt(j);
                        if (currentChar == comparedChar) {
                            throw error("%s repeated in _CYCLES", currentChar);
                        }
                    }
                }
            }
        }
        if (open != close) {
            throw error("# opening and closing parenthesis do not match");
        }
        _cycles = cycle;
    }

    /** Return the value of P modulo the size of this permutation. */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /** Returns the size of the alphabet I permute. */
    int size() {
        return _alphabet.size();
    }

    /** Return the result of applying this permutation to P modulo the
     *  alphabet size. */
    int permute(int p) {
        char inputChar = _alphabet.toChar(wrap(p));
        int cycleInt = _cycles.indexOf(inputChar);
        char outputChar;
        if (cycleInt < 0) {
            outputChar = inputChar;
        } else {
            outputChar = _cycles.charAt(cycleInt + 1);
            if (outputChar == ')') {
                for (int i = cycleInt; i >= 0; i--) {
                    if (_cycles.charAt(i) == '(') {
                        outputChar = _cycles.charAt(i + 1);
                        break;
                    }
                }
            }
        }
        return _alphabet.toInt(outputChar);
    }

    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size. */
    int invert(int c) {
        char outputChar = _alphabet.toChar(wrap(c));
        int cycleInt = _cycles.indexOf(outputChar);
        char inputChar;
        if (cycleInt < 0) {
            inputChar = outputChar;
        } else {
            inputChar = _cycles.charAt(cycleInt - 1);
            if (inputChar == '(') {
                for (int i = cycleInt; i < _cycles.length(); i++) {
                    if (_cycles.charAt(i) == ')') {
                        inputChar = _cycles.charAt(i - 1);
                        break;
                    }
                }
            }
        }
        return _alphabet.toInt(inputChar);
    }

    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        if (!_alphabet.contains(p)) {
            throw error("Character %s not found in _ALPHABET", p);
        }
        int inputInt = _alphabet.toInt(p);
        int outputInt = permute(inputInt);
        char charOutput = _alphabet.toChar(outputInt);
        return charOutput;
    }

    /** Return the result of applying the inverse of this permutation to C. */
    char invert(char c) {
        if (!_alphabet.contains(c)) {
            throw error("Character %s not found in alphabet", c);
        }
        int outputInt = _alphabet.toInt(c);
        int inputInt = invert(outputInt);
        char charInput = _alphabet.toChar(inputInt);
        return charInput;
    }

    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {
        return _alphabet;
    }

    /** Return true iff this permutation is a derangement (i.e., a
     *  permutation for which no value maps to itself). */
    boolean derangement() {
        for (int i = 0; i < size(); i++) {
            int outputInt = permute(i);
            if (i == outputInt) {
                return false;
            }
        }
        return true;
    }

    /** Alphabet of this permutation. */
    private Alphabet _alphabet;

    /** Cycles of this permutation. */
    private String _cycles;
}
