package de.hhn.sapmock.odata;

import java.util.List;

/**
 * OData-v2-Antwort für eine Liste von Entitäten: {"d":{"results":[...]}}
 */
public record ODataList<T>(Results<T> d) {

    public record Results<T>(List<T> results) {
    }

    public static <T> ODataList<T> of(List<T> results) {
        return new ODataList<>(new Results<>(results));
    }
}
