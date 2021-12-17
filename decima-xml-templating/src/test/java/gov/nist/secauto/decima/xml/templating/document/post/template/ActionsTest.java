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

import gov.nist.secauto.decima.core.classpath.ClasspathHandler;
import gov.nist.secauto.decima.core.document.DocumentException;
import gov.nist.secauto.decima.xml.document.SimpleXMLDocumentResolver;
import gov.nist.secauto.decima.xml.document.XMLDocument;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.XMLUnit;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public class ActionsTest {
  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  private static final String DECIMA_TEMPLATE_NS_URI = TemplateParser.TEMPLATE_NAMESPACE.getURI();
  private static SAXBuilder builder = new SAXBuilder();

  private String getStandardBaseXML() {
    StringBuilder baseXML = new StringBuilder();
    baseXML.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    baseXML.append("<root-node xmlns='http://tempuri.org'>");
    baseXML.append("	<sample-node-1/>");
    baseXML.append("	<sample-node-2 sample='test'/>");
    baseXML.append("</root-node>");
    return baseXML.toString();
  }

  @BeforeClass
  public static void initialize() {
    ClasspathHandler.initialize();
  }

  @Test
  public void testDeleteAction() throws Exception {
    String baseXML = getStandardBaseXML();
    File testXmlFile = tempFolder.newFile();

    // Delete an element
    StringBuilder actionXML = new StringBuilder();
    actionXML.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    actionXML.append("<template xmlns='" + DECIMA_TEMPLATE_NS_URI + "' template='" + testXmlFile.toURI()
        + "' xmlns:temp='http://tempuri.org'>");
    actionXML.append("	<delete xpath='/temp:root-node/temp:sample-node-1'/>");
    actionXML.append("</template>");

    String newXML = processActions(baseXML, testXmlFile, actionXML.toString());
    String controlXML = baseXML.replace("<sample-node-1/>", "");
    Diff diff = getNewDiff(controlXML, newXML);
    if (!diff.identical()) {
      fail(diff.toString(), controlXML, newXML);
    }

    // Delete an attribute
    actionXML = new StringBuilder();
    actionXML.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    actionXML.append("<template xmlns='" + DECIMA_TEMPLATE_NS_URI + "' template='" + testXmlFile.toURI()
        + "' xmlns:temp='http://tempuri.org'>");
    actionXML.append("	<delete xpath='/temp:root-node/temp:sample-node-2/@sample'/>");
    actionXML.append("</template>");

    newXML = processActions(baseXML, testXmlFile, actionXML.toString());
    controlXML = baseXML.replace("sample='test'", "");
    diff = getNewDiff(controlXML, newXML);
    if (!diff.identical()) {
      fail(diff.toString(), controlXML, newXML);
    }

  }

  @Test
  public void testInsertSiblingAction() throws Exception {
    String baseXML = getStandardBaseXML();
    File testXmlFile = tempFolder.newFile();

    // Insert without the attribute "before"
    StringBuilder actionXML = new StringBuilder();
    actionXML.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    actionXML.append("<template xmlns='" + DECIMA_TEMPLATE_NS_URI + "' template='" + testXmlFile.toURI()
        + "' xmlns:temp='http://tempuri.org'>");
    actionXML.append("	<insert-sibling xpath='/temp:root-node/temp:sample-node-1'>");
    actionXML.append("		<temp:test/>");
    actionXML.append("	</insert-sibling>");
    actionXML.append("</template>");

    String newXML = processActions(baseXML, testXmlFile, actionXML.toString());
    String controlXML = baseXML.replace("<sample-node-1/>", "<sample-node-1/><test/>");
    Diff diff = getNewDiff(controlXML, newXML);
    if (!diff.identical()) {
      fail(diff.toString(), controlXML, newXML);
    }

    // Insert with "before" set to true
    actionXML = new StringBuilder();
    actionXML.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    actionXML.append("<template xmlns='" + DECIMA_TEMPLATE_NS_URI + "' template='" + testXmlFile.toURI()
        + "' xmlns:temp='http://tempuri.org'>");
    actionXML.append("	<insert-sibling before='true' xpath='/temp:root-node/temp:sample-node-1'>");
    actionXML.append("		<temp:test/>");
    actionXML.append("	</insert-sibling>");
    actionXML.append("</template>");

    testXmlFile = tempFolder.newFile();
    newXML = processActions(baseXML, testXmlFile, actionXML.toString());
    controlXML = baseXML.replace("<sample-node-1/>", "<test/><sample-node-1/>");
    diff = getNewDiff(controlXML, newXML);
    if (!diff.identical()) {
      fail(diff.toString(), controlXML, newXML);
    }

    // Insert with "before" set to true
    actionXML = new StringBuilder();
    actionXML.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    actionXML.append("<template xmlns='" + DECIMA_TEMPLATE_NS_URI + "' template='" + testXmlFile.toURI()
        + "' xmlns:temp='http://tempuri.org'>");
    actionXML.append("	<insert-sibling before='false' xpath='/temp:root-node/temp:sample-node-1'>");
    actionXML.append("		<temp:test/>");
    actionXML.append("	</insert-sibling>");
    actionXML.append("</template>");

    newXML = processActions(baseXML, testXmlFile, actionXML.toString());
    controlXML = baseXML.replace("<sample-node-1/>", "<sample-node-1/><test/>");
    diff = getNewDiff(controlXML, newXML);
    if (!diff.identical()) {
      fail(diff.toString(), controlXML, newXML);
    }

    // Insert multiple without the attribute "before"
    actionXML = new StringBuilder();
    actionXML.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    actionXML.append("<template xmlns='" + DECIMA_TEMPLATE_NS_URI + "' template='" + testXmlFile.toURI()
        + "' xmlns:temp='http://tempuri.org'>");
    actionXML.append("	<insert-sibling xpath='/temp:root-node/temp:sample-node-2'>");
    actionXML.append("		<temp:test1/>");
    actionXML.append("		<temp:test2/>");
    actionXML.append("	</insert-sibling>");
    actionXML.append("</template>");

    newXML = processActions(baseXML, testXmlFile, actionXML.toString());
    controlXML = baseXML.replace("<sample-node-2 sample='test'/>", "<sample-node-2 sample='test'/><test1/><test2/>");
    diff = getNewDiff(controlXML, newXML);
    if (!diff.identical()) {
      fail(diff.toString(), controlXML, newXML);
    }

    // Insert multiple with the attribute "before" = true
    actionXML = new StringBuilder();
    actionXML.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    actionXML.append("<template xmlns='" + DECIMA_TEMPLATE_NS_URI + "' template='" + testXmlFile.toURI()
        + "' xmlns:temp='http://tempuri.org'>");
    actionXML.append("	<insert-sibling before='true' xpath='/temp:root-node/temp:sample-node-2'>");
    actionXML.append("		<temp:test1/>");
    actionXML.append("		<temp:test2/>");
    actionXML.append("	</insert-sibling>");
    actionXML.append("</template>");

    newXML = processActions(baseXML, testXmlFile, actionXML.toString());
    controlXML = baseXML.replace("<sample-node-2 sample='test'/>", "<test1/><test2/><sample-node-2 sample='test'/>");
    diff = getNewDiff(controlXML, newXML);
    if (!diff.identical()) {
      fail(diff.toString(), controlXML, newXML);
    }

  }

  @Test
  public void testInsertChildAction() throws Exception {
    String baseXML = getStandardBaseXML();
    File testXmlFile = tempFolder.newFile();

    // Insert without the attribute "index" (element appends)
    StringBuilder actionXML = new StringBuilder();
    actionXML.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    actionXML.append("<template xmlns='" + DECIMA_TEMPLATE_NS_URI + "' template='" + testXmlFile.toURI()
        + "' xmlns:temp='http://tempuri.org'>");
    actionXML.append("	<insert-child xpath='/temp:root-node'>");
    actionXML.append("		<temp:sample-node-3/>");
    actionXML.append("	</insert-child>");
    actionXML.append("</template>");

    String newXML = processActions(baseXML, testXmlFile, actionXML.toString());
    String controlXML
        = baseXML.replace("<sample-node-2 sample='test'/>", "<sample-node-2 sample='test'/><sample-node-3/>");
    Diff diff = getNewDiff(controlXML, newXML);
    if (!diff.identical()) {
      fail(diff.toString(), controlXML, newXML);
    }

    // Insert with "index" set to 0
    actionXML = new StringBuilder();
    actionXML.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    actionXML.append("<template xmlns='" + DECIMA_TEMPLATE_NS_URI + "' template='" + testXmlFile.toURI()
        + "' xmlns:temp='http://tempuri.org'>");
    actionXML.append("	<insert-child index='0' xpath='/temp:root-node'>");
    actionXML.append("		<temp:sample-node-3/>");
    actionXML.append("	</insert-child>");
    actionXML.append("</template>");

    newXML = processActions(baseXML, testXmlFile, actionXML.toString());
    controlXML = baseXML.replace("<sample-node-1/>", "<sample-node-3/><sample-node-1/>");
    diff = getNewDiff(controlXML, newXML);
    if (!diff.identical()) {
      fail(diff.toString(), controlXML, newXML);
    }

    // Insert with "index" set to 1
    actionXML = new StringBuilder();
    actionXML.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    actionXML.append("<template xmlns='" + DECIMA_TEMPLATE_NS_URI + "' template='" + testXmlFile.toURI()
        + "' xmlns:temp='http://tempuri.org'>");
    actionXML.append("	<insert-child index='1' xpath='/temp:root-node'>");
    actionXML.append("		<temp:sample-node-3/>");
    actionXML.append("	</insert-child>");
    actionXML.append("</template>");

    newXML = processActions(baseXML, testXmlFile, actionXML.toString());
    controlXML = baseXML.replace("<sample-node-1/>", "<sample-node-1/><sample-node-3/>");
    diff = getNewDiff(controlXML, newXML);
    if (!diff.identical()) {
      fail(diff.toString(), controlXML, newXML);
    }

    // Insert with "index" set to 2 (expect exception)
    actionXML = new StringBuilder();
    actionXML.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    actionXML.append("<template xmlns='" + DECIMA_TEMPLATE_NS_URI + "' template='" + testXmlFile.toURI()
        + "' xmlns:temp='http://tempuri.org'>");
    actionXML.append("	<insert-child index='2' xpath='/temp:root-node'>");
    actionXML.append("		<temp:sample-node-3/>");
    actionXML.append("	</insert-child>");
    actionXML.append("</template>");

    try {
      newXML = processActions(baseXML, testXmlFile, actionXML.toString());
      Assert.fail("The child index is out of bounds...an exception should have been thrown.");
    } catch (DocumentException e) {
      Assert.assertEquals(ActionProcessingException.class, e.getCause().getClass());
    }

    // Insert multiple at end
    actionXML = new StringBuilder();
    actionXML.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    actionXML.append("<template xmlns='" + DECIMA_TEMPLATE_NS_URI + "' template='" + testXmlFile.toURI()
        + "' xmlns:temp='http://tempuri.org'>");
    actionXML.append("	<insert-child xpath='/temp:root-node'>");
    actionXML.append("		<temp:sample-node-3/>");
    actionXML.append("		<temp:sample-node-4/>");
    actionXML.append("	</insert-child>");
    actionXML.append("</template>");

    newXML = processActions(baseXML, testXmlFile, actionXML.toString());
    controlXML = baseXML.replace("<sample-node-2 sample='test'/>",
        "<sample-node-2 sample='test'/><sample-node-3/><sample-node-4/>");
    diff = getNewDiff(controlXML, newXML);
    if (!diff.identical()) {
      fail(diff.toString(), controlXML, newXML);
    }

    // Insert multiple in middle
    actionXML = new StringBuilder();
    actionXML.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    actionXML.append("<template xmlns='" + DECIMA_TEMPLATE_NS_URI + "' template='" + testXmlFile.toURI()
        + "' xmlns:temp='http://tempuri.org'>");
    actionXML.append("	<insert-child xpath='/temp:root-node' index='1'>");
    actionXML.append("		<temp:sample-node-3/>");
    actionXML.append("		<temp:sample-node-4/>");
    actionXML.append("	</insert-child>");
    actionXML.append("</template>");

    newXML = processActions(baseXML, testXmlFile, actionXML.toString());
    controlXML = baseXML.replace("<sample-node-1/>", "<sample-node-1/><sample-node-3/><sample-node-4/>");
    diff = getNewDiff(controlXML, newXML);
    if (!diff.identical()) {
      fail(diff.toString(), controlXML, newXML);
    }

  }

  @Test
  public void testModifyAttributeAction() throws Exception {
    String baseXML = getStandardBaseXML();
    File testXmlFile = tempFolder.newFile();

    // Delete an element
    StringBuilder actionXML = new StringBuilder();
    actionXML.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    actionXML.append("<template xmlns='" + DECIMA_TEMPLATE_NS_URI + "' template='" + testXmlFile.toURI()
        + "' xmlns:temp='http://tempuri.org'>");
    actionXML.append("	<modify-attribute xpath='/temp:root-node/temp:sample-node-2/@sample' value='replace'/>");
    actionXML.append("</template>");

    String newXML = processActions(baseXML, testXmlFile, actionXML.toString());
    String controlXML = baseXML.replace("<sample-node-2 sample='test'/>", "<sample-node-2 sample='replace'/>");
    Diff diff = getNewDiff(controlXML, newXML);
    if (!diff.identical()) {
      fail(diff.toString(), controlXML, newXML);
    }
  }

  @Test
  public void testAddAttributeAction() throws Exception {
    String baseXML = getStandardBaseXML();
    File testXmlFile = tempFolder.newFile();

    // Delete an element
    StringBuilder actionXML = new StringBuilder();
    actionXML.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    actionXML.append("<template xmlns='" + DECIMA_TEMPLATE_NS_URI + "' template='" + testXmlFile.toURI()
        + "' xmlns:temp='http://tempuri.org'>");
    actionXML.append("	<add-attribute xpath='/temp:root-node/temp:sample-node-1' name='sample' value='replace'/>");
    actionXML.append("</template>");

    String newXML = processActions(baseXML, testXmlFile, actionXML.toString());
    String controlXML = baseXML.replace("<sample-node-1/>", "<sample-node-1 sample='replace'/>");
    Diff diff = getNewDiff(controlXML, newXML);
    if (!diff.identical()) {
      fail(diff.toString(), controlXML, newXML);
    }

    actionXML = new StringBuilder();
    actionXML.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    actionXML.append("<template xmlns='" + DECIMA_TEMPLATE_NS_URI + "' template='" + testXmlFile.toURI()
        + "' xmlns:temp='http://tempuri.org'>");
    actionXML.append("	<add-attribute xpath='/temp:root-node/temp:sample-node-2' name='sample' value='replace'/>");
    actionXML.append("</template>");

    try {
      newXML = processActions(baseXML, testXmlFile, actionXML.toString());
      Assert.fail(
          "Attempted to added an attribute to an element that already has that attribute...an exception should have been thrown.");
    } catch (DocumentException e) {
      Assert.assertEquals(ActionProcessingException.class, e.getCause().getClass());
    }

    actionXML = new StringBuilder();
    actionXML.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    actionXML.append("<template xmlns='" + DECIMA_TEMPLATE_NS_URI + "' template='" + testXmlFile.toURI()
        + "' xmlns:temp='http://tempuri.org'>");
    actionXML.append(
        "	<add-attribute xpath='/temp:root-node/temp:sample-node-1' name='sample' value='replace' ns='http://tempuri.org'/>");
    actionXML.append("</template>");

    newXML = processActions(baseXML, testXmlFile, actionXML.toString());
    controlXML
        = baseXML.replace("<sample-node-1/>", "<sample-node-1 xmlns:ns1='http://tempuri.org' ns1:sample='replace'/>");
    diff = getNewDiff(controlXML, newXML);
    if (!diff.identical()) {
      fail(diff.toString(), controlXML, newXML);
    }

    actionXML = new StringBuilder();
    actionXML.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    actionXML.append("<template xmlns='" + DECIMA_TEMPLATE_NS_URI + "' template='" + testXmlFile.toURI()
        + "' xmlns:temp='http://tempuri.org'>");
    actionXML.append(
        "	<add-attribute xpath='/temp:root-node/temp:sample-node-1' name='lang' value='en-US' ns='http://www.w3.org/XML/1998/namespace'/>");
    actionXML.append("</template>");

    newXML = processActions(baseXML, testXmlFile, actionXML.toString());
    controlXML = baseXML.replace("<sample-node-1/>",
        "<sample-node-1 xmlns:xml='http://www.w3.org/XML/1998/namespace' xml:lang='en-US'/>");
    diff = getNewDiff(controlXML, newXML);
    if (!diff.identical()) {
      fail(diff.toString(), controlXML, newXML);
    }

  }

  @Test
  public void testReplaceAction() throws Exception {
    String baseXML = getStandardBaseXML();
    File testXmlFile = tempFolder.newFile();

    // Delete an element
    StringBuilder actionXML = new StringBuilder();
    actionXML.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    actionXML.append("<template xmlns='" + DECIMA_TEMPLATE_NS_URI + "' template='" + testXmlFile.toURI()
        + "' xmlns:temp='http://tempuri.org'>");
    actionXML.append("	<replace xpath='/temp:root-node/temp:sample-node-2'>");
    actionXML.append("    <temp:test1/><temp:test2/>");
    actionXML.append("	</replace>");
    actionXML.append("</template>");

    String newXML = processActions(baseXML, testXmlFile, actionXML.toString());
    String controlXML = baseXML.replace("<sample-node-2 sample='test'/>", "<test1/><test2/>");
    Diff diff = getNewDiff(controlXML, newXML);
    if (!diff.identical()) {
      fail(diff.toString(), controlXML, newXML);
    }

    actionXML = new StringBuilder();
    actionXML.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    actionXML.append("<template xmlns='" + DECIMA_TEMPLATE_NS_URI + "' template='" + testXmlFile.toURI()
        + "' xmlns:temp='http://tempuri.org'>");
    actionXML.append("	<replace xpath='/temp:root-node/temp:sample-node-1'>");
    actionXML.append("    <temp:test1/><temp:test2/>");
    actionXML.append("	</replace>");
    actionXML.append("</template>");

    newXML = processActions(baseXML, testXmlFile, actionXML.toString());
    controlXML = baseXML.replace("<sample-node-1/>", "<test1/><test2/>");
    diff = getNewDiff(controlXML, newXML);
    if (!diff.identical()) {
      fail(diff.toString(), controlXML, newXML);
    }
  }

  private String processActions(String baseXML, File baseFile, String actionXML)
      throws UnsupportedEncodingException, JDOMException, IOException, TemplateParserException, DocumentException {
    Document baseDoc = buildDocumentFromString(baseXML);

    XMLOutputter xout = new XMLOutputter();
    xout.output(baseDoc, new FileWriter(baseFile));

    TemplateParser parser = TemplateParser.getInstance();
    InputStream inputStream = new ByteArrayInputStream(actionXML.getBytes(Charset.forName("UTF-8")));
    TemplateProcessor tp = parser.parse(inputStream, baseFile.toURI().toURL());
    XMLDocument document = tp.generate(new SimpleXMLDocumentResolver());
    return document.asString(Format.getRawFormat());
    // Document actionDoc = buildDocumentFromString(actionXML);
    //
    // XMLFileProcessor.processDocument(actionDoc, baseDoc);
    //
    // ByteArrayOutputStream baos = new ByteArrayOutputStream();
    // Source source = new JDOMSource(actionDoc);
    // Result result = new StreamResult(baos);
    // Transformer xformer = TransformerFactory.newInstance().newTransformer();
    // xformer.transform(source, result);
    // return new String(baos.toByteArray());
  }

  private Document buildDocumentFromString(String xml) throws UnsupportedEncodingException, JDOMException, IOException {
    return builder.build(new ByteArrayInputStream(xml.toString().getBytes("UTF-8")));
  }

  private void fail(String message, String controlXML, String testXML) {
    Assert.fail(message + "\n\nControl:\n " + controlXML + "\n\nResult:\n" + testXML);
  }

  private Diff getNewDiff(String controlXML, String testXML) throws SAXException, IOException {
    XMLUnit.setIgnoreWhitespace(true);
    Diff diff = new Diff(controlXML, testXML);
    diff.overrideDifferenceListener(new CustomDifferenceListener());
    return diff;
  }

  private static class CustomDifferenceListener implements DifferenceListener {
    @Override
    public int differenceFound(Difference difference) {
      if (difference.getDescription().equals("namespace prefix")) {
        return DifferenceListener.RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
      }
      return DifferenceListener.RETURN_ACCEPT_DIFFERENCE;
    }

    @Override
    public void skippedComparison(Node arg0, Node arg1) {
    }
  }

}
