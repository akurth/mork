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

/**
 * Symbols recognized by the Xml scanner. The constants here are taken from
 * DtdMapper.lst.
 * Caution: these constants change if you re-compile the xml grammar.
 * TODO: generate from xml.grm
 */

public interface Symbols {
    int DOCTYPE = 0;  // keyword

    int OPEN_ARRAY_BRACKET = 1;  // [
    int CLOSE_ARRAY_BRACKET = 2; // ]

    int TAG_END = 3;
    int TAG_START = 4;
    int END_TAG_START = 5;
    int EMPTY_TAG_END = 6;

    int ENTITY = 35;
    int PE = 36;

    int DOCTYPE_DECL = 61;  // nonterminal

    int GE_DECL = 96;
    int PE_DECL = 97;

    int SPACE = 107;
    int NAME = 109;
    int ENTITY_VALUE = 111;
    int ATT_VALUE = 112;
    int SYSTEM_LITERAL = 113;
    int CHAR_DATA = 116;
    int COMMENT = 117;
    int PI = 118;
    int CD_SECT = 120;
    int EQ = 126;
    int REFERENCE = 131;

    // TODO: CHAR_REF and ENTITY_REF would be more effient (and convenient) than REFERENCE, but they
    // are are inlined:
    // int CHAR_REF = 110;
    // int ENTITY_REF = 112;
    int PE_REFERENCE = 133;

    // TODO: duplicates from TextDecl (with magic numbers)
    int PARSED_ENT_TEXT_DECL = 134;
    int PE_TEXT_DECL = 135;
}
