# Framework Architecture

This document describes the architecture and key design decisions of the Selenium Test Automation framework.

## Overview

The framework is built on:
- **Selenium 4.52** for browser automation
- **TestNG 7.11** for test execution and parallelization
- **ExtentReports 5.1** for HTML reporting
- **WebDriverManager 6.4** for automatic driver management
- **Jackson** for JSON/YAML data serialization
- **Apache POI** for Excel test data support

## Layer Architecture

### 1. Configuration Layer (Framework `config/`)

**Purpose:** Centralized, environment-aware configuration with type safety.

**Components:**
- `ConfigReader`: Loads properties with precedence (system property → env var → config file → default)
- `FrameworkConfig`: Typed getters with validation (boolean, int, long, duration)
- `EnvironmentPreflight`: Pre-test URL reachability validation

**Key Design:**
- Env vars are dynamically derived from camelCase keys (e.g., `baseUrl` → `BASE_URL`)
- Typed helpers eliminate duplicated parsing across getters
- Validation happens at startup, not scattered throughout the code

### 2. Driver Management Layer (Framework `driver/` & `base/`)

**Purpose:** Isolate parallel test execution with thread-local WebDriver storage.

**Components:**
- `DriverFactory`: Creates Chrome, Firefox, or Edge drivers locally or on Selenium Grid
- `DriverManager`: Thread-local WebDriver storage for concurrent test isolation
- `BaseTest`: JUnit-style `@BeforeMethod` / `@AfterMethod` for browser lifecycle
- `BasePage`: Base class for all page objects, with explicit wait helpers

**Key Design:**
- Thread-local drivers prevent cross-test interference in parallel runs
- One driver per test thread, cleaned up automatically after each test
- WebDriverManager handles driver binaries (no manual setup needed)

### 3. Test Data Layer (Framework `data/` & Test-side `tests/testdata/`)

**Purpose:** Flexible, multi-format test data with typed mapping.

**Components:**
- `TestDataSource` (interface): Abstraction for format-specific readers
- `CsvDataSource`, `JsonDataSource`, `YamlDataSource`, `ExcelDataSource`: Format implementations
- `TestDataLoader`: Public API, auto-detects format, converts to typed objects or data providers
- `PathValidator`, `EnvironmentVariableResolver`: Shared safety utilities

**Test-side:**
- `DataProviders`: Centralized TestNG data providers for all test classes
- Test data records (`RejectedLogin`, `CheckoutValidation`, etc.): Typed POJO mapping via Jackson
- `SauceDemoData`: Constants accessor for SauceDemo fixture values

**Key Design:**
- Format detection by file extension: auto-picks the right reader
- Environment variable references (`${VAR_NAME}`) resolved in every format
- Typed records (via `@JsonProperty`) ensure compile-time safety
- DataProviders class centralizes all providers, replacing per-test scattered logic

### 4. Listeners & Reporting Layer (Framework `listeners/` & `reporting/`)

**Purpose:** Test orchestration, retry logic, and reporting hooks.

**Components:**
- `TestListener`: Implements `ITestListener` and `ISuiteListener`
  - Derives test categories from groups and `@TestInfo` annotation
  - Captures failure screenshots and diagnostics
  - Redacts sensitive parameters
- `TestInfo`: Annotation for per-test metadata (ID, feature, severity, owner, retries)
- `RetryAnalyzer`: Implements `IRetryAnalyzer`, honors per-test retries from `@TestInfo`
- `RetryTransformer`: Annotates every test method with `RetryAnalyzer`
- `TestStepReporter`: Decouples page objects from reporting library (pluggable consumer)
- `ExtentReportManager`: Wrapper over Extent Reports with screenshot attachment

**Key Design:**
- Retry logic is centralized and keyed per method+parameters, isolated per thread
- Per-test retries can override global `retryCount` via `@TestInfo(retries=2)`
- TestListener reads `@TestInfo` to populate report categories, ID, owner, severity
- Test step reporting is decoupled from the reporting implementation

### 5. API Testing Support (Framework `api/`)

**Purpose:** Optional REST API client for test data setup and validation.

**Components:**
- `ApiClient`: Thin wrapper over `java.net.http.HttpClient`
  - Methods: `get()`, `postJson()`, `putJson()`, `delete()`
  - Returns `ApiResponse` record (statusCode, body, headers)
  - 15s default timeout, URL redaction in logs
- `UrlRedactor`: Masks sensitive parts of URLs (tokens, paths)

**Key Design:**
- Intentionally minimal; extends easily for custom headers, auth, retry
- Currently unused in SauceDemo tests (no API for user/inventory management)
- Ready for client applications with actual APIs

### 6. Utilities (Framework `utils/`)

**Purpose:** Common helpers for waiting, test data, screenshots, etc.

**Components:**
- `CsvTestDataReader`: Legacy CSV parser (kept for backward compatibility)
- `TestDataReader`: Properties file accessor with caching
- `WaitHelper`: Wrapper over Selenium `WebDriverWait`
- `ExtentReportManager`: Extent Reports wrapper
- `BrowserDiagnostics`: Captures browser console logs, localStorage, etc.
- `UrlRedactor`: URL sanitization for logs

