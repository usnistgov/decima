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

import net.sf.saxon.option.jdom2.JDOM2DocumentWrapper;
import net.sf.saxon.xpath.XPathFactoryImpl;

import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Namespace;
import org.jdom2.filter.Filter;

import java.util.List;
import java.util.Map;

public class SaxonCompiledXPath<T> extends CompiledXPath<T, XPathFactoryImpl> {

  public SaxonCompiledXPath(XPathFactoryImpl xpathFactory, String query, Filter<T> filter,
      Map<String, Object> variables, Namespace[] namespaces) {
    super(xpathFactory, query, filter, variables, namespaces);
  }

  private Object wrap(Object node) {
    Object retval;
    if (node instanceof Document) {
      retval = new JDOM2DocumentWrapper((Document) node, getXPathFactory().getConfiguration());
    } else if (node instanceof Content) {
      Content content = (Content) node;
      JDOM2DocumentWrapper docWrapper
          = new JDOM2DocumentWrapper(content.getDocument(), getXPathFactory().getConfiguration());
      retval = docWrapper.wrap(content);
    } else {
      throw new IllegalArgumentException("Unrecognized node type: " + node.getClass());
    }
    return retval;
  }

  @Override
  protected List<?> evaluateRawAll(Object context) {
    Object wrappedNode = wrap(context);
    return super.evaluateRawAll(wrappedNode);
  }

  @Override
  protected Object evaluateRawFirst(Object context) {
    Object wrappedNode = wrap(context);
    return super.evaluateRawFirst(wrappedNode);
  }

}
