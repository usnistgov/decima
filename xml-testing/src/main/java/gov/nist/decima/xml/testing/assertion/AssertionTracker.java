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

import gov.nist.decima.core.assessment.result.BaseRequirementResult;
import gov.nist.decima.core.assessment.result.DerivedRequirementResult;

import java.util.HashSet;
import java.util.Set;

/**
 * Used to track which assertions have been checked vs. which have not. This is needed to support
 * {@link RemainingAssertion} semantics.
 */
public class AssertionTracker {
  private final Set<BaseRequirementResult> assertedBaseRequirements = new HashSet<>();
  private final Set<DerivedRequirementResult> assertedDerivedRequirements = new HashSet<>();

  /**
   * Registered the provided base requirement as asserted.
   * 
   * @param result
   *          the base requirement result to register
   * @throws AssertionException
   *           if the requirement has already been asserted
   */
  public void assertRequirement(BaseRequirementResult result) throws AssertionException {
    if (!assertedBaseRequirements.add(result)) {
      throw new AssertionException(
          "The base requirement '" + result.getBaseRequirement().getId() + "' has already been asserted.");
    }
  }

  /**
   * Registered the provided derived requirement as asserted.
   * 
   * @param result
   *          the derived requirement result to register
   * @throws AssertionException
   *           if the requirement has already been asserted
   */
  public void assertRequirement(DerivedRequirementResult result) throws AssertionException {
    if (!assertedDerivedRequirements.add(result)) {
      throw new AssertionException(
          "The derived requirement '" + result.getDerivedRequirement().getId() + "' has already been asserted.");
    }
  }

  public boolean isAsserted(BaseRequirementResult result) {
    return assertedBaseRequirements.contains(result);
  }

  public boolean isAsserted(DerivedRequirementResult result) {
    return assertedDerivedRequirements.contains(result);
  }
}
