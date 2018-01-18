package com.owsega.citydirectory;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.owsega.citydirectory.model.City;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * AsyncTask for loading data
 */
public class LoadDataTask extends AsyncTask<Void, Void, Void> {

    private InputStream in;

    public LoadDataTask(InputStream inputStream) {
        this.in = inputStream;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            readJsonStream(in);
        } catch (IOException e) {

        }

        return null;
    }

    public List<City> readJsonStream(InputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        List<City> cities = new ArrayList<>();
        reader.beginArray();
        while (reader.hasNext()) {
            City city = new Gson().fromJson(reader, City.class);
            cities.add(city);
            Log.e("seyi", "adding " + city);
        }
        reader.endArray();
        reader.close();
        return cities;
    }
}
