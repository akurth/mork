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

import junit.framework.TestCase;
import net.sf.beezle.mork.mapping.Conversion;
import net.sf.beezle.mork.mapping.Definition;
import net.sf.beezle.mork.misc.GenericException;
import net.sf.beezle.mork.semantics.SemanticError;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;

public class MainFunctionalTest extends TestCase {
    /** working directory for runnning mork in. */
    private File tmpDir;

    /** this is where all the test files are from. */
    private File dataDir;

    private String stdout;
    private String stderr;

    private int exitCode;

    private Output output;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        String name;
        File[] all;
        String className;

        exitCode = 4242;  // to make it obvious if exitCode has not been assigned

        // TODO: guess project home
        name = "target/test-tmp";
        tmpDir = new File(name);
        if (!tmpDir.exists()) {
            tmpDir.mkdirs();
        }
        if (!tmpDir.isDirectory()) {
            fail("no such directory: " + name);
        }

        // do not delete the tmpDir directory (and create a new, empty one)
        // because it is expected to be the current directory and if I remove it,
        // relative file names no longer work.
        all = tmpDir.listFiles();
        for (File f : all) {
            rmRf(f);
        }

        name = "src/test/java";
        if (name == null) {
            fail("TEST_BASE property missing");
        }
        className = MainFunctionalTest.class.getPackage().getName();
        dataDir = new File(name, (className + ".files").replace('.', File.separatorChar));
        if (!dataDir.isDirectory()) {
            fail("no such directory: " + dataDir);
        }
    }

    @Override
    protected void tearDown() throws Exception {
        // do not delete tmp directory, it might help to locate problems
    }

    public void testEmpty() {
        run(new String[] {});
        assertTrue("stdout", !stdout.equals(""));
        assertEquals("stderr", "", stderr);
        assertEquals("exit code", Main.HELP, exitCode);
    }

    public void testHelp() {
        run(new String[] {"-help"});
        assertTrue("stdout", !stdout.equals(""));
        assertEquals("stderr", "", stderr);
        assertEquals("exit code", Main.HELP, exitCode);
    }

    public void testInvalidOption() {
        run(new String[] {"-invalidoption"});
        assertEquals("stdout", "", stdout);
        assertTrue("stderr", !stderr.equals(""));
        assertEquals("exit code", Main.COMMANDLINE_ERROR, exitCode);
    }

    public void testNormal() throws IOException {
        File file;

        runWithFiles(
            new String[] { "Empty.mapper", "Ab.syntax" },
            new String[] { "Empty.mapper" }
        );
        assertTrue("stdout", !stdout.equals(""));
        assertEquals("stderr", "", stderr);
        assertEquals("exit code", 0, exitCode);

        file = new File("Empty.class");
        assertTrue("class file exists: " + file, file.exists());
        file = new File("Empty.lst");
        assertTrue("no listing: " + file, !file.exists());
    }

    public void testNoSuchFile() throws IOException {
        File[] files;

        runWithFiles(
            new String[] {},
            new String[] { "SomeNonExistingFile,map" }
        );
        assertTrue("stdout", !stdout.equals(""));
        assertTrue("some errors", !stderr.equals(""));
        assertTrue("error code", 0 != exitCode);

        files = tmpDir.listFiles();
        assertEquals("no files", 0, files.length);
    }

    public void testInvalidMapping() throws IOException {
        File[] files;

        runWithFiles(
            new String[] { "Invalid.mapper" },
            new String[] { "Invalid.mapper" }
        );
        assertTrue("stdout", !stdout.equals(""));
        assertTrue("stderr", !stderr.equals(""));
        assertEquals("exit code", Main.COMPILE_ERROR, exitCode);

        files = tmpDir.listFiles();
        assertEquals("no files", 1, files.length);
    }

    public void testInvalidGrammar() throws IOException {
        File[] files;

        runWithFiles(
            new String[] { "InvalidGrammar.mapper", "Invalid.syntax" },
            new String[] { "InvalidGrammar.mapper" }
        );
        assertTrue("stdout", !stdout.equals(""));
        assertTrue("stderr", !stderr.equals(""));
        assertEquals("exit code", Main.COMPILE_ERROR, exitCode);

        files = tmpDir.listFiles();
        assertEquals("no files", 2, files.length);
    }

    //-------------------------------------------------------------
    // check individual error messages

    public void testUndefinedSymbol() throws IOException {
        assertError("UndefinedSymbol.mapper", "Ab.syntax", Stubs.UNDEFINED_SYMBOL);
    }

    public void testArgumentNotAssigable() throws IOException {
        assertError("ArgumentNotAssignable.mapper", "Ab.syntax", Definition.ARGUMENT_NOT_ASSIGNABLE);
    }
    public static void noArguments() {
    }

    public void testArgumentTypeMismatch() throws IOException {
        assertError("ArgumentTypeMissmatch.mapper", "Ab.syntax", Conversion.ARGUMENT_TYPE_MISMATCH);
    }
    public static String ab(int a) {
        return null;
    }
    public static String ab(String b) {
        return null;
    }
    public static int aNumber() {
        return 1;
    }
    public static String aString() {
        return "string";
    }

    private void assertError(String mapFile, String grmFile, String error) throws IOException {
        Object obj;

        runWithFiles(new String[] { mapFile, grmFile }, new String[] { mapFile });
        assertEquals("exit code", Main.COMPILE_ERROR, exitCode);
        assertEquals(1, output.getErrorCount());
        obj = output.getLastError();
        if (obj instanceof SemanticError) {
            obj = ((SemanticError) obj).exception;
        }
        if (obj instanceof GenericException) {
            obj = ((GenericException) obj).id;
        }
        assertEquals(error, obj);
    }



    public void testListing() throws IOException {
        File file;

        runWithFiles(
            new String[] { "Empty.mapper", "Ab.syntax" },
            new String[] { "-lst", "Empty.mapper" }
        );
        assertTrue("stdout <" + stdout + ">", !stdout.equals(""));
        assertEquals("stderr", "", stderr);
        assertEquals("exit code", 0, exitCode);

        file = new File(tmpDir, "Empty.class");
        assertTrue("class file exists: " + file, file.exists());
        file = new File(tmpDir, "Empty.lst");
        assertTrue("listing file exists: " + file, file.exists());
    }

    private void run(String[] args) {
        ByteArrayOutputStream fakedOut;
        ByteArrayOutputStream fakedErr;

        fakedOut = new ByteArrayOutputStream();
        fakedErr = new ByteArrayOutputStream();
        output = new Output(new PrintStream(fakedErr));
        output.normal = new PrintStream(fakedOut);
        exitCode = new Main(output).run(args);
        stdout = fakedOut.toString();
        stderr = fakedErr.toString();
    }

    private void runWithFiles(String[] names, String[] args) throws IOException {
        for (String name : names) {
            copy(new File(dataDir, name), new File(tmpDir, name));
        }
        run(args);
    }

    private static void copy(File srcFile, File destFile) throws IOException {
        FileReader src;
        FileWriter dest;
        int c;

        src = new FileReader(srcFile);
        try {
            dest = new FileWriter(destFile);
            try {
                for (c = src.read(); c != -1; c = src.read()) {
                    dest.write((char) c);
                }
            } finally {
                dest.close();
            }
        } finally {
            src.close();
        }
    }

    private static void rmRf(File file) throws IOException {
        File[] lst;

        if (file.isDirectory()) {
            lst = file.listFiles();
            for (File child : lst) {
                rmRf(child);
            }
        }
        if (!file.delete()) {
            throw new IOException("cannot delete TMP_DIR: " + file);
        }

    }
}
