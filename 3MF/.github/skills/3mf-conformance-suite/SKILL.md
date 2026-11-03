---
name: 3mf-conformance-suite
description: 'Run the 3MF Consortium test suites (positive and/or negative .3mf fixtures) through the 3MF Editor CLI and verify the accept/reject outcome against the 3MF Test Specification v2.4.1. Use when asked to execute, validate, regression-test or report on the 3MF conformance suites, check whether the CLI correctly accepts positive tests, or triage CLI mismatches.'
argument-hint: 'Optional: polarity (positive|negative|all), suites 1-11, strict warnings'
---

# 3MF conformance suite with the 3MF Editor CLI

Executes every `.3mf` in `test_suites/suite*` through `StartUp -C -L files.txt -I`, then compares the result with the polarity encoded in the file name (`P_` = must be accepted, `N_` = must be rejected) and with the scenario text in the Test Specification (Appendix C and scenario tables).

## Prerequisites

- Sibling checkout of `https://github.com/3MFConsortium/test_suites` at `<repo>/../../../test_suites` (relative to `tests/cli`), containing `test_specification/3MF_Test_Specification_v2_4_1.docx` and `Consolidated_Schema/Schema`. Override with `-TestSuiteRoot`, `-Specification`, `-SchemaRoot`.
- JDK 17+ on `PATH` (`java`, `javac`). Without `-Jar` the runner compiles `src/` into the run folder. Pass `-Jar <file>` to test a built jar instead.
- Windows PowerShell 5.1 or later. Run from the project root (the folder holding `3MF.ini`).

## Procedure

1. Inventory first (fast, no Java). Confirms fixtures exist and match Appendix C:
   ```powershell
   powershell -NoProfile -File tests\cli\Run-Conformance.ps1 -Polarity Positive -InventoryOnly -OutputRoot $env:TEMP\3mf-inv
   ```
   Expect the missing/additional counts to be small. Missing fixtures are reported as `Error` in a real run.

2. Execute. Use `-BatchSize 25` (one JVM per 25 files) and a large timeout; the default of 1 means one JVM per model.
   `p:UUID` is required only by the Production extension tests, which are suites 1, 2 and 5 (`$ProductionSuites` in `Run-Conformance.ps1`); in all other suites it is optional. `-UuidMode Auto` (default) runs suites 1, 2, 5 with UUID checks on (`pNoUUIDchk=false`) and all other suites with UUID errors suppressed (`pNoUUIDchk=true`, the editor default):
   ```powershell
   powershell -NoProfile -File tests\cli\Run-Conformance.ps1 -Polarity Positive -BatchSize 25 -TimeoutSeconds 1200 -OutputRoot tests\cli\results\positive
   ```
   - `-UuidMode Strict` forces UUID checks in every suite (shows the schema false rejections, or for UUID negative cases); `-UuidMode Ignore` suppresses them in every suite.
   - `-Polarity Positive|Negative|All` (default `All`), `-Suite 1,2,...` (default 1-11). Pass several suites by calling the script with `& .\tests\cli\Run-Conformance.ps1 -Suite 3,5 ...`; `powershell -File ... -Suite 3,5` does not bind an int array.
   - `-StrictWarnings` treats a CLI `Warning` result as a failure of a positive test.
   - Exit code: 0 all matched, 1 mismatches or errors, 2 runner failure. Each run writes `inventory.csv`, `results.csv`, `junit.xml`, `summary.json` to a timestamped folder under `-OutputRoot` (git-ignored).
   - Takes several minutes for all ~1700 positive models. Run it in a terminal without a short timeout.

