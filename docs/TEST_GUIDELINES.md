# Writing Tests - Best Practices Guide

This guide walks you through writing tests that follow the framework's patterns and best practices.

## Quick Start: Your First Test

### Step 1: Create a Page Object

```java
package io.github.vinjavarapu.seleniumtestautomation.tests.pages;

import io.github.vinjavarapu.seleniumtestautomation.framework.base.BasePage;
import org.openqa.selenium.By;

public class MyPage extends BasePage {
    private static final By MY_BUTTON = By.id("my-button");
    private static final By MY_RESULT = By.id("result");

    public MyPage clickMyButton() {
        click(MY_BUTTON);
        return this;
    }

    public String getResult() {
        return readText(MY_RESULT);
    }
}
```

### Step 2: Create a Test Class

```java
package io.github.vinjavarapu.seleniumtestautomation.tests;

import io.github.vinjavarapu.seleniumtestautomation.framework.base.BaseTest;
import io.github.vinjavarapu.seleniumtestautomation.framework.listeners.TestGroups;
import io.github.vinjavarapu.seleniumtestautomation.framework.listeners.TestInfo;
import io.github.vinjavarapu.seleniumtestautomation.tests.pages.MyPage;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = {TestGroups.SMOKE, TestGroups.REGRESSION})
public class MyTests extends BaseTest {
    @Test(
        groups = TestGroups.LOGIN,
        description = "Button click produces expected result"
    )
    @TestInfo(
        id = "MY-001",
        feature = TestGroups.LOGIN,
        severity = TestGroups.CRITICAL,
        owner = "qa-team"
    )
    public void buttonClickProducesResult() {
        MyPage page = new MyPage();
        page.clickMyButton();
        String result = page.getResult();
        Assert.assertEquals(result, "Expected Result");
    }
}
```

### Step 3: Register in Test Suite

Add to `src/test/resources/suites/smoke.xml`:
```xml
<suite name="Smoke Tests">
    <test name="Smoke">
        <groups>
            <run>
                <include name="smoke"/>
            </run>
        </groups>
        <packages>
            <package name="io.github.vinjavarapu.seleniumtestautomation.tests"/>
        </packages>
    </test>
</suite>
```

### Step 4: Run It

```bash
mvn -Psmoke test   # Runs your new test
```

---

## Page Object Best Practices

### 1. Locators

```java
// ✓ GOOD: Descriptive names, static final, organized at top
private static final By SUBMIT_BUTTON = By.id("submit");
private static final By ERROR_MESSAGE = By.cssSelector("[data-test='error']");
private static final By PRODUCT_ROW_TEMPLATE = By.xpath("//tr[contains(td, '{}')]");

// ✗ BAD: Vague names, inline, repeated definitions
click(By.id("btn"));
click(By.id("btn")); // copied to 3 other methods
```

### 2. Element Actions

```java
// ✓ GOOD: Use BasePage helpers (built-in waits)
public MyPage enterUsername(String username) {
    enterText(USERNAME_FIELD, username);  // Waits for visibility
    return this;
}

public String getErrorMessage() {
    return readText(ERROR_MESSAGE);  // Waits for visibility
}

// ✗ BAD: Direct driver calls, no waits
public void enterUsername(String username) {
    driver.findElement(USERNAME_FIELD).sendKeys(username);  // No wait!
}
```

### 3. Fluent Interface

```java
// ✓ GOOD: Chaining for readability
loginPage
    .enterUsername("user")
    .enterPassword("pass")
    .clickLogin()
    .verifyInventoryLoaded();

// ✗ BAD: Procedural, no chaining
loginPage.enterUsername("user");
loginPage.enterPassword("pass");
loginPage.clickLogin();
// ... forgot to verify?
```

### 4. Locator Parametrization

```java
// ✓ GOOD: Template locators for dynamic elements
private static final String PRODUCT_ROW_XPATH = "//tr[contains(td, '%s')]";

public ProductRow selectProduct(String name) {
    By locator = By.xpath(String.format(PRODUCT_ROW_XPATH, name));
    click(locator);
    return this;
}

// ✗ BAD: Hard-coded for single product
private static final By PRODUCT_A = By.xpath("//tr[contains(td, 'Product A')]");
private static final By PRODUCT_B = By.xpath("//tr[contains(td, 'Product B')]");
// ... need new page object for each product
```

