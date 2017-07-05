package hci.skywatch.network;

/**
 * Base class for all responses from the API.
 */
@SuppressWarnings("all")
public abstract class Response {

    private final MetaData meta;
    private final Error error;

    protected Response(MetaData meta, Error error) {
        this.meta = meta;
        this.error = error;
    }

    public MetaData getMeta() {
        return meta;
    }

    public Error getError() {
        return error;
    }
}
