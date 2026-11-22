# SeleniumTestAutomation Framework Audit

**Review date:** 2026-11-22  
**Scope:** Read-only architecture, dependency, reliability, and CI review. No source or configuration changes were made as part of the audit.

## Executive assessment

The framework has a credible Selenium/TestNG foundation: thread-local browser management, per-method setup and teardown, explicit waits, configurable local and remote drivers, separate Maven test profiles, ExtentReports, and GitHub Actions artifacts. A redesign is not indicated.

The key items to address before calling it enterprise-stable are:

1. Triage and remediate confirmed dependency vulnerabilities.
2. Verify Dependency-Check report completeness and distinguish scanner failures from vulnerability findings in CI.
3. Reduce test-data and public-environment coupling before scaling parallel execution.
4. Confirm browser coverage and staging configuration against the framework's stated support.

The framework is a good portfolio/demo baseline. Its current test-data and external-environment assumptions, along with untriaged dependency findings, limit claims of production readiness.

## Architecture findings

| Severity | File / class | Evidence and impact | Recommendation |
|---|---|---|---|
| Medium | `src/test/resources/testdata/saucedemo.properties`; `TestDataReader`; SauceDemo tests | Checked-in public demo credentials and shared application data make runs dependent on the public demo service and can cause state interference between parallel tests. No supplied failure log proves a particular failure was caused by this. | Retain for the sample app. For client systems, use isolated accounts/data and explicit setup, reset, and teardown. |
| Medium | `framework/driver/DriverFactory.java`; `.github/workflows/selenium-tests.yml` | Edge driver creation is supported, but CI smoke covers Chrome and Firefox, while regression is Chrome-only. Edge-specific startup and UI behavior are therefore not exercised in CI. | Add Edge smoke coverage if Edge is part of the supported-browser promise; check browser and driver versions in that job. |
| Medium | `src/test/resources/config/staging.properties`; `.github/workflows/selenium-tests.yml` | The checked-in staging URL appears to be a placeholder. CI avoids using it unless `STAGING_BASE_URL` is configured, but local/ad-hoc staging runs can use an invalid default. | Require an explicit staging URL or configure a valid authorized environment. |
| Low | `framework/listeners/TestListener.java` | A failed attempt is logged before retry; a later successful retry can produce both failure and pass entries for one test method. | Specify whether reports represent attempts or final outcomes; retain failed-attempt evidence and make the final result unambiguous. |
| Low | `framework/unittests/FrameworkConfigTest.java`; `src/test/resources/suites/unit.xml` | Unit tests mutate JVM system properties. They currently run non-parallel, so no current interference was identified; parallelizing this suite could introduce flakiness. | Keep the unit suite non-parallel or isolate property changes before enabling parallel execution. |

### Existing strengths

- `DriverManager` uses `ThreadLocal` storage and removes it during cleanup.
- `BaseTest` uses `alwaysRun` setup/teardown for per-method browser lifecycle.
- `WaitHelper` uses explicit waits; fixed sleeps were not identified in the reviewed wait helper.
- Configuration is validated before browser startup and supports JVM/environment overrides.
- CI disables retries and uploads test artifacts.

The available evidence does not establish that any reported browser failure was caused by framework code, browser configuration, infrastructure, or shared application state. That requires the actual run logs and environment details.

## Dependency security findings

The inspected Dependency-Check JSON report was generated on **2026-11-21** by Dependency-Check **12.2.2**. It contains 55 scanned dependency entries and findings on six artifacts. The Maven dependency tree places these artifacts in test scope:

- ExtentReports is direct; FreeMarker is transitive through ExtentReports.
- HttpClient5 and HttpCore5 are transitive through WebDriverManager's Docker Java transport.
- Jackson is transitive through Docker Java.
- OpenTelemetry is transitive through Selenium's remote driver.

The currently configured threshold is `failBuildOnCVSS=11`, so CVE findings are report-only. `failOnError=true` is configured to fail on scanner analysis errors.

