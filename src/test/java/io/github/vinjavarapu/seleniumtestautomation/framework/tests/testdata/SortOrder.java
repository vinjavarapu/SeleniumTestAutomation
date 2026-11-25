package io.github.vinjavarapu.seleniumtestautomation.framework.tests.testdata;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Product sort order test data record. */
public record SortOrder(
        @JsonProperty("sort") String sort,
        @JsonProperty("names") String names,
        @JsonProperty("descending") String descending) {

    @JsonCreator
    public SortOrder {
    }
}
