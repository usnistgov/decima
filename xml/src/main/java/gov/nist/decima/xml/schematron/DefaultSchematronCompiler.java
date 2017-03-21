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
package gov.nist.decima.xml.schematron;

import gov.nist.decima.core.util.URLUtil;
import gov.nist.decima.xml.jdom2.JDOMUtil;
import gov.nist.decima.xml.util.ExtendedXSLTransformer;
import gov.nist.decima.xml.util.XSLTransformer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.transform.JDOMResult;

import java.io.IOException;
import java.net.URL;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.TransformerHandler;

public class DefaultSchematronCompiler implements SchematronCompiler {
  private static final Logger logger = LogManager.getLogger(DefaultSchematronCompiler.class);

  public static final String TEMPLATE_BASE = "classpath:xsl/schematron/";
  public static final String DSDL_INCLUDE_TEMPLATE = TEMPLATE_BASE + "iso_dsdl_include.xsl";
  public static final String ABSTRACT_EXPAND_TEMPLATE = TEMPLATE_BASE + "iso_abstract_expand.xsl";

  private final XSLTransformer transformer;
  private final Templates dsdlIncludeTemplate;
  private final Templates abstractExpandTemplate;
  private boolean includeSchematron = false;
  private boolean includeCRDL = false;
  private boolean includeXInclude = false;
  private boolean includeDTLL = false;
  private boolean includeRelaxNG = false;
  private boolean includeXLink = false;
  private String expandAbstractForSchemaId = null;

  public DefaultSchematronCompiler() throws SchematronCompilationException {
    this(new ExtendedXSLTransformer());
  }

  protected DefaultSchematronCompiler(XSLTransformer transformer) throws SchematronCompilationException {
    this.transformer = transformer;

    try {
      this.dsdlIncludeTemplate
          = getXSLTransformer().getTransformerFactory().newTemplates(URLUtil.getSource(DSDL_INCLUDE_TEMPLATE));
      this.abstractExpandTemplate
          = getXSLTransformer().getTransformerFactory().newTemplates(URLUtil.getSource(ABSTRACT_EXPAND_TEMPLATE));
    } catch (TransformerConfigurationException | IOException e) {
      throw new SchematronCompilationException(e);
    }
  }

  public XSLTransformer getXSLTransformer() {
    return transformer;
  }

  public boolean isIncludeSchematron() {
    return includeSchematron;
  }

  public void setIncludeSchematron(boolean includeSchematron) {
    this.includeSchematron = includeSchematron;
  }

  public boolean isIncludeCRDL() {
    return includeCRDL;
  }

  public void setIncludeCRDL(boolean includeCRDL) {
    this.includeCRDL = includeCRDL;
  }

  public boolean isIncludeXInclude() {
    return includeXInclude;
  }

  public void setIncludeXInclude(boolean includeXInclude) {
    this.includeXInclude = includeXInclude;
  }

  public boolean isIncludeDTLL() {
    return includeDTLL;
  }

  public void setIncludeDTLL(boolean includeDTLL) {
    this.includeDTLL = includeDTLL;
  }

  public boolean isIncludeRelaxNG() {
    return includeRelaxNG;
  }

  public void setIncludeRelaxNG(boolean includeRelaxNG) {
    this.includeRelaxNG = includeRelaxNG;
  }

  public boolean isIncludeXLink() {
    return includeXLink;
  }

  public void setIncludeXLink(boolean includeXLink) {
    this.includeXLink = includeXLink;
  }

  @Override
  public Schematron newSchematron(URL schematron) throws SchematronCompilationException {
    if (logger.isDebugEnabled()) {
      logger.debug("Pre-Compiling schematron ruleset: {}", schematron.toString());
    }
    TransformerHandler thRoot = null;
    TransformerHandler thEnd = null;

    if (expandAbstractForSchemaId != null) {
      if (logger.isDebugEnabled()) {
        logger.debug("Expanding abstract patterns in the transformation pipeline");
      }
      try {
        thRoot = getXSLTransformer().addChain(abstractExpandTemplate, thRoot);
      } catch (TransformerConfigurationException e) {
        logger.error(e);
        throw new SchematronCompilationException(e);
      }
      thRoot.getTransformer().setParameter("schema-id", expandAbstractForSchemaId);
      // Identify this transform as the last one
      thEnd = thRoot;
    }

    if (isIncludeSchematron() || isIncludeCRDL() || isIncludeXInclude() || isIncludeDTLL() || isIncludeRelaxNG()
        || isIncludeXLink()) {
      if (logger.isDebugEnabled()) {
        logger.debug("Processing includes in the transformation pipeline");
      }
      try {
        thRoot = getXSLTransformer().addChain(dsdlIncludeTemplate, thRoot);
      } catch (TransformerConfigurationException e) {
        logger.error(e);
        throw new SchematronCompilationException(e);
      }
      thRoot.getTransformer().setParameter("include-schematron", isIncludeSchematron());
      thRoot.getTransformer().setParameter("include-crdl", isIncludeCRDL());
      thRoot.getTransformer().setParameter("include-xinclude", isIncludeXInclude());
      thRoot.getTransformer().setParameter("include-dtll", isIncludeDTLL());
      thRoot.getTransformer().setParameter("include-relaxng", isIncludeRelaxNG());
      thRoot.getTransformer().setParameter("include-xlink", isIncludeXLink());

      if (thEnd == null) {
        // Identify this transform as the last one
        thEnd = thRoot;
      }
    }

    Document preprocessedSchematron;
    if (thRoot != null) {
      JDOMResult retval = new JDOMResult();
      thEnd.setResult(retval);

      if (logger.isDebugEnabled()) {
        logger.debug("Executing the transformation pipeline");
      }
      Transformer transformer;
      try {
        transformer = getXSLTransformer().getTransformerFactory().newTransformer();
      } catch (TransformerConfigurationException e) {
        logger.error(e);
        throw new SchematronCompilationException(e);
      }
      try {
        transformer.transform(URLUtil.getSource(schematron), new SAXResult(thRoot));
      } catch (TransformerException | IOException e) {
        logger.error(e);
        throw new SchematronCompilationException(e);
      }
      preprocessedSchematron = retval.getDocument();
    } else {
      try {
        preprocessedSchematron = new SAXBuilder().build(schematron);
      } catch (JDOMException | IOException e) {
        logger.error(e);
        throw new SchematronCompilationException(e);
      }
    }

    if (logger.isTraceEnabled()) {
      logger.trace("Resulting compiled schematron: {}", JDOMUtil.toString(preprocessedSchematron));
    }
    preprocessedSchematron.setBaseURI(schematron.toString());
    try {
      return new DefaultSchematron(preprocessedSchematron, getXSLTransformer().getTransformerFactory());
    } catch (TransformerConfigurationException | IOException e) {
      logger.error(e);
      throw new SchematronCompilationException(e);
    }
  }
}
