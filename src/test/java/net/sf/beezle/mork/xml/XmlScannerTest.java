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

package net.sf.beezle.mork.xml;

import net.sf.beezle.mork.misc.GenericException;
import net.sf.beezle.mork.misc.StringArrayList;
import net.sf.beezle.mork.scanner.Position;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import junit.framework.TestCase;
import net.sf.beezle.mork.scanner.Scanner;

/**
 * Test XmlScanner
 * TODO: factor out functionality common with GrammarScannerTest.
 */
public class XmlScannerTest extends TestCase {
    private StringArrayList symbolTable;
    private XmlScanner scanner;

    private URL dataDir;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        String name;
        File file;
        String path;

        name = "src/test/java";
        path = XmlScannerTest.class.getPackage().getName().replace('.', File.separatorChar);
        file = new File(name, path + File.separator + "files");
        if (!file.isDirectory()) {
            throw new Exception("no such directory: " + file);
        }
        dataDir = file.toURL();
    }

    public void testSimple() throws IOException {
        create(new String[] { "a" }, "<a></a>");
        start("a");
        end("a");
        eof();
    }

    public void testSimpleTwice() throws IOException {
        String input = "<a></a>";

        create(new String[] { "a" }, input);
        start("a");
        end("a");
        eof();

        scanner.open(new Position(new URL(dataDir, "virtual.xml")), new StringReader(input));
        start("a");
        end("a");
        eof();
    }

    public void testDoctypeDecl() throws IOException {
        create(new String[] { "a" }, "<!DOCTYPE a SYSTEM 'test.dtd'><a></a>");
        start("a");
        end("a");
        eof();
    }

    public void testConditional() throws IOException {
        System.out.println("TODO: conditional");
        /*      create(new String[] { "a" },
          "<!DOCTYPE a [\n" +
          " <!ENTITY % draft 'INCLUDE'>\n" +
          " <!ENTITY % final 'IGNORE'>\n" +
          " <!ENTITY % conditional SYSTEM 'conditional.xml'>\n" +
          "  %conditional;\n" +
          "]>\n" +
          "<a>&mode;</a>");
        start("a");
        text("draft mode");
        end("a");
         eof();*/
    }

    public void testDocbook() throws IOException {
        System.out.println("TODO: docbook");        /*
        // TODO: url
        create(new String[] { "a" },
           "<!DOCTYPE a SYSTEM '/usr/local/src/docbook-xml-4.12/docbookx.dtd'><a></a>");
        start("a");
        end("a");
        eof(); */
    }

    /** test tailing spaces commulated by PE references */
    public void testTailingSpace() throws IOException {
        create(new String[] { "a" },
           "<!DOCTYPE a [\n" +
           " <!ENTITY % A 'x '>\n" +
           " <!ELEMENT a (%A; )>\n" +
           "]>\n" +
           "<a/>\n");
        start("a");
        end("a");
         eof();
    }

    public void testWebXml() throws IOException {
        // TODO: URL
        create(new String[] { "a" },
            "<!DOCTYPE a SYSTEM '/usr/local/src/jakarta-tomcat-3.3.1-dev-src/" +
            "src/share/org/apache/tomcat/resources/web.dtd'><a></a>");
        start("a");
        end("a");
        eof();
    }

    public void testEmptyTag() throws IOException {
        String element = "tag";

        create(new String[] { element }, "<" + element + "/>");
        start(element);
        end(element);
        eof();
    }

    public void testTwoTagsWithSpace() throws IOException {
        String a = "A";
        String b = "B";

        create(new String[] { a, b },
               "<" + a + ">" + " " + "<" + b + ">" + "\n</" + a + ">\t</" + b + ">");
        start(a);
        start(b);
        end(a);
        end(b);    // tags are not properly nested -- but the scanner must not complain
        eof();
    }

    public void testNormalText() throws IOException {
        validCharData("hello");
    }
    public void testLeftSpacedText() throws IOException {
        validCharData(" hello");
    }
    public void testRightSpacedText() throws IOException {
        validCharData("hello ");
    }
    public void testMidSpacedText() throws IOException {
        validCharData("hel lo");
    }
    public void test8bitAscii() throws IOException {
        StringBuffer b;
        int i;

        b = new StringBuffer();
        for (i = 128; i < 256; i++) {
            b.append((char) i);
        }
        validCharData(b.toString());
    }

    public void testInvalidCharData() throws IOException {
        invalidCharData("&");
        invalidCharData("<");
        invalidCharData("]]>");
         // exerces considers this invalid, but I'm not sure how to read $ 2.5 ...
    }

    public void testNormalCData() throws IOException {
        validCharData("<![CDATA[some markup: <a></b><c/>]]>", "some markup: <a></b><c/>");
    }
    public void testEmptyCData() throws IOException {
        validCharData("<![CDATA[]]>", "");
    }
    public void testComment() throws IOException {
        validCharData("left<!-- comment -->right", "leftright");
    }
    public void testPI() throws IOException {
        validCharData("left<?blablabla1 ?>right", "leftright");
    }

    public void testPredefinedEntities() throws IOException {
        validCharData("&lt;&gt;&amp;&quot;&apos;", "<>&\"'");
    }

    public void testSpecialText() throws IOException {
        System.out.println("TODO: ]]&gt; is not accepted");
        // TODO: validCharData("]]&gt;", "]]>");
    }

    public void testInternalEntity() throws IOException {
        create(new String[] { "book" },
          "<!DOCTYPE book [\n" +
          " <!ENTITY abc 'xyz'>\n" +
          "]>\n" +
          "<book>&abc;</book>");
        start("book");
        text("xyz");
        end("book");
        eof();
    }

    public void testBackwardInternalEntity() throws IOException {
        create(new String[] { "book" },
          "<!DOCTYPE book [\n" +
          " <!ENTITY first 'xyz'>\n" +
          " <!ENTITY second 'pre&first;post'>\n" +
          "]>\n" +
          "<book>&second;</book>");
        start("book");
        text("prexyzpost");
        end("book");
        eof();
    }

    /**
     * Forward references to internal entities are ok, because they are not
     * expanded unless applied.
     */
    public void testForwardInternalEntity() throws IOException {
        create(new String[] { "book" },
          "<!DOCTYPE book [\n" +
          " <!ENTITY first 'pre&second;post'>\n" +
          " <!ENTITY second 'xyz'>\n" +
          "]>\n" +
          "<book>&first;</book>");
        start("book");
        text("prexyzpost");
        end("book");
        eof();
    }

    public void testExternalEntityUsedTwice() throws IOException {
        create(new String[] { "book", "chapter" },
          "<!DOCTYPE book [\n" +
          " <!ENTITY abc SYSTEM 'include.xml'>\n" +
          " <!ENTITY def SYSTEM '../files/include.xml'>\n" +
          "]>\n" +
          "<book>&abc;&abc;&def;</book>");
        start("book");
        start("chapter");
        text("some text");
        end("chapter");
        start("chapter");
        text("some text");
        end("chapter");
        start("chapter");
        text("some text");
        end("chapter");
        end("book");
    }

    public void testExternalEntityNested() throws IOException {
        create(new String[] { "book", "part", "chapter" },
          "<!DOCTYPE book [\n" +
          " <!ENTITY inner SYSTEM 'include.xml'>\n" +
          " <!ENTITY outer SYSTEM 'nested.xml'>\n" +
          "]>\n" +
          "<book>&outer;</book>");
        start("book");
        start("part");
        start("chapter");
        text("some text");
        end("chapter");
        end("part");
        end("book");
    }

    public void testInternalPE() throws IOException {
        create(new String[] { "root" },
          "<!DOCTYPE root [\n" +
          " <!ENTITY % A 'B'>\n" +
          " <!ENTITY C '%A;'>\n" +
          "]>\n" +
          "<root>pre&C;post</root>");
        start("root");
        text("preBpost");
        end("root");
    }

    public void testInternalPEEmbedded() throws IOException {
        System.out.println("TODO: embedded PE"); /*
        create(new String[] { "root" },
          "<!DOCTYPE root [\n" +
          " <!ENTITY % A 'B'>\n" +
          " <!ENTITY C 'pre%A;post'>\n" +
          "]>\n" +
          "<root>&C;</root>");
        start("root");
        text("preBpost");
         end("root");*/
    }

    public void testRecursiveEntity() throws IOException {
        try {
            create(new String[] { "book" },
              "<!DOCTYPE book [\n" +
              " <!ENTITY recursive 'foo &recursive; bar'>\n" +
              "]>\n" +
              "<book>&recursive;</book>");
            fail("recursive entities not detected");
        } catch (IOException e) {  // TODO: new exception handling
            // ok
        }
    }

    public void testInternalPEwithQuotes() throws IOException {
        System.out.println("PE with quotes"); /*
        create(new String[] { "root" },
          "<!DOCTYPE root [\n" +
          " <!ENTITY % YN '\"YES\"'>\n" +
          " <!ENTITY WhatHeSaid \"He said %YN;\">\n" +
          "]>\n" +
          "<root>&WhatHeSaid;</root>");
        start("root");
        text("He said \"YES\"");
         end("root");*/
    }

    public void testExternalPE() throws IOException {
        create(new String[] { "root", "a", "b" },
          "<!DOCTYPE root [\n" +
          " <!ENTITY % entities SYSTEM 'entities.xml'>\n" +
          " %entities;\n" +
          "]>\n" +
          "<root>&a;&b;</root>");
        start("root");
        start("a");
        text("t1");
        end("a");
        start("b");
        text("t2");
        end("b");
        end("root");
    }

    public void testTwoEntityNamespaces() throws IOException {
        create(new String[] { "root" },
          "<!DOCTYPE root [\n" +
          " <!ENTITY A 'geA'>\n" +
          " <!ENTITY % A 'peA'>\n" +
          " <!ENTITY % useGE '&A;'>\n" +
          " <!ENTITY % usePE '%A;'>\n" +
          " <!ENTITY ge '%useGE;'>\n" +
          " <!ENTITY pe '%usePE;'>\n" +
          "]>\n" +
          "<root>&ge;&pe;</root>");
        start("root");
        text("geApeA");
        end("root");
    }

    public void testAppendixDEntitiesFirst() throws IOException {
        System.out.println("TODO: first appendix example");
        /*
        create(new String[] { "root" },
          "<!DOCTYPE root [\n" +
          " <!ENTITY example '<p>An ampersand (&#38;#38;) may be escaped " +
                   "numerically (&#38;#38;#38;) or with a general entity " +
                   "(&amp;amp;).</p>'>\n" +
          "]>\n" +
          "<root>&example;</root>");
        start("root");
         text("An ampersant (&) may be escaped numerically (&#38;) " +
         "or with a general entity (&amp;).");
        end("root");*/
    }

    public void testAppendixDEntitiesSecond() throws IOException {
        System.out.println("TODO: second appendix example");
    }


    public void testDecimalCharRef() throws IOException {
        StringBuffer b;
        StringBuffer c;
        int i;

        b = new StringBuffer();
        c = new StringBuffer();
        for (i = 13; i < Character.MAX_VALUE; i += 173) {
            b.append("&#");
            b.append(i);
            b.append(";");
            c.append((char) i);
        }
        validCharData(b.toString(), c.toString());
    }

    public void testHexCharRef() throws IOException {
        StringBuffer b;
        StringBuffer c;
        int i;

        b = new StringBuffer();
        c = new StringBuffer();
        for (i = 11; i < Character.MAX_VALUE; i += 181) {
            b.append("&#x");
            b.append(Integer.toHexString(i));
            b.append(";");
            c.append((char) i);
        }
        validCharData(b.toString(), c.toString());
    }

    //-----------------------------------------------
    public void testAttributeWithQuotes() throws IOException {
        createAttr(Attribute.IMPLIED, null, "<a b='1'></a>");
        start("a");
        attribute("a", "b", "1");
        end("a");
        eof();
    }

    public void testAttributeWithDoubleQuotes() throws IOException {
        createAttr(Attribute.IMPLIED, null, "<a b=\"1\"></a>");
        start("a");
        attribute("a", "b", "1");
        end("a");
        eof();
    }

    public void testAttributeWithTailingSpace() throws IOException {
        createAttr(Attribute.IMPLIED, null, "<a b='1' ></a>");
        start("a");
        attribute("a", "b", "1");
        end("a");
        eof();
    }

    public void testAttributeWithEqSpace() throws IOException {
        createAttr(Attribute.IMPLIED, null, "<a b = '1'></a>");
        start("a");
        attribute("a", "b", "1");
        end("a");
        eof();
    }

    public void testMissingAttribute() throws IOException {
        createAttr(Attribute.IMPLIED, null, "<a></a>");
        start("a");
        end("a");
        eof();
    }

    public void testDuplicateAttribute() throws IOException {
        createAttr(Attribute.IMPLIED, null, "<a b='7' b='32'></a>");
        try {
            start("a");
            fail("duplicate attribute not detected");
        } catch (IOException e) { // TODO: new exception handling
            // ok, that what I want
        }
    }

    public void testNoneAttributeSupplied() throws IOException {
        createAttr(Attribute.NONE, "xxx", "<a b='111'></a>");
        start("a");
        attribute("a", "b", "111");
        end("a");
        eof();
    }
    public void testNoneAttributeDefaulted() throws IOException {
        createAttr(Attribute.NONE, "xxx", "<a></a>");
        start("a");
        attribute("a", "b", "xxx");
        end("a");
        eof();
    }

    public void testRequiredAttributeOk() throws IOException {
        createAttr(Attribute.REQUIRED, null, "<a b='23'></a>");
        start("a");
        attribute("a", "b", "23");
        end("a");
        eof();
    }
    public void testRequiredAttributeMissing() throws IOException {
        createAttr(Attribute.REQUIRED, null, "<a></a>");
        try {
            start("a");
            fail("missing required attribute not detected");
        } catch (IOException e) { // TODO: new exception handling
            // ok, that's what I want
        }
    }

    public void testFixedAttributeOk() throws IOException {
        createAttr(Attribute.FIXED, "xxx", "<a b='xxx'></a>");
        start("a");
        attribute("a", "b", "xxx");
        end("a");
        eof();
    }

    public void testFixedAttributeMissing() throws IOException {
        createAttr(Attribute.FIXED, "xxx", "<a></a>");
        try {
            start("a");
            fail("missing fixed attribute not detected");
        } catch (IOException e) { // TODO: new exception handling
            // ok, that's what I want
        }
    }

    public void testFixedAttributeDifferent() throws IOException {
        createAttr(Attribute.FIXED, "xxx", "<a b='yyy'></a>");
        try {
            start("a");
            fail("different fixed attribute not detected");
        } catch (IOException e) { // TODO: new exception handling
            // ok, that's what I want
        }
    }

    //-------------------------------------------------------------------------------------------

    private void validCharData(String text) throws IOException {
        validCharData(text, text);
    }

    private void validCharData(String text, String cmp) throws IOException {
        String element;

        element = "E";
        create(new String[] { element }, "<" + element + ">" + text + "</" + element + ">");
        start(element);
        text(cmp);
        end(element);
        eof();
    }

    private void invalidCharData(String chardata) throws IOException {
        try {
            create(new String[] { "E" }, "<E>" + chardata + "</E>");
            start("E");
            text(chardata);
            end("E");
            fail("invalid chardata accepted: " + chardata);
        } catch (IOException e) { // TODO: new exception handling
            // ok, that's what I want
        }
    }

    private void start(String tag) throws IOException {
        scan("<" + tag + ">", XmlSyntax.toStartTag(symbolTable, symbolTable.indexOf(tag)), null);
    }
    private void end(String tag) throws IOException {
        scan("</" + tag + ">", XmlSyntax.toEndTag(symbolTable, symbolTable.indexOf(tag)), null);
    }
    private void text(String cmp) throws IOException {
        scan("text", XmlScanner.PCTEXT, cmp);
    }
    private void attribute(String element, String attribute, String value) throws IOException {
        scan(element + "@" + attribute,
             XmlSyntax.toAttribute(symbolTable, element, attribute), value);
    }
    private void eof() throws IOException {
        scan("eof", Scanner.EOF, null);
    }

    private void scan(String what, int terminal, String text) throws IOException {
        int currentTerminal;

        currentTerminal = scanner.next(0);
        assertEquals("terminal " + what + "[" + scanner.getText() + "]", terminal, currentTerminal);
        if (text != null) {
            assertEquals("text " + what, text, scanner.getText());
        }
    }

    private XmlScanner create(String[] elements, String input) throws IOException {
        int len;

        len = elements.length;
        return create(elements, new String[len][], new int[len][],
                      new String[len][], input);
    }

    private void createAttr(int defaultMode, String defaultValue, String input) throws IOException {
        create(new String[] { "a" },
               new String[][] {{ "b" }},
               new int[][] {{ defaultMode }},
               new String[][] {{ defaultValue }},
               input);
    }

    private XmlScanner create(
        String[] elements, String[][] attrNames, int[][] defaultMode,
        String[][] defaultValues, String input)
    throws IOException {
        Attribute[][] attrs;
        String[] line;
        int i;
        int j;

        createSymbolTable(elements);
        attrs = new Attribute[elements.length][];
        for (i = 0; i < attrs.length; i++) {
            line = null;
            if (i < attrs.length) {
                line = attrNames[i];
            }
            if (line != null) {
                attrs[i] = new Attribute[line.length];
                for (j = 0; j < line.length; j++) {
                    try {
                        attrs[i][j] = new Attribute(elements[i], line[j],
                                defaultValues[i][j], symbolTable, null,
                                defaultMode[i][j]);
                    } catch (GenericException e) {
                        throw new RuntimeException("invalid attribute" + e);
                    }
                }
            } else {
                attrs[i] = new Attribute[0];
            }
        }
        scanner = new XmlScanner(symbolTable, attrs);
        scanner.open(new Position(new URL(dataDir, "virtual.xml")), new StringReader(input));
        return scanner;
    }

    private void createSymbolTable(String[] elements) {
        String element;
        int i;

        symbolTable = new StringArrayList();
        for (i = 0; i < elements.length; i++) {
            element = elements[i];
            symbolTable.add(element);
            symbolTable.add(XmlSyntax.toStartTag(element));
            symbolTable.add(XmlSyntax.toEndTag(element));
        }
    }
}
