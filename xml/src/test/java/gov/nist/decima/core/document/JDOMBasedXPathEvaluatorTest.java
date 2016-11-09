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

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Text;
import org.jdom2.filter.Filters;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;

import javax.xml.xpath.XPathExpressionException;

public class JDOMBasedXPathEvaluatorTest {
  private static final File DOCUMENT = new File("src/test/resources/test-document.xml");

  @Test
  public void testXPathRoot()
      throws FileNotFoundException, DocumentException, XPathExpressionException {
    JDOMDocument doc = new JDOMDocument(DOCUMENT);

    JDOMBasedXPathEvaluator eval = new JDOMBasedXPathEvaluator(doc.getJDOMDocument(false));
    XPathNamespaceContext nsContext = new XPathNamespaceContext("NS");
    nsContext.addNamespace("ns", "NS");
    eval.setNamespaceContext(nsContext);
    Element root = eval.evaluateSingle("/ns:root", Filters.element());
    Assert.assertNotNull(root);

    XPathContext context = doc.getContext(root);

    ContextAssert.assertContext("/*[local-name()='root' and namespace-uri()='NS'][1]", 2, 36,
        DOCUMENT.toURI().toString(), context);
  }

  @Test
  public void testXPathChild()
      throws FileNotFoundException, DocumentException, XPathExpressionException {
    JDOMDocument doc = new JDOMDocument(DOCUMENT);

    JDOMBasedXPathEvaluator eval = new JDOMBasedXPathEvaluator(doc.getJDOMDocument(false));
    XPathNamespaceContext nsContext = new XPathNamespaceContext("NS");
    nsContext.addNamespace("ns", "NS");
    eval.setNamespaceContext(nsContext);
    Element child = eval.evaluateSingle("/ns:root/ns:child[@id='A']", Filters.element());

    XPathContext context = doc.getContext(child);

    ContextAssert.assertContext(
        "/*[local-name()='root' and namespace-uri()='NS'][1]/*[local-name()='child' and namespace-uri()='NS'][1]",
        3, 35, DOCUMENT.toURI().toString(), context);
  }

  @Test
  public void testContextAttribute()
      throws FileNotFoundException, DocumentException, XPathExpressionException {
    JDOMDocument doc = new JDOMDocument(DOCUMENT);

    JDOMBasedXPathEvaluator eval = new JDOMBasedXPathEvaluator(doc.getJDOMDocument(false));
    XPathNamespaceContext nsContext = new XPathNamespaceContext("NS");
    nsContext.addNamespace("ns", "NS");
    eval.setNamespaceContext(nsContext);
    Attribute attr = eval.evaluateSingle("/ns:root/ns:child[@id='A']/@id", Filters.attribute());
    XPathContext context = doc.getContext(attr);

    ContextAssert.assertContext(
        "/*[local-name()='root' and namespace-uri()='NS'][1]/*[local-name()='child' and namespace-uri()='NS'][1]/@*[local-name()='id']",
        3, 35, DOCUMENT.toURI().toString(), context);
  }

  @Test
  public void testContextAttributeNS()
      throws FileNotFoundException, DocumentException, XPathExpressionException {
    JDOMDocument doc = new JDOMDocument(DOCUMENT);

    JDOMBasedXPathEvaluator eval = new JDOMBasedXPathEvaluator(doc.getJDOMDocument(false));
    XPathNamespaceContext nsContext = new XPathNamespaceContext("NS");
    nsContext.addNamespace("ns", "NS");
    nsContext.addNamespace("ns2", "otherNS");
    eval.setNamespaceContext(nsContext);
    Attribute attr
        = eval.evaluateSingle("/ns:root/ns:child[@id='A']/@ns2:test", Filters.attribute());

    XPathContext context = doc.getContext(attr);

    ContextAssert.assertContext(
        "/*[local-name()='root' and namespace-uri()='NS'][1]/*[local-name()='child' and namespace-uri()='NS'][1]/@*[local-name()='test' and namespace-uri()='otherNS']",
        3, 35, DOCUMENT.toURI().toString(), context);
  }

  @Test
  public void testContextText()
      throws FileNotFoundException, DocumentException, XPathExpressionException {
    JDOMDocument doc = new JDOMDocument(DOCUMENT);

    JDOMBasedXPathEvaluator eval = new JDOMBasedXPathEvaluator(doc.getJDOMDocument(false));
    XPathNamespaceContext nsContext = new XPathNamespaceContext("NS");
    nsContext.addNamespace("ns", "NS");
    nsContext.addNamespace("ns2", "otherNS");
    eval.setNamespaceContext(nsContext);
    Text text = eval.evaluateSingle("/ns:root/ns:child[@id='B']/text()", Filters.text());

    XPathContext context = doc.getContext(text);

    ContextAssert.assertContext(
        "/*[local-name()='root' and namespace-uri()='NS'][1]/*[local-name()='child' and namespace-uri()='NS'][2]/text()",
        4, 23, DOCUMENT.toURI().toString(), context);
  }
}
