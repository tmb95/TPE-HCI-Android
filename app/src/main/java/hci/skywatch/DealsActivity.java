package hci.skywatch;

import android.app.ProgressDialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import hci.skywatch.fragments.SearchDealsDialogFragment;
import hci.skywatch.model.City;
import hci.skywatch.model.Currency;
import hci.skywatch.model.Deal;
import hci.skywatch.network.CityResponse;
import hci.skywatch.network.Error;
import hci.skywatch.network.FlightDealsResponse;
import hci.skywatch.network.GsonRequest;
import hci.skywatch.network.RequestManager;

public class DealsActivity extends AppCompatActivity implements OnMapReadyCallback, SearchDealsDialogFragment.OnSearchListener {

    private GoogleMap mMap;
    private FloatingActionButton button;
    private Deal[] deals;

    private final String REQUEST_TAG = "deals";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deals);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setTitle(R.string.deals);
        }

        button = (FloatingActionButton) findViewById(R.id.button_search);
        if (button != null) {
            button.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mMap == null) {
                        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
                        Snackbar.make(coordinatorLayout, "Could not connect to GoogleMaps", Snackbar.LENGTH_SHORT).show();
                    } else {
                        SearchDealsDialogFragment dialog = new SearchDealsDialogFragment();
                        dialog.show(getSupportFragmentManager(), "dialog2");
                    }
                }
            });
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onSearch(final String departure) {

        final String URL = FlightDealsResponse.BASE_URL + departure.toUpperCase();

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.searching_deals));
        progressDialog.setCancelable(false);
        progressDialog.show();

        GsonRequest<FlightDealsResponse> dealsRequest = new GsonRequest<>(URL, FlightDealsResponse.class,
                new Response.Listener<FlightDealsResponse>() {
                    @Override
                    public void onResponse(FlightDealsResponse response) {
                        progressDialog.dismiss();
                        Error error = response.getError();
                        if (error != null) {
                            Toast.makeText(DealsActivity.this, Error.getMessage(error), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        deals = response.getDeals();
                        showCurrentCity(departure);
                        showDeals(response.getCurrency());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Toast.makeText(DealsActivity.this, getResources().getString(R.string.unexpected_error), Toast.LENGTH_SHORT).show();
                    }
                });

        dealsRequest.setTag(REQUEST_TAG);
        RequestManager.getInstance(this).addToRequestQueue(dealsRequest);
    }

    @Override
    protected void onStop() {
        super.onStop();

        RequestQueue requestQueue = RequestManager.getInstance(this.getApplicationContext()).getRequestQueue();
        if (requestQueue != null) {
            requestQueue.cancelAll(REQUEST_TAG);
        }
    }

    public void showDeals(Currency currency) {

        if(deals != null && mMap != null) {
            if (deals.length == 0) {
                Toast.makeText(this, R.string.no_deals_found, Toast.LENGTH_SHORT).show();
                return;
            }

            Resources resources = getResources();

            String found = resources.getString(R.string.found);
            String dealsString = resources.getString(R.string.deals);
            CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
            if (coordinatorLayout != null) {
                Snackbar.make(coordinatorLayout, found + ": " + deals.length + " " + dealsString, Snackbar.LENGTH_SHORT).show();
            }

            mMap.clear();
            for(Deal d : deals) {
                Double price = d.getPrice();
                Float color;

                if(price <= 500) {
                    color = BitmapDescriptorFactory.HUE_GREEN;
                }
                else if (price <= 1000) {
                    color = BitmapDescriptorFactory.HUE_YELLOW;
                }
                else {
                    color = BitmapDescriptorFactory.HUE_RED;
                }

                String snip = resources.getString(R.string.price_) + ": "  + currency.getid() + price;

                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(d.getCity().getLatitude(), d.getCity().getLongitude()))
                        .title(d.getCity().getName())
                        .snippet(snip)
                        .icon(BitmapDescriptorFactory.defaultMarker(color)));
            }
        }
    }

    public void showCurrentCity(String city) {
        final String URL = CityResponse.BASE_URL + city.toUpperCase();

        GsonRequest<CityResponse> cityRequest = new GsonRequest<>(URL, CityResponse.class,
                new Response.Listener<CityResponse>() {
                    @Override
                    public void onResponse(CityResponse response) {

                        Error error = response.getError();
                        if (error != null) {
                            return;
                        }
                        City myCity = response.getCity();
                        mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(myCity.getLatitude(), myCity.getLongitude()))
                                .title(myCity.getName())
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });

        RequestManager.getInstance(this).addToRequestQueue(cityRequest);
    }


}
