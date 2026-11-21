# 👋 Hi, I'm Ravi Kumar
### Senior Quality Engineering Lead & Test Automation Architect

A seasoned QA Leader with **17+ years of IT experience** specializing in architecting comprehensive test strategies and automation frameworks for mission-critical enterprise systems. Expert at driving quality assurance at scale, with deep focus on enterprise-grade test automation frameworks, cloud integration, and release readiness.

---

### 🛠️ Core Expertise & Technical Arsenal
* **Framework Design:** Selenium WebDriver, RestAssured, Page Object Model (POM)
* **CI/CD Integration:** GitHub Actions automated regression workflows
* **Methodologies:** Architecture Strategy, Agile Leadership, Risk Mitigation, Enterprise Release Readiness

---

## Selenium Java Automation Framework

Maven, Selenium, and TestNG framework using SauceDemo as the practice e-commerce application.

## Project layout

```text
src/test/java/org/example/framework/
  base/BaseTest.java              Browser lifecycle, simplified with listener-based reporting
  base/BasePage.java              Shared page actions backed by explicit waits
  config/ConfigReader.java        Central config.properties + environment overrides
  config/FrameworkConfig.java     Typed configuration access
  driver/DriverManager.java       Thread-local WebDriver access (parallel-safe)
  driver/DriverFactory.java       Browser driver creation (local or remote)
  listeners/TestListener.java     Extent reporting, screenshots, pass/fail handling
  listeners/RetryAnalyzer.java    Optional retries after the first attempt (disabled by default)
  listeners/RetryTransformer.java Apply retry logic to all @Test methods
  pages/                          Login, inventory, cart, and checkout pages
  utils/TestDataReader.java       Reads test data from the classpath
  utils/WaitHelper.java           Reusable explicit-wait conditions
src/test/java/org/example/framework/tests/
  LoginTests.java                 Valid and invalid login scenarios (smoke)
  CheckoutTests.java              Login-to-order-confirmation journey (regression)
  InventoryCartTests.java         Inventory and cart scenarios (regression)
  CatalogueRegressionTests.java   Coverage based on the SauceDemo test catalogue (regression)
  SmokeTest.java                  Basic smoke test (smoke)
src/test/resources/
  config.properties              Non-secret defaults (baseUrl, browser, timeouts, retry)
  config/
    qa.properties                 QA environment overrides
    staging.properties            Staging environment overrides
  suites/
    smoke.xml                     Smoke test suite (2 parallel threads)
    regression.xml                Regression suite (3 parallel threads)
    all.xml                       All tests (3 parallel threads)
  log4j2.xml                      Console and rolling-file logging
  testdata/saucedemo.properties   Public demo credentials and checkout data
.github/workflows/
  selenium-tests.yml              CI: smoke and Chrome regression on PRs, main pushes, and nightly runs
```

**Enterprise features:**
- **Typed configuration**: Central defaults, environment overrides (qa, staging), and system-property overrides
- **Parallel execution**: Suites define thread counts; `-Psmoke` and `-Pregression` profiles select subsets
- **Retry mechanism**: Configurable retries after the first attempt (default: 0 retries, 1 total attempt)
- **Listener-based reporting**: Extent reports generated via TestListener; screenshot attachment on failure
- **Modular driver factory**: Local or RemoteWebDriver (Grid/Docker ready)
- **CI coverage**: Pull requests run smoke tests on Chrome and Firefox plus the regression suite on Chrome; pushes to `main` and nightly scheduled runs also run both profiles. Staging jobs are enabled by `STAGING_BASE_URL`.

## Concurrent and multi-environment execution

TestNG parallelizes methods within each run, with a new thread-local WebDriver per test. Run each environment/browser combination in a separate Maven process. CI uses this model for QA/staging and Chrome/Firefox smoke coverage. CI retries are disabled (`retryCount=0`), so a first-attempt failure gates the run. Set the `STAGING_BASE_URL` GitHub Actions repository variable to enable staging jobs. Browser and driver versions are written to workflow logs; hosted runner updates can change them. See [`OnlineHelp/Concurrent_Execution_Design.md`](OnlineHelp/Concurrent_Execution_Design.md) for capacity, isolation, and Grid guidance.

## Configuration

Configuration precedence (highest to lowest):
1. System properties (`-Dbrowser=firefox`)
2. Environment-specific file (`src/test/resources/config/{env}.properties`, selected by `-Denv=qa`)
3. Defaults in `src/test/resources/config.properties`

**Available properties:**
- `baseUrl`: Application root (default: `https://www.saucedemo.com/`)
- `browser`: chrome, firefox, or edge (default: edge in `config.properties`)
- `headless`: true/false (default: true)
- `explicitWaitSeconds`: WebDriverWait timeout (default: 10)
- `retryCount`: Additional retries after the initial attempt (default: 0; `2` means up to 3 total attempts)
- `remoteUrl`: Selenium Grid URL for RemoteWebDriver (default: empty — uses local)
- `reportName`: Extent report title (default: Selenium Test Execution Report)

The framework uses explicit waits only; `WaitHelper` centralizes presence, visibility, clickability, URL, and custom conditions. There are no fixed `Thread.sleep` calls or implicit waits. Public SauceDemo fixtures are checked in under `testdata/saucedemo.properties`. For private test environments, provide values at runtime with `TESTDATA_<KEY>` environment variables (for example, `TESTDATA_VALID_USERNAME` and `TESTDATA_VALID_PASSWORD`) and use CI secrets to populate them. Do not commit private credentials in property files. Set the GitHub Actions repository variable `CHROME_VERSION` to a specific Chrome for Testing version to hold Chrome regression runs at that version; update it deliberately after reviewing a browser upgrade. If unset, CI uses the moving stable channel.

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

**Enable retries for local diagnosis:**
```bash
mvn clean test -Pregression -DretryCount=2
```

Logs appear in console and in `target/logs/automation.log`. Test reports (with screenshots and detailed steps) are in `target/extent-reports/Test-Report-*.html`; TestNG/Surefire XML reports are in `target/surefire-reports/`. Maven's `target/` directory is generated and ignored by Git.

## Tests and groups

Tests are tagged with groups for filtering:
- `groups = "smoke"`: LoginTests, SmokeTest (quick validation)
- `groups = "regression"`: CatalogueRegressionTests, InventoryCartTests, CheckoutTests (comprehensive)

Run a specific group:
```bash
mvn test -Dgroups=smoke
```

The test catalogue is in [`SauceDemo_Selenium_Java_Test_Cases.pdf`](OnlineHelp/SauceDemo_Selenium_Java_Test_Cases.pdf). Regression coverage includes login validation, inventory and sorting, product details, cart contents and removal, checkout validation and totals, order confirmation, navigation, session behavior, responsive inventory, keyboard traversal, and multi-item checkout. ROI, pass-rate, timeline, and reliability figures in portfolio materials are illustrative estimates unless supported by measured run data; this repository does not contain such measurements.
