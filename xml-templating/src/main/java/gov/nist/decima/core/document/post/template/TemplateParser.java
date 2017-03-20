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

package gov.nist.decima.core.document.post.template;

import gov.nist.decima.xml.document.XMLDocument;
import gov.nist.decima.xml.jdom2.JDOMUtil;
import gov.nist.decima.xml.jdom2.saxon.xpath.SaxonXPathFactory;

import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.DataConversionException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filter;
import org.jdom2.filter.Filters;
import org.jdom2.input.sax.SAXEngine;
import org.jdom2.xpath.XPathFactory;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TemplateParser {
  public static final Namespace TEMPLATE_NAMESPACE
      = Namespace.getNamespace("http://csrc.nist.gov/ns/decima/template/1.0");
  public static final String TEMPLATE_LOCAL_NAME = "template";

  private static final TemplateParser instance = new TemplateParser();

  public static TemplateParser getInstance() {
    return instance;
  }

  private final XPathFactory xpathfactory = new SaxonXPathFactory();

  private TemplateParser() {
    // prevent construction
  }

  protected XPathFactory getXpathfactory() {
    return xpathfactory;
  }

  public TemplateProcessor parse(XMLDocument template) throws TemplateParserException {
    return parse(template.getJDOMDocument().getRootElement(), template.getOriginalLocation());
  }

  /**
   * Parse an XML element as a Decima template.
   * 
   * @param templateElement
   *          the element to parse
   * @param contextSystemId
   *          the systemId of the containing document
   * @return a new template processor based on the element's contents
   * @throws TemplateParserException
   *           if an error occurred while parsing the element's contents
   */
  public TemplateProcessor parse(Element templateElement, URL contextSystemId) throws TemplateParserException {
    if (!TEMPLATE_NAMESPACE.equals(templateElement.getNamespace())
        || !TEMPLATE_LOCAL_NAME.equals(templateElement.getName())) {
      throw new TemplateParserException(
          "The provided element argument is not a template element {" + TEMPLATE_NAMESPACE + "}" + TEMPLATE_LOCAL_NAME
              + " The namespace found was: {" + templateElement.getNamespaceURI() + "}" + templateElement.getName());
    }
    return buildTemplate(templateElement, contextSystemId).build();
  }

  /**
   * Parse an XML document as a Decima template.
   * 
   * @param is
   *          the stream to read the document from
   * @param contextSystemId
   *          the systemId of the document being read
   * @return a new template processor based on the XML contents
   * @throws TemplateParserException
   *           if an error occurred while parsing the XML contents
   */
  public TemplateProcessor parse(InputStream is, URL contextSystemId) throws TemplateParserException {
    SAXEngine saxEngine;
    try {
      saxEngine = JDOMUtil.newValidatingSAXEngine(new URL("classpath:schema/decima/decima-template-1.0.xsd"));
    } catch (MalformedURLException | SAXException | JDOMException e) {
      throw new TemplateParserException(e);
    }
    Document document;
    try {
      document = saxEngine.build(is);
    } catch (IOException | JDOMException e) {
      throw new TemplateParserException("unable to load template document", e);
    }
    return parse(document.getRootElement(), contextSystemId);
  }
  //
  // public TemplateProcessor parse(TemplateDocument.Template template, Document dependency) throws
  // ParserException {
  // TemplateProcessorBuilder builder = buildTemplate(template);
  // builder.setTemplate(dependency);
  // return builder.build();
  // }

  protected TemplateProcessorBuilder buildTemplate(Element templateElement, URL contextSystemId)
      throws TemplateParserException {
    Objects.requireNonNull(templateElement);
    TemplateProcessorBuilder builder = new TemplateProcessorBuilder();
    builder.setContextSystemId(contextSystemId);
    String templatePath = templateElement.getAttributeValue("template");
    try {
      URI templateURI = URI.create(templatePath);
      // if (!templateURI.isAbsolute() && !templateURI.getPath().startsWith("./")) {
      // try {
      // templateURI = new URI(templateURI.getScheme(), templateURI.getUserInfo(),
      // templateURI.getHost(), templateURI.getPort(), "./"+templateURI.getPath(),
      // templateURI.getQuery(), templateURI.getFragment());
      // } catch (URISyntaxException e) {
      // throw new ParserException(e);
      // }
      // }
      // TODO: Why not just call contextSystemId.toURI()?
      URI base = URI.create(contextSystemId.toString());
      URL templateURL;
      // TODO: this is a hack because classpath URLs do not have a / starting with the schema
      // specific part.
      if (base.isOpaque() && base.getScheme().equals("classpath")) {
        templateURL = new URL(contextSystemId, templatePath);
      } else {
        templateURL = base.resolve(templateURI).toURL();
      }
      builder.setTemplateURL(templateURL);
    } catch (MalformedURLException e) {
      throw new TemplateParserException(e);
    }

    for (Element actionElement : templateElement.getChildren()) {
      String localName = actionElement.getName();
      switch (localName) {
      case "add-attribute":
        buildAddAction(builder, actionElement);
        break;
      case "delete":
        buildDeleteAction(builder, actionElement);
        break;
      case "insert-child":
        buildInsertChildAction(builder, actionElement);
        break;
      case "insert-sibling":
        buildInsertSiblingAction(builder, actionElement);
        break;
      case "modify-attribute":
        buildModifyAttributeAction(builder, actionElement);
        break;
      case "replace":
        buildReplaceAction(builder, actionElement);
        break;
      default:
        throw new TemplateParserException(
            "Invalid action type:  {" + actionElement.getNamespaceURI() + "}" + actionElement.getName());
      }
    }
    return builder;
  }

  private Map<String, String> getNamespacePrefixes(Element element) {
    Map<String, String> retval = new HashMap<>();
    for (Namespace ns : element.getNamespacesInScope()) {
      retval.put(ns.getPrefix(), ns.getURI());
    }
    retval.put(Namespace.XML_NAMESPACE.getPrefix(), Namespace.XML_NAMESPACE.getURI());
    return retval;
  }

  private <T extends Content> List<T> getContentNodes(Element element, Filter<T> filter)
      throws TemplateParserException {
    List<T> children = element.getContent(filter);
    if (children.isEmpty()) {
      throw new TemplateParserException("action does not contain content nodes");
    }
    List<T> retval = new ArrayList<>(children);
    for (T child : retval) {
      child.detach();
    }
    return Collections.unmodifiableList(retval);
  }

  protected void buildModifyAttributeAction(TemplateProcessorBuilder builder, Element actionElement) {
    Map<String, String> prefixToNamespaceMap = getNamespacePrefixes(actionElement);
    builder.addAction(new ModifyAttributeAction(getXpathfactory(), actionElement.getAttributeValue("xpath"),
        prefixToNamespaceMap, actionElement.getAttributeValue("value")));
  }

  protected void buildInsertSiblingAction(TemplateProcessorBuilder builder, Element actionElement)
      throws TemplateParserException {
    Map<String, String> prefixToNamespaceMap = getNamespacePrefixes(actionElement);
    builder.addAction(new InsertSiblingAction(getXpathfactory(), actionElement.getAttributeValue("xpath"),
        prefixToNamespaceMap, getContentNodes(actionElement, Filters.element()),
        Boolean.valueOf(actionElement.getAttributeValue("before"))));
  }

  protected void buildInsertChildAction(TemplateProcessorBuilder builder, Element actionElement)
      throws TemplateParserException {
    Map<String, String> prefixToNamespaceMap = getNamespacePrefixes(actionElement);
    Integer index = null;
    try {
      Attribute indexAttr = actionElement.getAttribute("index");
      if (indexAttr != null) {
        index = indexAttr.getIntValue();
      }
    } catch (DataConversionException e) {
      throw new TemplateParserException(e);
    }
    builder.addAction(new InsertChildAction(getXpathfactory(), actionElement.getAttributeValue("xpath"),
        prefixToNamespaceMap, getContentNodes(actionElement, Filters.element()), index));
  }

  protected void buildDeleteAction(TemplateProcessorBuilder builder, Element actionElement) {
    Map<String, String> prefixToNamespaceMap = getNamespacePrefixes(actionElement);
    builder
        .addAction(new DeleteAction(getXpathfactory(), actionElement.getAttributeValue("xpath"), prefixToNamespaceMap));
  }

  protected void buildAddAction(TemplateProcessorBuilder builder, Element actionElement) {
    Map<String, String> prefixToNamespaceMap = getNamespacePrefixes(actionElement);
    builder.addAction(new AddAttributeAction(getXpathfactory(), actionElement.getAttributeValue("xpath"),
        prefixToNamespaceMap, actionElement.getAttributeValue("ns"), actionElement.getAttributeValue("name"),
        actionElement.getAttributeValue("value")));
  }

  protected void buildReplaceAction(TemplateProcessorBuilder builder, Element actionElement)
      throws TemplateParserException {
    Map<String, String> prefixToNamespaceMap = getNamespacePrefixes(actionElement);
    builder.addAction(new ReplaceAction(getXpathfactory(), actionElement.getAttributeValue("xpath"),
        prefixToNamespaceMap, getContentNodes(actionElement, Filters.element())));
  }

}
