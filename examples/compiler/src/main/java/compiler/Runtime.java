package compiler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Runtime Library needed to run a compiled program.
 */

public class Runtime {
    private static final BufferedReader INPUT = new BufferedReader(new InputStreamReader(System.in));

    public static int inputInt() throws IOException {
        String str;

        str = INPUT.readLine();
        return Integer.parseInt(str);
    }

    public static String inputString() throws IOException {
        return INPUT.readLine();
    }

    public static void printInt(int a) {
        System.out.print("" + a);
    }

    public static void printString(String str) {
        System.out.print(str);
    }
}
