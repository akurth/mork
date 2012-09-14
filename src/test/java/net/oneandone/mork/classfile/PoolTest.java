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
package net.oneandone.mork.classfile;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PoolTest {
    @Test
    public void utf8() {
        check("");
        check("abc");
        check("hello, world!");
        for (char c = 0; c < 512; c++) {
            check("" + c);
            check("123" + c + "456");
        }
        for (char c = 517; c < 20048; c+=47) {
            check("" + c);
            check("xyz" + c + "XYZ");
        }
    }

    private void check(String str) {
        byte[] utf8;

        utf8 = Pool.toUtf8(str);
        assertEquals(str, Pool.fromUtf8(utf8, 3, utf8.length));
    }
}
