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

package gov.nist.decima.xml.assessment.schema;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public abstract class AbstractDelegatingContentHandler implements ContentHandler {
  private final ContentHandler delegate;

  public AbstractDelegatingContentHandler(ContentHandler delegate) {
    this.delegate = delegate;
  }

  public ContentHandler getDelegate() {
    return delegate;
  }

  @Override
  public void setDocumentLocator(Locator locator) {
    getDelegate().setDocumentLocator(locator);
  }

  @Override
  public void startDocument() throws SAXException {
    getDelegate().startDocument();
  }

  @Override
  public void endDocument() throws SAXException {
    getDelegate().endDocument();
  }

  @Override
  public void startPrefixMapping(String prefix, String uri) throws SAXException {
    getDelegate().startPrefixMapping(prefix, uri);
  }

  @Override
  public void endPrefixMapping(String prefix) throws SAXException {
    getDelegate().endPrefixMapping(prefix);
  }

  @Override
  public void startElement(String uri, String localName, String qname, Attributes attrs)
      throws SAXException {
    getDelegate().startElement(uri, localName, qname, attrs);
  }

  @Override
  public void endElement(String uri, String localName, String qname) throws SAXException {
    getDelegate().endElement(uri, localName, qname);
  }

  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {
    getDelegate().characters(ch, start, length);
  }

  @Override
  public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
    getDelegate().ignorableWhitespace(ch, start, length);
  }

  @Override
  public void processingInstruction(String target, String data) throws SAXException {
    getDelegate().processingInstruction(target, data);
  }

  @Override
  public void skippedEntity(String name) throws SAXException {
    getDelegate().skippedEntity(name);
  }
}
