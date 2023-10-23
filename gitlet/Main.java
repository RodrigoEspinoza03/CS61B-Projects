package gitlet;

import java.io.IOException;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Rodrigo Espinoza
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) throws IOException {
        Archive arch = new Archive();
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        }
        if (args[0].equals("init")) {
            arch.init(args);
        } else if (args[0].equals("add")) {
            arch.add(args);
        } else if (args[0].equals("commit")) {
            arch.commit(args);
        } else if (args[0].equals("rm")) {
            arch.remove(args);
        } else if (args[0].equals("log")) {
            arch.log(args);
        } else if (args[0].equals("global-log")) {
            arch.globalLog(args);
        } else if (args[0].equals("find")) {
            arch.find(args);
        } else if (args[0].equals("status")) {
            arch.status(args);
        } else if (args[0].equals("checkout")) {
            arch.checkout(args);
        } else if (args[0].equals("branch")) {
            arch.branch(args);
        } else if (args[0].equals("rm-branch")) {
            arch.removeBranch(args);
        } else if (args[0].equals("reset")) {
            arch.reset(args);
        } else if (args[0].equals("merge")) {
            arch.merge(args);
        } else {
            System.out.println("No command with that name exist.");
        }
    }


}
