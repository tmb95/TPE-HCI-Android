package hci.skywatch.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import hci.skywatch.MainActivity;
import hci.skywatch.R;
import hci.skywatch.SettingsActivity;
import hci.skywatch.adapters.FlightAdapter;
import hci.skywatch.adapters.FlightSearchAdapter;
import hci.skywatch.adapters.FlightViewHolder;
import hci.skywatch.adapters.SearchAdapter;
import hci.skywatch.model.Flight;
import hci.skywatch.network.DataBase;
import hci.skywatch.network.Error;
import hci.skywatch.network.FlightStatusResponse;
import hci.skywatch.network.GsonRequest;
import hci.skywatch.network.RequestManager;
import hci.skywatch.views.CustomRecyclerView;

import static hci.skywatch.network.FlightStatusResponse.BASE_URL_PART_1;
import static hci.skywatch.network.FlightStatusResponse.BASE_URL_PART_2;

/**
 * Source: http://nemanjakovacevic.net/blog/english/2016/01/12/recyclerview-swipe-to-delete-no-3rd-party-lib-necessary
 */
public class MyFlightsFragment extends Fragment implements DataBase.OnFlightUpdatedListener, FlightViewHolder.Callback {

    private FlightAdapter adapter;
    private FlightSearchAdapter searchAdapter;

    private List<Flight> myFlights;
    private CustomRecyclerView recyclerView;

    private OnFlightSelectedListener callback;

    private final String REQUEST_TAG = "flights";

    public static final String FLIGHT_JSONS = "flightJSONS";
    public static int HIGHLIGHTED_COLOR;
    public static boolean detailsView;
    public static boolean swipeEnabled;

    private Toast toast;

