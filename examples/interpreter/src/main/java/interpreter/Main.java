package interpreter;

import net.oneandone.mork.mapping.Mapper;

/** Command line invokation. */

public class Main {
    public static void main(String[] args) {
        Mapper mapper;
        Object[] result;
        Script script;

        if (args.length != 1) {
            System.out.println("usage: interpreter.Main <filename>");
        } else {
            mapper = new Mapper("interpreter.Mapper");
            result = mapper.run(args[0]);
            if (result == null) {
                return;
            }
            script = (Script) result[0];
            script.run();
        }
    }
}
