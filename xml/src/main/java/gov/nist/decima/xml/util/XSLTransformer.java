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

package gov.nist.decima.xml.util;

import gov.nist.decima.core.util.Log4jErrorListener;
import gov.nist.decima.core.util.URLUtil;
import gov.nist.decima.xml.service.TransformerExtensionService;

import net.sf.saxon.jaxp.SaxonTransformerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

/**
 * Provides support for XSL transformations in Decima.
 * <p>
 * For XSL transformations in Decima, the {@link ExtendedXSLTransformer} should be used. This
 * guarantees that any XSL extensions are properly loaded through the
 * {@link TransformerExtensionService} extension mechanism in the Decima framework.
 */
public class XSLTransformer implements URIResolver {
  private final SaxonTransformerFactory transformerFactory;

  public XSLTransformer() {
    this((SaxonTransformerFactory) TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null));
  }

  /**
   * Constructs an XSL transformer using the provided transformer factory.
   * 
   * @param transformerFactory
   *          the transformer factory to use
   */
  public XSLTransformer(SaxonTransformerFactory transformerFactory) {
    this.transformerFactory = transformerFactory;
    transformerFactory.setURIResolver(this);
    transformerFactory.setErrorListener(new Log4jErrorListener());
  }

  public SaxonTransformerFactory getTransformerFactory() {
    return transformerFactory;
  }

  /**
   * Creates a transformer handler in the context of a chain of transformer handlers.
   * 
   * @param template
   *          the template to use for this transformation step
   * @param nextHandler
   *          the handler to perform the next transform
   * @return the newly created transform handler
   * @throws TransformerConfigurationException
   *           if an error occurs while configuring the transformation
   */
  public TransformerHandler addChain(Templates template, TransformerHandler nextHandler)
      throws TransformerConfigurationException {
    SAXTransformerFactory stf = (SAXTransformerFactory) getTransformerFactory();

    TransformerHandler handler = stf.newTransformerHandler(template);
    if (nextHandler != null) {
      handler.setResult(new SAXResult(nextHandler));
    }
    return handler;
  }

  @Override
  public Source resolve(String href, String base) throws TransformerException {
    URL url;
    try {
      if (!base.isEmpty()) {
        URL baseURL = new URL(base);
        url = new URL(baseURL, href);
      } else {
        url = new URL(href);
      }
    } catch (MalformedURLException e) {
      throw new TransformerException(e);
    }
    try {
      return URLUtil.getSource(url);
    } catch (IOException e) {
      throw new TransformerException(e);
    }
  }
}
