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

import gov.nist.decima.core.document.context.XMLContextResolver;
import gov.nist.decima.core.jdom2.JDOMUtil;

import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.transform.JDOMSource;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathFactoryConfigurationException;

public abstract class AbstractJDOMDocument implements MutableXMLDocument  {

  public AbstractJDOMDocument() {
  }

  protected abstract XMLContextResolver getXMLContextResolver();

  @Override
  public String getSystemId() {
    return getJDOMDocument(false).getBaseURI();
  }

  @Override
  public String getSystemId(Content element) {
    return getXMLContextResolver().getSystemId(element);
  }

  @Override
  public Document getJDOMDocument() {
    return getJDOMDocument(false);
  }

  @Override
  public Source getSource() {
    return new JDOMSource(getJDOMDocument(false));
  }

  @Override
  public XPathEvaluator newXPathEvaluator() throws XPathFactoryConfigurationException {
    org.jdom2.Document document = getJDOMDocument();
    return new JDOMBasedXPathEvaluator(document);
  }

  @Override
  public InputStream newInputStream() {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Source xmlSource = getSource();
    Result outputTarget = new StreamResult(outputStream);
    try {
      TransformerFactory.newInstance().newTransformer().transform(xmlSource, outputTarget);
    } catch (TransformerException | TransformerFactoryConfigurationError e) {
      throw new RuntimeException(e);
    }
    return new ByteArrayInputStream(outputStream.toByteArray());
  }

  @Override
  public void copyTo(File file) throws FileNotFoundException, IOException {
    XMLOutputter out = new XMLOutputter();
    try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
      out.output(getJDOMDocument(false), os);
    }
  }

  @Override
  public XMLDocumentFragment newXMLDocumentFragment(String xpath) throws DocumentException {
    return new XMLDocumentFragment(this, xpath);
  }

  @Override
  public XMLDocumentFragment newXMLDocumentFragment(Element element) {
    return new XMLDocumentFragment(this, element);
  }

  @Override
  public String asString(Format format) {
    return JDOMUtil.toString(getJDOMDocument(false), format);
  }

  @Override
  public XPathContext getContext(Content content) {
    return getXMLContextResolver().getContext(content);
  }

  @Override
  public XPathContext getContext(Attribute attribute) {
    return getXMLContextResolver().getContext(attribute);
  }

  @Override
  public String getXPath(Content content) {
    return getXMLContextResolver().getXPath(content);
  }

  @Override
  public String getXPath(Attribute attribute) {
    return getXMLContextResolver().getXPath(attribute);
  }

  @Override
  public List<SourceInfo> getSourceInfo() {
      List<SourceInfo> retval = Collections.singletonList(new DefaultSourceInfo(this));
      return retval;
  }

}
