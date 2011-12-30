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

import junit.framework.TestCase;

import java.io.IOException;

/**
 * Test parsing command line options.
 */
public class MainTest extends TestCase {
    private Main main;
    private Job[] jobs;
    private Output output;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        output = new Output();
        output.normal = null;
        main = new Main(output);
        jobs = null;
    }

    public void testHelp() {
        assertEquals(Main.HELP, main.run(new String[] {}));
        assertEquals(Main.HELP, main.run(new String[] { "-help" }));
    }

    public void testNoJobForHelp() throws IOException {
        jobs = main.parseOptions(new String[] { "-help" });
        assertEquals(0, jobs.length);
    }

    public void testOneJob() throws IOException {
        jobs = main.parseOptions(new String[] { "a" });
        assertEquals(1, jobs.length);
        assertEquals(new Job("a"), jobs[0]);
    }

    public void testTwoJobs() throws IOException {
        jobs = main.parseOptions(new String[] { "a", "b" });
        assertEquals(2, jobs.length);
        assertEquals(new Job("a"), jobs[0]);
        assertEquals(new Job("b"), jobs[1]);
    }

    public void testListing() throws IOException {
        jobs = main.parseOptions(new String[] { "-lst", "a", "b" });
        assertEquals(2, jobs.length);
        assertEquals(new Job(null, false, true, "a"), jobs[0]);
        assertEquals(new Job(null, false, true, "b"), jobs[1]);
    }

    public void testOutputPath() throws IOException {
        jobs = main.parseOptions(new String[] { "-d", ".", "a", "b" });
        assertEquals(2, jobs.length);
        assertEquals(new Job(".", false, false, "a"), jobs[0]);
        assertEquals(new Job(".", false, false, "b"), jobs[1]);
    }
}
