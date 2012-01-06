package net.sf.beezle.mork.compiler;

import net.sf.beezle.mork.scanner.Scanner;

import java.io.IOException;

public interface ConflictResolver {
    /** @return action */
    int run(Scanner scanner, int mode, int eof) throws IOException;
}
