/**
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

import net.oneandone.mork.classfile.ClassRef;
import net.oneandone.mork.classfile.Code;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Identity function. Invokation takes 1 argument and returns it unchanged.
 * Can be used to case a Value.
 */

public class Identity extends Function {
    /** result type. Wrapped for serialization. */
    private Class result;

    /** argument type. Wrapper for serialization. */
    private Class arg;

    /** The Function name */
    private String name;

    /**
     * Create an Identity. Without conversion
     * @param  nameInit   the Function name
     * @param  typeInit   the argument and result type
     */
    public Identity(String nameInit, Class typeInit) {
        this(nameInit, typeInit, typeInit);
    }

    /**
     * Create an Identity. With Conversion.
     * @param  nameInit    the Function name
     * @param  resultInit  the result type
     * @param  argInit     the argument type
     */
    public Identity(String nameInit, Class resultInit, Class argInit) {
        name = nameInit;
        result = resultInit;
        arg = argInit;
    }

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
     * @return  the result type
     */
    @Override
    public Class getReturnType() {
        return result;
    }

    @Override
    public Class[] getParameterTypes() {
        return new Class[] { arg };
    }

    @Override
    public Class[] getExceptionTypes() {
        return NO_CLASSES;
    }

    /**
     * Takes 1 argument and returns it. If this Identity is used to
     * case the value, a possible runtime exception is thrown by
     * java.lang.reflect.
     * @param   paras  array of length 1
     * @return  the array element supplied
     */
    @Override
    public Object invoke(Object[] paras) {
        return paras[0];
    }

    //--
    // Manual serialization. Automatic serialization is not possible because
    // Java Methods are not serializable.

    /**
     * Writes this Function.
     * @param  out  target to write to
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        ClassRef.write(out, result);
        ClassRef.write(out, arg);
        out.writeUTF(name);
    }

    /**
     * Reads this Function.
     * @param   in  source to read from
     */
    private void readObject(ObjectInputStream in) throws ClassNotFoundException, NoSuchMethodException, IOException {
        result = ClassRef.read(in);
        arg = ClassRef.read(in);
        name = in.readUTF();
    }

    @Override
    public void translate(Code code) {
        // do nothing
    }
}
