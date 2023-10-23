package enigma;

import static enigma.EnigmaException.*;

/** Class that represents a rotating rotor in the enigma machine.
 *  @author Rodrigo Espinoza
 */
class MovingRotor extends Rotor {

    /** A rotor named NAME whose permutation in its default setting is
     *  PERM, and whose notches are at the positions indicated in NOTCHES.
     *  The Rotor is initally in its 0 setting (first character of its
     *  alphabet).
     */
    MovingRotor(String name, Permutation perm, String notches) {
        super(name, perm);
        _notch = notches;
        for (int i = 0; i < notches.length(); i++) {
            if (!alphabet().contains(notches.charAt(i))) {
                throw EnigmaException.error("Notches not in alphabet");
            }
        }
    }

    @Override
    void advance() {
        if (_setting + 1 == this.size()) {
            _setting = 0;
        } else {
            _setting += 1;
        }
    }

    @Override
    boolean rotates() {
        return true;
    }

    @Override
    String notches() {
        return _notch;
    }

    @Override
    public String toString() {
        return "Moving rotor" + name();
    }

}
