from pathlib import Path

from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER, TA_LEFT
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import mm
from reportlab.pdfbase.pdfmetrics import stringWidth
from reportlab.pdfgen import canvas
from reportlab.platypus import (
    BaseDocTemplate,
    Frame,
    HRFlowable,
    KeepTogether,
    PageBreak,
    PageTemplate,
    Paragraph,
    Spacer,
    Table,
    TableStyle,
)


ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "output" / "pdf" / "Selenium_Automation_Framework_Comprehensive_Guide.pdf"
OUT.parent.mkdir(parents=True, exist_ok=True)

NAVY = colors.HexColor("#17324D")
TEAL = colors.HexColor("#008489")
PALE = colors.HexColor("#EAF3F4")
LIGHT = colors.HexColor("#F4F7F9")
INK = colors.HexColor("#243746")
MUTED = colors.HexColor("#5A6B7A")
LINE = colors.HexColor("#D8E1E7")

styles = getSampleStyleSheet()
styles.add(ParagraphStyle(
    "CoverEyebrow", fontName="Helvetica-Bold", fontSize=9, leading=12,
    textColor=colors.HexColor("#8DE0DA"), alignment=TA_CENTER,
))
styles.add(ParagraphStyle(
    "CoverTitle", fontName="Helvetica-Bold", fontSize=28, leading=34,
    textColor=colors.white, alignment=TA_CENTER,
))
styles.add(ParagraphStyle(
    "CoverSub", fontName="Helvetica", fontSize=11, leading=16,
    textColor=colors.HexColor("#D5E3EB"), alignment=TA_CENTER,
))
styles.add(ParagraphStyle(
    "H1", fontName="Helvetica-Bold", fontSize=18, leading=22,
    textColor=NAVY, spaceBefore=4, spaceAfter=9,
))
styles.add(ParagraphStyle(
    "H2", fontName="Helvetica-Bold", fontSize=12, leading=15,
    textColor=NAVY, spaceBefore=8, spaceAfter=5,
))
styles.add(ParagraphStyle(
    "H3", fontName="Helvetica-Bold", fontSize=10, leading=13,
    textColor=TEAL, spaceBefore=6, spaceAfter=3,
))
styles.add(ParagraphStyle(
    "Body2", fontName="Helvetica", fontSize=8.8, leading=13,
    textColor=INK, spaceAfter=6,
))
styles.add(ParagraphStyle(
    "Small", fontName="Helvetica", fontSize=7.7, leading=10.5,
    textColor=MUTED,
))
styles.add(ParagraphStyle(
    "FrameworkCode", fontName="Courier", fontSize=7.2, leading=10,
    textColor=INK, leftIndent=8, rightIndent=8,
    borderColor=LINE, borderWidth=0.6, borderPadding=7,
    backColor=LIGHT, spaceBefore=4, spaceAfter=8,
))
styles.add(ParagraphStyle(
    "Callout", fontName="Helvetica", fontSize=8.5, leading=12,
    textColor=INK, borderColor=TEAL, borderWidth=0.8,
    borderPadding=8, backColor=PALE, spaceBefore=5, spaceAfter=8,
))


class GuideDocTemplate(BaseDocTemplate):
    pass


def P(text, style="Body2"):
    return Paragraph(text, styles[style])


def bullets(items):
    return [
        Paragraph("&#8226;&nbsp;&nbsp;" + item, styles["Body2"])
        for item in items
    ]


def code(text):
    return Paragraph(text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br/>"),
                     styles["FrameworkCode"])


def table(rows, widths, header=True, font_size=7.2):
    p_rows = []
    for r_index, row in enumerate(rows):
        p_rows.append([
            Paragraph(str(cell), styles["Small"] if r_index or not header else
                      ParagraphStyle("TableHead", parent=styles["Small"], fontName="Helvetica-Bold",
                                     textColor=colors.white, fontSize=font_size, leading=9.5))
            for cell in row
        ])
    t = Table(p_rows, colWidths=widths, repeatRows=1 if header else 0, hAlign="LEFT")
    commands = [
        ("GRID", (0, 0), (-1, -1), 0.35, LINE),
        ("VALIGN", (0, 0), (-1, -1), "TOP"),
        ("LEFTPADDING", (0, 0), (-1, -1), 5),
        ("RIGHTPADDING", (0, 0), (-1, -1), 5),
        ("TOPPADDING", (0, 0), (-1, -1), 4),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 4),
    ]
    if header:
        commands += [
            ("BACKGROUND", (0, 0), (-1, 0), NAVY),
            ("TEXTCOLOR", (0, 0), (-1, 0), colors.white),
        ]
    for i in range(1 if header else 0, len(rows)):
        if i % 2 == 0:
            commands.append(("BACKGROUND", (0, i), (-1, i), LIGHT))
    t.setStyle(TableStyle(commands))
    return t


def page_header_footer(canv, doc):
    page = canv.getPageNumber()
    if page == 1:
        return
    canv.saveState()
    canv.setStrokeColor(LINE)
    canv.setLineWidth(0.5)
    canv.line(18 * mm, 12 * mm, A4[0] - 18 * mm, 12 * mm)
    canv.setFillColor(MUTED)
    canv.setFont("Helvetica", 7)
    canv.drawString(18 * mm, 8 * mm, "SELENIUM TEST AUTOMATION | COMPREHENSIVE FRAMEWORK GUIDE")
    canv.drawRightString(A4[0] - 18 * mm, 8 * mm, str(page))
    canv.restoreState()


