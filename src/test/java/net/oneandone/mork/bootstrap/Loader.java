/*
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
package net.oneandone.mork.bootstrap;

import net.oneandone.mork.compiler.Mork;
import net.oneandone.mork.compiler.Specification;
import net.oneandone.mork.compiler.Syntax;
import net.oneandone.mork.reflect.Constructor;
import net.oneandone.mork.reflect.Function;
import net.oneandone.mork.reflect.Method;
import net.oneandone.mork.reflect.Selection;
import net.oneandone.mork.semantics.BuiltIn;
import net.oneandone.mork.semantics.IllegalLiteral;

import java.io.File;

/**
 * Loads an 0.4 mapper and runs it. Uses reflection, otherwise, I'd always need bsMork in my
 * classpath. Caution: this class is *not* for stub code to map to!
 */
public class Loader {
    // holds the last mapper file name loaded;  this is kind of ugly, but setting up/using
    // "[env]" is tedious since I'd have to use reflection ...
    private static String mapperFile;

    public static Specification loadMapper(String file) {
        mapperFile = file;
        return (Specification) load("net.oneandone.mork.bootstrap.MapperMapper", file);
    }

    public static Syntax loadSyntax(String fileName) throws IllegalLiteral {
        File absolute;

        absolute = Mork.absoluteFile(new File(mapperFile).getParentFile(), BuiltIn.parseString(fileName));
        return (Syntax) load("net.oneandone.mork.bootstrap.SyntaxMapper", absolute.getPath());
    }

    // returns null if an error has been reported
    private static Object load(String mapperName, String file) {
        Selection selection;
        Function loader;
        Object mapper;
        Function fn;
        Object[] objs;
        String name;

        name = "de.mlhartme.mxxx.mapping.Mapper";
        selection = Constructor.forName(name);
        selection = selection.restrictArgumentType(0, String.class);
        selection = selection.restrictArgumentCount(1);
        if (selection.size() != 1) {
            System.err.println("constructor not found: " + name);
            return null;
        }
        loader = selection.getFunction();
        try {
            mapper = loader.invokeN(mapperName);
        } catch (Exception e) {
            System.err.println("loading " + mapperName + " failed: " +
                               e.getClass().getName() + " " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        try {
            selection = Method.forName(name + ".run");
            selection = selection.restrictArgumentType(1, String.class);
            selection = selection.restrictArgumentCount(2);
            fn = selection.getFunction();
            objs = (Object[]) fn.invokeN(mapper, file);
        } catch (Exception e) {
            System.err.println("running " + mapperName + " failed: " +
                            e.getClass().getName() + " " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        if (objs == null) {
            return null;
        } else {
            return objs[0];
        }
    }
}
