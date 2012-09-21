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

import junit.framework.TestCase;
import net.oneandone.mork.mapping.Conversion;
import net.oneandone.mork.mapping.Definition;

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

        File[] all;
        String className;

        exitCode = 4242;  // to make it obvious if exitCode has not been assigned

        // TODO: guess project home
        tmpDir = new File("target/test-tmp");
        if (!tmpDir.exists()) {
            tmpDir.mkdirs();
        }
        if (!tmpDir.isDirectory()) {
            fail("no such directory: " + tmpDir);
        }

        // do not delete the tmpDir directory (and create a new, empty one)
        // because it is expected to be the current directory and if I remove it,
        // relative file names no longer work.
        all = tmpDir.listFiles();
        for (File f : all) {
            rmRf(f);
        }

        className = MainFunctionalTest.class.getPackage().getName();
        dataDir = new File("src/test/java", (className + ".files").replace('.', File.separatorChar));
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
        assertEquals("exit code", -1, exitCode);
    }

    public void testHelp() {
        run(new String[] {"-help"});
        assertTrue("stdout", !stdout.isEmpty());
        assertEquals("stderr", "", stderr);
        assertEquals("exit code", -1, exitCode);
    }

    public void testInvalidOption() {
        run(new String[] {"-invalidoption"});
        assertEquals("stdout", "", stdout);
        assertTrue("stderr", !stderr.equals(""));
        assertEquals("exit code", -1, exitCode);
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
        assertEquals("exit code", -1, exitCode);

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
        assertEquals("exit code", -1, exitCode);

        files = tmpDir.listFiles();
        assertEquals("no files", 2, files.length);
    }

    //-- check individual error messages

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
        runWithFiles(new String[] { mapFile, grmFile }, new String[] { mapFile });
        assertEquals("exit code", -1, exitCode);
        assertTrue(stderr, stderr.indexOf(error) != -1);
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

    // TODO: sushi
    private static void copy(File srcFile, File destFile) throws IOException {
        int c;

        try (FileReader src = new FileReader(srcFile);
             FileWriter dest = new FileWriter(destFile)) {
            for (c = src.read(); c != -1; c = src.read()) {
                dest.write((char) c);
            }
        }
    }

    // TODO: sushi
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
