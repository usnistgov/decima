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
package gov.nist.decima.testing;

import gov.nist.decima.core.assessment.Assessment;
import gov.nist.decima.core.document.post.template.TemplateProcessor;
import gov.nist.decima.testing.assertion.Assertion;
import gov.nist.decima.xml.document.XMLDocument;
import gov.nist.decima.xml.document.XMLDocumentFactory;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class AssessmentUnitTestBuilder {
  // TODO: generalize the document factory to work with any type of document
  private final XMLDocumentFactory xmlDocumentFactory;
  private String sourceURI;
  private String derivedRequirement;
  private String summary;
  private File resultDir;
  private TemplateProcessor template;
  private List<Assessment<XMLDocument>> assessments = new LinkedList<>();
  private List<Assertion> assertions = new LinkedList<>();

  public AssessmentUnitTestBuilder(XMLDocumentFactory factory) {
    this.xmlDocumentFactory = factory;
  }

  public XMLDocumentFactory getXMLDocumentFactory() {
    return xmlDocumentFactory;
  }

  /**
   * Creates a Decima dynamic JUnit test based on the information provided to the builder.
   * 
   * @return the test instance
   */
  public DefaultAssessmentUnitTest build() {
    Objects.requireNonNull(this.sourceURI);
    Objects.requireNonNull(derivedRequirement);
    if (derivedRequirement.isEmpty()) {
      throw new IllegalArgumentException("derivedRequirement must be non-empty");
    }
    Objects.requireNonNull(summary);
    Objects.requireNonNull(template);
    Objects.requireNonNull(resultDir);
    if (assessments.isEmpty()) {
      throw new IllegalArgumentException("assessments must be non-empty");
    }
    if (assertions.isEmpty()) {
      throw new IllegalArgumentException("assertions must be non-empty");
    }

    DefaultAssessmentUnitTest retval = new DefaultAssessmentUnitTest(derivedRequirement, sourceURI, resultDir);
    retval.setXMLDocumentFactory(getXMLDocumentFactory());
    retval.setSummary(this.summary);
    retval.setTemplateProcessor(template);
    retval.addAssessments(Collections.unmodifiableList(assessments));
    retval.setAssertions(Collections.unmodifiableList(assertions));
    retval.setXMLDocumentFactory(getXMLDocumentFactory());
    return retval;
  }

  public AssessmentUnitTestBuilder setResultDirectory(File dir) {
    this.resultDir = dir;
    return this;
  }

  public AssessmentUnitTestBuilder setDerivedRequirement(String derivedRequirement) {
    this.derivedRequirement = derivedRequirement;
    return this;
  }

  public AssessmentUnitTestBuilder setSummary(String text) {
    this.summary = text;
    return this;
  }

  public AssessmentUnitTestBuilder setTemplate(TemplateProcessor template) {
    this.template = template;
    return this;
  }

  public AssessmentUnitTestBuilder addAssessment(Assessment<XMLDocument> assessment) {
    this.assessments.add(assessment);
    return this;
  }

  public AssessmentUnitTestBuilder addAssessments(Collection<? extends Assessment<XMLDocument>> assessments) {
    this.assessments.addAll(assessments);
    return this;
  }

  public AssessmentUnitTestBuilder addAssertion(Assertion assertion) {
    this.assertions.add(assertion);
    return this;
  }

  public AssessmentUnitTestBuilder addAssertion(List<? extends Assertion> assertions) {
    this.assertions.addAll(assertions);
    return this;
  }

  public AssessmentUnitTestBuilder setSourceURI(String uri) {
    this.sourceURI = uri;
    return this;
  }
}
