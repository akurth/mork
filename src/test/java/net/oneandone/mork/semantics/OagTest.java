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
package net.oneandone.mork.semantics;

import junit.framework.TestCase;
import net.oneandone.mork.grammar.Grammar;
import net.oneandone.mork.misc.GenericException;
import net.oneandone.mork.reflect.Identity;
import net.oneandone.sushi.util.Separator;
import net.oneandone.sushi.util.Strings;

import java.util.ArrayList;
import java.util.List;

public class OagTest extends TestCase {
    public void testSimple() throws GenericException {
        run(createSimple());
    }

    public void testComplex() throws GenericException {
        run(createComplex());
    }

    public void testCyclic() {
        try {
            run(createCyclic());
            fail();
        } catch (GenericException e) {
            // ok
        }
    }

    public void run(Ag sems) throws GenericException {
        int i;
        Visits[] visits;

        System.out.println(sems.getGrammar().getSymbolTable().toString());
        visits = OagBuilder.run(sems, null, System.out);
        for (i = 0; i < visits.length; i++) {
            System.out.println(" " + i + "\t" + visits[i]);
        }
    }

    private static Ag createCyclic() {
        return forRules(new String[] {
            "a"
                + ">a.value a.value"
        });
    }

    private static Ag createSimple() {
        return forRules(new String[] {
                    "a left right"
                        + ">right.value left.value"
                        + ">a.result right.result",
                    "left"
                        + ">left.value",
                    "right last"
                        + ">last.value right.value"
                        + ">right.result last.result",
                    "last"
                        + ">last.result last.value"
                });
    }

    private static Ag createComplex() {
        return forRules(new String[] {
                "program primary"
                    + ">primary.access"
                    + ">primary.postmode primary.primode",
                "primary ( declaration ; assignment )"
                    + ">declaration.access primary.access"
                    + ">assignment.access primary.access declaration.description"
                    + ">primary.primode assignment.primode"
                    + ">assignment.postmode primary.postmode"
                    + ">primary.evaluable"
                    + ">primary.value",
                "primary identifier"
                    + ">primary.primode identifier.id primary.access"
                    + ">primary.evaluable"
                    + ">primary.value",
                "primary intconstant"
                    + ">primary.primode"
                    + ">primary.evaluable"
                    + ">primary.value primary.postmode intconstant.value",
                "primary realconstant"
                    + ">primary.primode"
                    + ">primary.evaluable"
                    + ">primary.value realconstant.value",
                "assignment identifier := expression"
                    + ">expression.access assignment.access"
                    + ">assignment.primode identifier.id assignment.access"
                    + ">expression.postmode assignment.primode",
                "expression expression + primary"
                    + ">expression#2.access expression.access"
                    + ">primary.access expression.access"
                    + ">expression.primode expression#2.primode primary.primode"
                    + ">expression#2.postmode expression.primode"
                    + ">primary.postmode expression.primode"
                    + ">expression.evaluable expression#2.evaluable primary.evaluable"
                    + ">expression.value expression.evaluable expression#2.value primary.value",
                "expression primary"
                    + ">primary.access expression.access"
                    + ">expression.primode primary.primode"
                    + ">primary.postmode expression.postmode"
                    + ">expression.evaluable primary.evaluable"
                    + ">expression.value primary.value",
                "declaration new identifier := expression"
                    + ">expression.access declaration.access"
                    + ">declaration.description identifier.id expression.primode"
                    + ">expression.postmode expression.primode",
                "identifier := FOO"
                    + ">identifier.id",
                "intconstant := FOO"
                    + ">intconstant.value",
                "realconstant := FOO"
                    + ">realconstant.value"
                });
    }

    private static final Separator SEPARATOR = Separator.on('>');

    public static Ag forRules(String[] rules) {
        int r;
        String[] prods;
        Ag sems;
        String[] tmp;
        String[][][] allCalls;
        Grammar grammar;
        List<Attribute> attrs;

        prods = new String[rules.length];
        allCalls = new String[rules.length][][];
        for (r = 0; r < rules.length; r++) {
            tmp = Strings.toArray(SEPARATOR.split(rules[r]));
            prods[r] = tmp[0];
            allCalls[r] = getCalls(tmp);
        }
        grammar = Grammar.forProductions(prods);
        attrs = declareAttributes(grammar, allCalls);
        sems = new Ag(grammar);
        for (r = 0; r < rules.length; r++) {
            addCalls(sems, attrs, grammar, r, allCalls[r]);
        }
        return sems;
    }

