# Selenium Java Automation Framework

Day 1 foundation for a Maven, Selenium, and TestNG framework using SauceDemo as the practice application.

## Project layout

```text
src/test/java/org/example/framework/
  base/BaseTest.java             Browser lifecycle and common logger
  driver/DriverManager.java      Thread-local WebDriver access
  pages/LoginPage.java           SauceDemo login screen actions
  pages/InventoryPage.java       Successful-login page state
  utils/TestDataReader.java      Reads test data from the classpath
src/test/java/org/example/framework/tests/
  LoginTests.java                Valid and invalid login scenarios
src/test/resources/
  log4j2.xml                     Console and rolling-file logging
  testdata/saucedemo.properties  Public demo account data
```

Test intent and assertions live in test classes. Page objects own page locators and actions. Test data is stored separately from Java code. The SauceDemo credentials in the properties file are public demo credentials, not private secrets.

## Run the suite

```bash
mvn test
```

Headless mode defaults to `true`. To see the browser locally, run `mvn test -Dheadless=false`. Choose another supported browser with `-Dbrowser=firefox` or `-Dbrowser=edge`. The default explicit wait is 10 seconds and can be changed with `-DexplicitWaitSeconds=15`.

Logs appear in the console and in `target/logs/automation.log`. Maven's `target/` directory is generated output and is ignored by Git.
