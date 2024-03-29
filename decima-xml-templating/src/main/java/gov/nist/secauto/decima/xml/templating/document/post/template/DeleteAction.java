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

package gov.nist.secauto.decima.xml.templating.document.post.template;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathFactory;

import java.util.List;
import java.util.Map;

/**
 * Deletes elements and/or attributes from an XML document based on the nodes returned by an XPath
 * query.
 */
public class DeleteAction
    extends AbstractXPathAction<Object> {
  /**
   * Construct a new DeleteAction based on an XPath string using the provided map to map XML prefixes
   * to namespaces.
   * 
   * @param xpathFactory
   *          the XPath implementation to use
   * @param xpath
   *          the XPath string
   * @param prefixToNamespaceMap
   *          a map of XML prefixes to namespaces used in the provided XPath
   */
  public DeleteAction(XPathFactory xpathFactory, String xpath, Map<String, String> prefixToNamespaceMap) {
    super(xpathFactory, xpath, Filters.fpassthrough(), prefixToNamespaceMap);
  }

  /**
   * Deletes any elements or attributes returned by the XPath expression.
   */
  @Override
  protected void process(List<Object> results) throws ActionException {
    for (Object child : results) {
      if (child instanceof Element) {
        ((Element) child).detach();
      } else if (child instanceof Attribute) {
        ((Attribute) child).detach();
      } else {
        throw new ActionProcessingException(
            "DeleteAction: the selected elements must be an element or attribute. Found " + child.getClass());
      }
    }
  }
}