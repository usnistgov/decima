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

package gov.nist.secauto.decima.xml.assessment.result;

import gov.nist.secauto.decima.xml.util.ExtendedXSLTransformer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class ReportGenerator {
  private static final String DEFAULT_RESULT_XSL_URL = "classpath:xsl/result.xsl";
  private static final String XSL_PARAM_HTML_TITLE = "html-title";
  private static final String XSL_PARAM_IGNORE_NOT_TESTED_RESULTS = "ignore-not-tested-results";
  private static final String XSL_PARAM_IGNORE_OUT_OF_SCOPE_RESULTS = "ignore-outofscope-results";
  private static final String XSL_PARAM_GENERATE_XML_OUTPUT = "generate-xml-output";
  private static final boolean XSL_PARAM_GENERATE_XML_OUTPUT_DEFAULT = true;
  private static final String XSL_PARAM_XML_OUTPUT_DEPTH = "xml-output-depth";
  private static final String XSL_PARAM_XML_OUTPUT_CHILD_LIMIT = "xml-output-child-limit";
  private static final String XSL_PARAM_TEST_RESULT_LIMIT = "test-result-limit";
  private static final int XSL_PARAM_TEST_RESULT_LIMIT_DEFAULT = 10;

  private boolean ignoreOutOfScopeResults = false;
  private boolean ignoreNotTestedResults = false;
  private boolean generateXmlOutput = XSL_PARAM_GENERATE_XML_OUTPUT_DEFAULT;
  private int xmlToHtmlOutputDepth = 1;
  private int xmlToHtmlOutputChildLimit = 10;
  private int testResultLimit = XSL_PARAM_TEST_RESULT_LIMIT_DEFAULT;
  private URI xslTemplateExtension;
  private String htmlTitle;
  private String targetName;

  public boolean isIgnoreOutOfScopeResults() {
    return ignoreOutOfScopeResults;
  }

  public void setIgnoreOutOfScopeResults(boolean ignoreOutOfScopeResults) {
    this.ignoreOutOfScopeResults = ignoreOutOfScopeResults;
  }

  public boolean isIgnoreNotTestedResults() {
    return ignoreNotTestedResults;
  }

  public void setIgnoreNotTestedResults(boolean ignoreNotTestedResults) {
    this.ignoreNotTestedResults = ignoreNotTestedResults;
  }

  public String getHtmlTitle() {
    return htmlTitle;
  }

  public void setHtmlTitle(String htmlTitle) {
    this.htmlTitle = htmlTitle;
  }

  public int getXmlToHtmlOutputDepth() {
    return xmlToHtmlOutputDepth;
  }

  /**
   * Return if XML should be rendered for test results in the generated HTML report.
   * 
   * @return the generateXmlOutput {@code true} if XML should be rendered, or {@code false} otherwise
   */
  public boolean isGenerateXmlOutput() {
    return generateXmlOutput;
  }

  /**
   * Set if the XML should be rendered for test results in the generated HTML report.
   * 
   * @param generateXmlOutput
   *          {@code true} if XML should be rendered, or {@code false} otherwise
   */
  public void setGenerateXmlOutput(boolean generateXmlOutput) {
    this.generateXmlOutput = generateXmlOutput;
  }

  /**
   * Defines the number of XML elements to output below the target element when generating XML
   * document views in HTML based on the XPath context for a test result.
   * 
   * @param xmlToHtmlOutputDepth
   *          a positive integer value indicating the depth to use
   * @throws IllegalArgumentException
   *           if the provided depth is not a positive integer value
   */
  public void setXmlToHtmlOutputDepth(int xmlToHtmlOutputDepth) {
    if (xmlToHtmlOutputDepth < 1) {
      throw new IllegalArgumentException(
          "Illegal depth: " + xmlToHtmlOutputDepth + ". The depth must be greater than 0");
    }
    this.xmlToHtmlOutputDepth = xmlToHtmlOutputDepth;
  }

  /**
   * Retrieve the child depth for generated XML content in the report. The depth is the number of
   * children to include relative to the target node before truncating.
   * 
   * @return the xmlToHtmlOutputChildLimit
   */
  public int getXmlToHtmlOutputChildLimit() {
    return xmlToHtmlOutputChildLimit;
  }

  /**
   * Used to set a limit for the number of child elements under the result target element to render. A
   * value of -1 will render all children, while a positive result will enforce a limit.
   * 
   * @param xmlToHtmlOutputChildLimit
   *          the xmlToHtmlOutputChildLimit to set
   * @throws IllegalArgumentException
   *           if the provided limit is not -1 or a positive integer value
   */
  public void setXmlToHtmlOutputChildLimit(int xmlToHtmlOutputChildLimit) {
    if (xmlToHtmlOutputChildLimit != -1 && xmlToHtmlOutputChildLimit < 1) {
      throw new IllegalArgumentException(
          "Illegal limit: " + xmlToHtmlOutputChildLimit + ". The limit must be -1 or greater than 0");
    }
    this.xmlToHtmlOutputChildLimit = xmlToHtmlOutputChildLimit;
  }

  public URI getXslTemplateExtension() {
    return xslTemplateExtension;
  }

  /**
   * The current rendering limit for test items.
   * 
   * @return the testResultLimit
   */
  public int getTestResultLimit() {
    return testResultLimit;
  }

  /**
   * Establishes a limit for the number of test items to render in the HTML report. If positive, the
   * items will be rendered up to this limit and any remaining items will be rendered using a single
   * entry that provides a count of the remaining issues that were omitted.
   * 
   * @param testResultLimit
   *          the testResultLimit to set
   */
  public void setTestResultLimit(int testResultLimit) {
    this.testResultLimit = testResultLimit;
  }

  /**
   * Used to specify an XSL template that extends the based reporting template
   * ({@link #DEFAULT_RESULT_XSL_URL}). Technically, this can be used to specify an alternate report
   * XSL as well, since there is no requirement to actually extend the base template.
   * 
   * @param file
   *          the template to use
   * @throws IOException
   *           if the file doesn't exist or it is invalid
   */
  public void setXslTemplateExtension(File file) throws IOException {
    Objects.requireNonNull(file);
    if (!file.exists()) {
      throw new IOException("File does not exist: " + file.getPath());
    }
    if (!file.isFile()) {
      throw new IOException("The file is not a file: " + file.getPath());
    }
    this.xslTemplateExtension = file.toURI();
  }

  public void setXslTemplateExtension(URI uri) {
    Objects.requireNonNull(uri);
    this.xslTemplateExtension = uri;
  }

  public String getTargetName() {
    return targetName;
  }

  public void setTargetName(String targetName) {
    this.targetName = targetName;
  }

  public void generate(File resultFile, File outputFile) throws TransformerException, IOException {
    generate(resultFile.toURI().toURL(), outputFile);
  }

  /**
   * Generates an HTML report using the provided results.
   * 
   * @param results
   *          the assessment result file location to read the results XML from
   * @param reportOutputFile
   *          the file location to write the report to
   * @throws TransformerException
   *           if an error occurred while transforming the results.
   * @throws IOException
   *           if an error occured while reading or writing one of the files
   */
  public void generate(URL results, File reportOutputFile) throws TransformerException, IOException {
    try (InputStream is = results.openStream()) {
      try (OutputStream os = new BufferedOutputStream(new FileOutputStream(reportOutputFile))) {
        generate(new StreamSource(is, results.toString()), new StreamResult(os));
      }
    }
  }

  /**
   * Generates an HTML report, using a Decima XML result source.
   * 
   * @param resultSource
   *          the Decima XML result source to use
   * @param reportResult
   *          the result to write the report to
   * @throws TransformerException
   *           if an error occurs while performing the XSL transform
   * @throws IOException
   *           if an error occurred while reading the source or writing the result
   */
  public void generate(Source resultSource, Result reportResult) throws TransformerException, IOException {
    ExtendedXSLTransformer xslTransformer = new ExtendedXSLTransformer();
    TransformerFactory factory = xslTransformer.getTransformerFactory();

    URI xslTemplate = getXslTemplateExtension();
    if (xslTemplate == null) {
      try {
        xslTemplate = new URI(DEFAULT_RESULT_XSL_URL);
      } catch (URISyntaxException e) {
        // this should never happen
        throw new RuntimeException(e);
      }
    }

    URL templateURL;
    try {
      templateURL = xslTemplate.toURL();
    } catch (MalformedURLException e) {
      throw new TransformerException("Invalid URL: " + xslTemplate.toString(), e);
    }

    try (InputStream isTemplate = templateURL.openStream()) {

      // Load the XSL template
      Source source = new StreamSource(isTemplate);

      // setup the transformer
      Transformer transformer = factory.newTransformer(source);

      transformer.setParameter(XSL_PARAM_GENERATE_XML_OUTPUT, isGenerateXmlOutput());
      transformer.setParameter(XSL_PARAM_XML_OUTPUT_DEPTH, getXmlToHtmlOutputDepth());
      transformer.setParameter(XSL_PARAM_XML_OUTPUT_CHILD_LIMIT, getXmlToHtmlOutputChildLimit());
      transformer.setParameter(XSL_PARAM_TEST_RESULT_LIMIT, getTestResultLimit());

      if (!isIgnoreNotTestedResults()) {
        transformer.setParameter(XSL_PARAM_IGNORE_NOT_TESTED_RESULTS, Boolean.FALSE);
      }

      if (!isIgnoreOutOfScopeResults()) {
        transformer.setParameter(XSL_PARAM_IGNORE_OUT_OF_SCOPE_RESULTS, Boolean.FALSE);
      }
      String title = getHtmlTitle();
      if (title != null) {
        transformer.setParameter(XSL_PARAM_HTML_TITLE, title);
      }

      transformer.transform(resultSource, reportResult);
    }
  }
}
