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

package gov.nist.decima.xml.assessment.schematron;

import gov.nist.decima.xml.schematron.Schematron;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class AbstractIdAwareSchematronHandler implements IdAwareSchematronHandler {
  private static final Logger log = LogManager.getLogger(AbstractIdAwareSchematronHandler.class);

  private Map<String, List<SchematronAssertionEntry>> patternIdToAssertionsMap;
  private Map<String, List<SchematronAssertionEntry>> ruleIdToAssertionsMap;

  /**
   * Constructs a new {@link AbstractIdAwareSchematronHandler} instance that uses the provided
   * {@link Schematron} to parse out a mapping of pattern and rule identifiers to assertions within
   * the Schematron rules. The Schematron used for generating this mapping, must be the same
   * Schematron used in the associated {@link SchematronAssessment}.
   * 
   * @param schematron
   *          the Schematron to parse to create the mapping
   */
  public AbstractIdAwareSchematronHandler(Schematron schematron) {
    AbstractDerivedRequirementParsingHandler handler = newDerivedRequirementParsingHandler(schematron);
    this.patternIdToAssertionsMap = handler.getPatternIdToAssertionsMap();
    this.ruleIdToAssertionsMap = handler.getRuleIdToAssertionsMap();
  }

  protected abstract AbstractDerivedRequirementParsingHandler
      newDerivedRequirementParsingHandler(Schematron schematron);

  /**
   * Get the list of assertions (i.e., assert, report) associated with the pattern having the provided
   * identifer.
   * 
   * @param patternId
   *          the identifier of the pattern to find associated assertions
   */
  public List<SchematronAssertionEntry> getAssertionsForPatternId(String patternId) {
    List<SchematronAssertionEntry> retval = patternIdToAssertionsMap.get(patternId);
    if (retval == null) {
      retval = Collections.emptyList();
      log.debug("No assertions found for pattern ID: " + patternId);
    } else {
      retval = Collections.unmodifiableList(retval);
    }
    return retval;
  }

  public List<SchematronAssertionEntry> getAssertionsForRuleId(String ruleId) {
    return Collections.unmodifiableList(ruleIdToAssertionsMap.get(ruleId));
  }

}