---

## Test Class Best Practices

### 1. Test Naming

```java
// ✓ GOOD: Describes what, not how
public void validUserCanLogIn() { }
public void invalidPasswordShowsError() { }
public void emptyPasswordFieldShowsValidationError() { }

// ✗ BAD: Vague or implementation-specific
public void testLogin() { }
public void test1() { }
public void loginErrorTest() { }
```

### 2. Test Tagging

```java
// ✓ GOOD: Complete metadata
@Test(groups = TestGroups.REGRESSION)
@TestInfo(
    id = "LOGIN-001",
    feature = TestGroups.LOGIN,
    severity = TestGroups.CRITICAL,
    owner = "alice@example.com"
)
public void validUserCanLogIn() { }

// ✗ BAD: Incomplete or missing
@Test
public void validUserCanLogIn() { }
```

### 3. Assertions

```java
// ✓ GOOD: Specific, with context
Assert.assertEquals(loginPage.getErrorMessage(), "Invalid password");
Assert.assertTrue(inventoryPage.isProductVisible("Backpack"), 
    "Product should be visible after login");

// ✗ BAD: Vague, easy to confuse
Assert.assertTrue(result);  // result of what?
Assert.assertFalse(error);  // which error?
```

### 4. Single Responsibility

```java
// ✓ GOOD: One thing per test
@Test
public void validUserCanLogIn() {
    loginPage.login("standard_user", "secret_sauce");
    Assert.assertTrue(inventoryPage.isDisplayed());
}

// ✗ BAD: Multiple unrelated scenarios
@Test
public void loginAndCheckout() {
    loginPage.login("standard_user", "secret_sauce");
    inventoryPage.selectProduct("Backpack");
    cartPage.checkout();
    checkoutPage.enterInfo("John", "Doe", "12345");
    // ... 20 more lines, hard to debug if any step fails
}
```

---

## Data-Driven Testing

### 1. Create a Test Data Record

```java
// src/test/java/.../framework/tests/testdata/LoginScenario.java
package io.github.vinjavarapu.seleniumtestautomation.framework.tests.testdata;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record LoginScenario(
        @JsonProperty("username") String username,
        @JsonProperty("password") String password,
        @JsonProperty("expectedMessage") String expectedMessage) {
    
    @JsonCreator
    public LoginScenario { }
}
```

### 2. Create Test Data File

```csv
# src/test/resources/testdata/login-scenarios.csv
username,password,expectedMessage
invalid-user,secret_sauce,do not match
standard_user,wrong_pass,do not match
,secret_sauce,Username is required
```

Or JSON:
```json
[
  {"username": "invalid-user", "password": "secret_sauce", "expectedMessage": "do not match"},
  {"username": "standard_user", "password": "wrong_pass", "expectedMessage": "do not match"}
]
```

### 3. Add DataProvider

```java
// In DataProviders class
@DataProvider(name = "loginScenarios")
public static Object[][] loginScenarios() {
    return TestDataLoader.toDataProvider(
        TestDataLoader.load("login-scenarios.csv", LoginScenario.class));
}
```

### 4. Write Parameterized Test

```java
@Test(dataProvider = "loginScenarios", dataProviderClass = DataProviders.class)
public void invalidCredentialsShowError(LoginScenario scenario) {
    loginPage.login(scenario.username(), scenario.password());
    Assert.assertEquals(loginPage.getErrorMessage(), scenario.expectedMessage());
}
```

---

## Waiting Strategies

### 1. Use Built-in Helpers

```java
// ✓ GOOD: Framework handles waits
public void waitForElement() {
    visibleElement(MY_ELEMENT);  // Waits up to explicitWaitSeconds
    clickableElement(MY_BUTTON); // Waits for clickability
    urlContains("dashboard");    // Waits for URL to contain text
}

// ✗ BAD: Hard-coded delays
Thread.sleep(2000);  // Flaky, slow, bad practice
```

### 2. For Stale Elements

