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

package gov.nist.secauto.decima.xml.templating.document.post.template;

import static org.junit.Assert.assertEquals;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.output.XMLOutputter;
import org.junit.Test;

import java.util.Collections;

public class AddAttributeActionTest {
  private static final Namespace NS_A = Namespace.getNamespace("prefix", "http://foo.org/xml/test");

  @Test
  public void testProcessMultipleElementsNoAttributeNamespace() throws ActionException {
    Document actual = new Document(new Element("root", NS_A));
    Element root = actual.getRootElement();
    root.setAttribute("old-attr", "test");
    Element child = new Element("child", NS_A);
    child.setAttribute("old-attr", "test");
    root.addContent(child);

    Document expected = actual.clone();
    root = expected.getRootElement();
    root.setAttribute("new-attr", "test2");
    child = root.getChild("child", NS_A);
    child.setAttribute("new-attr", "test2");

    Action action = new AddAttributeAction(AbstractActionTest.XPATH_FACTORY, "//*[@old-attr='test']",
        Collections.emptyMap(), null, "new-attr", "test2");
    action.execute(actual);
    XMLOutputter out = new XMLOutputter();
    assertEquals(out.outputString(expected), out.outputString(actual));
  }

  @Test
  public void testProcessMultipleElements() throws ActionException {
    Document actual = new Document(new Element("root", NS_A));
    Element root = actual.getRootElement();
    root.setAttribute("old-attr", "test");
    Element child = new Element("child", NS_A);
    child.setAttribute("old-attr", "test");
    root.addContent(child);

    Document expected = actual.clone();
    root = expected.getRootElement();
    root.setAttribute("new-attr", "test2", NS_A);
    child = root.getChild("child", NS_A);
    child.setAttribute("new-attr", "test2", NS_A);

    Action action = new AddAttributeAction(AbstractActionTest.XPATH_FACTORY, "//*[@old-attr='test']",
        Collections.emptyMap(), NS_A.getURI(), "new-attr", "test2");
    action.execute(actual);
    XMLOutputter out = new XMLOutputter();
    assertEquals(out.outputString(expected), out.outputString(actual));
  }

  @Test
  public void testAddNSAttributeWithDefaultDocumentNamespace() throws ActionException {
    Namespace ns = Namespace.getNamespace(NS_A.getURI());
    Document actual = new Document(new Element("root", ns));
    Element root = actual.getRootElement();
    root.setAttribute("old-attr1", "test");
    Element child = new Element("child", ns);
    child.setAttribute("old-attr2", "test");
    root.addContent(child);

    Document expected = actual.clone();
    root = expected.getRootElement();
    child = root.getChild("child", ns);
    child.setAttribute("new-attr", "test2", Namespace.getNamespace("ns1", "http://foo.org/xml/test"));

    Action action = new AddAttributeAction(AbstractActionTest.XPATH_FACTORY, "//*[@old-attr2='test']",
        Collections.emptyMap(), NS_A.getURI(), "new-attr", "test2");
    action.execute(actual);
    XMLOutputter out = new XMLOutputter();
    assertEquals(out.outputString(expected), out.outputString(actual));
  }

}
