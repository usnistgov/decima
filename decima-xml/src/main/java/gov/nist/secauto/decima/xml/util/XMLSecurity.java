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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.input.SAXBuilder;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.output.XMLOutputter;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.validation.SchemaFactory;

/** Security controls for document parsing and application-selected schema compilation. */
public final class XMLSecurity {
  private static final Logger log = LogManager.getLogger(XMLSecurity.class);
  private static final String DISALLOW_DOCTYPE = "http://apache.org/xml/features/disallow-doctype-decl";
  private static final String GENERAL_ENTITIES = "http://xml.org/sax/features/external-general-entities";
  private static final String PARAMETER_ENTITIES = "http://xml.org/sax/features/external-parameter-entities";
  private static final String LOAD_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
  private static final String XINCLUDE = "http://apache.org/xml/features/xinclude";

  private XMLSecurity() {
  }

  /** Configure before building an engine. Unsupported controls fail at engine initialization. */
  public static SAXBuilder harden(SAXBuilder builder) {
    builder.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    builder.setFeature(DISALLOW_DOCTYPE, true);
    builder.setFeature(GENERAL_ENTITIES, false);
    builder.setFeature(PARAMETER_ENTITIES, false);
    builder.setFeature(LOAD_DTD, false);
    builder.setFeature(XINCLUDE, false);
    builder.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    builder.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
    builder.setEntityResolver((publicId, systemId) -> {
      throw new SAXException("External entities are prohibited");
    });
    return builder;
  }

  /**
   * Create a namespace-aware DOM factory that rejects DOCTYPE declarations and disables external
   * entities, external schemas, and XInclude processing.
   *
   * @return the secured factory
   * @throws IllegalStateException if a required security control is unsupported
   */
  public static DocumentBuilderFactory newDocumentBuilderFactory() {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    try {
      factory.setNamespaceAware(true);
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      factory.setFeature(DISALLOW_DOCTYPE, true);
      factory.setFeature(GENERAL_ENTITIES, false);
      factory.setFeature(PARAMETER_ENTITIES, false);
      factory.setFeature(LOAD_DTD, false);
      factory.setXIncludeAware(false);
      factory.setExpandEntityReferences(false);
      factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
      factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
    } catch (ParserConfigurationException | IllegalArgumentException | UnsupportedOperationException e) {
      throw new IllegalStateException("Unable to configure secure DOM parsing", e);
    }
    return factory;
  }

  /**
   * Create a transformer factory for identity serialization with secure processing enabled.
   * External DTD and stylesheet access are disabled when the implementation supports these attributes.
   *
   * @return the secured factory
   * @throws IllegalStateException if secure processing cannot be enabled
   */
  public static TransformerFactory newTransformerFactory() {
    TransformerFactory factory = TransformerFactory.newInstance();
    try {
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    } catch (TransformerConfigurationException e) {
      throw new IllegalStateException("Unable to configure secure transformation", e);
    }
    try {
      factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    } catch (IllegalArgumentException e) {
      log.debug("Transformer factory does not support ACCESS_EXTERNAL_DTD", e);
    }
    try {
      factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
    } catch (IllegalArgumentException e) {
      log.debug("Transformer factory does not support ACCESS_EXTERNAL_STYLESHEET", e);
    }
    return factory;
  }

  /** Create a namespace-aware reader without DTD, entity, or XInclude processing. */
  public static XMLReader newXMLReader() throws ParserConfigurationException, SAXException {
    SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setNamespaceAware(true);
    factory.setValidating(false);
    factory.setXIncludeAware(false);
    factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    factory.setFeature(DISALLOW_DOCTYPE, true);
    factory.setFeature(GENERAL_ENTITIES, false);
    factory.setFeature(PARAMETER_ENTITIES, false);
    factory.setFeature(LOAD_DTD, false);
    XMLReader reader = factory.newSAXParser().getXMLReader();
    reader.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    reader.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
    reader.setEntityResolver((publicId, systemId) -> {
      throw new SAXException("External entities are prohibited");
    });
    return reader;
  }

