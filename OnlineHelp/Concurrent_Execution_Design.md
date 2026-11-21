# Concurrent browser and multi-environment execution

## Execution model

1. **Within one Maven JVM:** TestNG suites configure parallel method execution. `BaseTest` creates a browser session for each test method; `DriverManager` stores it in a `ThreadLocal`. Do not share browser sessions or page objects between test methods.
2. **Across environments and browsers:** start a separate Maven process per environment/browser combination. `FrameworkConfig` is process-level configuration. GitHub Actions uses a matrix for these combinations.

## Run locally

```powershell
mvn clean test -Psmoke -Denv=qa -Dbrowser=chrome -Dheadless=true -DretryCount=0
mvn clean test -Psmoke -Denv=staging -Dbrowser=firefox -Dheadless=true -DretryCount=0
```

To use Selenium Grid, set `-DremoteUrl=http://localhost:4444/wd/hub`. Each test method creates its own session; provide enough Grid slots for the suite's TestNG thread count.

## CI coverage and browser versions

Pull requests, pushes to `main`, and nightly scheduled runs execute the smoke profile on Chrome and Firefox and the regression profile on Chrome. QA runs by default. Setting the GitHub Actions repository variable `STAGING_BASE_URL` adds staging to the environment matrix. The regression profile runs on one browser to keep CI duration practical. All CI commands set `retryCount=0`, and failures therefore gate on the initial attempt. If retries are enabled for diagnosis locally, the report and log identify the first failure and whether a later attempt passed.

Third-party workflow actions are pinned to reviewed commit SHAs with version comments. The Chrome setup action installs a matching driver. Set `CHROME_VERSION` to a specific Chrome for Testing version to hold the Chrome regression browser fixed; update that repository variable deliberately when adopting a new browser version, then review the run before updating the value. If unset, the action uses the moving stable channel. Firefox smoke currently uses the version on the hosted runner. `BaseTest` records the actual browser and driver versions in the test logs, and workflow artifacts are unique per matrix job and run attempt.

Keep credentials for private environments in CI secrets or runtime environment variables, not property files. `TESTDATA_<KEY>` environment variables override fixture properties, for example `TESTDATA_VALID_USERNAME` and `TESTDATA_VALID_PASSWORD`. Checked-in SauceDemo values are public demo data.

## Isolation requirements for future tests

- Create or clean up test-owned data only where the application supports it; the SauceDemo fixture currently uses a shared public demo account.
- Keep WebDriver, page objects, and mutable test data scoped to one test invocation.
- Use unique run and invocation identifiers for artifacts and for any records created by supported application flows.
- Keep retries off for release gating and set parallel limits from actual browser/Grid capacity and target-environment rate limits.
- Select the environment at process startup; do not mutate JVM system properties while tests are running.
