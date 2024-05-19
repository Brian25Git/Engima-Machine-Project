package enigma;

import static enigma.EnigmaException.*;

/** Class that represents a rotating rotor in the enigma machine.
 *  @author Brian Chiang
 */
class MovingRotor extends Rotor {

    /** A rotor named NAME whose permutation in its default setting is
     *  PERM, and whose notches are at the positions indicated in NOTCHES.
     *  The Rotor is initally in its 0 setting (first character of its
     *  alphabet).
     */
    MovingRotor(String name, Permutation perm, String notches) {
        super(name, perm);
        _notches = notches;
        for (char b: notches.toCharArray()) {
            if (!alphabet().contains(b)) {
                throw error("Char %s is not found in alphabet", b);
            }
        }
    }
    @Override
    boolean rotates() {
        return true;
    }

    @Override
    boolean atNotch() {
        int truePerm = permutation().wrap(setting() + ringSetting());
        return _notches.indexOf(alphabet().toChar(truePerm)) >= 0;
    }

    @Override
    void advance() {
        set(true);
        set(setting() + 1);
    }

    /** Where notches are in ALPHA. */
    private String _notches;
}
