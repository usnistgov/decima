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
import gov.nist.decima.xml.assessment.result.XPathContext;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactoryConfigurationException;

public class CompositeJDOMDocumentTest {
    private static final Namespace COMPOSITE_NS = Namespace.getNamespace("http://decima.nist.gov/xml/test");
    private static final Namespace TEMPLATE_A_NS = Namespace.getNamespace("templateA");
    private static final Namespace TEMPLATE_B_NS = Namespace.getNamespace("templateB");

    protected CompositeXMLDocument buildComposite() throws DocumentException, MalformedURLException {
        XMLDocument sub1 = new JDOMDocument(new URL("classpath:templates/templateA.xml"));
        XMLDocument sub2 = new JDOMDocument(new URL("classpath:templates/templateB.xml"));
        Map<String, XMLDocument> subs = new HashMap<>();
        subs.put("sub1", sub1);
        subs.put("sub2", sub2);

        return new CompositeXMLDocument(new URL("classpath:templates/composite.xml"), subs);
    }

    @Test
    public void testXPathRoot() throws DocumentException, MalformedURLException, XPathExpressionException,
            XPathFactoryConfigurationException {
        CompositeXMLDocument base = buildComposite();

        XPathEvaluator eval = base.newXPathEvaluator();
        // JDOMBasedXPathEvaluator eval = new JDOMBasedXPathEvaluator(base.getJDOMDocument(false));
        XPathNamespaceContext nsContext = new XPathNamespaceContext("http://decima.nist.gov/xml/test");
        nsContext.addNamespace("t", "http://decima.nist.gov/xml/test");
        eval.setNamespaceContext(nsContext);
        Element root = eval.evaluateSingle("/t:test", Filters.element());
        Assert.assertNotNull("XPath did not match", root);

        XPathContext context = base.getContext(root);

        ContextAssert.assertContext("/*[local-name()='test' and namespace-uri()='http://decima.nist.gov/xml/test'][1]",
                2, 96, "classpath:templates/composite.xml", context);
    }

    @Test
    public void testXPathTemplateA() throws DocumentException, MalformedURLException, XPathExpressionException {
        CompositeXMLDocument base = buildComposite();

        JDOMBasedXPathEvaluator eval = new JDOMBasedXPathEvaluator(base.getJDOMDocument(false));
        XPathNamespaceContext nsContext = new XPathNamespaceContext("http://decima.nist.gov/xml/test");
        nsContext.addNamespace("a", "templateA");
        eval.setNamespaceContext(nsContext);
        List<Element> elements = eval.evaluate("//a:templateA", Filters.element());
        Assert.assertEquals("XPath should have 2 result elements", 2, elements.size());

        Element element = elements.get(0);

        // relative to the templateA
        ContextAssert.assertContext("/*[local-name()='templateA' and namespace-uri()='templateA'][1]", 2, 30,
                "classpath:templates/templateA.xml", base.getContext(element));

        element = elements.get(1);

        // relative to the templateA
        ContextAssert.assertContext("/*[local-name()='templateA' and namespace-uri()='templateA'][1]", 2, 30,
                "classpath:templates/templateA.xml", base.getContext(element));
    }

    @Test
    public void test() throws DocumentException, IOException {
        CompositeXMLDocument base = buildComposite();
        Document compiledDocument = base.getJDOMDocument();
        Element root = compiledDocument.getRootElement();
        // System.err.println(JDOMUtil.toString(root));
        assertElement("test", COMPOSITE_NS, root);

        // Assert composite test1 contains sub1
        List<Element> elements = root.getContent(Filters.element());
        Element child = elements.get(0);
        assertElement("test1", COMPOSITE_NS, child);
        Element sub = child.getChild("templateA", TEMPLATE_A_NS);
        assertElement("templateA", TEMPLATE_A_NS, sub);

        // Assert composite test2 contains sub2
        child = elements.get(1);
        assertElement("test2", COMPOSITE_NS, child);
        sub = child.getChild("templateB", TEMPLATE_B_NS);
        assertElement("templateB", TEMPLATE_B_NS, sub);

        // Assert composite test3 contains sub1
        child = elements.get(2);
        assertElement("test3", COMPOSITE_NS, child);
        sub = child.getChild("templateA", TEMPLATE_A_NS);
        assertElement("templateA", TEMPLATE_A_NS, sub);
    }

    private static void assertElement(String localname, Namespace ns, Element element) {
        org.junit.Assert.assertEquals(localname, element.getName());
        org.junit.Assert.assertTrue(ns.equals(element.getNamespace()));
    }

    // @Test
    // public void testNewFragment() throws DocumentException, MalformedURLException,
    // URISyntaxException {
    // CompositeXMLDocument base = buildComposite();
    //
    // XMLDocumentFragment fragment = base.newXMLDocumentFragment("/*:test/*:test1/*:templateA");
    //
    // XPathContext context = fragment.getContext(fragment.getElement());
    //
    // // relative to the templateA
    // ContextAssert.assertContext(
    // "/*[local-name()='templateA' and namespace-uri()='templateA'][1]",
    // 2, 30, "classpath:templates/templateA.xml", context);
    // }

}
