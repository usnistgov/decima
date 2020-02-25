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

package gov.nist.decima.xml.document;

import gov.nist.decima.core.assessment.AssessmentException;
import gov.nist.decima.core.assessment.Condition;

import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactoryConfigurationException;

public class XPathCondition implements Condition<XMLDocument> {

  public static XPathCondition newInstance(String xpath) {
    return new XPathCondition(xpath);
  }

  private final String xpath;

  public XPathCondition(String xpath) {
    this.xpath = xpath;
  }

  public String getXPath() {
    return xpath;
  }

  @Override
  public boolean appliesTo(XMLDocument targetDocument) throws AssessmentException {

    XPathEvaluator xpathEvaluator;
    try {
      xpathEvaluator = targetDocument.newXPathEvaluator();
    } catch (XPathFactoryConfigurationException e) {
      String msg = "Unable to get an XPATH evaluator for document: " + targetDocument.getSystemId();
      throw new AssessmentException(msg, e);
    }

    try {
      return xpathEvaluator.test(xpath);
    } catch (XPathExpressionException e) {
      String msg = "Unable to evaluate XPATH '" + getXPath() + "' for template: " + targetDocument.getSystemId();
      throw new AssessmentException(msg, e);
    }
  }

}
