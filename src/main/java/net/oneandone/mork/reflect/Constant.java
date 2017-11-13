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

import net.oneandone.mork.classfile.Bytecodes;
import net.oneandone.mork.classfile.ClassRef;
import net.oneandone.mork.classfile.Code;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Wraps an Object to make is useable as a constant Function, i.e.
 * a function always returning this Object.
 */

public class Constant extends Function implements Bytecodes {
    /** The result type. */
    private Class type;

    /** The Function name. */
    private String name;

    /** The value to be returned by all invokations. */
    private Object val;

    /**
     * Creates a new Constant.
     * @param typeInit  the result type
     * @param nameInit  the Function name
     * @param valInit   the value to be returned by all invokations
     */
    public Constant(Class typeInit, String nameInit, Object valInit) {
        if (!Serializable.class.isAssignableFrom(typeInit)) {
             throw new IllegalArgumentException("not serializable " +
                                                typeInit);
        }
        type = typeInit;
        name = nameInit;
        val = valInit;
    }

    /**
     * Replaces arguments to Functions by Constant Functions. Results in
     * Functions with fewer arguments.
     * @param  func  Functions whose arguments are filled in
     * @param  ofs   first argument to be filled
     * @param  paras Values for Constants used to fill arguments
     * @return       Function with filled arguments.
     */
    public static Function fillParas(Function func, int ofs, Object[] paras) {
        int i;

        if (func == null) {
            throw new NullPointerException();
        }

        for (i = 0; i < paras.length; i++) {
            // ofs is not changed!
            func = Composition.create(func, ofs,
                                      new Constant(paras[i].getClass(),
                                                   "arg"+i, paras[i]));
            if (func == null) {
                throw new RuntimeException();
            }
        }
        return func;

    }

    //--

    /**
     * Gets the Function name.
     * @return  the Function name
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Gets the result type.
     * @return  the result type.
     */
    @Override
    public Class getReturnType() {
        return type;
    }

    @Override
    public Class[] getParameterTypes() {
        return NO_CLASSES;
    }

    @Override
    public Class[] getExceptionTypes() {
        return NO_CLASSES;
    }

    /**
     * Gets the constant.
     * @param   paras   an Array of length 0
     * @return  the constant
     */
    @Override
    public Object invoke(Object[] paras) {
        return val;
    }

    //--
    // Manual serialization. Automatic serialization is not possible because
    // Java Methods are not serializable.

    /**
     * Writes this Function.
     * @param  out  target to write to
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        ClassRef.write(out, type);
        out.writeUTF(name);
        out.writeObject(val);
    }

    /**
     * Reads this Function.
     * @param   in  source to read from
     */
    private void readObject(ObjectInputStream in) throws ClassNotFoundException, NoSuchMethodException, IOException {
        type = ClassRef.read(in);
        name = in.readUTF();
        val = in.readObject();
    }

    @Override
    public void translate(Code dest) {
        dest.emitGeneric(LDC, new Object[] { val });
    }
}
