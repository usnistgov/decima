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
 * SHALL NIST BE LIABLE FOR ANY DAMAGES, INCLUDING, BUT NOT LIMITED TO, DIRECT,
 * INDIRECT, SPECIAL OR CONSEQUENTIAL DAMAGES, ARISING OUT OF, RESULTING FROM, OR
 * IN ANY WAY CONNECTED WITH THIS SOFTWARE, WHETHER OR NOT BASED UPON WARRANTY,
 * CONTRACT, TORT, OR OTHERWISE, WHETHER OR NOT INJURY WAS SUSTAINED BY PERSONS OR
 * PROPERTY OR OTHERWISE, AND WHETHER OR NOT LOSS WAS SUSTAINED FROM, OR AROSE OUT
 * OF THE RESULTS OF, OR USE OF, THE SOFTWARE OR SERVICES PROVIDED HEREUNDER.
 */

package gov.nist.decima.xml.document;

import gov.nist.decima.core.document.DocumentException;
import gov.nist.decima.xml.document.context.DefaultXMLContextResolver;
import gov.nist.decima.xml.document.context.XMLContextResolver;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.SAXEngine;
import org.jdom2.located.LocatedJDOMFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

public class JDOMDocument extends AbstractJDOMDocument {

  private static final SAXEngine DEFAULT_SAX_ENGINE;

  static {
    SAXBuilder builder = new SAXBuilder();
    builder.setJDOMFactory(new LocatedJDOMFactory());
    try {
      DEFAULT_SAX_ENGINE = builder.buildEngine();
    } catch (JDOMException e) {
      throw new RuntimeException(e.getLocalizedMessage(), e);
    }
  }

  private final URL originalLocation;
  private Document document;
  private XMLContextResolver xmlContextResolver;

  public JDOMDocument(File location) throws DocumentException, FileNotFoundException {
    this(location, DEFAULT_SAX_ENGINE);
  }

  /**
   * Loads a JDOM document from a {@link File} location using the provided {@link SAXEngine}.
   * 
   * @param location
   *          the file location to load the XML document from
   * @param saxEngine
   *          the JDOM {@link SAXEngine} to use to parse the document
   * @throws DocumentException
   *           if an error occured while reading the XML document
   * @throws FileNotFoundException
   *           if the file to be loaded does not exist
   */
  public JDOMDocument(File location, SAXEngine saxEngine) throws DocumentException, FileNotFoundException {
    try {
      this.originalLocation = location.toURI().toURL();
    } catch (MalformedURLException e) {
      throw new DocumentException(e.getLocalizedMessage(), e);
    }
    String systemId = originalLocation.toString();
    try (InputStream is = new BufferedInputStream(new FileInputStream(location))) {
      this.document = loadDocumentFromInputStream(is, systemId, saxEngine);
    } catch (IOException e) {
      throw new DocumentException(e.getLocalizedMessage(), e);
    }
    xmlContextResolver = new DefaultXMLContextResolver(this.document);
  }

  public JDOMDocument(File location, URL originalLocation) throws DocumentException, FileNotFoundException {
    this(location, originalLocation, DEFAULT_SAX_ENGINE);
  }

  /**
   * Loads a JDOM document from a {@link File} location using the provided {@link SAXEngine}.
   * 
   * @param location
   *          the file location to load the XML document from
   * @param originalLocation
   *          the location the document was originally loaded from, before creating the current
   *          instance
   * @param saxEngine
   *          the JDOM {@link SAXEngine} to use to parse the document
   * @throws DocumentException
   *           if an error occured while reading the XML document
   * @throws FileNotFoundException
   *           if the file to be loaded does not exist
   */
  public JDOMDocument(File location, URL originalLocation, SAXEngine saxEngine)
      throws DocumentException, FileNotFoundException {
    this.originalLocation = originalLocation;

    String baseURI = this.originalLocation.toString();
    try (InputStream is = new BufferedInputStream(new FileInputStream(location))) {
      this.document = loadDocumentFromInputStream(is, baseURI, saxEngine);
    } catch (FileNotFoundException e) {
      throw e;
    } catch (IOException e) {
      throw new DocumentException(e.getLocalizedMessage(), e);
    }
    xmlContextResolver = new DefaultXMLContextResolver(this.document);
  }

