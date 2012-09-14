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

import net.oneandone.mork.mapping.Mapper;
import net.oneandone.mork.misc.GenericException;
import net.oneandone.mork.reflect.Function;
import net.oneandone.mork.semantics.BuiltIn;
import net.oneandone.mork.semantics.IllegalLiteral;

import java.io.File;
import java.io.IOException;

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
    private final MorkMapper syntaxMapper;

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
        this.mapperMapper = new MorkMapper(this, "net.oneandone.mork.compiler.MapperMapper", mapperFn);
        this.syntaxMapper = new MorkMapper(this, "net.oneandone.mork.compiler.SyntaxMapper");
        this.compiler = new MapperCompiler(output);
        this.currentJob = null;
    }

    //-- the real functionality

    public boolean compile(Job job) throws IOException {
        boolean result;

        currentJob = job;
        result = compileCurrent();
        currentJob = null;
        return result;
    }

    /** Helper for compile */
    private boolean compileCurrent() throws IOException {
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
            result = spec.translate(currentJob.k, currentJob.threadCount, output);
            compiler.run(result, spec.getMapperName(), currentJob.source, currentJob.outputPath);
        } catch (GenericException e) {
            output.error(currentJob.source.getName(), e);
            return false;
        } catch (IOException e) {
            output.error(currentJob.source.getName(), e.getMessage());
            return false;
        }
        return true;
    }

    //-- load syntax

    public Syntax loadSyntax(String fileName) throws GenericException, IllegalLiteral, IOException {
        return loadSyntax(fileName, syntaxMapper);
    }

    private Syntax loadSyntax(String fileName, MorkMapper mapper) throws GenericException, IllegalLiteral, IOException {
        File file;
        Syntax syntax;

        fileName = BuiltIn.parseString(fileName);   // fileName use / on all platforms
        fileName = fileName.replace('/', File.separatorChar);
        file = absoluteFile(currentJob.source.getParentFile(), fileName);
        syntax = (Syntax) mapper.invoke(file);
        if (syntax == null) {
            throw new GenericException("error(s) in syntax file - aborted");
        }
        return syntax;
    }

    public static File absoluteFile(File dir, String fileName) {
        File file;

        file = new File(fileName);
        if (file.isAbsolute()) {
            return file;
        }
        return new File(dir, fileName);
    }
}
