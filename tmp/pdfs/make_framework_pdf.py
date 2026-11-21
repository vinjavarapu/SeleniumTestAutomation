from pathlib import Path

from reportlab.lib import colors
from reportlab.lib.enums import TA_LEFT
from reportlab.lib.pagesizes import letter
from reportlab.lib.styles import ParagraphStyle
from reportlab.pdfbase.pdfmetrics import stringWidth
from reportlab.pdfgen import canvas
from reportlab.platypus import Paragraph


OUT = Path("output/pdf/Selenium_Automation_Framework_Overview.pdf")
OUT.parent.mkdir(parents=True, exist_ok=True)
PAGE_W, PAGE_H = letter
NAVY = colors.HexColor("#17324D")
TEAL = colors.HexColor("#008489")
INK = colors.HexColor("#243746")
MUTED = colors.HexColor("#5A6B7A")
PALE = colors.HexColor("#EAF3F4")
LIGHT = colors.HexColor("#F3F6F8")
WHITE = colors.white
LINE = colors.HexColor("#D8E1E7")

styles = {
    "body": ParagraphStyle("body", fontName="Helvetica", fontSize=9.2, leading=13.2, textColor=INK, alignment=TA_LEFT),
    "small": ParagraphStyle("small", fontName="Helvetica", fontSize=8.1, leading=11, textColor=MUTED),
    "card": ParagraphStyle("card", fontName="Helvetica", fontSize=8.5, leading=11.2, textColor=INK),
    "step": ParagraphStyle("step", fontName="Helvetica", fontSize=7.4, leading=9.4, textColor=INK, alignment=TA_LEFT),
}


def para(c, text, x, top, width, style="body"):
    p = Paragraph(text, styles[style])
    _, h = p.wrap(width, PAGE_H)
    p.drawOn(c, x, top - h)
    return h


def section(c, label, y):
    c.setFillColor(TEAL)
    c.roundRect(42, y - 3, 4, 17, 2, fill=1, stroke=0)
    c.setFillColor(NAVY)
    c.setFont("Helvetica-Bold", 12)
    c.drawString(54, y, label)


def card(c, x, y, w, h, title, text, tint=LIGHT):
    c.setFillColor(tint)
    c.roundRect(x, y, w, h, 8, fill=1, stroke=0)
    c.setFillColor(TEAL)
    c.roundRect(x, y, 4, h, 2, fill=1, stroke=0)
    c.setFillColor(NAVY)
    c.setFont("Helvetica-Bold", 9.4)
    c.drawString(x + 13, y + h - 19, title)
    para(c, text, x + 13, y + h - 28, w - 24, "card")


def bullet(c, text, x, y, width, style="body"):
    c.setFillColor(TEAL)
    c.circle(x + 3, y - 4, 2.1, fill=1, stroke=0)
    h = para(c, text, x + 12, y, width - 12, style)
    return h + 7


def footer(c, page):
    c.setStrokeColor(LINE)
    c.setLineWidth(0.7)
    c.line(42, 38, PAGE_W - 42, 38)
    c.setFillColor(MUTED)
    c.setFont("Helvetica", 7.8)
    c.drawString(42, 24, "SELENIUM JAVA TEST AUTOMATION  |  FRAMEWORK GUIDE")
    c.drawRightString(PAGE_W - 42, 24, f"{page} / 2")


def page_header(c, eyebrow, title, subtitle):
    c.setFillColor(NAVY)
    c.rect(0, PAGE_H - 154, PAGE_W, 154, fill=1, stroke=0)
    c.setFillColor(colors.HexColor("#8DE0DA"))
    c.setFont("Helvetica-Bold", 8.5)
    c.drawString(42, PAGE_H - 35, eyebrow.upper())
    c.setFillColor(WHITE)
    c.setFont("Helvetica-Bold", 25 if len(title) < 34 else 22)
    c.drawString(42, PAGE_H - 72, title)
    c.setFillColor(colors.HexColor("#D5E3EB"))
    c.setFont("Helvetica", 10.2)
    c.drawString(42, PAGE_H - 96, subtitle)
    c.setFillColor(colors.HexColor("#214864"))
    c.roundRect(42, PAGE_H - 133, 314, 22, 10, fill=1, stroke=0)
    c.setFillColor(WHITE)
    c.setFont("Helvetica-Bold", 8)
    c.drawString(54, PAGE_H - 125, "MAVEN   /   JAVA 25   /   SELENIUM 4   /   TESTNG")


c = canvas.Canvas(str(OUT), pagesize=letter, pageCompression=1)
c.setTitle("Selenium Java Test Automation Framework - Overview")
c.setAuthor("Automation Engineering")
c.setSubject("Two-page overview of the framework architecture and concurrent execution model")

# Page 1: framework purpose and architecture.
page_header(c, "Automation framework | overview", "Selenium Java Framework", "How the test code, browser sessions, and run evidence fit together")
section(c, "Purpose", 610)
para(c,
     "This Maven project automates user-facing workflows in the SauceDemo practice application. Selenium drives the browser, TestNG organizes and parallelizes tests, and page objects keep UI details reusable. The framework is designed to run locally or against a Selenium Grid and to produce evidence that developers can inspect after each run.",
     42, 594, PAGE_W - 84)

section(c, "Core capabilities", 528)
gap = 12
cw = (PAGE_W - 84 - gap * 2) / 3
card(c, 42, 448, cw, 64, "Reusable UI layer", "Page objects group locators, waits, and actions by application screen.")
card(c, 42 + cw + gap, 448, cw, 64, "Parallel sessions", "A fresh thread-local WebDriver is created for each test method.", PALE)
card(c, 42 + (cw + gap) * 2, 448, cw, 64, "Run evidence", "Extent HTML, Surefire XML, logs, and failure screenshots.")

