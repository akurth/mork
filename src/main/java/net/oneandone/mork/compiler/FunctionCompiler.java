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
package net.oneandone.mork.compiler;

import net.oneandone.mork.classfile.Bytecodes;
import net.oneandone.mork.classfile.Code;
import net.oneandone.mork.reflect.Function;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates classes derived from CompiledFunctionBase classes. Generates zero, one (or multiple
 * if the code attribute grows to big) instances of InvocationCode that perform the actual work.
 * Support multiple instances of InvocationCode is currently rarely used because it takes quite a
 * few functions to exceed InvocationCode's code attribute size. However, future versions might
 * generate more code into InvocationCode.
 */

public class FunctionCompiler extends CustomCompiler implements Bytecodes {
    /**
     * Max number of functions in a single InvocationCode instance.
     * (The average size of a compiled function in Mork 0.3.4 is 40 bytes)
     */
    private static final int MAX_SIZE = 800;

    private final String className;

    /** Generated classes; list of InvocationCode instances. */
    private final List<InvocationCode> classes;

    /** null or last element of classes. */
    private InvocationCode current;

    /**
     * Functions previously translated.
     * Prevents that the same copy functions is written multiple times.
     */
    private Map<Function, Object[]> done;

    public FunctionCompiler(String className) {
        this.className = className;
        this.classes = new ArrayList<InvocationCode>();
        this.current = null;
        this.done = new HashMap<Function, Object[]>();
    }

    @Override
    public boolean matches(Class<?> type) {
        return Function.class.isAssignableFrom(type);
    }

    @Override
    public void beginTranslation(Object obj, Code dest) {
        if (current == null) {
            current = new InvocationCode(className + (classes.size() + 1));
            classes.add(current);
        }

        if (!current.reuse((Function) obj, dest, done)) {
            current.translate((Function) obj, dest, done);
        }
        if (current.size() == MAX_SIZE) {
            // create a new class if for the next call to this method
            current = null;
        }
    }

    @Override
    public void endTranslation(Object obj, Code dest) {
        // do nothing
    }

    @Override
    public Class<?>[] getFieldTypes() {
        return new Class[] {};
    }

    @Override
    public Object[] getFieldObjects(Object obj) {
        return new Object[0];
    }

    public void save(String fileBase) throws IOException {
        int i;
        int max;

        max = classes.size();
        for (i = 0; i < max; i++) {
            ((InvocationCode) classes.get(i)).save(new File(fileBase + (i + 1) + ".class"));
        }
        done = null;
    }
}
