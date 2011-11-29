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

import net.sf.beezle.mork.classfile.Access;
import net.sf.beezle.mork.classfile.Bytecodes;
import net.sf.beezle.mork.classfile.ClassDef;
import net.sf.beezle.mork.classfile.ClassRef;
import net.sf.beezle.mork.classfile.Code;
import net.sf.beezle.mork.classfile.Constants;
import net.sf.beezle.mork.classfile.MethodRef;
import net.sf.beezle.mork.reflect.Arrays;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Turn object into bytecode to create the object. Serializes an object into a class.
 */
public class ObjectCompiler implements Bytecodes, Constants {
    private Code dest;
    private CustomCompiler[] customs;
    private int buffer;  // buffer variable in code
    private ClassDef destClass;

    /** code objects currently pushed. Initialls empty. */
    private List<Object> stack;

    /** number of helper methods created. */
    private int helperMethods;

    /**
     * Code generation uses no jumps and no switches. Thus, every instruction has at most
     * 4 bytes, and thus, a limit of 16000 instructions seems a save bet for not exceeding
     * Java's 64k limit. (Testing has shown that 30000 would work as well.)
     */
    public static final int MAX_INSTRUCTIONS = 16000;

    /** Minimal number of statements allowed for an object. */
    public static final int MIN_INSTRUCTIONS = 5;

    public ObjectCompiler(Code dest, int buffer, CustomCompiler[] customs, ClassDef destClass) {
        this.dest = dest;
        this.buffer = buffer;
        this.customs = customs;
        this.destClass = destClass;
        this.stack = new ArrayList<Object>();
        this.helperMethods = 0;
    }

    public void run(Object obj) {
        if (obj == null) {
            run(Object.class, null, MAX_INSTRUCTIONS);
        } else {
            run(obj.getClass(), obj, MAX_INSTRUCTIONS);
        }
    }

    /**
     * type is the static type. If type is primitive, the primitive
     * object wrapped by val is compiled.
     *
     * @param limit max number of instactions the value may be compiled to. >= 1.
     */
    private void run(Class<?> type, Object val, int limit) {
        int initial;

        initial = dest.getSize();

        if (val == null) {
            if (type.isPrimitive()) {
                throw new IllegalArgumentException("primitive null");
            }
            // null is not a string, but otherwise it is  ambiguous
            dest.emit(LDC, (String) null);
        } else if (val instanceof String) {
            dest.emit(LDC, (String) val);
        } else if (type.isPrimitive()) {
            primitive(ClassRef.findComponent(type).id, val);
        } else if (type.isArray()) {
            array(type, val, limit);
        } else {
            object(type, val, limit);
        }

        if (dest.getSize() - initial > limit) {
            throw new IllegalStateException(
                    "limit:" + limit + " used:" + (dest.getSize() - initial) + " val:" + val);
        }
    }

    //--------------------------------------------------------------
    // primitive type

    /** generates exactly 1 instruction. */
    private void primitive(int typeCode, Object obj) {
        switch (typeCode) {
        case T_BOOLEAN:
            dest.emit(LDC, ((Boolean) obj).booleanValue()? 1 : 0);
            break;
        case T_CHAR:
            dest.emit(LDC, ((Character) obj).charValue());
            break;
        case T_BYTE:
        case T_SHORT:
            dest.emit(LDC, ((Number) obj).intValue());
            break;
        case T_INT:
        case T_FLOAT:
        case T_LONG:
        case T_DOUBLE:
            dest.emitGeneric(LDC, new Object[] { obj });
            break;
        default:
            throw new IllegalArgumentException("not supported: " + typeCode);
        }
    }

    //-----------------------------------------------------------------
    // array type


    /** reduce one dimension only, e.g. int[][] -> int[] **/
    private void array(Class<?> type, Object ar, int limit) {
        Class<?> compType;

        compType = type.getComponentType();
        if (compType.equals(Character.TYPE)) {
            charArray((char[]) ar, limit);
        } else {
            nonCharArray(compType, ar, limit);
        }
    }

    private void nonCharArray(Class<?> compType, Object ar, int limit) {
        int len;
        int i;
        Object comp;
        ClassRef compTypeRef;
        int nonNulls;
        int instrPerElement = 4;
        int oldSize;
        int used;
        int localLimit;
        int maxLen;

        int initialSize;
        int initialLimit;

        initialLimit = limit;
        initialSize = dest.getSize();

        compTypeRef = new ClassRef(compType);
        len = Array.getLength(ar);

        nonNulls = 0;
        for (i = 0; i < len; i++) {
            comp = Array.get(ar, i);
            if (!compTypeRef.isArrayDefaultElement(comp)) {
                nonNulls++;
            }
        }

        maxLen = 2 + nonNulls * instrPerElement;
        if (limit < maxLen) {
            pushMethod(Arrays.getArrayClass(compType));
            if (1 + nonNulls * instrPerElement > MAX_INSTRUCTIONS) {
                throw new IllegalStateException("array size ...");
            }
            nonCharArray(compType, ar, MAX_INSTRUCTIONS);
            popMethod();
            return;
        }

        dest.emit(LDC, len);
        compTypeRef.emitArrayNew(dest);
        limit -= 2;
        for (i = 0; i < len; i++) {
            comp = Array.get(ar, i);
            if (!compTypeRef.isArrayDefaultElement(comp)) {
                nonNulls--;  // number of remaining nunNulls
                dest.emit(DUP);  // array reference
                dest.emit(LDC, i);  // index
                limit -= 2;

                oldSize = dest.getSize();
                localLimit = limit - 1 - nonNulls * instrPerElement;
                run(compType, comp, localLimit);
                used = dest.getSize() - oldSize;
                if (used > localLimit) {
                    throw new IllegalStateException("used:" + used + " localLimit:" + localLimit
                                                        + " comp:" + comp);
                }
                limit -= used;
                compTypeRef.emitArrayStore(dest);
                limit--;
            }
        }
        if (nonNulls != 0) {
            throw new IllegalStateException();
        }
        if (limit < 0) {
            throw new IllegalStateException();
        }

        if (dest.getSize() - initialSize > initialLimit) {
            throw new IllegalStateException();
        }
    }

