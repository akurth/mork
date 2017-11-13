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
package net.oneandone.mork.reflect;

import net.oneandone.mork.classfile.Code;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

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
