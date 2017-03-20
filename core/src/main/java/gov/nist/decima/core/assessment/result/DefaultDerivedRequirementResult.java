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

import gov.nist.decima.core.requirement.DerivedRequirement;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class DefaultDerivedRequirementResult extends AbstractRequirementResult implements DerivedRequirementResult {
  private final DerivedRequirement derivedRequirement;
  private final List<TestResult> assertionResults = new LinkedList<>();

  /**
   * Construct a new derived requirement result with a specific result status.
   * 
   * @param derivedRequirement
   *          the derived requirement to associate this result with
   * @param status
   *          the initial status to assign
   */
  public DefaultDerivedRequirementResult(DerivedRequirement derivedRequirement, ResultStatus status) {
    super(status);
    Objects.requireNonNull(derivedRequirement, "derivedRequirement");
    this.derivedRequirement = derivedRequirement;
  }

  @Override
  public DerivedRequirement getDerivedRequirement() {
    return derivedRequirement;
  }

  @Override
  public List<TestResult> getTestResults() {
    return Collections.unmodifiableList(assertionResults);
  }

  /**
   * Appends to this result a result instance generated while conducting a specific derived
   * requirement test.
   * 
   * @param result
   *          the result to append
   */
  public void addTestResult(TestResult result) {
    assertionResults.add(result);

    TestStatus testStatus = result.getStatus();
    DerivedRequirement derived = getDerivedRequirement();

    ResultStatus newStatus = derived.getType().resolveTestResult(testStatus, derived.isConditional());
    if (newStatus.ordinal() > getStatus().ordinal()) {
      setStatus(newStatus);
    }
  }

  /**
   * A convenience method that iterates over the list and calls {@link #addTestResult(TestResult)}.
   * 
   * @param assertionResults
   *          the results to append
   */
  public void addTestResults(List<TestResult> assertionResults) {
    for (TestResult result : assertionResults) {
      addTestResult(result);
    }
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("id", getDerivedRequirement().getId())
        .append("status", getStatus()).toString();
  }
}
