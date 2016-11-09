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

package gov.nist.decima.core.assessment.result;

import gov.nist.decima.core.util.ExtendedXSLTransformer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class ReportGenerator {
  private static final String DEFAULT_RESULT_XSL_URL = "classpath:xsl/result.xsl";
  private static final String XSL_PARAM_BOOTSTRAP_PATH = "bootstrap-path";
  private static final Pattern URI_SEPERATOR_PATTERN = Pattern.compile("\\/");
  private static final String URI_SEPERATOR = "/";
  private static final String XSL_PARAM_IGNORE_NOT_TESTED_RESULTS = "ignore-not-tested-results";
  private static final String XSL_PARAM_IGNORE_OUT_OF_SCOPE_RESULTS = "ignore-outofscope-results";
  private static final String XSL_PARAM_XML_OUTPUT_DEPTH = "xml-output-depth";
  private static final String XSL_PARAM_TARGET_NAME = "target-name";

  private boolean ignoreOutOfScopeResults = false;
  private boolean ignoreNotTestedResults = false;
  private int xmlToHtmlOutputDepth = 1;
  private URI xslTemplateExtension;
  private File bootstrapDir = new File("bootstrap");
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

  public int getXmlToHtmlOutputDepth() {
    return xmlToHtmlOutputDepth;
  }

  /**
   * Defines the number of XML elements to output below the target element when generating XML
   * document views in HTML based on the XPath context for a test result.
   * 
   * @param xmlToHtmlOutputDepth
   *          a positive integer value indicating the depth to use
   */
  public void setXmlToHtmlOutputDepth(int xmlToHtmlOutputDepth) {
    if (xmlToHtmlOutputDepth < 1) {
      throw new IllegalArgumentException(
          "Illegal depth: " + xmlToHtmlOutputDepth + ". The depth must be greater than 0");
    }
    this.xmlToHtmlOutputDepth = xmlToHtmlOutputDepth;
  }

  public URI getXslTemplateExtension() {
    return xslTemplateExtension;
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

  public File getBootstrapDir() {
    return bootstrapDir;
  }

  /**
   * Establishes the location of the bootstrap files to use for the generated report.
   * 
   * @param dir
   *          the directory containing the bootstrap files
   * @throws IOException
   *           if the directory doesn't exist or it is invalid
   */
  public void setBootstrapDir(File dir) throws IOException {
    Objects.requireNonNull(dir);
    if (!dir.exists()) {
      throw new IOException("The bootstrap directory does not exist: " + dir.getPath());
    }
    if (!dir.isDirectory()) {
      throw new IOException("The provided directory is not a directory: " + dir.getPath());
    }
    this.bootstrapDir = dir;
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
      String bootstrapPath = getBootstrapPath(reportOutputFile);
      generate(new StreamSource(is, results.toString()), new StreamResult(reportOutputFile), bootstrapPath);
    }
  }

  /**
   * Generates an HTML report, using a Decima XML result source.
   * @param resultSource the Decima XML result source to use
   * @param reportResult the {@Result} to write the report to
   * @param bootstrapPath the path to the Bootstrap CSS and JavaScript files 
   * @throws TransformerException if an error occurs while performing the XSL transform
   * @throws IOException
   *           if an error occurred while reading the source or writing the result
   */
  public void generate(Source resultSource, Result reportResult, String bootstrapPath)
      throws TransformerException, IOException {
    // TODO: make bootstrapPath a property?
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

      transformer.setParameter(XSL_PARAM_BOOTSTRAP_PATH, bootstrapPath);
      transformer.setParameter(XSL_PARAM_XML_OUTPUT_DEPTH, getXmlToHtmlOutputDepth());

      if (!isIgnoreNotTestedResults()) {
        transformer.setParameter(XSL_PARAM_IGNORE_NOT_TESTED_RESULTS, Boolean.FALSE);
      }

      if (!isIgnoreOutOfScopeResults()) {
        transformer.setParameter(XSL_PARAM_IGNORE_OUT_OF_SCOPE_RESULTS, Boolean.FALSE);
      }

      String targetName = getTargetName();
      if (targetName != null) {
        transformer.setParameter(XSL_PARAM_TARGET_NAME, targetName);
      }

      transformer.transform(resultSource, reportResult);
    }
  }

  private String getBootstrapPath(File outputFile) {
    URI out = outputFile.toURI().normalize();
    URI bootstrap = getBootstrapDir().toURI().normalize();
    String retval = relativize(out.toString(), bootstrap.toString());
    if (retval.endsWith("/")) {
      retval = retval.substring(0, retval.length() - 1);
    }
    return retval;
  }

  /**
   * Based on code from
   * http://stackoverflow.com/questions/10801283/get-relative-path-of-two-uris-in-java
   */
  public static String relativize(String base, String target) {

    // Split paths into segments
    String[] baseSegments = URI_SEPERATOR_PATTERN.split(base);
    String[] targetSegments = URI_SEPERATOR_PATTERN.split(target);

    // Discard trailing segment of base path
    if (baseSegments.length > 0 && !base.endsWith("/")) {
      baseSegments = Arrays.copyOf(baseSegments, baseSegments.length - 1);
    }

    // Remove common prefix segments
    int segmentIndex = 0;
    while (segmentIndex < baseSegments.length && segmentIndex < targetSegments.length
        && baseSegments[segmentIndex].equals(targetSegments[segmentIndex])) {
      segmentIndex++;
    }

    // Construct the relative path
    // int size = (bSegments.length - i) + (tSegments.length - i);

    StringBuilder retval = new StringBuilder();
    for (int j = 0; j < (baseSegments.length - segmentIndex); j++) {
      retval.append("..");
      if (retval.length() != 0) {
        retval.append(URI_SEPERATOR);
      }
    }

    for (int j = segmentIndex; j < targetSegments.length; j++) {
      retval.append(targetSegments[j]);
      if (retval.length() != 0) {
        retval.append(URI_SEPERATOR);
      }
    }
    return retval.toString();
  }

}
