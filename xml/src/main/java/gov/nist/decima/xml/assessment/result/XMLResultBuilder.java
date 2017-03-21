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

package gov.nist.decima.xml.assessment.result;

import gov.nist.decima.core.assessment.result.AssessmentResults;
import gov.nist.decima.core.assessment.result.BaseRequirementResult;
import gov.nist.decima.core.assessment.result.DerivedRequirementResult;
import gov.nist.decima.core.assessment.result.TestResult;
import gov.nist.decima.core.assessment.result.TestStatus;
import gov.nist.decima.core.document.Context;
import gov.nist.decima.core.document.SourceInfo;
import gov.nist.decima.core.requirement.BaseRequirement;
import gov.nist.decima.core.requirement.DerivedRequirement;
import gov.nist.decima.core.requirement.RequirementsManager;
import gov.nist.decima.xml.document.XPathContext;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

// TODO: Improve the target document handling for error context
public class XMLResultBuilder {
  private static final Namespace RESULT_NAMESPACE
      = Namespace.getNamespace("http://csrc.nist.gov/ns/decima/results/1.0");

  public XMLResultBuilder() {
  }

  /**
   * Writes a provided set of {@link AssessmentResults} to the provided {@link OutputStream} as an
   * XML document.
   * 
   * @param results
   *          the results of an assessment to use
   * @param out
   *          the {@link OutputStream} to write the XML results to
   * @throws IOException
   *           if an error occurs while writing to the provided {@link OutputStream}
   */
  public void write(AssessmentResults results, OutputStream out) throws IOException {
    Document doc = newDocument(results);
    XMLOutputter xout = new XMLOutputter(Format.getPrettyFormat());
    xout.output(doc, out);
  }

  protected void buildSubjects(Element root, AssessmentResults results) {
    Element element = new Element("subjects", root.getNamespace());
    root.addContent(element);

    int nextSubjectId = 1;
    for (SourceInfo info : results.getAssessmentSubjects().values()) {
      Element subject = new Element("subject", root.getNamespace());
      subject.setAttribute("id", "sub" + nextSubjectId++);
      subject.addContent(new Element("href", root.getNamespace()).addContent(info.getSystemId()));

      URI source = info.getSource();
      if (source != null) {
        subject.addContent(new Element("source", root.getNamespace()).addContent(source.toString()));
      }
      element.addContent(subject);
    }
    //
    // // iterate over the subjects
    // for (BaseRequirementResult base : results.getBaseRequirementResults()) {
    // for (DerivedRequirementResult derived : base.getDerivedRequirementResults()) {
    // for (TestResult test : derived.getTestResults()) {
    // Context context = test.getContext();
    // if (context != null) {
    // String systemId = context.getSystemId();
    // if (systemId != null) {
    // // map the systemId
    // }
    // }
    // }
    // }
    // }
  }

  protected void buildProperties(Element root, Map<String, String> properties) {
    Element element = new Element("properties", root.getNamespace());
    root.addContent(element);
    for (Map.Entry<String, String> entry : properties.entrySet()) {
      Element property = new Element("property", root.getNamespace());
      property.setAttribute("name", entry.getKey());
      property.setText(entry.getValue());
      element.addContent(property);
    }
  }

  protected void buildRequirements(Element root, AssessmentResults results) {
    Element element = new Element("requirements", root.getNamespace());
    root.addContent(element);

    RequirementsManager manager = results.getRequirementsManager();
    for (URI definition : manager.getRequirementDefinitions()) {
      Element requirement = new Element("requirement", root.getNamespace());
      requirement.setAttribute("href", definition.toASCIIString());
      element.addContent(requirement);
    }
  }

  protected void buildResults(Element root, AssessmentResults results) {
    Namespace namespace = root.getNamespace();
    Element element = new Element("results", namespace);
    root.addContent(element);
    for (BaseRequirementResult base : results.getBaseRequirementResults()) {
      element.addContent(buildBaseRequirement(base, namespace));
    }
  }

  private static Element buildBaseRequirement(BaseRequirementResult result, Namespace namespace) {
    Element retval = new Element("base-requirement", namespace);

    BaseRequirement base = result.getBaseRequirement();
    retval.setAttribute("id", base.getId());

    Element status = new Element("status", namespace).setText(result.getStatus().name());
    retval.addContent(status);

    for (DerivedRequirementResult derived : result.getDerivedRequirementResults()) {
      retval.addContent(buildDerivedRequirement(derived, namespace));
    }
    return retval;
  }

  private static Element buildDerivedRequirement(DerivedRequirementResult result, Namespace namespace) {
    Element retval = new Element("derived-requirement", namespace);

    DerivedRequirement derived = result.getDerivedRequirement();
    retval.setAttribute("id", derived.getId());

    Element status = new Element("status", namespace).setText(result.getStatus().name());
    retval.addContent(status);
    for (TestResult test : result.getTestResults()) {
      retval.addContent(buildTestResult(test, derived, namespace));
    }

    return retval;
  }

  private static Element buildTestResult(TestResult result, DerivedRequirement derived, Namespace namespace) {
    Element retval = new Element("test", namespace);

    String testId = result.getTestId();
    if (testId != null) {
      retval.setAttribute("test-id-ref", testId);
    }

    TestStatus status = result.getStatus();
    retval.addContent(new Element("status", namespace).setText(status.name()));

    List<String> values = result.getResultValues();
    String message = derived.getMessageText(values.toArray(new String[values.size()]));
    if (message != null) {
      retval.addContent(new Element("message", namespace).setText(message));
    }

    Context context = result.getContext();
    if (context != null) {
      Element location = new Element("location", namespace).setAttribute("line", Integer.toString(context.getLine()))
          .setAttribute("column", Integer.toString(context.getColumn()));
      if (context.getSystemId() != null) {
        location.setAttribute("href", context.getSystemId());
      }

      // TODO: Find a better way to handle this. Maybe a property map?
      if (context instanceof XPathContext) {
        String xpath = ((XPathContext) context).getXPath();
        if (xpath != null) {
          location.setAttribute("xpath", ((XPathContext) context).getXPath());
        }
      }
      retval.addContent(location);
    }

    return retval;
  }

  private static String dateToString(ZonedDateTime dateTime) {
    return dateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
  }

  /**
   * Creates a new JDOM {@link Document} based on the provided {@link AssessmentResults}.
   * 
   * @param results
   *          the results of an assessment to use
   * @return a JDOM {@link Document} containing an XML representation of the results
   */
  public Document newDocument(AssessmentResults results) {
    Element root = new Element("assessment-results", RESULT_NAMESPACE);

    root.setAttribute("start", dateToString(results.getStartTimestamp()));
    root.setAttribute("end", dateToString(results.getEndTimestamp()));

    buildSubjects(root, results);
    Map<String, String> properties = results.getProperties();
    if (!properties.isEmpty()) {
      buildProperties(root, properties);
    }
    buildRequirements(root, results);
    buildResults(root, results);
    Document doc = new Document(root);
    return doc;
  }
}
