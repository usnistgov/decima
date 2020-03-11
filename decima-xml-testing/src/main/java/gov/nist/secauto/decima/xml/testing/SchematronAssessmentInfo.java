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

package gov.nist.secauto.decima.xml.testing;

import gov.nist.secauto.decima.xml.assessment.schematron.SchematronHandler;

import org.jdom2.Element;
import org.jdom2.filter.Filters;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class SchematronAssessmentInfo {
  private final URL rulsetLocation;
  private final String phase;
  private final String handlerClass;
  private final Map<String, String> parameters;

  /**
   * Creates a new data transfer object for a Schematron configuration based on the contents of the
   * "schematron-assessment" element.
   * 
   * @param assessmentElement
   *          the element to load the data from
   * @throws MalformedURLException
   *           if the ruleset URL is malformed
   */
  public SchematronAssessmentInfo(Element assessmentElement) throws MalformedURLException {
    this.rulsetLocation = new URL(assessmentElement.getAttributeValue("ruleset"));
    this.phase = assessmentElement.getAttributeValue("phase");
    this.handlerClass = assessmentElement.getAttributeValue("handler-class");

    this.parameters = handleParameters(
        assessmentElement.getContent(Filters.element("parameter", assessmentElement.getNamespace())));
  }

  private static Map<String, String> handleParameters(List<Element> content) {
    Map<String, String> retval;

    if (content.isEmpty()) {
      retval = Collections.emptyMap();
    } else {
      retval = new HashMap<>();
      for (Element p : content) {
        String key = p.getAttributeValue("name");
        String value = p.getText();
        retval.put(key, value);
      }
      retval = Collections.unmodifiableMap(retval);
    }
    return retval;
  }

  /**
   * Retrieves a {@link URL} pointing to the location of the Schematron rules.
   * 
   * @return the rulsetLocation
   */
  public URL getRulsetLocation() {
    return rulsetLocation;
  }

  /**
   * Retrieves the identified Schematron phase to use when performing Schematron validation.
   * 
   * @return the phase
   */
  public String getPhase() {
    return phase;
  }

  /**
   * Retrieves the class name of the {@link SchematronHandler} instance to use to process the SVRL
   * results.
   * 
   * @return the handlerClass
   */
  public String getHandlerClass() {
    return handlerClass;
  }

  /**
   * Retrieve the mapping of Schematron XSL parameters to use when performing Schematron validation.
   * 
   * @return the parameters
   */
  public Map<String, String> getParameters() {
    return parameters;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + rulsetLocation.hashCode();
    hash = 31 * hash + (null == phase ? 0 : phase.hashCode());
    hash = 31 * hash + (null == handlerClass ? 0 : handlerClass.hashCode());
    hash = 31 * hash + parameters.hashCode();
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if ((obj == null) || (obj.getClass() != this.getClass())) {
      return false;
    }

    // object must be an instance of the same class at this point
    SchematronAssessmentInfo other = (SchematronAssessmentInfo) obj;
    return rulsetLocation.equals(other.rulsetLocation)
        && (phase == other.phase || (phase != null && phase.equals(other.phase)))
        && (handlerClass == other.handlerClass || (handlerClass != null && handlerClass.equals(other.handlerClass)))
        && parameters.equals(other.parameters);
  }

}
