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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.filter.Filter;
import org.jdom2.xpath.XPathBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This abstract implementation provides basic XPath query support for {@link Action}
 * implementations that handle specific action operations.
 * 
 * @param <T>
 *            The generic type of the results of the XPath query after being processed by the JDOM
 *            {@code Filter<T>}
 */
public abstract class AbstractXPathAction<T> implements XPathAction<T> {
    private static final Logger logger = LogManager.getLogger(AbstractXPathAction.class);

    // TODO: replace this with the XMLDocument XPath functionality
    private final XPathExpression<T> xpath;

    /**
     * Construct a new AbstractXPathAction based on an XPath string, a JDOM {@code Filter<T>} using
     * the provided namespace map to map XML prefixes to namespaces.
     * 
     * @param xpathFactory
     *            the XPath implementation to use
     * @param xpath
     *            the XPath string
     * @param filter
     *            a filter to determine the types of nodes to return
     * @param prefixToNamespaceMap
     *            a map of XML prefixes to namespaces used in the provided XPath
     */
    public AbstractXPathAction(XPathFactory xpathFactory, String xpath, Filter<T> filter,
            Map<String, String> prefixToNamespaceMap) {
        Objects.requireNonNull(xpathFactory);
        Objects.requireNonNull(xpath);
        ObjectUtil.requireNonEmpty(xpath);
        Objects.requireNonNull(filter);
        Objects.requireNonNull(prefixToNamespaceMap);
        this.xpath = buildXPath(xpathFactory, xpath, filter, prefixToNamespaceMap);
    }

    @Override
    public XPathExpression<T> getXpath() {
        return xpath;
    }

    /**
     * Compiles the provided XPath.
     * 
     * @param xpath
     *            the XPath string
     * @param filter
     *            a filter to determine the types of nodes to return
     * @param prefixToNamespaceMap
     *            a map of XML prefixes to namespaces used in the provided XPath
     * @return a compiled JDOM XPath expression
     */
    private static <T> XPathExpression<T> buildXPath(XPathFactory xpathFactory, String xpath, Filter<T> filter,
            Map<String, String> prefixToNamespaceMap) {
        XPathBuilder<T> builder = new XPathBuilder<T>(xpath, filter);
        for (Map.Entry<String, String> entry : prefixToNamespaceMap.entrySet()) {
            if (!entry.getKey().isEmpty()) {
                builder.setNamespace(entry.getKey(), entry.getValue());
            }
        }
        return builder.compileWith(xpathFactory);
    }

    /**
     * Resolves the XPath expression against the provided document, returning the nodeset.
     * 
     * @param document
     *            the document to query against
     * @return a list of matching nodes
     * @throws ActionException
     *             if an error occurs while resolving the XPath expression
     */
    protected List<T> resolveXpath(Document document) throws ActionException {
        List<T> results;
        try {
            results = getXpath().evaluate(document);
        } catch (IllegalArgumentException e) {
            throw new InvalidXPathActionException("Invalid XPath: " + xpath, e);
        } catch (Throwable e) {
            throw new InvalidXPathActionException("An unexpected exception occured while processing XPath: " + xpath,
                    e);
        }
        if (results.isEmpty()) {
            throw new NoXPathResultsActionException("No XPath results found for XPath: " + xpath);
        }
        return results;
    }

    @Override
    public void execute(Document document) throws ActionException {
        if (logger.isTraceEnabled()) {
            logger.trace("Evaluating XPath: {}", getXpath().getExpression());
        }
        List<T> results = resolveXpath(document);
        if (logger.isTraceEnabled()) {
            logger.trace("Processing {} XPath result(s)", results.size());
        }
        process(results);
    }

    /**
     * Perform the implemented action on the provided XPath results.
     * 
     * @param results
     *            a list of XPath results guaranteed to be non-empty
     * @throws ActionException
     *             if an error occurs while processing the implemented action
     */
    protected abstract void process(List<T> results) throws ActionException;
}