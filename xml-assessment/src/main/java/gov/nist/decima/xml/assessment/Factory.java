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

package gov.nist.decima.xml.assessment;

import gov.nist.decima.core.Decima;
import gov.nist.decima.core.assessment.Assessment;
import gov.nist.decima.core.assessment.result.TestResult;
import gov.nist.decima.core.document.DocumentException;
import gov.nist.decima.core.requirement.RequirementsParser;
import gov.nist.decima.xml.assessment.result.XPathContext;
import gov.nist.decima.xml.assessment.schema.SchemaAssessment;
import gov.nist.decima.xml.assessment.schematron.DefaultSchematronHandler;
import gov.nist.decima.xml.assessment.schematron.SchematronAssessment;
import gov.nist.decima.xml.assessment.schematron.SchematronHandler;
import gov.nist.decima.xml.document.CompositeXMLDocument;
import gov.nist.decima.xml.document.JDOMDocument;
import gov.nist.decima.xml.document.SimpleXPathContext;
import gov.nist.decima.xml.document.XMLDocument;
import gov.nist.decima.xml.requirement.XMLRequirementsParser;
import gov.nist.decima.xml.schematron.DefaultSchematronCompiler;
import gov.nist.decima.xml.schematron.Schematron;
import gov.nist.decima.xml.schematron.SchematronCompilationException;
import gov.nist.decima.xml.schematron.SchematronCompiler;

import org.jdom2.JDOMException;
import org.jdom2.located.Located;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Source;

public class Factory extends Decima {
  public static XMLDocument newXMLDocument(File file) throws FileNotFoundException, DocumentException {
    return new JDOMDocument(file);
  }

  public static XMLDocument newXMLDocument(URL url) throws DocumentException {
    return new JDOMDocument(url);
  }

  public static XMLDocument newXMLDocument(InputStream is, String systemId)
      throws DocumentException, MalformedURLException {
    return new JDOMDocument(is, systemId);
  }

  public static CompositeXMLDocument newCompositeXMLDocument(File file, Map<String, ? extends XMLDocument> templates)
      throws DocumentException, FileNotFoundException {
    return new CompositeXMLDocument(file, templates);
  }

  public static CompositeXMLDocument newCompositeXMLDocument(URL url, Map<String, ? extends XMLDocument> templates)
      throws DocumentException {
    return new CompositeXMLDocument(url, templates);
  }

  public static XPathContext newXPathContext(String xpath, String systemId, Located located) {
    return new SimpleXPathContext(xpath, systemId, located.getLine(), located.getColumn());
  }

  public static XPathContext newXPathContext(String xpath, String systemId, int line, int column) {
    return new SimpleXPathContext(xpath, systemId, line, column);
  }

  public static RequirementsParser newXMLRequirementsParser() {
    return XMLRequirementsParser.instance();
  }

  /**
   * Constructs a requirements parser with a list of extension schema that describe possible
   * requirements definition extensions.
   * 
   * @param extensionSchemaSources
   *          extension schema that describe requirements definition extensions
   * @return the new requirements parser instance
   * @throws JDOMException
   *           if an error occurred while constructing the JDOM2 SAX parser
   * @throws MalformedURLException
   *           if one of the schema sources was a malformed URL
   * @throws SAXException
   *           if an error occurred while parsing the schema
   */
  public static RequirementsParser newXMLRequirementsParser(List<Source> extensionSchemaSources)
      throws MalformedURLException, JDOMException, SAXException {
    return new XMLRequirementsParser(extensionSchemaSources);
  }

  /**
   * Create a new {@link Schematron} instance that represents a pre-compiled schematron.
   * 
   * @param schematron
   *          the ISO Schematron ruleset to load
   * @return a {@link Schematron} instance that can be later evaluated
   * @throws SchematronCompilationException
   *           if an error occurred while preparing the {@link Schematron} instance
   */
  public static synchronized Schematron newSchematron(URL schematron) throws SchematronCompilationException {
    if (SCHEMATRON_COMPILER_INSTANCE == null) {
      SCHEMATRON_COMPILER_INSTANCE = new DefaultSchematronCompiler();
    }
    return SCHEMATRON_COMPILER_INSTANCE.newSchematron(schematron);
  }

  public static SchematronAssessment newSchematronAssessment(URL schematron) throws SchematronCompilationException {
    return newSchematronAssessment(newSchematron(schematron));
  }

  public static SchematronAssessment newSchematronAssessment(URL schematron, String phase)
      throws SchematronCompilationException {
    return newSchematronAssessment(newSchematron(schematron), phase);
  }

  public static SchematronAssessment newSchematronAssessment(Schematron schematron) {
    return newSchematronAssessment(schematron, null, new DefaultSchematronHandler(schematron));
  }

  public static SchematronAssessment newSchematronAssessment(Schematron schematron, String phase) {
    return newSchematronAssessment(schematron, phase, new DefaultSchematronHandler(schematron));
  }

  public static SchematronAssessment newSchematronAssessment(Schematron schematron, String phase,
      SchematronHandler schematronHandler) {
    return new SchematronAssessment(schematron, phase, schematronHandler);
  }

  public static SchemaAssessment newSchemaAssessment(String derivedRequirementId) {
    return newSchemaAssessment(derivedRequirementId, new LinkedList<>());
  }

  /**
   * Constructs a new XML schema-based {@link Assessment} that can be used to validate assessed
   * documents against a set of XML schema. The result of the {@link SchemaAssessment} is reported
   * against a single provided derived requirement identifier.
   * 
   * @param derivedRequirementId
   *          the identifier of the derived requirement to report {@link TestResult} instances against
   *          based on the schema validation results
   * @param schemaSources
   *          a collection of {@link Source} instances that point to schema resources
   * @return the new schema assessment instance
   */
  public static SchemaAssessment newSchemaAssessment(String derivedRequirementId,
      List<? extends Source> schemaSources) {
    return new SchemaAssessment(derivedRequirementId, schemaSources);
  }

  private static SchematronCompiler SCHEMATRON_COMPILER_INSTANCE;

  private Factory() {
    // Prevent construction
  }
}
