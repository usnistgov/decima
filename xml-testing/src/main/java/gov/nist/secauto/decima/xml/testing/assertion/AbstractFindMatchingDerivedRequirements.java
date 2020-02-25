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

package gov.nist.secauto.decima.xml.testing.assertion;

import gov.nist.secauto.decima.core.assessment.result.BaseRequirementResult;
import gov.nist.secauto.decima.core.assessment.result.DerivedRequirementResult;
import gov.nist.secauto.decima.core.assessment.result.ResultStatus;
import gov.nist.secauto.decima.core.assessment.result.TestResult;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public abstract class AbstractFindMatchingDerivedRequirements<T> implements RequirementHandler {
  private final Set<T> requirements = new LinkedHashSet<>();
  private final ResultStatus requiredStatus;
  private final AssertionTracker assertionTracker;

  public AbstractFindMatchingDerivedRequirements(ResultStatus matchingStatus, AssertionTracker tracker) {
    this.requiredStatus = matchingStatus;
    this.assertionTracker = tracker;
  }

  public ResultStatus getRequiredStatus() {
    return requiredStatus;
  }

  public AssertionTracker getAssertionTracker() {
    return assertionTracker;
  }

  public Set<T> getRequirements() {
    return Collections.unmodifiableSet(requirements);
  }

  @Override
  public boolean handleBaseRequirementResult(BaseRequirementResult baseResult) throws AssertionException {
    return baseResult.getStatus().ordinal() >= getRequiredStatus().ordinal();
  }

  @Override
  public boolean handleDerivedRequirementResult(BaseRequirementResult baseResult,
      DerivedRequirementResult derivedResult) throws AssertionException {
    if (getRequiredStatus().equals(derivedResult.getStatus())) {
      requirements.add(handleMatchingDerivedRequirement(baseResult, derivedResult));
    }
    return false;
  }

  protected abstract T handleMatchingDerivedRequirement(BaseRequirementResult baseResult,
      DerivedRequirementResult derivedResult);

  @Override
  public void handleTestResult(BaseRequirementResult baseResult, DerivedRequirementResult derivedResult,
      TestResult testResult) {
    // this should never get called
    throw new UnsupportedOperationException();
  }

}
