package io.github.vinjavarapu.seleniumtestautomation.framework.base;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.reflect.Proxy;

public class BasePageTest {
    private static final class TestablePage extends BasePage {
        private TestablePage() {
            super((WebDriver) Proxy.newProxyInstance(
                    WebDriver.class.getClassLoader(),
                    new Class<?>[] {WebDriver.class},
                    (proxy, method, args) -> null));
        }

        private boolean checkImageLoaded(WebElement image) {
            return isImageLoaded(image);
        }
    }

    @Test
    public void imageLoadCheckHandlesNullAndInvalidNaturalWidthSafely() {
        TestablePage page = new TestablePage();

        Assert.assertFalse(page.checkImageLoaded(stubImage("false", "100")));
        Assert.assertFalse(page.checkImageLoaded(stubImage("true", null)));
        Assert.assertFalse(page.checkImageLoaded(stubImage("true", "")));
        Assert.assertFalse(page.checkImageLoaded(stubImage("true", "0")));
        Assert.assertFalse(page.checkImageLoaded(stubImage("true", "NaN")));
        Assert.assertTrue(page.checkImageLoaded(stubImage("true", "240")));
    }

    private static WebElement stubImage(String complete, String naturalWidth) {
        return (WebElement) Proxy.newProxyInstance(
                WebElement.class.getClassLoader(),
                new Class<?>[] {WebElement.class},
                (proxy, method, args) -> {
                    if ("getDomProperty".equals(method.getName()) && args != null && args.length == 1) {
                        if ("complete".equals(args[0])) return complete;
                        if ("naturalWidth".equals(args[0])) return naturalWidth;
                    }
                    return null;
                });
    }
}
