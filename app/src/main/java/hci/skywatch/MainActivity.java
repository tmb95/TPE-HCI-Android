package hci.skywatch;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.gson.Gson;

import hci.skywatch.adapters.FlightAdapter;
import hci.skywatch.fragments.AddFlightDialogFragment;
import hci.skywatch.fragments.FlightDetailsFragment;
import hci.skywatch.fragments.MyFlightsFragment;
import hci.skywatch.model.Flight;
import hci.skywatch.notifications.AlarmReceiver;
import hci.skywatch.notifications.MyFlightsAlarmManager;

/**
 * Main activity with drawer layout.
 */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, AddFlightDialogFragment.OnFlightAddedListener, MyFlightsFragment.OnFlightSelectedListener, FlightAdapter.OnFlightRemovedListener {

    private DrawerLayout drawer;
    private FloatingActionButton button;
    private MyFlightsFragment myFlightsFragment;

    public static boolean dualPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawer.addDrawerListener(toggle);
        toggle.syncState();

        button = (FloatingActionButton) findViewById(R.id.button_add);
        if (button != null) {
            button.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AddFlightDialogFragment dialog = new AddFlightDialogFragment();
                    dialog.show(getSupportFragmentManager(), "dialog");
                }
            });
        }

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_my_flights);

        if (myFlightsFragment == null) {
            myFlightsFragment = (MyFlightsFragment) getSupportFragmentManager().findFragmentById(R.id.my_flights_fragment);
        }
        assert myFlightsFragment != null;

        dualPane = getResources().getBoolean(R.bool.dualPane);

        PendingIntent alarmOn = PendingIntent.getBroadcast(this, 0, new Intent(getApplicationContext(), AlarmReceiver.class), PendingIntent.FLAG_NO_CREATE);
        if (alarmOn == null) {
            SharedPreferences preferences = getPreferences(MODE_PRIVATE);
            Log.e("INTERVAL", Integer.toString(preferences.getInt("Interval", 1)));
            MyFlightsAlarmManager.setAlarm(getApplicationContext(), preferences.getInt("Interval", 1) * 1000 * 60);
        }
    }

    @Override
    public void onBackPressed() {
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setCheckedItem(R.id.nav_my_flights);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Change view when navigation drawer button is pressed
        int id = item.getItemId();

        if (id == R.id.nav_my_flights) {

        } else if (id == R.id.nav_deals) {
            Intent intent = new Intent(this, DealsActivity.class);
            startActivity(intent);
            drawer.closeDrawer(GravityCompat.START);
            return false;
        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }

        // Close drawer after making choice
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFlightAdded(String airlineId, String flightNumber) {
        myFlightsFragment.addFlight(airlineId, flightNumber);
    }

    @Override
    public void onFlightSelected(int position, String flightJson) {
        if (dualPane) {
            FlightDetailsFragment fragment = (FlightDetailsFragment) getSupportFragmentManager().findFragmentById(R.id.flight_details_fragment);
            fragment.updateFlightView(flightJson);
        } else {
            Intent intent = new Intent(this, FlightDetailsActivity.class);
            intent.putExtra(FlightDetailsFragment.FLIGHT_JSON, flightJson);
            startActivity(intent);
        }
    }

    @Override
    public void onFlightRemoved(Flight flight) {
        if (dualPane) {
            FlightDetailsFragment fragment = (FlightDetailsFragment) getSupportFragmentManager().findFragmentById(R.id.flight_details_fragment);
            fragment.removeFlightView(new Gson().toJson(flight));
        }
    }


//    @Override
//    protected void attachBaseContext(Context newBase) {
//        String language = "en";
//        Locale newLocale = new Locale(language);
//        Context context = ContextWrapper.wrap(newBase, newLocale);
//        super.attachBaseContext(context);
//    }

}
