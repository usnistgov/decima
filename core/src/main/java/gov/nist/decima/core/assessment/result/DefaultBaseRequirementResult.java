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

package gov.nist.decima.core.assessment.result;

import gov.nist.decima.core.requirement.BaseRequirement;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DefaultBaseRequirementResult extends AbstractRequirementResult implements BaseRequirementResult {
  private final BaseRequirement baseRequirement;
  private final Map<String, DefaultDerivedRequirementResult> derivedRequirementnResults = new LinkedHashMap<>();

  public DefaultBaseRequirementResult(BaseRequirement baseRequirement, ResultStatus initialStatus) {
    super(initialStatus);
    this.baseRequirement = baseRequirement;
  }

  @Override
  public BaseRequirement getBaseRequirement() {
    return baseRequirement;
  }

  public DefaultDerivedRequirementResult getDerivedRequirementResult(String id) {
    return derivedRequirementnResults.get(id);
  }

  @Override
  public List<DerivedRequirementResult> getDerivedRequirementResults() {
    return new ArrayList<DerivedRequirementResult>(derivedRequirementnResults.values());
  }

  /**
   * Appends a new derived requirement result to this base requirement result.
   * 
   * @param result
   *          the derived requirement result to append
   */
  public void addDerivedRequirementResult(DefaultDerivedRequirementResult result) {
    derivedRequirementnResults.put(result.getDerivedRequirement().getId(), result);

    // recompute the status
    if (result.getStatus().ordinal() > getStatus().ordinal()) {
      setStatus(result.getStatus());
    }
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("id", getBaseRequirement().getId())
        .append("status", getStatus()).toString();
  }
}
