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

package gov.nist.decima.xml.schematron;

import gov.nist.decima.core.util.URLUtil;
import gov.nist.decima.xml.jdom2.JDOMUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.transform.JDOMResult;
import org.jdom2.transform.JDOMSource;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

public class DefaultSchematron implements Schematron {
  private static final Logger logger = LogManager.getLogger(DefaultSchematron.class);

  public static final String SVRL_FOR_XSLT2_TEMPLATE
      = DefaultSchematronCompiler.TEMPLATE_BASE + "iso_svrl_for_xslt2.xsl";

  private final Document processedSchematron;
  private final TransformerFactory transformerFactory;
  private final Templates svrlTemplate;
  private Map<String, Document> phaseToCompiledSchematronMap = new HashMap<>();

  /**
   * Constructs a new {@link Schematron} instance that manages a Schematron that has been fully
   * resolved, but not been pre-compiled. Handling of includes and resolving abstract content can be
   * addressed before calling this method using a {@link SchematronCompiler}. The Schematron process
   * requires two compilations, a first compilation to produce an interim XSL template. The second
   * compilation is against the document to be validated using the generated template for this
   * transform.
   * 
   * @param processedSchematron
   *          the pre-compiled Schematron template with includes, and abstract content already
   *          processed
   * @param transformerFactory
   *          the transformation factory to use
   * @throws TransformerConfigurationException
   *           if an issue occurred while configuring the XSL transformer
   * @throws IOException
   *           if an error occurred while processing the SVRL template
   */
  public DefaultSchematron(Document processedSchematron, TransformerFactory transformerFactory)
      throws TransformerConfigurationException, IOException {
    this.processedSchematron = processedSchematron;
    this.transformerFactory = transformerFactory;
    try {
      this.svrlTemplate = transformerFactory.newTemplates(URLUtil.getSource(SVRL_FOR_XSLT2_TEMPLATE));
    } catch (MalformedURLException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public String getPath() {
    return getProcessedSchematron().getBaseURI();
  }

  @Override
  public Document getProcessedSchematron() {
    return processedSchematron;
  }

  @Override
  public TransformerFactory getTransformerFactory() {
    return transformerFactory;
  }

  @Override
  public Document getCompiledSchematron(String phase) throws SchematronCompilationException {
    Document compiledSchematron = phaseToCompiledSchematronMap.get(phase);

    if (compiledSchematron == null) {
      Document preprocessedSchematron = getProcessedSchematron();

      SAXTransformerFactory stf = (SAXTransformerFactory) getTransformerFactory();

      if (logger.isTraceEnabled()) {
        logger.trace("Compiling template: {}", preprocessedSchematron.getBaseURI());
      }
      TransformerHandler thRoot;
      try {
        thRoot = stf.newTransformerHandler(svrlTemplate);
      } catch (TransformerConfigurationException e) {
        logger.error(e);
        throw new SchematronCompilationException(e);
      }

      if (phase != null) {
        thRoot.getTransformer().setParameter("phase", phase);
      }

      JDOMResult retval = new JDOMResult();
      thRoot.setResult(retval);

      if (logger.isTraceEnabled()) {
        logger.trace("Executing the transformation pipeline");
      }
      Transformer transformer;
      try {
        transformer = stf.newTransformer();
      } catch (TransformerConfigurationException e) {
        // logger.error(e);
        throw new SchematronCompilationException(e);
      }
      try {
        transformer.transform(new JDOMSource(preprocessedSchematron), new SAXResult(thRoot));
      } catch (TransformerException e) {
        // logger.error(e);
        throw new SchematronCompilationException(e);
      }

      if (logger.isTraceEnabled()) {
        logger.trace("Resulting compiled schematron: {}", JDOMUtil.toString(retval.getDocument()));
      }
      compiledSchematron = retval.getDocument();
      compiledSchematron.setBaseURI(preprocessedSchematron.getBaseURI());
      phaseToCompiledSchematronMap.put(phase, compiledSchematron);
    }
    return compiledSchematron;
  }

  @Override
  public void transform(Source xml, Result result) throws SchematronEvaluationException {
    transformInternal(xml, result, null, Collections.emptyMap());
  }

  @Override
  public void transform(Source xml, Result result, String phase) throws SchematronEvaluationException {
    transformInternal(xml, result, phase, Collections.emptyMap());
  }

  @Override
  public void transform(Source xml, Result result, String phase, Map<String, String> parameters)
      throws SchematronEvaluationException {
    Objects.requireNonNull(parameters, "parameters must be non-null");
    transformInternal(xml, result, phase, parameters);
  }

  protected void transformInternal(Source xml, Result result, String phase, Map<String, String> parameters)
      throws SchematronEvaluationException {
    JDOMSource xsl;
    try {
      xsl = new JDOMSource(getCompiledSchematron(phase));
    } catch (SchematronCompilationException e) {
      throw new SchematronEvaluationException(e);
    }

    if (logger.isTraceEnabled()) {
      logger.trace("Generating SVRL for source {} using template {}", xml.getSystemId(), xsl.getSystemId());
    }

    Transformer transformer;
    try {
      TransformerFactory tf = getTransformerFactory();
      transformer = tf.newTransformer(xsl);
    } catch (TransformerConfigurationException e) {
      // logger.error(e);
      throw new SchematronEvaluationException(e);
    }
    if (parameters != null) {
      for (Map.Entry<String, String> entry : parameters.entrySet()) {
        transformer.setParameter(entry.getKey(), entry.getValue());
      }
    }
    try {
      transformer.transform(xml, result);
      if (logger.isTraceEnabled()) {
        logger.trace("Generating SVRL completed");
      }
    } catch (TransformerException e) {
      logger.error(e);
      throw new SchematronEvaluationException(e);
    }
  }

}
