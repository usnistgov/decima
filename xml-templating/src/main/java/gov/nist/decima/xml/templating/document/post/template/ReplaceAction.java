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

package gov.nist.decima.xml.templating.document.post.template;

import gov.nist.decima.core.util.ObjectUtil;

import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathFactory;

import java.util.List;
import java.util.Map;

/**
 * Replaces existing elements returned by an XPath query over an XML document with one or more new
 * elements.
 */
public class ReplaceAction extends AbstractXPathAction<Element> {
  private final List<Element> contentNodes;

  /**
   * Construct a new ReplaceAction based on an XPath string using the provided map to map XML
   * prefixes to namespaces within the XPath.
   * 
   * @param xpathFactory
   *          the XPath implementation to use
   * @param xpath
   *          the XPath string
   * @param prefixToNamespaceMap
   *          a map of XML prefixes to namespaces used in the provided XPath
   * @param contentNodes
   *          a list of new elements to use as the replacement
   */
  public ReplaceAction(XPathFactory xpathFactory, String xpath, Map<String, String> prefixToNamespaceMap,
      List<Element> contentNodes) {
    super(xpathFactory, xpath, Filters.element(), prefixToNamespaceMap);
    ObjectUtil.requireNonEmpty(contentNodes);
    this.contentNodes = contentNodes;
  }

  /**
   * Retrieves the elements to use as the replacement.
   * 
   * @return a list of elements
   */
  public List<Element> getContentNodes() {
    return contentNodes;
  }

  /**
   * Replaces existing elements based on the provided XPath results.
   */
  @Override
  protected void process(List<Element> results) throws ActionException {
    for (Element child : results) {
      if (child.isRootElement()) {
        throw new ActionProcessingException(
            "ReplaceAction: the selected element must not be the root element of the document");
      }
      Element parent = child.getParentElement();
      int index = parent.indexOf(child);

      boolean extraElement = false;
      for (Element newElement : getContentNodes()) {
        // Make a copy to insert into the DOM
        newElement = newElement.clone();

        if (extraElement) {
          if (index >= parent.getContentSize()) {
            parent.addContent(newElement);
          } else {
            parent.addContent(index, newElement);
          }
        } else {
          parent.setContent(index, newElement);
          extraElement = true;
        }
        index++;
      }

      child.detach();
    }
  }
}