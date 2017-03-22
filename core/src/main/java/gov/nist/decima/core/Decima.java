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

package gov.nist.decima.core;

import gov.nist.decima.core.assessment.Assessment;
import gov.nist.decima.core.assessment.AssessmentExecutorFactory;
import gov.nist.decima.core.assessment.AssessmentReactor;
import gov.nist.decima.core.assessment.BasicAssessmentExecutorFactory;
import gov.nist.decima.core.assessment.ConcurrentAssessmentExecutorFactory;
import gov.nist.decima.core.assessment.Condition;
import gov.nist.decima.core.assessment.ConditionalAssessment;
import gov.nist.decima.core.assessment.DefaultConditionalAssessment;
import gov.nist.decima.core.assessment.result.AssessmentResultBuilder;
import gov.nist.decima.core.assessment.result.BasicTestResult;
import gov.nist.decima.core.assessment.result.DefaultAssessmentResultBuilder;
import gov.nist.decima.core.assessment.result.ResultStatusBehavior;
import gov.nist.decima.core.assessment.result.TestResult;
import gov.nist.decima.core.assessment.result.TestStatus;
import gov.nist.decima.core.assessment.util.AssessmentSequence;
import gov.nist.decima.core.document.Context;
import gov.nist.decima.core.document.Document;
import gov.nist.decima.core.requirement.DefaultRequirementsManager;
import gov.nist.decima.core.requirement.MutableRequirementsManager;
import gov.nist.decima.core.requirement.RequirementsManager;

import java.util.List;
import java.util.concurrent.Executor;

public class Decima {
  private static AssessmentExecutorFactory DEFAULT_ASSESSMENT_EXECUTOR = new BasicAssessmentExecutorFactory();

  public static AssessmentExecutorFactory newConcurrentAssessmentExecutorFactory(int threads) {
    return new ConcurrentAssessmentExecutorFactory(threads);
  }

  public static AssessmentExecutorFactory newConcurrentAssessmentExecutorFactory(Executor executor) {
    return new ConcurrentAssessmentExecutorFactory(executor);
  }

  public static AssessmentExecutorFactory newAssessmentExecutorFactory() {
    return DEFAULT_ASSESSMENT_EXECUTOR;
  }

  /**
   * Creates a new {@link ConditionalAssessment}, making the provided assessment conditional based
   * on the provided {@link Condition}.
   * 
   * @param <DOC>
   *          the type of document that is the target of the assessment
   * @param delegate
   *          the assessment to make conditional
   * @param condition
   *          the applicability criteria
   * @return a new {@link ConditionalAssessment} instance based on the provided information
   */
  public static <DOC extends Document> ConditionalAssessment<DOC> newConditionalAssessment(Assessment<DOC> delegate,
      Condition<DOC> condition) {
    return new DefaultConditionalAssessment<>(delegate, condition);
  }

  /**
   * Creates an aggregation of multiple provided {@link Assessment} instances.
   * 
   * @param <DOC>
   *          the type of document that is the target of the assessment
   * @param assessments
   *          the list of {@link Assessment} instances to aggregate
   * @return a new {@link Assessment} instance based on the provided information
   */
  public static <DOC extends Document> Assessment<DOC>
      newAssessmentSequence(List<? extends Assessment<DOC>> assessments) {
    return new AssessmentSequence<>(assessments);
  }

  public static AssessmentReactor newAssessmentReactor(RequirementsManager requirementsManager) {
    return new AssessmentReactor(requirementsManager);
  }

  public static MutableRequirementsManager newRequirementsManager() {
    return new DefaultRequirementsManager();
  }

  public static AssessmentResultBuilder newAssessmentResultBuilder() {
    return new DefaultAssessmentResultBuilder();
  }

  public static AssessmentResultBuilder newAssessmentResultBuilder(ResultStatusBehavior behavior) {
    return new DefaultAssessmentResultBuilder(behavior);
  }

  public static TestResult newTestResult(String testId, TestStatus result, Context context) {
    return new BasicTestResult(testId, result, context);
  }
}
