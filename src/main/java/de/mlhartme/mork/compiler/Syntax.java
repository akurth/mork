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

package de.mlhartme.mork.compiler;

import de.mlhartme.mork.grammar.Grammar;
import de.mlhartme.mork.misc.GenericException;
import de.mlhartme.mork.parser.Parser;

/** Scanner and parser specification. **/
public abstract class Syntax {
    public abstract Grammar getGrammar();
    public abstract Parser translate(Output output) throws GenericException;

    public static final String LALR_CONFLICT =
        "lalr(1) conflicts (use the -lst option to obtain a listing of the automaton):\n";

}
