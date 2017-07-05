package hci.skywatch.model;

public class Country {

    private final String id;
    private final String name;

    Country(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "ID: " + id + " Name: " + name;
    }
}
