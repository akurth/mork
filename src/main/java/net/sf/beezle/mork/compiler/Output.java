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

import net.sf.beezle.mork.mapping.PrintStreamErrorHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 * Global IO configuration. Public fields - global variables, to allow modifiction at any time.
 * Modification is necessary for file-specific settings, e.g. the listing stream.
 *
 * No code other than this class and the Main class should use System.out or System.err.
 */
public class Output extends PrintStreamErrorHandler {
    public PrintStream normal;
    public PrintStream verbose;
    public PrintStream statistics;

    /**
     * A print-stream version the listing file for the current job --
     * null for no listing.
     */
    public PrintStream listing;

    public Output() {
        this(System.err);
    }

    public Output(PrintStream errors) {
        super(errors);

        normal = System.out;
        verbose = null;
        statistics = null;
        listing = null;
    }

    //-- error and warning messages

    public void normal(String str) {
        if (normal != null) {
            normal.println(str);
        }
    }

    public void verbose(String str) {
        if (verbose != null) {
            verbose.println(str);
        }
    }

    public void statistics() {
        statistics("");
    }

    public void statistics(String str) {
        if (statistics != null) {
            statistics.println(str);
        }
    }

    //-- listing

    public void openListing(File listingFile) {
        if (listingFile != null) {
            try {
                listing = new PrintStream(new FileOutputStream(listingFile));
            } catch (FileNotFoundException e) {
                error(listingFile.getName(), "can't open listing file - listing disabled");
                listing = null;
            }
        } else {
            listing = null;
        }
    }

    public void listing(String str) {
        if (listing != null) {
            listing.println(str);
        }
    }
}
