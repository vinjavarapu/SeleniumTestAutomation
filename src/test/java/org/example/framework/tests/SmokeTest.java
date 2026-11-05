package org.example.framework.tests;

import org.example.framework.base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SmokeTest extends BaseTest {
    @Test
    public void exampleSiteOpens() {
        getDriver().get("https://example.com");

        Assert.assertEquals(getDriver().getTitle(), "Example Domain");
    }
}