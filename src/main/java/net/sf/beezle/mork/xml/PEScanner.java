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

package net.sf.beezle.mork.xml;

import java.io.IOException;
import java.io.Reader;

import net.sf.beezle.mork.scanner.GrammarScanner;
import net.sf.beezle.mork.scanner.GrammarScannerFactory;
import net.sf.beezle.mork.scanner.Position;
import net.sf.beezle.mork.scanner.Scanner;

/**
 * Wraps a GrammarScanner to recognize PEReferences. Used internally by the DocumentBuilder.
 */
public class PEScanner implements Symbols, Scanner {
    /** scanner mode that scanns PEReference. TODO: obtain automatically. */
    private static final int PE_MODE = 1;

    private DocumentBuilder context;

    private GrammarScannerFactory table;
    private int eofSymbol;

    private GrammarScanner scanner;

    /** where PERefenrences can be matched */
    private boolean everywhere;

    //-------------------------------------------------------------------

    public PEScanner(
        DocumentBuilder context, GrammarScannerFactory table, int eofSymbol, boolean everywhere)
    {
        this.context = context;
        this.table = table;
        this.eofSymbol = eofSymbol;
        this.everywhere = everywhere;
    }

    public Scanner newInstance() {
        return new PEScanner(context, table, eofSymbol, everywhere);
    }

    public void open(Position position, Reader src) throws IOException {
        throw new RuntimeException("TODO");
        /*
        this.scanner = null; // new GrammarScanner(table, eofSymbol);
         this.scanner.open(position, src);*/
    }

    public void close() {
        scanner = null;
    }

    //----------------------------------------------------------------------
    // query state

    public void getPosition(Position result) {
        scanner.getPosition(result);
    }

    public String getText() {
        return scanner.getText();
    }

    public int getEofSymbol() {
        return eofSymbol;
    }

    //----------------------------------------------------------------------

    /** never returns PE_REFERENCE. */
    public int next(int mode) throws IOException {
        int terminal;
        String name;
        Object replacement;
        Position pos;

        while (true) {
            terminal = doEat(mode);
            // System.out.println("eat " + terminal + " text " + scanner.getText());
            if (terminal != PE_REFERENCE) {
                return terminal;
            }
            // TODO: check for recursion
            name = scanner.getText();
            name = name.substring(1, name.length() - 1);
            replacement = context.getParameterEntities().lookup(name);
            if (replacement == null) {
                pos = new Position();
                scanner.getPosition(pos);
                throw new IllegalToken(pos, "undefined parameter entity: " + name);
            }
            if (replacement instanceof Buffer) {
                prepend(((Buffer) replacement).getAllText());
            } else {
                prepend((String) replacement);
            }
        }
    }

    private int doEat(int mode) throws IOException {
        if (everywhere) {
            // TODO: find conflicting terminals and correct them if necessary
            return scanner.next(mode);
        } else {
            // TODO: doEat might return PE_REFENRECE resulting from error correction
            return scanner.next(mode);
        }
    }

    private void prepend(String replacement) throws IOException {
        // input has to be prepended -- just pushing scanners doesn't help since
        // a stack scanner cannot scan into the next scanner - which causes some
        // input split into multiple token where a single token would be correct.
        // In particular, whitespace does not get merged.
        GrammarScanner s;
        Position pos;
        String remaining;

        pos = new Position();
        scanner.getPosition(pos);  // TODO: wrong position
        remaining = scanner.getRemainingInput();
        throw new RuntimeException("TODO");
        /*
        scanner.close();
        scanner = new GrammarScanner(table, eofSymbol);
         scanner.open(pos, new StringReader(replacement + remaining));*/
    }
}
