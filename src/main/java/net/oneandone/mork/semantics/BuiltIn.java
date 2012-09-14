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
package net.oneandone.mork.semantics;



/**
 * Predefined functions usefull for attribution. Note. the Java API
 * defines many more functions, e.g. Integer.parseInt, Boolean.TRUE,
 * Boolean.FALSE.
 */

public class BuiltIn {
    // unlike Boolean.TRUE, there is no constant for null pointers
    public static final Object NULL = null;

    //--
    // character and string parsing

    // Helper for escapeCharacter and unicodeCharacter - set
    // by parseChar and parseString
    private static String text = null;
    private static int ofs;
    private static int len;

    public static char parseChar(String textInit) throws IllegalLiteral {
        char result;

        text = textInit;
        ofs = 1;
        len = text.length();

        try {
            if ((len > 2) && (text.charAt(0) == '\'') && (text.charAt(len - 1) == '\'')) {
                result = charOrEscape();
                if (ofs + 1 == len) {
                    return result;
                }
            }
            throw new IllegalLiteral("illegal character literal: " + textInit);
        } finally {
            text = null;
        }
    }

    public static String parseString(String textInit) throws IllegalLiteral {
        StringBuilder result;

        try {
            text = textInit;
            len = text.length();
            ofs = 1;
            result = new StringBuilder();

            if ((len >= 2) && (text.charAt(0) == '"') && (text.charAt(len - 1) == '"')) {
                while (ofs + 1 < len) {
                    result.append(charOrEscape());
                }
            } else {
                throw new IllegalLiteral("illegal string literal: " + textInit);
            }
            return result.toString();
        } finally {
            text = null;
        }
    }

    private static char charOrEscape() throws IllegalLiteral {
        int result;

        if (text.charAt(ofs) == '\\') {
            ofs++;
            result = charEscape();
            if (result != -1) {
                return (char) result;
            }
            result = unicodeEscape();
            if (result != -1) {
                return (char) result;
            }
            // TODO: ocatal escape

            throw new IllegalLiteral("illegal literal");
        } else {
            return text.charAt(ofs++);
        }
    }

    private static int unicodeEscape() {
        int start;
        int i, v;
        int result;

        start = ofs;
        while (ofs < len) {
            if (text.charAt(ofs) != 'u') {
                break;
            }
            ofs++;
        }
        if ((ofs == start) || (ofs + 4 > len)) {
            return -1;
        }

        result = 0;
        for (i = 0; i < 4; i++) {
            v = Character.digit(text.charAt(ofs + i), 16);
            if (v == -1) {
                return -1;
            }
            result = result * 16 + v;
        }
        ofs += 4;
        return result;
    }

    private static int charEscape() {
        if (ofs < len) {
            switch (text.charAt(ofs++)) {
            case 'b':       // backspace
                return '\b';
            case 't':        // horizontal tab
                return '\t';
            case 'n':        // linefeet LF
                return '\n';
            case 'f':        // form feet FF
                return '\f';
            case 'r':        // cariage return CR
                return '\r';
            case '"':        // double quotes
                return '\"';
            case '\'':       // single quotes
                return '\'';
            case '\\':       // blackslash \
                return '\\';
            default:
                ofs--;
                return -1;
            }
        }
        return -1;
    }
}
