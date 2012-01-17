package net.sf.beezle.mork.grammar;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PrefixSetTest {
    @Test
    public void grow() {
        PrefixSet set;

        set = new PrefixSet();
        for (int i = 0; i < 40; i++) {
            assertTrue(set.addUnpacked(i));
        }
        for (int i = 0; i < 40; i++) {
            assertFalse("" + i, set.addUnpacked(i));
        }
    }
}