  /**
   * Resolve schema hints through trusted catalogs only. DOCTYPE rejection remains enabled on the
   * document reader; its schema validator can also call the SAX entity resolver for XSD resources.
   */
  public static EntityResolver bundledEntityResolver(EntityResolver resolver) {
    return (publicId, systemId) -> {
      InputSource input = resolver == null ? null : resolver.resolveEntity(publicId, systemId);
      URL location = bundledLocation(new URL(input == null ? systemId : input.getSystemId()));
      if (input == null) {
        input = new InputSource(location.toString());
      }
      if (location.getPath().endsWith(".xsd")) {
        // Xerces propagates the candidate reader's DOCTYPE prohibition into dynamic schema loading.
        // Expand only this checked bundled schema's DTD, then omit its DOCTYPE from the grammar input.
        SAXBuilder schemas = newBundledSchemaBuilder();
        schemas.setEntityResolver(bundledEntityResolver(resolver));
        try {
          Document schema = schemas.build(location);
          schema.setDocType(null);
          input.setCharacterStream(new java.io.StringReader(new XMLOutputter().outputString(schema)));
          input.setByteStream(null);
        } catch (JDOMException e) {
          throw new SAXException("Unable to parse bundled schema", e);
        }
      } else {
        input.setByteStream(location.openStream());
        input.setCharacterStream(null);
      }
      return input;
    };
  }

  /**
   * Create a parser for schema input that has already passed bundledLocation. DOCTYPE is allowed
   * because XML-DSig references XMLSchema.dtd and datatypes.dtd. Every external reference must resolve
   * through the recursive bundled resolver or fail. This parser must never receive candidate input.
   */
  private static SAXBuilder newBundledSchemaBuilder() {
    SAXBuilder builder = new SAXBuilder();
    builder.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    builder.setFeature(DISALLOW_DOCTYPE, false);
    builder.setFeature(XINCLUDE, false);
    builder.setFeature(GENERAL_ENTITIES, false);
    builder.setFeature(PARAMETER_ENTITIES, true);
    builder.setFeature(LOAD_DTD, true);
    builder.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "classpath");
    builder.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
    builder.setEntityResolver(bundledEntityResolver(null));
    return builder;
  }

  static URL bundledLocation(URL location) throws java.io.IOException {
    String path = location.getPath();
    if (!"classpath".equals(location.getProtocol()) || path.contains("..") || path.contains("%")
        || !(path.startsWith("schema/") || path.startsWith("xsd/") || path.startsWith("scapval-xsd/"))) {
      throw new java.io.IOException("Schema dependency is not a bundled validation resource");
    }
    return location;
  }

  /**
   * Restrict trusted schema dependencies to bundled resources. The bundled signature schema needs the
   * XML Schema DTD and its relative datatype DTD. This policy must not parse candidate documents.
   */
  public static void hardenSchemaFactory(SchemaFactory factory, LSResourceResolver resolver) throws SAXException {
    factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "classpath");
    factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "classpath");
    factory.setResourceResolver((type, namespace, publicId, systemId, base) -> {
      LSInput input = resolver == null ? null : resolver.resolveResource(type, namespace, publicId, systemId, base);
      try {
        URL location
            = bundledLocation(input == null ? (base == null ? new URL(systemId) : new URL(new URL(base), systemId))
                : new URL(input.getSystemId()));
        if (input != null) {
          // A resolver result bypasses JAXP protocol checks. Open only the checked bundled target.
          input.setByteStream(location.openStream());
          input.setCharacterStream(null);
          input.setStringData(null);
        }
        return input;
      } catch (java.io.IOException e) {
        throw new IllegalStateException("Unable to resolve bundled schema dependency", e);
      }
    });
  }
}
