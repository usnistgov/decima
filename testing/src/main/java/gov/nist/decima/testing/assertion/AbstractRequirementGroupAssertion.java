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

package gov.nist.decima.testing.assertion;

import gov.nist.decima.core.assessment.result.AssessmentResults;
import gov.nist.decima.core.assessment.result.DerivedRequirementResult;
import gov.nist.decima.core.assessment.result.ResultStatus;
import gov.nist.decima.xml.document.XMLDocument;

import org.junit.Assert;

import java.util.Set;

public abstract class AbstractRequirementGroupAssertion extends AbstractAssertion {
  private final Integer quantifier;
  private final Operator operator;

  /**
   * Represents an assertion over the results of performing a set of assessments that applies to a
   * group of requirements.
   * 
   * @param status
   *          the required assessment result status for each requirement in the group
   * @param quantifier
   *          the number of requirements that must pass or <code>null</code> if all must pass
   * @param operator
   *          the type of comparison operator to use
   */
  public AbstractRequirementGroupAssertion(ResultStatus status, String quantifier, Operator operator) {
    super(status);
    this.quantifier = "ALL".equals(quantifier) ? null : Integer.valueOf(quantifier);
    this.operator = operator;
  }

  public Integer getQuantifier() {
    return quantifier;
  }

  public Operator getOperator() {
    return operator;
  }

  @Override
  public void evaluate(XMLDocument doc, AssessmentResults results, AssertionTracker tracker)
      throws AssertionError, AssertionException {
    if (quantifier == null) {
      handleAll(doc, results, tracker);
    } else {
      handleQuanity(doc, results, tracker);
    }
  }

  protected abstract Set<String> getMatchingDerivedRequirements(AssessmentResults results, ResultStatus matchingStatus,
      AssertionTracker tracker) throws AssertionException;

  protected void handleQuanity(XMLDocument doc, AssessmentResults results, AssertionTracker tracker)
      throws AssertionException {
    ResultStatus requiredStatus = getResultStatus();
    Set<String> matchingDerivedRequirements = getMatchingDerivedRequirements(results, requiredStatus, tracker);
    int matchingCount = matchingDerivedRequirements.size();

    String message = null;
    Integer quantifier = getQuantifier();
    switch (getOperator()) {
    case EQUAL:
      if (matchingCount != quantifier) {
        message = "Expected " + quantifier + " derived requirements to " + requiredStatus + ", but found "
            + matchingCount + " failures";
      }
      break;
    case GREATER_THAN:
      if (matchingCount <= quantifier) {
        message = "Expected more than " + quantifier + " derived requirements to " + requiredStatus + ", but found "
            + matchingCount + " failures";
      }
      break;
    case LESS_THAN:
      if (matchingCount >= quantifier) {
        message = "Expected less than " + quantifier + " derived requirements to " + requiredStatus + ", but found "
            + matchingCount + " failures";
      }
      break;
    default:
      throw new UnsupportedOperationException(getOperator().toString());
    }
    if (message != null) {
      Assert.fail(message);
    }
  }

  protected abstract Set<DerivedRequirementResult> getInvalidDerivedRequirements(ResultStatus requiredStatus,
      AssessmentResults results, AssertionTracker tracker) throws AssertionException;

  protected void handleAll(XMLDocument doc, AssessmentResults results, AssertionTracker tracker)
      throws AssertionException {
    ResultStatus requiredStatus = getResultStatus();
    Set<DerivedRequirementResult> invalidDerivedRequirements
        = getInvalidDerivedRequirements(requiredStatus, results, tracker);
    if (!invalidDerivedRequirements.isEmpty()) {

      StringBuilder builder = new StringBuilder();
      builder.append("Expected all derived results to ").append(requiredStatus)
          .append(", but the following did not match: ");
      boolean first = true;
      for (DerivedRequirementResult result : invalidDerivedRequirements) {
        if (first) {
          first = false;
        } else {
          builder.append(", ");
        }
        builder.append(result.getDerivedRequirement().getId());
        builder.append("=");
        builder.append(result.getStatus());
      }
      Assert.fail(builder.toString());
    }
  }
}
