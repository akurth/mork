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

import net.sf.beezle.mork.scanner.Position;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Logically maps an entity name to its replacement text. Technically maps a String to a Buffer
 * object. One instance is used for general entities, and another one for parameter entities.
 * Thus, the replacement text for both internal an external entities is represented by a Buffer
 * object. This this class, the term entity refers for general entities or paramter entities,
 * not the document entity. The document entity has no name and is not maintained here.
 */
public class Entities implements Symbols {
    private final Map entities;

    public Entities() {
        entities = new HashMap();
    }

    public void addDefaults() {
        define("lt", "<");
        define("amp", "&");
        define("gt", ">");
        define("apos", "'");
        define("quot", "\"");
    }

    public Object lookup(String name) {
        return entities.get(name);
    }

    /** helper for defineDefault */
    private void define(String name, String value) {
        Buffer entity;

        entity = new Buffer();
        entity.add(CHAR_DATA, value, new Position("predefined"));
        entities.put(name, entity);
    }

    public void define(String name, Object value) {
        if (!((value instanceof String) || (value instanceof Buffer))) {
            throw new IllegalArgumentException("" + value);
        }
        if (entities.get(name) != null) {
            // multiple declarations - §4.2
            System.err.println("warning: entity '" + name +
                                   "' already declared; ignoring latter declaration");
        } else {
            // there is no explicit check for recursive entitis -- but a declarating
            // is added *after* processing of the entity value, thus, recursion is just
            // a special case of an forward reference.
            entities.put(name, value);
        }
    }

    /** inserts recursively. @param ref includes &lt;..; */
    public void insert(String ref, Position pos, Buffer dest) throws IllegalToken {
        if (ref.charAt(1) == '#') {
            dest.add(CHAR_DATA, getCharRef(pos, ref), pos);
        } else {
            insertGeneralEntity(ref, pos, dest, new ArrayList());
        }
    }

    /** inserts recursively. @param ref includes &..; */
    private void insertGeneralEntity(String ref, Position pos, Buffer dest, List stack) throws IllegalToken {
        Buffer entity;
        int terminal;

        ref = ref.substring(1, ref.length() - 1);
        entity = (Buffer) entities.get(ref);
        if (entity == null) {
            throw new IllegalToken(pos, "no such entity: " + ref);
        }
        if (stack.contains(entity)) {
            throw new IllegalToken(pos, "recursive general entity: " + ref);
        }
        stack.add(entity);
        entity.reset();
        while (!entity.isEof()) {
            terminal = entity.getTerminal();
            if (terminal == REFERENCE) {
                insertGeneralEntity(entity.getText(), entity.getPosition(), dest, stack);
            } else {
                dest.add(terminal, entity.getText(), pos);
            }
            entity.next();
        }
        stack.remove(stack.size() - 1);
    }

    private static String getCharRef(Position pos, String str) throws IllegalToken {
        int i;
        int len;
        int num;
        int digit;

        if (str.charAt(2) != 'x') {
            len = str.length() - 1;
            num = 0;
            for (i = 2; i < len; i++) {
                num = num * 10 + str.charAt(i) - '0';
                if (num > Character.MAX_VALUE) {
                    throw new IllegalToken(pos, "character code exceeds 2 byte");
                }
            }
            return "" + (char) num;
        } else {
            len = str.length() - 1;
            num = 0;
            for (i = 3; i < len; i++) {
                digit = str.charAt(i);
                if (digit >=  'a') {
                    digit -= 'a' - 10;
                } else if (digit  >='A') {
                    digit -= 'A' - 10;
                } else {
                    digit -= '0';
                }
                num = num * 16 + digit;
                if (num > Character.MAX_VALUE) {
                    throw new IllegalToken(pos, "character code exceeds 2 byte");
                }
            }
            return "" + (char) num;
        }
    }
}
