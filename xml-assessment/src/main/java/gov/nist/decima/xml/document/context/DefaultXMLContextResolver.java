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

import gov.nist.decima.xml.assessment.result.XPathContext;
import gov.nist.decima.xml.document.SimpleXPathContext;

import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.located.Located;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DefaultXMLContextResolver implements XMLContextResolver {
    private final String baseXPath;
    private final Element baseElement;
    private final boolean skipBase;
    private final Map<Content, String> contentToXPathMap = new HashMap<>();

    public DefaultXMLContextResolver(Document document) {
        this("", document.getRootElement(), false);
    }

    public DefaultXMLContextResolver(String baseXPath, Element baseElement) {
        this(baseXPath, baseElement, true);
    }

    /**
     * Constructs a new {@link XMLContextResolver} that resolves resolves {@link XPathContext}
     * instances and related information based on a paths originating from a provided base XPath
     * that points to the provided element.
     * 
     * @param baseXPath
     *            an XPath expression that covers the path from the actual root of the
     *            {@link Document} to the provided root {@link Element}
     * @param baseElement
     *            the JDOM2 {@link Element} to use as the base for resolution
     * @param skipBase
     *            if {@code true} skip creating a path segment for the base element
     */
    public DefaultXMLContextResolver(String baseXPath, Element baseElement, boolean skipBase) {
        Objects.requireNonNull(baseElement, "baseElement");
        if (baseXPath == null) {
            this.baseXPath = "";
        } else {
            this.baseXPath = baseXPath;
        }
        this.baseElement = baseElement;
        this.skipBase = skipBase;
    }

    /**
     * Retrieves the {@link Element} that is the root of the resolution tree.
     * 
     * @return the base element
     */
    public Element getBaseElement() {
        return baseElement;
    }

    /**
     * Retrieves an XPath expression that can be used to query the root of the resolution tree.
     * 
     * @return an XPath string
     */
    public String getBaseXPath() {
        return baseXPath;
    }

    /**
     * If the base element should be skipped while generating an XPath.
     * 
     * @return the skipBase
     */
    public boolean isSkipBase() {
        return skipBase;
    }

    @Override
    public XPathContext getContext(Content content) {
        String xpath = getXPath(content);

        return newXPathContext(xpath, getLocated(content), getSystemId(content));
    }

    @Override
    public XPathContext getContext(Attribute attribute) {
        String xpath = getXPath(attribute);
        Element parent = attribute.getParent();
        return newXPathContext(xpath, getLocated(parent), getSystemId(parent));
    }

    protected XPathContext newXPathContext(String xpath, Located located, String systemId) {
        return new SimpleXPathContext(xpath, systemId, located.getLine(), located.getColumn());
    }

    @Override
    public String getXPath(Content content) {
        String retval = contentToXPathMap.get(content);
        if (retval == null) {
            StringBuilder xpath = new StringBuilder(getBaseXPath());
            switch (content.getCType()) {
            case CDATA:
            case Text:
                buildXPathText(xpath, content);
                break;
            case Element:
                buildXPathElement(xpath, (Element) content);
                break;
            default:
                throw new UnsupportedOperationException(
                        "Cannot build an XPath for content types: " + content.getCType());
            }
            retval = xpath.toString();

            if (retval.isEmpty()) {
                retval = "/";
            }
            contentToXPathMap.put(content, retval);
        }
        return retval;
    }

    @Override
    public String getXPath(Attribute attribute) {
        StringBuilder xpath = new StringBuilder();
        buildXPathAttribute(xpath, attribute);
        return xpath.toString();
    }

    protected void buildXPathAttribute(StringBuilder xpath, Attribute attribute) {
        xpath.append(getXPath(attribute.getParent()));

        xpath.append("/@*[local-name()='");
        xpath.append(attribute.getName());
        xpath.append("'");

        // Append the namespace, if prefixed
        String prefix = attribute.getNamespacePrefix();
        if (!"".equals(prefix)) {
            xpath.append(" and namespace-uri()='");
            xpath.append(attribute.getNamespaceURI());
            xpath.append("'");
        }
        xpath.append("]");
    }

    protected void buildXPathText(StringBuilder xpath, Content content) {
        buildXPathElement(xpath, getParentElement(content));

        xpath.append("/text()");
    }

    protected void buildXPathElement(StringBuilder xpath, Element element) {
        Element parent;
        if (element == getBaseElement() && isSkipBase()) {
            // skip the base
            return;
        }

        parent = getParentElement(element);
        if (parent != null) {
            buildXPathElement(xpath, parent);
        }

        String localname = element.getName();
        Namespace ns = element.getNamespace();
        xpath.append("/*[local-name()='");
        xpath.append(localname);
        xpath.append("' and namespace-uri()='");
        xpath.append(ns.getURI());
        xpath.append("']");

        int index;
        if (parent == null) {
            index = 1;
        } else {
            List<Element> children = parent.getChildren(localname, ns);
            index = children.indexOf(element) + 1;
        }

        xpath.append('[');
        xpath.append(index);
        xpath.append(']');
    }

    protected Element getParentElement(Content content) {
        return content.getParentElement();
    }

    protected Located getLocated(Content content) {
        Located retval;
        if (content instanceof Located) {
            retval = (Located) content;
        } else {
            // look up in the tree to find the approximate location
            Element parent = getParentElement(content);
            if (parent != null) {
                retval = getLocated(parent);
            } else {
                retval = UnknownLocated.INSTANCE;
            }
        }
        return retval;
    }

    @Override
    public String getSystemId(Content element) {
        return element.getDocument().getBaseURI();
    }
}
