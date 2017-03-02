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

package gov.nist.decima.core.assessment.util;

import gov.nist.decima.core.assessment.Assessment;
import gov.nist.decima.core.assessment.result.AssessmentResultBuilder;
import gov.nist.decima.core.assessment.result.AssessmentResults;
import gov.nist.decima.core.assessment.result.TestResult;
import gov.nist.decima.core.assessment.result.TestState;
import gov.nist.decima.core.document.Document;
import gov.nist.decima.core.requirement.RequirementsManager;

public class NoOpLoggingHandler implements LoggingHandler {
  private static final LoggingHandler INSTANCE = new NoOpLoggingHandler();

  public static LoggingHandler instance() {
    return INSTANCE;
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
  public <DOC extends Document> void assessmentExecutionStarted(DOC document) {
  }

  @Override
  public <DOC extends Document> void assessmentExecutionCompleted(DOC document) {
  }

  @Override
  public <DOC extends Document> void assessmentStarted(Assessment<? extends DOC> assessment, DOC document) {
  }

  @Override
  public <DOC extends Document> void assessmentCompleted(Assessment<? extends DOC> assessment, DOC document) {
  }

  @Override
  public <DOC extends Document> void assessmentError(Assessment<? extends DOC> assessment, DOC document, Throwable th) {
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
