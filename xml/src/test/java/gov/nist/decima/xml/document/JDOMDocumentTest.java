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
import gov.nist.decima.xml.document.JDOMDocument;
import gov.nist.decima.xml.document.XMLDocumentFragment;
import gov.nist.decima.xml.document.XPathContext;

import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class JDOMDocumentTest {

  private static final File DOCUMENT = new File("src/test/resources/test-document.xml");

  @Test
  public void testContextRoot() throws FileNotFoundException, DocumentException {
    JDOMDocument doc = new JDOMDocument(DOCUMENT);
    Element root = doc.getJDOMDocument().getRootElement();
    XPathContext context = doc.getContext(root);

    ContextAssert.assertContext("/*[local-name()='root' and namespace-uri()='NS'][1]", 2, 36,
        DOCUMENT.toURI().toString(), context);
  }

  @Test
  public void testContextChild() throws FileNotFoundException, DocumentException {
    JDOMDocument doc = new JDOMDocument(DOCUMENT);
    Element root = doc.getJDOMDocument().getRootElement();
    Element child = root.getChildren().get(0);
    XPathContext context = doc.getContext(child);

    ContextAssert.assertContext(
        "/*[local-name()='root' and namespace-uri()='NS'][1]/*[local-name()='child' and namespace-uri()='NS'][1]", 3,
        33, DOCUMENT.toURI().toString(), context);
  }

  @Test
  public void testContextAttribute() throws FileNotFoundException, DocumentException {
    JDOMDocument doc = new JDOMDocument(DOCUMENT);
    Element root = doc.getJDOMDocument().getRootElement();
    Element child = root.getChildren().get(0);
    Attribute attr = child.getAttribute("id");
    XPathContext context = doc.getContext(attr);

    ContextAssert.assertContext(
        "/*[local-name()='root' and namespace-uri()='NS'][1]/*[local-name()='child' and namespace-uri()='NS'][1]/@*[local-name()='id']",
        3, 33, DOCUMENT.toURI().toString(), context);
  }

  @Test
  public void testContextAttributeNS() throws FileNotFoundException, DocumentException {
    JDOMDocument doc = new JDOMDocument(DOCUMENT);
    Element root = doc.getJDOMDocument().getRootElement();
    Element child = root.getChildren().get(0);
    Attribute attr = child.getAttribute("test", Namespace.getNamespace("otherNS"));
    XPathContext context = doc.getContext(attr);

    ContextAssert.assertContext(
        "/*[local-name()='root' and namespace-uri()='NS'][1]/*[local-name()='child' and namespace-uri()='NS'][1]/@*[local-name()='test' and namespace-uri()='otherNS']",
        3, 33, DOCUMENT.toURI().toString(), context);
  }

  @Test
  public void testContextText() throws FileNotFoundException, DocumentException {
    JDOMDocument doc = new JDOMDocument(DOCUMENT);
    Element root = doc.getJDOMDocument().getRootElement();
    Element child = root.getChildren().get(1);
    Content text = child.getContent(0);
    XPathContext context = doc.getContext(text);

    ContextAssert.assertContext(
        "/*[local-name()='root' and namespace-uri()='NS'][1]/*[local-name()='child' and namespace-uri()='NS'][2]/text()",
        4, 20, DOCUMENT.toURI().toString(), context);
  }

  @Test
  public void testNewFragment() throws DocumentException, MalformedURLException, URISyntaxException {
    URL url = new URL("classpath:templates/composite.xml");
    JDOMDocument doc = new JDOMDocument(url);
    XMLDocumentFragment fragment = doc.newXMLDocumentFragment("//*:test1");

    XPathContext context = fragment.getContext(fragment.getElement());

    ContextAssert.assertContext(
        "/*[local-name()='test' and namespace-uri()='http://decima.nist.gov/xml/test'][1]/*[local-name()='test1' and namespace-uri()='http://decima.nist.gov/xml/test'][1]",
        3, 9, url.toURI().toString(), context);
  }

  @Test
  public void testNewFragmentAtRoot() throws DocumentException, MalformedURLException, URISyntaxException {
    URL url = new URL("classpath:templates/composite.xml");
    JDOMDocument doc = new JDOMDocument(url);
    XMLDocumentFragment fragment = doc.newXMLDocumentFragment("/*");

    XPathContext context = fragment.getContext(fragment.getElement());

    ContextAssert.assertContext("/*[local-name()='test' and namespace-uri()='http://decima.nist.gov/xml/test'][1]", 2,
        96, url.toURI().toString(), context);
  }

}
