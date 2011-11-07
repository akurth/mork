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

import de.mlhartme.mork.bootstrap.Loader;
import de.mlhartme.mork.mapping.Mapper;
import de.mlhartme.mork.misc.GenericException;
import de.mlhartme.mork.reflect.Function;
import de.mlhartme.mork.semantics.BuiltIn;
import de.mlhartme.mork.semantics.IllegalLiteral;
import java.io.File;

/**
 * Global state, instances represent globale options. The this class forms a Java API
 * to Mork -- whereas the Main class forms a command-line interface. Use <code>Mork</code>
 */

public class Mork {
    /**
     * IO settings
     */
    public final Output output;

    /**
     * Maps mapper files into Specification objects.
     */
    private final MorkMapper mapperMapper;

    /**
     * Maps grammar files into Syntax objects.
     */
    private final MorkMapper grammarMapper;

    /**
     * Maps dtd files into Syntax objects.
     */
    private final MorkMapper dtdMapper;

    /**
     * Helper objects to generate class files.
     */
    private final MapperCompiler compiler;

    /**
     * Only defined during compile
     */
    private Job currentJob;

    public Mork(Output output, Function mapperFn) {
        this.output = output;
        this.mapperMapper = new MorkMapper(this, "de.mlhartme.mork.compiler.MapperMapper", mapperFn);
        this.grammarMapper = new MorkMapper(this, "de.mlhartme.mork.compiler.GrammarMapper");
        this.dtdMapper = new MorkMapper(this, "de.mlhartme.mork.xml.DtdMapper");
        this.compiler = new MapperCompiler(output);
        this.currentJob = null;
    }

    //-----------------------------------------------------------------------
    // the real functionality

    public boolean compile(Job job) {
        boolean result;

        currentJob = job;
        result = compileCurrent();
        currentJob = null;
        return result;
    }

    /** Helper for compile */
    private boolean compileCurrent() {
        Specification spec;
        Mapper result;

        output.normal(currentJob.source + ":");
        if (currentJob.listing != null) {
            output.openListing(currentJob.listing);
        }
        spec = (Specification) mapperMapper.invoke(currentJob.source);
        if (spec == null) {
            return false;
        }
        try {
            result = spec.translate(output);
            compiler.run(result, spec.getMapperName(), currentJob.source, currentJob.outputPath);
        } catch (GenericException e) {
            output.error(currentJob.source.getName(), e);
            return false;
        }
        return true;
    }

    //--------------------------------------------------------------------------------
    // load syntax

    public Syntax loadGrammar(String fileName) throws GenericException, IllegalLiteral {
        return loadSyntax(fileName, grammarMapper);
    }

    public Syntax loadDtd(String fileName) throws GenericException, IllegalLiteral {
        return loadSyntax(fileName, dtdMapper);
    }

    private Syntax loadSyntax(String fileName, MorkMapper mapper) throws GenericException, IllegalLiteral {
        File file;
        Syntax syntax;

        fileName = BuiltIn.parseString(fileName);   // fileName use / on all platforms
        fileName = fileName.replace('/', File.separatorChar);
        file = Loader.absoluteFile(currentJob.source.getParentFile(), fileName);
        syntax = (Syntax) mapper.invoke(file);
        if (syntax == null) {
            throw new GenericException("error(s) in syntax file - aborted");
        }
        return syntax;
    }
}
