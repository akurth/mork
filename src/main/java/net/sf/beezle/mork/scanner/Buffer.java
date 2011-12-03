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

import java.io.IOException;
import java.io.Reader;

/**
 * Connection between Scanner and Reader. This class is kind of a StringBuilder optimized for
 * deleting at the beginning and appending at the end. In addition, appending is done buffered
 * from a Reader. The buffer behaves as if the reader is filled in completely at the beginning,
 * but this is done in steps.
 *
 * Buffer has a start and an end, that are used to select a possible token.
 * Moving the start forward removes characters from the beginning;
 * moving the end forward reads characters from the underlying stream (if they have not been
 * read before -- the end is not necessarily that last character read).
 *
 * Buffer storage is devided into pages.
 */

public class Buffer {
    /**
     * True if src.read() has returned -1. Does not necessarily meant that this buffer
     * is EOF as well.
     */
    private boolean eof;

    /**
     * Offset in the first page where the current selection starts.
     */
    private int start;

    /** start position */
    private Position position;

    private final Pages pages;

    /** reduncant, but more efficient */
    private final int pageSize;

    /** Index of the end page. endPage == pages.get(endPageIdx), pageIdx < pages.size() */
    private int endPageIdx;

    /** Current page. pages.get(pageNo) */
    private char[] endPage;

    /** Offset in the end page. */
    private int end;

    /** Last valid index in end page that has beend filled from the underlying stream. endPage[end] is valid if end < endFilled */
    private int endFilled;

    public Buffer() {
        this(8192);
    }

    public Buffer(int pageSize) {
        if (pageSize == 0) {
            throw new IllegalArgumentException();
        }
        this.pageSize = pageSize;
        this.pages = new Pages(pageSize);
    }

    public void open(Position position, Reader src) {
        this.position = position;
        this.eof = false;
        this.start = 0;
        this.pages.open(src);
        this.endPageIdx = 0;
        this.endPage = pages.get(0);
        this.end = 0;
        this.endFilled = pages.getFilled(0);
    }

    //----------------------------------------------------------------------


    public void assertInvariant() {
        if (start > pages.getSize()) {
            throw new IllegalStateException();
        }
        if (start > pageSize) {
            throw new IllegalStateException();
        }
        if (start > getEndOfs()) {
            throw new IllegalStateException();
        }
        if (end > endFilled) {
            throw new IllegalStateException();
        }
        if (endPage != pages.get(endPageIdx)) {
            throw new IllegalStateException();
        }
    }

    public int getEndOfs() {
        return endPageIdx * pageSize + end;
    }

    /**
     * Sets the current end ofs by to the specified value
     * @param ofs < getEndOfs()
     */
    public void resetEndOfs(int ofs) {
        if (endPageIdx == 0) {
            // because a precondition is that ofs is left of the
            // end
            end = ofs;
        } else {
            endPageIdx = ofs / pageSize;
            end = ofs % pageSize;
            if (end == 0 && pages.getLastNo() == endPageIdx) {
                // this happens if getOfs() was called after the last character of a page was read
                end += pageSize;
                endPageIdx--;
            }
            endPage = pages.get(endPageIdx);
            endFilled = pages.getFilled(endPageIdx);
        }
    }

    /**
     * Returns true if the end of file has been seen and the buffer is at it's end.
     * Does *not* try to read in order to check for an end-of-file.
     */
    public boolean wasEof() {
        return eof && (getEndOfs() == pages.getSize());
    }

    /**
     * Advances the end and returns the character at this positio.
     * @return character or Scanner.EOF
     */
    public int read() throws IOException {
        if (end == endFilled) {
            switch (pages.read(endPageIdx, endFilled)) {
                case -1:
                    eof = true;
                    return Scanner.EOF;
                case 0:
                    endFilled = pages.getFilled(endPageIdx);
                    break;
                case 1:
                    endPageIdx++;
                    end = 0;
                    endPage = pages.get(endPageIdx);
                    endFilled = pages.getFilled(endPageIdx);
                    break;
                default:
                    throw new RuntimeException();
            }
        }
        return endPage[end++];
    }

    //-------------------------------

    /**
     * Move start forward to the current position.
     */
    public void eat() {
        int i;

        if (endPageIdx == 0) {
            position.update(endPage, start, end);
            start = end;
        } else {
            position.update(pages.get(0), start, pageSize);
            for (i = 1; i < endPageIdx; i++) {
                position.update(pages.get(i), 0, pageSize);
            }
            pages.shrink(endPageIdx);
            endPageIdx = 0;
            endPage = pages.get(0);
            start = end;
            position.update(endPage, 0, start);
        }
    }

    /**
     * Returns the string between start and the current position.
     */
    public String createString() {
        int i;
        int count;

        if (endPageIdx == 0) {
            // speedup the most frequent situation
            return new String(endPage, start, end - start);
        } else {
            char[] buffer;

            buffer = new char[endPageIdx * pageSize + end - start];
            count = pageSize - start;
            System.arraycopy(pages.get(0), start, buffer, 0, count);
            for (i = 1; i < endPageIdx; i++) {
                System.arraycopy(pages.get(i), 0, buffer, count, pageSize);
                count += pageSize;
            }
            System.arraycopy(pages.get(endPageIdx), 0, buffer, count, end);
            return new String(buffer);
        }
    }

    public void getPosition(Position result) {
        result.set(position);
    }

    //-----------------------------------------------------------------------

    @Override
    public String toString() {
        StringBuilder buf;

        buf = new StringBuilder();
        buf.append("buffer {");
        buf.append("\n  srcEof     = ").append(eof);
        buf.append("\n  start      = ").append(start);
        buf.append("\n  endPageIdx = ").append(endPageIdx);
        buf.append("\n  end        = ").append(end);
        buf.append("\n  endUsed    = ").append(endFilled);
        buf.append(pages.toString());
        buf.append("\n}");
        return buf.toString();
    }
}
