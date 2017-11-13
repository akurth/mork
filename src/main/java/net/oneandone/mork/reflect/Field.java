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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.lang.reflect.Modifier;

/**
 * A Function that reads a Java Field. Both static and non-static fields
 * can be wrapped. Java Field means java.lang.reflect.Field.
 */

public class Field extends Function implements Bytecodes {
    /** The Java Field read by this Function */
    private java.lang.reflect.Field field;

    public static Field forName(String name) {
        int idx;
        Class cl;

        idx = name.lastIndexOf('.');
        if (idx == -1) {
            return null;
        }
        cl = ClassRef.classFind(name.substring(0, idx));
        if (cl == null) {
            return null;
        }

        return forName(cl, name.substring(idx + 1));
    }

    /**
     * Creates a Field if it the Java Field has valid modifiers.
     * @param  cl    Class containing the Java field
     * @param  name  of the field
     * @return Field or null
     */
    public static Field forName(Class cl, String name) {
        java.lang.reflect.Field result;

        try {
            result = cl.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            return null;
        }

        return create(result);
    }

    /**
     * Create a new Field.
     * @param  fieldInit  Java Field read by this Function
     */
    public Field(java.lang.reflect.Field fieldInit) {
        /*  TODO      if (!Modifier.isPublic(fieldInit.getModifiers())) {
            throw new IllegalArgumentException();
            }*/

        field = fieldInit;
        // TODO
        field.setAccessible(true);
    }


    public static Field create(java.lang.reflect.Field fieldInit) {
        try {
            return new Field(fieldInit);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    //--

    /**
     * Tests whether this Field is static.
     * @return  true if the Java Field is static
     */
    public boolean isStatic() {
        return Modifier.isStatic(field.getModifiers());
    }

    /**
     * Gets the Function name.
     * @return the Function name
     */
    @Override
    public String getName() {
        return field.getName();
    }

    /**
     * Gets the result type.
     * @return  the result type
     */
    @Override
    public Class getReturnType() {
        return field.getType();
    }

    @Override
    public Class[] getParameterTypes() {
        if (isStatic()) {
            return NO_CLASSES;
        } else {
            return new Class[] { field.getDeclaringClass() };
        }
    }

    @Override
    public Class[] getExceptionTypes() {
        return NO_CLASSES;
    }

    //--

    /**
     * Reads this Field.
     * @param vals  an array of length null, or an array with the Object containing this Java Field
     * @return the Object stored in the Java Field
     */
    @Override
    public Object invoke(Object[] vals) {
        try {
            if (isStatic()) {
                return field.get(null);
            } else {
                return field.get(vals[0]);
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (IllegalAccessException e) {
            // constructor should prevent this
            throw new RuntimeException("can't access field");
        }
    }

    //--
    // manual serialization. Automatic serialization is not possible,
    // because Java Fields are not serializable.

    /**
     * Writes a Field.
     * @param  out  target to write to
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        write(out, field);
    }

    /**
     * Reads a Field
     * @param  in  source to read from
     */
    private void readObject(ObjectInputStream in)
            throws ClassNotFoundException, NoSuchFieldException,
                   IOException {
        field = read(in);
    }

    //-- additional functionality for Java Fields

    /**
     * Writes a Java Field.
     * @param  out    target to write to
     * @param  field  Java Field to be written
     */
    public static void write(ObjectOutput out, java.lang.reflect.Field field) throws IOException {
        if (field == null) {
            ClassRef.write(out, null);
        } else {
            ClassRef.write(out, field.getDeclaringClass());
            out.writeUTF(field.getName());
        }
    }

    /**
     * Reads a Java Field.
     * @param  in  source to read from
     * @return  the Java Field read
     */
    public static java.lang.reflect.Field read(ObjectInput in)
            throws ClassNotFoundException, NoSuchFieldException, IOException {
        Class cl;
        String name;

        cl = ClassRef.read(in);
        if (cl == null) {
            return null;
        } else {
            name = in.readUTF();
            return cl.getDeclaredField(name);
        }
    }

    @Override
    public void translate(Code dest) {
        ClassRef type;

        if (isStatic()) {
            dest.emit(GETSTATIC, new FieldRef(field));
        } else {
            type = new ClassRef(getParameterTypes()[0]);
            if (!type.isPrimitive()) {
                dest.emit(CHECKCAST, type);
            }
            dest.emit(GETFIELD, new FieldRef(field));
        }
    }
}
