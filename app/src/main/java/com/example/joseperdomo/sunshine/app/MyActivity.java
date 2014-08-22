package com.example.joseperdomo.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class MyActivity extends ActionBarActivity implements ForecastFragment.Callback {
    private final String LOG_TAG = MyActivity.class.getSimpleName();
    public boolean mTwoPane;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG,"onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        if (findViewById(R.id.weather_detail_container) !=null)
        {
            mTwoPane = true;

            if (savedInstanceState == null){
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.weather_detail_container, new DetailFragment())
                        .commit();
            }
        } else {
            mTwoPane = false;
        }

        ForecastFragment forecastFragment = ((ForecastFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_forecast));
        forecastFragment.setUseTodayLayout(!mTwoPane);
    }

    @Override
    public void onItemSelected(String date) {
        if (mTwoPane){
            Bundle args = new Bundle();
            args.putString(DetailFragment.DATE_KEY,date);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.weather_detail_container,fragment)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class)
                    .putExtra(DetailFragment.DATE_KEY,date);
            startActivity(intent);
        }
    }

    @Override
    protected void onStart() {
        Log.d(LOG_TAG,"onStart");
        super.onStart();
    }

    @Override
    protected void onPause() {
        Log.d(LOG_TAG,"onPause");
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.d(LOG_TAG,"onResume");
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        Log.d(LOG_TAG,"onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        Log.d(LOG_TAG,"onStop");
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent viewSettings = new Intent(this, SettingsActivity.class);
            startActivity(viewSettings);
            return true;
        }
        if (id == R.id.action_map) {
            openPreferredLocationInMap();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openPreferredLocationInMap () {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String location = pref.getString(getString(R.string.pref_location_key),
                getString(R.string.pref_location_default));
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri loc = Uri.parse("geo:0,0?").buildUpon()
                .appendQueryParameter("q", location)
                .build();
        intent.setData(loc);
        if (intent.resolveActivity(getPackageManager())!= null) {
            startActivity(intent);
        } else {
            Log.d(LOG_TAG, "Couldn't call " + location + ":(" );
        }

    }
}
