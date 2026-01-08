# 3MF Editor V1.0

This utility primary use case is the development of test cases and the validation of 3MF packages against the 3MF schema and specification conformance rules. From a tree view, the 3MF package structure can be edited including cutting and pasting parts. Any selected 3MF part with XML can be edited.

A secondary use case for this utility is to enable ad hoc editing of 3MF files to assist with learning about the 3MF imaging format and the validation of 3MF files to determine if the conform to the 3MF standard. 

The editor can be executed by double clicking on the JAR file or if you are encountering a problem with the editor you can launch it with the console output to see debug messages that may help isolate the problem as follows:

java -jar 3MFedit.jar

>**Disclaimer** this editor is limited to relatively small files in order to validate 3MF conformance for sample produced 3MF files, but not for validating large user files.

The 3MF Editor supports the following versions of the 3MF specifications:
* 3MF Core Specification - [Version 1.4.0](https://github.com/3MFConsortium/spec_core/blob/1.4.0/3MF%20Core%20Specification.md)
* 3MF Materials and Properties Extension - [Version 1.2.1](https://github.com/3MFConsortium/spec_materials/blob/1.2.1/3MF%20Materials%20Extension.md)
* 3MF Production Extension - [Version 1.2.0](https://github.com/3MFConsortium/spec_production/blob/1.2.0/3MF%20Production%20Extension.md)
* 3MF Slice Extension - [Version 1.0.2](https://github.com/3MFConsortium/spec_slice/blob/1.0.2/3MF%20Slice%20Extension.md) 
* 3MF Beam Lattice Extension - [Version 1.2.0](https://github.com/3MFConsortium/spec_beamlattice/blob/1.2.0/3MF%20Beam%20Lattice%20Extension.md) 
* 3MF Secure Content Extension - [Version 1.0.2](https://github.com/3MFConsortium/spec_securecontent/blob/1.0.2/3MF%20Secure%20Content.md). It validates encrypted parts by the wrapping 3MF test key, as defined in the Appendix D of 3MF Test Suite specification.
* 3MF Boolean Operations Extension - [Version 1.1.1](https://github.com/3MFConsortium/spec_booleans/blob/1.1.1/3MF%20Boolean%20operations.md)
* 3MF Displacement Extension - [Version 1.0.0](https://github.com/3MFConsortium/spec_displacement/blob/1.0.0/3MF%20Displacement%20Extension.md)
* [Office Open XML File Formats - Open Packaging Conventions - December 2012](https://www.ecma-international.org/news/TC45_current_work/Office%20Open%20XML%20Part%202%20-%20Open%20Packaging%20Conventions.pdf)

It requires to select latest [3MF Consolidated Schema](https://github.com/3MFConsortium/test_suites/tree/master/Consolidated_Schema/Schema) downloaded from the 3MF Test suite.

