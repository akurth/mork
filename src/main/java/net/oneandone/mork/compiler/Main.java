/*
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

import net.oneandone.inline.Cli;
import net.oneandone.mork.misc.GenericException;
import net.oneandone.mork.reflect.Function;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Kick off code for the compiler, connects the Mork class with the operating system's command line.
 * I.e it defines the main method. Knowledge about command line syntax goes here, not into the Mork
 * class.
 */
public class Main {
    public static void main(String[] args) {
        System.exit(doMain(null, args));
    }

    private static Output redirect;

    public static int doMain(Output redirect, String[] args) {
        Cli cli;

        Main.redirect = redirect;
        try {
            cli = new Cli();
            cli.addDefault(Main.class, "notused -help -verbose -lst -stat -d=null -k -t -mapper=null file*");
            cli.run(args);
        } finally {
            Main.redirect = null;
        }
        return 0;
    }

    private final boolean help;
    private final boolean verbose;
    private final boolean lst;
    private final boolean stat;
    private final String directory;
    private final int k;
    private final int threadCount;
    private final String mapper;
    private List<String> files;

    private final Output output;
    private Function mapperFn;

    public Main(boolean help, boolean verbose, boolean lst, boolean stat, String directory, int k, int threadCount, String mapper, List<String> files) {
        this.help = help;
        this.verbose = verbose;
        this.lst = lst;
        this.stat = stat;
        this.directory = directory;
        this.k = k;
        this.threadCount = threadCount == 0 ?  Runtime.getRuntime().availableProcessors() : threadCount;
        this.mapper = mapper;
        this.files = files;
        this.output = redirect == null ? new Output() : redirect;
        this.mapperFn = null;
    }

    /**
     * @return null if errors have been reported
     */
    public Job[] jobs() {
        Job[] jobs;
        String outputPath;
        boolean listing;
        String errorPos = "mork";

        outputPath = null;
        listing = false;
        if (verbose) {
            output.verbose = new PrintWriter(System.out, true);
        }
        if (lst) {
            listing = true;
        }
        if (stat) {
            output.statistics = new PrintWriter(System.out, true);
        }
        if (directory != null) {
            outputPath = directory;
        }
        if (mapper != null) {
            try {
                mapperFn = MorkMapper.lookupMapperFn(mapper, Specification.class);
            } catch (GenericException e) {
                output.error(errorPos, e);
            }
        }

        jobs = new Job[files.size()];
        for (int j = 0; j < jobs.length; j++) {
            try {
                jobs[j] = new Job(outputPath, k, threadCount, listing, files.get(j));
            } catch (IOException e) {
                output.error(errorPos, e.getMessage());
                return null;
            }
        }
        return jobs;
    }

    public static final String USAGE =
      "usage: \"mork\" option* mapperfile*\n"
    + "option:\n"
    + " -help                 print this message and quit\n"
    + " -lst                  generate mapper listing\n"
    + " -d directory          sets the destination directory for class files\n"
    + " -k num                number of lookahead token, default is 1\n"
    + " -t num                parallel threads for pda generation, default is 1\n"
    + " -stat                 print mapper statistics\n"
    + " -verbose              issue overall progress information\n";

    public void printHelp() {
        output.normal("Mork compiler tool, version " + getVersion());
        output.normal("");
        output.normal(USAGE);
    }

    public String getVersion() {
        Package p;

        p = getClass().getPackage();
        if (p == null) {
            // mork has not been started from a jar file
            return "(unknown)";
        } else {
            return p.getSpecificationVersion()  + " (" + p.getImplementationVersion() + ")";
        }
    }


    public void run() throws Exception {
        Mork mork;
        Job[] jobs;
        int i;

        if (help || files.size() == 0) {
            printHelp();
            return;
        }
        jobs = jobs();
        mork = new Mork(output, mapperFn);
        for (i = 0; i < jobs.length; i++) {
            if (!mork.compile(jobs[i])) {
                throw new IOException("compile error");
            }
        }
    }
}
