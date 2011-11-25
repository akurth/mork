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

package net.sf.beezle.mork.reflect;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import net.sf.beezle.mork.classfile.Bytecodes;
import net.sf.beezle.mork.classfile.ClassRef;
import net.sf.beezle.mork.classfile.Code;
import net.sf.beezle.mork.classfile.MethodRef;

/**
 * Wraps a Java Method to make it useable as a Function. Both static
 * and non-static methods can be wrapped. Java Method means
 * java.lang.reflect.Method.
 */

public class Method extends Function implements Bytecodes {
    /** Java Method wrapped by this Function. */
    private java.lang.reflect.Method meth;

    //------------------------------------------------------------

    public static Selection forName(String name) {
        int idx;
        Class cl;

        idx = name.lastIndexOf('.'); // empty
        if (idx == -1) {
            return new Selection();
        }
        cl = ClassRef.classFind(name.substring(0, idx));
        if (cl == null) {
            return new Selection(); // empty
        }
        return forName(cl, name.substring(idx + 1));
    }

    /**
     * Gets all valid Methods from the specified class with the
     * specified name.
     * @param  cl      Class to look at
     * @param  name    the Function name to look for
     * @result  retrieves all Methods found
     */
    public static Selection forName(Class cl, String name) {
        java.lang.reflect.Method[] all;
        int i;
        List lst;
        Function fn;

        all = cl.getDeclaredMethods();
        lst = new ArrayList();
        for (i = 0; i < all.length; i++) {
            if (name.equals(all[i].getName())) {
                fn = create(all[i]);
                if (fn != null) {
                    lst.add(fn);
                }
            }
        }
        return new Selection(lst);
    }

    /**
     * Creates a Method.
     * @param methInit Java Method to wrap
     */
    public Method(java.lang.reflect.Method methInit) {
        int modif;

        modif = methInit.getModifiers();
        if (Modifier.isAbstract(modif) || !Modifier.isPublic(modif)) {
            throw new IllegalArgumentException();
        }

        meth = methInit;
    }

