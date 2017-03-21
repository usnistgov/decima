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
package gov.nist.decima.core.assessment.util;

import gov.nist.decima.core.assessment.Assessment;
import gov.nist.decima.core.assessment.result.TestResult;
import gov.nist.decima.core.assessment.result.TestState;
import gov.nist.decima.core.assessment.result.TestStatus;
import gov.nist.decima.core.document.Document;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class AssessmentSummarizingLoggingHandler extends AbstractDelegatingLoggingHandler {
  private static final Logger log = LogManager.getLogger(AssessmentSummarizingLoggingHandler.class);
  private final Map<Assessment<?>, AssessmentStatsImpl> assessmentToStatsMap
      = Collections.synchronizedMap(new HashMap<>());
  private final Level summaryLogLevel;

  public AssessmentSummarizingLoggingHandler(Level summaryLogLevel) {
    this(summaryLogLevel, null);
  }

  public AssessmentSummarizingLoggingHandler(Level summaryLogLevel, LoggingHandler delegate) {
    super(delegate);
    this.summaryLogLevel = summaryLogLevel;
  }

  /**
   * Retrieve the logging level to use when logging.
   * 
   * @return the summaryLogLevel
   */
  public Level getSummaryLogLevel() {
    return summaryLogLevel;
  }

  @Override
  public <DOC extends Document> void assessmentStarted(Assessment<? extends DOC> assessment, DOC document) {
    super.assessmentStarted(assessment, document);

    // add new stats for the assessment
    addAssessmentStats(assessment);
  }

  @Override
  public <DOC extends Document> void assessmentError(Assessment<? extends DOC> assessment, DOC document, Throwable th) {
    super.assessmentError(assessment, document, th);

    // the assessment didn't complete, but ended in error
    removeAssessmentStats(assessment);
  }

  @Override
  public synchronized <DOC extends Document> void addTestResult(Assessment<? extends DOC> assessment, DOC document,
      String derivedRequirementId, TestResult result) {
    super.addTestResult(assessment, document, derivedRequirementId, result);

    AssessmentStatsImpl stats = getAssessmentStatsInternal(assessment);
    if (stats == null) {
      throw new IllegalStateException("Must call assessmentStarted before reporting test results");
    }
    stats.addTestResult(derivedRequirementId, result);
  }

  @Override
  public <DOC extends Document> void assignTestStatus(Assessment<? extends DOC> assessment, DOC document,
      String derivedRequirementId, TestState state) {
    super.assignTestStatus(assessment, document, derivedRequirementId, state);

    AssessmentStatsImpl stats = getAssessmentStatsInternal(assessment);
    if (stats == null) {
      throw new IllegalStateException("Must call assessmentStarted before reporting test status");
    }
    stats.assignTestStatus(derivedRequirementId, state);
  }

  @Override
  public <DOC extends Document> void assessmentCompleted(Assessment<? extends DOC> assessment, DOC document) {
    super.assessmentCompleted(assessment, document);

    if (!isProvideSummary(assessment, document)) {
      return;
    }

    AssessmentStatsImpl stats = getAssessmentStatsInternal(assessment);
    if (stats == null) {
      throw new IllegalStateException("Must call assessmentStarted before completing the assessment");
    }

    Integer tested = stats.getDerivedRequirementStateCount().get(TestState.TESTED);
    if (tested == null) {
      tested = 0;
    }
    if (tested > 0) {
      Map<TestStatus, Integer> counts = stats.getDerivedRequirementStatusCount();
      Integer countPass = counts.get(TestStatus.PASS);
      Integer countWarning = counts.get(TestStatus.WARNING);
      Integer countFails = counts.get(TestStatus.FAIL);
      Integer countInfo = counts.get(TestStatus.INFORMATIONAL);

      log.log(getSummaryLogLevel(),
          "{}: Checked {} derived requirements with {} PASS, {} WARNING, {} FAIL, and {} INFORMATIONAL",
          assessment.getName(false), tested, countPass == null ? 0 : countPass, countWarning == null ? 0 : countWarning,
          countFails == null ? 0 : countFails, countInfo == null ? 0 : countInfo);
    } else {
      log.log(getSummaryLogLevel(), "{}: No requirements were checked", assessment.getName(false));
    }
  }

  protected <DOC extends Document> boolean isProvideSummary(Assessment<? extends DOC> assessment, DOC document) {
    return true;
  }

  private AssessmentStats addAssessmentStats(Assessment<?> assessment) {
    AssessmentStatsImpl retval = new AssessmentStatsImpl();
    if (assessmentToStatsMap.put(assessment, retval) != null) {
      throw new IllegalStateException("Assessment has already been added");
    }
    return retval;
  }

  private AssessmentStats removeAssessmentStats(Assessment<?> assessment) {
    AssessmentStats retval = assessmentToStatsMap.remove(assessment);
    if (retval == null) {
      throw new IllegalStateException("Assessment not found added");
    }
    return retval;
  }

  public AssessmentStats getAssessmentStats(Assessment<?> assessment) {
    return getAssessmentStatsInternal(assessment);
  }

  protected AssessmentStatsImpl getAssessmentStatsInternal(Assessment<?> assessment) {
    return assessmentToStatsMap.get(assessment);
  }

  private static class AssessmentStatsImpl implements AssessmentStats {
    private final EnumMap<TestStatus, Integer> testResultStatusToCountMap = new EnumMap<>(TestStatus.class);
    private int testResultCount = 0;
    private final Map<String, TestState> derivedRequirementIdsToStateMap = new HashMap<>();
    private final Map<String, TestStatus> derivedRequirementIdToTestStatusMap = new HashMap<>();

    public void addTestResult(String derivedRequirementId, TestResult result) {

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

    }

    public void assignTestStatus(String derivedRequirementId, TestState state) {
      updateState(derivedRequirementId, state);
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

    /*
     * (non-Javadoc)
     * 
     * @see gov.nist.decima.core.assessment.util.AssessmentStats#getDerivedRequirementStateCount()
     */
    @Override
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
      return Collections.unmodifiableMap(retval);
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nist.decima.core.assessment.util.AssessmentStats#getDerivedRequirementStatusCount()
     */
    @Override
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
      return Collections.unmodifiableMap(retval);
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nist.decima.core.assessment.util.AssessmentStats#getTestResultStatusCount()
     */
    @Override
    public synchronized Map<TestStatus, Integer> getTestResultStatusCount() {
      return Collections.unmodifiableMap(testResultStatusToCountMap);
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nist.decima.core.assessment.util.AssessmentStats#getTestResultCount()
     */
    @Override
    public synchronized int getTestResultCount() {
      return testResultCount;
    }
  }
}
