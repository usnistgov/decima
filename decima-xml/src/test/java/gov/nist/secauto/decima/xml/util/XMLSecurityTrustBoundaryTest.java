/**
 * Portions of this software was developed by employees of the National Institute
 * of Standards and Technology (NIST), an agency of the Federal Government and is
 * being made available as a public service. Pursuant to title 17 United States
 * Code Section 105, works of NIST employees are not subject to copyright
 * protection in the United States. This software may be subject to foreign
 * copyright. Permission in the United States and in foreign countries, to the
 * extent that NIST may hold copyright, to use, copy, modify, create derivative
 * works, and distribute this software and its documentation without fee is hereby
 * granted on a non-exclusive basis, provided that this notice and disclaimer
 * of warranty appears in all copies.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS' WITHOUT ANY WARRANTY OF ANY KIND, EITHER
 * EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
 * THAT THE SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND FREEDOM FROM
 * INFRINGEMENT, AND ANY WARRANTY THAT THE DOCUMENTATION WILL CONFORM TO THE
 * SOFTWARE, OR ANY WARRANTY THAT THE SOFTWARE WILL BE ERROR FREE.  IN NO EVENT
 * SHALL NIST BE LIABLE FOR ANY DAMAGES, INCLUDING, BUT NOT LIMITED TO, DIRECT,
 * INDIRECT, SPECIAL OR CONSEQUENTIAL DAMAGES, ARISING OUT OF, RESULTING FROM,
 * OR IN ANY WAY CONNECTED WITH THIS SOFTWARE, WHETHER OR NOT BASED UPON WARRANTY,
 * CONTRACT, TORT, OR OTHERWISE, WHETHER OR NOT INJURY WAS SUSTAINED BY PERSONS OR
 * PROPERTY OR OTHERWISE, AND WHETHER OR NOT LOSS WAS SUSTAINED FROM, OR AROSE OUT
 * OF THE RESULTS OF, OR USE OF, THE SOFTWARE OR SERVICES PROVIDED HEREUNDER.
 */

package gov.nist.secauto.decima.xml.util;

import gov.nist.secauto.decima.core.classpath.ClasspathHandler;
import gov.nist.secauto.decima.xml.document.JDOMDocument;
import gov.nist.secauto.decima.xml.service.ResourceResolverExtensionService;

