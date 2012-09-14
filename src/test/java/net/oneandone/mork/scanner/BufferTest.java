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
package net.oneandone.mork.scanner;

import net.oneandone.mork.misc.GenericException;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * TODO: more tests.
 */
public class BufferTest {
    private Reader src;
    private Buffer buffer;
    private Position pos;

    @Test
    public void empty() throws IOException {
        create("");
        checkEof();
    }

    @Test
    public void hello() throws IOException {
        String str = "hello";

        create(str);
        read(str);
        checkEof();
    }

    @Test
    public void shortStart() throws IOException {
        String a = "aa";
        String b = "bbb";
        String c = "c";

        create(a + b + c);
        read(a);
        read(b);
        read(c);
    }

    @Test
    public void longStart() throws IOException {
        String a = "a1234567890a";
        String b = "b12345678901234567890bb";
        String c = "c123456789012345678901234567890";

        create(a + b + c);
        read(a);
        read(b);
        read(c);
    }

    @Test
    public void position() throws IOException, GenericException {
        createPosition(9);
        readPosition(1);
        readPosition(2);
        readPosition(3);
        readPosition(3);
    }

    //--

    public void createPosition(int newlineCount) {
        StringBuilder sb;
        int i;

        sb = new StringBuilder();
        for (i = 0; i < newlineCount; i++) {
            sb.append('\n');
        }
        src = new StringReader(sb.toString());
        pos = new Position();
        pos.set(null, 1, 1, 0);
        buffer = new Buffer(2);
        buffer.open(pos, src);
    }

    private void readPosition(int newlineCount) throws IOException {
        int i;
        int startLine;
        int startOfs;

        startLine = pos.getLine();
        startOfs = pos.getOffset();
        for (i = 0; i < newlineCount; i++) {
            buffer.read();
        }
        buffer.eat();
        assertEquals(startLine + newlineCount, pos.getLine());
        assertEquals(startOfs + newlineCount, pos.getOffset());
    }

    private void checkEof() throws IOException {
        String str;

        str = buffer.createString();
        buffer.assertInvariant();
        assertEquals(Scanner.EOF, buffer.read());
        buffer.assertInvariant();
        assertTrue(buffer.wasEof());
        buffer.assertInvariant();
        assertEquals(str, buffer.createString());
        buffer.assertInvariant();
        assertEquals(Scanner.EOF, buffer.read());
        buffer.assertInvariant();
        assertEquals(str, buffer.createString());
        buffer.assertInvariant();
        assertTrue(buffer.wasEof());
        buffer.assertInvariant();
    }

    private void read(String str) throws IOException {
        int i;
        int initialOfs;
        int finalOfs;
        int ofs;

        buffer.eat();
        initialOfs = buffer.getEndOfs();
        finalOfs = initialOfs + str.length();
        for (ofs = initialOfs; ofs < finalOfs; ofs++) {
            buffer.resetEndOfs(ofs);
            buffer.assertInvariant();
            for (i = ofs; i < finalOfs; i++) {
                assertEquals(i, buffer.getEndOfs());
                assertEquals(str.charAt(i - initialOfs), buffer.read());
                buffer.assertInvariant();
            }
            assertEquals(str, buffer.createString());
        }
        buffer.assertInvariant();
    }

    private void create(String str) {
        src = new StringReader(str);
        buffer = new Buffer(7);
        buffer.open(new Position(), src);
    }
}
