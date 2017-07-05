package hci.skywatch.model;

public class Deal {

    private City city;
    private Double price;

    public Deal(City city, Double price) {
        this.city = city;
        this.price = price;
    }

    public City getCity() {
        return city;
    }

    public Double getPrice() {
        return price;
    }
}
