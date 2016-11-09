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

package gov.nist.decima.core.jdom2.saxon.xpath;

import net.sf.saxon.om.NamespaceResolver;

import org.jdom2.Namespace;
import org.jdom2.filter.Filter;
import org.jdom2.xpath.util.AbstractXPathCompiled;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathVariableResolver;

public class CompiledXPath<T, U extends javax.xml.xpath.XPathFactory>
    extends AbstractXPathCompiled<T> implements XPathVariableResolver {
  private final U xpathFactory;
  private XPath xpath;

  /**
   * Constructs a compiled XPath expression.
   * @param xpathFactory the XPath factory to use to compile the XPath
   * @param query the XPath query string
   * @param filter a filter to determine the type of elements to be returned
   * @param variables a mapping of variables used in the XPath
   * @param namespaces the namespaces that are used in the XPath
   */
  public CompiledXPath(U xpathFactory, String query, Filter<T> filter,
      Map<String, Object> variables, Namespace[] namespaces) {
    super(query, filter, variables, namespaces);
    this.xpathFactory = xpathFactory;
    this.xpath = xpathFactory.newXPath();
    this.xpath.setNamespaceContext(getNamespaceContext(namespaces));
    this.xpath.setXPathVariableResolver(this);
  }

  protected U getXPathFactory() {
    return xpathFactory;
  }

  private static javax.xml.namespace.NamespaceContext getNamespaceContext(Namespace[] namespaces) {

    return new TranslatedNamespaceContext(namespaces);
  }

  private static class TranslatedNamespaceContext
      implements javax.xml.namespace.NamespaceContext, NamespaceResolver {
    Map<String, String> prefixToNamespaceMap = new HashMap<>();
    Map<String, Set<String>> namespaceToPrefixesMap = new HashMap<>();

    public TranslatedNamespaceContext(Namespace[] namespaces) {
      for (Namespace namespace : namespaces) {
        initNamespace(namespace);
      }
    }

    private void initNamespace(Namespace namespace) {
      String prefix = namespace.getPrefix();
      String uri = namespace.getURI();

      prefixToNamespaceMap.put(prefix, uri);

      Set<String> prefixes = namespaceToPrefixesMap.get(uri);
      if (prefixes == null) {
        prefixes = new LinkedHashSet<>();
        namespaceToPrefixesMap.put(uri, prefixes);
      }
      prefixes.add(prefix);
    }

    @Override
    public String getNamespaceURI(String prefix) {
      return prefixToNamespaceMap.get(prefix);
    }

    @Override
    public String getPrefix(String namespaceURI) {
      Set<String> prefixes = namespaceToPrefixesMap.get(namespaceURI);

      String retval;
      if (prefixes == null) {
        retval = null;
      } else {
        retval = prefixes.iterator().next();
      }
      return retval;
    }

    @Override
    public Iterator<String> getPrefixes(String namespaceURI) {
      Set<String> prefixes = namespaceToPrefixesMap.get(namespaceURI);
      Iterator<String> retval;
      if (prefixes == null) {
        retval = null;
      } else {
        retval = Collections.unmodifiableSet(prefixes).iterator();
      }
      return retval;
    }

    @Override
    public String getURIForPrefix(String prefix, boolean useDefault) {
      return getNamespaceURI(prefix);
    }

    @Override
    public Iterator<String> iteratePrefixes() {
      return Collections.unmodifiableSet(prefixToNamespaceMap.keySet()).iterator();
    }
  }

  @Override
  protected List<?> evaluateRawAll(Object context) {
    List<Object> result;
    try {
      @SuppressWarnings("unchecked")
      List<Object> nodes
          = (List<Object>) xpath.evaluate(getExpression(), context, XPathConstants.NODESET);
      result = nodes;
    } catch (XPathExpressionException e) {
      throw new RuntimeException(e.getLocalizedMessage(), e);
    }

    return result;
  }

  @Override
  protected Object evaluateRawFirst(Object context) {
    try {
      return xpath.evaluate(getExpression(), context, XPathConstants.NODE);
    } catch (XPathExpressionException e) {
      throw new RuntimeException(e.getLocalizedMessage(), e);
    }
  }

  @Override
  public Object resolveVariable(QName variableName) {
    String prefix = variableName.getPrefix();
    String uri = variableName.getNamespaceURI();
    String localName = variableName.getLocalPart();
    //
    // if (uri == null) {
    // uri = "";
    // }
    // if (prefix == null) {
    // prefix = "";
    // }
    try {
      if ("".equals(uri)) {
        uri = getNamespace(prefix).getURI();
      }
      return getVariable(localName, Namespace.getNamespace(uri));
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(
          "Unable to resolve variable " + localName + " in namespace '" + uri + "' to a vaulue.");
    }
  }
}
