package com.kamwithk.ankiconnectandroid.request_parsers;

import androidx.annotation.NonNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class NoteRequest {

    public static class NoteOptions {
        private final boolean allowDuplicate;

        private final String duplicateScope;

        private final String deckName;

        private final boolean checkChildren;

        private final boolean checkAllModels;

        public NoteOptions(boolean allowDuplicate,
                           String duplicateScope,
                           String deckName,
                           boolean checkChildren,
                           boolean checkAllModels) {
            this.allowDuplicate = allowDuplicate;
            this.duplicateScope = duplicateScope;
            this.deckName = deckName;
            this.checkChildren = checkChildren;
            this.checkAllModels = checkAllModels;
        }

        public boolean isAllowDuplicate() {
            return allowDuplicate;
        }

        public String getDuplicateScope() {
            return duplicateScope;
        }

        public String getDeckName() {
            return deckName;
        }

        public boolean isCheckChildren() {
            return checkChildren;
        }

        public boolean isCheckAllModels() {
            return checkAllModels;
        }
    }

    private final String fieldName;
    private final String fieldValue;
    private final String modelName;

    private final String deckName;

    private final List<String> tags;

    private final NoteOptions options;

    public NoteRequest(String fieldName, String fieldValue, String modelName, String deckName, List<String> tags, NoteOptions options) {
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
        this.modelName = modelName;
        this.deckName = deckName;
        this.tags = tags;
        this.options = options;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getFieldValue() {
        return fieldValue;
    }

    public String getModelName() {
        return modelName;
    }

    public String getDeckName() {
        return deckName;
    }

    public List<String> getTags() {
        return tags;
    }

    public NoteOptions getOptions() {
        return options;
    }

    @NonNull
    public static NoteRequest fromJson(JsonElement noteElement) {
        ArrayList<String> tagList = new ArrayList<>();
        JsonObject noteObject = noteElement.getAsJsonObject();

        JsonObject fieldsObject = noteObject.get("fields").getAsJsonObject();
        String field = fieldsObject.keySet().toArray()[0].toString();
        String value = fieldsObject.get(field).getAsString();

        String modelName = noteObject.get("modelName").getAsString();
        String deckName = noteObject.get("deckName").getAsString();

        // Tags are optional, as in AnkiConnect: Hoshi Reader's duplicate check sends none.
        if (noteObject.has("tags") && noteObject.get("tags").isJsonArray()) {
            for (JsonElement tag : noteObject.get("tags").getAsJsonArray()) {
                tagList.add(tag.getAsString());
            }
        }

        NoteOptions options = null;
        if (noteObject.has("options")) {
            options = readNoteOptions(noteObject.get("options").getAsJsonObject());
        }

        return new NoteRequest(field,
                value,
                modelName,
                deckName,
                tagList,
                options);
    }

    @NonNull
    private static NoteOptions readNoteOptions(JsonObject optionsObject) {
        boolean allowDuplicate = false;
        String duplicateScope = null;
        String duplicateScopeDeckName = null;
        boolean duplicateScopeCheckChildren = false;
        boolean duplicateScopeCheckAllModels = false;


        // Every key is optional, with AnkiConnect's defaults (Hoshi Reader leaves some out).
        if (optionsObject.has("allowDuplicate")) {
            allowDuplicate = optionsObject.get("allowDuplicate").getAsBoolean();
        }
        duplicateScope = optionsObject.has("duplicateScope") ? optionsObject.get("duplicateScope").getAsString() : "collection";
        if (optionsObject.has("duplicateScopeOptions")) {
            JsonObject duplicateScopeObject = optionsObject.get("duplicateScopeOptions").getAsJsonObject();

            if (duplicateScopeObject.has("deckName")) {
                JsonElement duplicateDeckName =  duplicateScopeObject.get("deckName");
                if(!duplicateDeckName.isJsonNull()) {
                    duplicateScopeDeckName = duplicateDeckName.getAsString();
                }
            }
            if (duplicateScopeObject.has("checkChildren")) {
                duplicateScopeCheckChildren = duplicateScopeObject.get("checkChildren").getAsBoolean();
            }
            if (duplicateScopeObject.has("checkAllModels")) {
                duplicateScopeCheckAllModels = duplicateScopeObject.get("checkAllModels").getAsBoolean();
            }
        }

        return new NoteOptions(allowDuplicate,
                duplicateScope,
                duplicateScopeDeckName,
                duplicateScopeCheckChildren,
                duplicateScopeCheckAllModels);
    }
}
