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

import java.io.File;
import java.io.IOException;

public class JobTest extends TestCase {
    private Job a;
    private Job b;

    public void testFields() throws IOException {
        a = new Job(".", false, false, "a");
        assertEquals(new File("a"), a.source);
        assertEquals(new File("."), a.outputPath);
        assertNull(a.listing);
    }

    public void testListing() throws IOException {
        a = new Job(null, false, true, "a");
        assertNotNull(a.listing);
        assertTrue(!a.listing.equals(a.source));
        assertTrue(a.listing.getName().endsWith(Job.LST_SUFFIX));

        a = new Job(null, false, true, "a.xy");
        assertNotNull(a.listing);
        assertTrue(!a.listing.equals(a.source));
        assertTrue(a.listing.getName().endsWith(Job.LST_SUFFIX));

        a = new Job(null, false, true, "a" + Job.LST_SUFFIX);
        assertNotNull(a.listing);
        assertTrue(!a.listing.equals(a.source));
        assertTrue(a.listing.getName().endsWith(Job.LST_SUFFIX));
    }

    public void testEqual() throws IOException {
        a = new Job("a");
        b = new Job("a");
        assertEquals(a, b);

        a = new Job(null, false, true, "a");
        b = new Job(null, false, true, "a");
        assertEquals(a, b);

        a = new Job(".", false, true, "a");
        b = new Job(".", false, true, "a");
        assertEquals(a, b);
    }

    public void testOutputDiff() throws IOException {
        a = new Job(".", false, true, "a");
        b = new Job(new File(".").getParent(), false, true, "a");
        assertTrue(!a.equals(b));

        a = new Job(".", false, true, "a");
        b = new Job(null, false, true, "a");
        assertTrue(!a.equals(b));

        a = new Job(null, false, true, "a");
        b = new Job(".", false, true, "a");
        assertTrue(!a.equals(b));
    }

    public void testSourceDiff() throws IOException {
        a = new Job("a");
        b = new Job("b");
        assertTrue(!a.equals(b));
    }

    public void testFlagDiff() throws IOException {
        a = new Job(null, false, true, "a");
        b = new Job(null, false, false, "a");
        assertTrue(!a.equals(b));
    }
}
