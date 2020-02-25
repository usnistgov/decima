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

package gov.nist.secauto.decima.core.assessment;

import gov.nist.secauto.decima.core.assessment.result.AssessmentResultBuilder;
import gov.nist.secauto.decima.core.assessment.util.LoggingHandler;
import gov.nist.secauto.decima.core.document.Document;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class AssessmentExecutionHelper {

  /**
   * Performs the provided assessment over the provided document recording assessment results using
   * the provided builder.
   *
   * @param <DOC>
   *          the {@link Document} type used as the target of the assessment
   * @param assessment
   *          the assessment to perform
   * @param assessmentTarget
   *          the document to perform the assessment over
   * @param builder
   *          a non-null result builder instance
   * @throws AssessmentException
   *           if an error occurs while performing the assessment
   */
  public static <DOC extends Document> void executeAssessment(Assessment<DOC> assessment, DOC assessmentTarget,
      AssessmentResultBuilder builder) throws AssessmentException {

    LoggingHandler handler = builder.getLoggingHandler();

    handler.assessmentStarted(assessment, assessmentTarget);
    try {
      assessment.execute(assessmentTarget, builder);
      handler.assessmentCompleted(assessment, assessmentTarget);
    } catch (AssessmentException ex) {
      handler.assessmentError(assessment, assessmentTarget, ex);
      throw ex;
    } catch (Throwable th) {
      handler.assessmentError(assessment, assessmentTarget, th);
      throw new AssessmentException(
          "An unexpected error occured while processing the assessment: " + assessment.getName(false), th);
    }
  }

  /**
   * Retrieves the sequence of executable {@link Assessment} instances for a given collection of
   * assessments for the provided target {@link Document}.
   * 
   * @param <DOC>
   *          the {@link Document} type used as the target of the assessment
   * @param targetDocument
   *          the {@link Document} that the assessments will be eventually executed against
   * @param assessments
   *          a sequence of {@link Assessment} instances to process
   * @return a list of executable {@link Assessment} instances
   * @throws AssessmentException
   *           if an error occurred while determining the {@link Assessment} instances that executable
   */
  public static <DOC extends Document> List<Assessment<DOC>> getExecutableAssessments(DOC targetDocument,
      List<? extends Assessment<DOC>> assessments) throws AssessmentException {

    List<Assessment<DOC>> retval = new LinkedList<>();
    for (Assessment<DOC> assessment : assessments) {
      retval.addAll(assessment.getExecutableAssessments(targetDocument));
    }
    return Collections.unmodifiableList(retval);
  }

}
