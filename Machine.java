package enigma;

import java.util.HashMap;
import java.util.Collection;

import static enigma.EnigmaException.*;

/** Class that represents a complete enigma machine.
 *  @author Brian Chiang
 */
class Machine {

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        if (numRotors <= 1) {
            throw error("Not enough rotors.");
        }
        if (pawls < 0 || pawls >= numRotors) {
            throw error("Wrong number of pawls.");
        }
        _alphabet = alpha;
        _numRotors = numRotors;
        _numPawls = pawls;
        _allRotors = allRotors.toArray();
        _rotorMap = new HashMap<>();
        _rotors = new Rotor[numRotors];
        _plugboard = null;
    }

    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _numRotors;
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _numPawls;
    }

    /** Return Rotor #K, where Rotor #0 is the reflector, and Rotor
     *  #(numRotors()-1) is the fast Rotor.  Modifying this Rotor has
     *  undefined results. */
    Rotor getRotor(int k) {
        return _rotors[k];
    }

    Alphabet alphabet() {
        return _alphabet;
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {
        _rotorMap = new HashMap<>();
        _rotors = new Rotor[_numRotors];
        int len = rotors.length;
        if (len != numRotors()) {
            throw error("%2$d slots can't fit %1$d ROTORS ", len, numRotors());
        }
        int moving = numPawls();
        for (int i = rotors.length - 1; i > -1; i--) {
            for (int j = 0; j < _allRotors.length; j++) {
                Rotor r = (Rotor) _allRotors[j];
                String name = rotors[i];
                if (r.name().equals(name)) {
                    if (_rotorMap.containsKey(name)) {
                        throw error("%s ROTOR is used twice!", name);
                    }
                    _rotorMap.put(name, r);
                    _rotors[i] = r;
                    if (r.rotates()) {
                        moving--;
                        if (moving == 0) {
                            if (i != numRotors() - numPawls()) {
                                throw error("Moving rotors need PAWLS");
                            }
                        }
                    }
                    if (moving < 0) {
                        throw error("Too many moving rotors not enough pawls");
                    }
                    if (i == 0 && !r.reflecting()) {
                        throw error("Leftmost rotor should be a reflector");
                    }
                    if (i != 0 && r.reflecting()) {
                        throw error("Reflectors only in first slot");
                    }
                }
            }
            if (!_rotorMap.containsKey(rotors[i])) {
                throw error("Rotor name %s not found in _ALLROTORS", rotors[i]);
            }
        }
    }

    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        if (setting.length() >= numRotors()) {
            throw error("Too many settings for %d ROTORS", setting.length());
        }
        for (int i = 1; i < numRotors(); i++) {
            Rotor r = _rotors[i];
            char sett = setting.charAt(i - 1);
            if (!_alphabet.contains(sett)) {
                throw error("%s is not in ALPHA (not a valid setting)", sett);
            }
            r.set(sett);
        }
    }

    /**Set my rotor's rings according to RINGSET. */
    void setRings(String ringSet) {
        if (ringSet.length() >= numRotors()) {
            throw error("Too many RINGSETS for %d ROTORS", ringSet.length());
        }
        for (int i = 1; i < numRotors(); i++) {
            Rotor r = _rotors[i];
            char sett = ringSet.charAt(i - 1);
            if (!_alphabet.contains(sett)) {
                throw error("%s is not in ALPHA (not a valid setting)", sett);
            }
            r.setRing(sett);
        }
    }

    /** Return the current plugboard's permutation. */
    Permutation plugboard() {
        return _plugboard;
    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _plugboard = null;
        _plugboard = plugboard;
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine. */
    int convert(int c) {
        for (int i = 1; i < _rotors.length; i++) {
            Rotor r = _rotors[i];
            if (i == _rotors.length - 1) {
                if (r.atNotch()) {
                    Rotor left = _rotors[i - 1];
                    if (left.rotates() && !left.haveRotated()) {
                        left.advance();
                    }
                }
                if (!r.haveRotated()) {
                    r.advance();
                }
            } else if (r.atNotch()) {
                Rotor left = _rotors[i - 1];
                if (left.rotates()) {
                    if (!r.haveRotated()) {
                        r.advance();
                    }
                    if (!left.haveRotated()) {
                        left.advance();
                    }
                }
            }
        }
        for (Rotor r: _rotors) {
            r.set(false);
        }
        int result = c;
        if (_plugboard != null) {
            result = _plugboard.permute(c);
        }
        for (int j = numRotors() - 1; j > 0; j--) {
            Rotor r = _rotors[j];
            result = r.convertForward(result);
        }
        for (int k = 0; k < numRotors(); k++) {
            Rotor r = _rotors[k];
            result = r.convertBackward(result);
        }
        if (_plugboard != null) {
            result = _plugboard.invert(result);
        }
        return result;
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        char[] msgA = msg.toCharArray();
        for (int i = 0; i < msgA.length; i++) {
            char c = msgA[i];
            if (c != ' ') {
                int intC = convert(_alphabet.toInt(c));
                msgA[i] = _alphabet.toChar(intC);
            }
        }
        return new String(msgA);
    }

    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;

    /** Number of all Rotors. */
    private int _numRotors;

    /** Number of Pawls. */
    private int _numPawls;

    /** Array of all Rotors in the Machine. */
    private Object[] _allRotors;

    /** Rotors used in this Machine. */
    private Rotor[] _rotors;

    /** Maps Rotors to Names. */
    private HashMap<String, Rotor> _rotorMap;

    /** Plugboard for this Machine. */
    private Permutation _plugboard;
}
