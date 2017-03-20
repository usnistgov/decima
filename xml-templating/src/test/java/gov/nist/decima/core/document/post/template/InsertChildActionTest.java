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

package gov.nist.decima.core.document.post.template;

import static org.junit.Assert.assertEquals;

import org.hamcrest.core.IsInstanceOf;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.Text;
import org.jdom2.output.XMLOutputter;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class InsertChildActionTest {
  private static final Namespace NS_A = Namespace.getNamespace("prefix", "http://foo.org/xml/test");

  @Test
  public void testNullIndex() throws ActionException {
    Document actual = new Document(new Element("root", NS_A));
    Element root = actual.getRootElement();
    root.setAttribute("attr", "test");
    Element child = new Element("child", NS_A);
    child.setAttribute("attr", "test");
    root.addContent(child);

    Document expected = actual.clone();
    root = expected.getRootElement();
    root.addContent(new Element("new-child1", NS_A));
    root.addContent(new Element("new-child2", NS_A));

    List<Element> newNodes = new LinkedList<>();
    newNodes.add(new Element("new-child1", NS_A));
    newNodes.add(new Element("new-child2", NS_A));
    Action action = new InsertChildAction(AbstractActionTest.XPATH_FACTORY, "/prefix:root",
        Collections.singletonMap(NS_A.getPrefix(), NS_A.getURI()), Collections.unmodifiableList(newNodes), null);
    action.execute(actual);
    XMLOutputter out = new XMLOutputter();
    assertEquals(out.outputString(expected), out.outputString(actual));
  }

  @Test
  public void testZeroIndex() throws ActionException {
    Document actual = new Document(new Element("root", NS_A));
    Element root = actual.getRootElement();
    root.setAttribute("attr", "test");
    Element child = new Element("child", NS_A);
    child.setAttribute("attr", "test");
    root.addContent(child);

    Document expected = actual.clone();
    root = expected.getRootElement();
    root.addContent(0, new Element("new-child1", NS_A));
    root.addContent(1, new Element("new-child2", NS_A));

    List<Element> newNodes = new LinkedList<>();
    newNodes.add(new Element("new-child1", NS_A));
    newNodes.add(new Element("new-child2", NS_A));
    Action action = new InsertChildAction(AbstractActionTest.XPATH_FACTORY, "/prefix:root",
        Collections.singletonMap(NS_A.getPrefix(), NS_A.getURI()), Collections.unmodifiableList(newNodes), 0);
    action.execute(actual);
    XMLOutputter out = new XMLOutputter();
    assertEquals(out.outputString(expected), out.outputString(actual));
  }

  @Test
  public void testMiddleIndexWhitespace() throws ActionException {
    Document actual = new Document(new Element("root", NS_A));
    Element root = actual.getRootElement();
    root.setAttribute("attr", "test");
    root.addContent(new Text("\n"));
    Element child = new Element("old-child1", NS_A);
    child.setAttribute("attr", "test");
    root.addContent(child);
    root.addContent(new Text("\n"));
    root.addContent(new Element("old-child2", NS_A));

    Document expected = actual.clone();
    root = expected.getRootElement();
    root.addContent(3, new Element("new-child1", NS_A));
    root.addContent(4, new Element("new-child2", NS_A));

    List<Element> newNodes = new LinkedList<>();
    newNodes.add(new Element("new-child1", NS_A));
    newNodes.add(new Element("new-child2", NS_A));
    Action action = new InsertChildAction(AbstractActionTest.XPATH_FACTORY, "/prefix:root",
        Collections.singletonMap(NS_A.getPrefix(), NS_A.getURI()), Collections.unmodifiableList(newNodes), 1);
    action.execute(actual);
    XMLOutputter out = new XMLOutputter();
    assertEquals(out.outputString(expected), out.outputString(actual));
  }

  @Test
  public void testMiddleIndex() throws ActionException {
    Document actual = new Document(new Element("root", NS_A));
    Element root = actual.getRootElement();
    root.setAttribute("attr", "test");
    Element child = new Element("old-child1", NS_A);
    child.setAttribute("attr", "test");
    root.addContent(child);
    root.addContent(new Element("old-child2", NS_A));

    Document expected = actual.clone();
    root = expected.getRootElement();
    root.addContent(1, new Element("new-child1", NS_A));
    root.addContent(2, new Element("new-child2", NS_A));

    List<Element> newNodes = new LinkedList<>();
    newNodes.add(new Element("new-child1", NS_A));
    newNodes.add(new Element("new-child2", NS_A));
    Action action = new InsertChildAction(AbstractActionTest.XPATH_FACTORY, "/prefix:root",
        Collections.singletonMap(NS_A.getPrefix(), NS_A.getURI()), Collections.unmodifiableList(newNodes), 1);
    action.execute(actual);
    XMLOutputter out = new XMLOutputter();
    assertEquals(out.outputString(expected), out.outputString(actual));
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void testInvalidNegativeIndex() throws ActionException {
    new InsertChildAction(AbstractActionTest.XPATH_FACTORY, "/prefix:root",
        Collections.singletonMap(NS_A.getPrefix(), NS_A.getURI()),
        Collections.singletonList(new Element("new-child1", NS_A)), -1);
  }

  @Rule
  public ExpectedException expected = ExpectedException.none();

  @Test
  public void testInvalidPositiveIndex() throws ActionException {
    Document actual = new Document(new Element("root", NS_A));
    Element root = actual.getRootElement();
    root.setAttribute("attr", "test");
    Element child = new Element("child", NS_A);
    child.setAttribute("attr", "test");
    root.addContent(child);

    List<Element> newNodes = new LinkedList<>();
    newNodes.add(new Element("new-child1", NS_A));
    Action action = new InsertChildAction(AbstractActionTest.XPATH_FACTORY, "/prefix:root",
        Collections.singletonMap(NS_A.getPrefix(), NS_A.getURI()), Collections.unmodifiableList(newNodes), 2);

    expected.expect(ActionProcessingException.class);
    expected.expectCause(IsInstanceOf.instanceOf(IndexOutOfBoundsException.class));
    ;
    action.execute(actual);
  }

  @Test
  public void testAfterLastIndex() throws ActionException {
    Document actual = new Document(new Element("root", NS_A));
    Element root = actual.getRootElement();
    root.setAttribute("attr", "test");
    Element child = new Element("child", NS_A);
    child.setAttribute("attr", "test");
    root.addContent(child);

    List<Element> newNodes = new LinkedList<>();
    newNodes.add(new Element("new-child1", NS_A));
    newNodes.add(new Element("new-child2", NS_A));
    Action action = new InsertChildAction(AbstractActionTest.XPATH_FACTORY, "/prefix:root",
        Collections.singletonMap(NS_A.getPrefix(), NS_A.getURI()), Collections.unmodifiableList(newNodes), 1);

    expected.expect(ActionProcessingException.class);
    expected.expectCause(IsInstanceOf.instanceOf(IllegalArgumentException.class));
    ;
    action.execute(actual);
  }

}
