package io.github.vinjavarapu.seleniumtestautomation.framework.utils;

import org.testng.Assert;
import org.testng.annotations.Test;

public class UrlRedactorTest {
    @Test
    public void redactsUserInfoSensitiveQueryValuesAndFragments() {
        String result = UrlRedactor.redact(
                "https://user:password@example.test/path?keep=value&access_token=secret#session");

        Assert.assertEquals(result,
                "https://example.test/path?keep=value&access_token=%5BREDACTED%5D#[REDACTED]");
        Assert.assertFalse(result.contains("user"));
        Assert.assertFalse(result.contains("password"));
        Assert.assertFalse(result.contains("secret"));
        Assert.assertFalse(result.contains("session"));
    }

    @Test
    public void redactsEncodedSensitiveQueryNames() {
        Assert.assertEquals(UrlRedactor.redact("https://example.test/?api%5Fkey=private"),
                "https://example.test/?api%5Fkey=%5BREDACTED%5D");
    }

    @Test
    public void doesNotModifyNonSensitiveUrlComponents() {
        Assert.assertEquals(UrlRedactor.redact("https://example.test/catalog?page=2"),
                "https://example.test/catalog?page=2");
    }

    @Test
    public void doesNotExposeMalformedUrlInput() {
        Assert.assertEquals(UrlRedactor.redact("not a url"), "[invalid URL]");
    }
}
