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

import gov.nist.decima.core.assessment.result.SummarizingAssessmentResultsBuilder;
import gov.nist.decima.core.document.Document;

public interface AssessmentNotifier<DOC extends Document> {
  /**
   * Signals that an execution of one or more assessments has started.
   * 
   * @param document
   *          the document being assessed
   */
  void assessmentExecutionStarted(DOC document);

  /**
   * Signals that an execution of one or more assessments has ended.
   * 
   * @param document
   *          the document being assessed
   */
  void assessmentExecutionCompleted(DOC document);

  /**
   * Signals that evaluation of the target assessment started.
   * 
   * @param assessment
   *          the target assessment
   * @param document
   *          the document being assessed
   */
  void assessmentStarted(Assessment<DOC> assessment, DOC document);

  /**
   * Signals that evaluation of the target assessment completed successfully.
   * 
   * @param assessment
   *          the target assessment
   * @param document
   *          the document being assessed
   * @param summary
   *          the summary for this assessment, or {@code null} if {@link #isProvideSummary()}
   *          returns {@code false}
   */
  void assessmentCompleted(Assessment<DOC> assessment, DOC document, SummarizingAssessmentResultsBuilder summary);

  /**
   * Signals that evaluation of the target assessment resulted in an error.
   * 
   * @param assessment
   *          the target assessment
   * @param document
   *          the document being assessed
   * @param th
   *          the error that was thrown, or {@code null} if no exception was thrown
   */
  void assessmentError(Assessment<DOC> assessment, DOC document, Throwable th);

  boolean isProvideSummary();
}
