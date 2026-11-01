# Issue fixes and CLI regression checks

## Issue #21 fix: positive fixture acceptance

The editor now accepts the reported PNG thumbnail Overrides and unsupported
namespace attributes, and validates non-Production models with UUID checking
enabled. Production object, component and item UUID requirements remain active,
including in referenced model parts. Namespace declarations alone do not activate
Production. Structural validation also no longer mistakes a required Slice
namespace for Production when checking the root build UUID.

`ModelSchema.java` applies the two consolidated-schema compatibility corrections
in memory: conditional UUID requirements and the missing foreign-attribute
wildcard on `CT_Object`. Relative imports retain the original schema base URI.
Production-context detection streams XML without retaining another model DOM.
The external schema checkout and package XML are not rewritten. Invalid known
attributes, malformed UUIDs, unqualified unknown attributes and DTDs still fail.
`MainFrame` resolves effective content types using full package paths, with
Overrides taking precedence over extension Defaults, before selecting XML parts.
A thumbnail may share a basename with a genuine model without disabling model
validation.

Raw Unicode ZIP names now produce interoperability warning `WR_030`, consistent
with the positive fixtures and Core's SHOULD guidance on Unicode representation.
This is an explicit consumer policy rather than a claim of full OPC name-syntax
conformance. Prohibited leading-period names retain `ER_183`, including names
that also contain Unicode. Percent-encoded Unicode controls remain accepted
without this warning. The historical `N_XPM_0208_01` now returns Warning; the
full conformance runner still records that as a negative-fixture mismatch.

Alternative-object lookup now uses the same decryption path as ordinary model
references. It validates recovered content and still rejects missing object IDs,
wrong parts, forward references and nested alternatives. If a referenced model
is inaccessible, `WR_031` explicitly reports incomplete reference/geometry
validation. It does not misdiagnose inaccessible ciphertext as malformed XML
(`ER_055`/`ER_059`) or a missing object (`ER_057`). This warning survives schema-only,
structure-only, list and hidden-warning modes. Diagnostics remain bounded to one
incompleteness summary per validation phase and are reset between packages.

