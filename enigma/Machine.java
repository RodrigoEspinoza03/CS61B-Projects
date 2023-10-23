package enigma;

import java.util.ArrayList;
import java.util.Collection;

import static enigma.EnigmaException.*;

/** Class that represents a complete enigma machine.
 *  @author Rodrigo Espinoza
 */
class Machine {

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        _alphabet = alpha;
        _rotorsList.addAll(allRotors);
        _rotorSlots = numRotors;
        _numPawls = pawls;
        if (_numPawls <= 0 || _numPawls > _rotorSlots) {
            throw EnigmaException.error("numRotors less than 1");
        }
        _plugboard = new Permutation("", _alphabet);
        _rotorsInSlots = new ArrayList<>(numRotors);

    }

    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _rotorSlots;
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _numPawls;
    }

    /** Return Rotor #K, where Rotor #0 is the reflector, and Rotor
     *  #(numRotors()-1) is the fast Rotor.  Modifying this Rotor has
     *  undefined results. */
    Rotor getRotor(int k) {
        return _rotorsInSlots.get(k);
    }

    Alphabet alphabet() {
        return _alphabet;
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {
        if (rotors.length != _rotorSlots) {
            throw EnigmaException.error("More/less rotors than rotor slot");
        }
        for (int i = 0; i < rotors.length; i++) {
            String curr = rotors[i];
            for (int j = i + 1; j < rotors.length; j++) {
                if (curr.equals(rotors[j])) {
                    throw EnigmaException.error("Rotors repeating");
                }

            }
        }

        _rotorsInSlots.clear();

        for (String x: rotors) {
            for (Rotor y: _rotorsList) {
                if (x.equals(y.name())) {
                    _rotorsInSlots.add(y);
                    y.set(0);
                }
            }
        }

        for (int i = 0; i < numRotors(); i++) {
            Rotor curr = getRotor(i);
            int x = numRotors() - numPawls();
            if (curr instanceof Reflector) {
                if (i != 0) {
                    throw EnigmaException.error("reflector in wrong place");
                }
            } else if (curr instanceof FixedRotor) {
                if (i == 0 || i >= x) {
                    throw EnigmaException.error("fixed rotor in wrong place");
                }
            } else if (curr instanceof MovingRotor) {
                if (i < x) {
                    throw EnigmaException.error("moving rotor in wrong place");
                }
            } else {
                throw error("invalid rotor type detected");
            }

        }

    }

    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        if (setting.length() != numRotors() - 1) {
            throw EnigmaException.error("setting length less than numRotors()");
        }
        int rotorIndex = 1;
        for (int i = 0; i < setting.length(); i++) {
            Rotor curr = getRotor(rotorIndex);
            char c = setting.charAt(i);
            if (!_alphabet.contains(c)) {
                throw EnigmaException.error("Char not in alphabet");
            }
            curr.set(setting.charAt(i));
            rotorIndex++;
        }
    }

    /** Return the current plugboard's permutation. */
    Permutation plugboard() {
        return _plugboard;
    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _plugboard = plugboard;
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine. */
    int convert(int c) {
        advanceRotors();
        if (Main.verbose()) {
            System.err.printf("[");
            for (int r = 1; r < numRotors(); r += 1) {
                System.err.printf("%c",
                        alphabet().toChar(getRotor(r).setting()));
            }
            System.err.printf("] %c -> ", alphabet().toChar(c));
        }
        c = plugboard().permute(c);
        if (Main.verbose()) {
            System.err.printf("%c -> ", alphabet().toChar(c));
        }
        c = applyRotors(c);
        c = plugboard().permute(c);
        if (Main.verbose()) {
            System.err.printf("%c%n", alphabet().toChar(c));
        }
        return c;
    }

    /** Advance all rotors to their next position. */
    private void advanceRotors() {
        boolean[] moving = new boolean[numRotors()];

        for (int i = 0; i < numRotors(); i++) {
            moving[i] = (i == numRotors() - 1)
                    || (getRotor(i).rotates() && getRotor(i + 1).atNotch());
        }


        for (int i = 0; i < numRotors(); i++) {
            if (moving[i]) {
                getRotor(i).advance();
                if (i < numRotors() - 1) {
                    getRotor(i + 1).advance();
                    i += 1;
                }
            }
        }

    }

    /** Return the result of applying the rotors to the character C (as an
     *  index in the range 0..alphabet size - 1). */
    private int applyRotors(int c) {

        for (int i = _rotorsInSlots.size() - 1; i >= 0; i--) {
            c = _rotorsInSlots.get(i).convertForward(c);
        }

        for (int i = 1; i < _rotorsInSlots.size(); i++) {
            c = _rotorsInSlots.get(i).convertBackward(c);
        }

        return c;
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        String translated = "";
        for (int i = 0; i < msg.length(); i++) {
            int x = convert(_alphabet.toInt(msg.charAt(i)));
            translated += _alphabet.toChar(x);
        }
        return translated;
    }

    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;

    /** Contains a list of my rotors.*/
    private ArrayList<Rotor> _rotorsList = new ArrayList<>();

    /** Number of rotor slots available.*/
    private int _rotorSlots;

    /** Number of pawls available.*/
    private int _numPawls;

    /** Contains plugboard permutations.*/
    private Permutation _plugboard;


    /** Contains Rotors in slots.*/
    private ArrayList<Rotor> _rotorsInSlots;


}