import com.sun.net.httpserver.HttpServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class XMLSecurityTrustBoundaryTest {
  @Rule
  public TemporaryFolder temporary = new TemporaryFolder();
  private HttpServer server;
  private final AtomicInteger requests = new AtomicInteger();
  private String origin;

  @Before
  public void setup() throws Exception {
    ClasspathHandler.initialize();
    server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    origin = "http://127.0.0.1:" + server.getAddress().getPort();
    server.createContext("/", exchange -> {
      requests.incrementAndGet();
      exchange.sendResponseHeaders(200, -1);
      exchange.close();
    });
    server.start();
  }

  @After
  public void shutdown() {
    if (server != null) {
      server.stop(0);
    }
  }

  @Test
  public void bundledLocationsRejectOtherProtocolsAndRootEscapes() throws Exception {
    String[] rejected = { "file:/schema/x.xsd", origin + "/x.xsd", "jar:file:/unused.jar!/schema/x.xsd",
        "classpath:../schema/x.xsd", "classpath:schema/%2e%2e/x", "classpath:other/x.xsd" };
    for (String value : rejected) {
      try {
        XMLSecurity.bundledLocation(new URL(value));
        fail("Untrusted location must be rejected: " + value);
      } catch (IOException expected) {
        assertEquals("Schema dependency is not a bundled validation resource", expected.getMessage());
      }
    }
    for (String root : new String[] { "schema", "xsd", "scapval-xsd" }) {
      URL location = new URL("classpath:" + root + "/x.xsd");
      assertSame(location, XMLSecurity.bundledLocation(location));
    }
    assertEquals(0, requests.get());
  }

  @Test
  public void schemaImportsOutsideBundledRootsFailClosed() throws Exception {
    File dependency = temporary.newFile("outside.xsd");
    Files.writeString(dependency.toPath(), "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'/>");
    for (String location : new String[] { dependency.toURI().toString(), origin + "/schema",
        "classpath:other/outside.xsd" }) {
      assertSchemaDependencyRejected("<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'>"
          + "<xs:import namespace='urn:outside' schemaLocation='" + location + "'/></xs:schema>");
    }
    assertEquals(0, requests.get());
  }

  @Test
  public void schemaDtdOutsideBundledRootsFailsClosed() throws Exception {
    File dependency = temporary.newFile("outside.dtd");
    Files.writeString(dependency.toPath(), "<!ELEMENT xs:schema ANY>");
    for (String location : new String[] { dependency.toURI().toString(), origin + "/dtd" }) {
      assertSchemaDependencyRejected("<!DOCTYPE xs:schema SYSTEM '" + location + "'>"
          + "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'/>");
    }
    assertEquals(0, requests.get());
  }

  private void assertSchemaDependencyRejected(String schema) throws Exception {
    SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    XMLSecurity.hardenSchemaFactory(factory, null);
    try {
      factory.newSchema(new StreamSource(new StringReader(schema)));
      fail("Schema dependency outside bundled roots must be rejected");
    } catch (IllegalStateException expected) {
      assertEquals("Unable to resolve bundled schema dependency", expected.getMessage());
    }
  }

  @Test
  public void bundledSchemaDtdChainUsesRecursiveResolver() throws Exception {
    AtomicInteger dtdRequests = new AtomicInteger();
    AtomicInteger datatypeRequests = new AtomicInteger();
    org.xml.sax.EntityResolver delegate = ResourceResolverExtensionService.getInstance().getEntityResolver();
    InputSource schema = XMLSecurity.bundledEntityResolver((publicId, systemId) -> {
      if (systemId.endsWith("XMLSchema.dtd")) {
        dtdRequests.incrementAndGet();
      } else if (systemId.endsWith("datatypes.dtd")) {
        datatypeRequests.incrementAndGet();
      }
      return delegate.resolveEntity(publicId, systemId);
    }).resolveEntity(null, "classpath:schema/xmldsig/xmldsig-core-schema.xsd");
    assertTrue(schema.getCharacterStream() != null);
    assertEquals(1, dtdRequests.get());
    assertEquals(1, datatypeRequests.get());
    assertEquals(0, requests.get());
  }

  @Test
  public void domFactoryRejectsDoctypeAndFileEntitiesWithoutRequests() throws Exception {
    File secret = temporary.newFile("canary.txt");
    Files.writeString(secret.toPath(), "GENERATED-XXE-CANARY");
    String[] declarations = { "<!DOCTYPE root>",
        "<!DOCTYPE root [<!ENTITY leak SYSTEM '" + secret.toURI().toURL() + "'>]>",
        "<!DOCTYPE root [<!ENTITY % remote SYSTEM '" + origin + "/parameter'>%remote;]>" };
    for (String declaration : declarations) {
      try {
        XMLSecurity.newDocumentBuilderFactory().newDocumentBuilder()
            .parse(new InputSource(new StringReader(declaration + "<root>&leak;</root>")));
        fail("Candidate DOCTYPE must be rejected");
      } catch (SAXParseException expected) {
        assertTrue(expected.getMessage().contains("DOCTYPE"));
        assertFalse(expected.getMessage().contains("GENERATED-XXE-CANARY"));
      }
    }
    assertEquals(0, requests.get());
  }

  @Test
  public void domFactoryAppliesAllRequiredControls() throws Exception {
    DocumentBuilderFactory factory = XMLSecurity.newDocumentBuilderFactory();
    assertTrue(factory.isNamespaceAware());
    assertTrue(factory.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING));
    assertTrue(factory.getFeature("http://apache.org/xml/features/disallow-doctype-decl"));
    assertFalse(factory.getFeature("http://xml.org/sax/features/external-general-entities"));
    assertFalse(factory.getFeature("http://xml.org/sax/features/external-parameter-entities"));
    assertFalse(factory.getFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd"));
    assertFalse(factory.isXIncludeAware());
    assertFalse(factory.isExpandEntityReferences());
    assertEquals("", factory.getAttribute(XMLConstants.ACCESS_EXTERNAL_DTD));
    assertEquals("", factory.getAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA));
    assertEquals("urn:test", factory.newDocumentBuilder().parse(new InputSource(
        new StringReader("<root xmlns='urn:test'/>"))).getDocumentElement().getNamespaceURI());
  }

  @Test
  public void identityTransformerEnablesSecureProcessingAndSerializesDocuments() throws Exception {
    assertTrue(XMLSecurity.newTransformerFactory().getFeature(XMLConstants.FEATURE_SECURE_PROCESSING));
    JDOMDocument document = new JDOMDocument(new java.io.ByteArrayInputStream(
        "<root>safe</root>".getBytes(StandardCharsets.UTF_8)), null);
    try (java.io.InputStream input = document.newInputStream()) {
      assertTrue(new String(input.readAllBytes(), StandardCharsets.UTF_8).contains("<root>safe</root>"));
    }
    assertEquals(0, requests.get());
  }

  @Test
  public void bothTransformerFactoriesRejectAnExternalDtdInTextInput() throws Exception {
    // Text input that references an external DTD must fail without contacting the server, through
    // the general factory and also through the tree serializer if someone misuses it with text.
    String text = "<!DOCTYPE root SYSTEM '" + origin + "/external.dtd'><root/>";
    for (javax.xml.transform.TransformerFactory factory : new javax.xml.transform.TransformerFactory[] {
        XMLSecurity.newTransformerFactory(), XMLSecurity.newTreeSerializerFactory() }) {
      assertTrue(factory.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING));
      try {
        factory.newTransformer().transform(new StreamSource(new StringReader(text)),
            new javax.xml.transform.stream.StreamResult(new java.io.ByteArrayOutputStream()));
        fail("An external DTD must not be loaded");
      } catch (javax.xml.transform.TransformerException expected) {
        // rejected before any request is made
      }
    }
    assertEquals(0, requests.get());
  }

  @Test
  public void serializingAJdomTreeLogsNoParserPropertyWarning() throws Exception {
    // Saxon applies ACCESS_EXTERNAL_DTD as a parser property; the reader behind a JDOMSource rejects
    // it and Saxon prints "does not recognize the property" to stderr once per serialization.
    JDOMDocument document = new JDOMDocument(new java.io.ByteArrayInputStream(
        "<root xmlns='urn:test'><child a='1'>safe</child></root>".getBytes(StandardCharsets.UTF_8)), null);
    java.io.PrintStream original = System.err;
    java.io.ByteArrayOutputStream captured = new java.io.ByteArrayOutputStream();
    String serialized;
    System.setErr(new java.io.PrintStream(captured, true, StandardCharsets.UTF_8.name()));
    try (java.io.InputStream input = document.newInputStream()) {
      serialized = new String(input.readAllBytes(), StandardCharsets.UTF_8);
    } finally {
      System.setErr(original);
    }
    String stderr = captured.toString(StandardCharsets.UTF_8.name());
    assertFalse(stderr, stderr.contains("does not recognize the property"));
    assertTrue(serialized, serialized.contains("<root xmlns=\"urn:test\"><child a=\"1\">safe</child></root>"));
  }
}
