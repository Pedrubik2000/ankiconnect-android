package com.kamwithk.ankiconnectandroid.request_parsers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.gson.JsonParser;

import org.junit.Test;

public class NoteRequestTest {
    /** Hoshi Reader's duplicate check: no tags (it used to throw, so Hoshi never saw a duplicate). */
    @Test
    public void noteWithoutTags() {
        NoteRequest note = NoteRequest.fromJson(JsonParser.parseString(
                "{\"deckName\":\"Mined\",\"modelName\":\"Lapis\",\"fields\":{\"Expression\":\"猫\"},"
                        + "\"options\":{\"allowDuplicate\":false,\"duplicateScope\":\"collection\"}}"));
        assertEquals("猫", note.getFieldValue());
        assertTrue(note.getTags().isEmpty());
    }

    /** Hoshi's "deck root" scope (no checkAllModels) and "check all note types" alone (no deckName). */
    @Test
    public void partialScopeOptions() {
        NoteRequest root = NoteRequest.fromJson(JsonParser.parseString(
                "{\"deckName\":\"Mined::A\",\"modelName\":\"Lapis\",\"fields\":{\"Expression\":\"猫\"},\"options\":{\"allowDuplicate\":false,"
                        + "\"duplicateScope\":\"deck\",\"duplicateScopeOptions\":{\"deckName\":\"Mined\",\"checkChildren\":true}}}"));
        assertEquals("Mined", root.getOptions().getDeckName());
        assertTrue(root.getOptions().isCheckChildren());
        NoteRequest all = NoteRequest.fromJson(JsonParser.parseString(
                "{\"deckName\":\"Mined\",\"modelName\":\"Lapis\",\"fields\":{\"Expression\":\"猫\"},\"options\":{\"allowDuplicate\":false,"
                        + "\"duplicateScope\":\"collection\",\"duplicateScopeOptions\":{\"checkAllModels\":true}}}"));
        assertTrue(all.getOptions().isCheckAllModels());
    }

    @Test
    public void noteWithTags() {
        NoteRequest note = NoteRequest.fromJson(JsonParser.parseString(
                "{\"deckName\":\"Mined\",\"modelName\":\"Lapis\",\"fields\":{\"Expression\":\"猫\"},\"tags\":[\"a\",\"b\"]}"));
        assertEquals(2, note.getTags().size());
    }
}
