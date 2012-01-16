package net.sf.beezle.mork.grammar;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class PrefixIterator implements Iterator<Prefix> {
    private int index;
    private final long[] table;

    public PrefixIterator(long[] table, int size) {
        this.table = table;
        if (size > 0) {
            index = 0;
            step();
        } else {
            index = table.length;
        }
    }

    public boolean hasNext() {
        return index < table.length;
    }

    public Prefix next() {
        Prefix result;

        if (index >= table.length) {
            throw new NoSuchElementException();
        }
        result = new Prefix(table[index++]);
        step();
        return result;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    private void step() {
        for (; index < table.length; index++) {
            if (table[index] != PrefixSet.FREE) {
                break;
            }
        }
    }
}
