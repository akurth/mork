package command;

import net.oneandone.mork.mapping.Mapper;

public class Main {
    public static void main(String[] args) {
        Mapper mapper;
        Object[] tmp;
        Command command;

        if (args.length != 1) {
            System.out.println("command: add frontends to command line tools");
            System.out.println("  usage: command.Main <command file>");
        } else {
            mapper = new Mapper("command.Mapper");
            tmp = mapper.run(args[0]);
            if (tmp == null) {
                // runOrMessage has issued an error message
                System.exit(1);
            }
            command = (Command) tmp[0];
            command.run();
        }
        System.exit(0);     // just returning doesn't kill the gui threads
    }
}