```java
// ✓ GOOD: Re-find the element
public List<String> getVisibleProductNames() {
    List<WebElement> products = driver.findElements(PRODUCT_ITEM);
    List<String> names = new ArrayList<>();
    for (int i = 0; i < products.size(); i++) {
        WebElement product = driver.findElements(PRODUCT_ITEM).get(i);  // Re-find
        names.add(product.getText());
    }
    return names;
}

// ✗ BAD: Store element reference
List<WebElement> products = driver.findElements(PRODUCT_ITEM);
for (WebElement product : products) {
    names.add(product.getText());  // May throw StaleElementException if page updated
}
```

---

## Test Isolation

### 1. Setup & Teardown

```java
// ✓ GOOD: Clear state between tests
@BeforeMethod
public void setupTest() {
    loginPage = new LoginPage();
    loginPage.navigateTo();  // Go to known state
}

@AfterMethod
public void teardownTest() {
    if (FrameworkConfig.headless() == false) {
        // For debugging: keep window open in non-headless mode
    } else {
        // Browser closed automatically by BaseTest
    }
}

// ✗ BAD: Relying on previous test state
@Test
public void test2() {
    // This assumes test1 ran first and left the page logged in
    inventoryPage.selectProduct("Backpack");  // Will fail if run in isolation!
}
```

### 2. No Cross-Test Dependencies

```java
// ✓ GOOD: Each test is independent
@Test(dependsOnMethods = {}) // No dependencies
public void test1() { }

@Test(dependsOnMethods = {})
public void test2() { }

// ✗ BAD: Explicit dependencies (breaks parallelization)
@Test
public void test1() { }

@Test(dependsOnMethods = "test1")  // This test can't run in parallel
public void test2() { }
```

---

## Debugging & Troubleshooting

### 1. Enable Debug Logging

```bash
# Log at DEBUG level
mvn -Psmoke test -DLOG_LEVEL=DEBUG
```

### 2. Run Locally in Non-Headless Mode

```bash
mvn -Psmoke test -Dheadless=false
```

Browser stays open for 30 seconds after each test for inspection.

### 3. Check Failure Evidence

After test failure:
- Screenshots: `target/screenshots/`
- Logs: `target/logs/automation.log`
- Browser console: Captured in Extent report

### 4. Flaky Test Investigation

If a test passes after retry:
- Check `target/flaky-tests.json` for recurrence patterns
- Look for race conditions: add explicit waits for the flaky element
- Check if test is too fast: add verification that all async operations complete

---

## Common Mistakes

| Mistake | Impact | Fix |
|---------|--------|-----|
| `driver.findElement()` directly | No wait, fails intermittently | Use `BasePage` helpers |
| Storing WebElement references | Stale element exceptions | Re-find before each action |
| Hard-coded `Thread.sleep()` | Slow, flaky | Use explicit waits |
| Test depends on previous test | Can't run in parallel | Make each test independent |
| Test does multiple scenarios | Hard to debug, fails on first issue | One scenario per test |
| No @TestInfo annotation | No owner, severity tracking | Add metadata for reporting |
| Sensitive data in assertions | Logged in plain text | Use masking or env vars |
| Locator defined inline | Copy-paste errors | Define as static final at class level |

---

## Checklist: Before Pushing Your Tests

- [ ] Test runs locally: `mvn -Psmoke test`
- [ ] Test passes when run in isolation
- [ ] Test passes when run with other tests (no cross-dependencies)
- [ ] Page objects have descriptive method names (what, not how)
- [ ] Test method has `@Test`, `@TestInfo`, and proper groups
- [ ] Test class extends `BaseTest` or configures driver properly
- [ ] No hard-coded delays (`Thread.sleep()`)
- [ ] All assertions have clear failure messages
- [ ] No sensitive data (passwords) in test code
- [ ] Data-driven? Use `DataProviders` and `TestDataLoader`
- [ ] Checkstyle passes: `mvn checkstyle:check`

---

## Resources

- [Architecture Guide](ARCHITECTURE.md) - Design and extension points
- [CLAUDE.md](../CLAUDE.md) - Framework conventions and project structure
- [Troubleshooting](TROUBLESHOOTING.md) - Common issues and solutions
