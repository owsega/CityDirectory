package com.owsega.citydirectory.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * setup list UI
 * import json to assets
 * read json to memory at startup
 implement core logic for search
parse json into a trie
test trie search
implement map UI
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DummyContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<City> ITEMS = new ArrayList<>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, City> ITEM_MAP = new HashMap<>();

    private static final int COUNT = 25;

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createCity(i));
        }
    }

    private static void addItem(City item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.toString(), item);
    }

    private static City createCity(int position) {
        City city = new City();
        city.name = "Lagos" + position;
        city.country = "NG" + position;
        city._id = 1111 + position;
        return city;
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }
}
