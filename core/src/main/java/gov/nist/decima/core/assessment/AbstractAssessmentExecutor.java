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

package gov.nist.decima.core.assessment;

import gov.nist.decima.core.assessment.result.AssessmentResultBuilder;
import gov.nist.decima.core.assessment.util.LoggingHandler;
import gov.nist.decima.core.document.Document;
import gov.nist.decima.core.util.ObjectUtil;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Provides common assessment execution functions to support concrete {@link AssessmentExecutor}
 * implementations.
 */
public abstract class AbstractAssessmentExecutor<DOC extends Document> implements AssessmentExecutor<DOC> {
  private final List<? extends Assessment<DOC>> assessments;

  public AbstractAssessmentExecutor(List<? extends Assessment<DOC>> assessments) {
    ObjectUtil.requireNonEmpty(assessments, "assessments");
    this.assessments = Collections.unmodifiableList(assessments);
  }

  /**
   * Retrieves the assessments configured for this executor.
   * 
   * @return a list of assessments
   */
  public List<? extends Assessment<DOC>> getAssessments() {
    return Collections.unmodifiableList(assessments);
  }

  protected List<Assessment<DOC>> getExecutableAssessments(DOC targetDocument) throws AssessmentException {
    return AssessmentExecutionHelper.getExecutableAssessments(targetDocument, getAssessments());
  }

  @Override
  public void execute(DOC documentToAssess, AssessmentResultBuilder resultBuilder) throws AssessmentException {
    Objects.requireNonNull(documentToAssess, "documentToAssess");
    Objects.requireNonNull(resultBuilder, "resultBuilder");

    resultBuilder.start();

    LoggingHandler handler = resultBuilder.getLoggingHandler();

    handler.assessmentExecutionStarted(documentToAssess);

    executeInternal(documentToAssess, resultBuilder);

    handler.assessmentExecutionCompleted(documentToAssess);
  }

  protected void executeInternal(DOC documentToAssess, AssessmentResultBuilder resultBuilder)
      throws AssessmentException {
    for (Assessment<DOC> assessment : getExecutableAssessments(documentToAssess)) {
      AssessmentExecutionHelper.executeAssessment(assessment, documentToAssess, resultBuilder);
    }

  }
}
