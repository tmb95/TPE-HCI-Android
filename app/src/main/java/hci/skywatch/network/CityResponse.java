package hci.skywatch.network;

import hci.skywatch.model.City;

/**
 * Created by damii on 6/25/2017.
 */

public class CityResponse extends Response {

    public static final String BASE_URL = "http://hci.it.itba.edu.ar/v1/api/geo.groovy?method=getcitybyid&id=";

    private final City city;

    public CityResponse(MetaData meta, City city, Error error) {
        super(meta, error);
        this.city = city;
    }

    public City getCity() {
        return city;
    }

}
