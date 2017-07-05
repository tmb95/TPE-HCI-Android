package hci.skywatch.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
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
import hci.skywatch.model.Flight;
import hci.skywatch.network.DataBase;
import hci.skywatch.network.Error;
import hci.skywatch.network.FlightStatusResponse;
import hci.skywatch.network.GsonRequest;
import hci.skywatch.network.RequestManager;

import static hci.skywatch.network.FlightStatusResponse.BASE_URL_PART_1;
import static hci.skywatch.network.FlightStatusResponse.BASE_URL_PART_2;

/**
 * Source: http://nemanjakovacevic.net/blog/english/2016/01/12/recyclerview-swipe-to-delete-no-3rd-party-lib-necessary
 */
public class MyFlightsFragment extends Fragment implements FlightAdapter.OnClickListener, DataBase.OnFlightUpdatedListener {

    private List<Flight> myFlights;
    private RecyclerView recyclerView;
    private FlightAdapter adapter;

    private OnFlightSelectedListener callback;

    private final String REQUEST_TAG = "flights";

    public MyFlightsFragment() {
        setHasOptionsMenu(true);
    }

    public static final String FLIGHT_JSONS = "flightJSONS";

    public void onFlightViewChanged() {
        adapter = new FlightAdapter(getContext(), myFlights, this);
        setupRecyclerView();
    }

    @Override
    public void onClick(int position) {
        // Notify the parent activity of selected item
        Flight clickedFlight = adapter.getFlightAt(position);
        String flightJson = new Gson().toJson(clickedFlight);
        callback.onFlightSelected(position, flightJson);
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

    public static int HIGHLIGHTED_COLOR;

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

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
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

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        setUpItemTouchHelper();
        setUpAnimationDecoratorHelper();
    }

    public final static String color_red = "#D32F2F";

    /**
     * This is the standard support library way of implementing "swipe to delete" feature. You can do custom drawing in onChildDraw method
     * but whatever you draw will disappear once the swipe is over, and while the items are animating to their new position the recycler view
     * background will be visible. That is rarely an desired effect.
     */
    private void setUpItemTouchHelper() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            // we want to cache these and not allocate anything repeatedly in the onChildDraw method
            Drawable background;
            Drawable xMark;
            int xMarkMargin;
            boolean initiated;

            private void init() {
                background = new ColorDrawable(Color.parseColor(color_red));
                xMark = ContextCompat.getDrawable(getContext(), R.drawable.ic_delete_white_24dp);
                xMark.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                xMarkMargin = (int) getActivity().getResources().getDimension(R.dimen.ic_clear_margin);
                initiated = true;
            }

            // not important, we don't want drag & drop
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int position = viewHolder.getAdapterPosition();
                FlightAdapter adapter = (FlightAdapter) recyclerView.getAdapter();
                if (adapter.isUndoOn() && adapter.isPendingRemoval(position)) {
                    return 0;
                }
                return super.getSwipeDirs(recyclerView, viewHolder);
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int swipedPosition = viewHolder.getAdapterPosition();
                FlightAdapter adapter = (FlightAdapter) recyclerView.getAdapter();
                boolean undoOn = adapter.isUndoOn();
                if (undoOn) {
                    adapter.pendingRemoval(swipedPosition);
                } else {
                    adapter.remove(swipedPosition);
                }
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;

                // not sure why, but this method get's called for viewholder that are already swiped away
                if (viewHolder.getAdapterPosition() == -1) {
                    // not interested in those
                    return;
                }

                if (!initiated) {
                    init();
                }

                // draw red background
                background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                background.draw(c);

                // draw x mark
                int itemHeight = itemView.getBottom() - itemView.getTop();
                int intrinsicWidth = xMark.getIntrinsicWidth();
                int intrinsicHeight = xMark.getIntrinsicWidth();

                int xMarkLeft = itemView.getRight() - xMarkMargin - intrinsicWidth;
                int xMarkRight = itemView.getRight() - xMarkMargin;
                int xMarkTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
                int xMarkBottom = xMarkTop + intrinsicHeight;
                xMark.setBounds(xMarkLeft, xMarkTop, xMarkRight, xMarkBottom);

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                xMark.draw(c);

            }

        };
        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);
    }

    /**
     * We're gonna setup another ItemDecorator that will draw the red background in the empty space while the items are animating to their new positions
     * after an item is removed.
     */
    private void setUpAnimationDecoratorHelper() {
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {

            // we want to cache this and not allocate anything repeatedly in the onDraw method
            Drawable background;
            boolean initiated;

            private void init() {
                background = new ColorDrawable(Color.parseColor(color_red));
                initiated = true;
            }

            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {

                if (!initiated) {
                    init();
                }

                // only if animation is in progress
                if (parent.getItemAnimator().isRunning()) {

                    // some items might be animating down and some items might be animating up to close the gap left by the removed item
                    // this is not exclusive, both movement can be happening at the same time
                    // to reproduce this leave just enough items so the first one and the last one would be just a little off screen
                    // then remove one from the middle

                    // find first child with translationY > 0
                    // and last one with translationY < 0
                    // we're after a rect that is not covered in recycler-view views at this point in time
                    View lastViewComingDown = null;
                    View firstViewComingUp = null;

                    // this is fixed
                    int left = 0;
                    int right = parent.getWidth();

                    // this we need to find out
                    int top = 0;
                    int bottom = 0;

                    // find relevant translating views
                    int childCount = parent.getLayoutManager().getChildCount();
                    for (int i = 0; i < childCount; i++) {
                        View child = parent.getLayoutManager().getChildAt(i);
                        if (child.getTranslationY() < 0) {
                            // view is coming down
                            lastViewComingDown = child;
                        } else if (child.getTranslationY() > 0) {
                            // view is coming up
                            if (firstViewComingUp == null) {
                                firstViewComingUp = child;
                            }
                        }
                    }

                    if (lastViewComingDown != null && firstViewComingUp != null) {
                        // views are coming down AND going up to fill the void
                        top = lastViewComingDown.getBottom() + (int) lastViewComingDown.getTranslationY();
                        bottom = firstViewComingUp.getTop() + (int) firstViewComingUp.getTranslationY();
                    } else if (lastViewComingDown != null) {
                        // views are going down to fill the void
                        top = lastViewComingDown.getBottom() + (int) lastViewComingDown.getTranslationY();
                        bottom = lastViewComingDown.getBottom();
                    } else if (firstViewComingUp != null) {
                        // views are coming up to fill the void
                        top = firstViewComingUp.getTop();
                        bottom = firstViewComingUp.getTop() + (int) firstViewComingUp.getTranslationY();
                    }

                    background.setBounds(left, top, right, bottom);
                    background.draw(c);

                }
                super.onDraw(c, parent, state);
            }

        });
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

    public FlightAdapter getAdapter() {
        return adapter;
    }

}
