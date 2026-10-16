package de.hhn.sapmock.odata;

import org.springframework.http.HttpStatus;

/**
 * Fachlicher Fehler, der im SAP-Fehlerformat an den Aufrufer zurückgegeben wird.
 * code entspricht dem SAP-Nachrichtencode, z.B. "M7/021" für fehlenden Bestand.
 */
public class ODataException extends RuntimeException {

    private final String code;
    private final HttpStatus status;

    public ODataException(String code, String message) {
        this(code, message, HttpStatus.BAD_REQUEST);
    }

    public ODataException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
