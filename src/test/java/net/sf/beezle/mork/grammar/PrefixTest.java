/**
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
package net.sf.beezle.mork.grammar;


import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PrefixTest {
    @Test
    public void empty() {
        Prefix p;

        p = prefix();
        p.step();
        assertEquals(0, p.size());
    }

    @Test
    public void symbol() {
        Prefix p;

        p = prefix(0);
        assertEquals(1, p.size());
        assertEquals(0, p.first());
        assertEquals(0, p.follows(0).length);
        assertEquals(" 0", p.toString());
    }

    @Test
    public void twoSymbols() {
        Prefix p;

        p = prefix(1, 2);
        assertEquals(2, p.size());
        assertEquals(1, p.first());
        assertTrue(Arrays.equals(new int[]{2}, p.follows(1)));
        assertEquals(" 1 2", p.toString());
    }

    @Test
    public void concat() {
        long left;
        Prefix right;
        long test;

        left = prefix(10, 11).data;
        right = prefix(20, 21, 22);
        test = Prefix.concat(left, right.data, 2);
        assertEquals(left, test);
        check(Prefix.concat(left, right.data, 3), 10, 11, 20);
        check(Prefix.concat(left, right.data, 4), 10, 11, 20, 21);
        check(Prefix.concat(left, right.data, 5), 10, 11, 20, 21, 22);
        check(Prefix.concat(left, right.data, 6), 10, 11, 20, 21, 22);
        check(Prefix.concat(left, right.data, 7), 10, 11, 20, 21, 22);
    }

    private void check(long actual, int ... expected) {
        assertTrue(Arrays.equals(Prefix.unpack(actual), expected));
    }

    private Prefix prefix(int ... symbols) {
        PrefixSet set;
        Prefix result;

        set = new PrefixSet();
        set.addUnpacked(symbols);
        result = set.iterator();
        result.step();
        return result;
    }

    @Test
    public void hash() {
        long prefix;
        boolean[] hit;
        int hash;

        prefix = 9;
        hit = new boolean[PrefixSet.SIZES[0]];
        hash = Prefix.hashFirst(prefix, hit.length);
        hit[hash] = true;
        for (int i = 1; i < hit.length; i++) {
            hash = Prefix.hashNext(prefix, hash, hit.length);
            assertFalse(hit[hash]);
            hit[hash] = true;
        }
    }
}
