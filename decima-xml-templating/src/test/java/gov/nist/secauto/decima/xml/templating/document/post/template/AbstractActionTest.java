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

import gov.nist.secauto.decima.xml.jdom2.NamespaceUtil;
import gov.nist.secauto.decima.xml.jdom2.saxon.xpath.SaxonXPathFactory;
import gov.nist.secauto.decima.xml.templating.document.post.template.AbstractXPathAction;
import gov.nist.secauto.decima.xml.templating.document.post.template.ActionException;
import gov.nist.secauto.decima.xml.templating.document.post.template.InvalidXPathActionException;
import gov.nist.secauto.decima.xml.templating.document.post.template.NoXPathResultsActionException;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filter;
import org.jdom2.filter.Filters;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AbstractActionTest {

  private static final Namespace NS_A = Namespace.getNamespace("http://foo.org/xml/test");
  private static final Document SOURCE;
  static {
    SOURCE = new Document(new Element("test", NS_A));
    Element root = SOURCE.getRootElement();
    {
      Element child = new Element("childA", NS_A);
      root.addContent(child);
    }
    {
      Element child = new Element("childB", NS_A);
      child.setAttribute("attrA", "valueA");
      child.setAttribute("attrB", "valueB");
      root.addContent(child);
    }
    {
      Element child = new Element("childA", NS_A);
      child.setAttribute("attrA", "valueA");
      root.addContent(child);
    }
    try {
      new XMLOutputter().output(SOURCE, System.out);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  public static final XPathFactory XPATH_FACTORY = new SaxonXPathFactory();

  @Test(expected = NullPointerException.class)
  public void testNullXPath() {
    new StubAbstractAction<Object>(XPATH_FACTORY, null, Filters.fpassthrough(), Collections.emptyMap());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyXPath() {
    new StubAbstractAction<Object>(XPATH_FACTORY, "", Filters.fpassthrough(), Collections.emptyMap());
  }

  @Test(expected = NullPointerException.class)
  public void testNullFilter() {
    new StubAbstractAction<Object>(XPATH_FACTORY, "//node", null, Collections.emptyMap());
  }

  @Test(expected = NullPointerException.class)
  public void testNullPrefixMap() {
    new StubAbstractAction<Object>(XPATH_FACTORY, "//node", Filters.fpassthrough(), null);
  }

  @Test
  public void testValidPrefix() {
    new StubAbstractAction<Object>(XPATH_FACTORY, "//prefix:node", Filters.fpassthrough(),
        Collections.singletonMap("prefix", "http://foo.org/schema"));
  }

  @Test
  public void testExecuteInvalidPrefix() throws ActionException {
    Document doc = new Document(new Element("test", "http://foo.org/xml/test"));
    Element root = doc.getRootElement();
    AbstractXPathAction<Object> action = new StubAbstractAction<Object>(XPATH_FACTORY, "//prefix:node",
        Filters.fpassthrough(), NamespaceUtil.getPrefixToNamespaceMap(root));
    try {
      action.execute(doc);
    } catch (InvalidXPathActionException e) {
      // expected
    } catch (ActionException e) {
      Assert.fail("Unexpected exception");
      throw e;
    }
  }

  @Test
  public void testGetXpath() {
    String xpath = "//prefix:node";
    Filter<Object> filter = Filters.fpassthrough();
    Map<String, String> map = Collections.singletonMap("prefix", "http://foo.org/schema");
    AbstractXPathAction<Object> action = new StubAbstractAction<Object>(XPATH_FACTORY, xpath, filter, map);
    XPathExpression<Object> xpathExp = action.getXpath();
    assertEquals(xpath, xpathExp.getExpression());
    assertEquals(filter, xpathExp.getFilter());
    assertEquals(2, xpathExp.getNamespaces().length);
    assertEquals("prefix", xpathExp.getNamespaces()[1].getPrefix());
    assertEquals("http://foo.org/schema", xpathExp.getNamespaces()[1].getURI());
  }

  @Test(expected = NoXPathResultsActionException.class)
  public void testExecuteEmptyResults() throws ActionException {
    StubAbstractAction<Element> action = new StubAbstractAction<Element>(XPATH_FACTORY, "//prefix:child",
        Filters.element(), Collections.singletonMap("prefix", "http://foo.org/xml/test"));
    action.execute(SOURCE);
  }

  @Test
  public void testExecuteElement() throws ActionException {
    StubAbstractAction<Element> action = new StubAbstractAction<Element>(XPATH_FACTORY, "//prefix:childA",
        Filters.element(), Collections.singletonMap("prefix", "http://foo.org/xml/test"));
    action.execute(SOURCE);
    assertEquals(2, action.getResults().size());
  }

  @Test
  public void testExecuteAttribute() throws ActionException {
    StubAbstractAction<Attribute> action = new StubAbstractAction<Attribute>(XPATH_FACTORY, "//@attrB",
        Filters.attribute(), Collections.singletonMap("prefix", "http://foo.org/xml/test"));
    action.execute(SOURCE);
    assertEquals(1, action.getResults().size());
  }

  private static class StubAbstractAction<T> extends AbstractXPathAction<T> {
    private List<T> results;

    public StubAbstractAction(XPathFactory xpathFactory, String xpath, Filter<T> filter,
        Map<String, String> prefixToNamespaceMap) {
      super(xpathFactory, xpath, filter, prefixToNamespaceMap);
    }

    @Override
    protected void process(List<T> results) throws ActionException {
      this.results = results;
    }

    public List<T> getResults() {
      return results;
    }
  }
}
