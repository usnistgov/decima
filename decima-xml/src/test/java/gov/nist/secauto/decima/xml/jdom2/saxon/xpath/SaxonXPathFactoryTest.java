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

package gov.nist.secauto.decima.xml.jdom2.saxon.xpath;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class SaxonXPathFactoryTest {

  @Test
  public void test() {
    Namespace ns = Namespace.getNamespace("ns", "http://test.com/ns/test1");
    Element root = new Element("root", ns);
    Document document = new Document(root);

    Element child1 = new Element("child1", ns);
    Attribute xmlBase = new Attribute("base", "/base", Namespace.XML_NAMESPACE);
    child1.setAttribute(xmlBase);
    root.addContent(child1);

    SaxonXPathFactory factory = new SaxonXPathFactory();

    XPathExpression<Element> elementExpression
        = factory.compile("//ns:*[@xml:base]", Filters.element(), Collections.emptyMap(), Namespace.XML_NAMESPACE, ns);
    List<Element> result = elementExpression.evaluate(document);
    Assert.assertEquals(Collections.singletonList(child1), result);

    XPathExpression<Attribute> attributeExpression = factory.compile("//@xml:base[$test='test-var']",
        Filters.attribute(), Collections.singletonMap("test", "test-var"), Namespace.XML_NAMESPACE, ns);
    List<Attribute> attributeResult = attributeExpression.evaluate(document);
    Assert.assertEquals(Collections.singletonList(xmlBase), attributeResult);
  }

}