def cover_page(canv, doc):
    canv.saveState()
    canv.setFillColor(NAVY)
    canv.rect(0, 0, A4[0], A4[1], fill=1, stroke=0)
    canv.setFillColor(TEAL)
    canv.circle(A4[0] - 25 * mm, A4[1] - 25 * mm, 38 * mm, fill=1, stroke=0)
    canv.setFillColor(colors.HexColor("#214864"))
    canv.circle(A4[0] - 25 * mm, A4[1] - 25 * mm, 22 * mm, fill=1, stroke=0)

    canv.setFillColor(colors.HexColor("#8DE0DA"))
    canv.setFont("Helvetica-Bold", 9)
    canv.drawCentredString(A4[0] / 2, A4[1] - 72 * mm, "TECHNICAL DOCUMENTATION")
    canv.setFillColor(colors.white)
    canv.setFont("Helvetica-Bold", 28)
    canv.drawCentredString(A4[0] / 2, A4[1] - 94 * mm, "Selenium Test")
    canv.drawCentredString(A4[0] / 2, A4[1] - 104 * mm, "Automation Framework")
    canv.setFillColor(colors.HexColor("#D5E3EB"))
    canv.setFont("Helvetica", 11)
    canv.drawCentredString(A4[0] / 2, A4[1] - 119 * mm,
                           "Architecture, setup, execution, reporting, CI, and security")

    y = 70 * mm
    canv.setFillColor(colors.HexColor("#214864"))
    canv.roundRect(42 * mm, y - 9 * mm, A4[0] - 84 * mm, 18 * mm, 8, fill=1, stroke=0)
    canv.setFillColor(colors.white)
    canv.setFont("Helvetica-Bold", 8)
    canv.drawCentredString(A4[0] / 2, y - 1.5 * mm,
                           "JAVA 25  /  MAVEN  /  SELENIUM 4  /  TESTNG  /  SAUCEDEMO")

    canv.setFillColor(colors.HexColor("#D5E3EB"))
    canv.setFont("Helvetica", 8)
    canv.drawCentredString(A4[0] / 2, 24 * mm,
                           "Repository: vinjavarapu/SeleniumTestAutomation")
    canv.restoreState()


doc = GuideDocTemplate(
    str(OUT),
    pagesize=A4,
    leftMargin=18 * mm,
    rightMargin=18 * mm,
    topMargin=16 * mm,
    bottomMargin=16 * mm,
    title="Selenium Test Automation - Comprehensive Framework Guide",
    author="Vinja Vara Prasad",
    subject="Java Selenium TestNG framework architecture and operations guide",
)

cover = PageTemplate(id="Cover", frames=[Frame(18 * mm, 16 * mm, A4[0] - 36 * mm, A4[1] - 32 * mm,
                                               leftPadding=0, rightPadding=0, topPadding=0, bottomPadding=0)],
                     onPage=cover_page)
body = PageTemplate(id="Body", frames=[Frame(18 * mm, 16 * mm, A4[0] - 36 * mm, A4[1] - 32 * mm,
                                             leftPadding=0, rightPadding=0, topPadding=0, bottomPadding=0)],
                    onPage=page_header_footer)
doc.addPageTemplates([cover, body])

story = [Spacer(1, A4[1] - 35 * mm), PageBreak()]

