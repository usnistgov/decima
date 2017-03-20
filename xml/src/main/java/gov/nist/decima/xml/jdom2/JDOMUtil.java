/**
 * Portions of this software was developed by employees of the National Institute
 * of Standards and Technology (NIST), an agency of the Federal Government.
 * Pursuant to title 17 United States Code Section 105, works of NIST employees are
 * not subject to copyright protection in the United States and are considered to
 * be in the public domain. Permission to freely use, copy, modify, and distribute
 * this software and its documentation without fee is hereby granted, provided that
 * this notice and disclaimer of warranty appears in all copies.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS' WITHOUT ANY WARRANTY OF ANY KIND, EITHER
 * EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
 * THAT THE SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND FREEDOM FROM
 * INFRINGEMENT, AND ANY WARRANTY THAT THE DOCUMENTATION WILL CONFORM TO THE
 * SOFTWARE, OR ANY WARRANTY THAT THE SOFTWARE WILL BE ERROR FREE. IN NO EVENT
 * SHALL NASA BE LIABLE FOR ANY DAMAGES, INCLUDING, BUT NOT LIMITED TO, DIRECT,
 * INDIRECT, SPECIAL OR CONSEQUENTIAL DAMAGES, ARISING OUT OF, RESULTING FROM, OR
 * IN ANY WAY CONNECTED WITH THIS SOFTWARE, WHETHER OR NOT BASED UPON WARRANTY,
 * CONTRACT, TORT, OR OTHERWISE, WHETHER OR NOT INJURY WAS SUSTAINED BY PERSONS OR
 * PROPERTY OR OTHERWISE, AND WHETHER OR NOT LOSS WAS SUSTAINED FROM, OR AROSE OUT
 * OF THE RESULTS OF, OR USE OF, THE SOFTWARE OR SERVICES PROVIDED HEREUNDER.
 */

package gov.nist.decima.xml.jdom2;

import gov.nist.decima.xml.service.ResourceResolverExtensionService;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.SAXEngine;
import org.jdom2.input.sax.XMLReaderJDOMFactory;
import org.jdom2.input.sax.XMLReaderSchemaFactory;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.xml.sax.SAXException;

import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

public class JDOMUtil {
  private static final Format DEFAULT_FORMAT = Format.getPrettyFormat();

  public static String toString(Document doc) {
    return toString(doc, DEFAULT_FORMAT);
  }

  /**
   * A convenience method that produces a String containing a JDOM2 {@link Document} that has been
   * formatted using the provided format argument.
   * 
   * @param doc
   *          the document to write to the String
   * @param format
   *          the formatter to use
   * @return a string representation of the document
   */
  public static String toString(Document doc, Format format) {
    XMLOutputter out = new XMLOutputter(format);
    return out.outputString(doc);
  }

  public static String toString(Element element) {
    return toString(element, DEFAULT_FORMAT);
  }

  public static String toString(Element element, Format format) {
    XMLOutputter out = new XMLOutputter(format);
    return out.outputString(element);
  }

  /**
   * A convenience method to construct a new validating {@link SAXEngine} based on a collection of
   * schema sources. Any registered resolver extensions are also setup with the SAXEngine.
   * 
   * @param schemaSources
   *          an array of schema to use for validation
   * @return a new {@link SAXEngine} instance
   * @throws SAXException
   *           if an error occurred while parsing the schema
   * @throws JDOMException
   *           if an error occurred while constructing the {@link SAXEngine}
   * @see ResourceResolverExtensionService
   */
  public static SAXEngine newValidatingSAXEngine(Source[] schemaSources) throws SAXException, JDOMException {
    SchemaFactory schemafac = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    schemafac.setResourceResolver(ResourceResolverExtensionService.getInstance().getLSResolver());
    // Schema schema = schemafac.newSchema();
    Schema schema = schemafac.newSchema(schemaSources);
    XMLReaderJDOMFactory factory = new XMLReaderSchemaFactory(schema);
    SAXBuilder sax = new SAXBuilder(factory);
    sax.setEntityResolver(ResourceResolverExtensionService.getInstance().getEntityResolver());
    return sax.buildEngine();
  }

  public static SAXEngine newValidatingSAXEngine(URL schema) throws SAXException, JDOMException {
    return newValidatingSAXEngine(new Source[] { new StreamSource(schema.toExternalForm()) });
  }

  private JDOMUtil() {
    // disable construction
  }
}
