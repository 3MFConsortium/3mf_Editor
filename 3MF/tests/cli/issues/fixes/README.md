# Issue-specific checks and fixture repairs

This directory contains scripts created while investigating and fixing issues.
See the [issue documentation](../README.md) for each check and
its expected results. Run commands from the project directory, for example:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tests/cli/issues/fixes/Test-ValidationConsumer.ps1
```

The general regression tools stay in their existing locations:

- `tests/cli/Run-Conformance.ps1`: execute the official conformance fixtures.
- `tests/cli/Conformance.ps1`: shared discovery, execution and report helpers.
- `tests/cli/Compare-Specification.ps1`: compare saved results with the specification.
- `tests/cli/issues/Test-Runner.ps1`: test the regression harness itself.

Generated results remain under `tests/cli/results/`. Moving the scripts does not
change the Java helper locations or the default external fixture checkout.
