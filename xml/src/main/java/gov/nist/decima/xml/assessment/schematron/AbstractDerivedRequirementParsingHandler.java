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

package gov.nist.decima.xml.assessment.schematron;

import gov.nist.decima.core.assessment.result.TestState;
import gov.nist.decima.xml.schematron.SchematronHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class AbstractDerivedRequirementParsingHandler implements SchematronHandler {
  private static final Logger log = LogManager.getLogger(AbstractDerivedRequirementParsingHandler.class);

  private final Map<String, List<SchematronAssertionEntry>> patternIdToAssertionsMap = new HashMap<>();
  private final Map<String, List<SchematronAssertionEntry>> ruleIdToAssertionsMap = new HashMap<>();

  private String currentPatternId;
  private String currentRuleId;

  public Map<String, List<SchematronAssertionEntry>> getPatternIdToAssertionsMap() {
    return Collections.unmodifiableMap(patternIdToAssertionsMap);
  }

  public Map<String, List<SchematronAssertionEntry>> getRuleIdToAssertionsMap() {
    return Collections.unmodifiableMap(ruleIdToAssertionsMap);
  }

  public AbstractDerivedRequirementParsingHandler() {
  }

  @Override
  public boolean handlePattern(Element pattern) {
    currentPatternId = pattern.getAttributeValue("id");
    if (currentPatternId == null) {
      log.error("A schematron pattern element is missing an id attribute."
          + " This will prevent the evaluator from determining if a derived requirement is "
          + TestState.NOT_APPLICABLE);
    } else if (patternIdToAssertionsMap.containsKey(currentPatternId)) {
      log.error("Multiple schematron pattern elements have the same id attribute '" + currentPatternId
          + "'. This will prevent the evaluator from determining if a derived requirement is "
          + TestState.NOT_APPLICABLE);
    }
    return true;
  }

  @Override
  public boolean handleRule(Element rule) {
    currentRuleId = rule.getAttributeValue("id");
    if (currentRuleId == null) {
      log.error("A schematron rule element is missing an id attribute."
          + " This will prevent the evaluator from determining if an assertion is " + TestState.TESTED);
    } else if (ruleIdToAssertionsMap.containsKey(currentRuleId)) {
      log.error("Multiple schematron rule elements have the same id attribute '" + currentRuleId
          + "'. This will prevent the evaluator from determining if an assertion is " + TestState.TESTED);
    }
    return true;
  }

  @Override
  public void handleReport(Element reportElement) {
    handleElement(SchematronAssertionEntry.AssertionType.REPORT, getDerivedRequirement(reportElement));
  }

  protected abstract String getDerivedRequirement(Element assertionElement);

  @Override
  public void handleAssert(Element assertElement) {
    handleElement(SchematronAssertionEntry.AssertionType.ASSERT, getDerivedRequirement(assertElement));
  }

  private void handleElement(SchematronAssertionEntry.AssertionType type, String derivedRequirementId) {
    SchematronAssertionEntry entry = new SchematronAssertionEntry(type, derivedRequirementId);
    if (currentPatternId != null) {
      appendAssertion(patternIdToAssertionsMap, currentPatternId, entry);
    }
    if (currentRuleId != null) {
      appendAssertion(ruleIdToAssertionsMap, currentRuleId, entry);
    }
  }

  private static void appendAssertion(Map<String, List<SchematronAssertionEntry>> map, String key,
      SchematronAssertionEntry assertion) {
    List<SchematronAssertionEntry> assertions = map.get(key);
    if (assertions == null) {
      assertions = new LinkedList<>();
      map.put(key, assertions);
    }
    assertions.add(assertion);
  }
}
