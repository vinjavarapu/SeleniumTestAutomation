package org.example.framework.tests;

import org.example.framework.base.BaseTest;
import org.example.framework.config.FrameworkConfig;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SmokeTest extends BaseTest {
    @Test(groups = "smoke")
    public void applicationOpens() {
        getDriver().get(FrameworkConfig.baseUrl());

        Assert.assertEquals(getDriver().getTitle(), "Swag Labs");
    }
}
