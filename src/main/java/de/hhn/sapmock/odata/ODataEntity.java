package de.hhn.sapmock.odata;

/**
 * OData-v2-Antwort für eine einzelne Entität, z.B. nach einem POST: {"d":{...}}
 */
public record ODataEntity<T>(T d) {

    public static <T> ODataEntity<T> of(T entity) {
        return new ODataEntity<>(entity);
    }
}
