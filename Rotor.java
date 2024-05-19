package enigma;

import static enigma.EnigmaException.*;

/** Superclass that represents a rotor in the enigma machine.
 *  @author Brian Chiang
 */
class Rotor {

    /** A rotor named NAME whose permutation is given by PERM. */
    Rotor(String name, Permutation perm) {
        _name = name;
        _permutation = perm;
        _setting = 0;
        _haveRotated = false;
        _ringPosition = 0;
    }

    /** Return my name. */
    String name() {
        return _name;
    }

    /** Return my alphabet. */
    Alphabet alphabet() {
        return _permutation.alphabet();
    }

    /** Return my permutation. */
    Permutation permutation() {
        return _permutation;
    }

    /** Return the size of my alphabet. */
    int size() {
        return _permutation.size();
    }

    /** Return true iff I have a ratchet and can move. */
    boolean rotates() {
        return false;
    }

    /** Return true if I advanced this cycle. */
    boolean haveRotated() {
        return _haveRotated;
    }

    /** Return true iff I reflect. */
    boolean reflecting() {
        return false;
    }

    /** Return my current setting. */
    int setting() {
        return _setting;
    }

    /** Set setting() to POSN.  */
    void set(int posn) {
        _setting = permutation().wrap(posn);
    }

    /** Set setting() to character CPOSN. */
    void set(char cposn) {
        _setting = permutation().wrap(alphabet().toInt(cposn));
    }

    /** Set _haveRotated to B. */
    void set(boolean b) {
        _haveRotated = b;
    }

    /** Returns the RINGSETTING of ROTOR. */
    int ringSetting() {
        return _ringPosition;
    }

    /** Set rings to character RPOSN. */
    void setRing(char rposn) {
        _ringPosition = alphabet().toInt(rposn);
        set(_setting - _ringPosition);
    }

    /** Return the conversion of P (an integer in the range 0..size()-1)
     *  according to my permutation. */
    int convertForward(int p) {
        if (p >= size()) {
            throw error("%d is not within the _ALPHABET size", p);
        }
        int contact = permutation().wrap(p + setting());
        int innerOutput = permutation().permute(contact);
        int output = permutation().wrap(innerOutput - setting());
        return output;
    }

    /** Return the conversion of E (an integer in the range 0..size()-1)
     *  according to the inverse of my permutation. */
    int convertBackward(int e) {
        if (e >= size()) {
            throw error("%d is not within alphabet size", e);
        }
        int contact = permutation().wrap(e + setting());
        int innerInput = permutation().invert(contact);
        int input = permutation().wrap(innerInput - setting());
        return input;
    }

    /** Returns true iff I am positioned to allow the rotor to my left
     *  to advance. */
    boolean atNotch() {
        return false;
    }

    /** Advance me one position, if possible. By default, does nothing. */
    void advance() {
    }

    @Override
    public String toString() {
        return "Rotor " + _name;
    }

    /** My name. */
    private final String _name;

    /** The permutation implemented by this rotor in its 0 position. */
    private Permutation _permutation;

    /** My Setting. */
    private int _setting;

    /** Have I Rotated? */
    private boolean _haveRotated;

    /** Setting of my alphabet ring.  */
    private int _ringPosition;
}
