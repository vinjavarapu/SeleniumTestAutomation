# Concurrent browser and multi-environment execution

## Execution model

The framework uses two levels of isolation:

1. **Within one Maven JVM:** TestNG runs methods in parallel. Each test method gets a new WebDriver through `BaseTest`; `DriverManager` stores it in a `ThreadLocal`, and the current Extent test is also thread-local. Browser sessions and page objects must never be shared between test methods.
2. **Across environments and browser types:** run a separate Maven process per environment/browser combination. `FrameworkConfig` is immutable process-level configuration, so two environments must not be switched inside one JVM. GitHub Actions uses a matrix to run those processes concurrently.

This design makes environment selection explicit at process startup and prevents one parallel test from changing another test's base URL or browser settings. For larger suites, move remote sessions to Selenium Grid or a managed browser service and cap matrix and TestNG concurrency to the provider's available session capacity.

## Run locally

```powershell
# One environment and browser, with methods parallelized by the suite XML.
mvn clean test -Psmoke -Denv=qa -Dbrowser=chrome -Dheadless=true -DretryCount=0

# Run another environment in a separate process or terminal.
mvn clean test -Psmoke -Denv=staging -Dbrowser=firefox -Dheadless=true -DretryCount=0
```

To run against Selenium Grid, set `-DremoteUrl=http://localhost:4444/wd/hub`. Each test method creates its own remote session; provision enough Grid slots for the suite's TestNG `thread-count`.

## CI topology

The workflow matrix runs smoke tests for QA and staging on Chrome and Firefox, and runs regression tests for each environment on Chrome for main-branch pushes and scheduled runs. Matrix jobs use separate runners, Maven JVMs, and artifact names. Test retries are disabled in CI so an initial failure stays visible. Local retry behavior defaults to zero and can be enabled explicitly for investigation.

Set the GitHub Actions repository variable `STAGING_BASE_URL` to a reachable, authorized staging URL to enable staging matrix jobs; without it, staging jobs are skipped. For local runs, configure `staging.properties` or pass `-DbaseUrl=...`. Do not put credentials in this file; pass secrets through the CI secret store and inject them at runtime. The SauceDemo demo credentials are public test data, not credentials for a private system.

## Isolation requirements for future tests

- Create and clean up test-owned data; avoid shared mutable accounts/cart state across sessions.
- Keep WebDriver, page objects, and mutable test data scoped to one test invocation.
- Use unique run and invocation identifiers for artifacts and any created backend records.
- Keep retries off for release gating; report first-attempt failures if diagnostic retries are later introduced.
- Set parallel limits from actual browser/Grid capacity and target-environment rate limits.
- Keep environment selection at process startup. Do not mutate JVM system properties while tests are running.
