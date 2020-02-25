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

package gov.nist.decima.core.assessment;

import gov.nist.decima.core.assessment.result.AssessmentResultBuilder;
import gov.nist.decima.core.assessment.result.AssessmentResults;
import gov.nist.decima.core.assessment.result.TestResult;
import gov.nist.decima.core.document.Document;

import java.util.List;

/**
 * This interface represents an assessment, a series of tests used to check the well-formedness and
 * content-completeness of an XML document. Implementations of this interface must provide the
 * assessment logic used to perform the assessment. The results of the assessment are provided using
 * a {@link AssessmentResultBuilder} by adding a number of {@link TestResult} instances using the
 * {@link AssessmentResultBuilder#addTestResult(Assessment, Document, String, TestResult)} method.
 * The {@link AssessmentResultBuilder} instance is then capable of producing an
 * {@link AssessmentResults} object via the
 * {@link AssessmentResultBuilder#build(gov.nist.decima.core.requirement.RequirementsManager)}
 * method once all required assessments have been performed.
 * <p>
 * If an assessment needs to be conducted conditionally based on an XPath expression, then
 * implementations should also implement the {@link ConditionalAssessment} feature interface.
 * <p>
 * Implementations of this interface are expected to be thread safe when the
 * {@link #execute(Document, AssessmentResultBuilder)} method is invoked from different calling
 * contexts.
 * 
 * @param <DOC>
 *          the type of document that is the target of the assessment
 */
public interface Assessment<DOC extends Document> {
  /**
   * Returns a stable identifier indicating the type of assessment being performed.
   * 
   * @return an identifier
   */
  String getAssessmentType();

  /**
   * Executes the assessment, which then generates input into a {@link AssessmentResults} using the
   * provided {@link AssessmentResultBuilder}.
   * 
   * @param document
   *          the XML document to assess
   * @param builder
   *          the {@link AssessmentResultBuilder} used to eventually generate a
   *          {@link AssessmentResults} once all assessments have been performed
   * @throws AssessmentException
   *           if an error occurs while performing the assessment
   */
  void execute(DOC document, AssessmentResultBuilder builder) throws AssessmentException;

  /**
   * Retrieves a human-readable label for the assessment.
   * 
   * @param includeDetail
   *          if {@code true}, include identifying details
   * @return a human-readable label for the assessment.
   */
  String getName(boolean includeDetail);

  List<Assessment<DOC>> getExecutableAssessments(DOC document) throws AssessmentException;
}
