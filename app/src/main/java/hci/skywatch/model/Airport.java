package hci.skywatch.model;

import com.google.gson.annotations.SerializedName;

public class Airport {

    private final String id;
    private final String description;
    @SerializedName("time_zone")
    private final String timeZone;
    private final Double latitude;
    private final Double longitude;
    private final City city;
    private final String terminal;
    private final String gate;
    private final String baggage;

    public Airport(String id, String description, String timeZone, Double latitude, Double longitude,
                   City city, String terminal, String gate, String baggage) {
        this.id = id;
        this.description = description;
        this.timeZone = timeZone;
        this.latitude = latitude;
        this.longitude = longitude;
        this.city = city;
        this.terminal = terminal;
        this.gate = gate;
        this.baggage = baggage;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public City getCity() {
        return city;
    }

    public String getTerminal() {
        return terminal;
    }

    public String getGate() {
        return gate;
    }

    public String getBaggage() {
        return baggage;
    }

    @SuppressWarnings("all")
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ID: " + id);
        sb.append("\nDescription: " + description);
        sb.append("\nLatitude: " + latitude);
        sb.append("\nLongitude: " + longitude);
        sb.append("\nCity: " + city);
        sb.append("\nTerminal: " + terminal);
        sb.append("\nGate: " + gate);
        sb.append("\nBaggage: " + baggage);
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!obj.getClass().equals(this.getClass())) {
            return false;
        }

        Airport other = (Airport)obj;
        return other.id.equals(this.id);
    }
}
