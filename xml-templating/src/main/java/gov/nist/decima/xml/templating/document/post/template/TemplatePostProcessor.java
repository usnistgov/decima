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

package gov.nist.decima.core.document.post.template;

import gov.nist.decima.core.document.DocumentException;
import gov.nist.decima.core.document.handling.DocumentPostProcessor;
import gov.nist.decima.core.document.handling.ResourceResolver;
import gov.nist.decima.xml.document.MutableXMLDocument;
import gov.nist.decima.xml.document.XMLDocument;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;

public class TemplatePostProcessor implements DocumentPostProcessor<MutableXMLDocument> {
  public static final String TEMPLATE_NAMESPACE_URI = "http://csrc.nist.gov/ns/decima/template/1.0";
  public static final String TEMPLATE_LOCAL_NAME = "template";
  public static final String TEMPLATE_XPATH;
  public static final String TEMPLATE_ATTRIBUTE = "template";

  static {
    StringBuilder builder = new StringBuilder();
    builder.append("/*[namespace-uri()='");
    builder.append(TEMPLATE_NAMESPACE_URI);
    builder.append("' and local-name() = '");
    builder.append(TEMPLATE_LOCAL_NAME);
    builder.append("']");
    TEMPLATE_XPATH = builder.toString();
  }

  public TemplatePostProcessor() {
    super();
  }

  @Override
  public boolean handles(MutableXMLDocument subject) throws DocumentException {
    Document document = subject.getJDOMDocument(false);
    boolean retval = false;
    if (document.hasRootElement()) {
      Element root = document.getRootElement();
      if (TEMPLATE_LOCAL_NAME.equals(root.getName()) && TEMPLATE_NAMESPACE_URI.equals(root.getNamespace().getURI())) {
        retval = true;
      }
    }
    return retval;
  }

  @Override
  public MutableXMLDocument process(MutableXMLDocument subject, ResourceResolver<MutableXMLDocument> resolver)
      throws DocumentException {
    Document document = subject.getJDOMDocument(false);
    Element root = document.getRootElement();
    Attribute attribute = root.getAttribute(TEMPLATE_ATTRIBUTE);
    if (attribute == null) {
      throw new DocumentException("The document's root element doesn't have an attribute named 'template'.");
    }

    TemplateProcessor tp = newTemplateProcessor(subject);
    return tp.generate(resolver);
  }

  protected TemplateProcessor newTemplateProcessor(XMLDocument document) throws DocumentException {
    TemplateProcessor retval;
    try {
      retval = TemplateParser.getInstance().parse(document);
    } catch (TemplateParserException e) {
      throw new DocumentException("Unable to parse template", e);
    }
    return retval;
  }

}
