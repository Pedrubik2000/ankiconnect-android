package com.kamwithk.ankiconnectandroid.ankidroid_api;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.ichi2.anki.FlashCardsContract;
import com.ichi2.anki.api.AddContentApi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/** kuma3: findCards and cardsInfo, through the provider's card search. */
public class CardAPI {
    private static final int BATCH_SIZE = 500;
    private static final String[] CARD_ID_PROJECTION = {FlashCardsContract.Card._ID};
    private static final String[] CARD_INFO_PROJECTION = {
            FlashCardsContract.Card._ID,
            FlashCardsContract.Card.NOTE_ID,
            FlashCardsContract.Card.CARD_ORD,
            FlashCardsContract.Card.DECK_ID,
            FlashCardsContract.Card.TYPE,
            FlashCardsContract.Card.RAW_QUEUE,
            FlashCardsContract.Card.RAW_DUE,
            FlashCardsContract.Card.INTERVAL,
            FlashCardsContract.Card.RAW_SM2_FACTOR,
            FlashCardsContract.Card.REPS,
            FlashCardsContract.Card.LAPSES,
            FlashCardsContract.Card.RAW_LEFT,
    };

    private final ContentResolver resolver;
    private final AddContentApi api;
    private final NoteAPI noteAPI;

    public CardAPI(Context context, NoteAPI noteAPI) {
        this.resolver = context.getContentResolver();
        this.api = new AddContentApi(context);
        this.noteAPI = noteAPI;
    }

    public ArrayList<Long> findCards(String query) {
        ArrayList<Long> cardIds = new ArrayList<>();
        try (Cursor cursor = resolver.query(FlashCardsContract.Card.CONTENT_URI, CARD_ID_PROJECTION, query, null, null)) {
            while (cursor != null && cursor.moveToNext()) {
                cardIds.add(cursor.getLong(0));
            }
        }
        return cardIds;
    }

    /** One entry per card that still exists, in the requested order. */
    public List<CardInfo> cardsInfo(List<Long> cardIds) throws Exception {
        Map<Long, CardInfo> found = new HashMap<>();
        for (List<Long> batch : batches(cardIds, BATCH_SIZE)) {
            readCards(batch, found);
        }
        addNotesAndDecks(found);

        List<CardInfo> cards = new ArrayList<>();
        for (long cardId : cardIds) {
            CardInfo card = found.get(cardId);
            if (card != null && card.fields != null) {
                cards.add(card);
            }
        }
        return cards;
    }

    private void readCards(List<Long> cardIds, Map<Long, CardInfo> found) {
        String query = "cid:" + TextUtils.join(",", cardIds);
        try (Cursor cursor = resolver.query(FlashCardsContract.Card.CONTENT_URI, CARD_INFO_PROJECTION, query, null, null)) {
            while (cursor != null && cursor.moveToNext()) {
                CardInfo card = new CardInfo(cursor);
                found.put(card.cardId, card);
            }
        }
    }

    private void addNotesAndDecks(Map<Long, CardInfo> cards) throws Exception {
        LinkedHashSet<Long> noteIds = new LinkedHashSet<>();
        for (CardInfo card : cards.values()) {
            noteIds.add(card.note);
        }
        Map<Long, NoteAPI.NoteInfo> notes = new HashMap<>();
        for (List<Long> batch : batches(new ArrayList<>(noteIds), BATCH_SIZE)) {
            for (NoteAPI.NoteInfo note : noteAPI.notesInfo(new ArrayList<>(batch))) {
                notes.put(note.getNoteId(), note);
            }
        }

        Map<Long, String> decks = api.getDeckList();
        for (CardInfo card : cards.values()) {
            NoteAPI.NoteInfo note = notes.get(card.note);
            if (note != null) {
                card.fields = note.getFields();
                card.modelName = note.getModelName();
            }
            card.deckName = decks.get(card.deckId);
        }
    }

    static <T> List<List<T>> batches(List<T> items, int size) {
        List<List<T>> batches = new ArrayList<>();
        for (int i = 0; i < items.size(); i += size) {
            batches.add(items.subList(i, Math.min(i + size, items.size())));
        }
        return batches;
    }

    /** Named as AnkiConnect names them; Gson writes these fields as they are. */
    static class CardInfo {
        final long cardId;
        final long note;
        final int ord;
        final int fieldOrder;
        final transient long deckId;
        final int type;
        final int queue;
        final long due;
        final long interval;
        final int factor;
        final int reps;
        final int lapses;
        final int left;
        String deckName;
        String modelName;
        Map<String, NoteAPI.NoteInfoField> fields;

        CardInfo(Cursor cursor) {
            cardId = cursor.getLong(cursor.getColumnIndexOrThrow(FlashCardsContract.Card._ID));
            note = cursor.getLong(cursor.getColumnIndexOrThrow(FlashCardsContract.Card.NOTE_ID));
            ord = cursor.getInt(cursor.getColumnIndexOrThrow(FlashCardsContract.Card.CARD_ORD));
            fieldOrder = ord;
            deckId = cursor.getLong(cursor.getColumnIndexOrThrow(FlashCardsContract.Card.DECK_ID));
            type = cursor.getInt(cursor.getColumnIndexOrThrow(FlashCardsContract.Card.TYPE));
            queue = cursor.getInt(cursor.getColumnIndexOrThrow(FlashCardsContract.Card.RAW_QUEUE));
            due = cursor.getLong(cursor.getColumnIndexOrThrow(FlashCardsContract.Card.RAW_DUE));
            interval = cursor.getLong(cursor.getColumnIndexOrThrow(FlashCardsContract.Card.INTERVAL));
            factor = cursor.getInt(cursor.getColumnIndexOrThrow(FlashCardsContract.Card.RAW_SM2_FACTOR));
            reps = cursor.getInt(cursor.getColumnIndexOrThrow(FlashCardsContract.Card.REPS));
            lapses = cursor.getInt(cursor.getColumnIndexOrThrow(FlashCardsContract.Card.LAPSES));
            left = cursor.getInt(cursor.getColumnIndexOrThrow(FlashCardsContract.Card.RAW_LEFT));
        }
    }
}
