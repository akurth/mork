/**
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
package net.sf.beezle.mork.bootstrap;

import net.sf.beezle.mork.compiler.Mork;
import net.sf.beezle.mork.compiler.Specification;
import net.sf.beezle.mork.compiler.Syntax;
import net.sf.beezle.mork.reflect.Constructor;
import net.sf.beezle.mork.reflect.Function;
import net.sf.beezle.mork.reflect.Method;
import net.sf.beezle.mork.reflect.Selection;
import net.sf.beezle.mork.semantics.BuiltIn;
import net.sf.beezle.mork.semantics.IllegalLiteral;

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
        return (Specification) load("net.sf.beezle.mork.bootstrap.MapperMapper", file);
    }

    public static Syntax loadSyntax(String fileName) throws IllegalLiteral {
        File absolute;

        absolute = Mork.absoluteFile(new File(mapperFile).getParentFile(), BuiltIn.parseString(fileName));
        return (Syntax) load("net.sf.beezle.mork.bootstrap.SyntaxMapper", absolute.getPath());
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