| Reported artifact | Findings | Assessment and next step |
|---|---|---|
| `extentreports-5.1.2.jar: spark-script.js` | CVE-2020-7746 (Critical); CVE-2016-10735; CVE-2018-14040/-14041/-14042; CVE-2019-11358; CVE-2019-8331; CVE-2020-11022/-11023 | These findings concern bundled JavaScript assets rather than Java APIs. The 2016 Bootstrap and 2019 Bootstrap matches appear inconsistent with the reported stable Bootstrap 4.0.0 asset version. Confirm the JAR contents and asset versions before changing the reporting library or suppressing findings. |
| `org.freemarker:freemarker:2.3.32` | CVE-2026-84939 (Critical) | The reviewed advisory identifies versions 2.2.0–2.3.34 as affected and 2.3.35 as fixed. Candidate remediation: upgrade the transitive FreeMarker version to 2.3.35 and validate ExtentReports output. |
| `org.apache.httpcomponents.client5:httpclient5:5.6.1` | CVE-2026-71290 (Critical); CVE-2026-64607 (Medium) | Version matches the reported affected ranges. The critical issue has usage/configuration applicability conditions. Candidate common target: 5.6.4; verify compatibility with the WebDriverManager/Docker Java path. |
| `org.apache.httpcomponents.core5:httpcore5:5.4` | CVE-2026-54399 and CVE-2026-54428 (High) | CVE-2026-54428 identifies `httpcore5-h2`; the dependency tree includes `httpcore5-h2:5.4`, so verify the report's artifact attribution. A fixed version was not established in the gathered evidence. Do not select an override until the affected component and vendor fix are confirmed. |
| `com.fasterxml.jackson.core:jackson-databind:2.20.1` | CVE-2026-54512/-54513 (High); CVE-2026-54514/-54515 (Medium) | The reported version is within the affected ranges. The common evidenced fix for the four findings is 2.21.5. Align Jackson modules and validate the transitive consumer. |
| `io.opentelemetry:opentelemetry-api:1.67.0` | CVE-2026-54285 (Medium) | The advisory concerns OpenTelemetry JavaScript baggage parsing, not the Java API. This appears to be an ecosystem/CPE mismatch. Confirm Dependency-Check evidence before adding a narrowly scoped, documented suppression. |

**Caution:** Dependency-Check matches are investigation leads, not proof that the vulnerable code is reachable or exploitable in this test framework. In particular, verify embedded asset contents and artifact attribution before remediation or suppression. Do not use blanket suppressions.

### Sources for CVE verification

