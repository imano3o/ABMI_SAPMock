package de.hhn.sapmock.odata;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Gibt Fehler im OData-v2-Format zurück, so wie SAP es tut:
 * {"error":{"code":"M7/021","message":{"lang":"en","value":"..."}}}
 */
@RestControllerAdvice
public class ODataExceptionHandler {

    public record ODataError(Error error) {
        public record Error(String code, Message message) {
        }

        public record Message(String lang, String value) {
        }
    }

    @ExceptionHandler(ODataException.class)
    public ResponseEntity<ODataError> handle(ODataException ex) {
        ODataError body = new ODataError(new ODataError.Error(ex.getCode(), new ODataError.Message("en", ex.getMessage())));
        return ResponseEntity.status(ex.getStatus()).body(body);
    }
}
