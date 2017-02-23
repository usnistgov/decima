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
import gov.nist.decima.core.document.SourceInfo;
import gov.nist.decima.core.requirement.RequirementsManager;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class DefaultAssessmentResults implements AssessmentResults {
  private final RequirementsManager requirementsManager;
  private final ZonedDateTime startDateTime;
  private final ZonedDateTime endDateTime;
  private final Map<String, SourceInfo> assessedSubjectsMap = new LinkedHashMap<>();
  private final Map<String, BaseRequirementResult> baseRequirementMap = new LinkedHashMap<>();
  private final Map<String, DerivedRequirementResult> derivedRequirementMap = new LinkedHashMap<>();
  private final Map<String, String> assessmentProperties = new LinkedHashMap<>();

  /**
   * Construct a new collection of requirements-based results generated based on performing one or
   * more assessments.
   * 
   * @param requirementsManager
   *          the collection of requirements related to the assessments
   * @param startDateTime
   *          the starting date and time of the first assessment
   * @param endDateTime
   *          the date and time when assessment execution ended
   */
  public DefaultAssessmentResults(RequirementsManager requirementsManager, ZonedDateTime startDateTime,
      ZonedDateTime endDateTime) {
    this.requirementsManager = requirementsManager;
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
  }

  @Override
  public RequirementsManager getRequirementsManager() {
    return requirementsManager;
  }

  @Override
  public ZonedDateTime getStartTimestamp() {
    return startDateTime;
  }

  @Override
  public ZonedDateTime getEndTimestamp() {
    return endDateTime;
  }

  @Override
  public BaseRequirementResult getBaseRequirementResult(String id) {
    return baseRequirementMap.get(id);
  }

  @Override
  public DerivedRequirementResult getDerivedRequirementResult(String id) {
    return derivedRequirementMap.get(id);
  }

  @Override
  public Collection<BaseRequirementResult> getBaseRequirementResults() {
    return Collections.unmodifiableCollection(baseRequirementMap.values());
  }

  @Override
  public Map<String, String> getProperties() {
    return Collections.unmodifiableMap(assessmentProperties);
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.nist.decima.core.assessment.result.AssessmentResults#getAssessmentSubjects()
   */
  @Override
  public Map<String, SourceInfo> getAssessmentSubjects() {
    return Collections.unmodifiableMap(assessedSubjectsMap);
  }

  /**
   * Assigns a property value in the assessment results. This can be useful to associate arbitrary
   * metadata relating to the assessments being performed.
   * 
   * @param key
   *          the property key
   * @param value
   *          the value to assign
   */
  public void setProperty(String key, String value) {
    Objects.requireNonNull(key, "key");
    Objects.requireNonNull(value, "value");

    assessmentProperties.put(key, value);
  }

  /**
   * Registers an assessment subject with the results.
   * 
   * @param document
   *          the source to register
   */
  public void addAssessmentSubject(Document document) {
    for (SourceInfo source : document.getSourceInfo()) {
      String systemId = source.getSystemId();
      SourceInfo old = assessedSubjectsMap.put(systemId, source);
      if (old != null) {
        throw new RuntimeException("Duplicate systemId '" + systemId + "' used for multiple assessed documents");
      }
    }
  }

  /**
   * Appends the base requirement result and any related derived requirement results to this result
   * set.
   * 
   * @param baseRequirementResult
   *          the base requirement result to append
   */
  public void addValidationResult(BaseRequirementResult baseRequirementResult) {
    baseRequirementMap.put(baseRequirementResult.getBaseRequirement().getId(), baseRequirementResult);

    for (DerivedRequirementResult d : baseRequirementResult.getDerivedRequirementResults()) {
      derivedRequirementMap.put(d.getDerivedRequirement().getId(), d);
    }
  }
}
