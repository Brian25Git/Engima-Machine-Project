package enigma;

/** An alphabet of encodable characters.  Provides a mapping from characters
 *  to and from indices into the alphabet.
 *  @author Brian Chiang
 */
class Alphabet {

    /** A new alphabet containing CHARS. The K-th character has index
     *  K (numbering from 0). No character may be duplicated. */
    Alphabet(String chars) {
        char[] notAlpha = new char[] {'*', ')', '('};
        _alphabet = chars.toCharArray();
        _size = chars.length();
        for (int i = 0; i < _size; i++) {
            for (int j = 0; j < _size; j++) {
                char b = _alphabet[i];
                for (char c : notAlpha) {
                    if (b == c) {
                        throw new EnigmaException(b + " is illegal");
                    }
                }
                if (Character.isWhitespace(b)) {
                    throw new EnigmaException("No whitespaces in ALPHA");
                }
                if (i != j && b == _alphabet[j]) {
                    throw new EnigmaException("Char " + b + " duplicated");
                }
            }
        }
    }

    /** A default alphabet of all upper-case characters. */
    Alphabet() {
        this("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    /** Returns the size of the alphabet. */
    int size() {
        return _size;
    }

    /** Returns true if CH is in this alphabet. */
    boolean contains(char ch) {
        for (int i = 0; i < _size; i++) {
            if (_alphabet[i] == ch) {
                return true;
            }
        }
        return false;
    }

    /** Returns character number INDEX in the alphabet, where
     *  0 <= INDEX < size(). */
    char toChar(int index) {
        return _alphabet[index];
    }

    /** Returns the index of character CH which must be in
     *  the alphabet. This is the inverse of toChar(). */
    int toInt(char ch) {
        for (int i = 0; i < _size; i++) {
            if (_alphabet[i] == ch) {
                return i;
            }
        }
        return 0;
    }

    /** Characters in this alphabet. */
    private char[] _alphabet;

    /** Size of this alphabet. */
    private int _size;
}
