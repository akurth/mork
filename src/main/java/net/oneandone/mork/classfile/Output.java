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
package net.oneandone.mork.classfile;

import net.oneandone.sushi.util.IntArrayList;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Output implements Constants, AutoCloseable {
    private final ByteArrayOutputStream bufferDest;
    private final OutputStream finalDest;
    /** finalDest or bufferDest */
    private OutputStream dest;
    private Code context;
    private int codeStart;  // only defined within code context
    /** pairs of globalOfs, value */
    private final IntArrayList fixups;

    public char minor;
    public char major;
    private final Pool constants;

    public static void save(ClassDef c, File file) throws IOException {
        try (FileOutputStream stream = new FileOutputStream(file);
             Output output = new Output(stream)) {
            c.write(output);
        }
    }

    public Output(OutputStream dest) {
        this(dest, new Pool());
    }

    public Output(OutputStream destInit, Pool constantsInit) {
        major = 50;  // java 6
        minor = 0;

        bufferDest = new ByteArrayOutputStream();
        finalDest = destInit;
        dest = bufferDest;
        constants = constantsInit;
        context = null;
        codeStart = 0;
        fixups = new IntArrayList();
    }

    public void close() throws IOException {
        byte[] array;
        int i, max, old, ofs;

        dest.close();
        // TODO: this is expensive
        array = ((ByteArrayOutputStream) dest).toByteArray();
        dest = finalDest;
        writeU4(MAGIC);
        writeU2(minor);
        writeU2(major);
        constants.write(dest);
        max = fixups.size();
        old = 0;
        for (i = 0; i < max; i += 2) {
            ofs = fixups.get(i);
            write(array, old, ofs - old);
            writeU4(fixups.get(i + 1));
            old = ofs + 4;
        }
        dest.write(array, old, array.length - old);
        dest.close();
    }

    //-- code context

    public void openCode(Code code) {
        if (context != null) {
            throw new RuntimeException("nested code attribute");
        }
        context = code;
        codeStart = getGlobalOfs();
    }
    public void closeCode() {
        if (context == null) {
            throw new RuntimeException("no code attribute to close");
        }
        context = null;
    }
    public void requireCode() {
        if (context == null) {
            throw new RuntimeException("not int code attribute");
        }
    }
    public Code getCode() {
        return context;
    }

    public int getOfs() {
        return getGlobalOfs() - codeStart;
    }

    public void writeEndIdxOrLast(int startIdx, int idx) throws IOException {
        writeU2(context.findEndOfsOrLast(startIdx, idx));
    }

    public void writeIdxOrLast(int idx) throws IOException {
        writeU2(context.findOfsOrLast(idx));
    }

    public void writeIdx(int idx) throws IOException {
        writeU2(context.findOfs(idx));
    }

    //-- write primitives

    public void write(byte[] data) throws IOException {
        write(data, 0, data.length);
    }

    public void write(byte[] data, int ofs, int len) throws IOException {
        IO.write(dest, data, ofs, len);
    }

    public void writeU1(int u1) throws IOException {
        IO.writeU1(dest, u1);
    }

    public void writeS1(int u1) throws IOException {
        IO.writeS1(dest, u1);
    }

    /** int c  simplifies checking, casting */
    public void writeU2(int u2) throws IOException {
        IO.writeU2(dest, u2);
    }

    /** int c  simplifies checking, casting */
    public void writeS2(int u2) throws IOException {
        IO.writeS2(dest, u2);
    }


    /** int c  simplifies checking, casting */
    public void writeU4(int u4) throws IOException {
        IO.writeU4(dest, u4);
    }

    //--

    public void writeConstant(Object obj) throws IOException {
        MethodRef m;

        if (obj instanceof ClassRef) {
            writeClassRef((ClassRef) obj);
        } else if (obj instanceof FieldRef) {
            writeFieldRef((FieldRef) obj);
        } else if (obj instanceof MethodRef) {
            m = (MethodRef) obj;
            if (m.ifc) {
                writeInterfaceMethodRef(m);
            } else {
                writeClassMethodRef(m);
            }
        } else if (obj instanceof String) {
            writeString((String) obj);
        } else if (obj instanceof Integer) {
            writeInt(((Integer) obj).intValue());
        } else if (obj instanceof Float) {
            writeFloat(((Float) obj).floatValue());
        } else if (obj instanceof Long) {
            writeLong(((Long) obj).longValue());
        } else if (obj instanceof Double) {
            writeDouble(((Double) obj).doubleValue());
        } else {
            throw new IllegalArgumentException("" + obj);
        }
    }

    public void writeClassRef(ClassRef ref) throws IOException {
        constants.write(dest, CONSTANT_CLASS, ref);
    }
    public void writeFieldRef(FieldRef ref) throws IOException {
        constants.write(dest, CONSTANT_FIELDREF, ref);
    }
    public void writeClassMethodRef(MethodRef ref) throws IOException {
        constants.write(dest, CONSTANT_METHODREF, ref);
    }
    public void writeInterfaceMethodRef(MethodRef ref) throws IOException {
        constants.write(dest, CONSTANT_INTERFACEMETHODREF, ref);
    }
    public void writeString(String value) throws IOException {
        constants.write(dest, CONSTANT_STRING, value);
    }
    public void writeInt(int value) throws IOException {
        constants.write(dest, CONSTANT_INTEGER, new Integer(value));
    }
    public void writeFloat(float value) throws IOException {
        constants.write(dest, CONSTANT_FLOAT, new Float(value));
    }
    public void writeLong(long value) throws IOException {
        constants.write(dest, CONSTANT_LONG, new Long(value));
    }
    public void writeDouble(double value) throws IOException {
        constants.write(dest, CONSTANT_DOUBLE, new Double(value));
    }

    public void writeShortString(String value) throws IOException {
        constants.writeShort(dest, CONSTANT_STRING, value);
    }
    public void writeShortInt(int value) throws IOException {
        constants.writeShort(dest, CONSTANT_INTEGER, new Integer(value));
    }
    public void writeShortFloat(float value) throws IOException {
        constants.writeShort(dest, CONSTANT_FLOAT, new Float(value));
    }

    // note: no writeNameAndTyp

    public void writeUtf8(String value) throws IOException {
        constants.write(dest, CONSTANT_UTF8, value);
    }

    public void writePad() throws IOException {
        int count;

        count = IO.padSize(getOfs());
        while (count-- > 0) {
            writeU1(0);
        }
    }

    public int addIfNew(int id, Object obj) {
        return constants.addIfNew(id, obj);
    }

    public int getGlobalOfs() {
        // ofs in buffer
        return bufferDest.size();
    }

    public int writeSpace(int size) throws IOException {
        int result;

        result = getGlobalOfs();
        while (size > 0) {
            writeU1(0);
            size--;
        }

        fixups.add(result);
        fixups.add(0); // dummy value
        return result;
    }

    public void writeFixup(int fixup, int value) {
        int i, max;

        max = fixups.size();
        // using fixups.indexOf could find values ...
        for (i = 0; i < max; i += 2) {
            if (fixups.get(i) == fixup) {
                fixups.set(i + 1, value);
                return;
            }
        }
        throw new IllegalArgumentException("so such fixup: " + fixup);
    }
}
