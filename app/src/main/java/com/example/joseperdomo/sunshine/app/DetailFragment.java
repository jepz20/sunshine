package com.example.joseperdomo.sunshine.app;

/**
 * Created by jose.perdomo on 19/08/2014.
 */

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.joseperdomo.sunshine.app.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    private static final String LOCATION_KEY = "94043";

    private static final int DETAIL_LOADER = 0;
    private ImageView mIconView;
    private TextView mDateView;
    private TextView mDayView;
    private TextView mDescriptionView;
    private TextView mHighTemperatureView;
    private TextView mLowTemperatureView;
    private TextView mHumidityView;
    private TextView mWindView;
    private TextView mPressureView;

    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATETEXT,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID

    };

    public static final String DATE_KEY = "forecast_date";

    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
    private String mForecastStr;
    private String mLocation;
    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mIconView = (ImageView) rootView.findViewById(R.id.detail_icon_imageview);
        mDateView = (TextView) rootView.findViewById(R.id.detail_date_textview);
        mDayView = (TextView) rootView.findViewById(R.id.detail_day_textview);
        mDescriptionView = (TextView) rootView.findViewById(R.id.detail_desc_textview);
        mHighTemperatureView = (TextView) rootView.findViewById(R.id.detail_max_temp_textview);
        mLowTemperatureView = (TextView) rootView.findViewById(R.id.detail_min_temp_textview);
        mHumidityView = (TextView) rootView.findViewById(R.id.detail_humidty_textview);
        mWindView = (TextView) rootView.findViewById(R.id.detail_wind_textview);
        mPressureView = (TextView) rootView.findViewById(R.id.detail_pressure_textview);
        return rootView;
    }

    public Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                mForecastStr + FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detailfragment, menu);
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuItem menuItem = menu.findItem(R.id.action_share);
        ShareActionProvider mShareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        } else {
            Log.d(LOG_TAG, "Share action provider is null?");
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mLocation = savedInstanceState.getString(LOCATION_KEY);
        }
        Bundle args = getArguments();

        if (args != null && args.containsKey(DATE_KEY)) {
            getLoaderManager().initLoader(DETAIL_LOADER,null,this);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(LOCATION_KEY,mLocation);
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String dateStr = getArguments().getString(DATE_KEY);
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATETEXT + " ASC";
        mLocation = Utility.getPreferredLocation(getActivity());
        Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                mLocation, dateStr);

        return new CursorLoader(
                getActivity(),
                weatherUri, //Uri
                FORECAST_COLUMNS, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                sortOrder // sort order);
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) { return; }
        boolean isMetric = Utility.isMetric(getActivity());
        mIconView.setImageResource(Utility.getArtResourceForWeatherCondition(data.getInt(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID))));
        mDateView.setText(Utility.getFormattedMonthDay(getActivity(), data.getString(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATETEXT))));
        mDayView.setText(Utility.getDayName(getActivity(),data.getString(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATETEXT))));
        mDescriptionView.setText(data.getString(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC)));
        mHighTemperatureView.setText(Utility.formatTemperature(getActivity(), data.getDouble(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP)),isMetric));
        mLowTemperatureView.setText(Utility.formatTemperature(getActivity(), data.getDouble(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP)),isMetric));
        //Wind
        String windStr = Utility.getFormattedWind(getActivity(),data.getFloat(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED)),data.getFloat(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DEGREES)));
        mWindView.setText(windStr);

        String humidityStr = getActivity().getString(R.string.format_humidity, data.getDouble(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_HUMIDITY)));
        mHumidityView.setText(humidityStr);

        String pressureStr = getActivity().getString(R.string.format_pressure, data.getDouble(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_PRESSURE)));
        mPressureView.setText(pressureStr);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        Bundle args = getArguments();
        if (args != null  && args.containsKey(DATE_KEY) &&
            mLocation != null && !mLocation.equals(Utility.getPreferredLocation(getActivity()))) {
            getLoaderManager().restartLoader(DETAIL_LOADER,null,this);
        }
    }
}