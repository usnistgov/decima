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

import org.jdom2.Content;
import org.jdom2.Content.CType;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathFactory;

import java.util.List;
import java.util.Map;

/**
 * Inserts new child elements within existing elements returned by an XPath query over an XML
 * document.
 */
public class InsertChildAction extends AbstractXPathAction<Element> {

  private final List<Element> contentNodes;
  private final Integer index;
  private boolean ignoreWhitespace = true;

  /**
   * Construct a new InsertChildAction based on an XPath string using the provided map to map XML
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
   * @param index
   *          for each element returned by the XPath query, the index position (zero based)
   *          identifies the position within the element's content that the new element is to be
   *          inserted into. A {@code null} value will cause the inserted elements to be appended at
   *          the end of the sequence.
   */
  public InsertChildAction(XPathFactory xpathFactory, String xpath, Map<String, String> prefixToNamespaceMap,
      List<Element> contentNodes, Integer index) {
    super(xpathFactory, xpath, Filters.element(), prefixToNamespaceMap);
    ObjectUtil.requireNonEmpty(contentNodes);
    if (index != null && index < 0) {
      throw new IndexOutOfBoundsException("index value '" + index + "' is not >= 0");
    }
    this.contentNodes = contentNodes;
    this.index = index;
  }

  public boolean isIgnoreWhitespace() {
    return ignoreWhitespace;
  }

  public void setIgnoreWhitespace(boolean ignoreWhitespace) {
    this.ignoreWhitespace = ignoreWhitespace;
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
   * Retrieves the zero-based index position for which new nodes will be inserted.
   * 
   * @return a zero-based index position or {@code null} if new elements should be appended
   */
  public Integer getIndex() {
    return index;
  }

  /**
   * Inserts the child elements based on the provided XPath results.
   */
  @Override
  protected void process(List<Element> results) throws ActionException {
    for (Element parent : results) {
      Integer localIndex = getIndex();
      if (localIndex != null) {
        int childCount = parent.getContent().size();

        int pos = 0;
        for (Content content : parent.getContent()) {
          if (!CType.Element.equals(content.getCType())) {
            ++localIndex;
          }
          if (pos == localIndex) {
            break;
          }
          pos++;
        }

        if (localIndex == childCount) {
          int children = isIgnoreWhitespace() ? parent.getChildren().size() : parent.getContent().size();
          String msg = "Index '" + getIndex() + "' is equal to the number of child elements '" + children
              + ". Do not specify an index in this case.";
          throw new ActionProcessingException(msg,
              new IllegalArgumentException("index should be null instead of " + getIndex()));
        } else if (localIndex > childCount) {
          int children = isIgnoreWhitespace() ? parent.getChildren().size() : parent.getContent().size();
          String msg = "Index '" + getIndex() + "' is greater than the number of child elements '" + children;
          throw new ActionProcessingException(msg, new IndexOutOfBoundsException(msg));
        }
      }

      for (Element newChild : getContentNodes()) {
        newChild = newChild.clone();

        if (localIndex == null) {
          parent.addContent(newChild);
        } else {
          parent.addContent(localIndex++, newChild);
        }

        Namespace childNs = newChild.getNamespace();
        Namespace parentNs = parent.getNamespace();
        if (childNs.getURI().equals(parentNs.getURI()) && !childNs.getPrefix().equals(parentNs.getPrefix())) {
          newChild.setNamespace(parentNs);
        }
      }
    }
  }
}