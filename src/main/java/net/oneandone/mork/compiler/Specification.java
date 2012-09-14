/**
 * Copyright 1&1 Internet AG, https://github.com/1and1/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.oneandone.mork.compiler;

import net.oneandone.mork.mapping.Definition;
import net.oneandone.mork.mapping.Mapper;
import net.oneandone.mork.mapping.Path;
import net.oneandone.mork.mapping.Transport;
import net.oneandone.mork.misc.GenericException;
import net.oneandone.mork.parser.Parser;
import net.oneandone.mork.semantics.Ag;
import net.oneandone.mork.semantics.Attribute;
import net.oneandone.mork.semantics.Oag;
import net.oneandone.sushi.util.IntBitSet;

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
    public Mapper translate(int k, int threadCount, Output output) throws GenericException {
        Ag semanticsBuffer;
        Oag oag;
        Parser parser;
        int i;

        parser = syntax.translate(k, threadCount, output);
        output.verbose("processing mapping section");
        semanticsBuffer = new Ag(syntax.getGrammar());
        for (i = 0; i < definitions.length; i++) {
            definitions[i].translate(semanticsBuffer, transport, syntax.getGrammar());
        }
        output.verbose("computing oag");
        oag = semanticsBuffer.createSemantics(getDefinitionAttrs(syntax.getGrammar().getStart()));
        output.verbose("oag done");
        if (output.statistics != null) {
            output.statistics.println();
            output.statistics.println("Semantics statistics");
            output.statistics.println("  semantics: TODO"); /* mapping.getSize()); */
        }
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
