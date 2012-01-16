package net.sf.beezle.mork.grammar;


import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PrefixTest {
    @Test
    public void empty() {
        PrefixSet s;
        Prefix p;

        s = new PrefixSet();
        s.addEmpty();
        p = s.iterator();
        p.next();
        assertEquals(0, p.size());
    }

    @Test
    public void symbol() {
        Prefix p;

        p = prefix(0);
        assertEquals(1, p.size());
        assertEquals(0, p.first());
        assertEquals(0, p.follows(0).length);
        assertEquals(" 0", p.toString());
    }

    @Test
    public void twoSymbols() {
        Prefix p;

        p = prefix(1, 2);
        assertEquals(2, p.size());
        assertEquals(1, p.first());
        assertTrue(Arrays.equals(new int[]{2}, p.follows(1)));
        assertEquals(" 1 2", p.toString());
    }

    @Test
    public void concat() {
        long left;
        Prefix right;
        long test;

        left = prefix(10, 11).data;
        right = prefix(20, 21, 22);
        test = Prefix.concat(left, right.data, 2);
        assertEquals(left, test);
        test = Prefix.concat(left, right.data, 3);
        assertEquals(" 10 11 20", Prefix.toString(test));
        test = Prefix.concat(left, right.data, 4);
        assertEquals(" 10 11 20 21", Prefix.toString(test));
        test = Prefix.concat(left, right.data, 5);
        assertEquals(" 10 11 20 21 22", Prefix.toString(test));
        test = Prefix.concat(left, right.data, 6);
        assertEquals(" 10 11 20 21 22", Prefix.toString(test));
        test = Prefix.concat(left, right.data, 7);
        assertEquals(" 10 11 20 21 22", Prefix.toString(test));
    }

    private Prefix prefix(int head, int ... tail) {
        PrefixSet set;
        Prefix result;
        PrefixSet tmp;

        set = new PrefixSet();
        set.addSymbol(head);
        result = set.iterator();
        result.next();
        for (int symbol : tail) {
            tmp = new PrefixSet();
            tmp.add(Prefix.concat(result.data, symbol(symbol).data, tail.length + 1));
            result = tmp.iterator();
            result.next();
        }
        return result;
    }

    private Prefix symbol(int symbol) {
        PrefixSet set;
        Prefix prefix;

        set = new PrefixSet();
        set.addSymbol(symbol);
        prefix = set.iterator();
        prefix.next();
        return prefix;
    }
}
