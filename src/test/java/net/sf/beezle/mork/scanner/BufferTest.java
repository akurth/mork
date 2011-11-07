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

package net.sf.beezle.mork.scanner;

import net.sf.beezle.mork.misc.GenericException;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import junit.framework.TestCase;

/**
 * Test GrammarScanner.
 * TODO: more tests.
 * TODO: factor out the stuff common with XmlScannerTest.
 */

public class BufferTest extends TestCase {
    private Reader src;
    private Buffer buffer;
    private Position pos;

    public void testEmpty() throws IOException {
        create("");
        checkEof();
    }
    public void testHello() throws IOException {
        String str = "hello";

        create(str);
        read(str);
        checkEof();
    }

    public void testShortMark() throws IOException {
        String a = "aa";
        String b = "bbb";
        String c = "c";

        create(a + b + c);
        read(a);
        read(b);
        read(c);
    }

    public void testLongMark() throws IOException {
        String a = "a1234567890a";
        String b = "b12345678901234567890bb";
        String c = "c123456789012345678901234567890";

        create(a + b + c);
        read(a);
        read(b);
        read(c);
    }

    public void testPosition() throws IOException, GenericException {
        createPosition(9);
        readPosition(1);
        readPosition(2);
        readPosition(3);
        readPosition(3);
    }

    public void createPosition(int newlineCount) throws IOException, GenericException {
        StringBuffer sb;
        int i;

        sb = new StringBuffer();
        for (i = 0; i < newlineCount; i++) {
            sb.append('\n');
        }
        src = new StringReader(sb.toString());
        pos = new Position();
        pos.set(null, 1, 1, 0);
        buffer = new Buffer(2);
        buffer.open(pos, src);
    }

    private void readPosition(int newlineCount) throws IOException, GenericException {
        int c;
        int i;
        int startLine;
        int startOfs;

        startLine = pos.getLine();
        startOfs = pos.getOffset();
        for (i = 0; i < newlineCount; i++) {
            c = buffer.read();
        }
        buffer.mark();
        assertEquals(startLine + newlineCount, pos.getLine());
        assertEquals(startOfs + newlineCount, pos.getOffset());
    }

    private void checkEof() throws IOException {
        String str;

        str = buffer.createString();
        buffer.assertInvariant();
        assertEquals(-1, buffer.readOrEof());
        buffer.assertInvariant();
        assertTrue(buffer.wasEof());
        buffer.assertInvariant();
        assertEquals(str, buffer.createString());
        buffer.assertInvariant();
        assertEquals(-1, buffer.readOrEof());
        buffer.assertInvariant();
        assertEquals(str, buffer.createString());
        buffer.assertInvariant();
        assertTrue(buffer.wasEof());
        buffer.assertInvariant();
    }

    private void read(String str) throws IOException {
        int i;
        int max;
        int startOfs;
        int endOfs;
        int ofs;

        buffer.mark();
        startOfs = buffer.getOfs();
        endOfs = str.length();
        for (ofs = 0; ofs < endOfs; ofs++) {
            buffer.reset(ofs);
            buffer.assertInvariant();
            max = str.length();
            for (i = ofs; i < endOfs; i++) {
                assertEquals(startOfs + i, buffer.getOfs());
                assertEquals(str.charAt(i), buffer.readOrEof());
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
