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

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

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
    public void testFiltering() {
        ArgumentCaptor<PagedList<City>> captor = forClass(PagedList.class);
        Observer<PagedList<City>> observer = mock(Observer.class);
        viewModel.init(jsonReader, false);
        viewModel.cityList.observeForever(observer);
        verify(observer).onChanged(captor.capture());

        // test
//        viewModel.filterCities("Novinki");
        reset(observer);
        viewModel.filterCities("ab");
        verify(observer).onChanged(captor.capture());
        System.out.println(captor.getValue().size());
        assertThat(TEST_JSON_SIZE, CoreMatchers.equalTo(captor.getValue().size()));
    }

}
