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

package gov.nist.decima.xml.document;

import gov.nist.decima.core.document.DocumentException;
import gov.nist.decima.xml.document.JDOMDocument;
import gov.nist.decima.xml.document.XMLDocumentFragment;
import gov.nist.decima.xml.document.XPathContext;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.located.LocatedElement;
import org.junit.Test;

import java.io.FileNotFoundException;

import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactoryConfigurationException;

public class XMLDocumentFragmentTest {
  private static final Namespace NS = Namespace.getNamespace("nsA");

  private static Document newDocument() {
    LocatedElement root = new LocatedElement("root", NS);
    root.setLine(1);
    root.setColumn(1);
    Document retval = new Document(root);

    LocatedElement child = new LocatedElement("child1", NS);
    child.setLine(2);
    child.setColumn(2);
    child.setAttribute("attr1", "value1");
    root.addContent(child);

    child = new LocatedElement("child2", NS);
    child.setLine(3);
    child.setColumn(3);
    child.setAttribute("attr2", "value2");
    root.addContent(child);

    return retval;
  }

  @Test
  public void testFragmentRoot()
      throws FileNotFoundException, DocumentException, XPathExpressionException, XPathFactoryConfigurationException {
    JDOMDocument document = new JDOMDocument(newDocument(), null);

    Element element = document.newXPathEvaluator()
        .evaluateSingle("/*[local-name()='root' and namespace-uri()='" + NS.getURI() + "'][1]", Filters.element());

    XMLDocumentFragment fragment = document.newXMLDocumentFragment(element);
    XPathContext context = fragment.newXPathEvaluator().getContext("/*[local-name()='root']/*[local-name()='child2']");
    ContextAssert.assertContext("/*[local-name()='root' and namespace-uri()='" + NS.getURI()
        + "'][1]/*[local-name()='child2' and namespace-uri()='" + NS.getURI() + "'][1]", 3, 3, null, context);
  }

  @Test
  public void testFragmentChild()
      throws FileNotFoundException, DocumentException, XPathExpressionException, XPathFactoryConfigurationException {
    JDOMDocument document = new JDOMDocument(newDocument(), null);

    Element element
        = document.newXPathEvaluator().evaluateSingle("/*[local-name()='root' and namespace-uri()='" + NS.getURI()
            + "'][1]/*[local-name()='child2' and namespace-uri()='" + NS.getURI() + "'][1]", Filters.element());

    XMLDocumentFragment fragment = document.newXMLDocumentFragment(element);
    XPathContext context = fragment.newXPathEvaluator().getContext("/*[local-name()='child2']");
    System.out.println(context.getXPath());
    ContextAssert.assertContext("/*[local-name()='root' and namespace-uri()='" + NS.getURI()
        + "'][1]/*[local-name()='child2' and namespace-uri()='" + NS.getURI() + "'][1]", 3, 3, null, context);
  }
}
