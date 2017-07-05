package hci.skywatch.model;

public class Flight {

    private final Integer id;
    private final Integer number;
    private final Airline airline;
    private final String status;
    private final FlightInfo.Departure departure;
    private final FlightInfo.Arrival arrival;

    public Flight(Integer id, Integer number, Airline airline, String status, FlightInfo.Departure departure, FlightInfo.Arrival arrival) {
        this.id = id;
        this.number = number;
        this.airline = airline;
        this.status = status;
        this.departure = departure;
        this.arrival = arrival;
    }

    public Integer getId() {
        return id;
    }

    public Integer getNumber() {
        return number;
    }

    public String getStatus() {
        return status;
    }

    public Airline getAirline() {
        return airline;
    }

    public FlightInfo.Departure getDeparture() {
        return departure;
    }

    public FlightInfo.Arrival getArrival() {
        return arrival;
    }

//    @SuppressWarnings("all")
//    @Override
//    public String toString() {
//        StringBuilder sb = new StringBuilder();
//        sb.append("Flight: ID: " + id + " Number: " + number);
//        sb.append("\nAirline: " + airline);
//        sb.append("\nStatus: " + status);
//        sb.append("\n" + departure);
//        sb.append("\n" + arrival);
//        return sb.toString();
//    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!obj.getClass().equals(this.getClass())) {
            return false;
        }

        Flight other = (Flight)obj;

        return other.getId().equals(this.getId());
    }

    @Override
    public String toString() {
        return airline.getId() + "-" + number.toString();
    }

    public String getFromTo() {
        return departure.getAirport().getId() + "-" + arrival.getAirport().getId();
    }

    public String getName() {
        return airline.getId() + number;
    }

}

