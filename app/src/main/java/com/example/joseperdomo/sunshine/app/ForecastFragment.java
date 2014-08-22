package com.example.joseperdomo.sunshine.app;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.joseperdomo.sunshine.app.data.WeatherContract;
import com.example.joseperdomo.sunshine.app.data.WeatherContract.LocationEntry;
import com.example.joseperdomo.sunshine.app.data.WeatherContract.WeatherEntry;

import java.util.Date;

/**
 * A Forecast fragment containing a simple view.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = ForecastFragment.class.getSimpleName();
    private static final int FORECAST_LOADER = 0;
    private ListView mListview;
    private boolean mUseTodayLayout;
    public static final String POSITION = "position";
    private String mLocation;
    private int mPosition;

    private static final String[] FORECAST_COLUMNS= {
            WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
            WeatherEntry.COLUMN_DATETEXT,
            WeatherEntry.COLUMN_SHORT_DESC,
            WeatherEntry.COLUMN_MAX_TEMP,
            WeatherEntry.COLUMN_MIN_TEMP,
            LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherEntry.COLUMN_WEATHER_ID
    };

    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_LOCATION_SETTING = 5;

    public ForecastAdapter mForecastAdapter;

    public interface Callback {
        public void onItemSelected(String date);
    }

    public ForecastFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(FORECAST_LOADER,null,this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Agrego esto para que el fragmento maneje eventos de menus
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateWeather();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
        if (mForecastAdapter != null)
        {
            mForecastAdapter.setUseTodayLayout(mUseTodayLayout);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        //custom CursorAdapter
        mForecastAdapter = new ForecastAdapter(getActivity(), null, 0);
        mForecastAdapter.setUseTodayLayout(mUseTodayLayout);
        View rootView = inflater.inflate(R.layout.fragment_my, container, false);

        mListview = (ListView) rootView.findViewById(R.id.listview_forecast);
        mListview.setAdapter(mForecastAdapter);

        mListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Cursor cursor = mForecastAdapter.getCursor();
                if (cursor != null && cursor.moveToPosition(position)){
                    ((Callback) getActivity()).onItemSelected(cursor.getString(COL_WEATHER_DATE));
                }
                mPosition = position;

            }
        });
        if (savedInstanceState != null && savedInstanceState.containsKey(POSITION))
        {
            mPosition = savedInstanceState.getInt(POSITION);
        }
        return rootView;
    }

    public void updateWeather() {
        FetchWeatherTask task = new FetchWeatherTask(getActivity());
        String postal = Utility.getPreferredLocation(getActivity());
        //94043
        task.execute(postal);
    }

//    @Override
//    public void onStart(){
//        super.onStart();
//        updateWeather();
//    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.v(LOG_TAG, " onSaveInstanceState la posicion que quiero guardar es: " + mPosition);
        if (mPosition != ListView.INVALID_POSITION){
            outState.putInt(POSITION, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.

        // To only show current and future dates, get the String representation for today,
        // and filter the query to return weather only for dates after or including today.
        // Only return data after today.
        String startDate = WeatherContract.getDbDateString(new Date());

        // Sort order:  Ascending, by date.
        String sortOrder = WeatherEntry.COLUMN_DATETEXT + " ASC";

        mLocation = Utility.getPreferredLocation(getActivity());
        Uri weatherForLocationUri = WeatherEntry.buildWeatherLocationWithStartDate(
                mLocation, startDate);

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(
                getActivity(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mForecastAdapter.swapCursor(data);
        if (mPosition != ListView.INVALID_POSITION) {
            mListview.setSelection(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mLocation != null &&  !mLocation.equals(Utility.getPreferredLocation(getActivity())))
        {
            getLoaderManager().restartLoader(FORECAST_LOADER,null,this);
        }
    }
}