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

import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;

public class JobTest extends TestCase {
    private Job a;
    private Job b;

    public void testFields() throws IOException {
        a = new Job(".", false, "a");
        assertEquals(new File("a"), a.source);
        assertEquals(new File("."), a.outputPath);
        assertNull(a.listing);
    }

    public void testListing() throws IOException {
        a = new Job(null, true, "a");
        assertNotNull(a.listing);
        assertTrue(!a.listing.equals(a.source));
        assertTrue(a.listing.getName().endsWith(Job.LST_SUFFIX));

        a = new Job(null, true, "a.xy");
        assertNotNull(a.listing);
        assertTrue(!a.listing.equals(a.source));
        assertTrue(a.listing.getName().endsWith(Job.LST_SUFFIX));

        a = new Job(null, true, "a" + Job.LST_SUFFIX);
        assertNotNull(a.listing);
        assertTrue(!a.listing.equals(a.source));
        assertTrue(a.listing.getName().endsWith(Job.LST_SUFFIX));
    }

    public void testEqual() throws IOException {
        a = new Job("a");
        b = new Job("a");
        assertEquals(a, b);

        a = new Job(null, true, "a");
        b = new Job(null, true, "a");
        assertEquals(a, b);

        a = new Job(".", true, "a");
        b = new Job(".", true, "a");
        assertEquals(a, b);
    }

    public void testOutputDiff() throws IOException {
        a = new Job(".", true, "a");
        b = new Job(new File(".").getParent(), true, "a");
        assertTrue(!a.equals(b));

        a = new Job(".", true, "a");
        b = new Job(null, true, "a");
        assertTrue(!a.equals(b));

        a = new Job(null, true, "a");
        b = new Job(".", true, "a");
        assertTrue(!a.equals(b));
    }

    public void testSourceDiff() throws IOException {
        a = new Job("a");
        b = new Job("b");
        assertTrue(!a.equals(b));
    }

    public void testFlagDiff() throws IOException {
        a = new Job(null, true, "a");
        b = new Job(null, false, "a");
        assertTrue(!a.equals(b));
    }
}
