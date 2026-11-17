package com.kamwithk.ankiconnectandroid.ankidroid_api;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class StoredNamesTest {
    @Test
    public void pointsSoundAndImageAtStoredNames() {
        Map<String, String> fields = new HashMap<>();
        fields.put("Audio", "[sound:hoshi_a.mp3]");
        fields.put("Picture", "<img src=\"hoshi_b.png\">");
        fields.put("Word", "hoshi_a.mp3");
        Map<String, String> names = new HashMap<>();
        names.put("hoshi_a.mp3", "hoshi_a_123.mp3");
        names.put("hoshi_b.png", "hoshi_b_456.png");

        IntegratedAPI.useStoredNames(fields, names);

        assertEquals("[sound:hoshi_a_123.mp3]", fields.get("Audio"));
        assertEquals("<img src=\"hoshi_b_456.png\">", fields.get("Picture"));
        assertEquals("hoshi_a.mp3", fields.get("Word"));
    }
}
