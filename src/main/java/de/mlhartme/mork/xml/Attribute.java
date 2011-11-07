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
import de.mlhartme.mork.scanner.Position;

public class Attribute {
    // coding for defaultMode
    public static final int REQUIRED = 0;
    public static final int IMPLIED = 1;
    public static final int FIXED = 2;
    public static final int NONE = 3;

    private int element;
    private String attributeName; // name without element name prefix
    private int terminal;

    private int defaultMode;

    /** only defined for defaultMode FIXED and NONE. */
    private String defaultValue;

    public Attribute(
        int element, String attributeName, int terminal, int defaultMode, String defaultValue)
    {
        this.element = element;
        this.attributeName = attributeName;
        this.terminal = terminal;
        this.defaultMode = defaultMode;
        this.defaultValue = defaultValue;
    }

    /** @return true if the parser sees this attribute optionally. */
    public boolean isOptional() {
        return (defaultMode == IMPLIED);
    }

    public String setDefault(Position pos, String supplied) throws IllegalToken {
        switch (defaultMode) {
            case REQUIRED:
                if (supplied == null) {
                    throw new IllegalToken(pos, "missing attribute");
                }
                return supplied;
            case IMPLIED:
                return supplied;
            case FIXED:
                if (!defaultValue.equals(supplied)) {
                    throw new IllegalToken(pos, "invalue fixed value: " +
                                           supplied + " != " + defaultValue);
                }
                return supplied;
            case NONE:
                if (supplied != null) {
                    return supplied;
                } else {
                    return defaultValue;
                }
            default:
                throw new RuntimeException();
        }
    }

    public Attribute(String elementName, String attributeName,
                     String defaultValue, StringArrayList symbolTable, Object type, int defaultMode)
        throws GenericException {
        String tmp;

        this.attributeName = attributeName;
        this.defaultMode = defaultMode;
        this.defaultValue = defaultValue;

        element = symbolTable.indexOf(elementName);
        if (element == -1) {
            throw new GenericException("undefined element: " + elementName);
        }
        tmp = XmlSyntax.toAttribute(elementName, attributeName);
        if (symbolTable.indexOf(tmp) != -1) {
            throw new GenericException("duplicate definition: " +
                                           attributeName);
        }
        terminal = symbolTable.size();
        symbolTable.add(tmp);
    }

    public String getName() {
        return attributeName;
    }

    public int getElement() {
        return element;
    }

    public int getTerminal() {
        return terminal;
    }
}
