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

package gov.nist.decima.core.document;

import gov.nist.decima.core.document.context.DefaultXMLContextResolver;
import gov.nist.decima.core.document.context.XMLContextResolver;
import gov.nist.decima.core.jdom2.JDOMUtil;

import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.transform.JDOMSource;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import javax.xml.transform.Source;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactoryConfigurationException;

public class XMLDocumentFragment extends AbstractJDOMDocument {
  // private final XMLDocument document;
  private final Document document;
  private final URL originalLocation;
  private final XMLContextResolver xmlContextResolver;

  private static Element getBaseElement(XMLDocument document, String xpath) throws DocumentException {
    Element baseElement;
    try {
      baseElement = document.newXPathEvaluator().evaluateSingle(xpath, Filters.element());
    } catch (XPathExpressionException | XPathFactoryConfigurationException ex) {
      throw new DocumentException(ex);
    }

    if (baseElement == null) {
      throw new DocumentException("The XPath expression did not match an node: " + xpath);
    }
    return baseElement;
  }

  public XMLDocumentFragment(XMLDocument document, String xpath) throws DocumentException {
    this(document, getBaseElement(document, xpath));
  }

  /**
   * Constructs a new {@link XMLDocumentFragment} as a sub-tree of the provided document.
   * 
   * @param document
   *          the document to use a sub-tree from
   * @param baseElement
   *          the element in the provided document to use as the root of the sub-tree
   * @throws XPathFactoryConfigurationException
   *           if an error occurred due to an XPath configuration issue
   * @throws XPathExpressionException
   *           if an error occurred while processing an XPath expression
   */
  public XMLDocumentFragment(XMLDocument document, Element baseElement) {
    Document originalDocument = baseElement.getDocument();

    Element newRoot = baseElement.clone();
    this.document = new Document(newRoot);
    this.document.setBaseURI(originalDocument.getBaseURI());
    this.originalLocation = document.getOriginalLocation();
    String xpathBase = document.getXPath(baseElement);
    this.xmlContextResolver = new FragmentXMLContextResolver(xpathBase, newRoot, document);
  }

  public Element getElement() {
    return document.getRootElement();
  }

  @Override
  public String getSystemId() {
    return document.getBaseURI();
  }

  @Override
  public Document getJDOMDocument(boolean copy) {
    if (!copy) {
      throw new UnsupportedOperationException("Write access to the JDOM is not allowed for this class");
    } else {
      return new Document(getElement().clone());
    }
  }

  @Override
  public URL getOriginalLocation() {
    return originalLocation;
  }

  @Override
  public Source getSource() {
    return new JDOMSource(getElement());
  }

  @Override
  public String asString(Format format) {
    return JDOMUtil.toString(getElement(), format);
  }

  @Override
  public void copyTo(File file) throws FileNotFoundException, IOException {
    XMLOutputter out = new XMLOutputter();
    try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
      out.output(getElement(), os);
    }
  }

  @Override
  public XPathEvaluator newXPathEvaluator() throws XPathFactoryConfigurationException {
    return new JDOMBasedXPathEvaluator(document, getXMLContextResolver());
  }

  @Override
  protected XMLContextResolver getXMLContextResolver() {
    return xmlContextResolver;
  }

  private class FragmentXMLContextResolver extends DefaultXMLContextResolver {

    private final XMLDocument baseDocument;

    public FragmentXMLContextResolver(String baseXPath, Element baseElement, XMLDocument baseDocument) {
      super(baseXPath, baseElement);
      this.baseDocument = baseDocument;
    }

    @Override
    public String getSystemId(Content content) {
      String xpath = getXPath(content);
      Content baseContent;
      try {
        baseContent = baseDocument.newXPathEvaluator().evaluateSingle(xpath, Filters.content());
      } catch (XPathExpressionException | XPathFactoryConfigurationException ex) {
        throw new RuntimeException(ex);
      }
      return baseDocument.getSystemId(baseContent);
    }

  }
}
