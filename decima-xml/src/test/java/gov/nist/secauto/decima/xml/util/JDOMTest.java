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
import gov.nist.secauto.decima.xml.service.ResourceResolverExtensionService;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.JDOMParseException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.DefaultSAXHandlerFactory;
import org.jdom2.input.sax.XMLReaderJDOMFactory;
import org.jdom2.input.sax.XMLReaderSchemaFactory;
import org.jdom2.input.sax.XMLReaders;
import org.jdom2.located.LocatedJDOMFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.EntityResolver2;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.catalog.CatalogResolver;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

public class JDOMTest {
  // private static final Logger log = LogManager.getLogger(JDOMTest.class);

  private static final File XSI_FILE = new File("src/test/resources/jdom-resource-resolution/with-xsi.xml");
  private static final File NO_XSI_FILE = new File("src/test/resources/jdom-resource-resolution/no-xsi.xml");

  @BeforeClass
  public static void initialize() {
    ClasspathHandler.initialize();
  }

  @Test
  public void testEntityResolver() throws JDOMException, IOException {
    // log.debug("Testing EntityResolver with XSI definition...");
    handleEntityResolver(toInputSource(XSI_FILE));
  }

  @Test(expected = JDOMParseException.class)
  public void testEntityResolverNoXSI() throws JDOMException, IOException {
    // log.debug("Testing EntityResolver with no XSI definition...");
    handleEntityResolver(toInputSource(NO_XSI_FILE));
  }

  @Test
  public void testLSResolver() throws JDOMException, IOException, SAXException {
    // log.debug("Testing LSResourceResolver with XSI definition...");
    handleLSResolver(toInputSource(XSI_FILE));
  }

  @Test
  public void testLSResolverNoXSI() throws JDOMException, IOException, SAXException {
    // log.debug("Testing LSResourceResolver with no XSI definition...");
    handleLSResolver(toInputSource(NO_XSI_FILE));
  }
  //
  // @Test
  // public void testCatalog() throws JDOMException, IOException, SAXException {
  // testCatalogNoXSI();
  // testCatalogWithXSI();
  // }

  @Test
  public void testCatalogNoXSI() throws JDOMException, IOException, SAXException {
    // log.debug("Testing Catalog with no XSI definition...");
    handleCatalog(toInputSource(NO_XSI_FILE));
  }

  @Test
  public void testCatalogWithXSI() throws JDOMException, IOException, SAXException {
    // log.debug("Testing Catalog with XSI definition...");
    handleCatalog(toInputSource(XSI_FILE));
  }

  private InputSource toInputSource(File file) throws FileNotFoundException, MalformedURLException {
    InputSource retval = new InputSource();
    retval.setByteStream(new BufferedInputStream(new FileInputStream(file)));
    retval.setSystemId(file.toURI().toURL().toString());
    return retval;
  }

  private Document handleCatalog(InputSource source) throws SAXException, JDOMException, IOException {
    CatalogResolver resolver
        = ResourceResolverExtensionService.DEFAULT_CATALOG_RESOLVER;

    SchemaFactory schemafac = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    schemafac.setResourceResolver(new ProxyLSResourceResolver(resolver));
    // Schema schema = schemafac.newSchema();
    Schema schema = schemafac.newSchema(getSchemaSources());
    XMLReaderJDOMFactory factory = new XMLReaderSchemaFactory(schema);

    SAXBuilder sax = new SAXBuilder();
    EntityResolver entityResolver = new ProxyEntityResolver(resolver);
    sax.setEntityResolver(entityResolver);
    sax.setXMLReaderFactory(factory);
    return sax.build(source);
  }

  private Document handleEntityResolver(InputSource source) throws JDOMException, IOException {
    SAXBuilder sax = new SAXBuilder();
    sax.setXMLReaderFactory(XMLReaders.XSDVALIDATING);
    EntityResolver entityResolver = new TestEntityResolver();
    sax.setEntityResolver(entityResolver);
    return sax.build(source);
  }

  private Document handleLSResolver(InputSource source) throws SAXException, JDOMException, IOException {

    SchemaFactory schemafac = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    schemafac.setResourceResolver(new TestLSResourceResolver());
    // Schema schema = schemafac.newSchema();
    Schema schema = schemafac.newSchema(getSchemaSources());
    XMLReaderJDOMFactory factory = new XMLReaderSchemaFactory(schema);
    SAXBuilder sax = new SAXBuilder(factory, new DefaultSAXHandlerFactory(), new LocatedJDOMFactory());
    // sax.setXMLReaderFactory(XMLReaders.XSDVALIDATING);
    EntityResolver entityResolver = new TestEntityResolver();
    sax.setEntityResolver(entityResolver);
    return sax.build(source);
  }

  private Source[] getSchemaSources() {
    List<StreamSource> schemaSources = new ArrayList<>(2);
    schemaSources.add(new StreamSource("classpath:jdom-resource-resolution/test.xsd"));
    schemaSources.add(new StreamSource("classpath:jdom-resource-resolution/test2.xsd"));
    return schemaSources.toArray(new Source[schemaSources.size()]);
  }

  public static class ProxyEntityResolver implements EntityResolver {
    private final EntityResolver delegate;

    public ProxyEntityResolver(EntityResolver delegate) {
      this.delegate = delegate;
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
      // log.debug("resolveEntity called: publicId={}, systemId={}", publicId, systemId);
      return delegate.resolveEntity(publicId, systemId);
    }
  }

  private static class TestEntityResolver implements EntityResolver, EntityResolver2 {
    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
      // log.debug("resolveEntity called: publicId={}, systemId={}", publicId, systemId);
      return null;
    }

    @Override
    public InputSource getExternalSubset(String name, String baseURI) throws SAXException, IOException {
      // log.debug("getExternalSubset called: name={}, baseURI={}", name, baseURI);
      return null;
    }

    @Override
    public InputSource resolveEntity(String name, String publicId, String baseURI, String systemId)
        throws SAXException, IOException {
      // log.debug("resolveEntity called: name={}, publicId={}, baseURI={}, systemId={}",
      // name,
      // publicId, baseURI, systemId);
      return null;
    }

  }

  public static class TestLSResourceResolver implements LSResourceResolver {

    @Override
    public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
      // log.debug(
      // "resolveResource called: type={}, namespaceURI={}, publicId={}, systemId={},
      // baseURI={}",
      // type, namespaceURI, publicId, systemId, baseURI);
      return null;
    }

  }

  public static class ProxyLSResourceResolver implements LSResourceResolver {
    private final LSResourceResolver delegate;

    public ProxyLSResourceResolver(LSResourceResolver delegate) {
      this.delegate = delegate;
    }

    @Override
    public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
      // log.debug(
      // "resolveResource called: type={}, namespaceURI={}, publicId={}, systemId={},
      // baseURI={}",
      // type, namespaceURI, publicId, systemId, baseURI);
      LSInput retval = delegate.resolveResource(type, namespaceURI, publicId, systemId, baseURI);
      return retval;
    }

  }
}
