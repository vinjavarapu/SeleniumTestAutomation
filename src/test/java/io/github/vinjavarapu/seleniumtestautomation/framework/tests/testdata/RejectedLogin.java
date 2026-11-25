package io.github.vinjavarapu.seleniumtestautomation.framework.tests.testdata;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Rejected login scenario test data record. */
public record RejectedLogin(
        @JsonProperty("username") String username,
        @JsonProperty("password") String password,
        @JsonProperty("expectedMessage") String expectedMessage) {

    @JsonCreator
    public RejectedLogin {
    }
}
