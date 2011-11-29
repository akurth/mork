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

package net.sf.beezle.mork.reflect;

import net.sf.beezle.mork.classfile.ClassRef;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * A collection of functions. Immutable. Argument- and return-types
 * of functions are automatically wrapped/unwrapped.
 */

public class Selection {
    private final Function[] functions;

    public Selection() {
        functions = new Function[0];
    }

    public Selection(Function fn) {
        functions = new Function[] { fn };
    }

    public Selection(Collection fns) {
        int i;
        Iterator iterator;

        functions = new Function[fns.size()];
        iterator = fns.iterator();
        i = 0;
        while (iterator.hasNext()) {
            functions[i] = (Function) iterator.next();
            i++;
        }
    }

    public Selection(Function[] functionsInit) {
        functions = functionsInit;
    }

    //------------------------------------------------------------------

    /**
     * @return null for empty selections; otherwise, result is != null.
     */
    public Class calcResult() {
        int i;
        Class result;
        Class tmp;

        result = null;
        for (i = 0; i < functions.length; i++) {
            tmp = functions[i].getReturnType();
            tmp = ClassRef.wrappedType(tmp);
            result = ClassRef.commonBase(result, tmp);
            if (result == null) {
                throw new RuntimeException();
            }
        }
        return result;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public int size() {
        return functions.length;
    }

    public Function getFunction(int i) {
        return functions[i];
    }

    /**
     * @return null if not a function
     */
    public Function getFunction() {
        if (functions.length == 1) {
            return functions[0];
        } else {
            return null;
        }
    }

    //------------------------------------------------------------------

    public Selection add(Selection operand) {
        Function[] fns;

        if (operand.isEmpty()) {
            return this;
        } else if (this.isEmpty()) {
            return operand;
        } else {
            fns = new Function[size() + operand.size()];
            System.arraycopy(functions, 0, fns, 0, size());
            System.arraycopy(operand.functions, 0, fns, size(), operand.size());
            return new Selection(fns);
        }
    }

    //----------------------------------------------------------------------

    public Selection restrictArgumentType(int arg, Class type) {
        List<Function> lst;
        int i;
        Class tmp;
        Class[] paras;

        lst = new ArrayList<Function>();
        for (i = 0; i < functions.length; i++) {
            paras = functions[i].getParameterTypes();
            if (arg < paras.length) {
                tmp = paras[arg];
                tmp = ClassRef.wrappedType(tmp);
                if (tmp.isAssignableFrom(type)) {
                    lst.add(functions[i]);
                }
            }
        }
        return new Selection(lst);
    }

    public Selection restrictArgumentCount(int count) {
        List<Function> lst;
        int i;

        lst = new ArrayList<Function>();
        for (i = 0; i < functions.length; i++) {
            if (functions[i].getParameterTypes().length == count) {
                lst.add(functions[i]);
            }
        }
        return new Selection(lst);
    }

    //-------------------------------------------------------------------

    @Override
    public String toString() {
        StringBuilder buffer;
        int i;

        buffer = new StringBuilder();
        buffer.append("selection " + "{\n");
        for (i = 0; i < functions.length; i++) {
            buffer.append("  ");
            buffer.append(functions[i].toString());
            buffer.append('\n');
        }
        buffer.append("}");

        return buffer.toString();
    }
}
