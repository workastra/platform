"""Convert sqruff JSON lint output to SARIF 2.1.0 format."""

import hashlib
import json
import os
from pathlib import Path
from urllib.parse import quote

SARIF_SCHEMA = "https://raw.githubusercontent.com/oasis-tcs/sarif-spec/master/Schemata/sarif-schema-2.1.0.json"
SQRUFF_VERSION = "0.38.0"
SEVERITY_MAP = {"Warning": "warning", "Error": "error", "Note": "note"}


def uri(path: str) -> str:
    return quote(path, safe="/")


def to_region(r: dict) -> dict:
    return {
        "startLine":   max(r["start"]["line"], 1),
        "startColumn": max(r["start"]["character"], 1),
        "endLine":     max(r["end"]["line"], 1),
        "endColumn":   max(r["end"]["character"], 1),
    }


def fingerprint(rule_id: str, file_uri: str, line: int, message: str) -> str:
    return hashlib.sha256(f"{rule_id}:{file_uri}:{line}:{message}".encode()).hexdigest()


def build_rule(rule_id: str) -> dict:
    return {
        "id": rule_id,
        "shortDescription": {"text": f"sqruff rule {rule_id}"},
        "helpUri": f"https://docs.sqruff.com/rules/{rule_id.lower()}",
        "properties": {"tags": ["sql", "sqruff"]},
    }


def build_result(file_path: str, diag: dict) -> dict:
    rule_id = diag.get("code") or "unknown"
    file_uri = uri(file_path)
    line = diag["range"]["start"]["line"]
    message = diag["message"]

    return {
        "ruleId": rule_id,
        "level": SEVERITY_MAP.get(diag.get("severity", "Warning"), "warning"),
        "message": {"text": message},
        "partialFingerprints": {"sqruffFingerprint/v1": fingerprint(rule_id, file_uri, line, message)},
        "locations": [{
            "physicalLocation": {
                "artifactLocation": {"uri": file_uri, "uriBaseId": "%SRCROOT%"},
                "region": to_region(diag["range"]),
            }
        }],
    }


def build_sarif(data: dict) -> dict:
    pairs = [(fp, diag) for fp, diags in data.items() for diag in diags]
    rules = sorted({diag.get("code") for _, diag in pairs if diag.get("code")})

    return {
        "$schema": SARIF_SCHEMA,
        "version": "2.1.0",
        "runs": [{
            "tool": {
                "driver": {
                    "name": "sqruff",
                    "informationUri": "https://github.com/quarylabs/sqruff",
                    "version": SQRUFF_VERSION,
                    "rules": [build_rule(r) for r in rules],
                }
            },
            "results":   [build_result(fp, diag) for fp, diag in pairs],
            "artifacts": [
                {"location": {"uri": uri(fp), "uriBaseId": "%SRCROOT%"}}
                for fp in data
            ],
        }],
    }


def main() -> None:
    tmp = Path(os.environ["RUNNER_TEMP"])
    input_path  = tmp / "sqruff-raw.json"
    output_path = tmp / "sqruff.sarif"

    data = json.loads(input_path.read_text())
    output_path.write_text(json.dumps(build_sarif(data), indent=2))

    total = sum(len(v) for v in data.values())
    print(f"✅ {total} violation(s) across {len(data)} file(s) → {output_path}")
    if not total:
        print("🎉 No violations — clean SARIF will auto-close stale alerts")


if __name__ == "__main__":
    main()
