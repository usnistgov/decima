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

package gov.nist.decima.core.assessment.result;

import gov.nist.decima.core.assessment.Assessment;
import gov.nist.decima.core.assessment.util.AssessmentLoggingAssessmentNotifier;
import gov.nist.decima.core.assessment.util.SummarizingAssessmentResultsBuilder;
import gov.nist.decima.core.document.Document;

import org.apache.logging.log4j.Level;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

public class SummarizingAssessmentResultsBuilderTest {

  @Rule
  public JUnitRuleMockery context = new JUnitRuleMockery();

  /**
   * Test that a summarizing result builder works without any results being produced.
   */
  @Test
  public void testNoResults() {
    AssessmentResultBuilder delegate = context.mock(AssessmentResultBuilder.class);

    SummarizingAssessmentResultsBuilder builder = new SummarizingAssessmentResultsBuilder(delegate);

    AssessmentLoggingAssessmentNotifier<Document> notifier = new AssessmentLoggingAssessmentNotifier<Document>(Level.INFO);

    Document document = context.mock(Document.class);
    @SuppressWarnings("unchecked")
    Assessment<Document> assessment = context.mock(Assessment.class);

    Assert.assertTrue(notifier.isProvideSummary(assessment, document));

    // Sequence sequence = context.sequence("test");
    context.checking(new Expectations() {
      {
        allowing(assessment).getName(with(any(Boolean.class)));
        will(returnValue("assessment"));
      }
    });
    notifier.assessmentCompleted(assessment, document, builder);
  }

  @Test
  public void testAddTestResult() {
    AssessmentResultBuilder delegate = context.mock(AssessmentResultBuilder.class);

    SummarizingAssessmentResultsBuilder builder = new SummarizingAssessmentResultsBuilder(delegate);

    String derivedRequirementId = "DER-1";
    TestResult testResult = context.mock(TestResult.class);//new BasicTestResult("TEST-1", TestStatus.FAIL, con);

    context.checking(new Expectations() {
      {
        allowing(delegate).addTestResult(with(same(derivedRequirementId)), with(testResult));
        allowing(testResult).getStatus();
        will(returnValue(TestStatus.FAIL));
      }
    });

    builder.addTestResult(derivedRequirementId, testResult);
    Assert.assertEquals(1, builder.getTestResultCount());
    Assert.assertEquals(1, (int) builder.getDerivedRequirementStateCount().get(TestState.TESTED));
    Assert.assertEquals(1, (int) builder.getDerivedRequirementStatusCount().get(TestStatus.FAIL));
  }
}
