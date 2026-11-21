package io.github.vinjavarapu.seleniumtestautomation.framework.tests;

import io.github.vinjavarapu.seleniumtestautomation.framework.base.BaseTest;
import io.github.vinjavarapu.seleniumtestautomation.framework.config.FrameworkConfig;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SmokeTest extends BaseTest {
    @Test(groups = "smoke")
    public void applicationOpens() {
        getDriver().get(FrameworkConfig.baseUrl());

        Assert.assertEquals(getDriver().getTitle(), "Swag Labs");
    }
}
