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

import net.oneandone.mork.mapping.PrintWriterErrorHandler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Global IO configuration. Public fields - global variables, to allow modifiction at any time.
 * Modification is necessary for file-specific settings, e.g. the listing stream.
 *
 * No code other than this class and the Main class should use System.out or System.err.
 */
public class Output extends PrintWriterErrorHandler {
    public PrintWriter normal;
    public PrintWriter verbose;
    public PrintWriter statistics;

    /**
     * A print-stream version the listing file for the current job --
     * null for no listing.
     */
    public PrintWriter listing;

    public Output() {
        this(new PrintWriter(System.err, true));
    }

    public Output(PrintWriter errors) {
        super(errors);

        normal = new PrintWriter(System.out, true);
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

    //-- listing

    public void openListing(File listingFile) {
        if (listingFile != null) {
            try {
                listing = new PrintWriter(new FileWriter(listingFile));
            } catch (IOException e) {
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
