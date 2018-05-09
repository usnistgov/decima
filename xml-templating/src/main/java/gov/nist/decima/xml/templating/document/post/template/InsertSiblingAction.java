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

package gov.nist.decima.core.document.post.template;

import gov.nist.decima.core.util.ObjectUtil;

import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathFactory;

import java.util.List;
import java.util.Map;

/**
 * Inserts new sibling elements adjacent to existing elements returned by an XPath query over an XML
 * document.
 */
public class InsertSiblingAction extends AbstractXPathAction<Element> {
  private final List<Element> contentNodes;
  private final boolean before;

  /**
   * Construct a new InsertSiblingAction based on an XPath string using the provided map to map XML
   * prefixes to namespaces within the XPath.
   * 
   * @param xpathFactory
   *          the XPath implementation to use
   * @param xpath
   *          the XPath string
   * @param prefixToNamespaceMap
   *          a map of XML prefixes to namespaces used in the provided XPath
   * @param contentNodes
   *          a list of new elements to insert
   * @param before
   *          if {@code true} insert new elements before each result element or after if
   *          {@code false}
   */
  public InsertSiblingAction(XPathFactory xpathFactory, String xpath, Map<String, String> prefixToNamespaceMap,
      List<Element> contentNodes, boolean before) {
    super(xpathFactory, xpath, Filters.element(), prefixToNamespaceMap);
    ObjectUtil.requireNonEmpty(contentNodes);
    this.contentNodes = contentNodes;
    this.before = before;
  }

  /**
   * Retrieves the elements to insert.
   * 
   * @return a list of elements
   */
  public List<Element> getContentNodes() {
    return contentNodes;
  }

  /**
   * Retrieves a boolean value that indicates if inserted sibling elements should be inserted before
   * ({@code true}) or after ({@code false}) each element returned by evaluating the XPath
   * expression.
   * 
   * @return {@code true} if before or {@code false} if after
   */
  public boolean isBefore() {
    return before;
  }

  /**
   * Inserts the sibling elements based on the provided XPath results.
   */
  @Override
  protected void process(List<Element> results) throws ActionException {
    for (Element child : results) {
      if (child.isRootElement()) {
        throw new ActionProcessingException(
            "InsertSiblingAction: the selected element must not be the root element of the document");
      }

      Element parent = child.getParentElement();
      int index = parent.indexOf(child);
      index = isBefore() ? index : index + 1;

      for (Element insertElement : getContentNodes()) {
        insertElement = insertElement.clone();

        if (index >= parent.getContentSize()) {
          parent.addContent(insertElement);
          index++;
        } else {
          parent.addContent(index++, insertElement);
        }
      }
    }
  }
}