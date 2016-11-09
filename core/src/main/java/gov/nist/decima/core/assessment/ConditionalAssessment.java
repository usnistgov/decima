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

import gov.nist.decima.core.Decima;
import gov.nist.decima.core.document.Document;

/**
 * A feature interface that indicates that an assessment can be conditionally applied if the target
 * document matches an expected criteria. This is useful if the assessment is only valid if specific
 * document conditions are met (e.g., specific root element/namespace, specific document version,
 * etc.)
 * <p>
 * Any {@link Assessment} instance can be decorated as a {@link ConditionalAssessment} using the
 * {@link Decima#newConditionalAssessment(Assessment, Condition)} factory method.
 */
public interface ConditionalAssessment<DOC extends Document> extends Assessment<DOC> {
  /**
   * Called to check if the assessment applies to a given document. The provided XPath evaluation
   * context should be used to perform the applicability check.
   * 
   * @param targetDocument
   *          the document to check applicability with
   * @return {@code true} if the assessment applies to the document or {@code false} otherwise
   * @throws AssessmentException
   *           if an error occurs while evaluating the condition
   */
  boolean appliesTo(DOC targetDocument) throws AssessmentException;

}
