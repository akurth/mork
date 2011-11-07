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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.lang.reflect.Modifier;

import de.mlhartme.mork.classfile.Bytecodes;
import de.mlhartme.mork.classfile.ClassRef;
import de.mlhartme.mork.classfile.Code;
import de.mlhartme.mork.classfile.FieldRef;

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

    //----------------------------------------------------------------------

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

    //-----------------------------------------------------------------------

    /**
     * Reads this Field.
     * @param    an array of length null, or an array with the Object
     *           containing this Java Field
     * @return   the Object stored in the Java Field
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

    //--------------------------------------------------------------------
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

    //-------------------------------------------------------------------
    //  additional functionality for Java Fields

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
            name = (String) in.readUTF();
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
