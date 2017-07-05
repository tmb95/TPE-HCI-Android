package hci.skywatch.notifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;

import java.util.Map;

import hci.skywatch.FlightDetailsActivity;
import hci.skywatch.R;
import hci.skywatch.fragments.FlightDetailsFragment;
import hci.skywatch.model.Flight;
import hci.skywatch.network.DataBase;
import hci.skywatch.network.Error;
import hci.skywatch.network.FlightStatusResponse;
import hci.skywatch.network.GsonRequest;
import hci.skywatch.network.RequestManager;

import static hci.skywatch.network.FlightStatusResponse.BASE_URL_PART_1;
import static hci.skywatch.network.FlightStatusResponse.BASE_URL_PART_2;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {

        Log.e("ALARM RECEIVED", "UPDATING FLIGHTS...");

        Map<Integer, Flight> flightMap = DataBase.getInstance().getFlights();

        Log.e("flights", flightMap.toString());

        boolean first = true;

        for(Integer n : flightMap.keySet()) {

            // hardcoded notif
            if (first) {
                sendNotification(context, flightMap.get(n));
                first = false;
            }

            final Flight current = flightMap.get(n);

            String airlineId = current.getAirline().getId();
            String flightNumber = current.getNumber().toString();

            String URL = BASE_URL_PART_1 + airlineId.toUpperCase() + BASE_URL_PART_2 + flightNumber;

            GsonRequest<FlightStatusResponse> flightRequest = new GsonRequest<>(URL, FlightStatusResponse.class,
                    new Response.Listener<FlightStatusResponse>() {
                        @Override
                        public void onResponse(FlightStatusResponse response) {
                            Error error= response.getError();
                            if (error != null) {
                                Log.e("ERROR " + error.getCode() + ": ", error.getMessage());
                                return;
                            }

                            if(!compareFlights(current, response.getFlight())) {
                                // Flight is updated, create new notification
                                sendNotification(context, response.getFlight());
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                        }
                    });

            RequestManager.getInstance(context).addToRequestQueue(flightRequest);
        }
    }

    public boolean compareFlights(Flight f1, Flight f2) {

        return f1.getDeparture().equals(f2.getDeparture())
                && f1.getArrival().equals(f2.getArrival())
                && f1.getStatus().equals(f2.getStatus());
    }


    private void sendNotification(Context context, Flight current) {
        Intent resultIntent = new Intent(context, FlightDetailsActivity.class);
        resultIntent.putExtra(FlightDetailsFragment.FLIGHT_JSON, new Gson().toJson(current, Flight.class));

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(FlightDetailsActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE) // overrides do not disturb
                .setSmallIcon(R.mipmap.ic_menu_flights)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setContentTitle(current.toString())
                .setContentText("Flight updated")
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setGroup("skywatch");

        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(current.getId(), mBuilder.build());

        DataBase.getInstance().updateFlight(current);
    }
}
