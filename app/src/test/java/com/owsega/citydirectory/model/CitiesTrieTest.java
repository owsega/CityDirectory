package com.owsega.citydirectory.model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class CitiesTrieTest {

    private String citiesJson = "[\n" +
            "{\"country\":\"UA\",\"name\":\"Hurzuf\",\"_id\":707860,\"coord\":{\"lon\":34.283333,\"lat\":44.549999}},\n" +
            "{\"country\":\"RU\",\"name\":\"Hovinki\",\"_id\":519188,\"coord\":{\"lon\":37.666668,\"lat\":55.683334}},\n" +
            "{\"country\":\"NP\",\"name\":\"Gorkhā\",\"_id\":1283378,\"coord\":{\"lon\":84.633331,\"lat\":28}},\n" +
            "{\"country\":\"IN\",\"name\":\"State of Haryāna\",\"_id\":1270260,\"coord\":{\"lon\":76,\"lat\":29}}]";

    private Gson gson;
    private CitiesTrie trie;

    @Before
    public void setUp() {
        gson = new Gson();

        List<City> cities = gson.fromJson(citiesJson, new TypeToken<List<City>>() {
        }.getType());

        trie = new CitiesTrie();
        for (City city : cities) {
            trie.add(city);
        }
    }

    @Test
    public void testElements() {
        Assert.assertEquals(0, trie.find("Novinki"));
        Assert.assertEquals(1, trie.find("Hovinki"));
        Assert.assertEquals(1, trie.find("State of Haryāna, IN"));
        Assert.assertEquals(2, trie.find("h"));
        Assert.assertEquals(4, trie.find(""));
    }

}
