/*
 * Copyright 1&1 Internet AG, http://www.1and1.org
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package compiler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

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
