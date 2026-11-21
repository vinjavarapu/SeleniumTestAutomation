# Selenium Framework Design

This document describes the framework currently present in this repository. It is a Maven, Java, Selenium WebDriver, and TestNG sample that exercises the public SauceDemo practice site. It is not evidence of measured customer performance, release readiness, or production reliability.

## Current capabilities

- TestNG tests use page objects under `src/test/java/org/example/framework/pages` and listener based ExtentReports output.
- `-Psmoke` selects the smoke tests; `-Pregression` selects the regression tests. The suites configure parallel TestNG execution.
- Configuration comes from classpath property files and Maven system properties. `retryCount` means additional retries after the initial attempt; it defaults to `0` (one total attempt).
- GitHub Actions runs smoke tests in Chrome and Firefox and regression tests in Chrome for pull requests, `main` pushes, and the nightly schedule. A QA environment runs by default; a staging matrix entry is added when the repository variable `STAGING_BASE_URL` is nonempty. CI explicitly sets retries to zero.
- CI uploads Surefire, Extent, log, and screenshot output as run-specific artifacts. Browser and driver version commands are recorded in CI logs; hosted runner images can update those versions.

## Configuration and test data

The checked-in SauceDemo credentials and checkout values are public demo fixtures, not private secrets. Private environment credentials should be supplied at runtime through environment variables or CI secrets, and should not be added to property files. Environment-specific files may contain nonsecret defaults such as a base URL. Test data should remain within the behavior the public application supports; this repository does not provide a private data API.

## Interpreting estimates

Any ROI, pass-rate, timeline, execution-time, or reliability figures in portfolio materials are illustrative planning estimates only. This repository does not contain measured baselines or longitudinal run data that substantiate those figures. Actual outcomes depend on the target application, environment, browser versions, and test data.

## Running locally

```bash
mvn clean test -Psmoke
mvn clean test -Pregression
mvn clean test -Pregression -DretryCount=2
```

The last command permits up to three total attempts. Retries can help diagnose intermittent behavior, but they can also make a final pass obscure an initial failure if used as a gate. Keep retries disabled for release-gating CI and review the first-attempt failure whenever diagnostic retries are enabled.
