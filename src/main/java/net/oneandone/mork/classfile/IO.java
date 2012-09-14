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
package net.oneandone.mork.classfile;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Mostly byte IO. Int arguments/results to simplify casting
 */

public class IO {
    // returns number of bytes to read to reach a multiple of 4
    public static int padSize(int ofs) {
        return ((ofs + 3) & ~3) - ofs;
    }

    public static void read(InputStream src, byte[] buffer) throws IOException {
        read(src, buffer, 0, buffer.length);
    }

    public static void read(InputStream src, byte[] buffer, int ofs, int len) throws IOException {
        int count;

        while (len != 0) {
            count = src.read(buffer, ofs, len);
            if (count == -1) {
                throw new EOFException();
            }
            ofs += count;
            len -= count;
        }
    }

    public static int readU1(InputStream src) throws IOException {
        int result;

        result = src.read();
        if (result == -1) {
            throw new EOFException();
        }
        // no need for conversion, src returns values >= -1
        return result;
    }

    public static int readS1(InputStream src) throws IOException {
        int result;

        result = src.read();
        if (result == -1) {
            throw new EOFException();
        }
        return (byte) result;
    }

    public static char readU2(InputStream src) throws IOException {
        int a, b;

        a = src.read();
        b = src.read();
        if ((a == -1) || (b == -1)) {
            throw new EOFException();
        }
        return (char) ((a << 8) | b);
    }

    public static int readS2(InputStream src) throws IOException {
        int a, b;

        a = src.read();
        b = src.read();
        if ((a == -1) || (b == -1)) {
            throw new EOFException();
        }
        return (short) ((a << 8) | b);
    }

    public static int readU4(InputStream src) throws IOException {
        int a, b, c, d;

        a = src.read();
        b = src.read();
        c = src.read();
        d = src.read();
        if ((a == -1) || (b == -1) || (c == -1) || (d == -1)) {
            throw new EOFException();
        }
        return (a << 24) | (b << 16) | (c << 8) | d;
    }

    //--

    // no added functionality, just for symmetry reasons
    public static void write(OutputStream dest, byte[] data, int ofs, int len) throws IOException {
        dest.write(data, ofs, len);
    }

    public static void writeU1(OutputStream dest, int u1) throws IOException {
        if ((u1 & 0xffffff00) != 0) {
            throw new IllegalArgumentException();
        }
        dest.write(u1 & 0x000000ff);
    }

    public static void writeS1(OutputStream dest, int s1) throws IOException {
        if ((s1 < Byte.MIN_VALUE) || (s1 > Byte.MAX_VALUE)) {
            throw new IllegalArgumentException();
        }
        dest.write(s1 & 0x000000ff);
    }

    /** int c  simplifies checking, casting */
    public static void writeU2(OutputStream dest, int u2) throws IOException {
        if ((u2 < Character.MIN_VALUE) || (u2 > Character.MAX_VALUE)) {
            throw new IllegalArgumentException();
        }
        dest.write((byte) (u2 >> 8));
        dest.write((byte) u2);
    }

    /** int c  simplifies checking, casting */
    public static void writeS2(OutputStream dest, int s2) throws IOException {
        if ((s2 < Short.MIN_VALUE) || (s2 > Short.MAX_VALUE)) {
            throw new IllegalArgumentException();
        }
        dest.write((byte) (s2 >> 8));
        dest.write((byte) s2);
    }

    /** int c  simplifies checking, casting */
    public static void writeU4(OutputStream dest, int u4) throws IOException {
        dest.write((byte) (u4 >> 24));
        dest.write((byte) (u4 >> 16));
        dest.write((byte) (u4 >>  8));
        dest.write((byte) u4);
    }
}
