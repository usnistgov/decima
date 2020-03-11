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

import gov.nist.secauto.decima.core.util.ObjectUtil;
import gov.nist.secauto.decima.xml.jdom2.NamespaceUtil;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Adds a new XML attribute to an exiting element.
 */
public class AddAttributeAction extends AbstractXPathAction<Element> {
  private final String namespace;
  private final String name;
  private final String value;

  /**
   * Construct a new AddAttributeAction based on an XPath string using the provided map to map XML
   * prefixes to namespaces.
   * 
   * @param xpathFactory
   *          the XPath implementation to use
   * @param xpath
   *          the XPath string
   * @param prefixToNamespaceMap
   *          a map of XML prefixes to namespaces used in the provided XPath
   * @param namespace
   *          the namespace of the new attribute or {@code null} if the attribute has no namespace
   * @param name
   *          the name of the new attribute
   * @param value
   *          the value of the new attrubute
   */
  public AddAttributeAction(XPathFactory xpathFactory, String xpath, Map<String, String> prefixToNamespaceMap,
      String namespace, String name, String value) {
    super(xpathFactory, xpath, Filters.element(), prefixToNamespaceMap);
    ObjectUtil.requireNullOrNonEmpty(namespace, "namespace must be null or non-empty");
    Objects.requireNonNull(name);
    Objects.requireNonNull(value);
    this.namespace = namespace;
    this.name = name;
    this.value = value;
  }

  /**
   * Retrieve the namespace URI to use for the new attribute.
   * 
   * @return the namespace URI
   */
  public String getNamespace() {
    return namespace;
  }

  /**
   * Retrieve the name of the new attribute.
   * 
   * @return the attribute's name
   */
  public String getName() {
    return name;
  }

  /**
   * Retrieve the new attribute's value.
   * 
   * @return the new attributes value
   */
  public String getValue() {
    return value;
  }

  /**
   * Appends an attribute to each resulting {@code Element}.
   */
  @Override
  protected void process(List<Element> results) throws ActionException {
    for (Element child : results) {

      if (namespace != null) {
        Namespace ns = NamespaceUtil.lookupOrUseGeneratedNamespace(child, getNamespace(), true);
        if (child.getAttribute(getName(), ns) != null) {
          String msg = "An attribute with the name '" + getName() + "' already exists.";
          throw new ActionProcessingException(msg,
              new IllegalArgumentException("modify-attribute should be used instead."));
        }
        child.setAttribute(getName(), getValue(), ns);
      } else {
        if (child.getAttribute(getName()) != null) {
          String msg = "An attribute with the name '" + getName() + "' already exists.";
          throw new ActionProcessingException(msg,
              new IllegalArgumentException("modify-attribute should be used instead."));
        }
        child.setAttribute(getName(), getValue());
      }
    }

  }
}