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
package net.oneandone.mork.mapping;

import net.oneandone.mork.reflect.Method;
import net.oneandone.mork.reflect.Selection;
import net.oneandone.mork.semantics.Type;

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
