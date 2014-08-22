/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.joseperdomo.sunshine.app.test;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

import com.example.joseperdomo.sunshine.app.data.WeatherContract.LocationEntry;
import com.example.joseperdomo.sunshine.app.data.WeatherContract.WeatherEntry;

public class TestProvider extends AndroidTestCase {

    public static final String LOG_TAG = TestProvider.class.getSimpleName();
    public static String TEST_CITY_NAME = "North Pole";
    public static String TEST_LOCATION = "99705";
    public static String TEST_DATE = "20141205";
    // brings our database to an empty state
    public void deleteAllRecords() {
        mContext.getContentResolver().delete(
                WeatherEntry.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                LocationEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                WeatherEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                LocationEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();
    }

    // Since we want each test to start with a clean slate, run deleteAllRecords
    // in setUp (called by the test runner before each test).
    public void setUp() {
        deleteAllRecords();
    }
    public void testDeleteDb() throws Throwable {
        setUp();
    }

    public void testInsertReadProvider() {


        ContentValues testValues = TestDb.createNorthPoleLocationValues();

        long locationRowId;

        Uri locationInsertUri= mContext.getContentResolver().insert(LocationEntry.CONTENT_URI, testValues);

        locationRowId = ContentUris.parseId(locationInsertUri);
        // Verify we got a row back.
        Log.d(LOG_TAG, "New row id: " + locationRowId);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor cursor =  mContext.getContentResolver().query(LocationEntry.CONTENT_URI,
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null // sort order
        );

        TestDb.validateCursor(cursor, testValues);

        // A cursor is your primary interface to the query results.
        cursor =  mContext.getContentResolver().query(LocationEntry.buildLocationUri(locationRowId),
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null // sort order
        );
        TestDb.validateCursor(cursor, testValues);

        // Fantastic.  Now that we have a location, add some weather!
        ContentValues weatherValues = TestDb.createWeatherValues(locationRowId);

        long weatherRowId;
        Uri weatherInsertUri = mContext.getContentResolver().insert(WeatherEntry.CONTENT_URI, weatherValues);

        weatherRowId = ContentUris.parseId(weatherInsertUri);

        // A cursor is your primary interface to the query results.
        Cursor weatherCursor =  mContext.getContentResolver().query(WeatherEntry.CONTENT_URI,
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null // sort order
        );

        TestDb.validateCursor(weatherCursor, weatherValues);
        weatherCursor.close();

        weatherCursor =  mContext.getContentResolver().query(WeatherEntry.buildWeatherLocation(TEST_LOCATION),
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null // sort order
        );
        TestDb.validateCursor(weatherCursor, weatherValues);
        weatherCursor.close();

        weatherCursor =  mContext.getContentResolver().query(WeatherEntry.buildWeatherLocationWithStartDate(TEST_LOCATION,TEST_DATE),
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null // sort order
        );
        TestDb.validateCursor(weatherCursor, weatherValues);

        weatherCursor.close();

        weatherCursor =  mContext.getContentResolver().query(WeatherEntry.buildWeatherLocationWithDate(TEST_LOCATION,TEST_DATE),
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null // sort order
        );
        TestDb.validateCursor(weatherCursor, weatherValues);

    }

    public void testGetType() {
        // content://com.example.android.sunshine.app/weather/
        String type = mContext.getContentResolver().getType(WeatherEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
        assertEquals(WeatherEntry.CONTENT_TYPE, type);

        String testLocation = TEST_LOCATION;
        // content://com.example.android.sunshine.app/weather/94074
        type = mContext.getContentResolver().getType(
                WeatherEntry.buildWeatherLocation(testLocation));
        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
        assertEquals(WeatherEntry.CONTENT_TYPE, type);

        String testDate = TEST_DATE;
        // content://com.example.android.sunshine.app/weather/94074/20140612
        type = mContext.getContentResolver().getType(
                WeatherEntry.buildWeatherLocationWithDate(testLocation, testDate));
        // vnd.android.cursor.item/com.example.android.sunshine.app/weather
        assertEquals(WeatherEntry.CONTENT_ITEM_TYPE, type);

        // content://com.example.android.sunshine.app/location/
        type = mContext.getContentResolver().getType(LocationEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.sunshine.app/location
        assertEquals(LocationEntry.CONTENT_TYPE, type);

        // content://com.example.android.sunshine.app/location/1
        type = mContext.getContentResolver().getType(LocationEntry.buildLocationUri(1L));
        // vnd.android.cursor.item/com.example.android.sunshine.app/location
        assertEquals(LocationEntry.CONTENT_ITEM_TYPE, type);
    }
    public void testUpdateLocation() {
        // Create a new map of values, where column names are the keys
        ContentValues values = TestDb.createNorthPoleLocationValues();

        Uri locationUri = mContext.getContentResolver().
                insert(LocationEntry.CONTENT_URI, values);
        long locationRowId = ContentUris.parseId(locationUri);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New row id: " + locationRowId);

        ContentValues updatedValues = new ContentValues(values);
        updatedValues.put(LocationEntry._ID, locationRowId);
        updatedValues.put(LocationEntry.COLUMN_CITY_NAME, "Santa's Village");

        int count = mContext.getContentResolver().update(
                LocationEntry.CONTENT_URI, updatedValues, LocationEntry._ID + "= ?",
                new String[] { Long.toString(locationRowId)});

        assertEquals(count, 1);

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                LocationEntry.buildLocationUri(locationRowId),
                null,
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null // sort order
        );

        TestDb.validateCursor(cursor, updatedValues);
    }

    // Make sure we can still delete after adding/updating stuff
    public void testDeleteRecordsAtEnd() {
        deleteAllRecords();
    }
}