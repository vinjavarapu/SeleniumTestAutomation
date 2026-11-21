package io.github.vinjavarapu.seleniumtestautomation.framework.tests;

import io.github.vinjavarapu.seleniumtestautomation.framework.base.BaseTest;
import io.github.vinjavarapu.seleniumtestautomation.framework.reporting.TestStepReporter;
import io.github.vinjavarapu.seleniumtestautomation.framework.config.FrameworkConfig;
import org.testng.annotations.Test;

public class SmokeTest extends BaseTest {
    @Test(groups = "smoke")
    public void applicationOpens() {
        openUrl(FrameworkConfig.baseUrl(), "Opening the application home page");

        TestStepReporter.assertEquals(getDriver().getTitle(), "Swag Labs");
    }
}
