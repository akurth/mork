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

package net.sf.beezle.mork.compiler;

import net.sf.beezle.mork.classfile.Bytecodes;
import net.sf.beezle.mork.classfile.ClassRef;
import net.sf.beezle.mork.classfile.Code;
import net.sf.beezle.mork.classfile.MethodRef;
import net.sf.beezle.mork.reflect.Field;
import net.sf.beezle.mork.reflect.Function;
import net.sf.beezle.mork.reflect.Method;
import net.sf.beezle.mork.reflect.Selection;

import java.lang.reflect.InvocationTargetException;

/**
 * Compiling an object compiles it fields. The reverse process
 * creates objects from fields.   This is like serializing into a class file instead of an
 * ObjectOutputStream. Instantiate the resulting class to "deserialize" an
 * "bytecode"-serialized object.
 */

public class GenericCompiler extends CustomCompiler implements Bytecodes {
    static {
        if (ObjectCompiler.MIN_INSTRUCTIONS < 3) {
            // I need three statements
            throw new IllegalArgumentException();
        }
    }
    private final Class<?> type;

    /**
     * Reader methods to obtain a field value. Methods have
     * to take one argument of type "type".
     */
    private final Function[] fields;

    private final Class<?>[] fieldTypes;

    /** Reference to constructor function. */
    private final MethodRef constr;

    /** Bytecode to invoke constructor. */
    private final int constrType;

    public GenericCompiler(Class<?> typeInit, String[] fieldNames) {
        this(typeInit, fieldNames, null);
    }

    /**
     * @param constrName  name of constructor function or null for
     *        real constructor.
     */
    public GenericCompiler(Class<?> typeInit, String[] fieldNames, String constrName) {
        int i;
        ClassRef[] tmp;

        type = typeInit;
        fields = new Function[fieldNames.length];
        fieldTypes = new Class[fields.length];
        for (i = 0; i < fields.length; i++) {
            fields[i] = findField(fieldNames[i]);
            fieldTypes[i] = fields[i].getReturnType();
        }

        if (constrName == null) {
            tmp = new ClassRef[fields.length];
            for (i = 0; i < fields.length; i++) {
                tmp[i] = new ClassRef(fields[i].getReturnType());
            }
            constr = MethodRef.constr(new ClassRef(type), tmp);
            constrType = INVOKESPECIAL;
        } else {
            constr = new MethodRef(findConstr(constrName));
            constrType = INVOKESTATIC;
        }
    }

    @Override
    public boolean matches(Class<?> c) {
        return type.equals(c);
    }

    @Override
    public Class<?>[] getFieldTypes() {
        return fieldTypes;
    }

    @Override
    public Object[] getFieldObjects(Object obj) {
        Object[] result;
        int i;

        result = new Object[fieldTypes.length];
        for (i = 0; i < result.length; i++) {
            try {
                result[i] = fields[i].invokeN(obj);
            } catch (InvocationTargetException e) {
                throw new RuntimeException("can't get field: " + fields[i]
                        + ": " + e);
            }
        }
        return result;
    }


    //--

    // helper method for constructor
    private Function findField(String name) {
        Selection slkt;
        Function f;

        if (name.indexOf('.') == -1) {
            name = type.getName() + "." + name;
        }
        slkt = Method.forName(name);
        if (slkt.size() == 0) {
            f = Field.forName(name);
            if (f != null) {
                slkt = slkt.add(new Selection(f));
            }
        }
        slkt = slkt.restrictArgumentCount(1);
        slkt = slkt.restrictArgumentType(0, type);
        switch (slkt.size()) {
        case 0:
            throw new RuntimeException("no such field: " + name);
        case 1:
            return slkt.getFunction();
        default:
            throw new RuntimeException("ambiguous field: " + name);
        }
    }

    /** Helper method for the constructor. */
    private java.lang.reflect.Method findConstr(String name) {
        Selection slkt;
        int i;

        slkt = Method.forName(name);
        slkt = slkt.restrictArgumentCount(fields.length);
        for (i = 0; i < fields.length; i++) {
            slkt.restrictArgumentType(i, fields[i].getReturnType());
        }
        switch (slkt.size()) {
        case 0:
            throw new RuntimeException("no such constructor: " + name);
        case 1:
            return ((Method) slkt.getFunction()).getRaw();
        default:
            throw new RuntimeException("constructor ambiguous: " + name);
        }
    }

    /** called before prepareing the arguments. **/
    @Override
    public void beginTranslation(Object obj, Code dest) {
        if (constrType == INVOKESPECIAL) {
            dest.emit(NEW, new ClassRef(type));
            dest.emit(DUP);
            // save reference that remains on the stack
            // when object() is finished
        }
    }

    /** actually invoke. **/
    @Override
    public void endTranslation(Object obj, Code dest) {
        dest.emit(constrType, constr);
    }
}