3. Compare with the specification (does not rerun Java; verifies the spec SHA256 matches the run):
   ```powershell
   $run = (Get-ChildItem tests\cli\results\positive -Directory | Sort-Object Name | Select-Object -Last 1).FullName
   powershell -NoProfile -File tests\cli\Compare-Specification.ps1 -RunDirectory $run
   ```
   Output in `<run>\specification-match\`: `expected-vs-observed.csv` (one row per model with scenario, criteria, iteration text, CLI diagnostic), `scenario-definitions.json`, `comparison.md`, `summary.json`.

4. Triage every row where `AcceptanceVerification` is not `Match`:
   ```powershell
   Import-Csv "$run\specification-match\expected-vs-observed.csv" | Where-Object AcceptanceVerification -ne 'Match' |
     Select-Object Suite, Test, AcceptanceVerification, ScenarioKey, Iteration, SpecificationCriteria, OriginalDiagnostic | Format-List
   ```
   Open the raw CLI XML in `Report` and the fixture (`.3mf` is a ZIP) for each, then classify as: CLI bug, fixture defect, consumer-specific criterion, or expected limitation (see below).

## Interpreting results

- `Passed` = CLI result is `Pass`, or `Warning` (unless `-StrictWarnings`) for a positive; `Fail` for a negative.
- `Failed` = verified mismatch. `Error` = no trustworthy result (CLI wrote no/invalid XML, timeout, missing fixture). Never count an `Error` as a pass.
- CLI acceptance is only a schema/structure check. It does not prove rendering, selected alternative, colour or print appearance, and a rejected negative may be rejected for an unrelated reason. State this limit in any report.
- `P_0720`, `P_0722`, `P_0740`, `N_2604` are consumer-specific (printer vs viewer, encryption keys); the compare step flags them.

## Known baseline (spec v2.4.1, test_suites commit a63f4bf, 1719 positive models)

| `-UuidMode` | Passed | Failed | Error |
| --- | ---: | ---: | ---: |
| Strict (all suites) | 1023 | 693 | 5 |
| Ignore (all suites) | 1706 | 10 | 5 |
| Auto (default; suites 1, 2, 5 strict) | 1706 | 10 | 5 |

- Strict-mode mismatches are almost all one cause: `qli_3MF.xsd` marks `p:UUID` as required on `object`/`item`/`build`, so fixtures without the Production extension (suites 3, 4, 6 and 9) fail with `Attribute 'UUID' ... must appear`. UUIDs are required only in the Production extension tests and optional in all others, so these are false rejections caused by the consolidated schema (a schema/CLI defect), not fixture problems. Do not count them as valid mismatches for non-production tests; use `Auto` (or `Ignore`) as the result for those suites. Suites 1, 2 and 5 pass strict UUID checks (apart from the findings below), which confirms the production fixtures carry UUIDs.
- Missing fixtures: `10/P_3006_01`, `11/P_3212_04` (listed in Appendix C, absent from checkout).
- Remaining findings in `Auto`/`Ignore` mode to review (verify before filing as bugs):
  - `P_0102_03` (suites 1, 4): thumbnail named `*.model` with an Override to `image/png` is parsed as XML ("Content is not allowed in prolog"). CLI appears to choose by extension, not the Override: likely CLI bug.
  - `P_0104_04` (suites 1, 4): ER_183 rejects the unescaped Unicode ZIP part name that the test intentionally includes: CLI is stricter than the test expects.
  - `P_0909_01` (suites 1, 4): ER_132, Override `PartName` values lack the leading `/`: fixture defect (OPC), CLI correct.
  - `P_2202_04` (suite 9): `ww:unrecognized` attribute rejected by the schema, test expects the unrecognized namespace to be ignored: likely schema/CLI deviation.
  - `P_0740_01..06` (suite 5): need Secure Content keys; CLI cannot decrypt, reports ER_057/WR_019, and 01/05/06 produce malformed report XML (unescaped `<?xml` text in a diagnostic): the report generator is a CLI bug; the rest is a consumer-specific limitation.

## Negative tests

Run with `-Polarity Negative` (same `Auto` UUID mode, same compare step). A negative passes only when the CLI result is `Fail`; `Warning` or `Pass` is a mismatch. Check the raw report too: the CLI computes `Pass`/`Fail` from `bChkError`, and schema errors caused only by elements outside `http://schemas.microsoft.com/3dmanufacturing/` are tolerated (see `MainFrame.xmlValid`), so a report can say `result="Pass"` while holding a critical `PrimaryIssue`.

Baseline (spec v2.4.1, 552 models): 444 matched, 78 mismatched, 30 unverifiable (malformed report XML), 0 missing fixtures. Unverifiable cases are N_0206, N_0422, N_2602 to N_2607 and N_3302; their report headers read `Fail` (provisional only).

## Rules

- Do not edit fixtures in `test_suites`. Do not waive mismatches; report them with their classification.
- Keep all Java test sources and helpers in `tests/cli/src/`. Update regression-script compilation paths when moving or adding helpers.
- Keep results under `tests\cli\results\` (git-ignored). Record the spec SHA256 and test_suites commit with any report.
