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

import gov.nist.decima.xml.templating.document.post.template.Action;
import gov.nist.decima.xml.templating.document.post.template.ActionException;
import gov.nist.decima.xml.templating.document.post.template.ActionProcessingException;
import gov.nist.decima.xml.templating.document.post.template.ReplaceAction;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.output.XMLOutputter;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ReplaceActionTest {
    private static final Namespace NS_A = Namespace.getNamespace("prefix", "http://foo.org/xml/test");

    @Test
    public void testReplace() throws ActionException {
        Document actual = new Document(new Element("root", NS_A));
        Element root = actual.getRootElement();
        root.setAttribute("attr", "test");
        Element child = new Element("child", NS_A);
        child.setAttribute("attr", "test1");
        root.addContent(child);
        child = new Element("child2", NS_A);
        child.setAttribute("attr", "test1");
        root.addContent(child);

        List<Element> newNodes;

        Document expected = actual.clone();
        root = expected.getRootElement();
        // replace first child
        newNodes = new LinkedList<>();
        newNodes.add(new Element("new-child1", NS_A));
        newNodes.add(new Element("new-child2", NS_A));
        root.setContent(0, newNodes);
        // replace second child
        newNodes = new LinkedList<>();
        newNodes.add(new Element("new-child1", NS_A));
        newNodes.add(new Element("new-child2", NS_A));
        root.setContent(2, newNodes);

        // setup nodes for the ReplaceAction
        newNodes = new LinkedList<>();
        newNodes.add(new Element("new-child1", NS_A));
        newNodes.add(new Element("new-child2", NS_A));

        Action action = new ReplaceAction(AbstractActionTest.XPATH_FACTORY, "//*[@attr='test1']",
                Collections.emptyMap(), Collections.unmodifiableList(newNodes));
        action.execute(actual);
        XMLOutputter out = new XMLOutputter();
        assertEquals(out.outputString(expected), out.outputString(actual));
    }

    @Test(expected = ActionProcessingException.class)
    public void testReplaceRoot() throws ActionException {
        Document actual = new Document(new Element("root", NS_A));
        Element root = actual.getRootElement();
        root.setAttribute("attr", "test");
        Element child = new Element("child", NS_A);
        child.setAttribute("attr", "test");
        root.addContent(child);

        List<Element> newNodes = new LinkedList<>();
        newNodes.add(new Element("new-sibling1", NS_A));
        newNodes.add(new Element("new-sibling2", NS_A));
        Action action = new ReplaceAction(AbstractActionTest.XPATH_FACTORY, "//prefix:root",
                Collections.singletonMap(NS_A.getPrefix(), NS_A.getURI()), Collections.unmodifiableList(newNodes));
        action.execute(actual);
    }

}
