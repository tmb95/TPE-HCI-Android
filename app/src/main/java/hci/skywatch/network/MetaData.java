package hci.skywatch.network;

public class MetaData {

    private final String uuid;
    private final String time;

    public MetaData(String uuid, String time) {
        this.uuid = uuid;
        this.time = time;
    }

    public String getUuid() {
        return uuid;
    }

    public String getTime() {
        return time;
    }

}
