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

package gov.nist.decima.xml.document;

import gov.nist.decima.core.document.Context;

import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.filter.Filter;
import org.jdom2.filter.Filters;

import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathVariableResolver;

/**
 * Implementations of this interface provide XPath evaluation capabilities over a specific XML
 * document.
 */
public interface XPathEvaluator {
  /**
   * Retrieves an object that can be used for processing namespace contexts.
   * 
   * @return the current namespace context handler in effect, or <code>null</code> if no handler is
   *         in effect.
   */
  NamespaceContext getNamespaceContext();

  /**
   * Used to set handler for prefix-to-namespace mapping and other related operations. The utility
   * class {@link XPathNamespaceContext} must be used to define namespace mappings.
   * 
   * @param nsContext
   *          the new namespace context to use that will replace the existing context handler
   * @throws NullPointerException
   *           If <code>nsContext</code> is <code>null</code>.
   */
  void setNamespaceContext(XPathNamespaceContext nsContext);

  /**
   * Retrieves the resolver used to process XPath variables.
   * 
   * @return the current variable resolver in effect, or <code>null</code> if no resolver is in
   *         effect.
   */
  XPathVariableResolver getXPathVariableResolver();

  /**
   * Set a user provided XPath variable resolver that can be used to resolve variables that appear
   * in XPath statements.
   * 
   * @param resolver
   *          the new variable resolve to use that will replace the existing resolver
   * @throws NullPointerException
   *           If <code>resolver</code> is <code>null</code>.
   */
  void setXPathVariableResolver(XPathVariableResolver resolver);

  /**
   * Evaluates an XPath expression, returning a list of results matching the provided filter. A
   * filter can be created using the {@link Filters} class.
   * 
   * @param <T>
   *          the type of object to be filtered against when building the result set
   * @param xpath
   *          the XPath expression to evaluate
   * @param filter
   *          the filter to use to limit the returned results, or <code>null</code> if no filter is
   *          to be applied
   * @return a non-null result
   * @throws XPathExpressionException
   *           if an error occurred while evaluating the XPath expression
   */
  <T> List<T> evaluate(String xpath, Filter<T> filter) throws XPathExpressionException;

  /**
   * Evaluates an XPath expression, returning a single result matching the provided filter. A filter
   * can be created using the {@link Filters} class.
   * 
   * @param <T>
   *          the type of object to be filtered against when determining the result
   * @param xpath
   *          the XPath expression to evaluate
   * @param filter
   *          the filter to use to limit the returned result, or <code>null</code> if no filter is
   *          to be applied
   * @return the matching result, or <code>null</code> if no result matched
   * @throws XPathExpressionException
   *           if an error occurred while evaluating the XPath expression
   */
  <T> T evaluateSingle(String xpath, Filter<T> filter) throws XPathExpressionException;

  /**
   * Evaluates an XPath expression, returning a boolean result indicating if a non-empty result was
   * found matching the XPath expression.
   * 
   * @param xpath
   *          the XPath expression to evaluate
   * @return <code>true</code> if at least one match to the expression was found, or
   *         <code>false</code> otherwise
   * @throws XPathExpressionException
   *           if an error occurred while evaluating the XPath expression
   */
  boolean test(String xpath) throws XPathExpressionException;

  /**
   * The method will return the {@link Context}, the location within an XML document, for a provided
   * XPath expression. The provided XPath expression must evaluate to a single {@link Content} or
   * {@link Attribute} result.
   * 
   * @param xpath
   *          the XPath expression to find the context for
   * @return the context of the evaluated XPath expression result
   * @throws XPathExpressionException
   *           if an error occurred while evaluating the XPath expression
   */
  XPathContext getContext(String xpath) throws XPathExpressionException;
}
