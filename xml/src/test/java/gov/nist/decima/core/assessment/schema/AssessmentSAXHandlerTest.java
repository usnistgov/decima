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

package gov.nist.decima.core.assessment.schema;

import gov.nist.decima.core.assessment.result.AssessmentResultBuilder;
import gov.nist.decima.core.assessment.result.TestState;
import gov.nist.decima.core.document.DocumentException;
import gov.nist.decima.core.document.JDOMDocument;

import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaderJDOMFactory;
import org.jdom2.input.sax.XMLReaderSchemaFactory;
import org.jmock.Expectations;
import org.jmock.Sequence;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

public class AssessmentSAXHandlerTest {
  @Rule
  public JUnitRuleMockery context = new JUnitRuleMockery();

  @Test
  public void test() throws JDOMException, IOException, SAXException, DocumentException {
    JDOMDocument template
        = new JDOMDocument(new File("src/test/resources/jdom-resource-resolution/with-xsi.xml"));

    AssessmentResultBuilder builder = context.mock(AssessmentResultBuilder.class);

    SchemaFactory schemafac = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    Schema schema = schemafac.newSchema();
    XMLReaderJDOMFactory factory = new XMLReaderSchemaFactory(schema);
    SAXBuilder saxBuilder = new SAXBuilder(factory);

    XMLPathLocationAssessmentXMLFilter filter = new XMLPathLocationAssessmentXMLFilter();


    Sequence sequence = context.sequence("execute-assessment");
    context.checking(new Expectations() {
      {
        oneOf(builder).assignTestStatus(with(same("test-1")), with(same(TestState.TESTED)));
        inSequence(sequence);
      }
    });

    AssessmentSAXErrorHandler receiver
        = new AssessmentSAXErrorHandler(template, "test-1", builder, filter);
    saxBuilder.setErrorHandler(receiver);
    saxBuilder.setXMLFilter(filter);

    saxBuilder.build(template.newInputStream(), template.getSystemId());

//    for (String derivedRequirementId : resultBuilder.getDerivedRequirementsTestStatus().keySet()) {
//      System.out.println(derivedRequirementId);
//      for (TestResult asrResult : resultBuilder
//          .getAssertionResultsByDerivedRequirementId(derivedRequirementId)) {
//        System.out.println("  status=" + asrResult.getStatus() + ", message="
//            + asrResult.getResultValues() + ", location=" + asrResult.getContext().getLine() + ","
//            + asrResult.getContext().getColumn() + ", xpath=" + asrResult.getContext().getXPath());
//      }
//    }

    // AssessmentSAXHandler receiver = new AssessmentSAXHandler(doc, derivedRequirementId,
    // builder);
    //
    // ValidatorHandler vh = schema.newValidatorHandler();
    // vh.setContentHandler(receiver);
    // vh.setErrorHandler(receiver);
    // LSResourceResolver resourceResolver = getResourceResolver();
    // if (resourceResolver != null) {
    // vh.setResourceResolver(resourceResolver);
    // }
    //
    // SAXOutputter out = new SAXOutputter(vh);
    // out.
    // try {
    // out.output(doc.getJDOMDocument());
    // } catch (JDOMException | IOException e) {
    // throw new AssessmentException(e);
    // }
    //
    // fail("Not yet implemented");
  }

}
