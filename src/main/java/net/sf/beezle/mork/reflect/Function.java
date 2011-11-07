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

package de.mlhartme.mork.reflect;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

import de.mlhartme.mork.classfile.Code;

/**
 * <p> Interface for anything that can be invoked. Every function
 * has a name, a return type, and parameter types. Invocations takes
 * an array with the number of parameters. Each element must be
 * assignable to the corresponding argument types. Invocations return
 * an Object assignable to the function's return type.
 *
 * Use Object types to get around "static" type checking. </p>
 *
 * Note: Java reflection automatically wraps and unwraps
 * primitive types, but not arrays of primitive types.
 */

public abstract class Function implements Serializable {
    public static final Class<?>[] NO_CLASSES = new Class[0];

    /**
     * Gets the Function name.
     * @return  name  the Function name
     */
    public abstract String getName();

    /**
     * Gets the return type of this Function. Every invocation of this
     * Function returns a value assignable to the type returned by this
     * method.
     * @return  the return type of the Function
     */
    public abstract Class<?> getReturnType();

    /**
     * Gets the parameter types of this function.
     * @return  parameter types
     */
    public abstract Class<?>[] getParameterTypes();

    public abstract Class<?>[] getExceptionTypes();

    /**
     * Invokes the function.
     * API.
     * @param   vals   arguments to call the Function with; length
     *                 and types must conform the argument count and the
     *                 argument types of the Function
     * @return  what the functions returned; the type must conform the
     *          return type of the Function
     */
    public abstract Object invoke(Object[] vals)
        throws InvocationTargetException;

    // N in method name is necessary to reliably distinguished
    // the one-argument-version from invoke(Object[])

    public Object invokeN() throws InvocationTargetException {
        return invoke(new Object[0]);
    }
    public Object invokeN(Object a) throws InvocationTargetException {
        return invoke(new Object[] { a });
    }
    public Object invokeN(Object a, Object b) throws InvocationTargetException {
        return invoke(new Object[] { a, b });
    }
    public Object invokeN(Object a, Object b, Object c) throws InvocationTargetException {
        return invoke(new Object[] { a, b, c });
    }


    /**
     * Tests if the functions can be called with arguments of the
     * specified types.
     * @param   args  arguement types
     * @return  true, if the function can be called with these types
     */
    public boolean matches(Class<?>[] args) {
        int i;
        Class<?>[] paras;

        paras = getParameterTypes();
        if (args.length != paras.length) {
            return false;
        }
        for (i = 0; i < args.length; i++) {
            if (!paras[i].isAssignableFrom(args[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets a String representation for this Function. Implementors don't
     * need to overwrite this methods, a reasonable representation can be
     * built using the Function name, the return type and the argument
     * types.
     * @return the String representation
     */
    @Override
    public String toString() {
        StringBuilder buf;
        int i;
        Class<?>[] paras;

        buf = new StringBuilder();
        buf.append(getReturnType().getName());
        buf.append(' ');
        buf.append(getName());
        buf.append('(');
        paras = getParameterTypes();
        for (i = 0; i < paras.length; i++) {
            if (i > 0) {
                buf.append(", ");
            }
            buf.append(paras[i].getName());
        }
        buf.append(')');
        return buf.toString();
    }

    /**
     * Generate bytecode equivalent to invoke(). The generated code expects arguments
     * and the result on the operand stack.
     */
    public abstract void translate(Code dest);
}
