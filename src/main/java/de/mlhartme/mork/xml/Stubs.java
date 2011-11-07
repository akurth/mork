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

package de.mlhartme.mork.xml;

import de.mlhartme.mork.misc.GenericException;
import de.mlhartme.mork.misc.StringArrayList;
import de.mlhartme.mork.regexpr.Choice;
import de.mlhartme.mork.regexpr.Loop;
import de.mlhartme.mork.regexpr.RegExpr;
import de.mlhartme.mork.regexpr.Symbol;

/** Helper methods used by DtdMapper.map. */
public class Stubs {
    public static final int NOP = 0;
    public static final int OPTION = 1;
    public static final int STAR = 2;
    public static final int PLUS = 3;

    //--
    public static Object rule(String name, RegExpr content) {
        return new Object[] { name, content };
    }

    public static StringArrayList symbolTable(String[] names) {
        StringArrayList table;
        int i;

        table = new StringArrayList();
        table.add(XmlScanner.PCTEXT_NAME);
        for (i = 0; i < names.length; i++) {
            table.add(names[i]);
        }

        return table;
    }

    //--

    public static RegExpr unary(RegExpr re, int op) {
        switch (op) {
        case NOP:
            return re;
        case OPTION:
            return Choice.createOption(re);
        case STAR:
            return Loop.createStar(re);
        case PLUS:
            return new Loop(re);
        default:
            throw new IllegalArgumentException("unkown op: " + op);
        }
    }

    public static RegExpr mixed(int i) throws GenericException {
        if (i == -1) {
            throw new GenericException("mixed content not supported");
        }
        return Choice.createOption(new Symbol(XmlScanner.PCTEXT));
    }

    public static Symbol lookup(StringArrayList symbolTable, String name) throws GenericException {
        int idx;

        idx = symbolTable.indexOf(name);
        if (idx == -1) {
            throw new GenericException("undefined element: " + name);
        }
        return new Symbol(idx);
    }

    public static String removeQuotes(String str) {
        if (str == null) {
            return null;
        }
        return str.substring(1, str.length() - 1);
    }
}
