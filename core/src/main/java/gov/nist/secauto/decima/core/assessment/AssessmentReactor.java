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

import gov.nist.secauto.decima.core.Decima;
import gov.nist.secauto.decima.core.assessment.result.AssessmentResultBuilder;
import gov.nist.secauto.decima.core.assessment.result.AssessmentResults;
import gov.nist.secauto.decima.core.document.Document;
import gov.nist.secauto.decima.core.requirement.RequirementsManager;

import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

public class AssessmentReactor {
  private final RequirementsManager requirementsManager;
  private final Queue<AssessmentExecution<?>> assessmentExecutions = new LinkedList<>();

  public AssessmentReactor(RequirementsManager requirementsManager) {
    Objects.requireNonNull(requirementsManager, "requirementsManager");
    this.requirementsManager = requirementsManager;
  }

  public RequirementsManager getRequirementsManager() {
    return requirementsManager;
  }

  public synchronized <DOC extends Document> AssessmentReactor pushAssessmentExecution(DOC document,
      AssessmentExecutor<DOC> executor) {
    assessmentExecutions.add(new AssessmentExecution<DOC>(document, executor));
    return this;
  }

  /**
   * Conducts all queued assessments.
   * 
   * @return a set of assessment results based on the evaluated assessments
   * @throws AssessmentException
   *           if an error occurred while conducting the assessments
   */
  public AssessmentResults react() throws AssessmentException {
    return react(newAssessmentResultBuilder());
  }

  /**
   * Conducts all queued assessments.
   * 
   * @param builder
   *          the {@link AssessmentResultBuilder} to append results to
   * @return a set of assessment results based on the evaluated assessments
   * @throws AssessmentException
   *           if an error occurred while conducting the assessments
   */
  public AssessmentResults react(AssessmentResultBuilder builder) throws AssessmentException {
    builder.start();

    synchronized (this) {

      while (!assessmentExecutions.isEmpty()) {
        AssessmentExecution<?> execution = assessmentExecutions.poll();
        execution.execute(builder);
      }
    }

    return builder.end().build(getRequirementsManager());
  }

  protected AssessmentResultBuilder newAssessmentResultBuilder() {
    return Decima.newAssessmentResultBuilder();
  }
}
