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
import net.oneandone.mork.classfile.FieldRef;
import net.oneandone.mork.compiler.Util;

import java.lang.reflect.InvocationTargetException;

/**
 * I can't implement Option by feeding an Option-Value because I want to offer explicit definitions
 * like If(Expression), If(Expression,Expression)
 */

public class Option extends Function implements Bytecodes {
    // could be anything unique
    public static final Object TAG = new Integer(27);


    /** Java Method wrapped by this Function. */
    private final int optional;
    private final Function with;
    private final Class[] parameterTypes;

    //-- creation

    public Option(Function with, int optional) {
        Class[] tmp;

        this.with = with;
        this.optional = optional;

        tmp = this.with.getParameterTypes();
        parameterTypes = new Class[tmp.length];
        System.arraycopy(tmp, 0, parameterTypes, 0, tmp.length);

        // to have the same stack size when compiling, the optional
        // argument has to be a reference type
        parameterTypes[this.optional] = ClassRef.wrappedType(parameterTypes[this.optional]);
    }

    //--

    @Override
    public String getName() {
        return with.getName();
    }

    @Override
    public Class getReturnType() {
        return with.getReturnType();
    }

    @Override
    public Class[] getParameterTypes() {
        return parameterTypes;
    }

    @Override
    public Class[] getExceptionTypes() {
        return with.getExceptionTypes();
    }

    //--

    @Override
    public Object invoke(Object[] vals) throws InvocationTargetException {
        if (vals[optional] == TAG) {
            // TODO: primitive types!!
            // TODO: side effects on "vals"?
            vals[optional] =
                new ClassRef(getParameterTypes()[optional]).getDefault();
        }
        return with.invoke(vals);
    }

    // TODO: drop the parameter wrapping?
    @Override
    public void translate(Code dest) {
        int max;
        int ofs;
        int var;
        int[] vars;
        ClassRef type;
        ClassRef[] types;
        Class[] tmp;
        Class optType;
        int i;
        int unwrapOptionLabel;
        int pushTailingLabel;

        tmp = getParameterTypes();
        ofs = optional + 1;
        max = tmp.length - ofs;
        vars = new int[max];
        types = new ClassRef[max];
        // pop tailing args
        for (i = max - 1; i >= 0; i--) {
            type = new ClassRef(tmp[i + ofs]);
            types[i] = type;
            var = dest.allocate(type);
            vars[i] = var;
            type.emitStore(dest, var);
        }
        optType = with.getParameterTypes()[optional]; // ask "with", not tmp!

        unwrapOptionLabel = dest.declareLabel();
        pushTailingLabel = dest.declareLabel();

        // optional argument is on top of the stack now
        dest.emit(DUP); // optional argument is always a refenrence
        dest.emit(GETSTATIC,
                  new FieldRef(new ClassRef(Option.class), "TAG",
                                                    ClassRef.OBJECT));
        dest.emit(IF_ACMPNE, unwrapOptionLabel);

        // remove TAG, push constant
        dest.emit(POP); // discard TAG, always a reference
        new ClassRef(optType).emitDefault(dest);
        dest.emit(GOTO, pushTailingLabel);

        dest.defineLabel(unwrapOptionLabel);
        // optional value has to be a reference type!
        Util.unwrap(optType, dest);

        dest.defineLabel(pushTailingLabel);
        for (i = 0; i < max; i++) {
            types[i].emitLoad(dest, vars[i]);
        }
        with.translate(dest);
    }
}
