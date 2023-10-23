package enigma;

import static enigma.EnigmaException.*;

/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author Rodrigo Espinoza
 */
class Permutation {


    /** Set this Permutation to that specified by CYCLES, a string in the
     *  form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     *  is interpreted as a permutation in cycle notation.  Characters in the
     *  alphabet that are not included in any cycle map to themselves.
     *  Whitespace is ignored. */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        cycles = cycles.replaceAll("\\(", " ");
        cycles = cycles.replaceAll("\\)", " ");
        cycles = cycles.trim();
        _permutations = cycles.split("\\s+");

        for (String s : _permutations) {
            for (int i = 0; i < s.length(); i++) {
                if (!_alphabet.contains(s.charAt(i))) {
                    throw EnigmaException.error("Char not in alphabet");
                }
            }
        }

        cycles = cycles.replaceAll(" ", "");
        for (int index = 0; index < cycles.length(); index++) {
            char curr = cycles.charAt(index);
            for (int check = index + 1; check < cycles.length(); check++) {
                if (curr == cycles.charAt(check)) {
                    throw EnigmaException.error("duplicate characters");
                }
            }
        }
    }

    /** Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     *  c0c1...cm. */
    private void addCycle(String cycle) {

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
        char curr = _alphabet.toChar(wrap(p));
        if (this.contains(curr)) {
            String cycle = this.cycleFinder(curr);
            int index = this.indexOfChar(cycle, curr);
            if (index + 1 == cycle.length()) {
                return _alphabet.toInt(cycle.charAt(0));
            } else {
                return _alphabet.toInt(cycle.charAt(index + 1));
            }
        } else {
            return p;
        }
    }

    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size. */
    int invert(int c) {
        char curr = _alphabet.toChar(wrap(c));
        if (this.contains(curr)) {
            String cycle = this.cycleFinder(curr);
            int index = this.indexOfChar(cycle, curr);
            if (index == 0) {
                return _alphabet.toInt(cycle.charAt(cycle.length() - 1));
            } else {
                return _alphabet.toInt(cycle.charAt(index - 1));
            }
        } else {
            return c;
        }
    }

    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        int permutation = permute(_alphabet.toInt(p));
        return _alphabet.toChar(permutation);
    }

    /** Return the result of applying the inverse of this permutation to C. */
    char invert(char c) {
        int inverse = invert(_alphabet.toInt(c));
        return _alphabet.toChar(inverse);
    }

    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {
        return _alphabet;
    }

    /** Return true iff this permutation is a derangement (i.e., a
     *  permutation for which no value maps to itself). */
    boolean derangement() {
        int numOfCharacters = 0;
        for (String curr: _permutations) {
            if (curr.length() < 2) {
                return false;
            }
            numOfCharacters += curr.length();
        }
        return numOfCharacters == size();
    }

    /** Return true iff _permutations contains a specific character.
     * false iff it doesn't contain it
     * @param d
     */
    boolean contains(char d) {
        for (String curr : _permutations) {
            for (int index = 0; index < curr.length(); index++) {
                if (curr.charAt(index) == d) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Returns the String of _permutations (cycle) that contains.
     * the character c assuming _permutations contains it
     * @param c
     */
    String cycleFinder(char c) {
        String result = "";
        for (String curr : _permutations) {
            for (int currI = 0; currI < curr.length(); currI++) {
                if (curr.charAt(currI) == c) {
                    result = curr;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Return the index of where c is located in cycle.
     * @param cycle
     * @param c
     */
    int indexOfChar(String cycle, char c) {
        int val = 0;
        for (int i = 0; i < cycle.length(); i++) {
            if (cycle.charAt(i) == c) {
                val = i;
                break;
            }
        }
        return val;
    }

    /** Alphabet of this permutation. */
    private Alphabet _alphabet;

    /** List of all permutation cycles, containing cycles in String form. */
    private String [] _permutations;
}
