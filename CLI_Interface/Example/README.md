# CLI Interface instructions

### Windows Setup:

- Copy this directory content to c:\Temp\CLI_Interface folder.
- Copy latest 3MFedit.jar executable to this folder.

### Batch run

Use the following syntax from the DOS command line to run the example. Note that you may need to change the paths in the test_list.txt file to match your directory structure. And the CLI interface does not currently support the secure content extension. 

cli_batch -C -L test_list.txt -I -W

### Test results

Each test results, with errors and warnings detected, will be found in the cliResults folder, including a XML aggregate with these results.