section(c, "Execution lifecycle", 418)
steps = [
    ("1  CONFIGURE", "Choose env, browser, URL, and wait settings."),
    ("2  START", "BaseTest creates a browser session."),
    ("3  TEST", "Test calls page objects and assertions."),
    ("4  REPORT", "Listener records status and failure evidence."),
]
sw = (PAGE_W - 84 - 3 * 10) / 4
for i, (title, text) in enumerate(steps):
    x = 42 + i * (sw + 10)
    c.setFillColor(LIGHT)
    c.roundRect(x, 331, sw, 70, 7, fill=1, stroke=0)
    c.setFillColor(TEAL)
    c.setFont("Helvetica-Bold", 7.7)
    c.drawString(x + 9, 384, title)
    para(c, text, x + 9, 373, sw - 18, "step")
    if i < len(steps) - 1:
        c.setStrokeColor(TEAL)
        c.setLineWidth(1.4)
        c.line(x + sw + 1, 365, x + sw + 8, 365)
        c.line(x + sw + 5, 368, x + sw + 8, 365)
        c.line(x + sw + 5, 362, x + sw + 8, 365)

section(c, "Framework layers", 300)
left = [
    ("Test layer", "Login, smoke, inventory/cart, checkout, and catalogue regression scenarios."),
    ("Page layer", "Screen-level page objects use explicit waits and Selenium interactions."),
]
right = [
    ("Driver and config", "DriverFactory supports Chrome, Firefox, Edge, and RemoteWebDriver. FrameworkConfig resolves system overrides, environment properties, then defaults."),
    ("Reporting", "TestListener captures outcome, URL, and screenshots; ExtentReportManager creates a run-specific HTML report."),
]
for x, items in [(42, left), (PAGE_W / 2 + 6, right)]:
    y = 278
    for heading, text in items:
        c.setFillColor(NAVY)
        c.setFont("Helvetica-Bold", 9)
        c.drawString(x, y, heading)
        h = para(c, text, x, y - 9, PAGE_W / 2 - 58, "small")
        y -= h + 27

c.setFillColor(PALE)
c.roundRect(42, 75, PAGE_W - 84, 38, 7, fill=1, stroke=0)
c.setFillColor(NAVY)
c.setFont("Helvetica-Bold", 8.5)
c.drawString(54, 98, "TEST SCOPE")
para(c, "The current suite targets the public SauceDemo demo app. It is UI coverage, not a complete API, monitoring, or release-governance platform.", 122, 101, PAGE_W - 178, "small")
footer(c, 1)
c.showPage()

# Page 2: concurrency, environments, and practical operations.
page_header(c, "Execution model | operations", "Concurrent runs, safely", "Parallel tests within a run; isolated processes across environments")
section(c, "Two levels of concurrency", 610)
card(c, 42, 525, (PAGE_W - 96) / 2, 68, "Inside one Maven process", "TestNG runs methods in parallel. BaseTest creates and closes one browser per method; DriverManager and report context are thread-local.", PALE)
card(c, 54 + (PAGE_W - 96) / 2, 525, (PAGE_W - 96) / 2, 68, "Across environments and browsers", "Use separate Maven JVMs / CI matrix jobs. Environment config is fixed at startup, so processes cannot overwrite each other's target URL.")

section(c, "Environment configuration", 500)
y = 479
y -= bullet(c, "Precedence: JVM system property > selected <b>config/{env}.properties</b> > default.properties.", 48, y, PAGE_W - 96)
y -= bullet(c, "Common overrides: <b>-Denv=qa -Dbrowser=chrome -Dheadless=true</b>; use <b>-DbaseUrl=...</b> for a runtime target.", 48, y, PAGE_W - 96)
y -= bullet(c, "Staging CI matrix jobs are enabled by the GitHub Actions repository variable <b>STAGING_BASE_URL</b>; otherwise the matrix runs QA only.", 48, y, PAGE_W - 96)
y -= bullet(c, "Keep credentials out of property files. Use an approved secret store for private environments; demo credentials are public test data.", 48, y, PAGE_W - 96)

section(c, "Run examples", y - 5)
code_y = y - 31
c.setFillColor(NAVY)
c.roundRect(42, code_y - 56, PAGE_W - 84, 57, 7, fill=1, stroke=0)
c.setFillColor(colors.HexColor("#A8E6DF"))
c.setFont("Courier-Bold", 8.1)
c.drawString(54, code_y - 19, "mvn clean test -Psmoke -Denv=qa -Dbrowser=chrome -Dheadless=true")
c.drawString(54, code_y - 39, "mvn clean test -Pregression -Denv=staging -DremoteUrl=http://grid:4444")
para(c, "For staging, configure a reachable authorized URL in staging.properties or pass -DbaseUrl. Grid capacity must cover the test methods running at once.", 48, code_y - 67, PAGE_W - 96, "small")

section(c, "CI, evidence, and guardrails", 245)
card(c, 42, 150, (PAGE_W - 96) / 2, 78, "GitHub Actions", "Smoke runs on pull requests across Chrome and Firefox. Regression runs on main pushes and the nightly schedule using Chrome. Environments run in isolated jobs.")
card(c, 54 + (PAGE_W - 96) / 2, 150, (PAGE_W - 96) / 2, 78, "Artifacts and stability", "Jobs upload Extent HTML, Surefire XML, logs, and screenshots. Retries default to zero; explicit waits are preferred and implicit wait is zero.", PALE)
para(c, "Capacity guidance: set TestNG thread counts and CI matrix parallelism to available browser/Grid slots and the target environment's rate limits. Keep test data isolated per invocation.", 42, 135, PAGE_W - 84, "small")
footer(c, 2)
c.save()
print(OUT.resolve())
