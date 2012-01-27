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

import net.sf.beezle.mork.misc.GenericException;
import net.sf.beezle.mork.reflect.Function;

import java.io.IOException;

/**
 * Kick off code for the compiler, connects the Mork class with the operating system's command line.
 * I.e it defines the main method. Knowledge about command line syntax goes here, not into the Mork
 * class.
 */
public class Main {
    /**
     * Parse command-line and compile the specified files. Use <code>mainWithoutExit</code>
     * instead if you want to avoid <code>System.exit()</code>
     */
    public static void main(String[] args) {
        Main main;
        int result;

        main = new Main(new Output());
        result = main.run(args);
        System.exit(result);
    }

    // global options.
    private final Output output;
    private Function mapperFn;

    public Main(Output output) {
        this.output = output;
        this.mapperFn = null;
    }

    // some exit codes
    public static final int COMMANDLINE_ERROR = -43;
    public static final int COMPILE_ERROR = -44;
    public static final int HELP = -45;

    /**
     * Wraps <code>runCore</code> to catch unchecked exceptions and report them
     * as internal errors.
     */
    public int run(String[] args) {
        return runCore(args);
    }

    /** does not catch Throwable **/
    public int runCore(String[] args) {
        Mork mork;
        Job[] jobs;
        int i;

        jobs = parseOptions(args);
        if (jobs == null) {
            return COMMANDLINE_ERROR;
        }
        if (jobs.length == 0) {
            printHelp();
            return HELP;
        }
        mork = new Mork(output, mapperFn);
        for (i = 0; i < jobs.length; i++) {
            if (!mork.compile(jobs[i])) {
                return COMPILE_ERROR;
            }
        }
        return 0;
    }

    /**
     * @return null if errors have been reported
     */
    public Job[] parseOptions(String[] args) {
        int i;
        String opt;
        Job[] jobs;
        // global options propagated into all jobs:
        String outputPath;
        boolean listing;
        int k;
        String errorPos = "mork";

        outputPath = null;
        listing = false;
        k = 1;
        for (i = 0; i < args.length; i++) {
            opt = args[i];
            if (opt.equals("-help") || opt.equals("-h")) {
                return new Job[0];
            }
            if (opt.equals("-quiet")) {
                output.verbose = null;
            } else if (opt.equals("-verbose")) {
                output.verbose = System.out;
            } else if (opt.equals("-verbose:parsing")) {
                output.verboseParsing = System.out;
            } else if (opt.equals("-verbose:attribution")) {
                output.verboseAttribution = System.out;
            } else if (opt.equals("-verbose:translation")) {
                output.verboseTranslation = System.out;
            } else if (opt.equals("-lst")) {
                listing = true;
            } else if (opt.equals("-stat")) {
                output.statistics = System.out;
            } else if (opt.equals("-d")) {
                if (i + 1 >= args.length) {
                    output.error(errorPos, "missing directory name");
                    return null;
                }
                i++;
                outputPath = args[i];
            } else if (opt.equals("-k")) {
                if (i + 1 >= args.length) {
                    output.error(errorPos, "missing k number");
                    return null;
                }
                i++;
                k = Integer.parseInt(args[i]);
            } else if (opt.equals("-mapper")) {
                if (i + 1 >= args.length) {
                    output.error(errorPos, "missing function mapper name");
                    return null;
                }
                i++;
                try {
                    mapperFn = MorkMapper.lookupMapperFn(args[i], Specification.class);
                } catch (GenericException e) {
                    output.error(errorPos, e);
                }
            } else if (opt.startsWith("-")) {
                output.error(errorPos, "unknown option: " + opt);
                return null;
            } else {
                break;
            }
        }

        jobs = new Job[args.length - i];
        for (int j = i; j < args.length; j++) {
            try {
                jobs[j - i] = new Job(outputPath, k, listing, args[j]);
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
    + " -stat                 print mapper statistics\n"
    + " -lst                  generate mapper listing\n"
    + " -d directory          sets the destination directory for class files\n"
    + " -k num                specifies the number of lookahead token, default is 1\n"
    + " -quiet                suppress normal progress information\n"
    + " -verbose              issue overall progress information\n"
    + " -verbose:parsing      issue scanner and parsing progress information\n"
    + " -verbose:attribution  issue attribution progress information\n";

    public void printHelp() {
        output.normal("Mork compiler tool. ");
        output.normal("Version " + getVersion());
        output.normal("Copyright (C) Michael Hartmeier 1998-2012");
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

    private static final int WIDTH = 24;
}
