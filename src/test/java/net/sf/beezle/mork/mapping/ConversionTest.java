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

package net.sf.beezle.mork.mapping;

import net.sf.beezle.mork.reflect.Method;
import net.sf.beezle.mork.reflect.Selection;
import net.sf.beezle.mork.semantics.Type;

import java.util.List;

public class ConversionTest extends CompareBase {
    public void testAssignableFromValue() {
        assertAssignableFrom(String.class, new Type(String.class, Type.VALUE));
        assertAssignableFrom(List.class, new Type(List.class, Type.VALUE));
        assertAssignableFrom(String[].class, new Type(String[].class, Type.VALUE));
        assertAssignableFrom(Integer.class, new Type(Integer.class, Type.VALUE));
        assertAssignableFrom(Integer.TYPE, new Type(Integer.class, Type.VALUE));
    }

    public void testAssignableFromOption() {
        assertAssignableFrom(String.class, new Type(String.class, Type.OPTION));
        assertAssignableFrom(List.class, new Type(List.class, Type.OPTION));
        assertAssignableFrom(String[].class, new Type(String[].class, Type.OPTION));
        assertAssignableFrom(Integer.class, new Type(Integer.class, Type.OPTION));
        assertAssignableFrom(Integer.TYPE, new Type(Integer.class, Type.OPTION));
    }

    public void testAssignableFromSequence() {
        assertAssignableFrom(String[].class, new Type(String.class, Type.SEQUENCE));
        assertAssignableFrom(List.class, new Type(String.class, Type.SEQUENCE));
        assertAssignableFrom(Integer[].class, new Type(Integer.class, Type.SEQUENCE));
        assertAssignableFrom(List.class, new Type(Integer.class, Type.SEQUENCE));
        assertAssignableFrom(List[].class, new Type(List.class, Type.SEQUENCE));
        assertAssignableFrom(List.class, new Type(List.class, Type.SEQUENCE));
    }

    public void testNotAssignable() {
        assertTrue(!Conversion.isAssignableFrom(String.class, new Type(Integer.class, Type.VALUE)));
    }

    public void testHasFormalArgument() {
        Selection sel;

        sel = Method.forName(ConversionTest.class, "arg");
        assertTrue(!Conversion.hasFormalArgument(sel, new Type(String.class)));
        assertTrue(Conversion.hasFormalArgument(sel, new Type(Integer.class)));
        assertTrue(Conversion.hasFormalArgument(sel, new Type(Character.class)));
    }

    public static void arg(int a) {
    }

    public static void arg(int a, char c) {
    }


    private void assertAssignableFrom(Class formal, Type actual) {
        assertTrue(Conversion.isAssignableFrom(formal, actual));
    }
}
