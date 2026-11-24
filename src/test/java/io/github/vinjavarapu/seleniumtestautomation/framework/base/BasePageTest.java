package io.github.vinjavarapu.seleniumtestautomation.framework.base;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class BasePageTest {
    private static final class TestablePage extends BasePage {
        private TestablePage(WebDriver driver) {
            super(driver);
        }

        private boolean checkImageLoaded(WebElement image) {
            return isImageLoaded(image);
        }

        private int checkBadgeCount(By locator) {
            return readBadgeCount(locator);
        }
    }

    @Test
    public void imageLoadCheckHandlesNullAndInvalidNaturalWidthSafely() {
        TestablePage page = new TestablePage(stubDriver(List.of()));

        Assert.assertFalse(page.checkImageLoaded(stubImage("false", "100")));
        Assert.assertFalse(page.checkImageLoaded(stubImage("true", null)));
        Assert.assertFalse(page.checkImageLoaded(stubImage("true", "")));
        Assert.assertFalse(page.checkImageLoaded(stubImage("true", "0")));
        Assert.assertFalse(page.checkImageLoaded(stubImage("true", "NaN")));
        Assert.assertTrue(page.checkImageLoaded(stubImage("true", "240")));
    }

    @Test
    public void badgeCountRetriesWhenBadgeIsReplacedDuringRead() {
        AtomicInteger reads = new AtomicInteger();
        WebElement badge = (WebElement) Proxy.newProxyInstance(
                WebElement.class.getClassLoader(),
                new Class<?>[] {WebElement.class},
                (proxy, method, args) -> {
                    if ("isDisplayed".equals(method.getName())) return true;
                    if ("getText".equals(method.getName())) {
                        if (reads.incrementAndGet() == 1) {
                            throw new StaleElementReferenceException("Badge was replaced");
                        }
                        return "2";
                    }
                    return null;
                });
        TestablePage page = new TestablePage(stubDriver(List.of(badge)));

        Assert.assertEquals(page.checkBadgeCount(By.cssSelector("[data-test='shopping-cart-badge']")), 2);
        Assert.assertEquals(reads.get(), 2);
    }

    @Test
    public void missingBadgeHasZeroCount() {
        TestablePage page = new TestablePage(stubDriver(List.of()));

        Assert.assertEquals(page.checkBadgeCount(By.cssSelector("[data-test='shopping-cart-badge']")), 0);
    }

    private static WebDriver stubDriver(List<WebElement> elements) {
        return (WebDriver) Proxy.newProxyInstance(
                WebDriver.class.getClassLoader(),
                new Class<?>[] {WebDriver.class},
                (proxy, method, args) -> "findElements".equals(method.getName()) ? elements : null);
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