- [NVD CVE search](https://nvd.nist.gov/vuln/search)
- [Apache FreeMarker advisory](https://lists.apache.org/thread/hrd7o2ylwkkswdyhyzllgqt0f80kyd5y)
- [Jackson Databind advisory](https://github.com/FasterXML/jackson-databind/security/advisories/GHSA-j3rv-43j4-c7qm)
- [OpenTelemetry JS advisory](https://github.com/open-telemetry/opentelemetry-js/security/advisories/GHSA-8988-4f7v-96qf)
- [Chart.js 2.9.4 release](https://github.com/chartjs/Chart.js/releases/tag/v2.9.4)
- [Bootstrap 4.1.2 release](https://github.com/twbs/bootstrap/releases/tag/v4.1.2)
- [jQuery 3.5.0 security release](https://blog.jquery.com/2020/04/10/jquery-3-5-0-released/)

The reported Java 25 configuration and local scan do not establish that every transitive upgrade is compatible with all Selenium/WebDriverManager execution paths. Revalidate each proposed change with compilation, unit tests, the security scan, and report generation.

## Dependency-Check and CI assessment

- **Isolation:** `pom.xml` binds Dependency-Check to `verify` only in the `security` profile and skips test compilation/execution. Normal unit, smoke, regression, and default `all` profiles do not opt into it.
- **Test dependencies:** `skipTestScope=false` includes Selenium, reporting, and driver-management libraries.
- **Reports:** HTML and JSON are configured under `target/security-dependency-check/`; both report files existed locally during the audit.
- **NVD key:** The security workflow passes the secret as the scan step's `NVD_API_KEY` environment variable and passes Maven only the environment-variable name. The README documents local usage and the GitHub secret.
- **Database cache:** The workflow cache path matches the local Dependency-Check database path. The date-based key allows reuse and periodic refresh; cache behavior should still be confirmed in Actions logs.
- **Network reliability:** Plugin connection/read timeouts are 10/120 seconds. The 45-minute CI timeout may be tight for first use or rate-limited NVD updates. An earlier keyless local scan took about 24 minutes and logged repeated request retries.
- **Report validation:** Upload uses `if: always()`, so reports already generated can still upload after scan failure. `if-no-files-found: warn` means missing reports only warn, however. Add explicit validation that both expected reports exist and are non-empty, while keeping upload on `if: always()`.
- **Workflow security and independence:** Permissions are limited to `contents: read`; third-party actions are pinned by SHA. The security workflow does not install or invoke browsers or drivers. No Selenium workflow change is needed for security scanning.

## Prioritized implementation roadmap

| Priority | Proposal | Files likely affected | Benefit and validation |
|---|---|---|---|
| P0 | Triage and remediate confirmed Critical/High findings. Candidate targets include FreeMarker 2.3.35, HttpClient 5.6.4, and Jackson 2.21.5. Verify the correct HttpCore artifact and fixed release first. | `pom.xml`; possibly ExtentReports dependency/version | Reduce confirmed dependency risk. Validate Java 25 compilation, unit tests, report generation, and a fresh security scan. Risk: transitive overrides may affect WebDriverManager, Docker transport, or reports. |
| P0 | Verify ExtentReports bundled JavaScript assets, HttpCore artifact attribution, and OpenTelemetry ecosystem mismatch. Add only justified, narrowly scoped suppressions after confirming false positives. | JAR/report investigation; if justified, suppression XML and `pom.xml` | Avoid both false positives and hiding genuine issues. Re-scan and inspect each affected dependency and suppression rationale. |
| P1 | Fail the workflow if either expected report is missing or empty, but always upload any reports that were created. | `.github/workflows/dependency-check.yml` | Distinguish incomplete scans from completed scans. Validate locally for report presence and through an actual GitHub Actions run for artifact upload behavior. |
| P1 | Improve test-data isolation for parallel and client environments. | Application-specific setup/data fixtures and test lifecycle classes | Reduce shared-state interference. Validate with repeated parallel regression runs and actual failure logs. |
| P1 | Replace or require the staging URL; add Edge smoke coverage if Edge support is promised. | `staging.properties`; possibly `.github/workflows/selenium-tests.yml` | Avoid invalid environment defaults and catch unsupported browser regressions. Requires a valid authorized staging target and CI run. |
| P2 | Make retry reporting distinguish attempt history from final test outcome; review artifact sensitivity/retention for client data. | `TestListener.java`; possibly workflow artifact configuration | Improve traceability and data handling. Validate both retry failure-then-pass and final-failure scenarios. |

## Validation

### Completed during the audit

- `mvn --batch-mode --no-transfer-progress dependency:tree -Dscope=test` — succeeded; dependency paths were reviewed.
- Existing Dependency-Check JSON and HTML report presence — confirmed locally.
- No source or configuration files were changed; tests and scan were not rerun as part of this read-only audit.

### Recommended after approved changes

```powershell
mvn --batch-mode --no-transfer-progress test-compile
mvn --batch-mode --no-transfer-progress test -Punit
mvn --batch-mode --no-transfer-progress -Psecurity verify -DskipTests
mvn --batch-mode --no-transfer-progress test -Psmoke
mvn --batch-mode --no-transfer-progress test -Pregression
git diff --check
```

Inspect the HTML and JSON reports after the security command. Inspect the uploaded artifact from a GitHub Actions run to validate CI upload behavior. Browser tests and CI artifact upload cannot be fully validated locally; GCP stability requires a run on the actual Debian VM with its configured Chromium/ChromeDriver pair.

## Readiness conclusion

The repository is a credible portfolio/demo framework with sound foundational patterns. It is not yet demonstrated as enterprise-production-ready: dependency findings remain untriaged, parallel runs share public application state, staging configuration needs confirmation, and Edge is not covered in CI despite being supported. Address P0 items first, then validate the P1 items against the target environments. No changes are made by this report.
