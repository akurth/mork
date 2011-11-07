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

import net.sf.beezle.mork.mapping.Mapper;
import net.sf.beezle.mork.misc.GenericException;
import net.sf.beezle.mork.reflect.Function;
import net.sf.beezle.mork.reflect.Method;
import net.sf.beezle.mork.reflect.Selection;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;

/**
 * Global state, instances represent globale options. The this class forms a Java API
 * to Mork -- whereas the Main class forms a command-line interface. Use <code>Mork</code>
 */

public class MorkMapper extends Mapper {
    /**
     * IO settings
     */
    private final Mork mork;

    /**
     * Overrides mapper if != null
     */
    private Function mapperFn;

    public MorkMapper(Mork mork, String mapperName) {
        this(mork, mapperName, null);
    }

    public MorkMapper(Mork mork, String mapperName, Function mapperFn) {
        super(mapperName);

        this.mork = mork;
        this.mapperFn = mapperFn;
        setEnvironment(mork);
        setErrorHandler(mork.output);
    }

    /**
     * @return null if an error has been reported
     */
    public Object invoke(File source) {
        if (mapperFn != null) {
            return (Specification) invokeMapperFn(source.getPath());
        } else {
            return invokeMapper(source);
        }
    }

    /**
     * Read input. Wraps mapper.read with Mork-specific error handling.
     * Return type depends on the mapper actually used.
     *
     * @return  null if an error has been reported
     */
    private Object invokeMapper(File source) {
        Object[] results;
        String name;
        Reader src;

        name = source.getPath();
        mork.output.verbose("mapping " + name);
        setLogging(mork.output.verboseParsing, mork.output.verboseAttribution);
        if (source.getName().endsWith(".dtd")) {
            // TODO: ugly hack
            try {
                src = new FileReader(source);
            } catch (FileNotFoundException e) {
                mork.output.error(name, "file not found");
                return null;
            }
            src = new PushbackReader(src);
            try {
                ((PushbackReader) src).unread('2');
            } catch (IOException e) {
                throw new RuntimeException();
            }
            results = run(source, src);
        } else {
            results = run(name);
        }
        mork.output.verbose("finished mapping " + name);
        if (results == null) {
            return null;
        } else {
            return results[0];
        }
    }

    /**
     * @return null if an error has been reported
     */
    private Object invokeMapperFn(String source) {
        Throwable te;
        Object result;

        try {
            result = (Specification) mapperFn.invokeN(source);
        } catch (InvocationTargetException e) {
            te = e.getTargetException();
            if (te instanceof RuntimeException) {
                throw (RuntimeException) te;
            } else if (te instanceof Error) {
                throw (Error) te;
            } else {
                te.printStackTrace();
                throw new RuntimeException("unexpected checked exception: " +
                    te.getClass().getName() + ": " + te.getMessage());
            }
        }
        return result;
    }

    public static Function lookupMapperFn(String name, Class<?> resultType) throws GenericException {
        Selection selection;
        Function fn;
        Class<?>[] tmp;

        selection = Method.forName(name);
        if (selection.isEmpty()) {
            throw new GenericException("no such method: " + name);
        }
        selection = selection.restrictArgumentCount(1);
        selection = selection.restrictArgumentType(0, String.class);
        fn = selection.getFunction();
        if (fn == null) {
            throw new GenericException("argument type mismatch: 1 string argument expected: " +
                                           name);
        }
        if (!resultType.isAssignableFrom(fn.getReturnType())) {
            throw new GenericException("return type mismatch, expected: "+ resultType.getName());
        }
        tmp = fn.getExceptionTypes();
        for (int i = 0; i < tmp.length; i++) {
            if (!(RuntimeException.class.isAssignableFrom(tmp[i]) || Error.class.isAssignableFrom(tmp[i]))) {
                throw new GenericException("mapper method must not throw checked exception: " +
                                               tmp[i]);
            } else {
                // ok
            }
        }
        return fn;
    }
}