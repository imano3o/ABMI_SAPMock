package de.hhn.sapmock.odata;

/**
 * OData v2 response for a single entity, e.g. after a POST: {"d":{...}}
 */
public record ODataEntity<T>(T d) {

    public static <T> ODataEntity<T> of(T entity) {
        return new ODataEntity<>(entity);
    }
}
