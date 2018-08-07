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

package gov.nist.decima.xml.assessment.schema;

import gov.nist.decima.core.document.DocumentException;

import org.jdom2.JDOMException;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;

public class AssessmentSAXHandlerTest {
  @Rule
  public JUnitRuleMockery context = new JUnitRuleMockery();

  @Ignore
  @Test
  public void test() throws JDOMException, IOException, SAXException, DocumentException {
    // JDOMDocument template
    // = new JDOMDocument(new File("src/test/resources/jdom-resource-resolution/with-xsi.xml"));
    //
    // AssessmentResultBuilder builder = context.mock(AssessmentResultBuilder.class);
    //
    // SchemaFactory schemafac = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    // Schema schema = schemafac.newSchema();
    // XMLReaderJDOMFactory factory = new XMLReaderSchemaFactory(schema);
    // SAXBuilder saxBuilder = new SAXBuilder(factory);
    //
    // XMLPathLocationAssessmentXMLFilter filter = new XMLPathLocationAssessmentXMLFilter();
    //
    // SchemaAssessment assessment = new SchemaAssessment();
    //
    // Sequence sequence = context.sequence("execute-assessment");
    // context.checking(new Expectations() {
    // {
    // oneOf(builder).assignTestStatus(with(same("test-1")), with(same(TestState.TESTED)));
    // inSequence(sequence);
    // }
    // });
    //
    // AssessmentSAXErrorHandler receiver
    // = new AssessmentSAXErrorHandler(template, "test-1", builder, filter);
    // saxBuilder.setErrorHandler(receiver);
    // saxBuilder.setXMLFilter(filter);
    //
    // saxBuilder.build(template.newInputStream(), template.getSystemId());
  }

}
