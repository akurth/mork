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
import java.io.IOException;
import java.io.Reader;

/**
 * <p>Scanner for XmlParsers, corresponds to scanner.Scanner for grammar syntaxes.
 * An XmlScanner feeds XmlToken for a user-defined DTD to the appropriate Parser
 * for this DTD.</p>
 *
 * <p>Implementation: XmlScanner obtains grammar tokens from a Buffer. The XmlScanner
 * implements the translation of grammar token into Xml token.</p>
 */

public class XmlScanner implements Symbols, Scanner {
    private Buffer buffer;
    private int eofSymbol;

    /** indexed by elements (not start- or end-tags) */
    private Attribute[][] attrs;

    /** if != null: attribute values to be returned to the parser
        Note that insertAttrs.length == 0 is possible if an element
        has no attributes. */
    private String[] insertAttrs;

    // element to add attributes to
    private int insertAttrsElement;

    // undefined if insertAttrs == null
    private int insertAttrsOfs;

    // if != -1, insert the specified end tag
    private int insertEnd;

    // Head
    private int terminal;
    private String text;
    private Position position;

    private StringArrayList symbolTable;

    // symbols passed to the parser:
    public static final String PCTEXT_NAME = "PCTEXT";
    public static final int PCTEXT = 0;

    //-------------------------------------------------------------------

    public XmlScanner(StringArrayList symbolTable, int eofSymbol, Attribute[][] attrs) {
        this.symbolTable = symbolTable;
        this.eofSymbol = eofSymbol;
        this.attrs = attrs;

        this.insertEnd = -1;
        this.insertAttrs = null;
    }

    public void open(Position position, Reader src) throws IOException {
        this.buffer = EntityBuilder.run(DocumentBuilder.create(null), null, position, src, true);
    }

    //----------------------------------------------------------------------
    // query state

    public void getPosition(Position result) {
        result.set(position);
    }

    public String getText() {
        return text;
    }

    public int getEofSymbol() {
        return eofSymbol;
    }

    //----------------------------------------------------------------------

    private static final String EMPTY = "";

    public int next(int mode) throws IOException, IllegalToken {
        int element;
        int tmp;
        String current;
        String initialSpace;
        boolean endsWithXX;
        boolean prevEndsWithXX;

        if (mode > 0) {
            throw new IllegalArgumentException();
        }

        // handle the simple cases
        if (terminal == eofSymbol) {
            return terminal;
        }
        if (insertAttribute()) {
            return terminal;
        }
        if (insertEnd != -1) {
            terminal = insertEnd;
            text = EMPTY;
            insertEnd = -1;
            // position remains unchanged
            return terminal;
        }

        initialSpace = EMPTY;
        while (true) {
            tmp = buffer.getTerminal();
            if ((tmp == PI) || (tmp == COMMENT)) {
                buffer.next();
            } else if (tmp == SPACE) {
                initialSpace = initialSpace + buffer.getText();
                buffer.next();
            } else if (tmp == DOCTYPE) {
                buffer.next();
                while (buffer.getTerminal() != TAG_START) {
                    buffer.next();
                }
            } else {
                break;
            }
        }

        if (tmp == -1) {
            terminal = eofSymbol;
            position = null; // TODO
            text = EMPTY; // TODO
        } else if (tmp == TAG_START) {
            buffer.next();
            if (buffer.getTerminal() != NAME) {
                throw new IllegalToken(buffer.getPosition(),
                                       "name expected, found " + buffer.getText());
            }
            element = lookup(buffer.getPosition(), buffer.getText());
            terminal = XmlSyntax.toStartTag(symbolTable, element);
            position = buffer.getPosition(); // TODO
            buffer.next();
            eatAttributes(element, mode);
            text = EMPTY;
            switch (buffer.getTerminal()) {
            case TAG_END:
                buffer.next();
                break;
            case EMPTY_TAG_END:
                buffer.next();
                insertEnd = XmlSyntax.toEndTag(symbolTable, element);
                break;
            default:
                throw new IllegalToken(position, "> expected, found " + buffer.getText());
            }
        } else if (tmp == END_TAG_START) {
            buffer.next();
            if (buffer.getTerminal() != NAME) {
                throw new IllegalToken(buffer.getPosition(), "name expected");
            }
            element = lookup(buffer.getPosition(), buffer.getText());
            terminal = XmlSyntax.toEndTag(symbolTable, element);
            position = buffer.getPosition();
            buffer.next();
            text = EMPTY;
            if (buffer.getTerminal() == SPACE) {
                buffer.next();
            }
            if (buffer.getTerminal() != TAG_END) {
                throw new IllegalToken(position, "/> expected");
            }
            buffer.next();
        } else {
            // a scanner token that contributes to a parser text token
            terminal = PCTEXT;
            text = initialSpace;
            position = buffer.getPosition();
            endsWithXX = false;
           eatMore:
            while (true) {
                prevEndsWithXX = endsWithXX;
                endsWithXX = false;
                switch (tmp) {
                case CHAR_DATA:
                    current = buffer.getText();
                    if (prevEndsWithXX && current.startsWith(">")) {
                        // TODO: character references have been resolved in current
                        throw new IllegalToken(buffer.getPosition(),
                                               "unexpected ]]> in character data");
                    }
                    if (current.endsWith("]]")) {
                        endsWithXX = true;
                    }
                    break;
                case CD_SECT:
                    current = buffer.getText();
                    current = current.substring(9, current.length() - 3);
                    break;
                case REFERENCE:
                    throw new RuntimeException(); // Buffer resolves references
                case SPACE:
                case TAG_END:
                case EMPTY_TAG_END:
                case NAME:
                case EQ:
                case ATT_VALUE:
                    current = buffer.getText();
                    break;
                case PI:
                case COMMENT:
                    current = EMPTY; // IGNORE
                    break;
                default:
                    // definite assignment
                    current = null;
                    break eatMore;
                }

                text = text + current;
                buffer.next();
                tmp = buffer.getTerminal();
            }
            // TODO: fix end position
            // TODO: normalize line breaks;
        }
        return terminal;
    }

