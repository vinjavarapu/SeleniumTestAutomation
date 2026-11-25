# Troubleshooting Guide

Solutions for common issues when running tests.

## Configuration Issues

### "staging requires an explicit baseUrl"

**Error:**
```
java.lang.IllegalStateException: Staging requires an explicit baseUrl via -DbaseUrl or the BASE_URL environment variable
```

**Cause:** Staging environment has no default base URL (security measure).

**Solution:** Set the URL explicitly:
```bash
mvn test -Denv=staging -DbaseUrl=https://staging.example.com
# OR
export BASE_URL=https://staging.example.com
export ENV=staging
mvn test
```

---

### "Missing configuration property"

**Error:**
```
java.lang.IllegalStateException: Missing configuration property: someKey
```

**Cause:** A required property is not set anywhere in the precedence chain.

**Solution:**
1. Check `config.properties` and `config/{env}.properties` for the key
2. Or set via JVM property: `-DsomeKey=value`
3. Or set environment variable (camelCase → UPPER_SNAKE_CASE): `SOME_KEY=value`

**Example:**
```bash
# If error is about "remoteUrl":
mvn test -DremoteUrl=http://localhost:4444
# OR
export REMOTE_URL=http://localhost:4444
```

---

### "Unsupported browser"

**Error:**
```
java.lang.IllegalArgumentException: Unsupported browser 'safari'. Choose chrome, firefox, or edge.
```

**Solution:** Use a supported browser:
```bash
mvn -Psmoke test -Dbrowser=firefox  # chrome, firefox, or edge
```

---

## Driver & Browser Issues

### "WebDriver not found"

**Error:**
```
java.lang.IllegalStateException: Driver manager org.openqa.selenium.NoSuchDriverException: WebDriver not found
```

**Cause:** WebDriverManager couldn't download the driver for your OS/browser combo.

**Solution:**
```bash
# Check your OS and browser compatibility
# Download drivers manually from https://www.selenium.dev/downloads/
# Or try running with explicit cache clearing:
mvn clean test -Psmoke
```

---

### "Only local connections are allowed"

**Error:**
```
org.openqa.selenium.remote.SessionNotCreatedException: Only local connections are allowed
```

**Cause:** Trying to connect to Selenium Grid but it's not running or not accepting remote connections.

**Solution:**
1. Start Selenium Grid locally:
   ```bash
   docker-compose -f docker-compose.grid.yml up -d
   ```

2. Or run tests locally (without Grid):
   ```bash
   mvn -Psmoke test  # Uses local drivers by default
   ```

3. Or disable Grid:
   ```bash
   mvn -Psmoke test -DremoteUrl=  # Empty URL disables Grid
   ```

---

### Browser hangs or times out

**Error:**
```
org.openqa.selenium.TimeoutException: Timed out after 10 seconds waiting for visibility of element
```

**Causes:**
- Application is slow
- Element selector is wrong
- JavaScript on page is broken

**Solutions:**
1. Increase wait timeout:
   ```bash
   mvn test -DexplicitWaitSeconds=30
   ```

2. Run headless=false to see what's happening:
   ```bash
   mvn test -Dheadless=false
   ```

3. Check browser console for JavaScript errors:
   - Captured in `target/extent-reports/Test-Report-*.html` after test fails
   - Also logged in `target/logs/automation.log`

---

## Test Data Issues

### "CSV test data resource not found"

**Error:**
```
java.lang.IllegalStateException: CSV test data resource not found: /testdata/my-data.csv
```

**Cause:** File doesn't exist or isn't in the right location.

**Solution:**
1. Verify file exists: `src/test/resources/testdata/my-data.csv`
2. Check file name matches exactly (case-sensitive on Linux/Mac)
3. Verify Maven properly copies resources:
   ```bash
   mvn clean resources:resources  # Force resource copy
   ```

---

### "Unexpected CSV headers"

**Error:**
```
java.lang.IllegalStateException: Unexpected CSV headers in /testdata/logins.csv: [user, pass]; expected [username, password]
```

**Cause:** Column headers in CSV don't match expected headers in test.

**Solution:**
1. Update CSV headers to match:
   ```csv
   username,password   # changed from user,pass
   standard_user,secret_sauce
   ```

2. Or update test data provider to match CSV headers:
   ```java
   @DataProvider(name = "logins")
   public Object[][] logins() {
       return TestDataLoader.toDataProvider(
           TestDataLoader.load("logins.csv", Login.class));
   }
   ```

---

### "Invalid path traversal detected"

**Error:**
```
java.lang.IllegalArgumentException: Invalid path traversal detected in test data file: ../../../etc/passwd
```

**Cause:** Test data loader only allows files directly in `/testdata/`, not subdirectories or parent paths.

**Solution:** Use files directly in `src/test/resources/testdata/`:
```bash
# ✓ GOOD:
TestDataLoader.load("my-data.csv", MyData.class);

# ✗ BAD:
TestDataLoader.load("../my-data.csv", MyData.class);  // Rejected
TestDataLoader.load("subdir/my-data.csv", MyData.class);  // Rejected
```

---

## Element Interaction Issues

### "Element is not clickable" or "Stale element exception"

**Error:**
```
org.openqa.selenium.StaleElementReferenceException: Element is no longer attached to the DOM
```

**Causes:**
- Element was removed from DOM (page updated)
- Element is obscured by another element (overlay, modal)
- Element is not visible yet

**Solutions:**
```java
// ✓ GOOD: Re-find the element before each action
public void selectProduct(String name) {
    By locator = By.xpath("//button[contains(text(), '" + name + "')]");
    click(locator);  // Find fresh each time
    click(locator);  // Find fresh each time
}

// ✗ BAD: Store reference
WebElement button = driver.findElement(MY_BUTTON);
button.click();
button.click();  // May be stale!
```

