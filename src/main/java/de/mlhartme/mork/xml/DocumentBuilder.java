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

import de.mlhartme.mork.mapping.Mapper;
import de.mlhartme.mork.parser.Parser;
import de.mlhartme.mork.parser.ParserTable;
import de.mlhartme.mork.scanner.GrammarScanner;
import de.mlhartme.mork.scanner.Position;
import de.mlhartme.mork.scanner.GrammarScannerFactory;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * State shared by (recursive) EntityBuilder invokations.
 * Holds entities, parserTable and scannerTable (obtained from the DTDMapper).
 */
public class DocumentBuilder {
    public static final URL DEFAULT_BASE_URL;

    static {
        try {
            DEFAULT_BASE_URL = new URL("file://localhost/");
        } catch (MalformedURLException e) {
            throw new IllegalStateException("" + e);
        }
    }

    private ParserTable parserTable;
    private GrammarScannerFactory scannerTable;
    private int eofSymbol;
    private PrintStream log;

    private Entities general;
    private Entities parameter;

    /**
     * @param parserTable of the a grammar parser of xml.grm
     * @param scannerTable of the grammar scanner of xml.grm
     */
    public DocumentBuilder(
        ParserTable parserTable, GrammarScannerFactory scannerTable, int eofSymbol, PrintStream log)
    {
        this.parserTable = parserTable;
        this.scannerTable = scannerTable;
        this.eofSymbol = eofSymbol;
        this.log = log;
        this.parameter = new Entities();
        this.general = new Entities();
        this.general.addDefaults();
    }

    public static DocumentBuilder create(PrintStream log) throws IOException {
        Mapper mapper;

        mapper = new Mapper("de.mlhartme.mork.xml.DtdMapper");
        return create(log, mapper.getParser());
    }

    /**
     * @param  parser for xml.grm, referencing the appropriate grammar scanner
     */
    public static DocumentBuilder create(PrintStream log, Parser parser) throws IOException {
        GrammarScanner scanner;

        throw new RuntimeException("TODO"); 
        /*
        scanner = (GrammarScanner) parser.getScanner();
        return
         new DocumentBuilder(parser.getTable(), scanner.getTable(), scanner.getEofSymbol(), log);*/
    }

    public Entities getGeneralEntities() {
        return general;
    }

    public Entities getParameterEntities() {
        return parameter;
    }

    public PrintStream getLogging() {
        return log;
    }

    //-----------------------------------------------------

    public Parser createParser() {
        PEScanner scanner;

        throw new RuntimeException("TODO");
        /*
        scanner = new PEScanner(this, scannerTable, eofSymbol, false);
         return new Parser(parserTable, scanner);*/
    }

    public PrintStream getLog() {
        return log;
    }

    public void defineEntity(
        String name, String entityValue, Position entityValuePosition,
        URL systemLiteral, URL baseUrl, boolean generalEntity)
            throws IOException {
        Object replacement;

        if (entityValue != null) {
            replacement = internalReplacement(entityValue, entityValuePosition, baseUrl,
                                              generalEntity);
        } else {
            replacement = externalReplacement(systemLiteral, generalEntity);
        }

        if (generalEntity) {
            general.define(name, replacement);
        } else {
            parameter.define(name, replacement);
        }
    }

    private Object internalReplacement(
        String entityValue, Position entityValuePosition, URL baseUrl, boolean generalEntity)
            throws IOException {
        String literalStr;
        Object replacement;
        Position pos;

        literalStr = entityValue.substring(1, entityValue.length() - 1);
        if (generalEntity) {
            pos = new Position();
            pos.set(entityValuePosition);
            replacement = EntityBuilder.runGeneral(this, baseUrl, pos,
                                                   new StringReader(literalStr), false);
        } else {
            // PE structure is not known until actually used -- store the string.
            replacement = literalStr;
        }
        return replacement;
    }

    private Buffer externalReplacement(URL systemLiteral, boolean generalEntity) throws IOException {
        Buffer replacement;

        if (log != null) {
            log.println("open: " + systemLiteral);
        }
        if (generalEntity) {
            replacement = EntityBuilder.runGeneral(this, systemLiteral, new Position(systemLiteral),
                                   new InputStreamReader(systemLiteral.openStream()), true);
        } else {
            replacement = EntityBuilder.runPE(this, systemLiteral);
        }
        if (log != null) {
            log.println("done: " + systemLiteral);
        }

        return replacement;
    }
}
