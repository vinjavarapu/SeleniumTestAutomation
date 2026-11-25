# Selenium Test Automation Framework - Contributor Guide

This document describes conventions, patterns, and contributing guidelines for the Selenium Test Automation framework.

## Project Structure

```
SeleniumTestAutomation/
├── src/main/java/.../framework/
│   ├── config/        - Configuration management (ConfigReader, FrameworkConfig)
│   ├── data/          - Test data layer (TestDataSource, TestDataLoader)
│   ├── driver/        - WebDriver management (DriverFactory, DriverManager)
│   ├── listeners/     - TestNG listeners, retry, and test tagging
│   ├── reporting/     - Test reporting (ExtentReports bridge)
│   ├── api/           - REST API client for test data setup
│   ├── base/          - Base test and page object classes
│   └── utils/         - Utilities (CSV reader, URL redaction, etc.)
├── src/test/java/.../
│   ├── tests/         - Test classes and centralized DataProviders
│   ├── pages/         - Page objects for SauceDemo
│   └── testdata/      - Test data records and accessors
├── src/test/resources/
│   ├── config/        - Environment-specific config overrides
│   ├── suites/        - TestNG suite XML files
│   └── testdata/      - CSV, JSON, YAML, Excel test data files
└── pom.xml           - Maven build configuration
```

## Configuration

Configuration follows a strict precedence chain:
1. JVM system properties: `-DbaseUrl=... -Dbrowser=firefox`
2. Environment variables: `BASE_URL=...`, `BROWSER=...` (camelCase → UPPER_SNAKE_CASE)
3. Environment-specific properties: `config/qa.properties`, `config/staging.properties`
4. Default properties: `config.properties`
5. Code defaults: in `FrameworkConfig` getters

**Key properties:**
- `baseUrl` (required) - Application URL
- `browser` - chrome|firefox|edge (default: chrome)
- `headless` - true|false (default: true)
- `explicitWaitSeconds` - timeout seconds (default: 10, must be > 0)
- `retryCount` - retry attempts (default: 0, must be >= 0)
- `remoteUrl` - Selenium Grid URL (optional)
- `preflightCheck` - validate URL reachability (default: false)

## Test Data

### Loading Test Data

Use `TestDataLoader` for all test data:

```java
// Load as typed objects
List<RejectedLogin> logins = TestDataLoader.load("rejected-logins.csv", RejectedLogin.class);

// Load as row maps
List<Map<String, String>> rows = TestDataLoader.loadRows("rejected-logins.json");

// Convert to TestNG data provider
Object[][] provider = TestDataLoader.toDataProvider("sort-orders.yaml");
```

### Supported Formats

- **CSV** (`rejected-logins.csv`) - Standard RFC4180 with environment variable references
- **JSON** (`rejected-logins.json`) - Array of objects
- **YAML** (`sort-orders.yaml`) - Array of maps
- **Excel** (`*.xlsx`) - First sheet, first row is headers

**All formats support environment variable references:** `${VAR_NAME}` → `System.getenv("VAR_NAME")`

### Creating Test Data Files

1. Place files in `src/test/resources/testdata/`
2. Create typed record class matching the data structure:
   ```java
   public record RejectedLogin(
       @JsonProperty("username") String username,
       @JsonProperty("password") String password,
       @JsonProperty("expectedMessage") String expectedMessage) {}
   ```
3. Add a data provider in `DataProviders` class:
   ```java
   @DataProvider(name = "rejectedLogins")
   public static Object[][] rejectedLogins() {
       return TestDataLoader.toDataProvider(
           TestDataLoader.load("rejected-logins.csv", RejectedLogin.class));
   }
   ```
4. Use in test: `@Test(dataProvider = "rejectedLogins", dataProviderClass = DataProviders.class)`

## Writing Tests

### Basic Test Structure

```java
@Test(
    groups = {TestGroups.SMOKE, TestGroups.LOGIN},
    description = "Valid user can log in"
)
@TestInfo(
    id = "SD-001",
    feature = TestGroups.LOGIN,
    severity = TestGroups.CRITICAL,
    owner = "qa-team"
)
public void validUserCanLogIn() {
    loginPage.login("standard_user", "secret_sauce");
    Assert.assertTrue(inventoryPage.isDisplayed());
}
```

### Test Tagging with @TestInfo

- **id** - Test case ID for traceability (e.g., "SD-001")
- **feature** - Feature area: use `TestGroups` constants (LOGIN, CART, CHECKOUT, etc.)
- **severity** - CRITICAL, HIGH, MEDIUM, LOW (from `TestGroups`)
- **owner** - Team or person responsible
- **retries** - Per-test retry override (default: -1 uses global `retryCount`)

**Available groups (from `TestGroups`):**
- Scope: `SMOKE`, `REGRESSION`
- Severity: `CRITICAL`, `HIGH`, `MEDIUM`, `LOW`
- Features: `LOGIN`, `CART`, `CHECKOUT`, `INVENTORY`, `PAYMENT`, `PRODUCT`

### Page Objects

