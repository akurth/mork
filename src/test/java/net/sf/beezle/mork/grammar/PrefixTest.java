package net.sf.beezle.mork.grammar;


import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PrefixTest {
    @Test
    public void empty() {
        Prefix p;
        
        p = Prefix.EMPTY;
        assertEquals(0, p.size());
    }
    
    @Test
    public void symbol() {
        Prefix p;
        
        p = Prefix.forSymbol(0);
        assertEquals(1, p.size());
        assertEquals(0, p.first());
        assertEquals(0, p.follows(0).length);
        assertEquals(" 0", p.toString());
    }

    @Test
    public void twoSymbols() {
        Prefix p;

        p = Prefix.forSymbol(1).concat(Prefix.forSymbol(2), 2);
        assertEquals(2, p.size());
        assertEquals(1, p.first());
        assertTrue(Arrays.equals(new int[]{2}, p.follows(1)));
        assertEquals(" 1 2", p.toString());
    }

    @Test
    public void concat() {
        Prefix left;
        Prefix right;
        Prefix test;
        
        left = Prefix.forSymbol(10).concat(Prefix.forSymbol(11), 2);
        right = Prefix.forSymbol(20).concat(Prefix.forSymbol(21), 2);
        test = left.concat(right, 2);
        assertEquals(left, test);
        test = left.concat(right, 3);
        assertEquals(" 10 11 20", test.toString());
        test = left.concat(right, 4);
        assertEquals(" 10 11 20 21", test.toString());
        test = left.concat(right, 5);
        assertEquals(" 10 11 20 21", test.toString());
    }
}
