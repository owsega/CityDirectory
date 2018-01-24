package com.owsega.citydirectory.viewmodel;


import com.google.gson.stream.JsonReader;
import com.owsega.citydirectory.model.City;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class CityListViewModelTest {

    private static final String TEST_JSON = "smallcities.json";
    private static final int TEST_JSON_SIZE = 50;

    private CityListViewModel viewModel;
    private JsonReader jsonReader;
    private MockCityListViewModelUpdateListener updateListener;

    @Before
    public void setup() {
        viewModel = new CityListViewModel();
        updateListener = new MockCityListViewModelUpdateListener(viewModel);

        try {
            InputStream in = getClass().getClassLoader().getResourceAsStream(TEST_JSON);
            jsonReader = new JsonReader(new InputStreamReader(in, "UTF-8"));
            viewModel.addUpdateListener(updateListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @After
    public void tearDown() {
        viewModel.removeUpdateListener(updateListener);
    }

    @Test
    public void testDataReady() {
        viewModel.init(jsonReader, false);
        assertThat(updateListener.cityList, notNullValue());
    }

    @Test
    public void testInitialListLoading() {
        viewModel.init(jsonReader, false);
        assertThat(updateListener.cityList, notNullValue());
        assertThat(TEST_JSON_SIZE, equalTo(updateListener.cityList.size()));
    }

    @Test
    public void testFiltering() throws InterruptedException {
        viewModel.init(jsonReader, false);

        // test with full key
        viewModel.filterCities("Novinki");
        assertFalse(updateListener.emptyData);
        assertThat(1, equalTo(updateListener.cityList.size()));

        //   test no matches
        viewModel.filterCities("zzzkyt");
        assertTrue(updateListener.emptyData);

        // test prefix and trimming
        viewModel.filterCities(" g ");
        assertFalse(updateListener.emptyData);
        assertThat(4, equalTo(updateListener.cityList.size()));

        // test empty query
        viewModel.filterCities("");
        assertFalse(updateListener.emptyData);
        assertThat(TEST_JSON_SIZE, equalTo(updateListener.cityList.size()));
    }

    @Test
    public void testOnCityClicked() {
        viewModel.init(jsonReader, false);
        City city = updateListener.cityList.get(0);
        viewModel.onCityClicked(city);
        assertEquals(city, updateListener.selectedCity);
    }

    @Test
    public void testDataInBackground() throws UnsupportedEncodingException {
        updateListener.listenForDataReadyChanges();
        viewModel.init(jsonReader, true);
        assertEquals(0, updateListener.getDataReadyChangeCount());
    }

    @Test
    public void testDataRecreation() throws UnsupportedEncodingException {
        try {
            viewModel = new CityListViewModel();
            updateListener = new MockCityListViewModelUpdateListener(viewModel);
            viewModel.addUpdateListener(updateListener);

            // test dataReady not observed at first init call (because it's in background thread)
            updateListener.listenForDataReadyChanges();
            InputStream in = getClass().getClassLoader().getResourceAsStream(TEST_JSON);
            jsonReader = new JsonReader(new InputStreamReader(in, "UTF-8"));
            viewModel.init(jsonReader, true);
            assertEquals(0, updateListener.getDataReadyChangeCount());

            // test dataReady is observed on second call on the main thread, even though data
            // is loaded on a background thread. This means data is not being reloaded
            // every time init is called, but only the first time
            updateListener.listenForDataReadyChanges();
            in = getClass().getClassLoader().getResourceAsStream(TEST_JSON);
            jsonReader = new JsonReader(new InputStreamReader(in, "UTF-8"));
            viewModel.init(jsonReader, true);
            assertTrue(updateListener.getDataReadyChangeCount() > 0);
        } catch (RuntimeException rte) {
            // may throw exception if data loading is completed before the test is finished
            // this is because Android Looper is not mocked, and executing data loading on
            // the background thread will post to android's main thread at completion.
            rte.printStackTrace();
        }
    }
}
