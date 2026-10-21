# XML parsing boundaries

Default [document loading](src/main/java/gov/nist/secauto/decima/xml/document/JDOMDocument.java)
and the [document factory](src/main/java/gov/nist/secauto/decima/xml/document/DefaultXMLDocumentFactory.java)
reject DOCTYPE declarations. External general entities, parameter entities, external DTD loading,
and XInclude are disabled. Required parser controls fail initialization if unsupported.

Explicit top-level URL retrieval remains supported. It does not authorize secondary entity or DTD
retrieval. Arbitrary URL retrieval, XSLT document access, signature references, and general resource
consumption policies are separate concerns.

[Schema compilation](src/main/java/gov/nist/secauto/decima/xml/util/XMLSecurity.java)
has a separate trusted-resource boundary. Application-selected schema roots may depend on resources
under the bundled schema, xsd, and scapval-xsd classpath roots. Catalog extensions are consulted
before checking the resolved target. File and network fallback targets are rejected.
Custom resolver implementations are trusted application code and must not perform unapproved I/O
before returning a result. Custom resolvers do not exempt returned targets from the bundled-resource
check.

The bundled signature schema legitimately contains a DTD. Trusted schema parsing permits only
checked bundled DTD resources. For dynamic schema validation, the bundled schema is parsed and
its DOCTYPE removed before being supplied to the strict candidate reader. Candidate DOCTYPE
declarations remain prohibited throughout this process. The selection of a bundled schema from a
catalog does not select a permissive candidate parser.

[Saxon transformations](src/main/java/gov/nist/secauto/decima/xml/util/XSLTransformer.java)
use strict source and stylesheet parser pools. Required extension functions remain enabled.

Constructors accepting caller-supplied SAX engines or pre-parsed documents remain caller-controlled
APIs. Callers must secure those engines and the original parsing step. These APIs cannot undo
entity resolution or I/O that occurred before decima received the document. SCAPVAL's normal
validation uses secured engines; auxiliary signature/signing/JAXB command parsers are outside
this remediation.

The approved Saxon source replacement is documented in
[source provenance](SAXON-SOURCE-PROVENANCE.md).
