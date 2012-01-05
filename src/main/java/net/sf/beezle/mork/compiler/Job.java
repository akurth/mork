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

import net.sf.beezle.sushi.util.Strings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * A compile job. TODO: invoke mork from here; parsing the command line results
 * in an array of Job, the main class should become real thin.
 */

public class Job {
    /** source file, never null */
    public final File source;

    /** listing file, null for no listing */
    public final File listing;

    /**
     * -d argument that applies to this file. Null if not specified,
     * otherwise an existing directory.
     */
    public final File outputPath;

    public final int k;

    public static final String SRC_SUFFIX = ".mapper";
    public static final String LST_SUFFIX = ".lst";

    /**
     * @param srcName name of the source file.
     */
    public Job(String srcName) throws IOException {
        this(null, false, srcName);
    }

    public Job(String outputPathName, boolean listing, String srcName) throws IOException {
        this(outputPathName, 1, listing, srcName);
    }

    public Job(String outputPathName, int k, boolean listing, String srcName) throws IOException {
        String baseName;

        if (outputPathName == null) {
            this.outputPath = null;
        } else {
            this.outputPath = new File(outputPathName);
            if (!outputPath.isDirectory()) {
                throw new FileNotFoundException("no such directory: " + outputPath);
            }
        }
        this.k = k;
        this.source = new File(srcName);
        if (listing) {
            baseName = Strings.removeRightOpt(source.getName(), SRC_SUFFIX);
            // TODO: configurable target
            this.listing = new File("target/" + baseName + LST_SUFFIX);
        } else {
            this.listing = null;
        }
    }

    @Override
    public boolean equals(Object obj) {
        Job job;

        if (obj instanceof Job) {
            job = (Job) obj;

            return  source.equals(job.source)
                && eq(listing, job.listing) && eq(outputPath, job.outputPath);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return source.hashCode();
    }

    private static boolean eq(File a, File b) {
        if (a != null) {
            return a.equals(b);
        } else {
            return b == null;
        }
    }

    @Override
    public String toString() {
        return "source=" + source + " listing=" + listing + " output=" + outputPath;
    }
}
