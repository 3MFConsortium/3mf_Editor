# CLI conformance suite

Runs the 3MF Editor CLI against all model files in all 11 official suites and verifies its XML results using the positive/negative convention in section 5.1 of the [3MF Test Specification v2.4.1](https://github.com/3MFConsortium/test_suites/blob/master/test_specification/3MF_Test_Specification_v2_4_1.docx). Reads Appendix C directly from the DOCX to check that the checkout contains each specified suite/scenario/iteration. Duplicate Appendix C rows are treated as one membership entry.

## Run

Requires Windows PowerShell 5.1, Java and `javac` (JDK 17 or newer), and a local checkout of [3MFConsortium/test_suites](https://github.com/3MFConsortium/test_suites). The existing CLI creates Swing components even in CLI mode, so it requires a desktop session. A headless Linux CI agent cannot run it as-is.

From the `3MF` directory:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tests/cli/Run-Conformance.ps1 -TestSuiteRoot C:/path/to/test_suites
```

The default checkout location is `test_suites` beside the `3mf_Editor` repository. Sources are compiled into a fresh run directory; existing editor binaries and settings are not changed. To test a packaged executable instead, add `-Jar C:/path/to/3MFedit.jar` (only Java is needed).

Optional parameters:

| Parameter | Purpose |
| --- | --- |
| `-Suite 3` | Run one suite; defaults to all 11. From an existing PowerShell session, `& ./tests/cli/Run-Conformance.ps1 -Suite 3,5,8` selects several. |
| `-InventoryOnly` | Check Appendix C coverage without compiling or executing Java. |
| `-Specification PATH` | Use a different local copy of the v2.4.1 DOCX. |
| `-SchemaRoot PATH` | Override the checkout's consolidated schema directory. |
| `-StrictWarnings` | Treat warnings on positive models as test failures. |
| `-BatchSize 25` | Opt into faster batch execution. Default is 1, so every model gets its own process and timeout. |
| `-TimeoutSeconds 180` | Maximum runtime per batch. |
| `-OutputRoot PATH` | Parent for fresh run directories (default `tests/cli/results`). |
| `-Java PATH`, `-Javac PATH` | Select Java executables. |

The runner invokes `-C -L files.txt -I`, enabling schema and structure validation, including structure checks following schema failures. Warnings remain visible. Every batch uses a new working directory, preventing stale results, extraction collisions, and collisions between identical filenames in different suites. All discovered files are submitted, including Secure Content and Production Alternatives cases; no extension is silently skipped. A crashed or timed-out batch is recorded as errors and execution continues with later batches. Use smaller batches to isolate the offending file.

The generated INI explicitly sets `pNoUUIDchk = "false"`, enabling UUID checks rather than using the editor's default UUID suppression. This is necessary to exercise UUID negative cases. The consolidated schema can consequently reject older core-only positive fixtures for absent Production UUID attributes; these are reported as mismatches, not waived. Error limits are set to 100 for both validation and structure checks.

The default one-model batches ensure a hanging model cannot prevent another model from being attempted. Larger batches reduce JVM startup overhead, but a hanging model can prevent later models in that batch from completing; the whole affected batch is then marked as errors. Logs and result files are retained for diagnosis. Cancellation terminates the active child process.

## Verification and reports

| Model expectation | CLI XML result | Suite outcome |
| --- | --- | --- |
| Positive (`P_`) | `Pass` or `Warning` | Passed (`Warning` fails with `-StrictWarnings`) |
| Positive (`P_`) | `Fail` | Failed |
| Negative (`N_`) | `Fail`, with diagnostic issue | Passed |
| Negative (`N_`) | `Pass` or `Warning` | Failed |
| Any | Missing, malformed, wrong-file or unknown XML result; crash; timeout | Error |
| Appendix C entry | No corresponding model in checkout | Error |

Process exit code zero alone never passes a model. A warning alone never verifies a negative case. Additional checkout models absent from Appendix C are still executed and listed in the summary. The current v2.4.1 document and checkout have some inconsistencies; missing entries remain visible errors rather than being silently waived.

Each run directory contains:

- `results.csv`: per-model expected/actual status, diagnostics, and XML report path, checkpointed after each batch.
- `junit.xml`: CI-compatible test results, including missing-model errors.
- `summary.json`: totals, coverage differences, specification URL/path/SHA256, checkout and schema paths, selected options, and completion flag.
- `inventory.csv`: every discovered model and its specification key.
- Batch directories: CLI HTML/XML reports, file lists, isolated settings, stdout and stderr logs.
- `compile.log` and `classes/` when building from source.

Exit codes: **0** all checks passed; **1** expectation failures or execution/coverage errors; **2** setup/build/runner failure. An inventory-only run also returns 1 for missing Appendix C entries. An interrupted run has no `Completed: true` in `summary.json`.

## Scope of automated verification

This suite verifies the CLI's schema/structure **acceptance or rejection**, not complete consumer or printer conformance. Section 5.3 and the scenario definitions also specify rendering, geometry, color/material appearance, physical printing, and Production Alternatives selection behavior. Those require a renderer/printer and manual comparison with the expected-result albums; the CLI exposes no machine-verifiable rendering result. A positive acceptance result does not prove its rendered appearance is correct. Diagnostic codes are retained for investigation, but v2.4.1 does not specify this editor's `ER_` codes, so no invented code-to-scenario assertions are used.

Secure Content cases use the editor's existing decryption behavior and the official test fixtures; the runner does not inject keys or waive unsupported behavior. Schema and fixture revisions affect results: retain `summary.json` and the checkout revision when comparing runs.

## Test the runner itself

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tests/cli/Test-Runner.ps1
```

These dependency-free checks cover acceptance/rejection, warning policy, malformed/missing/wrong-file XML, DTD rejection, suffixed filenames, suite selection, polarity mismatch, JUnit escaping/counts, nonzero subprocess exit, large output, and timeout handling.
