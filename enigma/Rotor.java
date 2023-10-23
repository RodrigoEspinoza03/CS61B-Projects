package enigma;

import static enigma.EnigmaException.*;

/** Superclass that represents a rotor in the enigma machine.
 *  @author Rodrigo Espinoza
 */
class Rotor {

    /** A rotor named NAME whose permutation is given by PERM. */
    Rotor(String name, Permutation perm) {
        _name = name;
        _permutation = perm;
        _setting = 0;
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
        if (posn >= this.size()) {
            throw EnigmaException.error("int posn greater than alphabet size");
        } else {
            _setting = posn;
        }
    }

    /** Set setting() to character CPOSN. */
    void set(char cposn) {
        if (!this.alphabet().contains(cposn)) {
            throw EnigmaException.error("character cspon not in alphabet");
        } else {
            int val = this.alphabet().toInt(cposn);
            set(val);
        }

    }

    /** Return the conversion of P (an integer in the range 0..size()-1)
     *  according to my permutation. */
    int convertForward(int p) {
        int result = 0;
        result = permutation().permute(p + _setting);
        result -= _setting;
        return permutation().wrap(result);
    }

    /** Return the conversion of E (an integer in the range 0..size()-1)
     *  according to the inverse of my permutation. */
    int convertBackward(int e) {
        int result = 0;
        result = permutation().invert(e + _setting);
        result -= _setting;
        return permutation().wrap(result);
    }

    /** Returns the positions of the notches, as a string giving the letters
     *  on the ring at which they occur. */
    String notches() {
        return "";
    }

    /** Returns true iff I am positioned to allow the rotor to my left
     *  to advance. */
    boolean atNotch() {
        for (int i = 0; i < _notch.length(); i++) {
            if (_notch.charAt(i) == alphabet().toChar(_setting)) {
                return true;
            }
        }
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

    /** setting refers to the position of the rotor at.
     * as an int based on the _permutation's alphabet
     */
    protected int _setting;

    /** refers to notches in a  string.
     * initially set to ""
     */
    protected String _notch = notches();

}
