package com.owsega.citydirectory.provider;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.owsega.citydirectory.model.CitiesTrie;
import com.owsega.citydirectory.model.City;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Utils for loading data
 */
public class LoadDataUtils {

    /**
     * total time: 116471   (1.94118333 minutes)
     */
    static List<City> getCities(InputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        long begin = System.currentTimeMillis();
        Type type = new TypeToken<List<City>>() {
        }.getType();
        List<City> cities = new Gson().fromJson(reader, type);
        long diff = System.currentTimeMillis() - begin;
        Log.e("seyi", "time to parse json " + diff);
        reader.close();
        return cities;
    }

    static CitiesTrie passIntoTrie(List<City> cities) {
        CitiesTrie trie = new CitiesTrie();

        long begin = System.currentTimeMillis();
        for (City city : cities) {
            trie.add(city);
        }
        long diff = System.currentTimeMillis() - begin;
        Log.e("seyi", "time to build trie " + diff);
        return trie;
    }

    /**
     * total time: 116471   (1.94118333 minutes)
     */
    static List<City> readJsonWhole(InputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        long begin = System.currentTimeMillis();
        Type type = new TypeToken<List<City>>() {
        }.getType();
        List<City> cities = new Gson().fromJson(reader, type);
        long diff = System.currentTimeMillis() - begin;
        Log.e("seyi", "total time " + diff);
        reader.close();
        return cities;
    }

    /**
     * cities size: 209557, total time 1005445  (16.75741667 minutes)
     */
    static List<City> readJsonStream(InputStream in) throws IOException {
        List<City> cities = new ArrayList<>();
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        reader.beginArray();
        long begin = System.currentTimeMillis();
        while (reader.hasNext()) {
            City city = new Gson().fromJson(reader, City.class);
            cities.add(city);
            Log.e("seyi", "cities size: " + cities.size());
        }
        long diff = System.currentTimeMillis() - begin;
        Log.e("seyi", "total time " + diff);
        reader.endArray();
        reader.close();
        return cities;
    }
}
