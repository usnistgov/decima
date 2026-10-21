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
import gov.nist.secauto.decima.xml.document.DefaultXMLDocumentFactory;
import gov.nist.secauto.decima.xml.document.JDOMDocument;
import gov.nist.secauto.decima.xml.service.ResourceResolverExtensionService;

import com.sun.net.httpserver.HttpServer;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaderJDOMFactory;
import org.jdom2.input.sax.XMLReaderSchemaFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.XMLConstants;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class XMLSecurityTest {
  @Rule
  public TemporaryFolder temporary = new TemporaryFolder();
  private HttpServer server;
  private final AtomicInteger secondaryRequests = new AtomicInteger();
  private final AtomicInteger documentRequests = new AtomicInteger();
  private byte[] document;
  private String origin;
  private String canary;
  private File secret;

  @Before
  public void setup() throws Exception {
    ClasspathHandler.initialize();
    canary = "HARMLESS-XXE-" + UUID.randomUUID();
    secret = temporary.newFile("canary.txt");
    Files.write(secret.toPath(), canary.getBytes(StandardCharsets.UTF_8));
    server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    origin = "http://127.0.0.1:" + server.getAddress().getPort();
    server.createContext("/", exchange -> {
      byte[] response;
      if ("/input".equals(exchange.getRequestURI().getPath())) {
        documentRequests.incrementAndGet();
        response = document;
      } else {
        secondaryRequests.incrementAndGet();
        response = ("<!ENTITY leaked SYSTEM '" + secret.toURI() + "'>").getBytes(StandardCharsets.UTF_8);
      }
      exchange.sendResponseHeaders(200, response.length);
      try (java.io.OutputStream output = exchange.getResponseBody()) {
        output.write(response);
      }
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
  public void allDefaultLoadingPathsRejectEntitiesWithoutSecondaryRequests() throws Exception {
    String[] declarations = {
        "<!DOCTYPE root [<!ENTITY leaked SYSTEM '" + secret.toURI() + "'>]>",
        "<!DOCTYPE root [<!ENTITY leaked SYSTEM '" + origin + "/entity'>]>",
        "<!DOCTYPE root [<!ENTITY % remote SYSTEM '" + origin + "/parameter'>%remote;]>",
        "<!DOCTYPE root SYSTEM '" + origin + "/dtd'>",
        "<!DOCTYPE root [<!ENTITY % local SYSTEM '" + secret.toURI() + "'>%local;]>",
        "<!DOCTYPE root [<!ENTITY leaked 'internal'>]>" };
    for (String declaration : declarations) {
      document = (declaration + "<root>&leaked;</root>").getBytes(StandardCharsets.UTF_8);
      File candidate = temporary.newFile();
      Files.write(candidate.toPath(), document);
      URL location = new URL(origin + "/input");
      DefaultXMLDocumentFactory factory = new DefaultXMLDocumentFactory();
      rejected(() -> new JDOMDocument(candidate));
      rejected(() -> new JDOMDocument(candidate, location));
      rejected(() -> new JDOMDocument(new ByteArrayInputStream(document), location.toString()));
      rejected(() -> new JDOMDocument(new ByteArrayInputStream(document), null));
      rejected(() -> new JDOMDocument(location));
      rejected(() -> factory.load(candidate));
      rejected(() -> factory.load(location));
      File download = new File(temporary.getRoot(), UUID.randomUUID().toString());
      rejected(() -> factory.load(location, download));
      assertEquals("Entity/DTD requests must never occur", 0, secondaryRequests.get());
    }
    assertEquals("Only explicit top-level URL retrieval is allowed", declarations.length * 3,
        documentRequests.get());
    document = "<root>safe</root>".getBytes(StandardCharsets.UTF_8);
    assertEquals("safe", new JDOMDocument(new URL(origin + "/input")).getJDOMDocument().getRootElement().getText());
    assertEquals(0, secondaryRequests.get());
  }

  @Test
  public void mandatorySettingFailureAbortsEngineInitialization() throws Exception {
    AtomicInteger rejectedSettings = new AtomicInteger();
    SAXBuilder builder = new SAXBuilder(new XMLReaderJDOMFactory() {
      @Override
      public boolean isValidating() {
        return false;
      }

      @Override
      public XMLReader createXMLReader() {
        return new XMLFilterImpl() {
          @Override
          public boolean getFeature(String name) {
            return false;
          }

          @Override
          public void setFeature(String name, boolean value) throws SAXNotRecognizedException {
            if (name.endsWith("disallow-doctype-decl")) {
              rejectedSettings.incrementAndGet();
              throw new SAXNotRecognizedException("Mandatory feature unavailable");
            }
          }

          @Override
          public void setProperty(String name, Object value) {
            // Allow other configuration so the test reaches the mandatory security feature.
          }
        };
      }
    });
    try {
      XMLSecurity.harden(builder).buildEngine();
      fail("An unsafe engine must not be returned");
    } catch (JDOMException expected) {
      assertEquals(1, rejectedSettings.get());
    }
  }

  @Test
  public void saxonSourceStyleAndResolverParsersRejectDoctype() throws Exception {
    Transformer transformer = new XSLTransformer().getTransformerFactory().newTransformer();
    document = ("<!DOCTYPE root SYSTEM '" + origin + "/dtd'><root/>").getBytes(StandardCharsets.UTF_8);
    StringWriter output = new StringWriter();
    rejected(() -> {
      transformer.transform(new StreamSource(new ByteArrayInputStream(document)), new StreamResult(output));
      return null;
    });
    rejected(() -> new XSLTransformer().getTransformerFactory().newTransformer(new StreamSource(
        new StringReader("<!DOCTYPE xsl:stylesheet SYSTEM '" + origin + "/dtd'>"
            + "<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='1.0'/>"))));
    Transformer loadDocument = new XSLTransformer().getTransformerFactory().newTransformer(new StreamSource(
        new StringReader("<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='2.0'>"
            + "<xsl:template match='/'><xsl:copy-of select=\"document('" + origin
            + "/input')\"/></xsl:template></xsl:stylesheet>")));
    rejected(() -> {
      loadDocument.transform(new StreamSource(new StringReader("<root/>")), new StreamResult(output));
      return null;
    });
    assertEquals(1, documentRequests.get());
    assertEquals(0, secondaryRequests.get());
    assertFalse(output.toString().contains(canary));
  }

  @Test
  public void bundledSchemaDependenciesWorkButRemoteMissesFailClosed() throws Exception {
    SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    XMLSecurity.hardenSchemaFactory(factory, ResourceResolverExtensionService.getInstance().getLSResolver());
    factory.newSchema(new StreamSource("classpath:schema/xmldsig/xmldsig-core-schema.xsd"));
    rejected(() -> factory.newSchema(new StreamSource(new StringReader(
        "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'>"
            + "<xs:import namespace='urn:missing' schemaLocation='" + origin + "/schema'/></xs:schema>"))));
    assertEquals(0, secondaryRequests.get());
  }

  @Test
  public void dynamicValidationPreservesBundledDtdWithoutTrustingCandidateDoctypes() throws Exception {
    SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    XMLSecurity.hardenSchemaFactory(factory, ResourceResolverExtensionService.getInstance().getLSResolver());
    SAXBuilder builder = XMLSecurity.harden(new SAXBuilder(new XMLReaderSchemaFactory(factory.newSchema())));
    builder.setEntityResolver(XMLSecurity.bundledEntityResolver(
        ResourceResolverExtensionService.getInstance().getEntityResolver()));
    String valid = "<ds:DigestMethod xmlns:ds='http://www.w3.org/2000/09/xmldsig#' "
        + "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' Algorithm='urn:test' "
        + "xsi:schemaLocation='http://www.w3.org/2000/09/xmldsig# "
        + "http://www.w3.org/TR/xmldsig-core/xmldsig-core-schema.xsd'/>";
    assertEquals("DigestMethod", builder.build(new StringReader(valid)).getRootElement().getName());
    rejected(() -> builder.build(new StringReader("<!DOCTYPE ds:DigestMethod SYSTEM '" + origin + "/dtd'>" + valid)));
    rejected(() -> builder.build(new StringReader("<root xmlns='urn:missing' "
        + "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='urn:missing "
        + origin + "/schema'/>")));
    assertEquals(0, secondaryRequests.get());
  }

  @Test
  public void xincludeAndRelativeEntityReferencesCannotReadCanary() throws Exception {
    File candidate = temporary.newFile("relative.xml");
    Files.writeString(candidate.toPath(), "<!DOCTYPE root [<!ENTITY leak SYSTEM 'canary.txt'>]><root>&leak;</root>");
    rejected(() -> new JDOMDocument(candidate));
    Files.writeString(candidate.toPath(), "<root xmlns:xi='http://www.w3.org/2001/XInclude'>"
        + "<xi:include href='" + origin + "/include' parse='text'/></root>");
    assertFalse(new JDOMDocument(candidate).asString(org.jdom2.output.Format.getRawFormat()).contains(canary));
    assertEquals(0, secondaryRequests.get());
  }

  private void rejected(Attempt attempt) throws Exception {
    try {
      attempt.run();
      fail("Malicious XML must be rejected");
    } catch (Exception expected) {
      for (Throwable cause = expected; cause != null; cause = cause.getCause()) {
        assertFalse("Canary must not appear in exceptions", String.valueOf(cause.getMessage()).contains(canary));
      }
    }
  }

  private interface Attempt {
    Object run() throws Exception;
  }
}
