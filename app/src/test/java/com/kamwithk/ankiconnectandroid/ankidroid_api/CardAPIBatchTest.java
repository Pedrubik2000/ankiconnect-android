package com.kamwithk.ankiconnectandroid.ankidroid_api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CardAPIBatchTest {
    @Test
    public void splitsWithoutLosingOrReorderingItems() {
        List<List<Integer>> batches = CardAPI.batches(Arrays.asList(1, 2, 3, 4, 5), 2);
        assertEquals(Arrays.asList(Arrays.asList(1, 2), Arrays.asList(3, 4), Collections.singletonList(5)), batches);
    }

    @Test
    public void exactMultipleAndEmptyMakeNoExtraBatch() {
        assertEquals(1, CardAPI.batches(Arrays.asList(1, 2), 2).size());
        assertTrue(CardAPI.batches(Collections.<Integer>emptyList(), 2).isEmpty());
    }
}
