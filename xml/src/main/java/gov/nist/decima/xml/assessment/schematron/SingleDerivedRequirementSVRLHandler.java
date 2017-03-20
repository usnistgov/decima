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

import gov.nist.decima.core.assessment.Assessment;
import gov.nist.decima.core.assessment.AssessmentException;
import gov.nist.decima.core.assessment.result.AssessmentResultBuilder;
import gov.nist.decima.core.assessment.result.TestStatus;
import gov.nist.decima.xml.document.XMLDocument;

import org.jdom2.Element;

import java.util.Collections;

public class SingleDerivedRequirementSVRLHandler extends AbstractSVRLHandler {

  private final String derivedRequirementId;

  public SingleDerivedRequirementSVRLHandler(String derivedRequirementId, Assessment<? extends XMLDocument> assessment,
      XMLDocument sourceDocument, AssessmentResultBuilder assessmentResultBuilder) throws AssessmentException {
    super(assessment, sourceDocument, assessmentResultBuilder);
    this.derivedRequirementId = derivedRequirementId;
  }

  public String getDerivedRequirementId() {
    return derivedRequirementId;
  }

  @Override
  public void handleSuccessfulReport(Element successfulReport) {
    String assertionId = successfulReport.getAttributeValue("id");
    TestStatus testStatus = TestStatus.INFORMATIONAL;
    String xpath = successfulReport.getAttributeValue("location");
    String valueText = successfulReport.getChildText("text", successfulReport.getNamespace());
    handleAssertionResult(getDerivedRequirementId(), assertionId, testStatus, xpath,
        Collections.singletonList(valueText));
  }

  @Override
  public void handleFailedAssert(Element failedAssert) {
    String assertionId = failedAssert.getAttributeValue("id");
    TestStatus testStatus = TestStatus.FAIL;
    String xpath = failedAssert.getAttributeValue("location");
    String valueText = failedAssert.getChildText("text", failedAssert.getNamespace());
    handleAssertionResult(getDerivedRequirementId(), assertionId, testStatus, xpath,
        Collections.singletonList(valueText));

  }
}
