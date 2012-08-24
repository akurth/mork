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

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PrefixSetTest {
    public static void main(String[] args) {
        boolean[] dividable = new boolean[1000000];
        List<Integer> primes;
        int last;

        primes = new ArrayList<Integer>();
        for (int i = 2; i < dividable.length; i++) {
            if (!dividable[i]) {
                primes.add(i);
                for (int j = i * i; j > 0 && j < dividable.length; j += i) {
                    dividable[j] = true;
                }
            }
        }
        System.out.println("primes: " + primes.size());

        last = -10;
        for (Integer current : primes) {
            if (last + 2 == current) {
                System.out.println(last + ", " + current);
            }
            last = current;
        }
    }

    @Test
    public void grow() {
        PrefixSet set;

        set = new PrefixSet();
        for (int i = 0; i < 40; i++) {
            assertTrue(set.addUnpacked(i));
        }
        for (int i = 0; i < 40; i++) {
            assertFalse("" + i, set.addUnpacked(i));
        }
    }

    @Test
    public void qualityK1() {
        PrefixSet set;

        set = new PrefixSet();
        for (int i = 0; i < Prefix.BASE - 1; i++) {
            set.addUnpacked(i);
        }
        assertEquals(1.0, set.hashQuality(), 0.01);
    }

    @Test
    public void qualityK2() {
        PrefixSet set;

        set = new PrefixSet();
        for (int i = 0; i < Prefix.BASE - 1; i++) {
            for (int j = 0; j < Prefix.BASE - 1; j++) {
                set.addUnpacked(i, j);
            }
        }
        assertEquals(1.0, set.hashQuality(), 0.01);
    }
}