    public static Method create(java.lang.reflect.Method meth) {
        try {
            return new Method(meth);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    //----------------------------------------------------------------------

    /**
     * Returns true if a static Java Method is wrapped.
     * @return  true, if a static Java Method is wrapped
     */
    public boolean isStatic() {
        return Modifier.isStatic(meth.getModifiers());
    }

    // TODO: this method is a hack
    public java.lang.reflect.Method getRaw() {
        return meth;
    }

    /**
     * Gets the name of this Function.
     * @return  the Function name
     */
    @Override
    public String getName() {
        return meth.getName();
    }

    /**
     * Gets the result type of this Function. Result for non-static
     * Methods is the Class defining the method; invokations return
     * the Objected invoked, possible results of the method are
     * discarded.
     * @return  the result type
     */
    @Override
    public Class getReturnType() {
        return meth.getReturnType();
    }

    @Override
    public Class[] getParameterTypes() {
        Class[] tmp, result;

        if (isStatic()) {
            return meth.getParameterTypes();
        } else {
            tmp = meth.getParameterTypes();
            result = new Class[1 + tmp.length];
            result[0] = meth.getDeclaringClass();
            System.arraycopy(tmp, 0,  result, 1,  tmp.length);
            return result;
        }
    }

    @Override
    public Class[] getExceptionTypes() {
        return meth.getExceptionTypes();
    }

    //-----------------------------------------------------------------------

    /**
     * Invokes the wrapped Java Method. Throws Exceptions raised in the
     * Java Method.
     * @param   vals  arguments to the Java Method
     * @return  Object returned be the Java Method or the Object the method
     *          is invoked on.
     */
    @Override
    public Object invoke(Object[] vals) throws InvocationTargetException {
        Object[] tmp;
        int i;

        try {
            if (isStatic()) {
                return meth.invoke(null, vals);
            } else {
                if (vals.length == 0) {
                    throw new IllegalArgumentException("invalid arguments");
                }

                tmp = new Object[vals.length - 1];
                for (i = 1; i < vals.length; i++) {
                    tmp[i-1] = vals[i];
                }

                return meth.invoke(vals[0], tmp);
            }
        } catch (InvocationTargetException e) {
            throw e;
        } catch (IllegalAccessException e) {
            // isValid should prevent this
            throw new RuntimeException("can't access method");
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }


    //--------------------------------------------------------------------
    // Manual serialization. Automatic serialization is not possible because
    // Java Methods are not serializable.

    /**
     * Writes this Method.
     * @param  out  target to write to
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        write(out, meth);
    }

    /**
     * Reads this Method.
     * @param   in  source to read from
     */
    private void readObject(ObjectInputStream in)
            throws ClassNotFoundException, NoSuchMethodException,
                   IOException {
        meth = read(in);
    }

    //------------------------------------------------------------------------
    // additional functionality for Java Constructors

    /**
     * Writes a Java Method object.
     * @param  out  target to write to
     * @param  meth object to be written
     */
    public static void write(ObjectOutput out, java.lang.reflect.Method meth)
            throws IOException {
        Class cl;

        if (meth == null) {
            ClassRef.write(out, null);
        } else {
            cl = meth.getDeclaringClass();
            ClassRef.write(out, cl);
            out.writeUTF(meth.getName());
            ClassRef.writeClasses(out, meth.getParameterTypes());
        }
    }

    /**
     * Reads a Java Method object.
     * @param  in  source to read from
     * @return  Java Method read
     */
    public static java.lang.reflect.Method read(ObjectInput in)
            throws ClassNotFoundException, NoSuchMethodException, IOException {
        Class cl;
        Class[] types;
        String name;

        cl = ClassRef.read(in);
        if (cl == null) {
            return null;
        } else {
            name = (String) in.readUTF();
            types = ClassRef.readClasses(in);
            return cl.getMethod(name, types);
        }
    }

    @Override
    public void translate(Code dest) {
        Class[] tmp;
        int first;

        tmp = getParameterTypes();
        first = firstCastArguments(tmp);
        if (first != -1) {
            if (tmp.length == 1) {
                castSingleArgument(tmp, dest);
            } else {
                castArguments(first, tmp, dest);
            }
        } else {
            // no casting necessary
        }
        if (isStatic()) {
            dest.emit(INVOKESTATIC, new MethodRef(meth));
        } else {
            dest.emit(INVOKEVIRTUAL, new MethodRef(meth));
        }
    }

    /** @return true if the specified type is not primitive and not Object */
    public static boolean isCastType(Class type) {
        return !type.isPrimitive() && !Object.class.equals(type);
    }

    /* @return true if the specified array contains at least one cast type. */
    private static int firstCastArguments(Class[] tmp) {
        int i;

        for (i = 0; i < tmp.length; i++) {
            if (isCastType(tmp[i])) {
                return i;
            }
        }
        return -1;
    }

    private static void castSingleArgument(Class[] types, Code dest) {
        ClassRef ref;

        ref = new ClassRef(types[0]);
        dest.emit(CHECKCAST, ref);
    }

    /**
     * @param first   index of the first argument to be casted; 0: cast all arguments
     *                not that the first argument is the last on the operand stack
     */
    private static void castArguments(int first, Class[] tmp, Code dest) {
        int max;
        int ofs;
        int var;
        int[] vars;
        ClassRef type;
        ClassRef[] types;
        int i;

        // store operand stack in local variables and create a new, casted operand stack
        // TODO: isn't there a more elegant way?
        max = tmp.length;
        vars = new int[max];
        types = new ClassRef[max];
        for (i = max - 1; i >= first; i--) {
            type = new ClassRef(tmp[i]);
            types[i] = type;
            var = dest.allocate(type);
            vars[i] = var;
            type.emitStore(dest, var);
        }
        for (i = first; i < max; i++) {
            types[i].emitLoad(dest, vars[i]);
            if (isCastType(tmp[i])) {
                dest.emit(CHECKCAST, types[i]);
            }
        }
    }
}
