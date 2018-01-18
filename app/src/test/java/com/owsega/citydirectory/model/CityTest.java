package com.owsega.citydirectory.model;

import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class CityTest {

    private Gson gson;
    private String city1String;
    private String city2String;
    private City city1;
    private City city2;

    @Before
    public void setUp() {
        city1String = "{\"country\":\"UA\",\"name\":\"Hurzuf\",\"_id\":707860,\"coord\":{\"lon\":34.283333,\"lat\":44.549999}}";
        city2String = "{\"country\":\"RU\",\"name\":\"Novinki\",\"_id\":519188,\"coord\":{\"lon\":37.666668,\"lat\":55.683334}}";

        gson = new Gson();
        city1 = gson.fromJson(city1String, City.class);
        city2 = gson.fromJson(city2String, City.class);
    }

    @Test
    public void testCreation() {
        assertEquals(707860L, city1._id);
        assertEquals("Hurzuf", city1.name);
        assertEquals("UA", city1.country);
        assertEquals(44.549999F, city1.coord.lat);
        assertEquals(34.283333F, city1.coord.lon);
    }

    @Test
    public void testToString() {
        assertEquals("Hurzuf, UA", city1.toString());
    }

    @Test
    public void testCompare() {
        assertTrue(city1.compareTo(city2) < 0);
        assertTrue(city2.compareTo(city1) > 0);
    }

}