    public static void addCalls(Ag sems, List<Attribute> attrs, Grammar grm, int prod, String[][] prodCalls) {
        int i;
        int j;
        AttributionBuffer ab;

        for (i = 0; i < prodCalls.length; i++) {
            ab = new AttributionBuffer(prod, new Identity("foo", Object.class), getOccurrence(attrs, grm, prodCalls[i][0], prod));
            for (j = 1; j < prodCalls[i].length; j++) {
                ab.add(getOccurrence(attrs, grm, prodCalls[i][j], prod));
            }
            sems.add(ab);
        }
    }

    public static String[][] getCalls(String[] tmp) {
        int i;
        String[][] prodCalls;

        prodCalls = new String[tmp.length - 1][];
        for (i = 0; i < prodCalls.length; i++) {
            prodCalls[i] = Strings.toArray(Separator.SPACE.split(tmp[i + 1]));
        }
        return prodCalls;
    }

    public static List<Attribute> declareAttributes(Grammar grm, String[][][] allCalls) {
        int p;
        int i;
        List<Attribute> attrs;
        Object[] tmp;
        int symbol;
        String name;

        tmp = new Object[3];
        attrs = new ArrayList<Attribute>();
        for (p = 0; p < allCalls.length; p++) {
            for (i = 0; i < allCalls[p].length; i++) {
                getRawOccurrence(grm, allCalls[p][i][0], p, tmp);
                symbol = (Integer) tmp[0];
                name = (String) tmp[2];
                if (Attribute.find(attrs, symbol, name) == null) {
                    attrs.add(new Attribute(symbol, name));
                }
            }
        }
        return attrs;
    }

    public static AttributeOccurrence getOccurrence(List<Attribute> attrs, Grammar grm, String occurrence, int prod) {
        Object[] tmp;
        Attribute attr;

        tmp = new Object[3];
        getRawOccurrence(grm, occurrence, prod, tmp);
        attr = Attribute.find(attrs, (Integer) tmp[0], (String) tmp[2]);
        if (attr == null) {
            throw new IllegalArgumentException("undefined attribute occurrence: " + occurrence);
        }
        return new AttributeOccurrence(attr, (Integer) tmp[1]);
    }

    /**
     * @param result symbol, occurrence, attrName
     */
    public static void getRawOccurrence(Grammar grm, String occurrence, int prod, Object[] result) {
        int dot;
        String symbolName;
        int hash;
        int no;
        int symbol;
        int occ;

        dot = occurrence.indexOf('.');
        if (dot == -1) {
            throw new IllegalArgumentException("missing attribute name in attribute occurrence: "
                                                   + occurrence);
        }
        symbolName = occurrence.substring(0, dot);
        hash = symbolName.indexOf('#');
        if (hash == -1) {
            no = 1;
        } else {
            try {
                no = Integer.parseInt(symbolName.substring(hash + 1));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("invalid symbol occurrence: " + symbolName);
            }
            symbolName = symbolName.substring(0, hash);
        }
        symbol = grm.getSymbolTable().indexOf(symbolName);
        if (symbol == -1) {
            throw new IllegalArgumentException("undefined symbol: " + symbolName);
        }
        occ = getSymbolOccurrence(grm, prod, symbol, no - 1);
        result[0] = symbol;
        result[1] = occ;
        result[2] = occurrence.substring(dot + 1);
    }

    public static int getSymbolOccurrence(Grammar grm, int prod, int symbol, int occ) {
        int i;
        int max;

        if (grm.getLeft(prod) == symbol) {
            if (occ == 0) {
                return -1;
            }
            occ--;
        }
        max = grm.getLength(prod);
        for (i = 0; i < max; i++) {
            if (grm.getRight(prod, i) == symbol) {
                if (occ == 0) {
                    return i;
                }
                occ--;
            }
        }
        throw new IllegalStateException(grm.getSymbolTable().get(symbol) + "#" + occ + " not found");
    }
}
