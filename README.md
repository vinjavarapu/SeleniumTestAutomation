# Selenium Java Automation Framework

Enterprise-grade Maven, Selenium, and TestNG framework using SauceDemo as the practice e-commerce application.

## Project layout

```text
src/test/java/org/example/framework/
  base/BaseTest.java              Browser lifecycle, simplified with listener-based reporting
  base/BasePage.java              Shared explicit waits and page actions
  config/FrameworkConfig.java     Typed configuration with env-aware property files
  driver/DriverManager.java       Thread-local WebDriver access (parallel-safe)
  driver/DriverFactory.java       Browser driver creation (local or remote)
  listeners/TestListener.java     Extent reporting, screenshots, pass/fail handling
  listeners/RetryAnalyzer.java    Retry flaky tests automatically
  listeners/RetryTransformer.java Apply retry logic to all @Test methods
  pages/                          Login, inventory, cart, and checkout pages
  utils/TestDataReader.java       Reads test data from the classpath
src/test/java/org/example/framework/tests/
  LoginTests.java                 Valid and invalid login scenarios (smoke)
  CheckoutTests.java              Login-to-order-confirmation journey (regression)
  InventoryCartTests.java         Inventory and cart scenarios (regression)
  CatalogueRegressionTests.java   Coverage based on the SauceDemo test catalogue (regression)
  SmokeTest.java                  Basic smoke test (smoke)
src/test/resources/
  config/
    default.properties            Default configuration (baseUrl, browser, timeouts, retry)
    qa.properties                 QA environment overrides
    staging.properties            Staging environment overrides
  suites/
    smoke.xml                     Smoke test suite (2 parallel threads)
    regression.xml                Regression suite (3 parallel threads)
    all.xml                       All tests (3 parallel threads)
  log4j2.xml                      Console and rolling-file logging
  testdata/saucedemo.properties   Public demo credentials and checkout data
.github/workflows/
  selenium-tests.yml              CI: smoke (PR, matrix: chrome+firefox), regression (main, chrome)
```

**Enterprise features:**
- **Typed configuration**: Per-environment property files (default, qa, staging) with system-property overrides
- **Parallel execution**: Suites define thread counts; `-Psmoke` and `-Pregression` profiles select subsets
- **Retry mechanism**: Failed tests auto-retry (configurable, default 1 attempt)
- **Listener-based reporting**: Extent reports generated via TestListener; screenshot attachment on failure
- **Modular driver factory**: Local or RemoteWebDriver (Grid/Docker ready)
- **CI/CD matrix**: GitHub Actions runs smoke tests on all PRs (Chrome + Firefox), regression on main/nightly

## Configuration

Configuration precedence (highest to lowest):
1. System properties (`-Dbrowser=firefox`)
2. Environment-specific file (`src/test/resources/config/{env}.properties`, selected by `-Denv=qa`)
3. Default file (`src/test/resources/config/default.properties`)

**Available properties:**
- `baseUrl`: Application root (default: `https://www.saucedemo.com/`)
- `browser`: chrome, firefox, or edge (default: chrome)
- `headless`: true/false (default: true)
- `explicitWaitSeconds`: WebDriverWait timeout (default: 10)
- `implicitWaitSeconds`: Implicit wait (default: 0 — explicit waits preferred)
- `retryCount`: Max test retries on failure (default: 1)
- `remoteUrl`: Selenium Grid URL for RemoteWebDriver (default: empty — uses local)
- `reportName`: Extent report title (default: Selenium Test Execution Report)

## Run the suite

**All tests (default profile):**
```bash
mvn clean test
```

**Smoke tests (2 threads):**
```bash
mvn clean test -Psmoke
```

**Regression tests (3 threads):**
```bash
mvn clean test -Pregression
```

**Override configuration at runtime:**
```bash
mvn clean test -Psmoke -Denv=staging -Dbrowser=firefox -Dheadless=false
```

**Use a remote driver (Selenium Grid):**
```bash
mvn clean test -Pregression -DremoteUrl=http://grid.example.com:4444 -Dbrowser=chrome
```

**Increase retries on flaky environments:**
```bash
mvn clean test -Pregression -DretryCount=2
```

Logs appear in console and in `target/logs/automation.log`. Test reports (with screenshots and detailed steps) are in `target/extent-reports/Test-Report-*.html`. Maven's `target/` directory is generated and ignored by Git.

## Tests and groups

Tests are tagged with groups for filtering:
- `groups = "smoke"`: LoginTests, SmokeTest (quick validation)
- `groups = "regression"`: CatalogueRegressionTests, InventoryCartTests, CheckoutTests (comprehensive)

Run a specific group:
```bash
mvn test -Dgroups=smoke
```

The test catalogue is in `SauceDemo_Selenium_Java_Test_Cases.pdf`. Regression coverage includes login validation, inventory and sorting, product details, cart contents and removal, checkout validation and totals, order confirmation, navigation, session behavior, responsive inventory, keyboard traversal, and multi-item checkout.
