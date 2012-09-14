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
import net.oneandone.mork.reflect.Method;
import net.oneandone.mork.reflect.Selection;

import java.io.File;
import java.io.IOException;
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
    private final Function mapperFn;

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
    public Object invoke(File source) throws IOException {
        if (mapperFn != null) {
            return invokeMapperFn(source.getPath());
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
    private Object invokeMapper(File source) throws IOException {
        Object[] results;
        String name;
        Reader src;

        name = source.getPath();
        mork.output.verbose("mapping " + name);
        results = run(name);
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
        for (Class<?> c : tmp) {
            if (!(RuntimeException.class.isAssignableFrom(c) || Error.class.isAssignableFrom(c))) {
                throw new GenericException("mapper method must not throw checked exception: " + c);
            } else {
                // ok
            }
        }
        return fn;
    }
}
