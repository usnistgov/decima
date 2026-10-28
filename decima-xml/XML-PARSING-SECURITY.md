# XML parsing boundaries

Decima applies strict candidate-input parsing at these sites:

- [`JDOMDocument`](src/main/java/gov/nist/secauto/decima/xml/document/JDOMDocument.java)
- [`DefaultXMLDocumentFactory`](src/main/java/gov/nist/secauto/decima/xml/document/DefaultXMLDocumentFactory.java)
- [`JDOMUtil.newValidatingSAXEngine`](src/main/java/gov/nist/secauto/decima/xml/jdom2/JDOMUtil.java)
- [`SchemaAssessment`](../decima-xml-assessment/src/main/java/gov/nist/secauto/decima/xml/assessment/schema/SchemaAssessment.java)
- [`SVRLParser`, both modes](../decima-xml-assessment/src/main/java/gov/nist/secauto/decima/xml/assessment/schematron/SVRLParser.java)
- [`SchematronAssessment`](../decima-xml-assessment/src/main/java/gov/nist/secauto/decima/xml/assessment/schematron/SchematronAssessment.java)
- [`DefaultSchematronCompiler`, both compiler paths](../decima-xml-assessment/src/main/java/gov/nist/secauto/decima/xml/schematron/DefaultSchematronCompiler.java)
- [`XSLTransformer`, source and stylesheet parsers](src/main/java/gov/nist/secauto/decima/xml/util/XSLTransformer.java)
- [`AbstractJDOMDocument`, identity transformer](src/main/java/gov/nist/secauto/decima/xml/document/AbstractJDOMDocument.java)

Candidate XML parsers reject DOCTYPE declarations. External general entities, parameter entities,
external DTD loading, and XInclude are disabled. Required parser controls fail initialization if
unsupported. Identity serialization always uses `XMLSecurity.newTransformerFactory()`, with secure
processing required and external DTD and stylesheet access disabled when supported.

`XMLSecurity.newDocumentBuilderFactory()` provides the same strict candidate-input controls for
DOM-based callers. `XMLSecurity.newXMLReader()` provides a secured SAX reader.

Explicit top-level URL retrieval remains supported. It does not authorize secondary entity or DTD
retrieval. Arbitrary URL retrieval, XSLT document access, signature references, and general resource
consumption policies are separate concerns.

[Schema compilation](src/main/java/gov/nist/secauto/decima/xml/util/XMLSecurity.java)
has a separate trusted-resource boundary. Application-selected schema roots may depend on resources
under the bundled `schema/`, `xsd/`, and `scapval-xsd/` classpath roots. Catalog extensions are
consulted before checking the resolved target. File and network fallback targets are rejected.
Custom resolver implementations are trusted application code and must not perform unapproved I/O
before returning a result. Custom resolvers do not exempt returned targets from the bundled-resource
check.

The bundled XML-DSig schema references `XMLSchema.dtd`, which loads `datatypes.dtd` through a
parameter entity. Only the private `newBundledSchemaBuilder()` reparses a checked schema URL after
`bundledLocation()` accepts it. That parser permits the bundled DTD chain, disables external general
entities and XInclude, and uses a recursive bundled resolver for every external reference. Any
reference outside the bundled roots fails. The parsed schema has its DOCTYPE removed before it is
passed to schema validation. Candidate documents always use the strict parser and cannot be routed
through the bundled-schema parser.

[`XSLTransformer`](src/main/java/gov/nist/secauto/decima/xml/util/XSLTransformer.java) uses strict
source and stylesheet parser pools. Required extension functions remain enabled.

Constructors accepting a caller-supplied `SAXEngine` or a pre-parsed `Document` are caller-controlled
APIs. Callers must secure those engines and the original parsing step. [`DefaultSchematron`](../decima-xml-assessment/src/main/java/gov/nist/secauto/decima/xml/schematron/DefaultSchematron.java)
constructed with a caller-provided `Document` and `TransformerFactory` also depends on caller-controlled
parsing and transformation policy. These APIs cannot undo entity resolution or I/O that occurred
before decima received the objects.

SCAPVAL signature, signing, and JAXB command parsers are covered in the SCAPVAL repository through
`XMLSecurity.newDocumentBuilderFactory()` and `XMLSecurity.newXMLReader()`.

Candidate DOCTYPE rejection during XML-DSig validation is covered by
`XMLSecurityTest.dynamicValidationPreservesBundledDtdWithoutTrustingCandidateDoctypes`.

The approved Saxon source replacement is documented in
[source provenance](SAXON-SOURCE-PROVENANCE.md).
