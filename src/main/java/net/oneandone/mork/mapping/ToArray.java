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
package net.oneandone.mork.mapping;

import net.oneandone.mork.classfile.Bytecodes;
import net.oneandone.mork.classfile.ClassRef;
import net.oneandone.mork.classfile.Code;
import net.oneandone.mork.classfile.MethodRef;
import net.oneandone.mork.compiler.Util;
import net.oneandone.mork.reflect.Arrays;
import net.oneandone.mork.reflect.Function;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.util.List;

/**
 * Invokation creates an array initialized with the List passed as an
 * argument.
 * TODO: replace this by an ArrayConstructor constructor (that returns
 * an empty array) and some composition? Move into reflect package?
 */

public class ToArray extends Function implements Bytecodes {
    /** Component type of the array. */
    private Class componentType;

    public ToArray(Class componentType) {
        this.componentType = componentType;
    }


    /*
     * Gets the Function name.
     * @return  the function name
     */
    @Override
    public String getName() {
        return "To" + componentType.getName() + "Array";
    }

    /**
     * Gets the result type of this Function.
     * @return  the result type
     */
    @Override
    public Class getReturnType() {
        return Arrays.getArrayClass(componentType);
    }

    /**
     * Gets the argument count of this Function.
     * @return  the argument count
     */
    @Override
    public Class[] getParameterTypes() {
        return new Class[] { List.class };
    }

    @Override
    public Class[] getExceptionTypes() {
        return NO_CLASSES;
    }

    @Override
    public Object invoke(Object[] vals) {
        int i, max;
        Object result; // not Object[] because of arrays of primitive types
        List lst;

        lst = (List) vals[0];
        max = lst.size();
        result = Array.newInstance(componentType, max);
        for (i = 0; i < max; i++) {
            Array.set(result, i, lst.get(i));
        }
        return result;
    }

    //--
    // Manual serialization. Automatic Serialization is not possible
    // because Java Constructors are not serializable.

    /**
     * Writes this Constructor.
     * @param  out  target to write to
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        ClassRef.write(out, componentType);
    }

    /**
     * Reads this Constructor.
     * @param  in  source to read from
     */
    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException, NoSuchMethodException {
        componentType = ClassRef.read(in);
    }

    @Override
    public void translate(Code dest) {
        int lstVar; // the list, which is turned into an array
        int maxVar; // local variable for upper bound
        int iVar; // loop variable
        int arrayVar;
        int startLabel;
        int endLabel;
        ClassRef type;
        ClassRef listRef;

        listRef = new ClassRef(java.util.List.class);

        type = new ClassRef(componentType);

        lstVar = dest.allocate(listRef);
        maxVar = dest.allocate(ClassRef.INT);
        iVar = dest.allocate(ClassRef.INT);
        arrayVar = dest.allocate(ClassRef.OBJECT);

        dest.emit(CHECKCAST, listRef);
        dest.emit(DUP);
        dest.emit(ASTORE, lstVar);
        dest.emit(INVOKEINTERFACE,
                  MethodRef.ifc(listRef, ClassRef.INT, "size"));
        dest.emit(DUP);
        dest.emit(ISTORE, maxVar);
        type.emitArrayNew(dest);
        dest.emit(ASTORE, arrayVar);
        dest.emit(LDC, 0);
        dest.emit(ISTORE, iVar);
        startLabel = dest.currentLabel();
        endLabel = dest.declareLabel();
        dest.emit(ILOAD, iVar);
        dest.emit(ILOAD, maxVar);
        dest.emit(IF_ICMPGE, endLabel);
        dest.emit(ALOAD, arrayVar);
        dest.emit(ILOAD, iVar);
        dest.emit(ALOAD, lstVar);
        dest.emit(ILOAD, iVar);
        dest.emit(INVOKEINTERFACE,
            MethodRef.ifc(listRef, ClassRef.OBJECT, "get", ClassRef.INT));
        Util.unwrap(componentType, dest);
        if (!type.isPrimitive()) {
            dest.emit(CHECKCAST, type);
        }
        type.emitArrayStore(dest);
        dest.emit(IINC, iVar, 1);
        dest.emit(GOTO, startLabel);
        dest.defineLabel(endLabel);
        dest.emit(ALOAD, arrayVar);
    }
}
