package hci.skywatch.network;

import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hci.skywatch.R;
import hci.skywatch.fragments.MyFlightsFragment;
import hci.skywatch.model.Flight;

import static android.content.ContentValues.TAG;

public class DataBase {

    private static DataBase instance;

    private FirebaseDatabase firebaseInstance;
    private DatabaseReference firebaseDatabase;
    private Map<Integer, Flight> flightsMap;    //flight id => flight

    private List<OnFlightUpdatedListener> listeners;

    public interface OnFlightUpdatedListener {
        void onFlightUpdated(Flight flight);
    }
    
    private DataBase() {
        firebaseInstance = FirebaseDatabase.getInstance();
        firebaseInstance.setPersistenceEnabled(false);

        firebaseDatabase = firebaseInstance.getReference("flights");

        firebaseInstance.getReference("app_title").setValue("SkyWatch");
        firebaseInstance.getReference("app_title").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e("APP bar", "App title updated");
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e(TAG, "Failed to read app title value.", error.toException());
            }
        });
        flightsMap = new HashMap<>();
        listeners = new ArrayList<>();

        getAllFlights();
    }

    public static DataBase getInstance() {
        if (instance == null) {
            instance = new DataBase();
        }
        return instance;
    }

    // Create new flight and add it to the database
    public void createFlight(Flight flight) {
        Gson gson = new Gson();
        final String key = flight.getId().toString();
        String value = gson.toJson(flight);

        firebaseDatabase.child(key).setValue(value);
        flightsMap.put(flight.getId(), flight);
//        firebaseInstance.getReference(key).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                String json = dataSnapshot.getValue(String.class);
//                if (json == null) {
//                    Log.e(key, "flight has been replaced");
//                } else {
//                    Log.e(key, "flight added");
//                }
//            }
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Log.e(TAG, "Failed to read the value.", databaseError.toException());
//            }
//        });
    }

    // remove flight from database
    public void removeFlight(Flight flight) {
        if (flight != null && firebaseDatabase.child(flight.getId().toString()) != null) {
            firebaseDatabase.child(flight.getId().toString()).removeValue();
            flightsMap.remove(flight.getId());
        }
    }

    // update the flight by id, with a new instance
    public void updateFlight(Flight flight) {
        Gson gson = new Gson();
        String key = flight.getId().toString();
        String value = gson.toJson(flight);

        if (firebaseDatabase.child(key) != null) {
            Log.e("DB - FLIGHT UPDATE", flight.getName());
            firebaseDatabase.child(key).setValue(value);    // estoy updateando un vuelo ya existente

            for (OnFlightUpdatedListener listener : listeners) {
                listener.onFlightUpdated(flight);
            }
        }
    }

    // called on start
    private void getAllFlights() {
        firebaseDatabase.orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e("DB - ADDED:", "\"" + dataSnapshot.getKey()  +"\"");
                for (DataSnapshot children : dataSnapshot.getChildren()) {
                    String jsonString = children.getValue(String.class);
                    Flight aux = new Gson().fromJson(jsonString, Flight.class);
                    flightsMap.put(aux.getId(), aux);
                    Log.e("DB - ADDED:", "\"" + children.getKey()  +"\"");
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Failed to read the value.", databaseError.toException());
            }
        });
    }

    public void getAllFlights(final MyFlightsFragment fragment) {
        final List<Flight> flights = new ArrayList<>();

        // is not the first call of this method
        if (!flightsMap.isEmpty()) {
            flights.addAll(flightsMap.values());
            fragment.addAll(flights);
            return;
        }

        fragment.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        final ProgressDialog progressDialog = new ProgressDialog(fragment.getActivity());
        progressDialog.setMessage(fragment.getString(R.string.loading_flights));
        progressDialog.setCancelable(false);
        progressDialog.show();

        firebaseDatabase.orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                progressDialog.dismiss();
                Log.e("DB - ADDED:", "\"" + dataSnapshot.getKey()  +"\"");
                for (DataSnapshot children : dataSnapshot.getChildren()) {
                    String jsonString = children.getValue(String.class);
                    Flight aux = new Gson().fromJson(jsonString, Flight.class);
                    flightsMap.put(aux.getId(), aux);
                    flights.add(aux);
                    Log.e("DB - ADDED:", "\"" + children.getKey()  +"\"");
                }

                fragment.addAll(flights);
                fragment.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Failed to read the value.", databaseError.toException());
            }
        });
    }

    public Map<Integer, Flight> getFlights() {
        return flightsMap;
    }

    public void addOnFlightUpdatedListener(OnFlightUpdatedListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void updateAllFlights(List<Flight> myFlights) {

        for (Flight flight : myFlights) {
            String key = flight.getId().toString();
            String value = new Gson().toJson(flight);

            firebaseDatabase.child(key).setValue(value);
        }
    }

}