    public MyFlightsFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity) {
            try {
                callback = (OnFlightSelectedListener) context;
            } catch (ClassCastException e) {
                throw new ClassCastException(context.toString()
                        + " must implement OnFlightSelectedListener");
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myFlights = new ArrayList<>();
        adapter = new FlightAdapter(getActivity(), myFlights, this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            ArrayList<String> flightJSONS = savedInstanceState.getStringArrayList(FLIGHT_JSONS);
            if (flightJSONS != null) {
                Gson gson = new Gson();
                for (String jsonString : flightJSONS) {
                    myFlights.add(gson.fromJson(jsonString, Flight.class));
                }
                adapter.notifyDataSetChanged();
            }
        }

        View rootView = inflater.inflate(R.layout.fragment_my_flights, container, false);

        recyclerView = (CustomRecyclerView) rootView.findViewById(R.id.recycler_view);
        setupRecyclerView();

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        DataBase.getInstance().getAllFlights(this);
        DataBase.getInstance().addOnFlightUpdatedListener(this);

        getActivity().setTitle(R.string.my_flights);
        HIGHLIGHTED_COLOR = getActivity().getColor(R.color.colorAccent);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                searchAdapter = new FlightSearchAdapter(getActivity(), myFlights, MyFlightsFragment.this);
                recyclerView.setAdapter(searchAdapter);
                showToast("Search Started");
                setSwipeEnabled(false);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                showToast("Search Finished");
                recyclerView.setAdapter(adapter);
                searchAdapter = null;
                setSwipeEnabled(true);
                return true;
            }
        });
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            public boolean onQueryTextChange(String newText) {
                Log.d("AppBar", "onQueryTextChange -> " + newText);
                if (searchAdapter != null) {
                    searchAdapter.getFilter().filter(newText);
                }
                return true;
            }

            public boolean onQueryTextSubmit(String query) {
                Log.d("AppBar", "onQueryTextSubmit -> " + query);
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    private void showToast(String text) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_change_view:
                toggleView(item);
                return true;
            case R.id.action_update:
                updateFlights();
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void toggleView(MenuItem item) {
        if (item.getItemId() != R.id.action_change_view) {
            return;
        }

        Drawable detailsViewIcon = getActivity().getDrawable(R.drawable.ic_view_details);
        Drawable listView = getActivity().getDrawable(R.drawable.ic_view_list);

        if (detailsView) {
            item.setIcon(listView);
        } else {
            item.setIcon(detailsViewIcon);
        }

        detailsView = !detailsView;
        adapter = new FlightAdapter(getContext(), myFlights, this);
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setUpItemTouchHelper();
        recyclerView.setUpAnimationDecoratorHelper();
        setSwipeEnabled(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        ArrayList<String> flightJSONS = new ArrayList<>();
        Gson gson = new Gson();

        for (Flight flight : myFlights) {
            flightJSONS.add(gson.toJson(flight));
        }

        // save the current state to recreate the fragment
        outState.putStringArrayList(FLIGHT_JSONS, flightJSONS);
    }


    @Override
    public void onStop() {
        super.onStop();

        RequestQueue requestQueue = RequestManager.getInstance(getActivity()).getRequestQueue();
        if (requestQueue != null) {
            requestQueue.cancelAll(REQUEST_TAG);
        }
    }

    public void setSwipeEnabled(boolean enabled) {
        swipeEnabled = enabled;
    }

    /**
     * Creates a request to get a flight from the API.
     * <p>
     * Called by MainActivity when a flight is added.
     * {@link MainActivity#onFlightAdded(String, String)}
     */
    public void addFlight(String airlineId, String flightNumber) {
        String URL = BASE_URL_PART_1 + airlineId.toUpperCase() + BASE_URL_PART_2 + flightNumber.trim();

        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getString(R.string.searching_flight));
        progressDialog.setCancelable(false);
        progressDialog.show();

        GsonRequest<FlightStatusResponse> flightRequest = new GsonRequest<>(URL, FlightStatusResponse.class,
                new Response.Listener<FlightStatusResponse>() {
                    @Override
                    public void onResponse(FlightStatusResponse response) {
                        progressDialog.dismiss();
                        Error error = response.getError();
                        if (error != null) {
                            Toast.makeText(getActivity(), Error.getMessage(error), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (myFlights.contains(response.getFlight())) {
                            Toast.makeText(getActivity(), R.string.flight_already_present, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        myFlights.add(response.getFlight());
                        adapter.notifyDataSetChanged();

                        // add flight to data base
                        DataBase.getInstance().createFlight(response.getFlight());

                        String message = getResources().getString(R.string.flight_added_msg) + ": " + response.getFlight().toString();

                        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) getActivity().findViewById(R.id.coordinator_layout);
                        if (coordinatorLayout != null) {
                            Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), getResources().getString(R.string.unexpected_error), Toast.LENGTH_SHORT).show();
                    }
                });
        flightRequest.setTag(REQUEST_TAG);
        RequestManager.getInstance(getActivity()).addToRequestQueue(flightRequest);
    }

    /**
     * Implemented by the activity attached to the fragment
     */
    public interface OnFlightSelectedListener {
        /**
         * Called when a list item is selected
         */
        void onFlightSelected(int position, String flightJson);
    }

    @Override
    public void onItemClick(int position, boolean longClick) {
        if (!longClick) {
            // Notify the parent activity of selected item
            Flight clickedFlight;
            if (searchAdapter != null) {
                clickedFlight = searchAdapter.getFlightAt(position);
            } else {
                clickedFlight = adapter.getFlightAt(position);
            }
            String flightJson = new Gson().toJson(clickedFlight);
            callback.onFlightSelected(position, flightJson);
        }
    }

    public void addAll(List<Flight> flights) {
        if (myFlights != null && adapter != null) {
            for (Flight flight : flights) {
                if (!myFlights.contains(flight)) {
                    myFlights.add(flight);
                }
            }
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onFlightUpdated(Flight flight) {    //creo que indexOf no funca porque checkea por referencia
        boolean replace = false;
        int position = -1;
        if (myFlights != null) {
            for (int i = 0; i < myFlights.size(); i++)
                if (myFlights.get(i).getId().equals(flight.getId())) {
                    replace = true;
                    position = i;
                    break;
                }
        }

        if (replace) {
            myFlights.remove(position);
            myFlights.add(0, flight);
            adapter.notifyDataSetChanged();
            if (getActivity() != null) {
                Toast.makeText(getActivity(), "Flight " + flight.getName() + " updated", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class Update {
        private List<Flight> newList = new ArrayList<>();

        boolean isReady() {
            return newList.size() == myFlights.size();
        }

        void add(Flight flight) {
            newList.add(flight);
        }

        List<Flight> getUpdatedList() {
            return newList;
        }
    }

    public void updateFlights() {
        if (myFlights == null) {
            return;
        }

        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getString(R.string.updating_flights));
        progressDialog.setCancelable(false);
        progressDialog.show();

        final Update update = new Update();
        RequestManager.getInstance(getActivity()).getRequestQueue().getCache().clear();

        for (Flight flight : myFlights) {
            String url = BASE_URL_PART_1 + flight.getAirline().getId() + BASE_URL_PART_2 + flight.getNumber();
            GsonRequest<FlightStatusResponse> request = new GsonRequest<>(url, FlightStatusResponse.class, new Response.Listener<FlightStatusResponse>() {
                @Override
                public void onResponse(FlightStatusResponse response) {
                    if (response.getError() != null) {
                        return;
                    }
                    update.add(response.getFlight());
                    if (update.isReady()) {
                        myFlights = update.getUpdatedList();
                        adapter = new FlightAdapter(getActivity(), myFlights, MyFlightsFragment.this);
                        setupRecyclerView();
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), "Update finished", Toast.LENGTH_SHORT).show();
                        DataBase.getInstance().updateAllFlights(myFlights);
                        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    progressDialog.dismiss();
                }
            });
            request.setTag(REQUEST_TAG);
            RequestManager.getInstance(getActivity()).addToRequestQueue(request);
        }
    }

}
