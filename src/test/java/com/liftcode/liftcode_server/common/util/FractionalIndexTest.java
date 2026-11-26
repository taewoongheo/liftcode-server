package com.liftcode.liftcode_server.common.util;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class FractionalIndexTest {
    @Test
    void midpointAndPrefixGaps() {
        assertEquals("V0", FractionalIndex.between(null, null));
        assertEquals("a0V", FractionalIndex.between("a0", "a1"));
        String right = "01";
        for (int i = 0; i < 150; i++) {
            String middle = FractionalIndex.between(null, right);
            assertTrue(middle.compareTo(right) < 0);
            right = middle;
        }
    }

    @Test
    void randomInsertionsPreserveStrictOrder() {
        var keys = new ArrayList<String>();
        var random = new Random(283);
        for (int i = 0; i < 4000; i++) {
            int at = random.nextInt(keys.size() + 1);
            String left = at == 0 ? null : keys.get(at - 1);
            String right = at == keys.size() ? null : keys.get(at);
            String key = FractionalIndex.between(left, right);
            assertTrue(left == null || left.compareTo(key) < 0);
            assertTrue(right == null || key.compareTo(right) < 0);
            keys.add(at, key);
        }
        assertThrows(IllegalArgumentException.class, () -> FractionalIndex.between("BB", "BB"));
        assertThrows(IllegalArgumentException.class, () -> FractionalIndex.between(null, "A00"));
    }
}
