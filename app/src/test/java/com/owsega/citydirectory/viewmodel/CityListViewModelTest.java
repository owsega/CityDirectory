package com.owsega.citydirectory.viewmodel;

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.lifecycle.Observer;
import android.arch.paging.PagedList;

import com.google.gson.stream.JsonReader;
import com.owsega.citydirectory.model.City;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class CityListViewModelTest {

    private static final String TEST_JSON = "smallcities.json";
    private static final int TEST_JSON_SIZE = 50;

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    private CityListViewModel viewModel;
    private JsonReader jsonReader;

    @Before
    public void setup() {
        viewModel = new CityListViewModel();

        try {
            InputStream in = getClass().getClassLoader().getResourceAsStream(TEST_JSON);
            jsonReader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testNullMutableLiveDatas() {
        assertThat(viewModel.emptyData, notNullValue());
        assertThat(viewModel.selectedCity, notNullValue());
        assertThat(viewModel.dataReady, notNullValue());
    }

    @Test
    public void testDataReady() {
        Observer<Boolean> observer = mock(Observer.class);
        viewModel.dataReady.observeForever(observer);
        viewModel.init(jsonReader, false);
        verify(observer).onChanged(true);
        assertThat(viewModel.cityList, notNullValue());
    }

    @Test
    public void testInitialListLoading() {
        ArgumentCaptor<PagedList<City>> captor = forClass(PagedList.class);
        Observer<PagedList<City>> observer = mock(Observer.class);
        viewModel.init(jsonReader, false);
        viewModel.cityList.observeForever(observer);
        verify(observer).onChanged(captor.capture());
        assertThat(TEST_JSON_SIZE, CoreMatchers.equalTo(captor.getValue().size()));
    }

    @Test
    public void testFiltering() throws InterruptedException {
        ArgumentCaptor<PagedList<City>> captor = forClass(PagedList.class);
        Observer<PagedList<City>> listObserver = mock(Observer.class);
        Observer<Boolean> emptyDataObserver = mock(Observer.class);
        viewModel.init(jsonReader, false);
        viewModel.cityList.observeForever(listObserver);
        viewModel.emptyData.observeForever(emptyDataObserver);

        // test with full key
        viewModel.filterCities("Novinki");
        verify(emptyDataObserver, atLeastOnce()).onChanged(false);
        verify(listObserver, atLeastOnce()).onChanged(captor.capture());
        assertThat(1, CoreMatchers.equalTo(captor.getValue().size()));

        //   test no matches
        viewModel.filterCities("zzzkyt");
        verify(emptyDataObserver, atLeastOnce()).onChanged(true);

        // test prefix and trimming
        viewModel.filterCities(" g ");
        verify(emptyDataObserver, atLeastOnce()).onChanged(false);
        verify(listObserver, atLeastOnce()).onChanged(captor.capture());
        assertThat(4, CoreMatchers.equalTo(captor.getValue().size()));

        // test empty query
        viewModel.filterCities("");
        verify(emptyDataObserver, atLeastOnce()).onChanged(false);
        verify(listObserver, atLeastOnce()).onChanged(captor.capture());
        assertThat(TEST_JSON_SIZE, CoreMatchers.equalTo(captor.getValue().size()));
    }

    @Test
    public void testOnCityClicked() {
        ArgumentCaptor<PagedList<City>> captor = forClass(PagedList.class);
        Observer<PagedList<City>> observer = mock(Observer.class);
        viewModel.init(jsonReader, false);
        viewModel.cityList.observeForever(observer);
        verify(observer).onChanged(captor.capture());

        City city = captor.getValue().get(0);
        Observer<City> cityObserver = mock(Observer.class);
        viewModel.selectedCity.observeForever(cityObserver);
        viewModel.onCityClicked(city);
        verify(cityObserver, atLeastOnce()).onChanged(city);
    }

    @Test
    public void testDataInBackground() throws UnsupportedEncodingException {
        Observer<Boolean> observer = mock(Observer.class);
        viewModel.dataReady.observeForever(observer);
        viewModel.init(jsonReader, true);
        verifyNoMoreInteractions(observer);
    }

    @Test
    public void testDataRecreation() throws UnsupportedEncodingException {
        try {
            viewModel = new CityListViewModel();
            Observer<Boolean> observer = mock(Observer.class);
            viewModel.dataReady.observeForever(observer);

            // test dataReady not observed at first init call (because it's in background thread)
            InputStream in = getClass().getClassLoader().getResourceAsStream(TEST_JSON);
            jsonReader = new JsonReader(new InputStreamReader(in, "UTF-8"));
            viewModel.init(jsonReader, true);
            verifyNoMoreInteractions(observer);

            // test dataReady is observed on second call on the main thread, even though data
            // is loaded on a background thread. This means data is not being reloaded
            // every time init is called, but only the first time
            in = getClass().getClassLoader().getResourceAsStream(TEST_JSON);
            jsonReader = new JsonReader(new InputStreamReader(in, "UTF-8"));
            viewModel.init(jsonReader, true);
            verify(observer).onChanged(any());
        } catch (RuntimeException rte) {
            // may throw exception if data loading is completed before the test is finished
            // this is because Android Looper is not mocked, and executing data loading on
            // the background thread will post to android's main thread at completion.
            rte.printStackTrace();
        }
    }
}
