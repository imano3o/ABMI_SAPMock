package de.hhn.sapmock.odata;

import org.springframework.http.HttpStatus;

/**
 * Business error that is returned to the caller in SAP error format.
 * code is the SAP message code, e.g. "M7/021" for insufficient stock.
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