Inaccessible access rights and corrupt decryptable data are distinct:
`ER_190` rejects authentication/decompression failures when the wrapped content
key can be recovered. `ER_191` rejects a keystore missing its required root
relationship, per [Secure Content 3.2](https://github.com/3MFConsortium/spec_securecontent/blob/master/3MF%20Secure%20Content.md#32-relation-files).
The serialization regression now requires Warning plus `WR_031` for inaccessible
`2602`/`2604` content, and explicit errors for the corruption and missing-keystore
relationship controls. Limited-access `2604` tests explicitly allow editors to
ignore inaccessible content. Printer expectations remain enforced separately by
the unmodified full conformance runner.

The CLI remains a schema/structure validator: it does not select a viewer/printer
consumer or establish that a usable full-resolution alternative can be rendered.
All six `P_EPX_0740` cases therefore report Warning when content is inaccessible;
this is not a claim that cases `_05`/`_06` satisfy their printer/viewer criteria.
The invalid `0909_01` Overrides remain Fail, and missing Appendix C fixtures
remain outside this repository's fix.

Run the maintained regressions from the project directory:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tests/cli/issues/Test-PositiveAcceptance.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File tests/cli/issues/Test-ResultXml.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File tests/cli/issues/Test-LowSeverity.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File tests/cli/issues/Test-AlternativeReferences.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File tests/cli/issues/Test-SecureContent.ps1 -Headless
```

`Test-PositiveAcceptance.ps1` now asserts the resulting policy rather than treating
every positive fixture as an unconditional Pass. It runs all cases with UUID
checking enabled, includes generated positive/negative controls and a mixed batch,
and compiles fresh production sources plus 13 independent schema checks against
Java 8 APIs. Its `-FixtureName` option selects named official fixtures for a shorter
rerun. New source/helper/script files are not Git-ignored; generated reports remain
under ignored `tests/cli/results/`. No ignore rules changed.

Validation on 2026-11-01 with Zulu OpenJDK 21: all 36 issue checks, 13 independent
schema checks, 47 report fixtures, 22 package/XML helper checks, 30 available
package regressions, 18 headless Secure Content checks and 15 alternative-reference
checks passed. The report checks also passed single-file filename escaping, six
fallback assertions, grouped diagnostics and thirteen explicit warning/error-policy
assertions. The known missing `N_XPM_0208_03` remains separately unavailable.
After the streaming refactor, the 13 schema checks and the foreign-attribute and
four encrypted CLI mode probes were rerun successfully against fresh sources.
`git diff --check` and PowerShell syntax checks pass. No full conformance suite,
interactive GUI, rendering or printing run was performed.

Evidence is retained under `tests/cli/results/`:

- `positive-acceptance-20261101-101625-8769b792` (36 cases and mixed aggregate).
- `positive-acceptance-20261101-101817-73181f8c` and
  `positive-acceptance-20261101-101915-a6696e07` (final streaming checks).
- `result-xml-20261101-100950-dba109ed`.
- `low-severity-20261101-100937`.
- `secure-content-20261101-100319-ab3d52d2`.
- `alternative-references-20261101-100331-6ea14f48`.

### Investigation before the fix

[Issue #21](https://github.com/3MFConsortium/3mf_Editor/issues/21), checked live
on 2026-11-01, is open with no comments. This investigation changes no production
code or external schemas. Fresh sources at `e136b9aa7265782b2f51e6e53d86d6b22626e3ed`
were compiled against Java 8 APIs and run with Zulu OpenJDK 21, a 2 GiB heap,
and the official fixtures at `a63f4bf07c4ab287c8f84f1cdeab8ee92333a577`.
The v2.4.1 specification SHA256 matches the issue:
`614D900FD123C6331FDCFA32C88D3F56590A9F393DC0801D3FD95FA2CEDC9752`.

The targeted run covers 17 distinct official fixtures and 21 mode/fixture
combinations. All processes finish normally within the original 120-second
per-process budget. Every individual and aggregate report parses as XML.
There are seven accepted probes and fourteen rejected probes; these counts
include known fixture defects and consumer exceptions, so they are not a
conformance score. The script exits 1 to make the rejected acceptance probes
visible. Four baseline controls pass.

| Area / fixture | Current result | Finding |
| --- | --- | --- |
| Thumbnail Override: `P_SPX_0102_03`, `P_SXX_0102_03` | Fail, `SC_000`; structure-only Pass | PNG bytes in a `.model` thumbnail are incorrectly sent to the XML parser despite an explicit `image/png` Override. Confirmed CLI defect. |
| Unescaped Unicode: `P_SPX_0104_04`, `P_SXX_0104_04` | Fail, `ER_183` | Confirmed mismatch between the current hard-error policy and the positive fixture expectations. This is a policy/specification question, not a missing-file defect. |
| Unknown attribute: `P_XXX_2202_04` | Fail, `SC_000`; structure-only Pass | Consolidated `CT_Object` lacks an `xs:anyAttribute` wildcard. The foreign `ww:unrecognized` attribute triggers a false rejection. |
| Encrypted alternatives: `P_EPX_0740_01`, `_05`, `_06` | Fail, `ER_059`, `ER_057`, `WR_019` (also `WR_010` for `_01`, `_05`) | Reports now escape the XML-like diagnostic and parse correctly: issue #22 has resolved the serialization defect. Acceptance remains a separate problem. |
| Encrypted alternatives: `P_EPX_0740_02`, `_03`, `_04` | Fail, `ER_057`, `WR_019` (also `WR_010` for `_02`, `_04`) | Alternative lookup treats encrypted bytes as an object lookup failure. It does not use the available decryption helper. No consumer-aware fallback selection is implemented in that lookup. |
| Missing leading slash: `P_SPX_0909_01`, `P_SXX_0909_01` | Fail, `ER_132`, `WR_020` | ZIP inspection confirms many Override PartName values lack `/`. The reported fixture defect remains; weakening the validator is not the appropriate repair. |
| UUID control: `P_XXX_0101_01` | Auto Pass; strict Fail, `SC_000` | Reproduces the non-Production UUID false rejection on `object` and `item`. The consolidated schema also requires UUID on `component`; contrary to the issue wording, UUID on `build` is already optional in this checkout. |
| Other controls: `P_SPX_0101_01`, `P_SXX_0101_01`, `P_XPM_0101_01` | Pass | Production and non-Production baseline packages remain accepted under suite-aware UUID settings. |

### Causes and recommended work

1. **Content-type selection:** `MainFrame.java` around lines 2856-2859 selects
   XML candidates using the `.model` suffix. `xmlValidCon` around lines 3346-3352
   consults only the extension default, using the basename supplied by its
   caller. Resolve each part's full package path and effective content type
   (Override before Default) before choosing a parser/schema. Preserve XML
   validation for genuine model parts, including models without declarations.
2. **Consolidated schema:** `qli_3MF.xsd` is supplied by the external fixture
   checkout, not this editor project. Its `CT_Object` (lines 104-131) requires
   Production UUID and omits the foreign-attribute wildcard. `CT_Component` and
   `CT_Item` also require UUID (lines 184 and 201); `CT_Build` does not (line 84).
   Make the consolidated declarations compatible with non-Production Core
   files while preserving conditional Production requirements through semantic
   validation. Do not simply disable all UUID validation. `Run-Conformance.ps1`
   already works around the missing-UUID issue using Auto mode for non-Production
   suites; that does not repair normal CLI validation or the external XSD.
3. **Unicode severity:** `PackageChecks.invalidName` unconditionally returns
   an error for raw non-ASCII names, and `FileStructureCheck` emits `ER_183`.
   Core 2.2 combines OPC syntax requirements with SHOULD guidance for Unicode
   representation. Review the exact OPC rule before changing severity; keep
   prohibited leading-period names distinct from this Unicode interoperability
   question. No Unicode policy is changed by this investigation.
4. **Encrypted alternatives:** the alternative branch in `FileStructureCheck`
   around lines 5723-5817 reads the ZIP entry, parses it only if it starts with
   `<?xml`, and otherwise leaves `fndObj` false before emitting `ER_057`.
   Unlike other model-reference paths, this branch never calls `CheckKeyInfo`
   or `DoDecrypt`. The component path also emits `ER_059` when the encrypted
   primary representation cannot be decrypted. Distinguish inaccessible content
   from a proven missing object, reuse decryption where possible, and evaluate
   accessible alternatives according to consumer/resolution policy. Suppressing
   `ER_057` globally would hide genuinely broken references.

The [Core specification, 2.3.3.1](https://github.com/3MFConsortium/spec_core/blob/master/3MF%20Core%20Specification.md#2331-xml-namespaces)
requires consumers/editors to ignore unsupported namespaces. Its
[naming rules](https://github.com/3MFConsortium/spec_core/blob/master/3MF%20Core%20Specification.md#22-naming-conventions)
are relevant to the Unicode policy. The
[Production specification](https://github.com/3MFConsortium/spec_production/blob/master/3MF%20Production%20Extension.md)
defines alternative priority, access rights and resolution constraints.
Test specification v2.4.1 explicitly expects rendering for `0740_01` through
`_04`; `_05` expects a printer exception but viewer rendering; `_06` expects
exceptions for both. Therefore neither accepting every `P_` case nor rejecting
every inaccessible encrypted part is a correct consumer policy. This run
does not establish rendering behavior or that all decryption keys are absent.

### Reproduction and scope

Run from the project directory:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tests/cli/issues/Test-PositiveAcceptance.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File tests/cli/Run-Conformance.ps1 -Polarity Positive -InventoryOnly -OutputRoot tests/cli/results/issue21-inventory
```

The targeted script supports `-TestSuiteRoot`, `-TimeoutSeconds` (now defaults
to 1200 for the large `0909` models), and `-FixtureName` for a narrower rerun.
It follows suite-aware UUID settings, adds strict/structure-only probes, and
saves CSV, JUnit, classes, logs and XML/HTML reports under ignored
`tests/cli/results/positive-acceptance-*`. Evidence for the investigation is in
`tests/cli/results/positive-acceptance-20261101-094153-ae868777`.

The inventory independently confirms 1719 positive models, the two missing
Appendix C entries `10/P_3006_01` and `11/P_3212_04`, and 15 additional models
outside the Appendix C mapping. Evidence is in
`tests/cli/results/issue21-inventory/20261101-094529-682d39b5`.
The inventory command exits 1 because entries are missing, as intended.
The historical count of 693 UUID rejections was not rerun. No full acceptance
suite, rendering/printing test, interactive GUI test or production fix was
performed. The new investigation script and this README are not Git-ignored;
generated results are ignored. `git diff --check` passes.

## Issue #22: well-formed CLI diagnostic reports

Follow-up in issue #21: the three `P_EPX_0740` serialization controls now report
Warning with `WR_031`, and inaccessible `2602`/`2604` models also retain explicit
incompleteness warnings. The historical acceptance results below predate that fix.

[Issue #22](https://github.com/3MFConsortium/3mf_Editor/issues/22) reports raw
ampersands in schema patterns, raw XML-like diagnostic text, and a duplicated
`ER_175` opening element. Schema and structural diagnostic text now uses XML 1.0
escaping, including exception messages, package paths and warning text. Text is
escaped once, separately from the issue markup. The duplicate opening tag is
removed and report filenames are escaped in both single-file and list modes.
Grouped schema diagnostics separate the line number at the final delimiter,
preserving XSD pattern alternation characters in the message.
If report formatting fails, the CLI writes a well-formed `Fail` report with an
explicit `ER_REPORT_XML` serialization diagnostic and the escaped original report.
Aggregate fallback output retains the `TestResults` wrapper.

Run from the project directory with a JDK and the official fixture checkout:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tests/cli/issues/Test-ResultXml.ps1
```

The script supports `-TestSuiteRoot`, compiles fresh sources and a fallback helper
against Java 8 APIs, and checks individual and aggregate XML for 47 official
fixtures across all reported scenario families, including neighboring cases and
the three `P_EPX_0740` fixtures mentioned in issue #21. It verifies diagnostic
content, rejects empty/nested diagnostics and unexpected fallback reports, and
checks single-file output with special characters in the filename, grouped schema
diagnostics, and six fallback assertions. Reports and process logs go under
ignored `tests/cli/results/result-xml-*`; the regression files are not ignored.
Validation on 2026-11-01: all checks passed with Zulu OpenJDK 21 targeting Java 8.
The baseline reproduced 33 malformed individual reports (30 negative fixtures
and three positive fixtures); the fixed run produced none. `git diff --check`
also passed.

This fixes report serialization, not other fixture acceptance policies:
`P_EPX_0740_01`, `_05`, `_06` still report `Fail`, and `N_EPX_2606_01` still reports
`Warning`. The positive control `P_XPM_0101_01` reports `Pass`. No decryption or
geometry rules are changed. This targeted run does not establish full-suite
conformance or interactive GUI correctness.

Keep issue-fix documentation in this file and all `Test-*.ps1` regression scripts
in `tests/cli/issues/`. Commands below run from the `3MF` project directory. Shared CLI
utilities remain in `tests/cli/`, alongside the ignored generated reports.
Java test helper sources live in `tests/src/` and `tests/cli/` and are compiled explicitly by the
regression scripts.

Runs the 3MF Editor CLI against all model files in all 11 official suites and verifies its XML results using the positive/negative convention in section 5.1 of the [3MF Test Specification v2.4.1](https://github.com/3MFConsortium/test_suites/blob/master/test_specification/3MF_Test_Specification_v2_4_1.docx). Reads Appendix C directly from the DOCX to check that the checkout contains each specified suite/scenario/iteration. Duplicate Appendix C rows are treated as one membership entry.

## Issue #10 fix: size limits and geometry checks

The recommendations below are implemented in `GeometryCheck.java`, integrated
with `FileStructureCheck.java` and the existing XML/HTML CLI reports:

| Diagnostic | Resulting behavior |
| --- | --- |
| `WR_008` | All ZIP part names remain in the inventory, even when content exceeds the unchanged 200,000,000-byte structural limit. Reports explicitly say validation is incomplete; existing oversized parts no longer produce `ER_015`/`ER_027`. This warning remains visible with `-W`. |
| `ER_178` | Reject non-manifold indexed edges in ordinary and displacement base meshes of type `model` or `solidsupport`. |
| `ER_179` | Reject adjacent triangles traversing a shared edge in the same direction. |
| `ER_180` | Reject a consistently wound, closed, single connected base mesh with negative signed volume. Mirroring never repairs or invalidates its intrinsic winding. |
| `ER_181` | Reject displacement images whose PNG signature or effective package content type is inconsistent with PNG. Content-type overrides take precedence over defaults. The bitmap is not decoded. |
| `ER_182` | A part above the unchanged 514,215,936-byte schema limit explicitly fails with a validation-limit diagnostic, rather than being skipped without affecting the result. |
| `WR_027` | Warn for singular or nearly singular individual and composed transforms. A negative determinant alone remains valid. |
| `WR_028` | Warn when a printable instance's transformed ordinary base mesh extends into a negative coordinate. Compose build/component matrices in row-vector order, resolve Production paths by namespace, convert part units, and examine referenced triangle vertices rather than transformed bounding boxes or unused vertices. No printer-specific bed limits are assumed. |
| `WR_029` | Explicitly report incomplete geometry checks when a budget is reached or an encrypted displacement image cannot be inspected. One summary remains available even after the warning limit. |

Follow-up: the saved GUI setting `bdoWarn = "true"` means **Hide Warnings**.
It previously hid `WR_028` for `N_XPM_0421_01.3mf` and left a misleading green
"No structural errors" message. The shared report presenter now shows an amber
warning status with its diagnostic IDs when details are hidden. It preserves
warning diagnostics in XML independently of GUI preferences or CLI `-W`, so the
same model remains `Warning` in both single-file and list modes. `-W` and **Hide
Warnings** filter the detail text; they do not turn a warning into `Pass`.
Disabling **Hide Warnings** shows the full placement diagnostic, including
minimum coordinates `(-10.1, -10.1, 30.1)`. No green success message is emitted
for a result with warnings. Positive results still show green success, and
placement remains a warning rather than a hard format error.

Targeted reproduction from the `3MF` directory:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tests/cli/issues/Test-NegativePlacement.ps1
```

This compiles production code against Java 8 APIs, uses the actual official
fixture with `tests/cli/StructureWarningTest.java` to test the GUI presenter
headlessly, and runs six CLI cases against fresh sources. All ten presenter
checks (GUI/CLI preference combinations, a positive result after a warning, and
an error control) and six CLI checks pass. The CLI checks cover single-file and
list modes, both warning settings, HTML/XML status, and positive controls.
Reports are retained under ignored `tests/cli/results/negative-placement-*`;
the new helper and regression script are not ignored. The test does not open
an interactive editor or change the user's saved settings.

The topology rules preserve the Core exceptions for `support`, `surface`, and
`other`. Low-resolution meshes backed by required slice data are excluded from
these fabrication checks, including when an ancestor supplies the authoritative
slice data. The same mesh is still checked if also used independently without
that slice context. Existing slice-plane restrictions and reference validation
remain active. Placement ignores non-printable build items and does not use a
non-root model's build list as another print job.

The helper bounds retained vertices, triangles, objects and references to
500,000 total elements, with at most 100,000 component visits and 128 levels of
descent. It releases temporary edge tables per mesh, deduplicates diagnostics,
and respects the existing report limits. Signed volume is calculated about a
nearby origin with normalized coordinates and compensated summation, avoiding
cancellation for translated models. Near-singularity uses a scale-normalized
determinant; placement uses a relative tolerance of `1e-9` per coordinate axis.

This implements base-mesh validation, not complete constructive geometry.
Volume classification deliberately does not reject disconnected or nested
shell combinations on the sign of their aggregate volume; valid hollow meshes
are preserved. General Positive-fill evaluation and self-intersection resolution
remain outside scope. Displacement-surface geometry, slice contours and unknown
shape representations are not evaluated for final bounds or volume. A PNG
signature check does not establish that the entire image decodes correctly.

Run from the `3MF` project directory (JDK 17+, official fixture checkout):

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tests/cli/issues/Test-ToolLimitations.ps1
```

The script accepts `-TestSuiteRoot PATH`, `-HeapMB` (default 2048) and
`-TimeoutSeconds` (default 90). It compiles production sources with `--release 8`
to check the IDE/CI API and bytecode target, and separately compiles the independent
`tests/cli/GeometryCheckTest.java` helper with `--release 17`, then runs unchanged official fixtures
in isolated JVMs with schema/structure validation and UUID checking enabled.
The helper covers winding, manifold edges, object-type exceptions, a nested
cavity, mirrors, stable volume calculations, composed transforms and part units,
slice inheritance, reused objects, non-printable items, bounded traversal and
image signatures/content types. Additional CLI checks exercise `-W` with the
large positive fixture and a generated valid model padded to 514,215,937 bytes
to test the schema guard. Generated large extracted files remain in the ignored
results directory.

Expectations now follow the diagnostic policy above: `_0416_02` and displacement
`_3314_03` are accepted as valid mirrors; placement and singularity produce
Warning; inverted meshes and reversed faces produce Fail. The historical `N_`
polarity is not used to require an invalid mirror rejection. Missing `_3314_09`
is recorded in `summary.json` as unavailable and is not counted as tested; any
other missing fixture is an error. CSV, JUnit, summary JSON, HTML/XML reports,
compile output, helper output and process logs remain under
`tests/cli/results/tool-limitations-*`.

Known official-suite mismatch: `N_DPX_3314_07.3mf` (determinant zero) still
produces `Warning` / `WR_027`, whereas the v2.4.1 negative-test criteria expect
an error. `N_XPM_0421_01.3mf` likewise produces `Warning` / `WR_028`, not `Fail`.
The targeted regression explicitly verifies this warning policy; passing that
regression does not establish compliance with these official negative-case
expectations. The full conformance runner continues to report such warnings on
negative cases as expectation failures. Nearly singular transforms share the
same warning policy. These limitations are retained explicitly rather than
claiming that all issue #10 fixtures satisfy the official suite.

Validation on Windows/Zulu OpenJDK 21.0.3: 43 independent geometry checks,
22 available official cases, both limit-reporting CLI checks, the existing
25 transform regressions and 18 headless secure-content checks pass. No full
conformance suite or interactive GUI run was performed. The new source/helper
files and regression script are not Git-ignored; generated evidence is ignored.
The project `.gitignore` also excludes the temporary `/zips/` and `/ztmp/`
folders. `git diff --check` passes.

Build compatibility: the IDE settings and GitHub workflows target Java 8.
The PNG signature reader uses a fixed eight-byte read loop, preserving short-file
handling without `InputStream.readNBytes` (unavailable on Java 8). Compiling only
with a newer JDK's default target had previously hidden this build failure. The
regression command above now enforces Java 8 APIs and class files for production;
its test helper and regression runner still require JDK 17+.

### Original investigation before the fix

[Issue #10](https://github.com/3MFConsortium/3mf_Editor/issues/10), checked live
on 2026-10-31, was open and had no comments. Before this fix, its five reported
limitations were partly reproducible. The original investigation changed no
production code. Baseline tests used Windows, Zulu OpenJDK 21.0.3, a 2 GiB Java heap,
editor revision `fa8d26de6ad5758a9d8fe62f96961fd70385a611`, and official fixture
checkout `a63f4bf07c4ab287c8f84f1cdeab8ee92333a577`.

### Baseline results

| Reported area / fixtures | Baseline CLI result | Finding |
| --- | --- | --- |
| Large positive `P_XPM_0909_04` (listed twice in the issue) | Fail: `ER_015`, `ER_027`, `WR_008` | Size limitation persists; missing-file diagnostics are misleading. No `ER_014` or out-of-memory crash occurred in this run. |
| Negative volume `N_XPM_0416_01`, `_02`, `_03` | All Pass, no issues | Negative base-mesh volumes are undetected in `_01` and `_03`. `_02` has positive volume and a mirror transform; see the specification caveat below. |
| Inward face `N_XPM_0418_01` | Pass, no issues | Missing triangle-orientation check; total signed volume alone would not catch this case. |
| Negative placement `N_XPM_0421_01` | Pass, no issues | Transformed geometry bounds are not checked. |
| Displacement `N_DPX_3314_02`, `_03`, `_04`, `_07` | All Pass, no issues | Negative base volumes (`_02`, `_04`) and a singular transform (`_07`) are undetected. `_03` is another mirror-transform caveat. |
| Displacement `N_DPX_3314_06` | Fail: `ER_153` | This specific fixture is now rejected for non-unique triangle vertex references; this does not establish general manifold validation. |
| Displacement `N_DPX_3314_09` | Not run: missing | No `_09` file exists in this checkout, although the issue and test specification reference it. |
| Nearby displacement `_05`, `_08` | Both Pass, no issues | Additional gaps: inconsistent face orientation in `_05`; a JPEG displacement image in `_08`. |
| Core positive `P_XPM_0101_01`, transforms `P_XPM_0910_01` through `_06`, displacement positive `P_DPX_3200_02` | All eight Pass | Relevant positive acceptance controls. |
| Displacement negative `N_DPX_3314_01` | Fail: `ER_144` | Control confirms required-extension validation is active. |

The 23 requested cases produce **10 expectation matches, 12 mismatches, and one
missing-fixture error** using the existing `P_`/`N_` acceptance convention.
All 22 available fixtures complete without process errors or timeouts. The 12
mismatches are not 12 independently confirmed specification violations: the
historical test expectations need the interpretation below.

### Causes and specification interpretation

`FileStructureCheck.java` skips entries larger than **200,000,000 bytes before
adding their names to `entryNames`** (around lines 236-263). Relationship checks
subsequently consult that incomplete inventory (for example, `ER_015` around
line 1120), so an existing oversized part is reported as missing. The named
fixture's model part is **380,470,980 bytes** uncompressed. The skip also emits
`WR_008`; increasing `-Xmx` does not remove this hard limit. There are separate
limits in `MainFrame.java`: **75,000,000 bytes** for XML editor display and
**514,215,936 bytes** for schema validation. The latter silently continues past
an oversized part in CLI mode after logging it. The named fixture is below that
schema limit but above both display and structure limits. These are separate
paths; this run exercises CLI validation, not GUI loading.

The ordinary mesh path in `fillUuid` counts vertices and validates triangle
indices and property references. It does not calculate signed volume, shared
edge incidence/direction, or final transformed bounds. Existing transform checks
collect slice-plane matrix violations and apply them only for slice-only objects;
they do not compose the object/component/build transforms into printable bounds.
The displacement path checks references, counts and other structural rules. Its
`ER_175` scalar-product calculation checks displacement vectors relative to a
triangle normal; it is not a mesh-volume or adjacent-face orientation check.

An independent, read-only inspection of these small fixture meshes found:

- `N_XPM_0416_01` and `_03`, and `N_DPX_3314_02` and `_04`, have signed base
  mesh volume **-1,000,010** in cubic model units. They are closed by indexed
  edge incidence and have consistent winding, demonstrating why edge checks
  alone would miss an entirely inverted shell.
- `N_XPM_0418_01` has positive summed volume **113,071.95074**, but three shared
  edges have inconsistent direction. Displacement `_05` likewise contains an
  object with three such edges despite positive summed volume. Checking only
  total signed volume would miss the reversed face.
- `N_XPM_0421_01` has an identity linear transform and translation
  `(-10.1, -10.1, 30.1)`. Its mesh bounds become
  X `[-10.1, 89.899]`, Y `[-10.1, 29.9]`, Z `[30.1, 64.74]`.
- Displacement `_07` has an all-zero linear transform: determinant **0**.
- `N_XPM_0416_02` and displacement `_03` have positive base volume and
  determinant **-1**. These must not be rejected merely for having a negative
  determinant.
- Displacement `_06` also has four non-manifold indexed edges, but the CLI
  rejection is specifically for duplicate indices. `_08` references
  `/3D/textures/new_rgb_text_image.jpg`, declared as `image/jpeg` in the package.
  There is no `ContentType` attribute on its `displacement2d` elements, despite
  the older test scenario wording.

The current [Core 1.4.0 specification, section 3.3](https://github.com/3MFConsortium/spec_core/blob/1.4.0/3MF%20Core%20Specification.md#33-3d-matrices)
requires a mirrored shape to preserve its volume sign. A negative determinant
is therefore not itself invalid. The same section expresses nonsingularity,
positive final volume, and positive-octant placement as **SHOULD** requirements;
the placement fixture also refers to a printer's printable area, which the CLI
does not know. A future placement/singularity check needs an explicit diagnostic
policy, rather than automatically converting every historical `N_` file into a
hard format error. The v2.4.1 test specification calls `_0416_02` and displacement
`_3314_03` negative-determinant cases, so those expectations need reconciliation
with the current Core text.

[Core section 4.1](https://github.com/3MFConsortium/spec_core/blob/1.4.0/3MF%20Core%20Specification.md#41-meshes)
requires manifold edges and consistent outward-facing triangle normals for
`model` and `solidsupport` meshes, with explicit exceptions for other object
types. These base-mesh checks do not require a renderer. Nested shells,
self-intersections, the Positive fill rule, and authoritative slice data need
care when implementing a general validator; a single summed volume or a rule
that rejects every mirrored transform is insufficient.
The [Displacement 1.0.0 specification, section 3.1](https://github.com/3MFConsortium/spec_displacement/blob/master/3MF%20Displacement%20Extension.md#31-displacement2d)
specifies PNG displacement images. Actual displacement-surface volume or bounds
would additionally require evaluating the image, displacement vectors, heights,
offsets and stitching rules. The base-mesh measurements above do not claim to
measure the resulting displaced surface.

### Baseline evidence and recommendations

Investigation evidence is retained in
`tests/cli/results/tool-limitations-20261031-081303-7eac6635/`, including
`geometry-probe.ps1` and `geometry.jsonl` for the independent base-mesh inspection.
That probe handles only direct indexed meshes and records individual transform
determinants; it is not a renderer or a general composed-transform validator.

The original recommended implementation order was:

1. Keep the complete ZIP part inventory even when skipping content validation;
   report incomplete validation explicitly, without spurious missing-file errors.
   Do not simply raise the memory guards.
2. Add shared base-mesh topology and winding checks, with object-type and slice
   semantics, for ordinary and displacement meshes. Handle fully inverted shells
   separately from local reversed faces.
3. Define a warning/error policy for singular transforms and placement, then
   evaluate composed transforms with the required mirror orientation handling.
4. Validate displacement image format independently from geometry; evaluate the
   displaced surface only if that broader capability is intended.

In that baseline run, all eight positive controls and both detectable negative
controls passed their acceptance expectations. No full conformance suite or
interactive GUI test was run.

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

## Issue #13: saved dark mode for the four main panes

[Issue #13](https://github.com/3MFConsortium/3mf_Editor/issues/13) is implemented
with **View > Dark Mode**. The toggle updates the package tree, XML editor,
statistics and HTML console immediately, along with surrounding controls and
image-panel backgrounds. XML syntax, caret, selection, tree selection, links and
diagnostic severity colors use a readable dark palette. Thumbnail/alpha image
pixels are unchanged. Disabling the option restores the original light colors.
Menus and separate dialogs retain the platform look and feel.

The preference is stored as `darkMode = "true"` or `"false"` in `3MF.ini`
through the existing settings-save lifecycle. Missing settings default to light.
Switching themes preserves the XML document, edit state and undo history. New
package trees and subsequent console updates use the active theme. Console HTML
documents are reset when updated so stylesheet rules cannot leak into light mode.
CLI mode ignores the GUI preference and retains the original report colors.

Run from the `3MF` directory (JDK 17+):

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tests/cli/issues/Test-DarkMode.ps1
```

This compiles current sources and tests real editor components in headless mode:
XML painting, pane colors, replacement tree renderer/selection colors, console
severity colors and updates, exact light HTML restoration, document/undo
preservation, preference round trips and CLI theme isolation. It saves a rendered
`dark-xml.png` and logs under ignored `tests/cli/results/dark-mode-*`.
Use `-EditorSmoke` to additionally construct the native editor and exercise its
actual View menu toggle (requires desktop AWT; no window is shown).
Use `-TestSuiteRoot PATH` to run official positive and negative beam fixtures
with both saved preference values and check that GUI colors do not enter CLI HTML.
The optional fixture checkout is normally `../../test_suites` from this directory.

Validation on Windows/JDK 21: the headless component checks, native menu smoke
check, all four saved-preference CLI fixture checks and all 18 existing
`Test-SecureContent.ps1 -Headless` checks pass. The rendered dark XML was visually
inspected and `git diff --check` passes. Interactive four-pane visual testing
with a loaded package remains a manual check; no full conformance run was made.
The new regression script and Java sources are not Git-ignored; generated reports
remain ignored. No ignore rules were changed.

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

## Issue #16 fix: volumetric and implicit resource ordering

[Issue #16](https://github.com/3MFConsortium/3mf_Editor/issues/16) is confirmed
against current sources on Windows/JDK 21 on 2026-10-30. The issue has no attached
sample and no comments. The fix changes the structure check and adds a targeted
CLI regression script.

Previously, `FileStructureCheck.java` scanned each `resources` element and,
after seeing an `object`, any subsequent element other than another `object`
produced `ER_103`, regardless of namespace or the model's `requiredextensions`.
The diagnostic appeared in both HTML and XML and made the CLI result `Fail`.
The consolidated schema's `CT_Resources` instead uses a
repeated choice including objects, volumetric resources, and implicit functions,
so these models can pass schema validation before failing the structure check.

The upstream [Volumetric and Implicit specification, section 1.1](https://github.com/3MFConsortium/spec_volumetric/blob/master/3MF%20Volumetric%20Extension.md#11-breaking-change-to-the-core-specification),
checked live on 2026-10-30, permits the relevant resources to be interleaved
**only when the volumetric extension is declared required**. Definitions must
still precede references, and material ordering is not relaxed. Merely declaring
an XML namespace or recommending the extension is not the stated trigger.
The implemented acceptance rule also enables the relaxation when **implicit
alone** is required.

The check now resolves whitespace-separated `requiredextensions` prefixes to
namespace URIs on the current model part. If either of the following is required,
resources in either namespace may follow objects:

- `http://schemas.3mf.io/3dmanufacturing/volumetric/2022/01`
- `http://schemas.3mf.io/3dmanufacturing/implicit/2023/12`

Aliases are supported; the decision is recomputed per model part rather than
using the validator's accumulated required-prefix list. Declared-only,
recommended-only, and unrelated required namespaces do not enable relaxation.
Material and unrelated resource ordering still uses `ER_102`/`ER_103` as before.
No reference-validation checks are bypassed.

Reproduce from the `3MF` project directory:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tests/cli/issues/Test-ResourceOrdering.ps1
```

The script accepts `-TestSuiteRoot PATH`, compiles fresh editor sources, and
generates 17 variants of official core fixture `P_XXX_0101_01.3mf`. It adds
Production UUIDs needed by the consolidated schema and independently validates
every generated model against that XSD before running the CLI. Minimal unused
`v:volumedata` and `i:implicitfunction` resources isolate the ordering rule;
these are not rendering or full extension-semantic conformance tests.

| Case | Schema | Expected CLI | Actual CLI |
| --- | --- | --- | --- |
| Core control | Pass | Accept | Pass |
| Volumedata before object, volumetric required | Pass | Accept | Pass |
| Volumedata after object, volumetric required | Pass | Accept | Pass |
| Same, required volumetric namespace bound to an alternate prefix | Pass | Accept | Pass |
| Implicit function before object, volumetric and implicit required | Pass | Accept | Pass |
| Implicit function after object, volumetric and implicit required | Pass | Accept | Pass |
| Either resource after object, only implicit required (2 cases) | Pass | Accept | Pass |
| Implicit function after object, only volumetric required | Pass | Accept | Pass |
| Implicit required through an alias | Pass | Accept | Pass |
| Required list with mixed whitespace, unrelated prefix, and volumetric alias | Pass | Accept | Pass |
| Volumedata after object, volumetric not required | Pass | Reject | Fail, ER_103 |
| Implicit function after object, neither extension required | Pass | Reject | Fail, ER_103 |
| Extensions recommended only | Pass | Reject | Fail, ER_103 |
| Unrelated extension required | Pass | Reject | Fail, ER_103 |
| Base materials after object, volumetric required | Pass | Reject | Fail, ER_103 |
| Base materials after object, implicit required | Pass | Reject | Fail, ER_103 |

Before the fix, the original eight-case reproduction showed three false
rejections, each containing only `ER_103` in XML and the same error in HTML.
The regression checks acceptance/rejection and `ER_103` in both report formats.
Results, JUnit output, generated packages, and CLI logs
are retained under ignored `tests/cli/results/resource-ordering-*`.

The new script is not Git-ignored under the current local rules. The CLI ignore
file now also excludes `powershell.log`. All 17 regression cases pass on Windows/JDK 21, with all
17 generated models passing independent consolidated-schema validation.
`git diff --check` passes. No full conformance run was performed.

The independent .NET schema check maps W3C `xml.xsd` imports to the same local
`xml.xsd` used by relative imports. This prevents duplicate `xml:lang`
declarations when the W3C URL is reachable and avoids network-dependent schema
loading. It changes neither the source schemas nor production validation.
The script also runs from `tests/cli` with
`powershell -NoProfile -ExecutionPolicy Bypass -File .\issues\Test-ResourceOrdering.ps1`.

## Issue #9 fix: package names, markup, relationships, images and materials

Follow-up in issue #21: raw Unicode names now produce `WR_030` rather than
`ER_183`. `N_XPM_0208_01` is maintained as a Warning-policy regression; the
original investigation and validation below describe the previous hard-error policy.

The confirmed gaps below are fixed in the structure checker and the shared XML
parsers. Both XML and HTML reports now carry explicit errors for the affected
packages.

| Diagnostic | Resulting behavior |
| --- | --- |
| `ER_183` | Reject part-name segments beginning with a period. Preserve OPC's special `_rels/.rels` name. Raw Unicode is now reported separately as `WR_030`; percent-encoded UTF-8 names remain accepted. This is targeted validation, not a complete OPC URI validator. |
| `ER_184` | Reject `xml:space` in model markup, including nested elements, using its namespace URI. |
| `ER_185` | Require a `3dmodel` relationship for a Production component/build-item path. Resolve model and relationship paths relative to the referencing part and require an internal relationship. Custom relationship types remain accepted with a warning. |
| `ER_186` | Reject DTDs before structural DOM parsing. GUI parsing and schema validation use the same DTD prohibition; schema-only rejection is reported as `SC_000`, including model XML without an XML declaration. Internal and external DTDs are both prohibited. |
| `ER_187` | Reject four-component CMYK/YCCK JPEGs by reading the primary JPEG frame header. Skip complete APP/EXIF segments so an embedded RGB preview cannot mask a CMYK main image. Check JPEG extensions and effective `image/jpeg` content types, including overrides. No bitmap is decoded. |
| `ER_169` | Diagnose ordinary solid base meshes with one to three triangles, alongside the existing manifold check. Preserve `support`, `surface`, `other`, authoritative slice contexts, and meshes without triangles used by beam lattices. |
| `ER_188` | Report base/composite material above the first multiproperty layer as a structured error, with an error count and primary-diagnostic selection. |
| `ER_189` | Reject invalid hexadecimal material colors in structure-only mode as well as the existing schema rejection. Accept six/eight hexadecimal digits and either letter case. |
| `WR_022` | Store unknown non-root relationship warnings in the XML warning stream, correcting the previous HTML-only diagnostic. Hiding warning details does not remove the warning from XML or change its status to Pass. |

Relationship collections are cleared for each validation run. New diagnostics
escape user-supplied text and respect the existing structure error limit. The
DTD parser settings are applied before parsing, rather than checking a DOM
after entities could have been processed. Existing schema imports are unchanged.

Validation on Windows/Zulu OpenJDK 21.0.3: all **30 available CLI cases** in
`Test-LowSeverity.ps1`, **20 package/XML helper checks**, **44 geometry checks**,
**18 headless Secure Content regressions**, and **35 reference-whitespace
regressions** pass. Production and package helper sources compile against Java
8 APIs with `--release 8`; the independent geometry helper uses Java 17 APIs.
The missing historical `N_XPM_0208_03` remains explicitly unavailable and is
excluded from the tested count. No full conformance suite or interactive GUI
run was performed. The checks cover every available issue fixture, repaired
controls, official percent-encoded Unicode controls, custom relationships with
warnings shown/hidden, CMYK content-type overrides, and separate schema/structure
modes. Repaired material controls also reorder the corresponding property
indices, preserving their meaning when swapping layers.

JPEG checking establishes the prohibited four-component representation, not
complete JPEG validity, RGB color management, or PNG decoding. The existing
200,000,000-byte structure limit still applies; oversized parts retain their
incomplete-validation warning. Encrypted image bytes are not decrypted by this
header check. The unavailable fixture cannot be claimed as tested.

### Original investigation before the fix

[Issue #9](https://github.com/3MFConsortium/3mf_Editor/issues/9), checked live on
2026-10-31, is open with no comments. This investigation changes no production
code. Fresh production sources at `0bafd4dd1b39c584ab045cd9e604789561228e93`
were compiled with `--release 8` and run on Windows/Zulu OpenJDK 21.0.3 with
a 2 GiB heap, schema and structural checks, and UUID checking enabled.
Official fixtures are from `a63f4bf07c4ab287c8f84f1cdeab8ee92333a577`.

| Reported area / fixture | Current CLI XML result | Finding |
| --- | --- | --- |
| Unicode part names: `N_XPM_0208_01` | Pass | Raw Unicode character U+052A occurs in model and relationship part names. No package-name syntax check diagnoses it. This establishes the historical negative-fixture mismatch, not a rule banning all Unicode names. |
| Unicode part names: `N_XPM_0208_03` | Unavailable | Not present in this checkout; not counted as tested. |
| Unicode/content types: `N_XPX_0205_02` | Fail, `ER_108` | Rejected for duplicate Content Types Override PartName `/3D/3dmodel.model`, not for Unicode. This is not evidence of a general name-syntax fix. |
| Non-root relationship type: `N_XPM_0405_03` | Pass | `wrong3dmodel` is detected as `WR_022` in HTML, but omitted from XML and does not reject the model. |
| Unsupported `xml:space`: `N_XPM_0409_01` | Pass | The model has `xml:space="preserve"`; no explicit check rejects it. |
| Invalid path: `N_XPM_0415_01` | Pass | Existing part `/3D/nonroot/.3dmodel1.model` and matching references bypass existence-only checks. |
| Non-RGB image: `N_XPM_0419_01` | Pass | The package thumbnail is `Thumbnails/CMYKjpeg.jpg`; its JPEG SOF has four components. No thumbnail color-space check rejects it. |
| DTD: `N_XPM_0420_01` | Pass | Internal `DOCTYPE note` is accepted. Structure DOM factories do not disallow DOCTYPE; no explicit DTD rejection is present. This test does not exercise external entities or entity-expansion attacks. |
| Fewer than four triangles: `N_XPM_0426_01` | Fail, `ER_178` | The three-triangle mesh is now rejected by non-manifold geometry validation. The separate `ER_169` minimum-triangle check is inside displacement handling, so this does not establish a general minimum-count diagnostic. |
| Material above first layer: `N_XPM_0604_03` | Pass | `multiproperties pids="6 1"` references a color group before base materials. HTML reports structural failure, while XML incorrectly reports Pass. |
| Invalid color: `N_XPM_0608_01` | Fail, `SC_000` | `#FFHFFF` is rejected by the consolidated schema's color pattern. The reported missing rejection is not reproduced with schema validation enabled. |
| Positive controls: `P_XPM_0101_01`, `P_XPX_0101_01` | Both Pass | Relevant baseline packages remain accepted. |

Of 11 named negative fixtures, ten are available: seven produce Pass and three
produce Fail. Both positive controls pass. All 12 available packages complete
without process errors or timeouts. Five acceptance expectations match and
seven do not; the missing fixture is recorded separately.

### Causes and specification checks

In `FileStructureCheck.java`, the non-root relationship branch around lines
1277-1297 appends the `WR_022` XML markup to **fWarningReturn**, rather than
**fxmlWarningReturn**. It also treats an unknown type only as a warning. A fix
should verify the required relationship for a referenced model, without
blanket-rejecting custom relationship types permitted by extension rules.

The multiproperty branch around line 3450 appends plain text to
**fErrorReturn**, but creates no XML issue, increments no error counter, and
does not call `errChk`. `MainFrame` chooses the CLI status from the XML issue
strings (around lines 983 and 1128), explaining the HTML/XML disagreement.
Repairing these diagnostics should be a priority alongside the missing checks.

The live [Core specification](https://github.com/3MFConsortium/spec_core/blob/master/3MF%20Core%20Specification.md)
requires appropriate payload relationships (2.1.1), forbids leading-period part
names (2.2), prohibits DTD content and recommends treating it as an error
(2.3), forbids `xml:space` (2.3.2), and prohibits CMYK JPEGs (6.1).
Its Unicode-name guidance refers to OPC Part URI Syntax and uses SHOULD;
confirm the exact ZIP/URI encoding defect against OPC before implementing a
Unicode rejection policy. The
[Materials specification, 2.5](https://github.com/3MFConsortium/spec_materials/blob/master/3MF%20Materials%20Extension.md)
requires a material to be first in the multiproperty list. A follow-up fix should
cover correct encoded names, custom relationships, RGB images, ordinary XML
without DTDs, and correctly ordered multiproperties as positive controls.

Reproduce from the project directory (JDK 17+ and official fixture checkout):

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tests/cli/issues/Test-LowSeverity.ps1
```

The script accepts `-TestSuiteRoot PATH`, compiles fresh sources, isolates each
CLI process, and saves CSV/JSON summaries and XML/HTML/process logs under ignored
`tests/cli/results/low-severity-*`. The maintained regression exits 1 for failed
expectations, process errors, or unexpected missing fixtures; the known missing
historical `_0208_03` is recorded as unavailable without failing the run.
The original investigation evidence is in
`tests/cli/results/low-severity-20261031-125515`.
The new helper sources and script are not Git-ignored. No ignore rules were
changed. `git diff --check`
passes. No full conformance suite or interactive GUI run was performed.
