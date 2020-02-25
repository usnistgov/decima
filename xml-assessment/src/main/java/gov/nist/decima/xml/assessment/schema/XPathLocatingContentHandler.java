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

package gov.nist.decima.xml.assessment.schema;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.xml.namespace.QName;

public class XPathLocatingContentHandler implements ContentHandler, SAXLocationXPathResolver {
  private final Deque<XPathContext> elementStack = new LinkedList<>();
  private Locator locator;

  public XPathLocatingContentHandler() {
  }

  public void reset() {
    elementStack.clear();
  }

  @Override
  public void startElement(String uri, String localName, String qname, Attributes attrs) throws SAXException {
    XPathContext currentContext = getCurrentNode();

    QName childQName = new QName(uri, localName);
    XPathContext childContext;
    if (currentContext == null) {
      childContext = new XPathContext(childQName, null, "");
    } else {
      childContext
          = new XPathContext(childQName, currentContext.getNewNodeIndex(childQName), currentContext.getXPath());
    }

    elementStack.push(childContext);
  }

  @Override
  public void endElement(String uri, String localName, String qname) throws SAXException {
    elementStack.pop();
  }

  @Override
  public String getCurrentXPath() {
    XPathContext currentNode = getCurrentNode();
    String retval;
    if (currentNode == null) {
      retval = "/";
    } else {
      retval = currentNode.getXPath();
    }
    return retval;
  }

  protected XPathContext getCurrentNode() {
    return elementStack.peek();
  }

  protected Locator getLocator() {
    return locator;
  }

  public void setLocator(Locator locator) {
    this.locator = locator;
  }

  @Override
  public void setDocumentLocator(Locator locator) {
    this.locator = locator;
  }

  @Override
  public void startDocument() throws SAXException {
    // do nothing
  }

  @Override
  public void endDocument() throws SAXException {
    // do nothing
  }

  @Override
  public void startPrefixMapping(String prefix, String uri) throws SAXException {
    // do nothing
  }

  @Override
  public void endPrefixMapping(String prefix) throws SAXException {
    // do nothing
  }

  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {
    // do nothing
  }

  @Override
  public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
    // do nothing
  }

  @Override
  public void processingInstruction(String target, String data) throws SAXException {
    // do nothing
  }

  @Override
  public void skippedEntity(String name) throws SAXException {
    // do nothing
  }

  private static class XPathContext {
    private Map<QName, Integer> elementCounts = new HashMap<>();
    private String xpath;

    public XPathContext(QName qname, Integer index, String parentXPath) {
      this.xpath = buildXPath(parentXPath, qname, index);
    }

    private String buildXPath(String parentXPath, QName qname, Integer index) {
      StringBuilder builder = new StringBuilder();
      // should always be non-null
      builder.append(parentXPath);

      builder.append("/*[local-name()='");
      builder.append(qname.getLocalPart());
      builder.append("' and namespace-uri()='");
      builder.append(qname.getNamespaceURI());
      builder.append("']");

      // will be null for the root element
      if (index != null) {
        builder.append("[");
        builder.append(index);
        builder.append("]");
      }
      return builder.toString();
    }

    public String getXPath() {
      return xpath;
    }

    public Integer getNewNodeIndex(QName childQName) {
      Integer value = elementCounts.get(childQName);
      if (value == null) {
        value = 1;
      } else {
        ++value;
      }
      elementCounts.put(childQName, value);
      return value;
    }

  }
}
