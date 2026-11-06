# Selenium Java Automation Framework

Incremental Maven, Selenium, and TestNG framework using SauceDemo as the practice e-commerce application.

## Project layout

```text
src/test/java/org/example/framework/
  base/BaseTest.java              Browser lifecycle and common logger
  base/BasePage.java              Shared explicit waits and page actions
  driver/DriverManager.java       Thread-local WebDriver access
  pages/                          Login, inventory, cart, and checkout pages
  utils/TestDataReader.java       Reads test data from the classpath
src/test/java/org/example/framework/tests/
  LoginTests.java                 Valid and invalid login scenarios
  CheckoutTests.java              Login-to-order-confirmation journey
  InventoryCartTests.java         Inventory and cart scenarios
  CatalogueRegressionTests.java   Coverage based on the SauceDemo test catalogue
src/test/resources/
  log4j2.xml                      Console and rolling-file logging
  testdata/saucedemo.properties   Public demo credentials and checkout data
```

Test intent and assertions live in test classes. Page objects own page locators and actions, while `BasePage` shares explicit waits and common interactions. Test data is stored separately from Java code. The SauceDemo credentials in the properties file are public demo credentials, not private secrets.

## Run the suite

```bash
mvn clean test
```

Headless mode defaults to `true`. To see the browser locally, run `mvn clean test -Dheadless=false`. Choose another supported browser with `-Dbrowser=firefox` or `-Dbrowser=edge`. The default explicit wait is 10 seconds and can be changed with `-DexplicitWaitSeconds=15`.

Logs appear in the console and in `target/logs/automation.log`. Maven's `target/` directory is generated output and is ignored by Git.

The test catalogue is stored in `SauceDemo_Selenium_Java_Test_Cases.pdf`. Regression coverage includes login validation, inventory and sorting, product details, cart contents and removal, checkout validation and totals, order confirmation, navigation, session behavior, responsive inventory, keyboard traversal, and multi-item checkout. Some catalogue expectations describe demo behavior that can change; verify them against the live app before using them as release acceptance criteria.
