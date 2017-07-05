package hci.skywatch.model;

import com.google.gson.annotations.SerializedName;

public class Airline {

    private final String id;
    private final String name;
    @SerializedName("logo")
    private final String logoUrl;

    public Airline(String id, String name, String logoUrl) {
        this.id = id;
        this.name = name;
        this.logoUrl = logoUrl;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    @Override
    public String toString() {
        return "ID: " + id + " name: " + name;
    }

    public boolean equals(Object o) {
        if(o == null) {
            return false;
        }
        if(!o.getClass().equals(this.getClass())) {
            return false;
        }

        Airport airport = (Airport) o;

        return id.equals(airport.getId());
    }
}
