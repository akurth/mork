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
package net.oneandone.mork.compiler;

import net.oneandone.mork.classfile.Access;
import net.oneandone.mork.classfile.Bytecodes;
import net.oneandone.mork.classfile.ClassDef;
import net.oneandone.mork.classfile.ClassRef;
import net.oneandone.mork.classfile.Code;
import net.oneandone.mork.classfile.Output;
import net.oneandone.mork.mapping.Mapper;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;


public class MapperCompiler implements Bytecodes {
    private net.oneandone.mork.compiler.Output output;

    public MapperCompiler(net.oneandone.mork.compiler.Output output) {
        this.output = output;
    }

    /**
     * @param mapperName        mapper name as specified in the map file.
     * @param src               file that specified the mapper
     * @param explicitOutputDir "-d", points to a directory or null.
     */
    public void run(Mapper mapper, String mapperName, File src, File explicitOutputDir) throws IOException {
        String baseName;
        File outputDir;  // directory to write all class files to

        String mapperClassName;
        File mapperFile;
        String functionClassName;
        String functionFileBase;

        ClassDef c;
        FunctionCompiler fc;

        baseName = mapperName.substring(mapperName.lastIndexOf('.') + 1);
          // also ok of idx -1
        outputDir = outputDir(src, explicitOutputDir, mapperName);

        mapperClassName = mapperName;
        mapperFile = new File(outputDir, baseName + ".class");
        functionClassName = mapperName + "Functions";
        functionFileBase = new File(outputDir, baseName + "Functions").getPath();

        fc = new FunctionCompiler(functionClassName);
        customs[0] = fc;
        output.verbose("translating " + mapperClassName);
        c = translate(mapper, mapperClassName);

        output.verbose("writing " + mapperFile);
        try {
            Output.save(c, mapperFile);
        } catch (IOException e) {
            output.error(mapperFile.toString(), "write failed: " + e);
        }
        try {
            fc.save(functionFileBase);
        } catch (IOException e) {
            output.error(functionFileBase.toString(), "write failed: " + e);
        }
        output.verbose("done");
    }

    /** creates new directory if necessary. */
    public File outputDir(File src, File explicitOutputDir, String mapperName) throws IOException {
        File outputDir;
        int prev;
        int idx;
        File subDir;

        if (explicitOutputDir == null) {
            outputDir = src.getParentFile();
        } else {
            outputDir = explicitOutputDir;
            idx = mapperName.indexOf('.');
            prev = 0;
            while (idx != -1) {
                subDir = new File(outputDir, mapperName.substring(prev, idx));
                if (!subDir.isDirectory()) {
                    if (!subDir.mkdir()) {
                        throw new IOException("cannot create directory: " + subDir);
                    }
                }
                prev = idx + 1;
                idx = mapperName.indexOf('.', prev);
                outputDir = subDir;
            }
        }
        return outputDir;
    }

    private ClassDef translate(Mapper mapper, String className) {
        ClassDef result;
        ObjectCompiler compiler;
        Code code;

        code = new Code();
        code.locals = 1; // this
        result = createClass(className, code);
        compiler = new ObjectCompiler(code, code.allocate(ClassRef.INT), customs, result);
        code.emit(LDC, 2);
        code.emit(ANEWARRAY, ClassRef.OBJECT); // this, the new Mapper
        code.emit(DUP);
        code.emit(LDC, 0);
        compiler.run(mapper.getParser());
        code.emit(AASTORE);
        code.emit(DUP);
        code.emit(LDC, 1);
        compiler.run(mapper.getSemantics());
        code.emit(AASTORE);
        code.emit(ARETURN);
        return result;
    }

    private ClassDef createClass(String className, Code code) {
        ClassDef result;

        result = new ClassDef(new ClassRef(className), ClassRef.OBJECT);
        result.addMethod(Access.fromArray(Access.PUBLIC, Access.STATIC),
                         ClassRef.OBJECT, "load", ClassRef.NONE, code);
        return result;
    }

    //-- declarations how to compile the various classes

    private static final CustomCompiler[] customs = {
        null,  // reserved for function compiler
        new GenericCompiler(net.oneandone.mork.grammar.Rule.class,
            new String[] { "left", "right" }),
        new GenericCompiler(net.oneandone.mork.parser.ParserTable.class,
            new String[] { "startState", "symbolCount", "eofSymbol", "getStateCount",
                           "packValues", "lengths", "lefts", "modes" }),
        new GenericCompiler(ConflictResolver.class,
            new String[] { "lines" }),
        new GenericCompiler(net.oneandone.mork.compiler.Line.class,
            new String[] { "terminals", "action" }),
        new GenericCompiler(net.oneandone.mork.semantics.Attribution.class,
            new String[] { "function", "resultOfs", "resultAttr", "argsOfs", "argsAttr"}),
        new GenericCompiler(net.oneandone.mork.semantics.Oag.class,
            new String[] { "visits", "internalAttrs" }),
        new GenericCompiler(net.oneandone.mork.semantics.Visits.class,
            new String[] { "visits" }),
        new GenericCompiler(net.oneandone.mork.parser.Parser.class,
            new String[] { "table", "resolvers", "scannerFactory"}),
        new GenericCompiler(net.oneandone.mork.scanner.ScannerFactory.class,
            new String[] { "start", "modeCount", "table" }),
        new GenericCompiler(net.oneandone.sushi.util.IntBitSet.class,
            new String[] { "data" }),
        new GenericCompiler(net.oneandone.sushi.util.IntArrayList.class,
            new String[] { "size", "data" }),
        new GenericCompiler(net.oneandone.mork.misc.StringArrayList.class,
            new String[] { "size", "data" }),
        new GenericCompiler(java.lang.Integer.class,
            new String[] {
                "net.oneandone.mork.compiler.MapperCompiler.saveInteger" },
                "net.oneandone.mork.compiler.MapperCompiler.loadInteger"),
        new GenericCompiler(java.lang.Class.class,
            new String[] {
                "net.oneandone.mork.compiler.MapperCompiler.saveClass" },
                "net.oneandone.mork.compiler.MapperCompiler.loadClass"),
        new GenericCompiler(java.lang.reflect.Constructor.class,
            new String[] { "getDeclaringClass", "getParameterTypes" },
            "net.oneandone.mork.compiler.MapperCompiler.loadConstructor"),
        new GenericCompiler(java.lang.reflect.Method.class,
            new String[] { "getDeclaringClass", "getName", "getParameterTypes" },
            "net.oneandone.mork.compiler.MapperCompiler.loadMethod"),
        new GenericCompiler(java.lang.reflect.Field.class,
            new String[] { "getDeclaringClass", "getName" },
            "net.oneandone.mork.compiler.MapperCompiler.loadField")
    };

    //-- static "constructor" code for various GenericCompiler

    public static int saveInteger(Integer i) {
        return i.intValue();
    }

    public static Integer loadInteger(int i) {
        return new Integer(i);
    }

    public static String saveClass(Class<?> c) {
        return new ClassRef(c).toFieldDescriptor();
    }

    public static Class<?> loadClass(String name) {
        return ClassRef.forFieldDescriptor(name).lookup();
    }

    public static Field loadField(Class<?> type, String name) throws NoSuchFieldException {
        return type.getDeclaredField(name);
    }

    public static Constructor<?> loadConstructor(Class<?> type, Class<?>[] args) throws NoSuchMethodException {
        return type.getDeclaredConstructor(args);
    }

    public static Method loadMethod(Class<?> type, String name, Class<?>[] args) throws NoSuchMethodException {
        return type.getDeclaredMethod(name, args);
    }
}
