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

package gov.nist.decima.core.assessment.util;

import gov.nist.decima.core.assessment.Assessment;
import gov.nist.decima.core.assessment.result.TestState;
import gov.nist.decima.core.assessment.result.TestStatus;
import gov.nist.decima.core.document.Document;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class AssessmentLoggingAssessmentNotifier<DOC extends Document>
    extends AbstractLoggingAssessmentNotifier<DOC> {
  private static final Logger log = LogManager.getLogger(AssessmentLoggingAssessmentNotifier.class);
  private static final AssessmentLoggingAssessmentNotifier<?> INSTANCE = new AssessmentLoggingAssessmentNotifier<>();

  /**
   * Retrieve the singleton notifier instance.
   * 
   * @return the singleton instance
   */
  public static <DOC extends Document> AssessmentLoggingAssessmentNotifier<DOC> instance() {
    @SuppressWarnings("unchecked")
    AssessmentLoggingAssessmentNotifier<DOC> retval = (AssessmentLoggingAssessmentNotifier<DOC>) INSTANCE;
    return retval;
  }

  public AssessmentLoggingAssessmentNotifier() {
    this(Level.DEBUG);
  }

  public AssessmentLoggingAssessmentNotifier(Level summaryLogLevel) {
    super(summaryLogLevel);
  }

  @Override
  public void assessmentExecutionStarted(DOC document) {
    log.info("Starting assessment execution");
  }

  @Override
  public void assessmentExecutionCompleted(DOC document) {
    log.info("Assessment execution completed");
  }

  @Override
  public void assessmentStarted(Assessment<DOC> assessment, DOC document) {
    if (log.isInfoEnabled()) {
      log.info("Executing assessment: " + assessment.getName(true));
    }
  }

  @Override
  public void assessmentCompleted(Assessment<DOC> assessment, DOC document,
      SummarizingAssessmentResultsBuilder summary) {
    if (log.isInfoEnabled()) {
      log.info("Assessment completed: {}", assessment.getName(false));
    }

    if (isProvideSummary(assessment, document)) {
      Integer tested = summary.getDerivedRequirementStateCount().get(TestState.TESTED);
      if (tested == null) {
        tested = 0;
      }
      if (tested > 0) {
        Map<TestStatus, Integer> counts = summary.getDerivedRequirementStatusCount();
        Integer countPass = counts.get(TestStatus.PASS);
        Integer countWarning = counts.get(TestStatus.WARNING);
        Integer countFails = counts.get(TestStatus.FAIL);
        Integer countInfo = counts.get(TestStatus.INFORMATIONAL);
        log.log(getSummaryLevel(),
            "{}: Checked {} derived requirements with {} PASS, {} WARNING, {} FAIL, and {} INFORMATIONAL",
            assessment.getName(false), tested, countPass == null ? 0 : countPass,
            countWarning == null ? 0 : countWarning, countFails == null ? 0 : countFails,
            countInfo == null ? 0 : countInfo);
      } else {
        log.log(getSummaryLevel(), "{}: No requirements were checked", assessment.getName(false));
      }
    }
  }

  @Override
  public void assessmentError(Assessment<DOC> assessment, DOC document, Throwable th) {
    if (log.isErrorEnabled()) {
      log.error("Error performing assessment: " + assessment.getName(false), th);
    }
  }

  @Override
  public boolean isProvideSummary(Assessment<DOC> assessment, DOC document) {
    return log.isEnabled(getSummaryLevel());
  }

}
