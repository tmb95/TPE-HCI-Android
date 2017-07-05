package hci.skywatch.model;

public class City {

    private final String id;
    private final String name;
    private final Double latitude;
    private final Double longitude;
    private final Country country;

    public City(String id, String name, Double latitude, Double longitude, Country country) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.country = country;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Country getCountry() {
        return country;
    }

    @SuppressWarnings("all")
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ID: " + id);
        sb.append("\nName: " + name);
        sb.append("\nLatitude: " + latitude);
        sb.append("\nLongitude: " + longitude);
        sb.append("\nCountry: " + country);
        return sb.toString();
    }
}

