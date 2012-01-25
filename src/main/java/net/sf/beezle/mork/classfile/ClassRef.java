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

package net.sf.beezle.mork.classfile;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class ClassRef extends Reference implements Bytecodes, Constants {
    //--
    // some common classRef constants
    // Note: must follow PRIMITIVE_.. in order to use them

    public static final ClassRef VOID    = new ClassRef(Void.TYPE);
    public static final ClassRef BOOLEAN = new ClassRef(Boolean.TYPE);
    public static final ClassRef INT     = new ClassRef(Integer.TYPE);
    public static final ClassRef CHAR    = new ClassRef(Character.TYPE);
    public static final ClassRef OBJECT  = new ClassRef("java.lang.Object");
    public static final ClassRef STRING  = new ClassRef("java.lang.String");

    public static final ClassRef[] NONE = new ClassRef[] {};

    //--

    public final String name; // component name
    public final int dimensions;
    public final Type componentType;

    public ClassRef(Class<?> c) {
        int d;

        d = 0;
        while (c.isArray()) {
            c = c.getComponentType();
            d++;
        }
        name = c.getName();
        dimensions = d;
        componentType = findType(name);
    }

    public ClassRef(String name) {
        this(name, 0, findType(name));
    }

    public ClassRef(String name, int dimensions) {
        this(name, dimensions, findType(name));
    }

    private ClassRef(String name, int dimensions, Type componentType) {
        if (name.startsWith("[")) {
            throw new IllegalArgumentException(name);
        }
        this.name = name;
        this.dimensions = dimensions;
        this.componentType = componentType;
    }

    @Override
    public ClassRef getOwner() {
        return this;
    }

    @Override
    public ClassDef lookup(Repository repository) throws ResolveException {
        try {
            return repository.lookup(dimensions == 0 ? name : "java.lang.Object" /* TODO: that the base class of the array class that should be returned */);
        } catch (IOException e) {
            throw new ResolveException(this, e);
        }
    }

    public boolean isArray() {
        return dimensions != 0;
    }

    public boolean isPrimitive() {
        return (dimensions == 0) && (componentType.id != T_REFERENCE);
    }

    public boolean isJavaLangObject() {
        return name.equals("java.lang.Object");
    }

    public int operandSize() {
        return getTypeCode().size;
    }


    private static Type findType(String name) {
        int i;
        Type cmp;

        for (i = 0; i < Type.PRIMITIVES.length; i++) {
            cmp = Type.PRIMITIVES[i];
            if (name.equals(cmp.name)) {
                return cmp;
            }
        }
        return Type.REFERENCE;
    }

    // returns Type.REFERENCE_TYPE;
    public static Type findComponent(Class<?> type) {
        int i;
        Type cmp;

        for (i = 0; i < Type.PRIMITIVES.length; i++) {
            cmp = Type.PRIMITIVES[i];
            if (type == cmp.type) {
                return cmp;
            }
        }
        return Type.REFERENCE;
    }

    @Override
    public boolean equals(Object obj) {
        ClassRef r;

        if (obj instanceof ClassRef) {
            r = (ClassRef) obj;
            return (dimensions == r.dimensions) && (componentType == r.componentType) && name.equals(r.name);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public Class<?> lookup() {
        Class<?> result;
        int count;

        result = ClassRef.classFind(name);
        for (count = dimensions; count > 0; count--) {
            result = net.sf.beezle.sushi.util.Arrays.getArrayClass(result);
        }
        return result;
    }

    @Override
    public String toString() {
        String result;
        int i;

        result = name;
        for (i = 0; i < dimensions; i++) {
            result = result + "[]";
        }
        return result;
    }

    //--
    // descriptor as defined in JVM spec $ 4.3.2

    public String toFieldDescriptor() {
        int i;
        StringBuilder result;

        result = new StringBuilder();
        for (i = 0; i < dimensions; i++) {
            result.append('[');
        }
        result.append(componentType.descriptor);
        if (componentType.id == T_REFERENCE) {
            result.append(name.replace('.', '/'));
            result.append(';');
        }
        return result.toString();
    }

    public static ClassRef forFieldDescriptor(String descriptor) {
        return (ClassRef) forFieldDescriptor(descriptor, 0, descriptor.length())[0];
    }

    /**
     * @return ClassRef nextOfs
     */
    public static Object[] forFieldDescriptor(String descriptor, int ofs, int length) {
        int i, dimensions;
        char c;
        Type typeCode;
        String name;
        int nextOfs;

        dimensions = 0;
        while ((ofs < length) && (descriptor.charAt(ofs) == '[')) {
            dimensions++;
            ofs++;
        }
        if (ofs >= length) {
            throw new RuntimeException("illegal descriptor: " + descriptor);
        }
        c = descriptor.charAt(ofs);
        if (c == Type.REFERENCE.descriptor) {
            typeCode = Type.REFERENCE;
            i = descriptor.indexOf(';', ofs);
            if (i == -1) {
                throw new RuntimeException(descriptor + ofs);
            }
            name = descriptor.substring(ofs + 1, i).replace('/', '.');
            nextOfs = i + 1;
        } else {
            typeCode = null;
            for (i = 0; i < Type.PRIMITIVES.length; i++) {
                typeCode = Type.PRIMITIVES[i];
                if (typeCode.descriptor == c) {
                    break;
                }
            }
            if (typeCode == null) {
                throw new RuntimeException("illegal descriptor char "
                    + (int) c + ", ofs " + ofs + " in: " + descriptor);
            }
            name = typeCode.name;
            nextOfs = ofs + 1;
        }
        return new Object[] { new ClassRef(name, dimensions, typeCode), nextOfs };
    }

    //--
    // full qualified class name as defined in JVM spec $4.2

    public String toDescriptor() {
        int i;
        StringBuilder result;

        if (isArray()) {
            result = new StringBuilder();
            for (i = 0; i < dimensions; i++) {
                result.append('[');
            }
            result.append(componentType.descriptor);
            if (componentType.id == T_REFERENCE) {
                result.append(name.replace('.', '/'));
                result.append(';');
            }
            return result.toString();
        } else {
            if (componentType.id != T_REFERENCE) {
                throw new RuntimeException("not a reference type: " + this);
            }
            return name.replace('.', '/');
        }
    }

    //--
    // file name:       java\lang\String.class  or java/lang/String.class,
    //     depending on the platform
    // resource name:   java/lang/String.class
    //     / separator
    // classname:       java.lang.String
    //     . separator, no extension

    public static String fileToResName(String fileName) {
        return fileName.replace(File.pathSeparatorChar, '/');
    }

    public static String resToFileName(String resName) {
        return resName.replace('/', File.pathSeparatorChar);
    }

    public static String resToClassName(String resName) {
        return resName.substring(0, resName.length() - 6).replace('/', '.');
    }

    public static String classToResName(String className) {
        return className.replace('.', '/') + ".class";
    }

    public static String fileToClassName(String fileName) {
        return resToClassName(fileToResName(fileName));
    }

    public static String classToFileName(String className) {
        return resToFileName(classToResName(className));
    }

    //--
    // additional functionality for primitive Java Classes

    public static Class<?> wrappedType(Class<?> c) {
        int i;

        if (c.isPrimitive()) {
            for (i = 0; i < Type.PRIMITIVES.length; i++) {
                if (Type.PRIMITIVES[i].type == c) {
                    return Type.PRIMITIVES[i].wrapper;
                }
            }
            throw new RuntimeException();
        } else {
            return c;
        }
    }

    public static Class<?> unwrappedType(Class<?> c) {
        int i;

        for (i = 0; i < Type.PRIMITIVES.length; i++) {
            if (Type.PRIMITIVES[i].wrapper == c) {
                return Type.PRIMITIVES[i].type;
            }
        }
        return c;
    }

    //--
    // additional functionality for Java Classes

    /**
     * Looks up a class by name. In contrast to Class.forName, primitive
     * classes are found and not found is indicated by null.
     * @param  name  the name of the Class to be found
     * @return       the Class found, null if nothing was found
     */

    public static Class<?> classFind(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            return findType(name).type;
        }
    }


    /**
     * Gets the common base of two classes. Common base is the most special
     * class both argument are assignable to. The common base for
     * different primitive types is null; the common base for a primitive
     * type and a reference type is null.
     * @param   a  first class, may be null
     * @param   b  second class
     * @return  the common base; b, if a == null;
     */
    public static Class<?> commonBase(Class<?> a, Class<?> b) {
        if (b == null) {
            throw new IllegalArgumentException();
        } else if (a == null) {
            return b;
        } else {
            return commonSuperClass(a, b);
        }
    }

    private static Class<?> commonSuperClass(Class<?> a, Class<?> b) {
        if (a.isAssignableFrom(b)) {
            return a;
        } else if (b.isAssignableFrom(a)) {
            return b;
        } else {
            Class<?> c = b.getSuperclass();
            if (c == null) {
                return null; // primitive type, Void, or Interface
            } else {
                return commonSuperClass(a, c);
            }
        }
    }

    /**
     * Writes a class Object.
     * @param  out target to write to
     * @param  cl  class to be written
     */
    public static void write(ObjectOutput out, Class<?> cl) throws IOException {
        int dim;

        if (cl == null) {
            out.writeByte(-1);
        } else {
            dim = 0;
            while (cl.isArray()) {
                dim++;
                cl = cl.getComponentType();
            }

            if (dim > Byte.MAX_VALUE) {
                throw new RuntimeException("to many dimensions");
            }

            out.writeByte((byte) dim);
            out.writeUTF(cl.getName());
        }
    }

    /**
     * Reads a class Object.
     * @param  in  source to read from
     * @return the Class read
     */
    public static Class<?> read(ObjectInput in) throws java.io.IOException {
        byte dim;
        Class<?> cl;
        String name;

        dim = in.readByte();
        if (dim == -1) {
            return null;
        } else {
            name = in.readUTF();
            cl = classFind(name);
            if (cl == null) {
                throw new RuntimeException("can't load class " + name);
            }

            while (dim-- > 0) {
                cl = net.sf.beezle.sushi.util.Arrays.getArrayClass(cl);
            }
            return cl;
        }
    }

    /**
     * Writes an array of Class objects.
     * @param  out    target to write to
     * @param  types  Classes to be written
     */
    public static void writeClasses(ObjectOutput out, Class<?>[] types)
            throws java.io.IOException {
        int i;

        if (types.length > Byte.MAX_VALUE) {
            throw new RuntimeException("to many dimensions");
        }
        out.writeByte((byte) types.length);
        for (i = 0; i < types.length; i++) {
            write(out, types[i]);
        }
    }

    /**
     * Reads an array of Class objects.
     * @param  in  source to read from
     * @return the Classes read
     */
    public static Class<?>[] readClasses(ObjectInput in)
            throws java.io.IOException, ClassNotFoundException {
        int i, len;
        Class<?>[] result;

        len = in.readByte();
        result = new Class[len];
        for (i = 0; i < len; i++) {
            result[i] = read(in);
        }
        return result;
    }

    //-- code generation

    private Type getTypeCode() {
        if (isArray()) {
            return Type.REFERENCE;
        } else {
            return componentType;
        }
    }

    /**
     * Returns are default suitable for LDC. Primitive defaults are
     * wrapped - as needed by LDC.
     */
    public Object getDefault() {
        return getTypeCode().zero;
    }

    public Object getLdcDefault() {
        return getTypeCode().zeroLdc;
    }

    public void emitDefault(Code dest) {
        dest.emitGeneric(LDC, new Object[] { getLdcDefault() });
    }

    public void emitLoad(Code dest, int var) {
        int c;

        c = getTypeCode().load;
        if (c != -1) {
            dest.emit(c, var);
        }
    }

    public void emitStore(Code dest, int var) {
        int c;

        c = getTypeCode().store;
        if (c != -1) {
            dest.emit(c, var);
        }
    }

    // create an array with this as component type
    public void emitArrayNew(Code dest) {
        Type typeCode;

        typeCode = getTypeCode();
        if (typeCode.id == T_REFERENCE) {
            dest.emit(ANEWARRAY, this);
        } else {
            dest.emit(NEWARRAY, typeCode.id);
        }
    }

    public void emitArrayLoad(Code dest) {
        dest.emit(getTypeCode().arrayLoad);
    }

    public void emitArrayStore(Code dest) {
        dest.emit(getTypeCode().arrayStore);
    }

    public boolean isArrayDefaultElement(Object obj) {
        Type typeCode;

        typeCode = getTypeCode();
        switch (typeCode.id) {
        case T_BOOLEAN:
            return !((Boolean) obj).booleanValue();
        case T_BYTE:
        case T_SHORT:
        case T_INT:
        case T_LONG:
            return ((Number) obj).longValue() == 0;
        case T_CHAR:
            return ((Character) obj).charValue() == 0;
        case T_DOUBLE:
        case T_FLOAT:
            return ((Number) obj).doubleValue() == 0.0;
        case T_REFERENCE:
            return obj == null;
        default:
            throw new RuntimeException("not supported: " + typeCode.id);
        }
    }
}
