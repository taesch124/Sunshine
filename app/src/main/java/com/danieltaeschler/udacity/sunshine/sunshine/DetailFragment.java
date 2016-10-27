package com.danieltaeschler.udacity.sunshine.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
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
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.CursorLoader;

import com.danieltaeschler.udacity.sunshine.sunshine.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = DetailFragment.class.getSimpleName();
    private static final int WEATHER_DETAIL_LOADER_ID = 0;

    private TextView mWeekdayTextView;
    private TextView mDateTextView;
    private TextView mHighTempTextView;
    private TextView mLowTempTextView;
    private ImageView mIconImageView;
    private TextView mForecastTextView;
    private TextView mHumidityTextView;
    private TextView mWindTextView;
    private TextView mPressureTextView;
    private String mForecastString;
    private ShareActionProvider mShareActionProvider;

    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID
    };

    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_WEATHER_HUMIDITY = 5;
    static final int COL_WEATHER_WIND_SPEED = 6;
    static final int COL_WEATHER_WIND_DIRECTION = 7;
    static final int COL_WEATHER_PRESSURE = 8;
    static final int COL_WEATHER_CONDITION_ID = 9;

    public DetailFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(WEATHER_DETAIL_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        mWeekdayTextView = (TextView)rootView.findViewById(R.id.detail_date_weekday_textview);
        mDateTextView = (TextView)rootView.findViewById(R.id.detail_date_textview);

        mHighTempTextView = (TextView)rootView.findViewById(R.id.detail_highTemp_textview);
        mLowTempTextView = (TextView)rootView.findViewById(R.id.detail_lowTemp_textview);

        mIconImageView = (ImageView)rootView.findViewById(R.id.detail_icon);
        mForecastTextView = (TextView)rootView.findViewById(R.id.detail_forecast_textview);

        mHumidityTextView = (TextView)rootView.findViewById(R.id.detail_humidity_textview);
        mWindTextView = (TextView)rootView.findViewById(R.id.detail_wind_textview);
        mPressureTextView = (TextView)rootView.findViewById(R.id.detail_pressure_textview);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu, inflater);
        getActivity().getMenuInflater().inflate(R.menu.menu_detail, menu);
        MenuItem shareItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);

        if (mShareActionProvider != null && mForecastString != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        } else {
            Log.v(TAG, "Share Provider null.");
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent i = new Intent(getActivity(), SettingsActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private Intent createShareForecastIntent() {
        Log.v(TAG, "Setting share intent.");
        String shareString = mForecastString + " #SunshineApp";
        Intent i = new Intent(Intent.ACTION_SEND);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, shareString);
        return i;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Intent i = getActivity().getIntent();
        if(i == null) {
            return null;
        }

        return new CursorLoader(getActivity(),
                i.getData(),
                FORECAST_COLUMNS,
                null,
                null,
                null);    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) { return; }

        boolean isMetric = Utility.isMetric(getActivity());

        //Enter this for the ShareIntent
        int weatherId = data.getInt(COL_WEATHER_CONDITION_ID);
        String dateString = Utility.formatDate(data.getLong(COL_WEATHER_DATE));
        String weatherDescription = data.getString(COL_WEATHER_DESC);
        String high = Utility.formatTemperature(getActivity(), data.getDouble(COL_WEATHER_MAX_TEMP), isMetric);
        String low = Utility.formatTemperature(getActivity(), data.getDouble(COL_WEATHER_MIN_TEMP), isMetric);
        mForecastString = String.format("%s - %s - %s/%s", dateString, weatherDescription, high, low);

        //Connect data to view items

        //Connect Date
        long date = data.getLong(COL_WEATHER_DATE);
        mWeekdayTextView.setText(Utility.getDayName(getContext(), date));
        mDateTextView.setText(Utility.getFormattedMonthDay(getContext(), date));

        //Connect Temperature
        mHighTempTextView.setText(Utility.formatTemperature(getContext(), data.getDouble(COL_WEATHER_MAX_TEMP), isMetric));
        mLowTempTextView.setText(Utility.formatTemperature(getContext(), data.getDouble(COL_WEATHER_MIN_TEMP), isMetric));

        //Connect weather icon resource
        mIconImageView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));
        //Connect description
        mForecastTextView.setText(data.getString(COL_WEATHER_DESC));

        //Connect Humidity
        mHumidityTextView.setText(String.format(getContext().getString(R.string.format_humidity), data.getFloat(COL_WEATHER_HUMIDITY)));
        //Connect Wind
        mWindTextView.setText(Utility.getFormattedWind(getContext(), data.getFloat(COL_WEATHER_WIND_SPEED), data.getFloat(COL_WEATHER_WIND_DIRECTION)));
        //Connect Pressure
        mPressureTextView.setText(String.format(getContext().getString(R.string.format_pressure), data.getFloat(COL_WEATHER_PRESSURE)));



        //mForecastStringTextView.setText(mForecastString);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
