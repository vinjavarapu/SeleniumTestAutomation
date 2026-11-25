package io.github.vinjavarapu.seleniumtestautomation.framework.tests.testdata;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Checkout information validation test data record. */
public record CheckoutValidation(
        @JsonProperty("firstName") String firstName,
        @JsonProperty("lastName") String lastName,
        @JsonProperty("postalCode") String postalCode,
        @JsonProperty("expectedMessage") String expectedMessage) {

    @JsonCreator
    public CheckoutValidation {
    }
}
