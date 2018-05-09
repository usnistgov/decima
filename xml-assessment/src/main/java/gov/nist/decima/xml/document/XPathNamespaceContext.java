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

package gov.nist.decima.xml.document;

import net.sf.saxon.om.NamespaceResolver;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

/**
 * This class acts a bridge between a JAXP {@link NamespaceContext} and a Saxon
 * {@link NamespaceResolver}.
 */
public class XPathNamespaceContext implements NamespaceContext, NamespaceResolver {
    private static final Set<String> XML_NAMESPACE_PREFIXES = Collections.singleton(XMLConstants.XML_NS_PREFIX);
    private static final Set<String> XML_NS_ATTRIBUTE_PREFIXES = Collections.singleton(XMLConstants.XMLNS_ATTRIBUTE);
    private final Map<String, String> prefixToNSMap = new HashMap<>();
    private final Map<String, Set<String>> nsToPrefixMap = new HashMap<>();

    public XPathNamespaceContext() {
        addNamespaceInternal(XMLConstants.DEFAULT_NS_PREFIX, XMLConstants.NULL_NS_URI);
    }

    public XPathNamespaceContext(String defaultNamespaceURI) {
        addNamespaceInternal(XMLConstants.DEFAULT_NS_PREFIX, defaultNamespaceURI);
    }

    /**
     * Add a prefix to namespace URI mapping.
     * 
     * @param prefix
     *            the namespace prefix
     * @param namespaceURI
     *            the namespace URI
     */
    public void addNamespace(String prefix, String namespaceURI) {
        String bound = getNamespaceURI(namespaceURI);
        if (bound != null) {
            throw new IllegalArgumentException("The namespace prefix is already bound to: " + bound);
        }
        addNamespaceInternal(prefix, namespaceURI);
    }

    private void addNamespaceInternal(String prefix, String namespaceURI) {
        prefixToNSMap.put(prefix, namespaceURI);
        Set<String> prefixes = nsToPrefixMap.get(namespaceURI);
        if (prefixes == null) {
            prefixes = new HashSet<>();
            nsToPrefixMap.put(namespaceURI, prefixes);
        }
        prefixes.add(prefix);
    }

    @Override
    public String getNamespaceURI(String prefix) {
        String retval = prefixToNSMap.get(prefix);
        if (retval == null) {
            if (XMLConstants.XML_NS_PREFIX.equals(prefix)) {
                retval = XMLConstants.XML_NS_URI;
            } else if (XMLConstants.XMLNS_ATTRIBUTE.equals(prefix)) {
                retval = XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
            }
        }
        return retval;
    }

    @Override
    public String getPrefix(String namespaceURI) {
        Iterator<String> result = getPrefixes(namespaceURI);
        String retval;
        if (!result.hasNext()) {
            retval = null;
        } else {
            retval = result.next();
        }
        return retval;
    }

    @Override
    public Iterator<String> getPrefixes(String namespaceURI) {
        Iterator<String> retval;
        if (XMLConstants.XML_NS_URI.equals(namespaceURI)) {
            retval = XML_NAMESPACE_PREFIXES.iterator();
        } else if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(namespaceURI)) {
            retval = XML_NS_ATTRIBUTE_PREFIXES.iterator();
        } else {
            Set<String> result = nsToPrefixMap.get(namespaceURI);
            if (result == null) {
                retval = Collections.emptyIterator();
            } else {
                retval = Collections.unmodifiableSet(result).iterator();
            }
        }
        return retval;
    }

    @Override
    public String getURIForPrefix(String prefix, boolean useDefault) {
        String retval;
        if ("".equals(prefix) && !useDefault) {
            retval = "";
        } else {
            retval = getNamespaceURI(prefix);
        }
        return retval;
    }

    @Override
    public Iterator<String> iteratePrefixes() {
        return Collections.unmodifiableSet(prefixToNSMap.keySet()).iterator();
    }
}
