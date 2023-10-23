package enigma;


/** An alphabet of encodable characters.  Provides a mapping from characters
 *  to and from indices into the alphabet.
 *  @author Rodrigo Espinoza
 */
class Alphabet {
    /** Contains the alphabet in a string.
     */
    private String _alphabet;

    /** A new alphabet containing CHARS. The K-th character has index
     *  K (numbering from 0). No character may be duplicated. */
    Alphabet(String chars) {
        chars = chars.trim();
        chars = chars.replaceAll(" ", "");
        for (int index = 0; index < chars.length(); index++) {
            char curr = chars.charAt(index);
            for (int check = index + 1; check < chars.length(); check++) {
                if (curr == chars.charAt(check)) {
                    throw EnigmaException.error("duplicate characters in char");
                }
            }
        }
        if (chars.contains(")") || chars.contains("(") || chars.contains("*")) {
            throw EnigmaException.error("illegal character used in alphabet");
        }
        if (chars.isEmpty()) {
            throw EnigmaException.error("no alphabet");
        }
        this._alphabet = chars;
    }

    /** A default alphabet of all upper-case characters. */
    Alphabet() {
        this("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    /** Returns the size of the alphabet. */
    int size() {
        return _alphabet.length();
    }

    /** Returns true if CH is in this alphabet. */
    boolean contains(char ch) {
        for (int index = 0; index < this.size(); index++) {
            if (this.toChar(index) == ch) {
                return true;
            }
        }
        return false;
    }

    /** Returns character number INDEX in the alphabet, where
     *  0 <= INDEX < size(). */
    char toChar(int index) {
        return _alphabet.charAt(index);
    }

    /** Returns the index of character CH which must be in
     *  the alphabet. This is the inverse of toChar(). */
    int toInt(char ch) {
        int index = 0;
        while (index < this.size()) {
            if (this.toChar(index) == ch) {
                break;
            }
            index++;
        }
        return index;
    }

}
