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

package gov.nist.decima.xml.assessment.schema;

import gov.nist.decima.core.document.Context;
import gov.nist.decima.xml.assessment.result.XPathContext;

import org.xml.sax.SAXParseException;

import java.util.Objects;

public class SAXContext implements XPathContext {
  private final SAXParseException exception;
  private final String xpath;
  private final String systemId;

  /**
   * Constructs a Decima {@link Context} based on a {@link SAXParseException}.
   * 
   * @param ex
   *          the {@link SAXParseException} to generate the {@link Context} for
   * @param xpath
   *          the XPath identifying the location where the error occurred
   * @param systemId
   *          the file for which the SAX error originated
   */
  public SAXContext(SAXParseException ex, String xpath, String systemId) {
    Objects.requireNonNull(ex, "ex");
    Objects.requireNonNull(xpath, "xpath");
    Objects.requireNonNull(systemId, "systemId");
    this.exception = ex;
    this.xpath = xpath;
    this.systemId = systemId;
  }

  public SAXParseException getException() {
    return exception;
  }

  @Override
  public int getLine() {
    return getException().getLineNumber();
  }

  @Override
  public int getColumn() {
    return getException().getColumnNumber();
  }

  @Override
  public String getSystemId() {
    return systemId;
  }

  @Override
  public String getXPath() {
    return xpath;
  }
}
