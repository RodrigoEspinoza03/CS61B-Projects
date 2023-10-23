package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import ucb.util.CommandArgs;

import static enigma.EnigmaException.*;

/** Enigma simulator.
 *  @author Rodrigo Espinoza
 */
public final class Main {

    /** Process a sequence of encryptions and decryptions, as
     *  specified by ARGS, where 1 <= ARGS.length <= 3.
     *  ARGS[0] is the name of a configuration file.
     *  ARGS[1] is optional; when present, it names an input file
     *  containing messages.  Otherwise, input comes from the standard
     *  input.  ARGS[2] is optional; when present, it names an output
     *  file for processed messages.  Otherwise, output goes to the
     *  standard output. Exits normally if there are no errors in the input;
     *  otherwise with code 1. */
    public static void main(String... args) {
        try {
            CommandArgs options =
                new CommandArgs("--verbose --=(.*){1,3}", args);
            if (!options.ok()) {
                throw error("Usage: java enigma.Main [--verbose] "
                            + "[INPUT [OUTPUT]]");
            }

            _verbose = options.contains("--verbose");
            new Main(options.get("--")).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /** Open the necessary files for non-option arguments ARGS (see comment
      *  on main). */
    Main(List<String> args) {
        _config = getInput(args.get(0));

        if (args.size() > 1) {
            _input = getInput(args.get(1));
        } else {
            _input = new Scanner(System.in);
        }

        if (args.size() > 2) {
            _output = getOutput(args.get(2));
        } else {
            _output = System.out;
        }
    }

    /** Return a Scanner reading from the file named NAME. */
    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Return a PrintStream writing to the file named NAME. */
    private PrintStream getOutput(String name) {
        try {
            return new PrintStream(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Configure an Enigma machine from the contents of configuration
     *  file _config and apply it to the messages in _input, sending the
     *  results to _output. */
    private void process() {
        Machine curr = readConfig();
        while (_input.hasNext()) {
            String settings = _input.nextLine();
            if (settings.isEmpty()) {
                printMessageLine("");
                settings = _input.nextLine();
            }
            if (!settings.contains("*")) {
                throw EnigmaException.error("wrong format");
            }
            setUp(curr, settings);
            while (!_input.hasNext("(?<=^|\n)\\*.*") && _input.hasNext()) {
                String message = _input.nextLine();
                message = message.trim();
                message = message.replaceAll("\\s", "");
                message = curr.convert(message);
                printMessageLine(message);
            }
        }
    }

    /** Return an Enigma machine configured from the contents of configuration
     *  file _config. */
    private Machine readConfig() {
        try {
            _alphabet = new Alphabet(_config.nextLine());
            int rotorSlots = _config.nextInt();
            int pawls = _config.nextInt();

            ArrayList<Rotor> allRotors = new ArrayList<>();
            while (_config.hasNext()) {
                allRotors.add(readRotor());
            }

            return new Machine(_alphabet, rotorSlots, pawls, allRotors);
        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }

    /** Return a rotor, reading its description from _config. */
    private Rotor readRotor() {
        try {
            String name = _config.next();
            name = name.trim();
            if (name.contains("(") || name.contains(")")) {
                throw EnigmaException.error("wrong name characters");
            }

            String typeOfRotor = _config.next();
            typeOfRotor = typeOfRotor.trim();

            String cycles = "";

            while (_config.hasNext(".*[\\(|\\)]+.*")) {
                cycles += _config.next();
            }

            int parR = 0;
            int parL = 0;
            for (int i = 0; i < cycles.length(); i++) {
                if (cycles.charAt(i) == ')') {
                    parL++;
                } else if (cycles.charAt(i) == '(') {
                    parR++;
                }
            }
            if (parL != parR) {
                throw EnigmaException.error("bad format");
            }

            Permutation perm = new Permutation(cycles, _alphabet);
            Rotor curr;

            if (typeOfRotor.charAt(0) == 'R') {
                curr = new Reflector(name, perm);
                if (typeOfRotor.length() > 1) {
                    throw EnigmaException.error("No notches");
                }
            } else if (typeOfRotor.charAt(0) == 'N') {
                curr = new FixedRotor(name, perm);
                if (typeOfRotor.length() > 1) {
                    throw EnigmaException.error("No notches");
                }
            } else if (typeOfRotor.charAt(0) == 'M') {
                if (typeOfRotor.length() == 1) {
                    throw EnigmaException.error("No notches");
                }
                curr = new MovingRotor(name, perm, typeOfRotor.substring(1));
            } else {
                throw EnigmaException.error("wrong rotor type");
            }

            return curr;
        } catch (NoSuchElementException excp) {
            throw error("bad rotor description");
        }
    }

    /** Set M according to the specification given on SETTINGS,
     *  which must have the format specified in the assignment. */
    private void setUp(Machine M, String settings) {
        settings = settings.replaceAll("\\*", "");
        settings = settings.trim();
        String[] set = settings.split(" \\s*");
        if (set.length - 1 < M.numRotors()) {
            throw EnigmaException.error("Not enough arguments in setting");
        }

        String[] rotors = new String[M.numRotors()];
        for (int i = 0; i < M.numRotors(); i++) {
            rotors[i] = set[i];
        }

        for (int i = 0; i < rotors.length - 1; i++) {
            for (int j = i + 1; j < rotors.length; j++) {
                if (rotors[i].equals(rotors[j])) {
                    throw EnigmaException.error("Repeated Rotor");
                }
            }
        }

        String setting = set[M.numRotors()];
        for (int i = 0; i < setting.length(); i++) {
            if (!M.alphabet().contains(setting.charAt(i))) {
                throw EnigmaException.error("Char not in alphabet");
            }
        }
        M.insertRotors(rotors);

        if (!M.getRotor(0).reflecting()) {
            throw EnigmaException.error("First Rotor should be a reflector");
        }
        M.setRotors(setting);

        if (set.length > M.numRotors()) {
            String plug = "";
            for (int i = set.length - 1; i > M.numRotors(); i--) {
                plug += set[i];
            }
            M.setPlugboard(new Permutation(plug, M.alphabet()));
        }


    }

    /** Return true iff verbose option specified. */
    static boolean verbose() {
        return _verbose;
    }

    /** Print MSG in groups of five (except that the last group may
     *  have fewer letters). */
    private void printMessageLine(String msg) {
        String curr = "";
        for (int i = 0; i < msg.length(); i++) {
            if (i == 0) {
                curr += msg.charAt(i);
            } else if ((i + 1) % 5 == 0) {
                curr += msg.charAt(i);
                curr += " ";
            } else {
                curr += msg.charAt(i);
            }
        }
        _output.println(curr);

    }

    /** Alphabet used in this machine. */
    private Alphabet _alphabet;

    /** Source of input messages. */
    private Scanner _input;

    /** Source of machine configuration. */
    private Scanner _config;

    /** File for encoded/decoded messages. */
    private PrintStream _output;

    /** True if --verbose specified. */
    private static boolean _verbose;
}
