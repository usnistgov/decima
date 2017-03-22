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

package gov.nist.decima.xml.jdom2;

import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class NamespaceUtil {

  private NamespaceUtil() {
    // Disable construction
  }

  /**
   * Creates a map of prefix to namespace URIs based on the namespaces in scope for the provided
   * element.
   * 
   * @param element
   *          the element
   * @return a map of prefixes to namespace URIs
   */
  public static Map<String, String> getPrefixToNamespaceMap(Element element) {
    Map<String, String> retval = new HashMap<>();
    for (Namespace ns : element.getNamespacesInScope()) {
      retval.put(ns.getPrefix(), ns.getURI());
    }
    return Collections.unmodifiableMap(retval);
  }

  /**
   * Retrieves a namespace object for a provided element and namespace URI.
   * 
   * @param element
   *          the element
   * @param uri
   *          the namespace URI
   * @return the {@link Namespace} object with the URI or <code>null</code> if a matching namespace
   *         cannot be found
   */
  public static Namespace lookupNamespace(Element element, String uri) {
    Namespace retval = null;
    for (Namespace namespace : element.getNamespacesInScope()) {
      if (namespace.getURI().equals(uri)) {
        retval = namespace;
        break;
      }
    }
    return retval;
  }

  public static Namespace lookupOrUseGeneratedNamespace(Element element, String uri) {
    return lookupOrUseGeneratedNamespace(element, uri, "ns", false);
  }

  public static Namespace lookupOrUseGeneratedNamespace(Element element, String uri, boolean requirePrefix) {
    return lookupOrUseGeneratedNamespace(element, uri, "ns", requirePrefix);
  }

  /**
   * Retrieves a namespace object for a provided element and namespace URI. If that namespace cannot
   * be found, a new {@link Namespace} will be created with the provided defaultPrefix instead.
   * 
   * @param element
   *          the element
   * @param uri
   *          the namespace URI
   * @param defaultPrefix
   *          the prefix to use if a new {@link Namespace} needs to be created
   * @param requirePrefix
   *          if <code>true</code> find or create a namespace that is not the default/blank
   *          namespace prefix
   * @return the {@link Namespace} object associated with the URI
   */
  public static Namespace lookupOrUseGeneratedNamespace(Element element, String uri, String defaultPrefix,
      boolean requirePrefix) {
    Namespace retval = lookupNamespace(element, uri);
    if (retval == null) {
      if (Namespace.XML_NAMESPACE.getURI().equals(uri)) {
        retval = Namespace.XML_NAMESPACE;
      } else {
        retval = generateNewNamespaceWithPrefix(element, uri, defaultPrefix);
      }
    } else if (requirePrefix && "".equals(retval.getPrefix())) {
      retval = generateNewNamespaceWithPrefix(element, uri, defaultPrefix);
    }
    return retval;
  }

  /**
   * Generates a new {@link Namespace} on the element for the provided URI.
   * 
   * @param element
   *          the element
   * @param uri
   *          the namespace URI
   * @param defaultPrefix
   *          the prefix to use if a new {@link Namespace} needs to be created
   * @return a new {@link Namespace} based on the provided information
   */
  public static Namespace generateNewNamespaceWithPrefix(Element element, String uri, String defaultPrefix) {
    Namespace retval = null;
    int prefixCount = 1;
    do {
      String prefix = defaultPrefix + prefixCount++;
      Namespace existing = element.getNamespace(prefix);
      if (existing == null) {
        retval = Namespace.getNamespace(prefix, uri);
      }
    } while (retval == null);
    return retval;
  }
}