## Concurrency Model

### Parallel Execution

- **Thread Pool:** Configured per suite (e.g., `smoke.xml` runs `parallel="methods" thread-count="2"`)
- **Driver Isolation:** Each thread has its own WebDriver via `DriverManager.getDriver()` (thread-local)
- **Data Isolation:** Retry tracking is keyed per method+parameters, isolated per thread
- **Listener Isolation:** Log4j ThreadContext and TestNG thread context separate test names

### Retry Logic

1. `RetryTransformer` registers `RetryAnalyzer` on every test method at class-load time
2. On failure, `RetryAnalyzer.retry()` is called:
   - Checks per-test `@TestInfo(retries=N)` or falls back to global `retryCount`
   - Tracks attempts per (context, method, parameters) key
   - If retries remain, returns `true` (TestNG re-runs the test)
3. `TestListener` checks `RetryAnalyzer.consumeRetriedOutcome()` to report "passed after retry"

## Test Hierarchy

```
Tests (data-driven & single-method)
├── SmokeTest: Quick validation, runs on all browsers
├── LoginTests: Login scenarios, smoke + regression
├── CheckoutTests: Purchase flow, smoke + regression
├── InventoryCartTests: Product catalog & cart, regression
└── CatalogueRegressionTests: ~40 data-driven scenarios, regression

Page Objects
├── LoginPage: User login, error messages
├── InventoryPage: Product list, sorting, filters
├── ProductDetailsPage: Product details & add-to-cart
├── CartPage: Cart items, quantity, total
├── CheckoutInformationPage: User info entry
├── CheckoutOverviewPage: Order summary
└── CheckoutCompletePage: Order confirmation

Test Data
├── SauceDemoData: Constants (valid.username, product.name, etc.)
├── DataProviders: Centralized @DataProvider methods
└── Test data files:
    ├── rejected-logins.csv/json
    ├── sort-orders.csv/yaml
    └── checkout-validation.csv
```

## Configuration File Precedence

```
System Property (-DbaseUrl=...)                         [Highest]
  ↓
Environment Variable (BASE_URL=...)
  ↓
Environment Config (config/qa.properties, config/staging.properties)
  ↓
Default Config (config.properties)
  ↓
Code Default (FrameworkConfig.baseUrl() default param) [Lowest]
```

**Example:** To override base URL for a specific test run:
```bash
mvn -Psmoke test -DbaseUrl=https://custom.com
```

## Reporting & Artifacts

### Extent Reports
- Generated after each test run: `target/extent-reports/Test-Report-*.html`
- Includes:
  - Test summary (passed/failed/skipped)
  - Individual test details with groups, description, ID, owner
  - Screenshots on failure
  - Console logs and stack traces

### Test Logs
- Output to `target/logs/automation.log` (rolling file, 10 MB per file)
- Format: `[timestamp] [thread] [level] [class] [testName] message`
- Sensitive parameters (passwords, tokens) automatically redacted

### Screenshots
- Saved to `target/screenshots/` on test failure
- File naming: `{test-name}-{unique-hash}.png`
- Also attached to Extent report

## Extension Points

### Adding a New Configuration Property

1. Add to `config.properties` or `config/{env}.properties`
2. Add getter to `FrameworkConfig`:
   ```java
   public static String myNewProperty() {
       return ConfigReader.getOptional("myNewProperty", "default");
   }
   ```
3. For typed values, use: `ConfigReader.getBoolean()`, `getInt()`, `getLong()`, `getDuration()`

### Adding a New Test Data Format

1. Create a class implementing `TestDataSource`:
   ```java
   class XmlDataSource implements TestDataSource {
       @Override
       public List<Map<String, String>> rows(String fileName) { /* parse XML */ }
   }
   ```
2. Register in `TestDataLoader.selectSourceByExtension()`:
   ```java
   case "xml" -> new XmlDataSource();
   ```

### Adding a New Page Object

1. Create class extending `BasePage`:
   ```java
   public class MyPage extends BasePage {
       private static final By ELEMENT = By.id("element-id");
       
       public MyPage doSomething() {
           click(ELEMENT);
           return this;
       }
   }
   ```
2. Use in tests:
   ```java
   @Test
   public void myTest() {
       MyPage page = new MyPage();
       page.doSomething();
   }
   ```

## Security Considerations

- **Credentials:** Never commit passwords or tokens; use environment variables or external vaults
- **Parameter Redaction:** Automatically redacts method parameters matching `password|secret|token|credential`
- **URL Masking:** Logs redact sensitive URL parts (tokens, auth headers)
- **Dependency Scanning:** `mvn -Psecurity verify` runs OWASP Dependency-Check on every build

## Future Considerations

- **Allure Reports:** Planned optional integration (pending Java 25 AspectJ compatibility)
- **Cloud Execution:** Framework is ready for distributed runs via Selenium Grid
- **Performance Metrics:** TestNG listener can track test duration and generate trend reports
- **Custom Hooks:** TestNG listeners can be extended for custom on-test-start, on-test-end logic
