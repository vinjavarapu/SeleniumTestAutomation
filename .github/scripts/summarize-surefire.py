#!/usr/bin/env python3
"""Generate a Markdown summary from Maven Surefire XML reports for GitHub Actions."""

from pathlib import Path
import os
import sys
import xml.etree.ElementTree as ET


def main() -> int:
    title = sys.argv[1] if len(sys.argv) > 1 else "Test Results"
    reports_dir = Path("target/surefire-reports")
    xml_files = sorted(reports_dir.glob("TEST-*.xml"))

    total = failures = errors = skipped = 0
    duration = 0.0
    failed_cases: list[str] = []

    for xml_file in xml_files:
        root = ET.parse(xml_file).getroot()
        total += int(root.attrib.get("tests", 0))
        failures += int(root.attrib.get("failures", 0))
        errors += int(root.attrib.get("errors", 0))
        skipped += int(root.attrib.get("skipped", 0))
        duration += float(root.attrib.get("time", 0.0))

        for testcase in root.findall("testcase"):
            failure = testcase.find("failure")
            error = testcase.find("error")
            issue = failure if failure is not None else error
            if issue is not None:
                name = f"{testcase.attrib.get('classname', '')}.{testcase.attrib.get('name', '')}"
                message = (issue.attrib.get("message") or issue.attrib.get("type") or "Failed").strip()
                failed_cases.append(f"- `{name}`: {message.splitlines()[0]}")

    passed = max(0, total - failures - errors - skipped)
    lines = [
        f"### {title}",
        "",
        "| Total | Passed | Failed | Errors | Skipped | Duration (s) |",
        "|---:|---:|---:|---:|---:|---:|",
        f"| {total} | {passed} | {failures} | {errors} | {skipped} | {duration:.2f} |",
        "",
    ]

    if failed_cases:
        lines.append("**Failures:**")
        lines.extend(failed_cases)
        lines.append("")

    markdown = "\n".join(lines)
    summary_path = os.environ.get("GITHUB_STEP_SUMMARY")
    if summary_path:
        with open(summary_path, "a", encoding="utf-8") as handle:
            handle.write(markdown + "\n")
    else:
        print(markdown)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
