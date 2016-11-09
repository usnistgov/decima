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

package gov.nist.decima.testing;

import gov.nist.decima.core.assessment.Assessment;
import gov.nist.decima.core.document.DocumentException;
import gov.nist.decima.core.document.XMLDocument;
import gov.nist.decima.core.document.XMLDocumentFactory;
import gov.nist.decima.core.document.post.template.TemplateProcessor;
import gov.nist.decima.testing.assertion.Assertion;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.output.XMLOutputter;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public abstract class UnitTestSupport implements AssessmentUnitTestBase {
  private static final Logger log = LogManager.getLogger(UnitTestSupport.class);

  private static final XMLOutputter DEFAULT_XML_OUTPUTTER = new XMLOutputter();

  private final String name;
  private final String sourceURI;
  private final File resultDir;
  private XMLOutputter xmlOutputter;
  private String summary;

  private List<? extends Assessment<XMLDocument>> assessments = new LinkedList<>();
  private List<Assertion> assertions = new LinkedList<>();

  private TemplateProcessor templateProcessor;
  private XMLDocumentFactory xmlDocumentFactory;

  /**
   * Construct a new instance.
   * @param name the requirement id or name to be tested
   * @param sourceURI the URI for the resource from which the test was loaded
   * @param resultDir the directory to write results to
   */
  public UnitTestSupport(String name, String sourceURI, File resultDir) {
    this.name = name;
    this.sourceURI = sourceURI;
    this.resultDir = resultDir;
  }

  /**
   * Retrieves the XML outputter that can be used to output XML documents.
   * @return the {@link XMLOutputter} instance
   */
  public XMLOutputter getXmlOutputter() {
    if (xmlOutputter == null) {
      xmlOutputter = DEFAULT_XML_OUTPUTTER;
    }
    return xmlOutputter;
  }

  @Override
  public String getName() {
    return name;
  }

  public String getSourceURI() {
    return sourceURI;
  }

  public File getResultDir() {
    return resultDir;
  }

  @Override
  public String getSummary() {
    return summary;
  }

  public void setSummary(String text) {
    this.summary = text;
  }

  @Override
  public List<? extends Assessment<XMLDocument>> getAssessments() {
    return assessments;
  }

  public void addAssessments(List<? extends Assessment<XMLDocument>> assessments) {
    this.assessments = assessments;
  }

  @Override
  public List<Assertion> getAssertions() {
    return assertions;
  }

  public void setAssertions(List<Assertion> assertions) {
    this.assertions = assertions;
  }

  @Override
  public TemplateProcessor getTemplateProcessor() {
    return templateProcessor;
  }

  public void setTemplateProcessor(TemplateProcessor processor) {
    this.templateProcessor = processor;
  }

  public XMLDocumentFactory getXMLDocumentFactory() {
    return xmlDocumentFactory;
  }

  public void setXMLDocumentFactory(XMLDocumentFactory factory) {
    this.xmlDocumentFactory = factory;
  }

  protected XMLDocument processTemplate() throws DocumentException {
    TemplateProcessor tp = getTemplateProcessor();
    XMLDocument doc;
    try {
      doc = tp.generate(getXMLDocumentFactory());
    } catch (DocumentException e) {
      // log.error("Unable to process template: "+tp.getTemplateURL(), e);
      throw e;
    } catch (Throwable e) {
      log.error("An unexpected error occured while processing template: " + tp.getBaseTemplateURL(), e);
      throw e;
    }

    File resultDir = getResultDir();
    if (resultDir != null) {
      if (!resultDir.exists() && !resultDir.mkdirs()) {
        throw new DocumentException("Unable to create result directory: " + resultDir);
      }

      File outputFile = new File(resultDir, "template.xml");
      try {
        doc.copyTo(outputFile);
      } catch (IOException e) {
        throw new DocumentException(
            "Unable to write template file to : " + outputFile.getAbsolutePath(), e);
      }
    }
    return doc;
  }

}
