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
import net.sf.beezle.sushi.cli.Cli;
import net.sf.beezle.sushi.cli.Command;
import net.sf.beezle.sushi.cli.Option;
import net.sf.beezle.sushi.cli.Remaining;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Kick off code for the compiler, connects the Mork class with the operating system's command line.
 * I.e it defines the main method. Knowledge about command line syntax goes here, not into the Mork
 * class.
 */
public class Main extends Cli implements Command {
    public static void main(String[] args) {
        Main main;

        main = new Main(new Output());
        System.exit(main.run(args));
    }

    @Option("help")
    private boolean help;

    @Option("verbose")
    private boolean verbose;

    @Option("lst")
    private boolean lst;

    @Option("stat")
    private boolean stat;

    @Option("d")
    private String directory;

    @Option("k")
    private int k;

    @Option("t")
    private int threadCount = Runtime.getRuntime().availableProcessors();
;
    @Option("mapper")
    private String mapper;

    private List<String> files = new ArrayList<String>();

    @Remaining
    public void addRemaining(String file) {
        files.add(file);
    }

    private final Output output;
    private Function mapperFn;

    public Main(Output output) {
        this.output = output;
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
            output.verbose = System.out;
        }
        if (lst) {
            listing = true;
        }
        if (stat) {
            output.statistics = System.out;
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


    @Override
    public void invoke() throws Exception {
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
