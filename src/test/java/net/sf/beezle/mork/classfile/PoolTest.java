package net.sf.beezle.mork.classfile;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PoolTest {
    @Test
    public void utf8() {
        check("");
        check("abc");
        check("hello, world!");
        for (char c = 0; c < 512; c++) {
            check("" + c);
            check("123" + c + "456");
        }
        for (char c = 517; c < 20048; c+=47) {
            check("" + c);
            check("xyz" + c + "XYZ");
        }
    }

    private void check(String str) {
        byte[] utf8;

        utf8 = Pool.toUtf8(str);
        assertEquals(str, Pool.fromUtf8(utf8, 3, utf8.length));
    }
}
