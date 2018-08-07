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

package gov.nist.decima.xml.assessment.schematron;

import gov.nist.decima.core.assessment.AbstractAssessment;
import gov.nist.decima.core.assessment.Assessment;
import gov.nist.decima.core.assessment.AssessmentException;
import gov.nist.decima.core.assessment.result.AssessmentResultBuilder;
import gov.nist.decima.xml.document.XMLDocument;
import gov.nist.decima.xml.schematron.Schematron;
import gov.nist.decima.xml.schematron.SchematronCompilationException;
import gov.nist.decima.xml.schematron.SchematronEvaluationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.SAXEngine;
import org.jdom2.output.XMLOutputter;
import org.jdom2.transform.JDOMResult;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

public class SchematronAssessment extends AbstractAssessment<XMLDocument> {
  private static final Logger log = LogManager.getLogger(SchematronAssessment.class);
  public static final String ASSESSMENT_TYPE = "Schematron";
  private static final XMLOutputter DEFAULT_XML_OUTPUTTER = new XMLOutputter();

  private final Schematron schematron;
  private final String phase;
  private final SchematronHandler schematronHandler;

  private final Map<String, String> parameters = new HashMap<>();

  /**
   * Constructs a new {@link Assessment} that uses a Schematron ruleset to validate an XML document.
   * 
   * Constructs a new Schematron evaluation handler that is capable of processing the Schematron
   * source and the resulting SVRL output from a Schematron validation run.
   * 
   * @param schematron
   *          a {@link Schematron} instance for the Schematron to be evaluated
   * @param phase
   *          the Schematron phase to use during the Schematron validation, which may be {@code null}
   * @param schematronHandler
   *          a handler that is capable of processing Schematron and SVRL documents based on custom
   *          logic
   */
  public SchematronAssessment(Schematron schematron, String phase, SchematronHandler schematronHandler) {
    Objects.requireNonNull(schematron, "schematron");
    Objects.requireNonNull(schematronHandler, "schematronHandler");
    this.schematron = schematron;
    this.phase = phase;
    this.schematronHandler = schematronHandler;

    if (log.isInfoEnabled()) {
      StringBuilder sb = new StringBuilder();
      sb.append('[');
      sb.append(getId());
      sb.append(']');
      sb.append("Creating a Schematron assessment using schematron '");
      sb.append(schematron.getPath());
      sb.append("'");

      if (phase != null) {
        sb.append(" with phase '");
        sb.append(phase);
        sb.append("'");
      }
      log.info(sb.toString());
    }
  }

  public Schematron getSchematron() {
    return schematron;
  }

  public String getPhase() {
    return phase;
  }

  protected SchematronHandler getSchematronHandler() {
    return schematronHandler;
  }

  public Map<String, String> getParameters() {
    return Collections.unmodifiableMap(this.parameters);
  }

  @Override
  protected void executeInternal(XMLDocument doc, AssessmentResultBuilder builder) throws AssessmentException {

    File resultDir = getResultDirectory();
    if (resultDir != null) {
      Document compiledSchematron;
      try {
        compiledSchematron = getSchematron().getCompiledSchematron(getPhase());
      } catch (SchematronCompilationException e) {
        throw new AssessmentException("Unable to get compiled schematron", e);
      }

      File schematronXslFile = new File(resultDir, "schematron.xsl");
      try (OutputStream os = new BufferedOutputStream(new FileOutputStream(schematronXslFile))) {
        DEFAULT_XML_OUTPUTTER.output(compiledSchematron, os);
      } catch (IOException e) {
        throw new AssessmentException("Unable to output the compiled schematron to: " + schematronXslFile.getPath(), e);
      }
    }

    File svrlFile;
    if (resultDir != null) {
      svrlFile = new File(resultDir, "svrl.xml");
    } else {
      // try {
      // svrlFile = File.createTempFile("svrl", ".xml");
      // svrlFile.deleteOnExit();
      // } catch (IOException e) {
      // throw new AssessmentException("Unable to create temp SVRL result file", e);
      // }
      svrlFile = null;
    }
    if (log.isDebugEnabled()) {
      log.debug("[{}]Processing schematron: {}", getId(), getSchematron().getPath());
    }
    process(doc, builder, svrlFile);
    if (log.isDebugEnabled()) {
      log.debug("[{}]Schematron assessment complete", getId());
    }
  }

  protected void process(XMLDocument document, AssessmentResultBuilder builder, File svrlFile)
      throws AssessmentException {

    Result svrlResult2;
    if (svrlFile == null) {
      log.debug("Transforming SVRL results");
      svrlResult2 = new JDOMResult();
    } else {
      log.debug("Transforming SVRL results to: " + svrlFile);
      svrlResult2 = new StreamResult(svrlFile);
    }

    try {
      schematron.transform(document.getSource(), svrlResult2, getPhase(), getParameters());
    } catch (SchematronEvaluationException ex) {
      throw new AssessmentException(ex);
    }

    Document svrlDocument;
    if (svrlFile == null) {
      log.debug("Analyzing SVRL results");
      svrlDocument = ((JDOMResult) svrlResult2).getDocument();
    } else {
      try {
        log.debug("Analyzing SVRL results from: " + svrlFile);
        SAXEngine saxEngine = new SAXBuilder();
        svrlDocument = saxEngine.build(svrlFile);
      } catch (JDOMException | IOException e) {
        throw new AssessmentException(e);
      }
    }

    SVRLHandler svrlHandler = getSchematronHandler().newSVRLHandler(this, document, builder);
    SVRLParser.parse(svrlHandler, svrlDocument);
    log.debug("SVRL result analysis completed");
  }

  @Override
  public String getAssessmentType() {
    return ASSESSMENT_TYPE;
  }

  public void addParameter(String name, String value) {
    parameters.put(name, value);
  }

  public void addParameters(Map<String, String> params) {
    parameters.putAll(params);
  }

  @Override
  protected String getNameDetails() {
    StringBuilder builder = new StringBuilder();
    builder.append(getSchematron().getPath());
    String phase = getPhase();
    if (phase != null) {
      builder.append("(").append(phase).append(")");
    }
    return builder.toString();
  }

}
