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
package gov.nist.decima.xml.templating.document.post.template;

import static org.junit.Assert.assertEquals;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.output.XMLOutputter;
import org.junit.Test;

import java.util.Collections;

public class DeleteActionTest {
  private static final Namespace NS_A = Namespace.getNamespace("http://foo.org/xml/test");

  @Test
  public void testProcessDeleteAttribute() throws ActionException {
    Document actual = new Document(new Element("root", NS_A));
    Element root = actual.getRootElement();
    root.setAttribute("old-attr", "test");
    Element child = new Element("child", NS_A);
    child.setAttribute("old-attr", "test");
    root.addContent(child);

    Document expected = actual.clone();
    root = expected.getRootElement();
    root.getAttribute("old-attr").detach();
    child = root.getChild("child", NS_A);
    child.getAttribute("old-attr").detach();

    Action action = new DeleteAction(AbstractActionTest.XPATH_FACTORY, "//@old-attr", Collections.emptyMap());
    action.execute(actual);
    XMLOutputter out = new XMLOutputter();
    assertEquals(out.outputString(expected), out.outputString(actual));
  }

  @Test
  public void testProcessDeleteElement() throws ActionException {
    Document actual = new Document(new Element("root", NS_A));
    Element root = actual.getRootElement();
    root.setAttribute("old-attr", "test");
    Element child = new Element("child", NS_A);
    child.setAttribute("old-attr", "test");
    root.addContent(child);

    Document expected = actual.clone();
    root = expected.getRootElement();
    child = root.getChild("child", NS_A);
    child.detach();

    Action action = new DeleteAction(AbstractActionTest.XPATH_FACTORY, "//prefix:child",
        Collections.singletonMap("prefix", NS_A.getURI()));
    action.execute(actual);
    XMLOutputter out = new XMLOutputter();
    assertEquals(out.outputString(expected), out.outputString(actual));
  }

  @Test(expected = ActionProcessingException.class)
  public void testProcessInvalid() throws ActionException {
    DeleteAction action = new DeleteAction(AbstractActionTest.XPATH_FACTORY, "//prefix:child",
        Collections.singletonMap("prefix", NS_A.getURI()));
    action.process(Collections.singletonList(new Object()));
  }
}
