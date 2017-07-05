package hci.skywatch.network;

import hci.skywatch.model.Currency;
import hci.skywatch.model.Deal;

public class FlightDealsResponse extends Response {

    public static final String BASE_URL = "http://hci.it.itba.edu.ar/v1/api/booking.groovy?method=getflightdeals&from=";

    private final Currency currency;
    private final Deal[] deals;

    public FlightDealsResponse(MetaData meta, Currency currency, Deal[] deals, Error error) {
        super(meta, error);
        this.deals = deals;
        this.currency = currency;
    }

    public Currency getCurrency() {
        return currency;
    }

    public Deal[] getDeals() {
        return deals;
    }
}
