package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import ucb.util.CommandArgs;

import static enigma.EnigmaException.*;

/** Enigma simulator.
 *  @author Brian Chiang
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
        Machine machine = readConfig();
        if (!_input.hasNext("\\*.*")) {
            throw error("input must have a setting line");
        }
        while (_input.hasNext("\\*.*")) {
            String settings = _input.nextLine();
            while (settings.length() == 0) {
                _output.println(settings);
                settings = _input.nextLine();
            }
            setUp(machine, settings);
            while ((!_input.hasNext("\\*.*")) && _input.hasNextLine()) {
                String untranslated = " ";
                untranslated += _input.nextLine();
                for (int i = 0; i < untranslated.length(); i++) {
                    char b = untranslated.charAt(i);
                    if (!_alphabet.contains(b)) {
                        if (!Character.isWhitespace(b)) {
                            throw error("%s is not valid", b);
                        }
                    }
                }
                String trans = machine.convert(untranslated.substring(1));
                printMessageLine(trans);
            }
        }
    }

    /** Return an Enigma machine configured from the contents of configuration
     *  file _config. */
    private Machine readConfig() {
        try {
            if (!_config.hasNextLine()) {
                throw error("Configuration file is empty");
            }
            String letter = _config.nextLine();
            _alphabet = new Alphabet(letter);
            if (!_config.hasNextInt()) {
                throw error("Missing number of Pawls and Rotors");
            }
            int rotors = _config.nextInt();
            if (!_config.hasNextInt()) {
                throw error("Missing Pawls");
            }
            int pawls = _config.nextInt();
            if (pawls >= rotors) {
                throw error("Too many pawls");
            }
            ArrayList<Rotor> everyRotor = new ArrayList<Rotor>();
            while (_config.hasNext()) {
                everyRotor.add(readRotor());
            }
            return new Machine(_alphabet, rotors, pawls, everyRotor);
        } catch (NoSuchElementException excp) {
            throw error("Configuration file is truncated");
        }
    }

    /** Return a rotor, reading its description from _config. */
    private Rotor readRotor() {
        try {
            String cycles = "";
            String rotor = _config.next();
            String config = _config.next();
            while (_config.hasNext("\\(.*\\)")) {
                cycles = cycles +  _config.next();
            }
            Permutation permute = new Permutation(cycles, _alphabet);
            char character = config.charAt(0);
            if (character == 'M') {
                String nutch = "";
                for (int elem = 1; elem < config.length(); elem++) {
                    nutch += config.charAt(elem);
                }
                return new MovingRotor(rotor, permute, nutch);
            } else if (character == 'N') {
                return new FixedRotor(rotor, permute);
            } else if (character == 'R') {
                return new Reflector(rotor, permute);
            } else {
                throw new EnigmaException("Rotor is unavailable");
            }
        } catch (NoSuchElementException excp) {
            throw error("Rotor description insufficient");
        }
    }

    /** Set M according to the specification given on SETTINGS,
     *  which must have the format specified in the assignment. */
    private void setUp(Machine M, String settings) {
        String[] settingsArr = settings.split("(\\s)");

        if (!settingsArr[0].equals("*")) {
            throw error("No * in first column of settings line");
        }
        if (settingsArr.length < M.numRotors() + 2) {
            throw error("Wrong number of arguments");
        }
        String[] rotors = new String[M.numRotors()];
        for (int i = 0; i < M.numRotors(); i++) {
            rotors[i] = settingsArr[i + 1];
        }
        M.insertRotors(rotors);

        String setting = settingsArr[M.numRotors() + 1];
        M.setRotors(setting);

        if (settingsArr.length != M.numRotors() + 2) {
            int a = 2;
            if (settingsArr[M.numRotors() + 2].charAt(0) != '(') {
                String ringSet = settingsArr[M.numRotors() + 2];
                M.setRings(ringSet);
                a = 3;
            }
            String cycle = " ";
            for (int i = M.numRotors() + a; i < settingsArr.length; i++) {
                cycle += settingsArr[i];
                if (i != settingsArr.length - 1) {
                    cycle += " ";
                }
            }
            cycle = cycle.trim();
            Permutation newplugboard = new Permutation(cycle, _alphabet);
            M.setPlugboard(newplugboard);
        }
    }

    /** Return true iff verbose option specified. */
    static boolean verbose() {
        return _verbose;
    }

    /** Print MSG in groups of five (except that the last group may
     *  have fewer letters). */
    private void printMessageLine(String msg) {
        msg = msg.replace(" ", "");
        String messaged = "";
        while (msg.length() >= 6) {
            messaged = messaged + msg.substring(0, 5) + " ";
            msg = msg.substring(5);
        }
        _output.println(messaged + msg);
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