story += [
    P("Document Purpose and Scope", "H1"),
    P(
        "This guide documents the SeleniumTestAutomation repository as it exists in the reviewed source. "
        "It is intended for developers, testers, reviewers, and DevOps engineers who need to run the framework, "
        "understand its design, extend it safely, or diagnose failures.",
        "Body2",
    ),
    P(
        "<b>Scope.</b> The repository demonstrates browser automation for the public SauceDemo practice application "
        "using Java, Maven, Selenium WebDriver, and TestNG. It includes unit tests for framework configuration, "
        "smoke and regression browser suites, page objects, local and remote WebDriver creation, ExtentReports, "
        "Surefire output, screenshots, logs, GitHub Actions, and OWASP Dependency-Check.",
        "Body2",
    ),
    P(
        "<b>Important boundary.</b> This is a portfolio/sample framework. Checked-in coverage and run configuration "
        "do not constitute measured production readiness, customer-environment validation, load testing, or "
        "business-acceptance sign-off.",
        "Callout",
    ),
    P("Contents", "H2"),
    table([
        ["Section", "Topics"],
        ["1. Executive overview", "Purpose, capabilities, repository status"],
        ["2. Technology stack", "Versions and responsibilities"],
        ["3. Repository layout", "Source, resources, CI, reports, scripts"],
        ["4. Architecture", "Test, page, base, configuration, driver, and reporting layers"],
        ["5. Execution lifecycle", "Setup, browser startup, test, teardown, evidence"],
        ["6. Configuration", "Precedence, property files, JVM and environment overrides"],
        ["7. Test suites", "Unit, smoke, regression, parallelism, and test areas"],
        ["8. Page-object model", "Reusable UI abstractions and action patterns"],
        ["9. Parallel and remote execution", "Thread-local browser state and Selenium Grid"],
        ["10. Reporting and diagnostics", "Extent, Surefire, logs, screenshots, retries"],
        ["11. CI/CD", "GitHub Actions unit, smoke, regression, and security jobs"],
        ["12. Dependency security", "OWASP scan, findings triage, report policy"],
        ["13. Setup and commands", "Prerequisites, local runs, Ubuntu setup, Grid runs"],
        ["14. Troubleshooting", "Driver, browser, configuration, and CI failures"],
        ["15. Extension guide", "Adding tests and adopting the framework for a client"],
        ["16. Known limitations", "Current risks and recommended roadmap"],
    ], [42 * mm, 118 * mm]),
    PageBreak(),

    P("1. Executive Overview", "H1"),
    P(
        "SeleniumTestAutomation is a Maven-based Java framework for browser testing. It separates test scenarios "
        "from Selenium implementation details and supplies the supporting infrastructure required to create a "
        "browser, synchronize interactions, manage parallel execution, collect evidence, and execute suites in CI.",
        "Body2",
    ),
    table([
        ["Capability", "Repository implementation"],
        ["Page-object model", "Login, inventory, product details, cart, checkout information/overview, and completion pages."],
        ["Explicit synchronization", "WaitHelper adds WebDriverWait operations with the configured timeout; no fixed sleep helper is used."],
        ["Browser support", "Local and remote Chrome, Firefox, and Edge options are implemented."],
        ["Parallel execution", "TestNG smoke suite uses methods in 2 threads; regression uses methods in 3 threads."],
        ["Thread isolation", "A ThreadLocal stores one WebDriver instance per TestNG worker thread."],
        ["Configuration", "Classpath properties plus environment overrides and JVM system-property precedence."],
        ["Reporting", "Extent HTML report, Surefire XML, Log4j2 logs, and screenshots on failure."],
        ["CI", "GitHub Actions runs unit, smoke, regression, and dependency-security workflows."],
        ["Retries", "Optional IRetryAnalyzer; CI sets retryCount=0 for first-attempt release-gate visibility."],
    ], [44 * mm, 116 * mm]),
    P("Design goals", "H2"),
    *bullets([
        "Keep application locators and UI actions out of test scenarios.",
        "Provide predictable browser setup and teardown for each TestNG method.",
        "Make browser, environment, URL, timeout, Grid, and retry values configurable.",
        "Collect enough evidence to diagnose a failed browser run.",
        "Allow the same suite definitions to run locally and in GitHub Actions.",
    ]),
    P("Evidence limitation", "Callout"),
    P(
        "A framework review can establish structure and implementation patterns. It cannot prove application "
        "reliability or absence of defects without actual repeatable run data. Browser behavior may depend on "
        "browser versions, network state, application state, test data, and environment capacity.",
        "Body2",
    ),
    PageBreak(),

    P("2. Technology Stack", "H1"),
    table([
        ["Component", "Version/configuration", "Responsibility"],
        ["Java", "Maven compiler release 25", "Implementation language and runtime."],
        ["Apache Maven", "Profiles plus Surefire", "Build, dependency management, suite selection, and test execution."],
        ["Selenium", "4.52.0", "Browser automation, locators, navigation, waits, and capabilities."],
        ["TestNG", "7.11.0", "Test discovery, groups, suites, data providers, lifecycle, listeners, and parallel methods."],
        ["WebDriverManager", "6.4.0", "Resolves local browser drivers."],
        ["Log4j2", "2.26.1", "Structured framework and test logs."],
        ["ExtentReports", "5.1.2", "Human-readable HTML execution report."],
        ["OWASP Dependency-Check", "12.2.2 Maven plugin", "CVE analysis for direct and transitive dependencies."],
        ["GitHub Actions", "Pinned action SHAs", "Unit, browser, and security automation."],
    ], [34 * mm, 42 * mm, 84 * mm]),
    P("Maven profiles", "H2"),
    table([
        ["Profile", "Suite", "Browser-free?", "Primary use"],
        ["unit", "unit.xml", "Yes", "Framework configuration checks"],
        ["smoke", "smoke.xml", "No", "Core login and navigation coverage"],
        ["regression", "regression.xml", "No", "Broader catalogue and checkout coverage"],
        ["all", "all.xml", "Mixed", "Default suite when no profile is selected"],
        ["security", "unit.xml", "Yes", "Dependency-Check verification; tests skipped"],
    ], [26 * mm, 32 * mm, 27 * mm, 75 * mm]),
    P("Target application", "H2"),
    P(
        "The checked-in suites exercise https://www.saucedemo.com/. Public demo credentials and data are intentionally "
        "non-secret. Client adoption requires an authorized environment, isolated accounts, reset mechanisms, "
        "and environment-specific fixtures.",
        "Body2",
    ),
    PageBreak(),

    P("3. Repository Layout", "H1"),
    code(
        "src/test/java/io/github/vinjavarapu/seleniumtestautomation/framework/\n"
        "  base/       BaseTest and shared page/BasePage behavior\n"
        "  config/     Property loading and typed framework configuration\n"
        "  driver/     Local/remote factory and thread-local WebDriver storage\n"
        "  listeners/  Retry analyzer/transformer and TestNG lifecycle listener\n"
        "  pages/      SauceDemo page objects\n"
        "  reporting/  Test-step reporting bridge\n"
        "  tests/      Smoke and regression scenarios\n"
        "  unittests/  Framework configuration unit tests\n"
        "  utils/      Wait, test data, and Extent report helpers\n"
        "src/test/resources/\n"
        "  config/     QA and staging property overrides\n"
        "  suites/     Unit, smoke, regression, and all suite XML\n"
        "  testdata/   Public SauceDemo fixture values\n"
        ".github/workflows/  Browser and dependency-security automation\n"
        "scripts/            Ubuntu Chrome setup\n"
        "output/pdf/         Generated PDF deliverables\n"
        "tmp/pdfs/           PDF generator scripts"
    ),
    P("Primary source entry points", "H2"),
    table([
        ["File", "Purpose"],
        ["pom.xml", "Dependencies, Maven profiles, Surefire suite selection, and dependency scanning."],
        ["BaseTest.java", "Per-method browser setup and teardown shared by browser test classes."],
        ["FrameworkConfig.java", "Typed access and startup validation for framework settings."],
        ["ConfigReader.java", "Property precedence and environment selection."],
        ["DriverFactory.java", "Local Chrome/Firefox/Edge or remote Selenium Grid creation."],
        ["DriverManager.java", "ThreadLocal WebDriver lifecycle."],
        ["TestListener.java", "Suite/test lifecycle reporting and failure screenshots."],
        ["SmokeTest.java / LoginTests.java", "Core smoke coverage."],
        ["CatalogueRegressionTests.java", "Broad catalogue, inventory, cart, checkout, keyboard, and viewport coverage."],
    ], [58 * mm, 102 * mm]),
    P("Supporting documentation", "H2"),
    *bullets([
        "README.md describes setup, run commands, CI behavior, and Dependency-Check operations.",
        "FRAMEWORK_AUDIT_REPORT.md records architecture, dependency, reliability, and CI observations.",
        "OnlineHelp contains design and portfolio material.",
        "SauceDemo_Selenium_Java_Test_Cases.pdf provides test-case catalogue material.",
    ]),
    PageBreak(),

    P("4. Architecture", "H1"),
    P("The framework is organized as a set of cooperating layers. Each test method follows the same lifecycle.", "Body2"),
    table([
        ["Layer", "Classes", "Responsibility"],
        ["Execution", "TestNG suite XML, Maven Surefire", "Selects groups, creates worker threads, and invokes test methods."],
        ["Lifecycle", "BaseTest", "Creates and closes a browser around each method; exposes shared page actions."],
        ["Configuration", "ConfigReader, FrameworkConfig", "Resolves settings and validates startup inputs."],
        ["Browser", "DriverFactory, DriverManager", "Creates local or remote browsers and isolates WebDriver state by thread."],
        ["Application", "LoginPage, InventoryPage, CartPage, Checkout...Page", "Encapsulates locators and screen behaviors."],
        ["Synchronization", "BasePage, WaitHelper", "Centralizes explicit Selenium wait operations."],
        ["Evidence", "TestListener, TestStepReporter, ExtentReportManager", "Records steps, outcomes, logs, screenshots, and HTML evidence."],
        ["Fixtures", "TestDataReader", "Loads public or runtime test data."],
    ], [28 * mm, 42 * mm, 90 * mm]),
    P("High-level flow", "H2"),
    code(
        "GitHub Actions / Maven\n"
        "        |\n"
        "        v\n"
        "TestNG suite XML -> test groups and method threads\n"
        "        |\n"
        "        v\n"
        "BaseTest @BeforeMethod -> FrameworkConfig -> DriverFactory\n"
        "        |\n"
        "        v\n"
        "ThreadLocal WebDriver -> Page objects -> WaitHelper\n"
        "        |\n"
        "        v\n"
        "Assertions + TestStepReporter -> TestListener\n"
        "        |\n"
        "        v\n"
        "BaseTest @AfterMethod -> quit browser\n"
        "        |\n"
        "        v\n"
        "Extent HTML / Surefire XML / logs / screenshots"
    ),
    P("Shared responsibilities", "H2"),
    P(
        "The lifecycle base is the only class that owns browser creation and teardown. Tests should use "
        "<font name='Courier'>getDriver()</font> and page objects rather than constructing WebDriver instances "
        "independently. Page objects should not create a browser or assert whole test outcomes; they should expose "
        "screen behavior and return or move to the appropriate next page.",
        "Body2",
    ),
    P("5. Execution Lifecycle", "H1"),
    table([
        ["Phase", "Implementation", "Result"],
        ["1. Config resolve", "ConfigReader loads env and property values.", "Browser, URL, timeout, headless, retry, and remote URL are available."],
        ["2. Validate", "FrameworkConfig.validate()", "Unsupported browser or invalid URL/timeouts fail before browser startup."],
        ["3. Setup", "@BeforeMethod(alwaysRun=true) in BaseTest", "DriverFactory creates a browser and DriverManager stores it on the thread."],
        ["4. Test", "TestNG invokes a method.", "Page objects perform actions and assertions record pass/fail behavior."],
        ["5. Reporting", "TestListener/TestStepReporter", "Suite/test status, steps, URLs, and failure evidence are captured."],
        ["6. Teardown", "@AfterMethod(alwaysRun=true)", "The thread's browser is quit and ThreadLocal state is removed."],
    ], [30 * mm, 58 * mm, 72 * mm]),
    PageBreak(),

    P("6. Configuration", "H1"),
    P("Settings are resolved in descending precedence:", "Body2"),
    table([
        ["Priority", "Source", "Examples"],
        ["1 (highest)", "JVM system property", "-Dbrowser=chrome, -DbaseUrl=https://..., -Dheadless=true"],
        ["2", "config/{env}.properties", "QA or staging environment overrides"],
        ["3 (lowest)", "config.properties", "Default application and framework values"],
    ], [34 * mm, 55 * mm, 71 * mm]),
    P("Setting reference", "H2"),
    table([
        ["Setting", "Default", "Validation/behavior"],
        ["baseUrl", "https://www.saucedemo.com/", "Must be an absolute HTTP or HTTPS URL with a host."],
        ["browser", "edge", "Chrome, firefox, or edge."],
        ["headless", "true", "Must be true or false."],
        ["explicitWaitSeconds", "10", "Integer/long greater than zero."],
        ["retryCount", "0", "Additional retries after the initial attempt; must be zero or greater."],
        ["remoteUrl", "empty", "When set, must be an absolute HTTP(S) URL."],
        ["reportName", "Selenium Test Execution Report", "Extent report display title."],
    ], [43 * mm, 48 * mm, 69 * mm]),
    P("Environment selection", "H2"),
    P(
        "The default environment is QA. <font name='Courier'>-Denv=qa</font> or "
        "<font name='Courier'>-Denv=staging</font> selects the corresponding property file. Runtime JVM properties "
        "override environment values. GitHub Actions passes QA or staging and also supplies an optional "
        "<font name='Courier'>STAGING_BASE_URL</font> through <font name='Courier'>-DbaseUrl</font>.",
        "Body2",
    ),
    P("Shared sandbox configuration", "H2"),
    P(
        "For this repository, QA and staging are duplicate profiles against the same public SauceDemo sandbox URL. "
        "The staging profile may be overridden by <font name='Courier'>STAGING_BASE_URL</font> in CI or by "
        "<font name='Courier'>-DbaseUrl</font> locally. This removes a placeholder staging target while preserving "
        "the ability to point both profiles at one shared authorized sandbox.",
        "Body2",
    ),
    P("Secret handling", "Callout"),
    P(
        "The public SauceDemo credentials and checkout fixture values are not secrets. Private usernames, passwords, "
        "API tokens, customer URLs, and test data should come from environment variables or a secret manager. "
        "Do not add them to property files or pass secret values as POM arguments.",
        "Body2",
    ),
    P("Example overrides", "H2"),
    code(
        "mvn clean test -Psmoke -Denv=qa -Dbrowser=chrome -Dheadless=true\n"
        "mvn clean test -Pregression -Dbrowser=firefox -DexplicitWaitSeconds=15\n"
        "mvn clean test -Pregression -Denv=staging -DbaseUrl=https://authorized.example.test\n"
        "mvn clean test -Pregression -DremoteUrl=http://localhost:4444 -Dbrowser=chrome"
    ),
    PageBreak(),

    P("7. Test Suites", "H1"),
    P("The suite XML files control which groups and test classes run.", "Body2"),
    table([
        ["Suite", "Thread count", "Group", "Focus"],
        ["unit.xml", "Default", "Framework unit tests", "Configuration parsing, precedence, validation, and browser-free behavior."],
        ["smoke.xml", "2", "smoke", "Core valid login, invalid login, and baseline application access."],
        ["regression.xml", "3", "regression", "Catalogue, inventory, cart, checkout, menu, keyboard, sorting, responsive, and resilience scenarios."],
        ["all.xml", "Suite-defined", "All", "Combined suite entry; active by default when no profile is selected."],
    ], [30 * mm, 27 * mm, 32 * mm, 71 * mm]),
    P("Smoke coverage", "H2"),
    *bullets([
        "A valid SauceDemo user can sign in.",
        "Invalid credentials are rejected.",
        "A baseline smoke scenario verifies public application access.",
    ]),
    P("Regression coverage", "H2"),
    *bullets([
        "Login controls and invalid/incomplete login behavior.",
        "Problem and performance-glitch user authentication observations.",
        "Logout and guarded-session behavior.",
        "Inventory names, prices, images, details, and sorting.",
        "Cart add/remove behavior, quantities, totals, and navigation.",
        "Checkout field validation, cancellation, overview totals, and order completion.",
        "Side menu, reset state, refresh/back/forward behavior.",
        "Keyboard operation and visible focus indicators.",
        "Narrow and desktop viewport usability.",
    ]),
    P("Test-data providers", "H2"),
    P(
        "CatalogueRegressionTests uses data providers for rejected login combinations, sorting orders, and checkout "
        "field validation. CheckoutTests demonstrates an end-to-end happy path: login, add a product, cart, customer "
        "information, overview, and completion.",
        "Body2",
    ),
    P("Suite caution", "Callout"),
    P(
        "Parallel tests share the public application and public demo state. The framework does not currently establish "
        "per-thread accounts or reset state by API. Parallelism should be increased only after test isolation, data "
        "ownership, and environment capacity are verified.",
        "Body2",
    ),
    PageBreak(),

    P("8. Page-Object Model", "H1"),
    table([
        ["Page", "Primary responsibility"],
        ["LoginPage", "Login controls, entering credentials, submitting, logout state, and login outcomes."],
        ["InventoryPage", "Product catalogue, sorting, cart badge, add/remove controls, menu, and product navigation."],
        ["ProductDetailsPage", "Selected product details, image, add/remove, and back navigation."],
        ["CartPage", "Cart line items, removal, continue shopping, and checkout navigation."],
        ["CheckoutInformationPage", "Customer field validation and continue/cancel navigation."],
        ["CheckoutOverviewPage", "Line-item and calculated-total verification plus finish/cancel actions."],
        ["CheckoutCompletePage", "Order-completion confirmation and return to inventory."],
    ], [52 * mm, 108 * mm]),
    P("BasePage role", "H2"),
    P(
        "BasePage supplies common low-level interactions and explicit waits. Page classes should call the base helpers "
        "for clicks, text entry, visibility, text retrieval, and assertions only where useful. Selectors should remain "
        "close to the page class that owns the UI.",
        "Body2",
    ),
    P("Wait strategy", "H2"),
    P(
        "WaitHelper wraps Selenium <font name='Courier'>WebDriverWait</font>. Expected conditions are used for "
        "visibility and clickability rather than fixed sleeps. The timeout comes from "
        "<font name='Courier'>explicitWaitSeconds</font>.",
        "Body2",
    ),
    P("Recommended page-object rules", "H2"),
    *bullets([
        "Name methods by user intent, for example addToCart or enterShippingDetails.",
        "Keep Selenium locators private to or clearly grouped within the page class.",
        "Return a new page object after navigation when it improves test readability.",
        "Keep assertions in tests unless the assertion represents a stable page invariant.",
        "Avoid WebDriver creation or suite-specific behavior inside page objects.",
    ]),
    P("9. Parallel and Remote Execution", "H1"),
    P(
        "Two concurrency levels are relevant: TestNG worker methods inside a Maven process and separate CI jobs or "
        "Maven JVM processes. The framework should treat these as distinct capacity choices.",
        "Body2",
    ),
    table([
        ["Level", "Mechanism", "State isolation"],
        ["Methods in one suite", "smoke.xml 2 threads; regression.xml 3 threads", "Each thread owns a separate WebDriver in ThreadLocal."],
        ["CI matrix", "GitHub Actions matrix jobs", "Separate runners/processes and separate browser sessions."],
        ["Selenium Grid", "remoteUrl and RemoteWebDriver", "Grid allocates a remote browser session for each local request."],
    ], [42 * mm, 57 * mm, 61 * mm]),
    P("Thread isolation", "H2"),
    P(
        "DriverManager stores WebDriver in a ThreadLocal. setDriver rejects a second WebDriver on the same thread. "
        "quitDriver closes the browser and removes the thread-local entry, even if quit throws.",
        "Body2",
    ),
    P("Remote Grid operation", "H2"),
    code(
        "mvn clean test -Pregression \\\n"
        "  -Dbrowser=chrome \\\n"
        "  -DremoteUrl=http://selenium-grid-host:4444 \\\n"
        "  -Dheadless=true"
    ),
    P(
        "Grid URL and browser capability validation happens before creating the RemoteWebDriver. Grid capacity must "
        "match the TestNG thread count and scenario timeout. A shared public demo app may still require lower "
        "parallelism even when browser capacity is available.",
        "Body2",
    ),
    PageBreak(),

    P("10. Reporting and Diagnostics", "H1"),
    table([
        ["Artifact", "Typical location", "Use"],
        ["Surefire XML/text", "target/surefire-reports/", "Machine-readable test result and failure details."],
        ["Extent HTML", "target/extent-reports/", "Human-readable test steps and execution status."],
        ["Screenshots", "target/screenshots/", "Visual evidence from failures."],
        ["Log4j2 logs", "target/logs/", "Browser lifecycle, configuration, and diagnostic messages."],
        ["CI artifacts", "GitHub Actions run", "30-day collection of run evidence."],
    ], [43 * mm, 43 * mm, 74 * mm]),
    P("Report lifecycle", "H2"),
    P(
        "TestListener records suite/test lifecycle events and creates or updates the run report. Failure handling "
        "captures a screenshot and URL details where available. TestStepReporter provides a bridge for recording "
        "individual test actions.",
        "Body2",
    ),
    P("Retry interpretation", "H2"),
    P(
        "RetryTransformer applies RetryAnalyzer to TestNG methods. retryCount is additional attempts after the initial "
        "test; zero means one total attempt. CI explicitly disables retries. A report that combines failed attempts "
        "and a later pass can obscure first-attempt failure, so release gates should interpret final status carefully "
        "and retain attempt history.",
        "Body2",
    ),
    P("Diagnostic checklist", "H2"),
    *bullets([
        "Read the TestNG result before the report or screenshot.",
        "Verify browser major version and driver major version.",
        "Confirm baseUrl and environment selection.",
        "Check browser startup capabilities and remote/Grid connectivity.",
        "Review target/logs for framework exceptions and cleanup failures.",
        "Open the failure screenshot and compare it with the recorded URL/step.",
        "Repeat only after identifying whether the defect is isolated or shared-state related.",
    ]),
    P("11. CI/CD", "H1"),
    table([
        ["Job", "Trigger", "Execution"],
        ["Unit", "Push, PR, manual, schedule", "Java 25 with Maven unit profile."],
        ["Smoke", "Push, PR, manual, schedule", "Matrix: Chrome/Firefox by QA, and optional staging when STAGING_BASE_URL is set."],
        ["Regression", "PR, main push, schedule", "Chrome by QA, and optional staging; not configured for workflow_dispatch unless a push/PR triggers it."],
        ["Security", "Weekly, manual, POM/workflow changes", "OWASP Dependency-Check in verify; test compilation/execution skipped."],
    ], [38 * mm, 48 * mm, 74 * mm]),
    P("Browser/driver control", "H2"),
    P(
        "CI uses browser-actions/setup-chrome and validates Chrome and ChromeDriver major versions. Firefox is covered "
        "only by smoke jobs. Edge implementation exists, but CI does not currently cover Edge.",
        "Body2",
    ),
    P("Artifacts", "H2"),
    P(
        "CI uploads Surefire, Extent, log, and screenshot directories with a 30-day retention policy. Unit results are "
        "uploaded separately. Browser jobs record browser and driver versions even on failure.",
        "Body2",
    ),
    P("CI guardrails", "Callout"),
    P(
        "Retries are set to zero for CI, Chrome major versions are validated against ChromeDriver, and workflow "
        "permissions are limited to contents read. Third-party actions are pinned by commit SHA.",
        "Body2",
    ),
    PageBreak(),

    P("12. Dependency Security", "H1"),
    P(
        "The repository includes an isolated security Maven profile. It runs OWASP Dependency-Check in Maven verify "
        "without compiling or executing tests, includes test-scoped dependencies, and writes HTML and JSON reports.",
        "Body2",
    ),
    code(
        "mvn --batch-mode --no-transfer-progress -Psecurity verify -DskipTests"
    ),
    P("Reports", "H2"),
    table([
        ["Report", "Location", "Purpose"],
        ["HTML", "target/security-dependency-check/dependency-check-report.html", "Human-readable dependency, CVE, CVSS, evidence, and remediation review."],
        ["JSON", "target/security-dependency-check/dependency-check-report.json", "Machine-readable findings and dependency data."],
    ], [30 * mm, 72 * mm, 58 * mm]),
    P("Current policy", "H2"),
    P(
        "The plugin uses failBuildOnCVSS=11, which is above the normal CVSS range. This makes dependency findings "
        "report-only instead of a build gate. failOnError=true still fails on scanner analysis errors. This initial "
        "policy permits triage before enforcing CVE thresholds.",
        "Body2",
    ),
    P("Triage principles", "H2"),
    *bullets([
        "Treat Dependency-Check matches as leads, not proof of reachable exploitation.",
        "Review dependency scope, version, affected code, fixed release, and application usage.",
        "Investigate embedded JavaScript assets and ecosystem/CPE mismatches before suppressing.",
        "Prefer dependency upgrades over suppressions.",
        "Use a narrowly scoped suppression only with rationale, owner, review date, and expiry.",
        "Rerun unit, smoke, and regression coverage after security-related upgrades.",
    ]),
    P("Known triage areas from the repository audit", "H2"),
    table([
        ["Area", "Observation"],
        ["FreeMarker", "Audit identifies a candidate 2.3.35 upgrade after validating ExtentReports compatibility."],
        ["HttpClient5/HttpCore5", "A potential transitive WebDriverManager/Docker path; fixed and applicable components require verification."],
        ["Jackson", "Transitive consumer and module alignment require validation before upgrading."],
        ["ExtentReports assets", "Embedded JavaScript CVEs require JAR and asset-version verification."],
        ["OpenTelemetry", "Possible ecosystem/CPE mismatch requiring evidence review before suppression."],
    ], [46 * mm, 114 * mm]),
    P("NVD API key", "H2"),
    P(
        "An NVD API key is optional and increases request rate limits. It should be stored in a local environment "
        "variable or GitHub secret, not source control or the POM. Fork pull requests do not receive repository secrets.",
        "Body2",
    ),
    P("13. Setup and Local Commands", "H1"),
    P("Prerequisites", "H2"),
    *bullets([
        "JDK 25",
        "Apache Maven 3.8.1 or later",
        "Outbound network access for Maven dependencies",
        "Chrome, Firefox, Edge, or an accessible Selenium Grid",
        "A user account/environment authorized for browser testing",
    ]),
    P("Windows/PowerShell commands", "H2"),
    code(
        "mvn --batch-mode test -Punit\n"
        "mvn --batch-mode test -Psmoke\n"
        "mvn --batch-mode test -Pregression\n"
        "mvn --batch-mode test -Pregression -Denv=qa -Dbrowser=chrome -Dheadless=true\n"
        "mvn --batch-mode clean test -Psecurity verify -DskipTests"
    ),
    P("Ubuntu setup", "H2"),
    P(
        "The repository provides <font name='Courier'>scripts/setup-ubuntu-chrome.sh</font>. It is intended for Ubuntu "
        "VMs that should use Google Chrome Stable rather than an unreliable Snap Chromium pair.",
        "Body2",
    ),
    code(
        "sudo bash scripts/setup-ubuntu-chrome.sh\n"
        "google-chrome --version\n"
        "mvn --batch-mode clean test -Psmoke -Denv=qa -Dbrowser=chrome -Dheadless=true"
    ),
    P("Clean and diagnostic runs", "H2"),
    code(
        "mvn --batch-mode --no-transfer-progress clean test -Punit\n"
        "mvn --batch-mode --no-transfer-progress clean test -Psmoke -Dbrowser=firefox\n"
        "mvn --batch-mode --no-transfer-progress clean test -Pregression -DretryCount=2"
    ),
    P(
        "Use retryCount=2 for diagnostic only. It permits up to three total attempts and can make failure-to-pass "
        "history ambiguous. Keep it at zero for release gates.",
        "Callout",
    ),
    PageBreak(),

    P("14. Troubleshooting", "H1"),
    table([
        ["Symptom", "Likely checks", "Recommended action"],
        ["Browser does not start", "Browser binary, driver availability, OS restrictions, Selenium logs", "Check version and capabilities; verify Chrome/Firefox/Edge installation; run one test locally."],
        ["Invalid baseUrl", "Environment/property precedence", "Print effective -D values and confirm HTTP(S) host; use -DbaseUrl only for an authorized target."],
        ["Unsupported browser", "browser setting", "Use chrome, firefox, or edge exactly; retry after correcting the property/JVM override."],
        ["Grid connection failure", "remoteUrl, network/DNS, Grid health, proxy/firewall", "Confirm the Grid endpoint, session allocation, and browser capability."],
        ["Chrome/ChromeDriver mismatch", "Browser and driver major versions", "In CI pin CHROME_VERSION or use setup-chrome matching; validate major versions."],
        ["Parallel flakiness", "Shared application state, data, screenshots, timeouts", "Lower thread count, isolate accounts/data, and reproduce with one method first."],
        ["Test passes after retry but initial attempt failed", "retryCount, report interpretation", "Set retryCount=0 for gate runs and inspect failed-attempt evidence separately."],
        ["Missing reports", "Test crash, cleanup, artifact path", "Verify target directories and runner upload condition; collect Surefire and framework logs."],
        ["Dependency scan missing reports", "Network/NVD, database update, plugin exception", "Check workflow logs and report directory; validate HTML/JSON files before upload."],
        ["Staging uses duplicate sandbox URL", "STAGING_BASE_URL/staging.properties", "QA and staging share the SauceDemo sandbox unless STAGING_BASE_URL is configured."],
    ], [43 * mm, 53 * mm, 64 * mm]),
    P("General debugging sequence", "H2"),
    *bullets([
        "Reproduce a focused test locally.",
        "Run with one thread and headless or a visible browser.",
        "Verify effective environment and browser.",
        "Inspect log, screenshot, URL, and Surefire failure together.",
        "Fix application setup or framework synchronization rather than adding a blind delay.",
        "Rerun repeatedly before returning concurrency to the original level.",
    ]),
    P("15. Extension Guide", "H1"),
    P("Adding a browser test", "H2"),
    table([
        ["Step", "Implementation"],
        ["1. Define behavior", "Add a scenario to a TestNG test class with an appropriate smoke/regression group."],
        ["2. Prepare data", "Use TestDataReader or an approved fixture/API setup; avoid private credentials in source."],
        ["3. Implement UI", "Add or update a page object; keep locators and screen actions localized."],
        ["4. Synchronize", "Use WaitHelper/expected conditions for dynamic state."],
        ["5. Assert", "Add clear TestNG assertions for user-visible outcomes."],
        ["6. Capture steps", "Use TestStepReporter for important actions when reporting is enabled."],
        ["7. Run locally", "Execute unit, focused browser, smoke, and regression suites as appropriate."],
        ["8. Add CI coverage", "Update suite/profile or matrix only after local stability is established."],
    ], [38 * mm, 122 * mm]),
    P("Adopting for a client application", "H2"),
    *bullets([
        "Replace the SauceDemo base URL with an authorized environment.",
        "Use dedicated test accounts and isolated test data.",
        "Implement application setup/reset/teardown through APIs or data fixtures.",
        "Define browser support and viewport requirements explicitly.",
        "Set timeouts and retries based on measured behavior, not guesswork.",
        "Define artifact data-retention and sensitivity rules.",
        "Add environment-specific secrets and approved CI variables.",
        "Establish pass-rate, flake, and runtime baselines before making release decisions.",
    ]),
    P("16. Known Limitations and Roadmap", "H1"),
    table([
        ["Priority", "Limitation", "Recommendation"],
        ["P0", "Dependency findings remain untriaged.", "Investigate CPE/asset matches and remediation candidates; establish CVE gate policy after triage."],
        ["P0", "Security workflow report presence is now validated.", "CI fails clearly for missing/empty HTML/JSON while retaining if-always artifact upload."],
        ["P1", "Public shared data can interfere with parallel runs.", "Adopt isolated accounts/data and setup/reset APIs before scaling threads."],
        ["P1", "QA and staging duplicate the sandbox.", "Shared SauceDemo sandbox is intentional; override STAGING_BASE_URL for a separate authorized environment."],
        ["P1", "Edge is supported by code but not covered by CI.", "Add Edge smoke coverage if Edge is part of the supported-browser contract."],
        ["P1", "Retry reporting may combine attempt history.", "Define attempt versus final-outcome semantics in the report."],
        ["P2", "No measured reliability baseline exists.", "Capture pass rate, runtime, flaky tests, browser versions, and environment state."],
        ["P2", "No full API/data setup layer exists.", "Add reusable API/data fixtures for client systems."],
        ["P2", "Artifact sensitivity/retention rules are not application-specific.", "Define redaction and retention before adding customer content."],
    ], [22 * mm, 64 * mm, 74 * mm]),
    P("Readiness conclusion", "H2"),
    P(
        "The repository provides a credible Java/Selenium/TestNG foundation: it separates lifecycle, configuration, "
        "drivers, pages, and evidence while offering local, remote, and CI execution. It is a strong portfolio/demo "
        "baseline. Before describing it as enterprise or production ready, teams should isolate test data, complete "
        "dependency triage, validate target environments and browsers, establish reliability baselines, and implement "
        "artifact and CI governance.",
        "Callout",
    ),
    PageBreak(),

    P("Appendix A. Command Reference", "H1"),
    table([
        ["Purpose", "Command"],
        ["Unit", "mvn --batch-mode test -Punit"],
        ["Smoke", "mvn --batch-mode test -Psmoke"],
        ["Regression", "mvn --batch-mode test -Pregression"],
        ["Chrome smoke", "mvn --batch-mode test -Psmoke -Dbrowser=chrome -Dheadless=true"],
        ["Firefox smoke", "mvn --batch-mode test -Psmoke -Dbrowser=firefox -Dheadless=true"],
        ["QA regression", "mvn --batch-mode test -Pregression -Denv=qa"],
        ["Staging regression", "mvn --batch-mode test -Pregression -Denv=staging -DbaseUrl=https://authorized.example.test"],
        ["Remote Grid", "mvn --batch-mode test -Pregression -DremoteUrl=http://grid:4444 -Dbrowser=chrome"],
        ["Diagnostic retries", "mvn --batch-mode test -Pregression -DretryCount=2"],
        ["Security scan", "mvn --batch-mode --no-transfer-progress -Psecurity verify -DskipTests"],
        ["Clean unit run", "mvn --batch-mode --no-transfer-progress clean test -Punit"],
        ["Clean smoke run", "mvn --batch-mode --no-transfer-progress clean test -Psmoke"],
    ], [48 * mm, 112 * mm]),
    P("Appendix B. Key Source Reference", "H1"),
    table([
        ["Topic", "Repository path"],
        ["Maven dependencies/profiles", "pom.xml"],
        ["Browser lifecycle", "src/test/java/.../framework/base/BaseTest.java"],
        ["Page base", "src/test/java/.../framework/base/BasePage.java"],
        ["Configuration access", "src/test/java/.../framework/config/FrameworkConfig.java"],
        ["Property precedence", "src/test/java/.../framework/config/ConfigReader.java"],
        ["Driver factory", "src/test/java/.../framework/driver/DriverFactory.java"],
        ["Thread-local state", "src/test/java/.../framework/driver/DriverManager.java"],
        ["Test listener", "src/test/java/.../framework/listeners/TestListener.java"],
        ["Retry behavior", "src/test/java/.../framework/listeners/RetryAnalyzer.java"],
        ["Smoke suite", "src/test/resources/suites/smoke.xml"],
        ["Regression suite", "src/test/resources/suites/regression.xml"],
        ["Browser CI", ".github/workflows/selenium-tests.yml"],
        ["Security CI", ".github/workflows/dependency-check.yml"],
    ], [52 * mm, 108 * mm]),
    P("Appendix C. Validation Checklist", "H1"),
    *bullets([
        "JUnit/TestNG unit profile succeeds.",
        "Test compilation succeeds on Java 25.",
        "Smoke suite passes in at least Chrome and Firefox.",
        "Regression suite passes with the agreed environment and test data.",
        "Browser and driver major versions match.",
        "Surefire, Extent, logs, and screenshots are present for a failed run.",
        "Retry count is zero for release-gate CI.",
        "Remote URL works if Grid execution is supported.",
        "Dependency HTML and JSON reports are generated and reviewed.",
        "GitHub Actions artifacts upload successfully.",
    ]),
]

doc.multiBuild(story)
print(OUT.resolve())
