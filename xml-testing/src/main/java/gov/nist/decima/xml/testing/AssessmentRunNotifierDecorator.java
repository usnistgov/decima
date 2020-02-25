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

package gov.nist.decima.xml.testing;

import gov.nist.decima.core.assessment.Assessment;
import gov.nist.decima.core.assessment.result.AssessmentResultBuilder;
import gov.nist.decima.core.assessment.result.AssessmentResults;
import gov.nist.decima.core.assessment.result.TestResult;
import gov.nist.decima.core.assessment.result.TestState;
import gov.nist.decima.core.assessment.util.LoggingHandler;
import gov.nist.decima.core.document.Document;
import gov.nist.decima.core.requirement.RequirementsManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

import java.util.Objects;

public class AssessmentRunNotifierDecorator implements LoggingHandler {
  private static final Logger log = LogManager.getLogger(AssessmentRunNotifierDecorator.class);

  private final RunNotifier delegate;
  private final DescriptionResolver descriptionResolver;

  /**
   * Construct a new decorator that wraps a {@link RunNotifier} to allow AssessmentNotifier events to
   * generate JUnit run notifications.
   * 
   * @param delegate
   *          the JUnit {@link RunNotifier} to send events to
   * @param descriptionResolver
   *          the {@link DescriptionResolver} to use to determine the assessments JUnit description
   */
  public AssessmentRunNotifierDecorator(RunNotifier delegate, DescriptionResolver descriptionResolver) {
    Objects.requireNonNull(delegate, "delegate");
    Objects.requireNonNull(descriptionResolver, "descriptionResolver");
    this.delegate = delegate;
    this.descriptionResolver = descriptionResolver;
  }

  public RunNotifier getDelegate() {
    return delegate;
  }

  public DescriptionResolver getDescriptionResolver() {
    return descriptionResolver;
  }

  private Description getAssessmentDescription(Assessment<?> assessment) {
    DescriptionResolver resolver = getDescriptionResolver();
    return resolver.getDescription(assessment);
  }

  @Override
  public <DOC extends Document> void assessmentExecutionStarted(DOC document) {
  }

  @Override
  public <DOC extends Document> void assessmentExecutionCompleted(DOC document) {
  }

  @Override
  public <DOC extends Document> void assessmentStarted(Assessment<? extends DOC> assessment, DOC document) {
    if (log.isDebugEnabled()) {
      log.debug("Starting assessment: " + assessment.getName(true));
    }
    getDelegate().fireTestStarted(getAssessmentDescription(assessment));
  }

  @Override
  public <DOC extends Document> void assessmentCompleted(Assessment<? extends DOC> assessment, DOC document) {
    if (log.isDebugEnabled()) {
      log.debug("Completing assessment: " + assessment.getName(false));
    }
    getDelegate().fireTestFinished(getAssessmentDescription(assessment));
  }

  @Override
  public <DOC extends Document> void assessmentError(Assessment<? extends DOC> assessment, DOC document, Throwable th) {
    if (log.isDebugEnabled()) {
      log.error("An error occured during the assessment: " + assessment.getName(false));
    }
    getDelegate().fireTestFailure(new Failure(getAssessmentDescription(assessment), th));
  }

  @Override
  public <DOC extends Document> void addTestResult(Assessment<? extends DOC> assessment, DOC document,
      String derivedRequirementId, TestResult result) {
  }

  @Override
  public <DOC extends Document> void assignTestStatus(Assessment<? extends DOC> assessment, DOC document,
      String derivedRequirementId, TestState state) {
  }

  @Override
  public void validationStarted() {
  }

  @Override
  public void validationEnded(AssessmentResultBuilder builder) {
  }

  @Override
  public void producingResults(AssessmentResultBuilder builder, RequirementsManager requirementsManager) {
  }

  @Override
  public void completedResults(AssessmentResultBuilder builder, RequirementsManager requirementsManager,
      AssessmentResults results) {
  }

}
