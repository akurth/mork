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

import net.oneandone.sushi.util.Strings;

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

    public final int threadCount;

    public static final String SRC_SUFFIX = ".mapper";
    public static final String LST_SUFFIX = ".lst";

    /**
     * @param srcName name of the source file.
     */
    public Job(String srcName) throws IOException {
        this(null, false, srcName);
    }

    public Job(String outputPathName, boolean listing, String srcName) throws IOException {
        this(outputPathName, 1, 1, listing, srcName);
    }

    public Job(String outputPathName, int k, int threadCount, boolean listing, String srcName) throws IOException {
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
        this.threadCount = threadCount;
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
