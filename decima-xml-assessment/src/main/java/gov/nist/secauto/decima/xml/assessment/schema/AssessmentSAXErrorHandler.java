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

package gov.nist.secauto.decima.xml.assessment.schema;

import gov.nist.secauto.decima.core.assessment.result.AssessmentResultBuilder;
import gov.nist.secauto.decima.core.assessment.result.TestResult;
import gov.nist.secauto.decima.core.assessment.result.TestState;
import gov.nist.secauto.decima.core.assessment.result.TestStatus;
import gov.nist.secauto.decima.xml.document.XMLDocument;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class AssessmentSAXErrorHandler implements ErrorHandler {
  private final SchemaAssessment schemaAssessment;
  private final XMLDocument assessedDocument;
  private final String derivedRequirementId;
  private final AssessmentResultBuilder builder;
  private final SAXLocationXPathResolver saxLocationXPathResolver;
  private boolean treatWarningsAsErrors = false;

  /**
   * Constructs a {@link ErrorHandler} that is capable of asserting any SAX errors as a Decima
   * {@link TestResult}.
   * 
   * @param schemaAssessment
   *          the assessment this handler supports
   * @param assessedDocument
   *          the XML document target of the SAX operation
   * @param derivedRequirementId
   *          the derived requirement to associate the SAX errors/warnings with
   * @param builder
   *          the {@link AssessmentResultBuilder} to use to post the {@link TestResult} to
   * @param saxLocationXPathResolver
   *          an XPathResolver to use to lookup error locations
   */
  public AssessmentSAXErrorHandler(SchemaAssessment schemaAssessment, XMLDocument assessedDocument,
      String derivedRequirementId, AssessmentResultBuilder builder, SAXLocationXPathResolver saxLocationXPathResolver) {
    this.schemaAssessment = schemaAssessment;
    this.assessedDocument = assessedDocument;
    this.derivedRequirementId = derivedRequirementId;
    this.builder = builder;
    this.saxLocationXPathResolver = saxLocationXPathResolver;
    builder.assignTestStatus(schemaAssessment, assessedDocument, derivedRequirementId, TestState.TESTED);
  }

  /**
   * Retrieve the assessment this handler supports.
   * 
   * @return the schemaAssessment
   */
  public SchemaAssessment getSchemaAssessment() {
    return schemaAssessment;
  }

  /**
   * Retieve the document this handler is reporting issues for.
   * 
   * @return the assessedDocument
   */
  public XMLDocument getAssessedDocument() {
    return assessedDocument;
  }

  @Override
  public void warning(SAXParseException ex) throws SAXException {
    TestStatus status = (treatWarningsAsErrors ? TestStatus.FAIL : TestStatus.WARNING);
    newSAXAssertionResult(status, ex);
  }

  @Override
  public void error(SAXParseException ex) throws SAXException {
    newSAXAssertionResult(TestStatus.FAIL, ex);
  }

  @Override
  public void fatalError(SAXParseException ex) throws SAXException {
    error(ex);
  }

  private SAXTestResult newSAXAssertionResult(TestStatus status, SAXParseException ex) {
    SAXTestResult retval = new SAXTestResult(assessedDocument, status, ex, saxLocationXPathResolver.getCurrentXPath());
    builder.addTestResult(getSchemaAssessment(), getAssessedDocument(), derivedRequirementId, retval);
    return retval;
  }
}
