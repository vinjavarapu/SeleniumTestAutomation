# Selenium Java Automation Framework

This project uses Maven and TestNG. Maven fits this Java project well because it manages dependencies and provides a standard test lifecycle; TestNG supplies test setup/teardown and suite execution.

## Planned structure

```text
src
├── main/java                 # Application code, if this repository later needs it
└── test
    ├── java/org/example
    │   ├── framework
    │   │   ├── base           # BaseTest and (later) BasePage
    │   │   ├── driver         # WebDriver lifecycle and browser factory
    │   │   ├── pages          # One page object per application page or component
    │   │   ├── reporting      # Extent Reports setup/listeners
    │   │   └── utils          # Shared helpers, kept small and focused
    │   └── tests              # Test classes grouped by feature
    └── resources
        ├── config             # Environment configuration
        └── testdata           # Small, non-sensitive test data files
```

### Page Object Model

Keep test intent in test classes and page interaction in page classes. A page object owns the locators for one screen, exposes actions such as `loginAs(...)` and meaningful observations such as `isDashboardVisible()`, and hides low-level Selenium calls from tests. Avoid putting assertions in page objects; tests should compare observed results with expected results. This keeps maintenance localized when a page changes.

## Browser options

The `BaseTest` class starts and quits a browser for each TestNG test method. Headless mode defaults to `true`, which suits CI agents. You can choose the browser and waits with JVM properties, and set `-Dheadless=false` to show the browser locally:

```text
mvn test -Dbrowser=chrome
mvn test -Dbrowser=chrome -Dheadless=false
mvn test -Dbrowser=firefox -DexplicitWaitSeconds=15
```

Supported browser names are `chrome`, `firefox`, and `edge`. Selenium Manager can manage drivers by itself; WebDriverManager is included as requested and is used by the current base class. Explicit waits should be used for conditions tied to the action under test. The implicit wait defaults to zero because mixing implicit and explicit waits can make failures take longer and less predictably.