  public JDOMDocument(URL location) throws DocumentException {
    this(location, DEFAULT_SAX_ENGINE);
  }

  public JDOMDocument(URL location, SAXEngine saxEngine) throws DocumentException {
    this(loadDocumentFromURL(location, saxEngine), location);
  }

  public JDOMDocument(InputStream is, String systemId) throws DocumentException, MalformedURLException {
    this(is, systemId, DEFAULT_SAX_ENGINE);
  }

  /**
   * Loads an XML document from an {@link InputStream}.
   * 
   * @param is
   *          the {@link InputStream} to read from
   * @param systemId
   *          the location of the resource being read
   * @param saxEngine
   *          the JDOM {@link SAXEngine} to use to parse the document
   * @throws DocumentException
   *           if an error occured while reading the XML document
   * @throws MalformedURLException
   *           if the original location URL is malformed
   */
  public JDOMDocument(InputStream is, String systemId, SAXEngine saxEngine)
      throws DocumentException, MalformedURLException {
    Objects.requireNonNull(is, "is");
    Objects.requireNonNull(saxEngine, "saxEngine");
    if (systemId != null) {
      this.originalLocation = new URL(systemId);
    } else {
      this.originalLocation = null;
    }
    this.document = loadDocumentFromInputStream(is, systemId, saxEngine);
    xmlContextResolver = new DefaultXMLContextResolver(this.document);
  }

  public JDOMDocument(Document document, URL originalLocation) throws DocumentException {
    this(document, originalLocation, new DefaultXMLContextResolver(document));
  }

  /**
   * A basic constructor for a {@link JDOMDocument} that can be used by extensions to construct a
   * new document.
   * 
   * @param document
   *          the JDOM 2 {@link Document} objects that backs this document.
   * @param originalLocation
   *          the original document location or {@code null}
   * @param resolver
   *          the {@link XMLContextResolver} to use to resolve XPaths, {@link XPathContext}, and
   *          systemId.
   */
  protected JDOMDocument(Document document, URL originalLocation, XMLContextResolver resolver) {
    this.originalLocation = originalLocation;
    this.document = document;
    this.xmlContextResolver = resolver;
  }

  /**
   * A copy constructor.
   * 
   * @param toCopy
   *          the {@link XMLDocument} to copy
   */
  public JDOMDocument(XMLDocument toCopy) {
    this.originalLocation = toCopy.getOriginalLocation();
    this.document = toCopy.getJDOMDocument().clone();
    xmlContextResolver = new DefaultXMLContextResolver(this.document);

  }

  private static Document loadDocumentFromURL(URL location, SAXEngine saxEngine) throws DocumentException {
    try (InputStream is = location.openStream()) {
      return loadDocumentFromInputStream(is, location.toString(), saxEngine);
    } catch (IOException e) {
      throw new DocumentException(e.getLocalizedMessage(), e);
    }
  }

  private static Document loadDocumentFromInputStream(InputStream is, String systemId, SAXEngine saxEngine)
      throws DocumentException {
    synchronized (saxEngine) {
      try {
        Document document;
        if (systemId == null) {
          document = saxEngine.build(is);
        } else {
          document = saxEngine.build(is, systemId);
        }
        return document;
      } catch (JDOMException | IOException e) {
        throw new DocumentException(e.getLocalizedMessage(), e);
      }
    }
  }

  @Override
  public URL getOriginalLocation() {
    return originalLocation;
  }

  @Override
  public Document getJDOMDocument(boolean copy) {
    Document retval;
    if (copy) {
      retval = document.clone();
    } else {
      retval = document;
    }
    return retval;
  }

  protected XMLContextResolver getXMLContextResolver() {
    return xmlContextResolver;
  }

}
