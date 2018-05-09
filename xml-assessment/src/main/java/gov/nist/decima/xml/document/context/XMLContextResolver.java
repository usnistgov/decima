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

package gov.nist.decima.xml.document.context;

import gov.nist.decima.core.document.Context;
import gov.nist.decima.xml.document.XPathContext;

import org.jdom2.Attribute;
import org.jdom2.Content;

public interface XMLContextResolver {
  /**
   * Retrieve a {@link Context} entry for the provided JDOM {@link Content}.
   * 
   * @param content
   *          the JDOM {@link Content} to retrieve a {@link Context} entry for
   * @return the {@link Context} entry for the provided JDOM {@link Content}
   */
  XPathContext getContext(Content content);

  /**
   * Retrieve a {@link Context} entry for the provided JDOM {@link Attribute}.
   * 
   * @param attribute
   *          the JDOM {@link Attribute} to retrieve a {@link Context} entry for
   * @return the {@link Context} entry for the provided JDOM {@link Attribute}
   */
  XPathContext getContext(Attribute attribute);

  /**
   * Creates an XPath expression that can be used to query the provided content.
   * 
   * @param content
   *          the {@link Content} node to build the XPath expression for
   * @return an XPath string
   */
  String getXPath(Content content);

  /**
   * Creates an XPath expression that can be used to query the provided {@link Attribute}.
   * 
   * @param attribute
   *          the {@link Attribute} to build the XPath expression for
   * @return an XPath string
   */
  String getXPath(Attribute attribute);

  /**
   * Lookup the system identifier of the provided element. Implementations of this interface may
   * allow for a single document to aggregate multiple documents that each have different systemIds.
   * In such a case, this method is expected to return the systemId of the inner document.
   * 
   * @param element
   *          the element to lookup a systemId for
   * @return the systemId of the element
   */
  String getSystemId(Content element);

}
