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

import net.sf.beezle.mork.misc.StringArrayList;
import net.sf.beezle.mork.scanner.Position;
import net.sf.beezle.mork.scanner.Scanner;
import net.sf.beezle.mork.scanner.ScannerFactory;
import java.io.IOException;
import java.io.Reader;

public class XmlScannerFactory implements ScannerFactory {
    private final StringArrayList symbolTable;

    /** indexed by elements (not start- or end-tags) */
    private final Attribute[][] attrs;

    private final int eofSymbol;

    public XmlScannerFactory(StringArrayList symbolTable, int eofSymbol, Attribute[][] attrs) {
        this.symbolTable = symbolTable;
        this.eofSymbol = eofSymbol;
        this.attrs = attrs;
    }

    public Scanner newInstance(Position pos, Reader src) throws IOException {
        XmlScanner scanner;

        scanner = new XmlScanner(symbolTable, eofSymbol, attrs);
        scanner.open(pos, src);
        return scanner;
    }
}
