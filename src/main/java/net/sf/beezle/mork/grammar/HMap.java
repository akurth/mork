package net.sf.beezle.mork.grammar;

import java.util.Iterator;
import java.util.NoSuchElementException;


public class HMap {
    /**
     * The default initial capacity - MUST be a power of two.
     */
    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    /**
     * The maximum capacity, used if a higher value is implicitly specified
     * by either of the constructors with arguments.
     * MUST be a power of two <= 1<<30.
     */
    private static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * The load factor used when none specified in constructor.
     */
    private static final float LOAD_FACTOR = 0.75f;

    /**
     * The table, resized as necessary. Length MUST Always be a power of two.
     */
    private transient Entry[] table;

    /**
     * The number of key-value mappings contained in this map.
     */
    private transient int size;

    /**
     * The next size value at which to resize (capacity * load factor).
     * @serial
     */
    private int threshold;

    public HMap() {
        threshold = (int)(DEFAULT_INITIAL_CAPACITY * LOAD_FACTOR);
        table = new Entry[DEFAULT_INITIAL_CAPACITY];
    }

    public HMap(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);
        }
        if (initialCapacity > MAXIMUM_CAPACITY) {
            throw new IllegalArgumentException();
        }

        // Find a power of 2 >= initialCapacity
        int capacity = 1;
        while (capacity < initialCapacity)
            capacity <<= 1;

        threshold = (int)(capacity * LOAD_FACTOR);
        table = new Entry[capacity];
    }

    // internal utilities

    /**
     * Applies a supplemental hash function to a given hashCode, which
     * defends against poor quality hash functions.  This is critical
     * because HashMap uses power-of-two length hash tables, that
     * otherwise encounter collisions for hashCodes that do not differ
     * in lower bits. Note: Null keys always map to hash 0, thus index 0.
     */
    static int hash(int h) {
        // This function ensures that hashCodes that differ only by
        // constant multiples at each bit position have a bounded
        // number of collisions (approximately 8 at default load factor).
        h ^= (h >>> 20) ^ (h >>> 12);
        return h ^ (h >>> 7) ^ (h >>> 4);
    }

    /**
     * Returns index for hash code h.
     */
    static int indexFor(int h, int length) {
        return h & (length-1);
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean containsKey(Object key) {
        return getEntry(key) != null;
    }

    private Entry getEntry(Object key) {
        int hash = (key == null) ? 0 : hash(key.hashCode());
        for (Entry e = table[indexFor(hash, table.length)];
             e != null;
             e = e.next) {
            Object k;
            if (e.hash == hash &&
                    ((k = e.key) == key || (key != null && key.equals(k))))
                return e;
        }
        return null;
    }


    public boolean put(Prefix key) {
        int hash = hash(key.hashCode());
        int i = indexFor(hash, table.length);
        for (Entry e = table[i]; e != null; e = e.next) {
            Object k;
            if (e.hash == hash && ((k = e.key) == key || key.equals(k))) {
                return true;
            }
        }
        Entry e = table[i];
        table[i] = new Entry(hash, key, e);
        if (size++ >= threshold)
            resize(2 * table.length);
        return false;
    }

    private void resize(int newCapacity) {
        Entry[] oldTable = table;
        int oldCapacity = oldTable.length;
        if (oldCapacity == MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return;
        }

        Entry[] newTable = new Entry[newCapacity];
        transfer(newTable);
        table = newTable;
        threshold = (int)(newCapacity * LOAD_FACTOR);
    }

    private void transfer(Entry[] newTable) {
        Entry[] src = table;
        int newCapacity = newTable.length;
        for (int j = 0; j < src.length; j++) {
            Entry e = src[j];
            if (e != null) {
                src[j] = null;
                do {
                    Entry next = e.next;
                    int i = indexFor(e.hash, newCapacity);
                    e.next = newTable[i];
                    newTable[i] = e;
                    e = next;
                } while (e != null);
            }
        }
    }

    public boolean remove(Object key) {
        return removeEntryForKey(key) == null;
    }

    /**
     * Removes and returns the entry associated with the specified key
     * in the HashMap.  Returns null if the HashMap contains no mapping
     * for this key.
     */
    private Entry removeEntryForKey(Object key) {
        int hash = (key == null) ? 0 : hash(key.hashCode());
        int i = indexFor(hash, table.length);
        Entry prev = table[i];
        Entry e = prev;

        while (e != null) {
            Entry next = e.next;
            Object k;
            if (e.hash == hash &&
                    ((k = e.key) == key || (key != null && key.equals(k)))) {
                size--;
                if (prev == e)
                    table[i] = next;
                else
                    prev.next = next;
                return e;
            }
            prev = e;
            e = next;
        }

        return e;
    }

    private static class Entry {
        /** never null */
        Prefix key;
        Entry next;
        final int hash;

        Entry(int h, Prefix k, Entry n) {
            next = n;
            key = k;
            hash = h;
        }

        public final Prefix getKey() {
            return key;
        }

        public final boolean equals(Object o) {
            if (!(o instanceof Entry))
                return false;
            Entry e = (Entry) o;
            Object k1 = getKey();
            Object k2 = e.getKey();
            return k1 == k2 || (k1 != null && k1.equals(k2));
        }

        public final int hashCode() {
            return key.hashCode();
        }

        public final String toString() {
            return getKey().toString();
        }
    }

    private class KeyIterator implements Iterator<Prefix> {
        Entry next;        // next entry to return
        int index;              // current slot
        Entry current;     // current entry

        public KeyIterator() {
            if (size > 0) { // advance to first entry
                Entry[] t = table;
                while (index < t.length && (next = t[index++]) == null)
                    ;
            }
        }

        public boolean hasNext() {
            return next != null;
        }

        public Prefix next() {
            Entry e = next;
            if (e == null)
                throw new NoSuchElementException();

            if ((next = e.next) == null) {
                Entry[] t = table;
                while (index < t.length && (next = t[index++]) == null)
                    ;
            }
            current = e;
            return e.getKey();
        }

        public void remove() {
            if (current == null)
                throw new IllegalStateException();
            Object k = current.key;
            current = null;
            HMap.this.removeEntryForKey(k);
        }
    }


    // Subclass overrides these to alter behavior of views' iterator() method
    Iterator<Prefix> newKeyIterator()   {
        return new KeyIterator();
    }

    public boolean equals(Object o) {
        if (o == this)
            return true;

        if (!(o instanceof HMap))
            return false;
        HMap m = (HMap) o;
        if (m.size() != size())
            return false;

        Iterator<Prefix> i = newKeyIterator();
        while (i.hasNext()) {
            Prefix key = i.next();
            if (!m.containsKey(key)) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        return size();
    }
}