    // eats (S Attribute)* S?
    private void eatAttributes(int element, int mode) throws IOException {
        Attribute[] attributes;
        String name;
        String value;

        if (element >= attrs.length) {
            if (buffer.getTerminal() == SPACE) {
                buffer.next();
            }
            return;
        }
        attributes = attrs[element];
        insertAttrs = new String[attributes.length];
        insertAttrsOfs = 0;
        insertAttrsElement = element;

        while (buffer.getTerminal() == SPACE) {
            buffer.next();
            if (buffer.getTerminal() != NAME) {
                break;
            }
            name = buffer.getText();
            buffer.next();
            if (buffer.getTerminal() != EQ) {
                throw new RuntimeException("= expected");
            }
            buffer.next();
            if (buffer.getTerminal() != ATT_VALUE) {
                throw new RuntimeException("attribute value expected");
            }
            value = buffer.getText();
            insertData(buffer.getPosition(), attributes, name, value);
            buffer.next();
        }
        insertDefaults(buffer.getPosition(), attributes);
    }

    private void insertData(Position position, Attribute[] attributes, String name, String value) throws IllegalToken {
        int i;

        for (i = 0; i < attributes.length; i++) {
            if (attributes[i].getName().equals(name)) {
                if (insertAttrs[i] != null) {
                    throw new IllegalToken(position,
                                           "duplicate attribute: " + name);
                }
                // remove quotes
                insertAttrs[i] = value.substring(1, value.length() - 1);
                return;
            }
        }
        throw new IllegalToken(position, "undefined attribute: " + name);
    }

    private void insertDefaults(Position pos, Attribute[] attributes) throws IllegalToken {
        int i;

        for (i = 0; i < attributes.length; i++) {
            insertAttrs[i] = attributes[i].setDefault(pos, insertAttrs[i]);
        }
    }

    private boolean insertAttribute() {
        Attribute[] all;
        Attribute a;

        if (insertAttrs == null) {
            return false;
        }
        all = attrs[insertAttrsElement];
        do {
            if (insertAttrsOfs == all.length) {
                // all attributes have been inserted, switch it off
                insertAttrs = null;
                return false;
            }
            a = all[insertAttrsOfs];
            text = insertAttrs[insertAttrsOfs++];
        } while (text == null);
        terminal = a.getTerminal();
        // do not change the position
        return true;
    }

    // maps a tag to the appropriate parser symbol
    // @param  str   a tag without the intial interpunctation
    private int lookup(Position pos, String str) throws IllegalToken {
        int element;

        element = symbolTable.indexOf(str);
        if (element == -1) {
            throw new IllegalToken(pos, "unknown element: " + str);
        }
        return element;
    }
}
