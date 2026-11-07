# 👋 Hi, I'm Ravi Kumar
### Senior Quality Engineering Lead & Test Automation Architect

A seasoned QA Leader with **17+ years of IT experience** specializing in architecting comprehensive test strategies and automation frameworks for mission-critical enterprise systems. Expert at driving quality assurance at scale, with deep focus on enterprise-grade test automation frameworks, cloud integration, and release readiness.

---

### 🛠️ Core Expertise & Technical Arsenal
* **Framework Design:** Selenium WebDriver, RestAssured, Page Object Model (POM)
* **CI/CD Integration:** GitHub Actions automated regression workflows
* **Methodologies:** Architecture Strategy, Agile Leadership, Risk Mitigation, Enterprise Release Readiness

---

# Project layout

```text
src/test/java/org/example/Framework/
   base/BaseTest.java               Browser lifecycle, simplified with listener-based reporting
   base/BasePage.java               Shared explicit waits and page actions
   config/FrameworkConfig.java       Typed configuration with env-aware property files
   driver/DriverManager.java       Thread-local WebDriver access (parallel-safe)
   driver/DriverFactory.java        Browser driver creation (local or remote)
   listeners/TestListener.java      Extent reporting, screenshots, pass/fail handling
   listeners/RetryAnalyzer.java     Retry flaky tests automatically
   listeners/RetryTransformer.java  Apply retry logic to all @Test methods
   pages/                           Login, inventory, cart, and checkout pages
   utils/TestDataReader.java        Reads test data from the classpath
src/test/java/org/example/Framework/Tests/
   LoginTests.java                  Valid and invalid login scenarios (smoke)
   CheckoutTests.java               Login-to-order-confirmation journey (regression)
   InventoryCartTests.java          Inventory and cart scenarios (regression)
   CatalogueRegressionTests.java    Coverage based on the SauceDemo test catalogue (regression)
   Smoketest.java                   Basic smoke test (smoke)
src/test/resources/
   config/
      default.properties            Default configuration (baseUrl, browser, timeouts, retry)
      qa.properties                QA environment overrides
      staging.properties           Staging environment overrides
   suites/
      smoke.xml                     Smoke test suite (2 parallel threads)
      regression.xml                Regression suite (3 parallel threads)
      all.xml                       All tests (3 parallel threads)
```
