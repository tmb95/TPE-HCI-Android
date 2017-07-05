package hci.skywatch;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Locale;

import hci.skywatch.notifications.MyFlightsAlarmManager;


public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setTitle(R.string.settings);
        }

        preferences = getPreferences(MODE_PRIVATE);

        Spinner fetch_new_data = (Spinner) findViewById(R.id.fetch_spinner);

        Integer interval = preferences.getInt("Interval", 1);
        fetch_new_data.setSelection(getIntervalSelection(interval));
        fetch_new_data.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                int interval;   // minutes
                if (pos == 0) {
                    interval = 1;
                } else if (pos == 1) {
                    interval = 5;
                } else if (pos == 2) {
                    interval = 15;
                } else if (pos == 3) {
                    interval = 30;
                } else {
                    Log.e("INVALID", "INTERVAL");
                    return;
                }

                int savedInterval = preferences.getInt("Interval", 1);

                if (savedInterval == interval) {
                    return;
                }

                SharedPreferences.Editor editor = preferences.edit();
                Log.e("OPTION SELECTED ", Integer.toString(interval));
                editor.putInt("Interval", interval);
                editor.apply();
                MyFlightsAlarmManager.updateAlarm(getApplicationContext(), interval * 1000 * 60);
            }

            public void onNothingSelected(AdapterView<?> arg0) {
                Toast.makeText(getApplicationContext(), R.string.nothing, Toast.LENGTH_LONG).show();
            }
        });


        Spinner languageSpinner = (Spinner) findViewById(R.id.language_spinner);

        String language = preferences.getString("Language", "en");
        languageSpinner.setSelection(getLanguageSelection(language));
        languageSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String language;
                if (pos == 0) {
                    language = "en";
                } else if (pos == 1) {
                    language = "es";
                } else {
                    Log.e("INVALID", "LANGUAGE");
                    return;
                }

                String savedLanguage = preferences.getString("Language", "en");

                if (savedLanguage.equals(language)) {
                    return;
                }

                SharedPreferences.Editor editor = preferences.edit();
                Log.e("OPTION SELECTED ", language);
                editor.putString("Language", language);
                editor.apply();
                setLocale(language);
            }

            public void onNothingSelected(AdapterView<?> arg0) {
                Toast.makeText(getApplicationContext(), R.string.nothing, Toast.LENGTH_LONG).show();
            }
        });

        if (savedInstanceState != null) {
            //setLocale(savedInstanceState.getString("language"));
        }
    }

    private int getLanguageSelection(String language) {
        switch (language) {
            case "en":
                return 0;
            case "es":
                return 1;
            default:
                return 0;
        }
    }

    private int getIntervalSelection(Integer interval) {
        switch (interval) {
            case 1:
                return 0;
            case 5:
                return 1;
            case 15:
                return 2;
            case 30:
                return 3;
            case 60:
                return 4;
            default:
                return 0;
        }
    }

    public void setLocale(String lang) {

        Locale newLocale = new Locale(lang);
        Locale.setDefault(newLocale);
        Locale.setDefault(newLocale);
        Configuration config = new Configuration();
        config.setLocale(newLocale);
        Resources res = getBaseContext().getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        res.updateConfiguration(config, dm);
        recreate();

//        Locale myLocale = new Locale(lang);
//        Locale.setDefault(myLocale);
//        Configuration config = new Configuration();
//        config.setLocale(myLocale);
//        Context context = createConfigurationContext(config);
//
//        Intent refresh = new Intent(this, SettingsActivity.class);
//        Intent refresh2 = new Intent(this, MainActivity.class);
//        startActivity(refresh2);
//        startActivity(refresh);
//        finish();

//        Context context = ContextWrapper.wrap(getApplicationContext(), myLocale);
//        Intent refresh = new Intent(context, SettingsActivity.class);
//        Intent refresh2 = new Intent(context, MainActivity.class);
//        startActivity(refresh2);
//        startActivity(refresh);
//        finish();

//        Locale.setDefault(myLocale);
//        Configuration config = new Configuration();
//        config.setLocale(myLocale);
//        Resources res = getBaseContext().getResources();
//        DisplayMetrics dm = res.getDisplayMetrics();
//        res.updateConfiguration(config, dm);

//        Intent refresh = new Intent(this, SettingsActivity.class);
//        Intent refresh2 = new Intent(this, MainActivity.class);
//        startActivity(refresh2);
//        startActivity(refresh);
//        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("language", preferences.getString("Language", "en"));
        super.onSaveInstanceState(outState);
    }
}