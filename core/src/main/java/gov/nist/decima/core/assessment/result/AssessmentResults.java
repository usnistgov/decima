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
/**
 * 
 */

package gov.nist.decima.core.assessment.result;

import gov.nist.decima.core.requirement.RequirementsManager;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Map;

/**
 * Represents the collected results of performing one or more assessments.
 */
public interface AssessmentResults {
  RequirementsManager getRequirementsManager();

  /**
   * The time at which the group of assessments started.
   * 
   * @return a {@code non-null} date time
   */
  ZonedDateTime getStartTimestamp();

  /**
   * The time at which the group of assessments ended.
   * 
   * @return a {@code non-null} date time
   */
  ZonedDateTime getEndTimestamp();

  /**
   * An collection of base requirement results based on a executed group of assessments.
   * 
   * @return a list of zero or more derived requirement results
   */
  Collection<BaseRequirementResult> getBaseRequirementResults();

  // /**
  // * An ordered collection of derived requirement results based on a
  // executed group of assessments.
  // * @return a list of zero or more derived requirement results
  // */
  // List<DerivedRequirementResult> getDerivedRequirementResults();

  /**
   * Retrieves a specific {@link DerivedRequirementResult} based on the provided identifier.
   * 
   * @param derivedRequirementId
   *          the identifier of a {@link DerivedRequirementResult}
   * @return the {@link DerivedRequirementResult} or {@code null} if it was not found to exist
   */
  DerivedRequirementResult getDerivedRequirementResult(String derivedRequirementId);

  BaseRequirementResult getBaseRequirementResult(String baseRequirement);

  /**
   * Used to retrieve meta information associated with the results.
   * 
   * @return an unmodifiable map of property keys to values
   */
  Map<String, String> getProperties();

}
