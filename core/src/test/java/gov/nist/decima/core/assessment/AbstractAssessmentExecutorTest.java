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

package gov.nist.decima.core.assessment;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

import gov.nist.decima.core.assessment.result.AssessmentResultBuilder;
import gov.nist.decima.core.assessment.result.SummarizingAssessmentResultsBuilder;
import gov.nist.decima.core.document.Document;

import org.hamcrest.Matchers;
import org.hamcrest.core.IsInstanceOf;
import org.jmock.Expectations;
import org.jmock.Sequence;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactoryConfigurationException;

public class AbstractAssessmentExecutorTest {
  @Rule
  public JUnitRuleMockery context = new JUnitRuleMockery();

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Test
  public void testNullAssessments() {
    exception.expect(NullPointerException.class);

    new AbstractAssessmentExecutor<Document>(null) {
    };
  }

  @Test
  public void testEmptyAssessments() {
    exception.expect(IllegalArgumentException.class);

    new AbstractAssessmentExecutor<Document>(Collections.emptyList()) {
    };
  }

  @Test
  public void testExecuteAssessments()
      throws AssessmentException, XPathFactoryConfigurationException, XPathExpressionException {
    @SuppressWarnings("unchecked")
    ConditionalAssessment<Document> assessment1
        = (ConditionalAssessment<Document>) context.mock(ConditionalAssessment.class, "assessment1");
    @SuppressWarnings("unchecked")
    ConditionalAssessment<Document> assessment2
        = (ConditionalAssessment<Document>) context.mock(ConditionalAssessment.class, "assessment2");
    @SuppressWarnings("unchecked")
    Assessment<Document> assessment3 = (Assessment<Document>) context.mock(Assessment.class, "assessment3");
    List<Assessment<Document>> assesments = new ArrayList<>(3);
    assesments.add(assessment1);
    assesments.add(assessment2);
    assesments.add(assessment3);

    TestableAbstractAssessmentExecutor executor = new TestableAbstractAssessmentExecutor(assesments);

    Document document = context.mock(Document.class);

    AssessmentResultBuilder builder = context.mock(AssessmentResultBuilder.class);

    @SuppressWarnings("unchecked")
    AssessmentNotifier<Document> notifier = (AssessmentNotifier<Document>) context.mock(AssessmentNotifier.class);

    Sequence sequence = context.sequence("execute-assessments");
    context.checking(new Expectations() {
      {
        // Starting the assessment execution
        oneOf(builder).start();
        inSequence(sequence);

        oneOf(notifier).assessmentExecutionStarted(with(same(document)));
        inSequence(sequence);

        // determine the assessments to perform
        oneOf(assessment1).getExecutableAssessments(with(same(document)));
        will(returnValue(Collections.emptyList()));
        inSequence(sequence);
        oneOf(assessment2).getExecutableAssessments(with(same(document)));
        will(returnValue(Collections.singletonList(assessment2)));
        inSequence(sequence);
        oneOf(assessment3).getExecutableAssessments(with(same(document)));
        will(returnValue(Collections.singletonList(assessment3)));
        inSequence(sequence);

        // Perform each assessment
        // -----------------------
        // assessment 2 (conditional: true)
        oneOf(notifier).isProvideSummary();
        will(returnValue(false));
        inSequence(sequence);
        // Starting the assessment
        oneOf(notifier).assessmentStarted(with(same(assessment2)), with(same(document)));
        inSequence(sequence);
        // execute
        oneOf(assessment2).execute(with(same(document)), with(same(builder)));
        // completing the assessment
        oneOf(notifier).assessmentCompleted(with(same(assessment2)), with(same(document)),
            with(aNull(SummarizingAssessmentResultsBuilder.class)));
        inSequence(sequence);
        // -----------------------
        // assessment 3 (non-conditional)
        oneOf(notifier).isProvideSummary();
        will(returnValue(false));
        inSequence(sequence);
        // Starting the assessment
        oneOf(notifier).assessmentStarted(with(same(assessment3)), with(same(document)));
        inSequence(sequence);
        // execute
        oneOf(assessment3).execute(with(same(document)), with(same(builder)));
        // completing the assessment
        oneOf(notifier).assessmentCompleted(with(same(assessment3)), with(same(document)),
            with(aNull(SummarizingAssessmentResultsBuilder.class)));
        inSequence(sequence);
        // complete the execution
        oneOf(notifier).assessmentExecutionCompleted(with(same(document)));
        inSequence(sequence);
      }
    });
    executor.execute(document, builder, notifier);
  }

