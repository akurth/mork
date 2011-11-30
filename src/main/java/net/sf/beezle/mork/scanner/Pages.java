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

public class Pages {
    private final int pageSize;

    private Reader src;

    /** Index of the last page */
    private int lastNo;

    /** Number of bytes on the last page */
    private int lastFilled;

    /** Invariant: pages.length > 0 && i: 0..lastNo: (pages[i] != null && pages.get(i).length == PAGE_SIZE) */
    private char[][] pages;

    private char[] newPage;

    public Pages(int pageSize) {
        if (pageSize == 0) {
            throw new IllegalArgumentException();
        }
        this.pageSize = pageSize;
        this.pages = new char[2][];
        this.pages[0] = new char[pageSize];
        this.newPage = null;
    }

    public void open(Reader src) {
        this.src = src;
        this.lastFilled = 0;
        this.lastNo = 0;
    }

    public char[] get(int no) {
        return pages[no];
    }

    /** @return number of bytes used on the specified page */
    public int getFilled(int no) {
        if (no == lastNo) {
            return lastFilled;
        } else {
            return pageSize;
        }
    }

    /** number of pages used. */
    public int getLastNo() {
        return lastNo;
    }

    public int getSize() {
        return pageSize * lastNo + lastFilled;
    }

    /**
     * @return -1  eof   0: current page grown   1: new page
     */
    public int read(int pageNo, int pageUsed) throws IOException {
        if (pageUsed < pageSize) {
            // assert pageNo == lastNo
            if (!fill()) {
                return -1;
            }
            return 0;
        } else {
            if (pageNo == lastNo) {
                if (!append()) {
                    return -1;
                }
            }
            if (getFilled(pageNo + 1) == 0) {
                if (!fill()) {
                    return -1;
                }
            }
            return 1;
        }
    }

    private boolean fill() throws IOException {
        int count;

        if (lastFilled == pageSize) {
            throw new IllegalStateException();
        }
        count = src.read(pages[lastNo], lastFilled, pageSize - lastFilled);
        if (count <= 0) {
            if (count == 0) {
                throw new RuntimeException();
            }
            return false;
        }
        lastFilled += count;
        return true;
    }

    private boolean append() throws IOException {
        char[] p;
        int count;
        char[][] newPages;

        if (lastFilled != pageSize) {
            throw new IllegalStateException();
        }
        lastNo++;
        if (lastNo >= pages.length) {
            newPages = new char[lastNo * 5 / 2][];
            System.arraycopy(pages, 0, newPages, 0, lastNo);
            pages = newPages;
        }
        if (newPage == null) {
            p = new char[pageSize];
        } else {
            p = newPage;
            newPage = null;
        }
        pages[lastNo] = p;
        count = src.read(p, 0, pageSize);
        if (count <= 0) {
            if (count == 0) {
                throw new RuntimeException();
            }
            lastFilled = 0;
            return false;
        }
        lastFilled = count;
        return true;
    }

    public void remove(int count) {
        newPage = pages[0];
        lastNo -= count;
        if (lastNo < 0) {
            throw new IllegalStateException();
        }
        System.arraycopy(pages, count, pages, 0, lastNo + 1);
    }

    @Override
    public String toString() {
        StringBuilder builder;
        int i, p;
        char[] pg;

        builder = new StringBuilder();
        builder.append("pages {");
        for (p = 0; p <= lastNo; p++) {
            pg = get(p);
            builder.append("\n  page " + p + ":");
            for (i = 0; i < pageSize; i++) {
                builder.append(pg[i]);
            }
            builder.append("\n    ");
            for (i = 0; i < pageSize; i++) {
                builder.append(" " + ((int) pg[i]));
            }
        }
        builder.append("\n}");

        return builder.toString();
    }
}
