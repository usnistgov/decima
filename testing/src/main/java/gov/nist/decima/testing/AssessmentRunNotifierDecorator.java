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

package gov.nist.decima.testing;

import gov.nist.decima.core.assessment.Assessment;
import gov.nist.decima.core.assessment.util.AssessmentNotifier;
import gov.nist.decima.core.assessment.util.SummarizingAssessmentResultsBuilder;
import gov.nist.decima.core.document.Document;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

import java.util.Objects;

public class AssessmentRunNotifierDecorator<DOC extends Document> implements AssessmentNotifier<DOC> {
  private static final Logger log = LogManager.getLogger(AssessmentRunNotifierDecorator.class);

  private final RunNotifier delegate;
  private final DescriptionResolver<DOC> descriptionResolver;

  /**
   * Construct a new decorator that wraps a {@link RunNotifier} to allow AssessmentNotifier events
   * to generate JUnit run notifications.
   * 
   * @param delegate
   *          the JUnit {@link RunNotifier} to send events to
   * @param descriptionResolver
   *          the {@link DescriptionResolver} to use to determine the assessments JUnit description
   */
  public AssessmentRunNotifierDecorator(RunNotifier delegate, DescriptionResolver<DOC> descriptionResolver) {
    Objects.requireNonNull(delegate, "delegate");
    Objects.requireNonNull(descriptionResolver, "descriptionResolver");
    this.delegate = delegate;
    this.descriptionResolver = descriptionResolver;
  }

  public RunNotifier getDelegate() {
    return delegate;
  }

  public DescriptionResolver<DOC> getDescriptionResolver() {
    return descriptionResolver;
  }

  private Description getAssessmentDescription(Assessment<DOC> assessment) {
    return getDescriptionResolver().getDescription(assessment);
  }

  @Override
  public void assessmentExecutionStarted(DOC document) {
  }

  @Override
  public void assessmentExecutionCompleted(DOC document) {
  }

  @Override
  public void assessmentStarted(Assessment<DOC> assessment, DOC document) {
    if (log.isDebugEnabled()) {
      log.debug("Starting assessment: " + assessment.getName(true));
    }
    getDelegate().fireTestStarted(getAssessmentDescription(assessment));
  }

  @Override
  public void assessmentCompleted(Assessment<DOC> assessment, DOC document,
      SummarizingAssessmentResultsBuilder summary) {
    if (log.isDebugEnabled()) {
      log.debug("Completing assessment: " + assessment.getName(false));
    }
    getDelegate().fireTestFinished(getAssessmentDescription(assessment));
  }

  @Override
  public void assessmentError(Assessment<DOC> assessment, DOC document, Throwable th) {
    if (log.isDebugEnabled()) {
      log.error("An error occured during the assessment: " + assessment.getName(false));
    }
    getDelegate().fireTestFailure(new Failure(getAssessmentDescription(assessment), th));
  }

  @Override
  public boolean isProvideSummary(Assessment<DOC> assessment, DOC document) {
    return false;
  }
}
