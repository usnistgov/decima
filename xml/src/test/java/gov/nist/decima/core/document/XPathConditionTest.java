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

package gov.nist.decima.core.document;

import gov.nist.decima.core.assessment.AssessmentException;

import org.jmock.Expectations;
import org.jmock.Sequence;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactoryConfigurationException;

public class XPathConditionTest {
  @Rule
  public JUnitRuleMockery context = new JUnitRuleMockery();

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Test
  public void testXPathMatch() throws AssessmentException, XPathFactoryConfigurationException, XPathExpressionException {
    XMLDocument document = context.mock(XMLDocument.class);
    XPathEvaluator xpathEval = context.mock(XPathEvaluator.class);

    String xpath = "xpath";
    XPathCondition xpathCondition = new XPathCondition(xpath);

    
    Sequence sequence = context.sequence("execute-assessments");
    context.checking(new Expectations() {
      {
        // get the evaluator
        oneOf(document).newXPathEvaluator();
        will(returnValue(xpathEval));
        inSequence(sequence);
        // check correct xpath
        oneOf(xpathEval).test(with(same(xpath)));
        will(returnValue(true));
        inSequence(sequence);
      }
    });
    Assert.assertTrue(xpathCondition.appliesTo(document));
    Assert.assertSame(xpath, xpathCondition.getXPath());
  }

  @Test
  public void testXPathMisMatch() throws AssessmentException, XPathFactoryConfigurationException, XPathExpressionException {
    XMLDocument document = context.mock(XMLDocument.class);
    XPathEvaluator xpathEval = context.mock(XPathEvaluator.class);

    String xpath = "xpath";
    XPathCondition xpathCondition = new XPathCondition(xpath);

    Sequence sequence = context.sequence("execute-assessments");
    context.checking(new Expectations() {
      {
        // get the evaluator
        oneOf(document).newXPathEvaluator();
        will(returnValue(xpathEval));
        inSequence(sequence);
        // check correct xpath
        oneOf(xpathEval).test(with(same(xpath)));
        will(returnValue(false));
        inSequence(sequence);
      }
    });
    Assert.assertFalse(xpathCondition.appliesTo(document));
  }

  @Test
  public void testXPathThrowXPathExpressionException() throws AssessmentException, XPathFactoryConfigurationException, XPathExpressionException {
    XMLDocument document = context.mock(XMLDocument.class);
    XPathEvaluator xpathEval = context.mock(XPathEvaluator.class);

    String xpath = "xpath";
    XPathCondition xpathCondition = new XPathCondition(xpath);

    Sequence sequence = context.sequence("execute-assessments");
    context.checking(new Expectations() {
      {
        // check the condition
        oneOf(document).newXPathEvaluator();
        will(returnValue(xpathEval));
        inSequence(sequence);
        // check the condition
        oneOf(xpathEval).test(with(same(xpath)));
        will(throwException(new XPathExpressionException(xpath)));
        inSequence(sequence);
        // building the AssessmentException
        oneOf(document).getSystemId();
        will(returnValue("systemId"));
        inSequence(sequence);
      }
    });

    try {
      xpathCondition.appliesTo(document);
    } catch (Exception ex) {
      Assert.assertSame(AssessmentException.class, ex.getClass());
      Assert.assertSame(XPathExpressionException.class, ex.getCause().getClass());
      Assert.assertSame(xpath, ex.getCause().getMessage());
    }
  }

  @Test
  public void testXPathThrowXPathFactoryConfigurationException() throws AssessmentException, XPathFactoryConfigurationException, XPathExpressionException {
    XMLDocument document = context.mock(XMLDocument.class);
    String xpath = "xpath";
    XPathCondition xpathCondition = new XPathCondition(xpath);

    Sequence sequence = context.sequence("execute-assessments");
    context.checking(new Expectations() {
      {
        // check the condition
        oneOf(document).newXPathEvaluator();
        will(throwException(new XPathFactoryConfigurationException(xpath)));
        inSequence(sequence);
        // building the AssessmentException
        oneOf(document).getSystemId();
        will(returnValue("systemId"));
        inSequence(sequence);
      }
    });

    try {
      xpathCondition.appliesTo(document);
    } catch (Exception ex) {
      Assert.assertSame(AssessmentException.class, ex.getClass());
      Assert.assertSame(XPathFactoryConfigurationException.class, ex.getCause().getClass());
      Assert.assertSame(xpath, ex.getCause().getMessage());
    }
  }
}