  @Test
  public void testAssessmentException() throws XPathFactoryConfigurationException, AssessmentException {
    @SuppressWarnings("unchecked")
    Assessment<Document> assessment = (Assessment<Document>) context.mock(Assessment.class, "assessment");

    TestableAbstractAssessmentExecutor executor
        = new TestableAbstractAssessmentExecutor(Collections.singletonList(assessment));

    Document document = context.mock(Document.class);

    AssessmentResultBuilder builder = context.mock(AssessmentResultBuilder.class);

    @SuppressWarnings("unchecked")
    AssessmentNotifier<Document> notifier = (AssessmentNotifier<Document>) context.mock(AssessmentNotifier.class);

    Throwable ex = new AssessmentException();
    Sequence sequence = context.sequence("execute-assessments");
    context.checking(new Expectations() {
      {
        // Startin the assessment execution
        oneOf(builder).start();
        inSequence(sequence);

        oneOf(notifier).assessmentExecutionStarted(with(same(document)));
        inSequence(sequence);

        // determine the assessments to perform
        oneOf(assessment).getExecutableAssessments(with(same(document)));
        will(returnValue(Collections.singletonList(assessment)));
        inSequence(sequence);

        // Perform each assessment
        // assessment 1 (non-conditional)
        oneOf(notifier).isProvideSummary();
        will(returnValue(false));
        inSequence(sequence);
        // Starting the assessment
        oneOf(notifier).assessmentStarted(with(same(assessment)), with(same(document)));
        inSequence(sequence);
        // Perform the assessment
        oneOf(assessment).execute(with(same(document)), with(same(builder)));
        will(throwException(ex));
        inSequence(sequence);
        // this will generate an error notification
        oneOf(notifier).assessmentError(with(same(assessment)), with(same(document)), with(same(ex)));
        inSequence(sequence);
      }
    });

    exception.expect(is(equalTo(ex)));
    executor.execute(document, builder, notifier);
  }

  @Test
  public void testAssessmentUnknownException() throws XPathFactoryConfigurationException, AssessmentException {
    @SuppressWarnings("unchecked")
    Assessment<Document> assessment = (Assessment<Document>) context.mock(Assessment.class, "assessment");

    TestableAbstractAssessmentExecutor executor
        = new TestableAbstractAssessmentExecutor(Collections.singletonList(assessment));

    Document document = context.mock(Document.class);

    AssessmentResultBuilder builder = context.mock(AssessmentResultBuilder.class);

    @SuppressWarnings("unchecked")
    AssessmentNotifier<Document> notifier = (AssessmentNotifier<Document>) context.mock(AssessmentNotifier.class);
    RuntimeException ex = new RuntimeException();

    Sequence sequence = context.sequence("execute-assessments");
    context.checking(new Expectations() {
      {
        // Starting the assessment execution
        oneOf(builder).start();
        inSequence(sequence);
        oneOf(notifier).assessmentExecutionStarted(with(same(document)));
        inSequence(sequence);
        // determine the assessments to perform
        oneOf(assessment).getExecutableAssessments(with(same(document)));
        will(returnValue(Collections.singletonList(assessment)));
        inSequence(sequence);
        // Perform each assessment
        // assessment 1 (non-conditional)
        oneOf(notifier).isProvideSummary();
        will(returnValue(false));
        inSequence(sequence);
        // Starting the assessment
        oneOf(notifier).assessmentStarted(with(same(assessment)), with(same(document)));
        inSequence(sequence);
        // execute it
        oneOf(assessment).execute(with(same(document)), with(same(builder)));
        will(throwException(ex));
        inSequence(sequence);
        // this will generate an error notification
        oneOf(notifier).assessmentError(with(same(assessment)), with(same(document)), with(same(ex)));
        inSequence(sequence);
        oneOf(assessment).getName(with(same(false)));
        inSequence(sequence);
      }
    });

    exception.expect(AssessmentException.class);
    exception.expectCause(IsInstanceOf.<Throwable> instanceOf(RuntimeException.class));
    exception.expectMessage(Matchers.any(String.class));
    executor.execute(document, builder, notifier);
  }

  private static class TestableAbstractAssessmentExecutor extends AbstractAssessmentExecutor<Document> {

    public TestableAbstractAssessmentExecutor(List<? extends Assessment<Document>> assessments) {
      super(assessments);
    }
  }
}
