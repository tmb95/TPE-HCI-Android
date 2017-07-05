package hci.skywatch.model;

import android.graphics.Color;
import android.graphics.drawable.Drawable;

import hci.skywatch.R;

public enum FlightStatus {

    SCHEDULED("S", Color.parseColor("#1abc9c"), R.string.flight_status_scheduled),
    //1abc9c
    //16a085
    ACTIVE("A", Color.parseColor("#3498db"), R.string.flight_status_active),
    //3498db
    //2980b9
    LANDED("L", Color.parseColor("#27ae60"), R.string.flight_status_landed),
    //2ecc71
    //27ae60
    DIVERTED("R", Color.parseColor("#f1c40f"), R.string.flight_status_diverted),
    //f1c40f
    //f39c12
    CANCELLED("C", Color.parseColor("#e74c3c"), R.string.flight_status_cancelled),
    //e74c3c
    //c0392b

    UNKNOWN("U", Color.parseColor("#34495e"), R.string.flight_stats_unknown);
    //34495e
    //2c3e50


    public final String ID;
    public final int color;
    public final int stringResourceId;

    FlightStatus(String ID, int color, int stringResourceId) {
        this.ID = ID;
        this.color = color;
        this.stringResourceId = stringResourceId;
    }

    public static FlightStatus getStatusById(String id) {
        for (FlightStatus status : values()) {
            if (status.ID.equals(id)) {
                return status;
            }
        }
        return UNKNOWN;
    }

}
