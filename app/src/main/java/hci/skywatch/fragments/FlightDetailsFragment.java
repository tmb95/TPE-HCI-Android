package hci.skywatch.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;

import hci.skywatch.R;
import hci.skywatch.model.Flight;
import hci.skywatch.model.FlightStatus;

public class FlightDetailsFragment extends Fragment {

    public static final String FLIGHT_JSON = "flight_json";
    private String currentFlightJSON = "";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_flight_details, container, false);

        // If activity recreated (such as from screen rotate), restore
        // the previous flight selection set by onSaveInstanceState().
        // This is primarily necessary when in the two-pane layout.
        if (savedInstanceState != null) {
            currentFlightJSON = savedInstanceState.getString(FLIGHT_JSON);
        }

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        // During startup, check if there are arguments passed to the fragment.
        // onStart is a good place to do this because the layout has already been
        // applied to the fragment at this point so we can safely call the method
        // below that sets the text.
        Bundle args = getArguments();
        if (args != null) {
            // Set flight based on argument passed in
            updateFlightView(args.getString(FLIGHT_JSON));
        } else if (!currentFlightJSON.equals("")) {
            // Set flight based on saved instance state defined during onCreateView
            updateFlightView(currentFlightJSON);
        } else {
            resetView();
        }
    }

    public void updateFlightView(String jsonString) {
        Flight flight = new Gson().fromJson(jsonString, Flight.class);

        getActivity().setTitle(flight.getAirline().getId() + flight.getNumber());

        //TODO, parsear fecha formato: Saturday, October 8. duration formato 1h 20m. Hora 7:00 AM
        String[] departureScheduledTime = flight.getDeparture().getScheduledTime().split("\\s");
        String[] arrivalScheduledTime = flight.getArrival().getScheduledTime().split("\\s");

        String departureAirportID = flight.getDeparture().getAirport().getId();
        String departureAirportDescription = flight.getDeparture().getAirport().getDescription();
        String arrivalAirportID = flight.getArrival().getAirport().getId();
        String arrivalAirportDescription = flight.getArrival().getAirport().getDescription();

        CardView cardView = (CardView) getActivity().findViewById(R.id.status_card_view);

        FlightStatus status = FlightStatus.getStatusById(flight.getStatus());
        cardView.setCardBackgroundColor(status.color);

        TextView textView = (TextView) getActivity().findViewById(R.id.flight_status_text);
        textView.setText(status.stringResourceId);

        TextView departureAirport = (TextView) getActivity().findViewById(R.id.departure_airport);
        departureAirport.setText(departureAirportDescription + " (" + departureAirportID + ")");

        TextView departureTime = (TextView) getActivity().findViewById(R.id.departure_date_and_time);
        departureTime.setText(departureScheduledTime[0] + "  " + trimHour(departureScheduledTime[1]));

        TextView departureTerminal  = (TextView) getActivity().findViewById(R.id.departure_terminal);
        departureTerminal.setText(flight.getDeparture().getAirport().getTerminal());

        TextView departureGate = (TextView) getActivity().findViewById(R.id.departure_gate);
        String departureGateText = flight.getDeparture().getAirport().getGate();
        if (departureGateText == null || departureGateText.equals("")) {
            departureGate.setText(" - ");
        } else {
            departureGate.setText(departureGateText);
        }

        TextView arrivalAirport = (TextView) getActivity().findViewById(R.id.arrival_airport);
        arrivalAirport.setText(arrivalAirportDescription + " (" + arrivalAirportID + ")");

        TextView arrivalTime = (TextView) getActivity().findViewById(R.id.arrival_date_and_time);
        arrivalTime.setText(arrivalScheduledTime[0] + "  " + trimHour(arrivalScheduledTime[1]));

        TextView arrivalTerminal  = (TextView) getActivity().findViewById(R.id.arrival_terminal);
        arrivalTerminal.setText(flight.getArrival().getAirport().getTerminal());

        TextView arrivalGate = (TextView) getActivity().findViewById(R.id.arrival_gate);
        String arrivalGateText = flight.getArrival().getAirport().getGate();
        if (arrivalGateText == null || arrivalGateText.equals("")) {
            arrivalGate.setText(" - ");
        } else {
            arrivalGate.setText(arrivalGateText);
        }

        TextView baggageGate = (TextView) getActivity().findViewById(R.id.arrival_baggage_gate);
        String baggageGateText = flight.getArrival().getAirport().getBaggage();
        if (baggageGateText == null || baggageGateText.equals("")) {
            baggageGate.setText(" - ");
        } else {
            baggageGate.setText(baggageGateText);
        }

        currentFlightJSON = jsonString;
    }

    private String trimHour(String s) {
        return s.substring(0, s.length() - 3);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the current flight selection in case we need to recreate the fragment
        outState.putString(FLIGHT_JSON, currentFlightJSON);
    }

    public void removeFlightView(String jsonString) {
        // el vuelo eliminado es el mismo que se esta mostrando
        if (jsonString.equals(currentFlightJSON)) {
            currentFlightJSON = "";
            resetView();
        }
    }

    // hides every element on the view
    private void resetView() {

        getActivity().setTitle(R.string.my_flights);

        CardView cardView = (CardView) getActivity().findViewById(R.id.status_card_view);

        cardView.setCardBackgroundColor(FlightStatus.UNKNOWN.color);
        TextView textView = (TextView) getActivity().findViewById(R.id.flight_status_text);
        textView.setText(R.string.no_flight_selected);

        TextView departureAirport = (TextView) getActivity().findViewById(R.id.departure_airport);
        departureAirport.setText("");

        TextView departureTime = (TextView) getActivity().findViewById(R.id.departure_date_and_time);
        departureTime.setText("");

        TextView departureTerminal  = (TextView) getActivity().findViewById(R.id.departure_terminal);
        departureTerminal.setText("");

        TextView departureGate = (TextView) getActivity().findViewById(R.id.departure_gate);
        departureGate.setText("");

        TextView arrivalAirport = (TextView) getActivity().findViewById(R.id.arrival_airport);
        arrivalAirport.setText("");

        TextView arrivalTime = (TextView) getActivity().findViewById(R.id.arrival_date_and_time);
        arrivalTime.setText("");

        TextView arrivalTerminal  = (TextView) getActivity().findViewById(R.id.arrival_terminal);
        arrivalTerminal.setText("");

        TextView arrivalGate = (TextView) getActivity().findViewById(R.id.arrival_gate);
        arrivalGate.setText("");

        TextView baggageGate = (TextView) getActivity().findViewById(R.id.arrival_baggage_gate);
        baggageGate.setText("");
    }
}
