/**
 * Copyright 1&1 Internet AG, https://github.com/1and1/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.oneandone.mork.compiler;

import net.oneandone.mork.mapping.Mapper;

import java.io.File;
import java.io.IOException;

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
