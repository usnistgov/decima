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

import gov.nist.decima.core.document.DefaultSourceInfo;
import gov.nist.decima.core.document.Document;
import gov.nist.decima.core.document.DocumentException;
import gov.nist.decima.core.document.SourceInfo;
import gov.nist.decima.xml.document.context.DefaultXMLContextResolver;
import gov.nist.decima.xml.document.context.XMLContextResolver;

import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.filter.Filters;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactoryConfigurationException;

public class CompositeXMLDocument extends JDOMDocument {
  public static final String COMPOSITE_NS_URI = "http://decima.nist.gov/xml/composite";
  public static final String COMPOSITE_PLACEHOLDER_LOCAL_NAME = "sub";
  public static final QName COMPOSITE_QNAME = new QName(COMPOSITE_NS_URI, COMPOSITE_PLACEHOLDER_LOCAL_NAME);

  private Map<Element, String> elementToSystemIdMap = new HashMap<>();
  private Map<String, ? extends XMLDocument> composites;

  /**
   * Construct a new CompositeXMLDocument using the provided base document as the root and the
   * provided templates as inserted content.
   * 
   * @param base
   *          the root document to insert into
   * @param templates
   *          a mapping of insertion point labels to documents to insert
   * @throws DocumentException
   *           if an error occured building the composite document
   */
  public CompositeXMLDocument(XMLDocument base, Map<String, ? extends XMLDocument> templates) throws DocumentException {
    super(base);
    this.composites = Collections.unmodifiableMap(templates);
    initializeDelegate(templates);
  }

  public CompositeXMLDocument(File file, Map<String, ? extends XMLDocument> templates)
      throws DocumentException, FileNotFoundException {
    super(file);
    initializeDelegate(templates);
  }

  public CompositeXMLDocument(URL url, Map<String, ? extends XMLDocument> templates) throws DocumentException {
    super(url);
    initializeDelegate(templates);
  }

  @Override
  public XPathEvaluator newXPathEvaluator() throws XPathFactoryConfigurationException {
    org.jdom2.Document document = getJDOMDocument(false);
    return new JDOMBasedXPathEvaluator(document.getRootElement(), getXMLContextResolver());
  }

  private void initializeDelegate(Map<String, ? extends XMLDocument> templates) throws DocumentException {
    XPathEvaluator evaluator;
    try {
      evaluator = newXPathEvaluator();
    } catch (XPathFactoryConfigurationException e) {
      throw new DocumentException("Unable to initialize the XPath evaluator for the document", e);
    }

    List<Element> results;
    try {
      results = evaluator.evaluate(
          "//*[local-name()='" + COMPOSITE_PLACEHOLDER_LOCAL_NAME + "' and namespace-uri()='" + COMPOSITE_NS_URI + "']",
          Filters.element());
    } catch (XPathExpressionException e) {
      throw new DocumentException(
          "Unable to evaluate the XPath to locate the composite placeholders: " + COMPOSITE_QNAME.toString(), e);
    }

    for (Element node : results) {
      String key = node.getAttributeValue("name");
      XMLDocument value = templates.get(key);
      if (value == null) {
        throw new RuntimeException();
      }
      // replace the resulting element with the new one
      Element newChild = (Element) value.getJDOMDocument().getRootElement().clone();
      Element parent = node.getParentElement();
      int index = parent.indexOf(node);
      parent.setContent(index, newChild);

      // record the systemId of the new element for future lookups
      elementToSystemIdMap.put(newChild, value.getSystemId());
    }
  }

  @Override
  protected XMLContextResolver getXMLContextResolver() {
    // TODO: Reuse instance?
    return new CompositeXMLContextResolver();
  }

  @Override
  public XMLDocumentFragment newXMLDocumentFragment(String xpath) throws DocumentException {
    throw new UnsupportedOperationException("Fragments must be made from non-composite XMLDocuments");
  }

  @Override
  public XMLDocumentFragment newXMLDocumentFragment(Element element) {
    throw new UnsupportedOperationException("Fragments must be made from non-composite XMLDocuments");
  }

  /**
   * Flattens the composite document into a normal JDOMDocument.
   * 
   * @param newFile
   *          the file to write the composite document to
   * @return a new document instance
   * @throws FileNotFoundException
   *           if the target file cannot be opened for writing for some reason
   * @throws IOException
   *           if an error occurs while writing to the file
   * @throws DocumentException
   *           if an error occurs while loading the newly written file
   */
  public JDOMDocument toJDOMDocument(File newFile) throws FileNotFoundException, IOException, DocumentException {
    copyTo(newFile);
    return new JDOMDocument(newFile);
  }

  @Override
  public List<SourceInfo> getSourceInfo() {
    List<SourceInfo> base = super.getSourceInfo();
    List<SourceInfo> retval = new ArrayList<SourceInfo>(composites.size() + base.size());
    retval.addAll(base);
    for (Document composite : composites.values()) {
      retval.add(new DefaultSourceInfo(composite));
    }
    return retval;
  }

  private class CompositeXMLContextResolver extends DefaultXMLContextResolver {

    public CompositeXMLContextResolver() {
      super("", getJDOMDocument(false).getRootElement(), false);
    }

    @Override
    public String getSystemId(Content element) {
      String retval = elementToSystemIdMap.get(element);
      if (retval == null) {
        Element parent = element.getParentElement();
        if (parent == null) {
          retval = super.getSystemId(element);
        } else {
          retval = getSystemId(parent);
        }
      }
      return retval;
    }

    @Override
    protected Element getParentElement(Content content) {
      Element retval;
      if (elementToSystemIdMap.containsKey(content)) {
        retval = null;
      } else {
        retval = super.getParentElement(content);
      }
      return retval;
    }
  }
}
