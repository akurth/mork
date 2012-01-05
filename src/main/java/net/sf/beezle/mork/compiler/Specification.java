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

package net.sf.beezle.mork.compiler;

import net.sf.beezle.mork.mapping.Definition;
import net.sf.beezle.mork.mapping.Mapper;
import net.sf.beezle.mork.mapping.Path;
import net.sf.beezle.mork.mapping.Transport;
import net.sf.beezle.mork.misc.GenericException;
import net.sf.beezle.mork.parser.Parser;
import net.sf.beezle.mork.semantics.Ag;
import net.sf.beezle.mork.semantics.Attribute;
import net.sf.beezle.mork.semantics.Oag;
import net.sf.beezle.sushi.util.IntBitSet;

import java.util.ArrayList;
import java.util.List;

/**
 * Specifies a mapper, thus, a Mapping object is a Mapper before translation.
 * Represents a map file.
 */
public class Specification {
    private final Syntax syntax;

    private final Transport transport;

    private final Definition[] definitions;

    private final IntBitSet mainBorder; // set of symbols

    private final String mapperName;

    private final List<Definition> mainDefs;

    public Specification(String mapperName, Syntax syntax, Definition[] definitions) {
        int i;
        Definition d;
        Attribute a;

        this.mapperName = mapperName;
        this.definitions = definitions;
        this.syntax = syntax;
        this.transport = new Transport();
        this.mainBorder = new IntBitSet();
        this.mainDefs = new ArrayList<Definition>();
        for (i = 0; i < definitions.length; i++) {
            d = definitions[i];
            a = d.getAttribute();
            mainBorder.add(a.symbol);
            if (d.isMain()) {
                mainDefs.add(d);
            }
        }
    }

    public Syntax getSyntax() {
        return syntax;
    }

    public Transport getTransport() {
        return transport;
    }

    public void translateDefaultPushPath(Definition seed) throws GenericException {
        Path.translate(syntax, seed, Path.UPS, mainBorder, mainDefs, Path.MERGEABLE);
    }

    public Definition lookup(String name) {
        return lookup(-1, name);
    }

    public Definition lookup(int symbol, String name) {
        Definition d;
        Attribute a;
        int i;

        for (i = 0; i < definitions.length; i++) {
            d = definitions[i];
            a = d.getAttribute();
            if (name.equals(a.name)) {
                if ((a.symbol == symbol) || (symbol == -1)) {
                    return d;
                }
            }
        }
        return null;
    }

    public String getMapperName() {
        return mapperName;
    }

    /**
     * @return != null
     */
    public Mapper translate(int k, Output output) throws GenericException {
        Ag semanticsBuffer;
        Oag oag;
        Parser parser;
        int i;
        Definition def;

        parser = syntax.translate(k, output);
        output.verbose("processing mapping section");
        semanticsBuffer = new Ag(syntax.getGrammar());
        for (i = 0; i < definitions.length; i++) {
            def = definitions[i];
            if (output.verboseTranslation != null) {
                output.verboseTranslation.println("translating " + def.getName());
            }
            definitions[i].translate(semanticsBuffer, transport, syntax.getGrammar());
        }
        output.verbose("computing oag");
        oag = semanticsBuffer.createSemantics(
                getDefinitionAttrs(syntax.getGrammar().getStart()));
        output.verbose("oag done");
        output.statistics();
        output.statistics("Semantics statistics");
        output.statistics("  semantics: TODO"); /* mapping.getSize()); */
        if (output.listing != null) {
            output.listing("\n\nAttribute Grammar\n");
            output.listing(semanticsBuffer.toString());
            output.listing.println("Visit sequences");
            oag.printVisits(output.listing);
        }
        return new Mapper(mapperName, parser, oag);
    }

    /**
     * @return list of attributes defined for the specfied symbol (in order of definition)
     */
    private List<Attribute> getDefinitionAttrs(int symbol) {
        List<Attribute> lst;
        Attribute a;
        int i;

        lst = new ArrayList<Attribute>();
        for (i = 0; i < definitions.length; i++) {
            a = definitions[i].getAttribute();
            if (a.symbol == symbol) {
                lst.add(a);
            }
        }
        return lst;
    }
}
