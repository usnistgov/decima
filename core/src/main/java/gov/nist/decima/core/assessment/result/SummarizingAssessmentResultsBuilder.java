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

import gov.nist.decima.core.document.Document;
import gov.nist.decima.core.requirement.RequirementsManager;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class SummarizingAssessmentResultsBuilder implements AssessmentResultBuilder {
  private final AssessmentResultBuilder delegate;
  private final EnumMap<TestStatus, Integer> testResultStatusToCountMap = new EnumMap<>(TestStatus.class);
  private int testResultCount = 0;
  private final Map<String, TestState> derivedRequirementIdsToStateMap = new HashMap<>();
  private final Map<String, TestStatus> derivedRequirementIdToTestStatusMap = new HashMap<>();

  public SummarizingAssessmentResultsBuilder(AssessmentResultBuilder delegate) {
    this.delegate = delegate;
  }

  public AssessmentResultBuilder getDelegate() {
    return delegate;
  }

  @Override
  public Map<String, TestState> getTestStateByDerivedRequirementId() {
    return delegate.getTestStateByDerivedRequirementId();
  }

  @Override
  public AssessmentResultBuilder start() {
    delegate.start();
    return this;
  }

  @Override
  public AssessmentResultBuilder end() {
    delegate.end();
    return this;
  }

  @Override
  public AssessmentResultBuilder addAssessmentTarget(Document document) {
    return delegate.addAssessmentTarget(document);
  }

  @Override
  public synchronized AssessmentResultBuilder addTestResult(String derivedRequirementId, TestResult result) {
    delegate.addTestResult(derivedRequirementId, result);

    TestStatus status = result.getStatus();
    updateState(derivedRequirementId, TestState.TESTED);
    updateStatus(derivedRequirementId, status);

    // increment the status
    Integer count = testResultStatusToCountMap.get(status);
    if (count == null) {
      count = 0;
    }
    count++;
    testResultStatusToCountMap.put(status, count);

    // increment the count of reported TestResult instances
    testResultCount++;
    return this;
  }

  private void updateStatus(String derivedRequirementId, TestStatus status) {
    // record the derived requirement result
    TestStatus oldStatus = derivedRequirementIdToTestStatusMap.get(derivedRequirementId);
    if (oldStatus == null || oldStatus.ordinal() < status.ordinal()) {
      derivedRequirementIdToTestStatusMap.put(derivedRequirementId, status);
    }
  }

  private void updateState(String derivedRequirementId, TestState state) {
    // record the derived requirement result
    TestState oldState = derivedRequirementIdsToStateMap.get(derivedRequirementId);
    if (oldState == null || oldState.ordinal() < state.ordinal()) {
      derivedRequirementIdsToStateMap.put(derivedRequirementId, state);
    }
  }

  @Override
  public AssessmentResultBuilder assignTestStatus(String derivedRequirementId, TestState state) {
    delegate.assignTestStatus(derivedRequirementId, state);
    updateState(derivedRequirementId, state);
    return this;
  }

  /**
   * For each {@link TestState}, count the number of derived requirements that have that specific
   * {@link TestState}.
   * 
   * @return a mapping of {@link TestState} values to the count of derived requirements that have
   *         that specific {@link TestState}
   */
  public synchronized Map<TestState, Integer> getDerivedRequirementStateCount() {
    Map<TestState, Integer> retval = new EnumMap<>(TestState.class);
    for (Map.Entry<String, TestState> entry : derivedRequirementIdsToStateMap.entrySet()) {

      TestState state = entry.getValue();
      Integer count = retval.get(state);
      if (count == null) {
        count = 0;
      }
      retval.put(state, ++count);
    }
    return retval;
  }

  /**
   * For each {@link TestStatus}, count the number of derived requirements that have that specific
   * {@link TestStatus}.
   * 
   * @return a mapping of {@link TestStatus} values to the count of derived requirements that have
   *         that specific {@link TestStatus}
   */
  public synchronized Map<TestStatus, Integer> getDerivedRequirementStatusCount() {
    Map<TestStatus, Integer> retval = new EnumMap<>(TestStatus.class);
    for (Map.Entry<String, TestState> entry : derivedRequirementIdsToStateMap.entrySet()) {
      if (!TestState.TESTED.equals(entry.getValue())) {
        continue;
      }

      String derivedRequirementId = entry.getKey();
      TestStatus status = derivedRequirementIdToTestStatusMap.get(derivedRequirementId);
      if (status == null) {
        status = TestStatus.PASS;
      }
      Integer count = retval.get(status);
      if (count == null) {
        count = 0;
      }
      retval.put(status, ++count);
    }
    return retval;
  }

  public synchronized Map<TestStatus, Integer> getTestResultStatusCount() {
    return Collections.unmodifiableMap(testResultStatusToCountMap);
  }

  public synchronized int getTestResultCount() {
    return testResultCount;
  }

  @Override
  public AssessmentResults build(RequirementsManager requirementsManager) {
    return delegate.build(requirementsManager);
  }

}
