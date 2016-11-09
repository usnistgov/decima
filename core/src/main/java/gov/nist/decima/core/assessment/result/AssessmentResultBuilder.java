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

package gov.nist.decima.core.assessment.result;

import gov.nist.decima.core.assessment.Assessment;
import gov.nist.decima.core.requirement.DerivedRequirement;
import gov.nist.decima.core.requirement.RequirementsManager;

import java.util.Map;

/**
 * Implementations of this interface can be used to build an {@link AssessmentResults} based on
 * results reported to the builder during an assessment. An {@link AssessmentResultBuilder} is used by the various
 * {@link Assessment} implementations to track a collection of {@link TestResult} instances
 * generated during the assessment process. Once all assessments are completed, an
 * {@link AssessmentResults} instance can be generated using the {@link #build(RequirementsManager)}
 * method. Before calling the {@link DefaultAssessmentResultBuilder#build(RequirementsManager)},
 * callers must first call the {@link DefaultAssessmentResultBuilder#end()} method to designate the
 * end of the assessment run.
 * <p>
 * This interface supports marking specific derived requirements as tested by calling the
 * {@link #assignTestStatus(String, TestState)} and
 * {@link #assignTestStatus(DerivedRequirement, TestState)} methods. This can be useful to ensure
 * that the derived requirement {@link ResultStatus} does not get evaluated as
 * {@link ResultStatus#NOT_TESTED} when a TestResult has not been generated for the derived
 * requirement because there was no failure.
 */
public interface AssessmentResultBuilder {
  /**
   * Retrieves the mapping of derived requirement identifiers to {@link TestState}. To be tested the
   * derived requirement must: 1) have an associated {@link TestResult} provided by calling the
   * {@link #addTestResult(String, TestResult)} method, or 2) be declared as a specific
   * {@link TestState} by calling the {@link #assignTestStatus(String, TestState)} or
   * {@link #assignTestStatus(DerivedRequirement, TestState)} methods.
   * 
   * @return a map of derived requirement identifiers to the corresponding test status
   */
  Map<String, TestState> getTestStateByDerivedRequirementId();

  /**
   * Calling this method signals the start of the assessment. The assessment will be automatically
   * started the first time either {@link #addTestResult(String, TestResult)},
   * {@link #assignTestStatus(String, TestState)}, or
   * {@link #assignTestStatus(DerivedRequirement, TestState)} is called. Multiple calls to this
   * method will not change the start time from the initial time.
   * 
   * @return the same builder instance
   */
  AssessmentResultBuilder start();

  /**
   * Calling this method signals the end of the assessment.
   * 
   * @return the same builder instance
   */
  AssessmentResultBuilder end();

  /**
   * Appends a new {@link TestResult} to the collection of test results associated with the provided
   * derived requirement identifier.
   * 
   * @param derivedRequirementId
   *          the derived requirement that the test result is associated with
   * @param result
   *          the test result to append
   * @return the same builder instance
   */
  AssessmentResultBuilder addTestResult(String derivedRequirementId, TestResult result);

  /**
   * Marks the derived requirement associated with the provided identifier as having a specific
   * TestState. This method is called when the {@link #addTestResult(String, TestResult)} method is
   * called. If no test result is reported for a given derived requirement, then this method needs
   * to be called to indicate the actual test state of a derived requirement.
   * 
   * @param derivedRequirementId
   *          the derived requirement to set the state for
   * @param state
   *          the state to assign
   * @return the current builder instance
   */
  AssessmentResultBuilder assignTestStatus(String derivedRequirementId, TestState state);

  /**
   * Generates a new {@link AssessmentResults} instance based on the previously recorded
   * {@link TestResult} records and other statuses provided to the builder..
   * <p>
   * Note: {@link #end()} must be called before this method to terminate the assessment.
   * 
   * @param requirementsManager
   *          the set of requirements corresponding to the assessments performed
   * @return a new assessment result instance
   * @throws IllegalStateException
   *           if the {@link #end()} method hasn't first been called
   */
  AssessmentResults build(RequirementsManager requirementsManager);
}
