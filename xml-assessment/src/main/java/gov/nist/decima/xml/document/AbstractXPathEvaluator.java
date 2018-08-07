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

import gov.nist.decima.xml.assessment.result.XPathContext;
import gov.nist.decima.xml.document.context.XMLContextResolver;

import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.filter.Filter;

import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathVariableResolver;

/**
 * This class provides convenience methods for evaluating XPath expressions using a provided
 * {@link XPathFactory}.
 *
 * @param <FACTORY>
 *          the type of the XPathFactory to use
 */
public abstract class AbstractXPathEvaluator<FACTORY extends XPathFactory> implements XPathEvaluator {

  private final FACTORY factory;
  private final XPath xpath;
  private final XMLContextResolver xmlContextResolver;

  protected AbstractXPathEvaluator(FACTORY factory, XMLContextResolver xmlContextResolver) {
    this.factory = factory;
    this.xpath = factory.newXPath();
    this.xmlContextResolver = xmlContextResolver;
  }

  protected XMLContextResolver getXMLContextResolver() {
    return xmlContextResolver;
  }

  /**
   * Resets the XPath execution environment to allow reuse.
   * 
   * @see XPath#reset()
   */
  public void reset() {
    xpath.reset();
  }

  /**
   * Retrieves the {@link NamespaceContext} from the underlying {@link XPath} instance.
   * <p>
   * {@inheritDoc}
   * 
   * @see XPath#getNamespaceContext()
   */
  @Override
  public NamespaceContext getNamespaceContext() {
    return xpath.getNamespaceContext();
  }

  /**
   * Sets the {@link NamespaceContext} in the underlying {@link XPath} instance.
   * <p>
   * {@inheritDoc}
   * 
   * @see XPath#setNamespaceContext(NamespaceContext)
   */
  @Override
  public void setNamespaceContext(XPathNamespaceContext nsContext) {
    xpath.setNamespaceContext(nsContext);
  }

  /**
   * Retrieves the {@link XPathVariableResolver} from the underlying {@link XPath} instance.
   * <p>
   * {@inheritDoc}
   * 
   * @see XPath#getXPathVariableResolver()
   */
  @Override
  public XPathVariableResolver getXPathVariableResolver() {
    return xpath.getXPathVariableResolver();
  }

  /**
   * Sets the {@link XPathVariableResolver} in the underlying {@link XPath} instance.
   * <p>
   * {@inheritDoc}
   * 
   * @see XPath#setXPathVariableResolver(XPathVariableResolver)
   */
  @Override
  public void setXPathVariableResolver(XPathVariableResolver resolver) {
    xpath.setXPathVariableResolver(resolver);
  }

  protected FACTORY getFactory() {
    return factory;
  }

  protected XPath getXPath() {
    return xpath;
  }

  /**
   * Evaluates an XPath expression. Callers of this method are expected to ensure that this method is
   * called in a thread-safe context.
   * 
   * @param xe
   *          the XPath expression to evaluate
   * @param returnType
   *          the expected object type of the return value
   * @return the XPath result
   * @throws XPathExpressionException
   *           if an error occurs while evaluating the XPath expression
   */
  protected abstract Object evaluateCompiled(XPathExpression xe, QName returnType) throws XPathExpressionException;

  /**
   * Used to compile and evaluate all XPath expressions. The returnType is a constant from
   * {@link XPathConstants}.
   * 
   * @param <T>
   *          the type of object to be returned as the evaluation result
   * @param expression
   *          the XPath expression to compile and evaluate
   * @param returnType
   *          the expected object type of the return value
   * @return the evaluation result
   * @throws XPathExpressionException
   *           If an error occurred while compiling or evaluating the XPath expression
   */
  protected synchronized <T> T evaluateInternal(String expression, QName returnType) throws XPathExpressionException {
    XPathExpression xe = getXPath().compile(expression);

    @SuppressWarnings("unchecked")
    T retval = (T) evaluateCompiled(xe, returnType);
    return retval;
  }

  @Override
  public <T> T evaluateSingle(String xpath, Filter<T> filter) throws XPathExpressionException {
    T retval = evaluateInternal(xpath, XPathConstants.NODE);
    if (filter != null) {
      retval = filter.filter(retval);
    }
    return retval;
  }

  @Override
  public <T> List<T> evaluate(String xpath, Filter<T> filter) throws XPathExpressionException {
    List<T> retval = evaluateInternal(xpath, XPathConstants.NODESET);
    if (filter != null) {
      retval = filter.filter(retval);
    }
    return retval;
  }

  @Override
  public boolean test(String xpath) throws XPathExpressionException {
    List<?> result = evaluate(xpath, null);
    return !result.isEmpty();
  }

  @Override
  public XPathContext getContext(String xpath) throws XPathExpressionException {
    Object result = evaluateSingle(xpath, null);
    XPathContext retval = null;
    if (result instanceof Content) {
      retval = getXMLContextResolver().getContext((Content) result);
    } else if (result instanceof Attribute) {
      Attribute attr = (Attribute) result;
      retval = getXMLContextResolver().getContext(attr);
    } else {
      throw new XPathExpressionException("Unknown XPath result type: " + result.getClass());
    }
    return retval;
  }
}
