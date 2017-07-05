package hci.skywatch.network;

import hci.skywatch.R;

public class Error {

    private final Integer code;
    private final String message;

    public Error(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    private static final int MISSING_METHOD = 1;
    private static final int MISSING_FLIGHT_NUMBER = 20;
    private static final int MISSING_AIRLINE = 21;
    private static final int INVALID_METHOD = 100;
    private static final int INVALID_FLIGHT = 136;
    private static final int UNKNOWN_ERROR = 999;

    private static final int MISSING_FROM = 11;
    private static final int INVALID_FROM = 125;

/*
 *  1: Missing method: Se requiere un nombre de método el cual no fue suministrado.
 * 11: Missing from: Se requiere un identificador de ciudad o aeropuerto de origen el cual no fue suministrado.
 * 20: Missing flight number: Se requiere un número de vuelo el cual no fue suministrado.
 * 21: Missing airline id: Se requiere un identificador de aerolínea el cual no fue suministrado.
 * 100: Invalid method: El nombre método suministrado es inválido.
 * 125: Invalid from: El origen suministrado es inválido.
 * 136: Invalid flight: Los datos del vuelo suministrados son inválidos.
 * 999: Unknown error: Se produjo un error inesperado procesando la solicitud.
 */

    public static int getMessage(Error error) {
        switch (error.getCode()) {
            /* Bad requests */
            case MISSING_METHOD:
            case INVALID_METHOD:
                return R.string.unexpected_error;

            /* Missing arguments */
            case MISSING_FROM:
                return R.string.missing_departure_location;
            case MISSING_FLIGHT_NUMBER:
                return R.string.missing_flight_number;
            case MISSING_AIRLINE:
                return R.string.missing_airline;

            /* Invalid arguments */
            case INVALID_FLIGHT:
                return R.string.invalid_flight;
            case INVALID_FROM:
                return R.string.invalid_from;

            case UNKNOWN_ERROR:
                return R.string.unexpected_error;
            default:
                return R.string.unexpected_error;
        }
    }

}
