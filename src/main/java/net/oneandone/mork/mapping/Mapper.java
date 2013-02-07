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
package net.oneandone.mork.mapping;

import net.oneandone.mork.parser.Parser;
import net.oneandone.mork.scanner.Position;
import net.oneandone.mork.semantics.Node;
import net.oneandone.mork.semantics.Oag;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Maps streams into Objects by scanning. Implements the analyzing parts a compiler or any other text processing
 * program: scanner, parser and attribution. Mappers don't store symbol tables because they would only be necessary
 * for visualization.
 *
 * Technically, a <code>Mapper</code> is a translated <code>Mapping</code>.
 */
public class Mapper implements Serializable {
    private final String name;
    private Parser parser;  // null: not loaded
    private Oag oag;  // undefined if not loaded
    private PrintWriter logParsing;
    private PrintWriter logAttribution;
    private Object environment;  // default environment is null

    /** never null */
    private ErrorHandler errorHandler;

    //--

    /** Creates a mapper with the specified name. **/
    public Mapper(String name) {
        this(name, null, null);
    }

    public Mapper(String name, ErrorHandler errorHandler) {
        this(name, null, null, errorHandler);
    }

    /**
     * Create a mapper with the specified parser and semantics. This constructor is used
     * my Mork when generating a mapper, applications will usually use <code>Mapper(String)</code>.
     */
    public Mapper(String name, Parser parser, Oag oag) {
        this(name, parser, oag, new PrintWriterErrorHandler(new PrintWriter(System.err, true)));
    }

    public Mapper(String name, Parser parser, Oag oag, ErrorHandler errorHandler) {
        if (errorHandler == null) {
            throw new IllegalArgumentException();
        }
        this.name = name;
        this.parser = parser;
        this.oag = oag;
        this.errorHandler = errorHandler;
        this.logParsing = null;
        this.logAttribution = null;
    }

    /**
     * Creates a new mapper instance.
     * Shares common data (esp. scanner and parser table with this instance.
     */
    public Mapper newInstance() {
        Mapper mapper;

        mapper = new Mapper(name, parser.newInstance(), oag.newInstance());
        mapper.setLogging(logParsing, logAttribution);
        return mapper;
    }

    /**
     * <p>Loads the mapper tables if not already done so. This method is usually invoked implicity
     * by the various <code>run</code> methods. Don't call <code>load</code> explicitly unless
     * you want to force loading of mapper tables.</p>
     *
     * <p>I tried to load the mapper from a background Thread started in the constructor. But
     * the overall performance (of the jp example) was worse, probably because class loading
     * is to fast (and out-performes the threading overhead).
     *
     * @throws IllegalStateException to indicate a class loading problem
     */
    public void load() {
        ClassLoader loader;
        Class c;
        Method m;
        Object[] tables;

        if (isLoaded()) {
            return;
        }
        loader = Mapper.class.getClassLoader();
        try {
            c = loader.loadClass(name);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        try {
            m = c.getMethod("load", new Class[]{});
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        try {
            tables = (Object[]) m.invoke(null, new Object[] {});
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(e.getTargetException().getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        parser = (Parser) tables[0];
        oag = (Oag) tables[1];
    }

    /**
     * Returns true if the mapper tables have already been loaded.
     */
    public boolean isLoaded() {
        return parser != null;
    }

    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    /**
     * Defines the handler to report errors to.
     *
     * @param errorHandler  may be null
     */
    public void setErrorHandler(ErrorHandler errorHandler) {
        if (errorHandler == null) {
            throw new IllegalArgumentException();
        }
        this.errorHandler = errorHandler;
    }

    /**
     * Defines the environment object of the mapper. To access the environment object
     * from your mapper file use <code>YourSymbols : [env];</code>.  The default environment
     * object is <code>this</code>.
     *
     * @param environment  may be null
     */
    public void setEnvironment(Object environment) {
        this.environment = environment;
    }

    public void setLogging(PrintWriter logParsing, PrintWriter logAttribution) {
        this.logParsing = logParsing;
        this.logAttribution = logAttribution;
    }

    public Parser getParser() {
        load();
        return parser;
    }

    public Oag getSemantics() {
        load();
        return oag;
    }

    //-- running the mapper

    public Object[] run(String fileName) throws IOException {
        return run(new File(fileName));
    }

    public Object[] run(net.oneandone.sushi.fs.Node node) throws IOException {
        return run(node.toString(), node.createReader());
    }

    public Object[] run(File file) throws IOException {
        return run(file.toURI().toURL().toString(), new FileReader(file));
    }

    public Object[] run(String context, Reader src) throws IOException {
        return run(new Position(context), src);
    }

    /**
     * Reads an stream, creates the syntax tree, computes the attributes and returns
     * the attributes of the start symbol. Main functionality of this class, all other
     * <code>run</code> methods use it.  Reports errors to the registered errorHander;
     * if there is no errorHandler defined, this method defines a PrintWriterErrorHandler
     * for System.err.
     *
     * @param  src when the method returns, src is always closed.
     * @return never null
     * @throws IOException to report errors
     */
    public Object[] run(Position position, Reader src) throws IOException {
        Node node;

        load();
        oag.setEnvironment(environment);
        oag.setLogging(logAttribution);
        parser.setErrorHandler(errorHandler);
        // casting is ok: the Treebuilder used in a mapper always creates Nodes
        node = (Node) parser.run(position, src, oag, logParsing);
        src.close();
        errorHandler.close();
        if (node == null) {
            throw new IllegalStateException("errorHandler.close expected to throw an exception");
        } else {
            return node.attrs;
        }
    }

    /**
     * Read-eval-print loop. Loop terminates if the specified end string is
     * entered. This method is handy to test mappers interactively.
     * TODO: should print all attributes returned by the mapper,
     * but currently, this would also print transport attributes.
     *
     * @param prompt  prompt string.
     * @param end     string to terminal real-eval-print loop; null specifies
     *                that the loop will not be terminated.
     */
    public void repl(String prompt, String end) {
        BufferedReader input;
        String line;
        Object[] result;

        input = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print(prompt);
            try {
                line = input.readLine();
            } catch (IOException e) {
                System.out.println("io error: " + e.toString());
                return;
            }
            if (line == null) {
                // EOF (ctrl-d on unix, ctrl-c on windows)
                return;
            }
            if (line.equals(end)) {
                return;
            }
            try {
                result = run("", new StringReader(line));
            } catch (IOException e) {
                throw new IllegalStateException("unexpected io exception from StringReader", e);
            }
            if ((result != null) && (result.length > 0)) {
                System.out.println(result[0]);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder buf;

        buf = new StringBuilder();
        buf.append("Parser:\n");
        buf.append(parser.toString());
        buf.append("Semantics:\n");
        buf.append(oag.toString());

        return buf.toString();
    }
}
