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

package net.sf.beezle.mork.compiler;

import java.io.File;
import java.io.IOException;

import net.sf.beezle.mork.mapping.Mapper;

public class Benchmark {
    public static void main(String[] args) {
        System.exit(run(args));
    }

    public static int run(String[] args) {
        try {
            runUnchecked(args);
            return 0;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return -1;
        }
    }

    public static void runUnchecked(String[] args) throws IOException {
        int num;
        Mapper mapper;
        String[] files;

        if (args.length < 3) {
            throw new IOException("usage: \"benchmark\" count mapper file+");
        }
        try {
            num = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            throw new IOException("not a number: " + args[0]);
        }
        mapper = new Mapper(args[1]);
        files = new String[args.length - 2];
        System.arraycopy(args, 2, files, 0, files.length);
        run(false, num, mapper, files);
    }

    public static void run(boolean verbose, int count, Mapper mapper, String[] files) throws IOException {
        int i;
        int j;
        Object[] result;

        for (i = 0; i < count; i++) {
            if (verbose) {
                System.out.print("" + i);
            }
            for (j = 0; j < files.length; j++) {
                if (verbose) {
                    System.out.print(".");
                }
                result = mapper.run(new File(files[j]));
                if (result == null) {
                    throw new IOException("error mapping " + files[j]);
                }
            }
            if (verbose) {
                System.out.println();
            }
        }
    }
}
