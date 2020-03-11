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

package gov.nist.secauto.decima.core.requirement;

import gov.nist.secauto.decima.core.assessment.result.ResultStatus;
import gov.nist.secauto.decima.core.assessment.result.Severity;
import gov.nist.secauto.decima.core.assessment.result.TestStatus;

public enum RequirementType {
  MUST(Severity.ERROR, ResultStatus.PASS, ResultStatus.PASS, ResultStatus.FAIL, ResultStatus.WARNING),
  MUST_NOT(Severity.ERROR, ResultStatus.PASS, ResultStatus.PASS, ResultStatus.FAIL, ResultStatus.WARNING),
  SHOULD(Severity.WARNING, ResultStatus.PASS, ResultStatus.PASS, ResultStatus.WARNING, ResultStatus.WARNING),
  SHOULD_NOT(Severity.WARNING, ResultStatus.PASS, ResultStatus.PASS, ResultStatus.WARNING, ResultStatus.WARNING),
  MAY(Severity.INFO, ResultStatus.PASS, ResultStatus.PASS, ResultStatus.INFORMATIONAL, ResultStatus.INFORMATIONAL),
  INFORMATIONAL(
      Severity.INFO,
      ResultStatus.INFORMATIONAL,
      ResultStatus.INFORMATIONAL,
      ResultStatus.INFORMATIONAL,
      ResultStatus.INFORMATIONAL);

  private final Severity severity;
  private final ResultStatus statusOnPass;
  private final ResultStatus statusOnWarning;
  private final ResultStatus statusOnFail;
  private final ResultStatus statusOnConditionalFail;

  private RequirementType(Severity severity, ResultStatus statusOnPass, ResultStatus statusOnWarning,
      ResultStatus statusOnFail, ResultStatus statusOnConditionalFail) {
    this.severity = severity;
    this.statusOnPass = statusOnPass;
    this.statusOnWarning = statusOnWarning;
    this.statusOnFail = statusOnFail;
    this.statusOnConditionalFail = statusOnConditionalFail;
  }

  public Severity getSeverity() {
    return severity;
  }

  /**
   * Determines the {@link ResultStatus} for a provided {@link TestStatus}.
   * 
   * @param result
   *          the result of a given test
   * @param conditional
   *          indicates if the test has a conditional requirement
   * @return the appropriate mapped ResultStatus
   */
  public ResultStatus resolveTestResult(TestStatus result, boolean conditional) {
    ResultStatus retval;
    switch (result) {
    case FAIL:
      if (conditional) {
        retval = getStatusOnConditionalFail();
      } else {
        retval = getStatusOnFail();
      }
      break;
    case PASS:
      retval = getStatusOnPass();
      break;
    case WARNING:
      retval = getStatusOnWarning();
      break;
    case INFORMATIONAL:
      retval = ResultStatus.INFORMATIONAL;
      break;
    default:
      throw new IllegalStateException("Unknown test status: " + result.toString());
    }
    return retval;
  }

  public ResultStatus getStatusOnPass() {
    return statusOnPass;
  }

  public ResultStatus getStatusOnWarning() {
    return statusOnWarning;
  }

  public ResultStatus getStatusOnFail() {
    return statusOnFail;
  }

  public ResultStatus getStatusOnConditionalFail() {
    return statusOnConditionalFail;
  }

}