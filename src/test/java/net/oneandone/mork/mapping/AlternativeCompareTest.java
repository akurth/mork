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
package net.oneandone.mork.mapping;

import net.oneandone.mork.semantics.Alternative;

public class AlternativeCompareTest extends CompareBase {
    private Alternative a;
    private Alternative b;

    public void testNoArgs() {
        a = new Alternative(0, -1);
        b = new Alternative(0, -1);
        assertEquals(EQ, a.compare(a));
        assertEquals(EQ, a.compare(b));
    }

    public void testOneArg() {
        a = new Alternative(0, -1);
        a.addAll(ofs(7), attrs(1));
        b = new Alternative(0, -1);
        b.addAll(ofs(7), attrs(1));
        assertEquals(EQ, a.compare(a));
        assertEquals(EQ, b.compare(b));
        assertEquals(EQ, a.compare(b));
        assertEquals(EQ, b.compare(a));
    }

    // a a b b
    public void testLT() {
        a = new Alternative(0, -1);
        a.addAll(ofs(1, 3), attrs(2));
        b = new Alternative(0, -1);
        b.addAll(ofs(4, 9, 10), attrs(3));
        assertEquals(LT, a.compare(b));
        assertEquals(GT, b.compare(a));
        assertEquals(NE, a.compare(a));
        assertEquals(NE, b.compare(b));
    }

    // b b a a
    public void testGT() {
        a = new Alternative(0, -1);
        a.addAll(ofs(5, 6), attrs(2));
        b = new Alternative(0, -1);
        b.addAll(ofs(2, 4), attrs(2));
        assertEquals(GT, a.compare(b));
        assertEquals(LT, b.compare(a));
        assertEquals(NE, a.compare(a));
        assertEquals(NE, b.compare(b));
    }

    // a b a b
    public void testObviousNE() {
        a = new Alternative(0, -1);
        a.addAll(ofs(3, 5), attrs(2));
        b = new Alternative(0, -1);
        b.addAll(ofs(4, 6), attrs(2));
        assertEquals(NE, a.compare(b));
        assertEquals(NE, b.compare(a));
    }

    public void testMultipleNE() {
        a = new Alternative(0, -1);
        a.addAll(ofs(1, 3, 9), attrs(3));
        b = new Alternative(0, -1);
        b.addAll(ofs(1, 3, 9), attrs(3));
        assertEquals(NE, a.compare(a));
        assertEquals(NE, a.compare(b));
    }

    // a b a
    public void testAroundNE() {
        a = new Alternative(0, -1);
        a.addAll(ofs(0, 2), attrs(2));
        b = new Alternative(0, -1);
        b.addAll(ofs(1), attrs(1));
        assertEquals(NE, a.compare(b));
        assertEquals(NE, b.compare(a));
    }

    public void testAlt() {
        a = new Alternative(0, -1);
        b = new Alternative(0, -1);
        b.addAll(ofs(1), attrs(1));
        assertEquals(ALT, a.compare(b));
        assertEquals(ALT, b.compare(a));
    }
}
