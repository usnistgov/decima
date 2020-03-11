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

package gov.nist.secauto.decima.xml.assessment.schematron;

import gov.nist.secauto.decima.xml.jdom2.JDOMUtil;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.SAXEngine;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class SVRLParser {
  // private static final Logger log = LogManager.getLogger(SVRLParser.class);

  private static final String NS_PREFIX = "ns-prefix-in-attribute-values";
  private static final String ACTIVE_PATTERN = "active-pattern";
  private static final String FIRED_RULE = "fired-rule";
  private static final String SUCCESSFUL_REPORT = "successful-report";
  private static final String FAILED_ASSERT = "failed-assert";

  public static void parse(SVRLHandler handler, File file) throws JDOMException, IOException {
    parse(handler, file, false);
  }

  /**
   * Parses a SVRL result using the provided handler to process SVRL elements.
   * 
   * @param handler
   *          the handler to use to process the SVRL result
   * @param file
   *          the SVRL file
   * @param validate
   *          if {@code true} perform schema validation on the SVRL file
   * @throws JDOMException
   *           if an error occured while parsing the SVRL file
   * @throws IOException
   *           if an error occurred that prevented the SVRL results from being parsed
   */
  public static void parse(SVRLHandler handler, File file, boolean validate) throws JDOMException, IOException {
    SAXEngine saxEngine;
    if (validate) {
      try {
        saxEngine = JDOMUtil.newValidatingSAXEngine(new URL("classpath:schema/schematron/iso-schematron-svrl.xsd"));
      } catch (SAXException | MalformedURLException | JDOMException e) {
        // These exceptions should never happen
        throw new RuntimeException(e);
      }
    } else {
      saxEngine = new SAXBuilder();
    }

    Document document = saxEngine.build(file);
    parse(handler, document);
  }

  /**
   * Parses a SVRL result, provided as a JDOM {@link Document}, using the provided handler to process
   * SVRL elements.
   * 
   * @param handler
   *          the handler to use to process the SVRL result
   * @param document
   *          the SVRL results
   */
  public static void parse(SVRLHandler handler, Document document) {
    // @SuppressWarnings("unused")
    // Map<String, String> namespaceMap = new HashMap<String, String>();
    //
    Element outputElement = document.getRootElement();
    for (Element child : outputElement.getChildren()) {
      switch (child.getName()) {
      case NS_PREFIX:
        handler.handleNSPrefix(child);
        break;
      case ACTIVE_PATTERN:
        handler.handleActivePattern(child);
        break;
      case FIRED_RULE:
        handler.handleFiredRule(child);
        break;
      case SUCCESSFUL_REPORT:
        handler.handleSuccessfulReport(child);
        break;
      case FAILED_ASSERT:
        handler.handleFailedAssert(child);
        break;
      default:
        // ignore it
      }
    }
  }

  private SVRLParser() {
    // prevent construction
  }
}