    // Max number of chars in a string. The encoded string
    // may not exceed 64k and in the worst case, one char is encoded
    // to 3 bytes. So 16k is a save bet.
    private static final int CHUNK = 16384;

    private static final MethodRef GET_CHARS = MethodRef.meth(
        ClassRef.STRING, ClassRef.VOID, "getChars",
        ClassRef.INT, ClassRef.INT, new ClassRef("char", 1), ClassRef.INT);

    // expects array reference on operand stack; returns with this
    // reference on the operand stack
    private void charArray(char[] vals, int limit) {
        int len;
        char c;
        int left, right;
        String str;
        int instrsPerChunk = 6;
        int maxLen;
        int used;

        left = 0;
        len = vals.length;
        // len/CHUNK + 1 is conservative because len % CHUNK may be 0
        maxLen = 3 + (len / CHUNK + 1) * instrsPerChunk + 1;
        if (limit < maxLen) {
            pushMethod(char.class);
            if (limit < maxLen) {
                throw new IllegalStateException("array size ...");
            }
            charArray(vals, MAX_INSTRUCTIONS);
            popMethod();
            return;
        }

        dest.emit(LDC, len);
        ClassRef.CHAR.emitArrayNew(dest);
        dest.emit(ASTORE, buffer);  // store array reference
        while (left < len) {
            c = vals[left];
            if (c != 0) {
                right = Math.min(len, left + CHUNK);
                used = right - left;
                while (used > 0 && vals[left + used - 1] == 0) {
                    used--;
                }
                if (used > 0) {
                    str = String.copyValueOf(vals, left, used);
                    dest.emit(LDC, str);
                    dest.emit(LDC, 0);
                    dest.emit(LDC, used);
                    dest.emit(ALOAD, buffer);
                    dest.emit(LDC, left);
                    dest.emit(INVOKEVIRTUAL, GET_CHARS);
                    left = right;
                } else {
                    // empty chunk, don't save it
                }
            } else {
                left++;
            }
        }
        dest.emit(ALOAD, buffer);
    }

    //-----------------------------------------------------------------
    // non-null, non-String object

    private void object(Class<?> type, Object obj, int limit) {
        Class<?>[] types;
        Object[] objects;
        int i;
        CustomCompiler decl;
        int oldSize;
        int used;  // number of instructions actually generated
        int localLimit;

        int initialSize;
        int initialLimit;

        initialSize = dest.getSize();
        initialLimit = limit;

        decl = findDecl(obj.getClass());
        types = decl.getFieldTypes();
        objects = decl.getFieldObjects(obj);
        if (limit < MIN_INSTRUCTIONS + types.length * 1 + MIN_INSTRUCTIONS) {
            // I need the static type, not the dynamic type of the object
            pushMethod(type);
            object(type, obj, MAX_INSTRUCTIONS);
            popMethod();
            return;
        }

        oldSize = dest.getSize();
        decl.beginTranslation(obj, dest);
        used = dest.getSize() - oldSize;
        if (used > MIN_INSTRUCTIONS) {
            throw new IllegalStateException();
        }
        limit -= used;
        for (i = 0; i < types.length; i++) {
            localLimit = limit - (types.length - i - 1) - MIN_INSTRUCTIONS;
            oldSize = dest.getSize();
            run(types[i], objects[i], localLimit);
            used = dest.getSize() - oldSize;
            if (used > localLimit) {
                throw new IllegalStateException("used:" + used + " localLimit:" + localLimit
                                        + " ele:" + objects[i]);
            }
            limit -= used;
        }
        if (limit < MIN_INSTRUCTIONS) {
            throw new IllegalStateException();
        }
        oldSize = dest.getSize();
        decl.endTranslation(obj, dest);
        if (dest.getSize() - oldSize > MIN_INSTRUCTIONS) {
            throw new IllegalStateException();
        }

        if (dest.getSize() - initialSize > initialLimit) {
            throw new IllegalStateException();
        }
    }

    private CustomCompiler findDecl(Class<?> type) {
        int i;

        for (i = 0; i < customs.length; i++) {
            if (customs[i].matches(type)) {
                return customs[i];
            }
        }
        throw new RuntimeException("decl not found: " + type);
    }

    /** @param obj != null */
    private void pushMethod(Class<?> returnTypeClass) {
        ClassRef returnType;
        Code nextDest;
        MethodRef ref;
        String name;

        returnType = new ClassRef(returnTypeClass);
        name = "helper" + helperMethods;
        helperMethods++;
        nextDest = new Code();
        nextDest.locals = buffer + 1;  // TODO
        destClass.addMethod(new HashSet<Access>(java.util.Arrays.asList(Access.PRIVATE, Access.STATIC)), returnType, name,
                                  ClassRef.NONE, nextDest);
        ref = new MethodRef(destClass.thisClass, false, returnType, name, ClassRef.NONE);
        dest.emit(INVOKESTATIC, ref);
        stack.add(dest);
        dest = nextDest;
    }

    private void popMethod() {
        dest.emit(ARETURN);
        dest = (Code) stack.remove(stack.size() - 1);
    }
}

