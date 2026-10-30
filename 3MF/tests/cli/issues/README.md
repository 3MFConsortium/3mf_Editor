# Issue fixes and CLI regression checks

Keep issue-fix documentation in this file and all `Test-*.ps1` regression scripts
in `tests/cli/issues/`. Commands below run from the `3MF` project directory. Shared CLI
utilities remain in `tests/cli/`, alongside the ignored generated reports.
Java test helper sources live in `tests/src/` and are compiled explicitly by the
regression scripts.

Runs the 3MF Editor CLI against all model files in all 11 official suites and verifies its XML results using the positive/negative convention in section 5.1 of the [3MF Test Specification v2.4.1](https://github.com/3MFConsortium/test_suites/blob/master/test_specification/3MF_Test_Specification_v2_4_1.docx). Reads Appendix C directly from the DOCX to check that the checkout contains each specified suite/scenario/iteration. Duplicate Appendix C rows are treated as one membership entry.

## Issue #19 investigation: documentation link points to a personal path

[Issue #19](https://github.com/3MFConsortium/3mf_Editor/issues/19) is reproduced
on the public [3MF Editor v1.0.0 announcement](https://3mf.io/announcement/2026/05/3mf-editor-v1-0-0-now-available/),
checked on 2026-10-30. The **Full documentation** anchor in the sentence
"Full documentation is included in the release" has this literal `href`:

```text
///Users/duann/Documents/BITS%20to%20ATOMS/3MF/3MF_Editor_Guide_v1_00.pdf
```

On the HTTPS page, browsers resolve this malformed reference to
`https://users/duann/Documents/BITS%20to%20ATOMS/3MF/3MF_Editor_Guide_v1_00.pdf`,
exactly matching the issue body. It does not retrieve a file from the author's
computer; `users` becomes the network hostname. The only issue comment asks where
the link was found and contains no additional reproduction details.

The website maintainer should replace that anchor's destination with the
[v1.0.0 release PDF](https://github.com/3MFConsortium/3mf_Editor/releases/download/v1.0.0/3MF_Editor_Guide_v1_00.pdf).
An HTTP HEAD request followed redirects and returned 200 on 2026-10-30. The
repository also provides the [Markdown guide](../../../Documentation/3MF_Editor_Guide_v1_00.md).
No occurrence of the personal URL was found in current repository text sources,
the public README, or the release description. The affected website content is
outside this checkout; this investigation changes no production code and does
not modify the website or post an issue comment.

Manual regression after the website correction: open the announcement, click
**Full documentation**, and confirm that the public release PDF opens and the
destination no longer contains the personal path or the `users` hostname.
No CLI conformance test applies to this website link.

Separate finding: `MainFrame.java`'s Help action still opens a local
`3MF_Editor_Guide_v1_00.pdf`, falling back to an embedded resource of that name.
Commit `7da2102` removed the embedded PDF on 2026-10-29 after the Markdown
conversion in `1527bac`. At the user's request, the original 178,457-byte PDF
has been restored unchanged from `7da2102^` to
`src/resources/3MF_Editor_Guide_v1_00.pdf`, making the embedded fallback available
again when resources are packaged. Its Git blob ID matches the original:
`2c3a36ccb79816490cece58041592bfcbe0974e8`. This is distinct from the website
link reported in #19; the Help action contains no personal URL.

## Run

Requires Windows PowerShell 5.1, Java and `javac` (JDK 17 or newer), and a local checkout of [3MFConsortium/test_suites](https://github.com/3MFConsortium/test_suites). CLI mode selects headless AWT before initializing Swing and does not require a desktop session or X11 DISPLAY.

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

To check archive extraction and natural filename ordering without a desktop session
or the official fixtures, run:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tests/cli/issues/Test-ZipFileUtil.ps1
```

This compiles the archive utility and runs filesystem regressions for all three
extraction methods, path traversal and absolute paths, preserving source read-only
attributes, preserving unrelated `ztmp` contents during fallback, and comparator
ordering (including underscores, leading zeros and numbers larger than a `long`).
Generated fixtures and logs are retained under `tests/cli/results/zip-utils-*`.

To verify alternative object references (issue #3), run:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tests/cli/issues/Test-AlternativeReferences.ps1
```

This compiles the current sources and checks all 11 official `P_XPX_0720` fixtures,
plus generated cases for same-model forward references, missing objects, references
to the wrong model, and nested alternatives. It uses the same default test-suite
checkout as the conformance runner; override it with `-TestSuiteRoot PATH`.
Reports are saved under `tests/cli/results/alternative-references-*`.

To verify beam minimum-length handling, run:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tests/cli/issues/Test-BeamMinlength.ps1
```

This checks positive and negative beam fixtures and existing cross-extension
cases. Reports are saved under `tests/cli/results/beam-minlength-*`.

To verify slice-only transform restrictions (issue #4), run:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tests/cli/issues/Test-Transforms.ps1
```

This checks all 11 official `P_XPX_0720` fixtures and generated item/component
cases for identity, scaling, and rotation, with and without slice references.
It checks both HTML and XML for spurious transform diagnostics and verifies that
invalid slice transforms still fail with the expected diagnostic. Two cases retain
an unrelated UUID error to exercise the XML diagnostic leak from issue #4. The generated
fixtures include Production UUIDs required by the consolidated schema.
Use `-TestSuiteRoot PATH` to override the default checkout. Reports are saved
under `tests/cli/results/transforms-*`.

To verify duplicate content-type overrides (issue #6), run:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tests/cli/issues/Test-ContentTypeOverrides.ps1
```

Overrides now report `ER_108` when a PartName has already appeared, using the
same case-insensitive comparison as override lookup. The check applies even
when the two overrides specify different content types. It updates both HTML
and XML diagnostics, primary-issue selection, and the structure error count.
Different parts may share a content type, and a Default and an Override may
both cover a part.

The regression checks all 12 official `N_*_0205_01`/`N_*_0205_02` fixtures in
suites 1 through 6, plus generated single, case-variant, conflicting-type,
distinct-part, and Default-plus-Override cases. It requires the duplicate
diagnostic in both HTML and XML, so unrelated fixture errors cannot pass the
regression. Use `-TestSuiteRoot PATH` to override the checkout location.
Reports are saved under `tests/cli/results/content-type-overrides-*`.

The issue cites `N_XPM_0204_02.3mf`, but that file contains no overrides in the
local checkout or upstream copy inspected during investigation. The v2.4.1 test
specification assigns duplicate content types to scenario `0205`; the actual
duplicate-override fixture is `N_XPM_0205_02.3mf`. The reported `0204` fixture
tests thumbnail target capitalization and is not changed by this fix.

To match an existing completed run to the scenario definitions and criteria, without rerunning the CLI:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tests/cli/Compare-Specification.ps1 -RunDirectory tests/cli/results/YOUR-RUN
```

This writes `specification-match/expected-vs-observed.csv`, `comparison.md`, `summary.json`, and `scenario-definitions.json` inside that run directory. It verifies the specification hash, links every model to its scenario, preserves the original result status, and flags consumer-specific criteria. Header-only results from malformed XML are labeled provisional and do not turn report errors into verified passes. Rendering and diagnostic-reason correctness remain outside acceptance-only matching.

To check the harness itself:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tests/cli/issues/Test-Runner.ps1
```

These dependency-free checks cover acceptance/rejection, warning policy, malformed/missing/wrong-file XML, DTD rejection, suffixed filenames, suite selection, polarity mismatch, JUnit escaping/counts, nonzero subprocess exit, large output, and timeout handling.

## Issue #7: beam lattice object types

[Issue #7](https://github.com/3MFConsortium/3mf_Editor/issues/7) reports that
`N_BXX_2503_02.3mf` is accepted despite containing a beam lattice in object 22,
whose type is `support`. The beam lattice extension, section 2, requires the
containing mesh object to have type `model` or `solidsupport`. Core defaults an
omitted object type to `model`.

The beam lattice validation checked geometry and references but omitted this
restriction. It now reports `ER_177` (`invalid object type`) in HTML and XML,
includes the model part, object ID and offending type, updates the structure
error count, and registers the diagnostic for primary-issue selection. Objects
without beam lattices are unaffected by this rule.

Run from the `3MF` project directory (JDK 17+ required):

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tests/cli/issues/Test-BeamObjectTypes.ps1
```

The script compiles current sources and checks the official reported fixture,
generated `support` and `other` negative cases, explicit `model` and
`solidsupport`, omitted type, and a `support` object without a beam lattice.
It verifies acceptance/rejection, the diagnostic in both report formats, and
primary-issue selection. Use `-TestSuiteRoot PATH` to override the default
`test_suites` checkout beside the editor repository. Reports remain under
`tests/cli/results/beam-object-types-*`.

Validation: all seven object-type cases pass, as do all twelve existing cases
in `tests/cli/issues/Test-BeamMinlength.ps1`.

## Issue #8: Secure Content decryption in CLI validation

[Issue #8](https://github.com/3MFConsortium/3mf_Editor/issues/8) reports that CLI
validation does not decrypt Secure Content. Package keys were loaded by the GUI's
thumbnail setup, which `cliOpenFile` never runs. Schema validation silently skipped
encrypted XML, and structure checks could not inspect the encrypted models.

Package extraction now loads the keystore before validation, for both single-file
(`-F`) and file-list (`-L`) execution. The key list is cleared before keystore
discovery, including when root relationships are absent, so later packages cannot
reuse previous package keys. Existing cryptographic algorithms and key selection
are unchanged.

Run from the `3MF` project directory (JDK 17+ required):

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tests/cli/issues/Test-SecureContent.ps1
```

Use `-TestSuiteRoot PATH` to override the default official fixture checkout beside
the editor repository. The script compiles current sources and the shared Java
fixture helper, then checks default, schema-only and structure-only CLI modes,
SHA-1 and SHA-256 wrapping, deflated and uncompressed content, and multiple files
in one process. Generated fixtures contain an authenticated encrypted schema error
or out-of-range vertex reference; their specific diagnostics must appear in both
HTML and XML. A separate opening-path check verifies key loading and clearing,
including a package without a keystore and absent root relationships.

The unpatched source incorrectly reports `Pass` for the encrypted schema-error
fixture with `-C -F schema.3mf -V`; the patched source reports `Fail` and identifies
`invalid-unit`. Reports and generated fixtures remain under
`tests/cli/results/secure-content-*`. A narrow exception in `tests/cli/.gitignore`
keeps the new regression script visible despite the repository's issue-directory
ignore rule.

Validation: all 11 regression checks pass, including the independent key-reset
check. The encrypted schema-error fixture was also run against the unpatched
source to confirm its incorrect acceptance.

## Issue #11: headless CLI execution

[Issue #11](https://github.com/3MFConsortium/3mf_Editor/issues/11) reports that CLI
execution requires an X11 DISPLAY. Startup constructed a JFrame even with `-C`,
and structure validation accessed that window's glass pane. Forcing
`-Djava.awt.headless=true` reproduced a HeadlessException before validation.

MainFrame now holds the editor content and creates its JFrame only in GUI mode.
CLI startup selects headless AWT before constructing Swing components; shared
validation and HTML reporting still use lightweight Swing components. Window
setup, menu updates and the structure-check busy cursor are skipped in CLI mode.
Directory errors and a missing print job ticket use logging without CLI dialogs.
The editor retains its window, menus, layout, saved geometry and close behavior.

Run from the `3MF` project directory (JDK 17+ and official fixtures required):

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tests/cli/issues/Test-SecureContent.ps1 -Headless
```

This extends the existing Secure Content regressions with unencrypted positive
and negative fixtures and CLI startup errors. It checks default, schema-only,
structure-only and file-list execution; valid and invalid encrypted models;
HTML/XML diagnostics; package-key reset; missing input/files/lists/job tickets;
and a blocked working directory. A Java shutdown check requires headless AWT and zero windows.
Error-path runs start with `java.awt.headless=false` to verify that CLI startup
selects headless mode itself. Use `-TestSuiteRoot PATH` to override the checkout.
Reports remain under `tests/cli/results/secure-content-*`.

With `-EditorSmoke` instead of `-Headless`, the script also checks that GUI startup
creates a visible editor window with its content, title and menu; this GUI check
requires a desktop session. The maintained script is already tracked, and the
new Java helper is not ignored by Git. Generated reports remain ignored.

Validation on Windows/JDK 21: all 18 headless checks, all seven beam object-type
regressions, and the editor-window smoke check pass. A native Linux run was not
available; headless JVM mode and zero-window assertions exercise display isolation.

## Issue #12: whitespace in path and target references

[Issue #12](https://github.com/3MFConsortium/3mf_Editor/issues/12) requests a warning
when path and relationship target references contain leading or trailing spaces.
The validator silently trimmed these values, hiding possible differences in how
renderers resolve them.

Structure validation now emits `WR_026` (`reference whitespace`) in HTML and XML
for padded `path` (including namespace-prefixed paths), `slicepath`, `thumbnail`,
and `Target` attributes. The diagnostic identifies the part, element, attribute,
and original reference. Report text is escaped, the warning participates in
primary-issue selection and the existing warning limit, and each attribute is
reported once even when its part is read again. Decrypted model parts are also
checked. Reference lookup and the package content are unchanged. Detection uses
the same `trim()` comparison as existing lookup; internal and percent-encoded
spaces do not produce this warning.

Run from the `3MF` project directory (JDK 17+ and official fixtures required):

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tests/cli/issues/Test-ReferenceWhitespace.ps1
```

Use `-TestSuiteRoot PATH` to override the default official fixture checkout.
The script compiles current sources and generates cases from the official core,
Production, Materials, and Slice fixtures. It checks clean values, leading spaces,
trailing spaces, padding on both sides, and tabs/newlines for package relationships,
part relationships, Production paths, texture paths, slice references and thumbnails.
Additional cases check internal/encoded spaces, unrelated object-name whitespace,
multiple padded references in one part, and escaped diagnostic text. It requires
the expected warning count in both reports, primary-issue selection, and acceptance
of the normalized references. Reports remain under
`tests/cli/results/reference-whitespace-*`.

Before the fix, all 24 padded-reference cases silently reported `Pass`, while the
six clean controls passed. The regression script accompanies this fix. This fix
does not change ignore rules.

Validation on Windows/JDK 21: all 35 reference-whitespace regression cases and
all 11 existing Secure Content regression checks pass. The changed Java source
and this README also pass `git diff --check`.

## Issue #14 investigation: XML without line breaks

[Issue #14](https://github.com/3MFConsortium/3mf_Editor/issues/14) reports an
editor hang displaying a 29 MB XML model without line breaks. No sample package
is attached to the issue, and there were no comments when investigated.
This investigation changes no production code.

The package-tree selection handler in `MainFrame.java` rejects parts larger than
75,000,000 bytes, then passes smaller parts to `xmlEd.setText(...)` on the Swing
event dispatch thread. `XmlView` extends Swing's non-wrapping `PlainView`.
On JDK 21, `DefaultEditorKit.read` inserts text in chunks. Each insertion invokes
`PlainView.updateDamage`, which measures the growing longest line through
`getLineWidth` and `Utilities.getTabbedTextWidth`. The repeated measurements
produce approximately quadratic loading cost for a large single line and block
the UI. Two independent 29 MB probes timed out in this loading path; their thread
dumps identify `PlainView.getLineWidth`, not XML parsing or package extraction.

There is also a separate paint cost: `XmlView.drawUnselectedText` scans the
provided text with six regular expressions and builds two TreeMaps of matches.
The synthetic compact control still required 9.4 seconds to paint after bypassing
incremental loading. Improving loading alone therefore leaves visible stalls.

Measurements on Windows, Zulu OpenJDK 21.0.3, `-Xmx1g`, headless Swing, with a
1000 by 700 pixel image, using repeated XML vertex elements (decimal MB):

| Input | Load | First paint |
| --- | ---: | ---: |
| 0.25 MB, single line | 90 ms | 205 ms |
| 1 MB, single line | 515 ms | 408 ms |
| 4 MB, single line | 6,428 ms | 1,222 ms |
| 29 MB, single line | timed out at 20 seconds | not reached |
| 29 MB, newline after each vertex | 2,360 ms | 10 ms |
| 29 MB, single line, detached document populated in one insertion | 118 ms | 9,412 ms |

These observations establish a performance problem, not a fixed byte threshold.
The effective limit depends on line length, XML token density, font, hardware,
JDK, available heap, and acceptable UI delay. The reported 29 MB lies well below
the existing file-size guard. The probe isolates the real editor component but
does not open an actual package or validate the generated mesh; this is not an
end-to-end reproduction of the reporter's unavailable model.

Recommended next implementation: check maximum line length before calling
`setText` and refuse unsafe display with a clear message. Select the line-length
budget explicitly rather than treating 29 MB as a universal boundary. A broader
performance fix should bulk-load a detached document and bound highlighting and
measurement work to visible text. Any pretty-print-and-save option should be
explicit, run before display, and preserve the original until the user saves.
The existing pretty-print menu operates on text already loaded into the editor,
so it cannot rescue the blocked initial load.

Reproduce from the `3MF` project directory (JDK 17+; no official fixtures needed):

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tests/cli/issues/Test-LongXmlLines.ps1
```

The script compiles only the four editor component classes and
`tests/src/LongLineProbe.java`, then runs each case in a separate headless JVM.
The internal watchdog dumps the Swing thread and exits 124 on timeout;
`-TimeoutSeconds` changes its default 20-second budget. The outer harness also
enforces a process timeout. This is a measurement/reproduction harness, not a
pass/fail conformance check; inspect `results.csv` and each case's stdout/stderr.
Logs remain under ignored `tests/cli/results/long-xml-lines-*`. Both new source
files are visible to Git under the current local ignore rules; no ignore rules
were changed by this investigation.