---

### "Element is not visible"

**Error:**
```
org.openqa.selenium.ElementNotVisibleException: Element is not visible
```

**Causes:**
- CSS `display: none`, `opacity: 0`, or off-screen
- Parent element is hidden
- Element will appear via JavaScript (not waited for)

**Solutions:**
1. Wait for element:
   ```java
   public void waitForElement() {
       visibleElement(MY_ELEMENT);  // Built-in wait
   }
   ```

2. Debug with headless=false:
   ```bash
   mvn test -Dheadless=false
   # Browser stays open; check DevTools
   ```

3. Add explicit wait for the condition:
   ```java
   public void waitForOverlayToClose() {
       WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
       wait.until(ExpectedConditions.invisibilityOfElementLocated(LOADING_OVERLAY));
   }
   ```

---

## Test Failure Issues

### Test passes locally but fails in CI

**Causes:**
- Different environment (network, timing, OS)
- Different browser version
- Race condition or flakiness
- Hard-coded waits too short

**Solutions:**
1. Check CI logs for details:
   - GitHub Actions: Check "Regression tests" job output
   - Artifacts: Extent reports, screenshots, logs

2. Increase retry count:
   ```bash
   mvn test -DretryCount=2
   ```

3. Increase wait timeout:
   ```bash
   mvn test -DexplicitWaitSeconds=20
   ```

4. Run headless=false locally to match CI environment (if possible):
   ```bash
   mvn test -Dheadless=true  # What CI uses
   ```

---

### "Test passed after retry"

**Meaning:** Test failed once, was retried per `retryCount`, and eventually passed.

**Analysis:**
1. Check `target/flaky-tests.json` for the test name
2. Investigate why it's intermittent:
   - Is there a wait needed? (`visibleElement()`, `stalenessOf()`)
   - Is there a race condition? (async loading, animations)
   - Is there environmental flakiness? (network latency, slow CI runner)

**Solutions:**
```java
// Add explicit waits
public void waitForAndClick() {
    clickableElement(MY_BUTTON);  // Wait for clickability, not just visibility
}

// Wait for page transition
public void navigateAndWait() {
    click(LINK);
    urlContains("new-page");  // Wait for URL to change
    visibleElement(NEW_PAGE_ELEMENT);  // Wait for new content
}

// Reduce flakiness with per-test retry override
@TestInfo(retries = 3)  // Extra retries for this flaky test
public void sometimesFlaky() { }
```

---

## Reporting Issues

### "Extent report not generated"

**Error:** No `Test-Report-*.html` file in `target/extent-reports/`

**Causes:**
- Tests didn't run (compile error, skip all)
- Different report directory in config

**Solutions:**
1. Check build succeeded: `mvn -Psmoke verify` (should end with `BUILD SUCCESS`)
2. Check test suite configuration:
   ```bash
   cat src/test/resources/suites/smoke.xml
   # Verify it has correct package or class names
   ```

3. Check for test compilation errors:
   ```bash
   mvn compile
   # Should list all test classes as compiled
   ```

---

### Screenshots not captured on failure

**Error:** Failure screenshot not in `target/extent-reports/Test-Report-*.html`

**Causes:**
- Test did not use `BaseTest` (no driver attached)
- Driver was closed before failure reporting

**Solution:** Ensure test extends `BaseTest`:
```java
public class MyTests extends BaseTest {  // ← Important!
    @Test
    public void myTest() { }
}
```

---

## Performance Issues

### Tests are too slow

**Common causes:**
- Running all tests serially (sequential, not parallel)
- Excessive hard-coded delays
- Network latency to SauceDemo or Grid

**Solutions:**
1. Enable parallelization:
   - `smoke.xml` runs `parallel="methods" thread-count="4"`
   - `regression.xml` is sequential (design choice for stability)

2. Remove unnecessary delays:
   ```bash
   # Find delays in logs
   grep -r "Thread.sleep" src/test/java/
   ```

3. Increase thread count:
   ```xml
   <!-- In smoke.xml -->
   <suite parallel="methods" thread-count="8">
   ```

---

## CI/CD Issues

### GitHub Actions job fails with "Process exited with code 1"

**Error:** No other details provided.

**Solution:** Check job logs for the actual error:
1. Go to GitHub repo → Actions
2. Click the failed workflow run
3. Click the job (e.g., "Regression tests")
4. Scroll down for detailed output

---

### Artifacts not uploaded

**Error:** GitHub Actions job succeeded but artifacts not in Actions tab.

**Cause:** Extent reports not generated (see [Extent report not generated](#extent-report-not-generated)).

**Solution:** Ensure tests actually run:
```yaml
# In .github/workflows/selenium-tests.yml
- name: "Run regression tests"
  run: mvn -Pregression test  # This must generate reports
```

---

## Getting Help

### Still stuck?

1. **Check logs:**
   ```bash
   cat target/logs/automation.log | grep -i error
   ```

2. **Check screenshots:**
   ```bash
   ls -la target/screenshots/
   ```

3. **Run with debug logging:**
   ```bash
   mvn test -DLOG_LEVEL=DEBUG 2>&1 | tee debug.log
   ```

4. **Search existing issues:** 
   - GitHub Issues (framework repo)
   - Selenium documentation: https://www.selenium.dev/documentation/

5. **Ask for help:**
   - Include: error message, stack trace, test name, OS, browser
   - Include: how to reproduce (command, data files)
   - Include: logs from `target/logs/`
