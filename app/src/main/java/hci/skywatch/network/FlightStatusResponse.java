package hci.skywatch.network;

import com.google.gson.annotations.SerializedName;

import hci.skywatch.model.Flight;

/**
 * Response to the API Get Flight Status Method
 */
public class FlightStatusResponse extends Response {

    public static final String BASE_URL_PART_1 = "http://hci.it.itba.edu.ar/v1/api/status.groovy?method=getflightstatus&airline_id=";
    public static final String BASE_URL_PART_2 = "&flight_number=";

    @SerializedName("status")
    private final Flight flight;

    public FlightStatusResponse(MetaData meta, Error error, Flight flight) {
        super(meta, error);
        this.flight = flight;
    }

    public Flight getFlight() {
        return flight;
    }

}