```java
public class LoginPage extends BasePage {
    private static final By USERNAME_FIELD = By.id("user-name");
    private static final By PASSWORD_FIELD = By.id("password");
    private static final By LOGIN_BUTTON = By.id("login-button");

    public LoginPage login(String username, String password) {
        enterText(USERNAME_FIELD, username);
        enterText(PASSWORD_FIELD, password);
        click(LOGIN_BUTTON);
        return this;
    }

    public String getErrorMessage() {
        return readText(By.cssSelector("[data-test='error']"));
    }
}
```

### Parameterized Tests

```java
@Test(dataProvider = "rejectedLogins", dataProviderClass = DataProviders.class)
public void rejectedLoginShowsError(RejectedLogin scenario) {
    loginPage.login(scenario.username(), scenario.password());
    Assert.assertEquals(loginPage.getErrorMessage(), scenario.expectedMessage());
}
```

## Running Tests

### By Maven Profile

```bash
mvn -Punit verify          # Framework configuration tests only
mvn -Psmoke test           # Smoke tests (3 browsers, local)
mvn -Pregression test      # Full regression tests (sequential)
mvn -Pcritical test        # Critical tests only
mvn -Psecurity verify      # Dependency security scan (no tests)
mvn test                   # Default profile (all tests)
```

### By System Properties

```bash
mvn -Psmoke test \
  -DbaseUrl=https://qa.app.com \
  -Dbrowser=firefox \
  -DheadLess=false \
  -DexplicitWaitSeconds=15 \
  -DretryCount=2
```

### By Environment Variables

```bash
export BASE_URL=https://qa.app.com
export BROWSER=firefox
export HEADLESS=false
mvn -Psmoke test
```

## Reporting

### Extent Reports

After each test run, Extent reports are generated:
- **Location:** `target/extent-reports/Test-Report-*.html`
- **Contents:** Test summary, pass/fail details, screenshots, test groups, owner info

### Flaky Test Detection

After running regression tests, check `target/flaky-tests.json` for tests that passed after retry (potential flakiness indicators).

## Code Conventions

### Naming
- Test classes: `*Tests.java` or `*Test.java`
- Page objects: `*Page.java`
- Data records: `*Record.java` or just the entity name
- Variables: camelCase, descriptive names

### Locators
- Define as `private static final By` fields
- Use meaningful names: `SUBMIT_BUTTON`, `ERROR_MESSAGE`, not `ELEMENT_1`

### Waits
- Use `BasePage` helpers: `visibleElement()`, `clickableElement()`, `urlContains()`
- Avoid hard-coded `Thread.sleep()` except in edge cases (comment why)

### Logging
- Use SLF4j via Log4j; logs go to `target/logs/automation.log`
- Log test flow entry points and key assertions
- Sensitive data is automatically redacted (passwords, tokens)

### Configuration Reading
```java
// Use ConfigReader for raw string values (rarely needed)
String envValue = ConfigReader.get("key");

// Use FrameworkConfig for typed, validated values (preferred)
long timeout = FrameworkConfig.explicitWaitSeconds();
boolean headless = FrameworkConfig.headless();
```

### Data Access
```java
// Use SauceDemoData for SauceDemo constants
String username = SauceDemoData.validUsername();

// Use TestDataLoader for data-driven tests
List<RejectedLogin> scenarios = TestDataLoader.load("rejected-logins.csv", RejectedLogin.class);
```

## CI/CD Integration

GitHub Actions runs three jobs per push/PR:
1. **Unit tests** (10 min) - Framework configuration, readers, utilities
2. **Smoke tests** (15 min) - 3 browsers (Chrome, Firefox, Edge) × 2 environments
3. **Regression tests** (30 min) - Firefox, full test coverage, sequential

Artifacts retained 30 days:
- Surefire XML reports
- Extent HTML reports
- Test logs and screenshots
- Flaky test JSON

## Troubleshooting

### Test runs locally but fails in CI
- Check environment-specific config: `config/qa.properties`, `config/staging.properties`
- Verify environment variables are set in CI: `BASE_URL`, `BROWSER`, `HEADLESS`
- Check if test relies on hard-coded delays or flaky selectors

### "staging requires an explicit baseUrl"
- Set `-DbaseUrl=...` or `BASE_URL=...` env var when running against staging

### Data file not found
- Ensure file is in `src/test/resources/testdata/`
- Check file extension matches supported format: .csv, .json, .yaml, .xlsx

### Stale element or flakiness
- Use explicit waits: `visibleElement()`, `stalenessOf()`
- Avoid storing WebElement references across actions; find fresh each time

## Resources

- **Framework Design:** [OnlineHelp/Framework_Design_Document.md](OnlineHelp/Framework_Design_Document.md)
- **Concurrent Execution:** [OnlineHelp/Concurrent_Execution_Design.md](OnlineHelp/Concurrent_Execution_Design.md)
- **Existing Tests:** `src/test/java/.../tests/`
- **API Client:** `src/main/java/.../framework/api/ApiClient.java`
