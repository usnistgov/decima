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

package gov.nist.decima.core.assessment.logic;

import gov.nist.decima.core.assessment.AbstractAssessment;
import gov.nist.decima.core.assessment.AssessmentException;
import gov.nist.decima.core.assessment.result.AssessmentResultBuilder;
import gov.nist.decima.core.assessment.result.TestState;
import gov.nist.decima.core.document.Document;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is a stub for assessment logic handled in code.
 */
public abstract class AbstractLogicAssessment<DOC extends Document> extends AbstractAssessment<DOC>
    implements LogicAssessment<DOC> {
  private static final Logger log = LogManager.getLogger(AbstractLogicAssessment.class);
  private static final String ASSESSMENT_TYPE = "Logic";

  @Override
  public String getAssessmentType() {
    return ASSESSMENT_TYPE;
  }

  @Override
  protected void executeInternal(DOC document, AssessmentResultBuilder builder) throws AssessmentException {
    log.debug("[{}]Executing logic assessment", getId());
    doAssessment(document, builder);
    log.debug("[{}]Logic assessment complete", getId());
  }

  /**
   * When executing the assessessment, the
   * {@link AssessmentResultBuilder#assignTestStatus(gov.nist.decima.core.assessment.Assessment, Document, String, TestState)}
   * method must be called on the builder instance to identify the tested state of all covered
   * derived requirements. If this is not done, the requirement will default to the result state of
   * {@link TestState#NOT_APPLICABLE}.
   * 
   * @param document
   *          the {@link Document} instance to assess
   * @param builder
   *          the {@link AssessmentResultBuilder} to append assessment results to
   * @throws AssessmentException
   *           if an error occurs while performing the assessment
   */
  protected abstract void doAssessment(DOC document, AssessmentResultBuilder builder) throws AssessmentException;
}
