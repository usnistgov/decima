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

package gov.nist.secauto.decima.core.assessment.util;

import gov.nist.secauto.decima.core.assessment.result.AssessmentResultBuilder;
import gov.nist.secauto.decima.core.assessment.result.AssessmentResults;
import gov.nist.secauto.decima.core.assessment.result.BaseRequirementResult;
import gov.nist.secauto.decima.core.assessment.result.DerivedRequirementResult;
import gov.nist.secauto.decima.core.assessment.result.ResultStatus;
import gov.nist.secauto.decima.core.requirement.RequirementsManager;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumMap;

public class OverallSummaryLoggingHandler extends AbstractDelegatingLoggingHandler {
  private static final Logger log = LogManager.getLogger(AssessmentSummarizingLoggingHandler.class);
  private final Level summaryLevel;

  public OverallSummaryLoggingHandler(Level summaryLogLevel) {
    this(summaryLogLevel, null);
  }

  public OverallSummaryLoggingHandler(Level level, LoggingHandler delegate) {
    super(delegate);
    this.summaryLevel = level;
  }

  /**
   * Retrieve the logging level to be used to log result information.
   * 
   * @return the summaryLevel
   */
  public Level getSummaryLevel() {
    return summaryLevel;
  }

  @Override
  public void completedResults(AssessmentResultBuilder builder, RequirementsManager requirementsManager,
      AssessmentResults results) {
    super.completedResults(builder, requirementsManager, results);

    logDerivedRequirements(results);
    logOverallSummary(results);
  }

  private void logDerivedRequirements(AssessmentResults results) {
    int testedCount = 0;
    EnumMap<ResultStatus, Integer> counts = new EnumMap<ResultStatus, Integer>(ResultStatus.class);

    for (BaseRequirementResult baseResult : results.getBaseRequirementResults()) {
      for (DerivedRequirementResult derivedResult : baseResult.getDerivedRequirementResults()) {
        ResultStatus status = derivedResult.getStatus();
        Integer count = counts.get(status);
        if (count == null) {
          count = 0;
        }
        counts.put(status, ++count);

        if (ResultStatus.INFORMATIONAL.compareTo(status) <= 0) {
          ++testedCount;
        }
      }
    }
    log.log(getSummaryLevel(),
        "Checked {} derived requirements having {} PASS, {} WARNING, {} FAIL, and {} INFORMATIONAL results",
        testedCount, counts.getOrDefault(ResultStatus.PASS, 0), counts.getOrDefault(ResultStatus.WARNING, 0),
        counts.getOrDefault(ResultStatus.FAIL, 0), counts.getOrDefault(ResultStatus.INFORMATIONAL, 0));
  }

  private void logOverallSummary(AssessmentResults results) {
    int testedCount = 0;
    EnumMap<ResultStatus, Integer> counts = new EnumMap<ResultStatus, Integer>(ResultStatus.class);

    for (BaseRequirementResult baseResult : results.getBaseRequirementResults()) {
      ResultStatus status = baseResult.getStatus();
      Integer count = counts.get(status);
      if (count == null) {
        count = 0;
      }
      counts.put(status, ++count);

      if (ResultStatus.INFORMATIONAL.compareTo(status) <= 0) {
        ++testedCount;
      }
    }
    log.log(getSummaryLevel(),
        "Checked {} base requirements having {} PASS, {} WARNING, {} FAIL, and {} INFORMATIONAL results", testedCount,
        counts.getOrDefault(ResultStatus.PASS, 0), counts.getOrDefault(ResultStatus.WARNING, 0),
        counts.getOrDefault(ResultStatus.FAIL, 0), counts.getOrDefault(ResultStatus.INFORMATIONAL, 0));
    boolean isPassing = (counts.getOrDefault(ResultStatus.FAIL, 0) == 0);
    log.log(getSummaryLevel(), "The target is {}valid", (isPassing ? "" : "in"));
  }

}
