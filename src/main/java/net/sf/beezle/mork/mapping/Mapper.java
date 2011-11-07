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

package net.sf.beezle.mork.mapping;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

import net.sf.beezle.mork.parser.Parser;
import net.sf.beezle.mork.scanner.Position;
import net.sf.beezle.mork.semantics.Node;
import net.sf.beezle.mork.semantics.Oag;

/**
 * Wraps a stream to add mapping functionality. Implements the
 * analyzing parts a compiler or any other text processing programm:
 * scanner, parser and attribution. Mappers don't store symbol tables
 * because they would only be necessary for visualization.
 *
 * Technically, a <code>Mapper</code> is a translated <code>Mapping</code>.
 */
public class Mapper implements Serializable {
    private String name;
    private Parser parser;  // null: not loaded
    private Oag oag;  // undefined if not loaded
    private PrintStream logParsing;
    private PrintStream logAttribution;
    private Object environment;  // default environment is null
    private ErrorHandler errorHandler;

    //-------------------------------------------------------------------

    /** Creates a mapper with the specified name. **/
    public Mapper(String name) {
        this(name, null, null);
    }

    /**
     * Create a mapper with the specified parser and semantics. This constructor is used
     * my Mork when generating a mapper, applications will usually use <code>Mapper(String)</code>.
     */
    public Mapper(String name, Parser parser, Oag oag) {
        this.name = name;
        this.parser = parser;
        this.oag = oag;
        this.logParsing = null;
        this.logAttribution = null;
        this.errorHandler = null;
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
        Mapper result;
        Method m;
        Object[] tables;

        if (isLoaded()) {
            return;
        }
        loader = Mapper.class.getClassLoader();
        try {
            c = loader.loadClass(name);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e.toString());
        }
        try {
            m = c.getMethod("load", new Class[]{});
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e.toString());
        }
        try {
            tables = (Object[]) m.invoke(null, new Object[] {});
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(e.getTargetException().toString());
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e.toString());
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

    /**
     * Defines the handler to report errors to.
     *
     * @param errorHandler  may be null
     */
    public void setErrorHandler(ErrorHandler errorHandler) {
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

    public void setLogging(PrintStream logParsing, PrintStream logAttribution) {
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

    //-------------------------------------------------------------------
    // running the mapper

    public Object[] run(String fileName) {
        return run(new File(fileName));
    }

    public Object[] run(File file) {
        try {
            return run(file.toURL(), new FileReader(file));
        } catch (MalformedURLException e) {
            System.err.println("malformed file name: " + file);
            return null;
        } catch (FileNotFoundException e) {
            errorHandler();
            errorHandler.ioError(file.toString(), "file not found", e);
            return null;
        }
    }

    public Object[] run(URL url) {
        try {
            return run(url, new InputStreamReader(url.openStream()));
        } catch (IOException e) {
            errorHandler.ioError(url.toString(), "cannot open stream", e);
            return null;
        }
    }

    public Object[] run(Object context, Reader src) {
        return run(new Position(context), src);
    }

    /**
     * Reads an stream, creates the syntax tree, computes the attributes and returns
     * the attributes of the start symbol. Main functionality of this class, all other
     * <code>run</code> methods use it.  Reports errors to the registered errorHander;
     * if there is no errorHandler defined, this method defines a PrintStreamErrorHandler
     * for System.err.
     *
     * @param  src when the method returns, src is always closed.
     * @return null if error an error has been reported to the error handler
     */
    public Object[] run(Position position, Reader src) {
        Node node;

        load();
        errorHandler();
        oag.setEnvironment(environment);
        oag.setLogging(logAttribution);
        parser.setErrorHandler(errorHandler);
        // casting is ok: the Treebuilder used in a mapper always creates Nodes
        node = (Node) parser.run(position, src, oag, logParsing);
        try {
            src.close();
        } catch (IOException e) {
            // nothing I can do - a flow-up problem of a previous io exception
        }
        if (node == null) {
            return null;
        } else {
            return node.attrs;
        }
    }

    private void errorHandler() {
        if (errorHandler == null) {
            errorHandler = new PrintStreamErrorHandler(System.err);
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
            result = run("", new StringReader(line));
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
