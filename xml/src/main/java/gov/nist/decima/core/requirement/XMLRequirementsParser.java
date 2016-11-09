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

package gov.nist.decima.core.requirement;

import gov.nist.decima.core.jdom2.JDOMUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filter;
import org.jdom2.filter.Filters;
import org.jdom2.input.sax.SAXEngine;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

public class XMLRequirementsParser implements RequirementsParser {
  private static final Logger log = LogManager.getLogger(XMLRequirementsParser.class);

  private static final Map<String, RequirementType> typeTranslationMap;
  private static XMLRequirementsParser INSTANCE;

  static {
    typeTranslationMap = new HashMap<>();
    for (RequirementType type : RequirementType.values()) {
      typeTranslationMap.put(type.name(), type);
    }
  }

  private static RequirementType translateType(String value) {
    return typeTranslationMap.get(value);
  }

  /**
   * Retrieve a sington instance of a basic {@link XMLRequirementsParser} that supports no
   * extensions. This singleton instance is constructed by calling
   * {@link XMLRequirementsParser#XMLRequirementsParser()}. If a parser is needed that supports
   * extension elements and attributes, then an new parser instance must be constructed using the
   * constructor {@link XMLRequirementsParser#XMLRequirementsParser(List)}.
   * 
   * @return a singleton instance of the {@link XMLRequirementsParser}
   * @see XMLRequirementsParser#XMLRequirementsParser()
   */
  public static synchronized RequirementsParser instance() {
    if (INSTANCE == null) {
      try {
        INSTANCE = new XMLRequirementsParser();
      } catch (MalformedURLException | JDOMException | SAXException ex) {
        throw new RuntimeException(ex);
      }
    }
    return INSTANCE;
  }

  private final SAXEngine saxEngine;

  public XMLRequirementsParser() throws JDOMException, MalformedURLException, SAXException {
    this(Collections.emptyList());
  }

  /**
   * Constructs a requirements parser with a list of extension schema that describe possible
   * requirements definition extensions.
   * 
   * @param extensionSchemaSources
   *          extension schema that describe requirements definition extensions
   * @throws JDOMException
   *           if an error occurred while constructing the JDOM2 SAX parser
   * @throws MalformedURLException
   *           if one of the schema sources was a malformed URL
   * @throws SAXException
   *           if an error occurred while parsing the schema
   */
  public XMLRequirementsParser(List<Source> extensionSchemaSources)
      throws JDOMException, MalformedURLException, SAXException {
    Objects.requireNonNull(extensionSchemaSources, "extensionSchemaSources");
    // Sources will always have at least one member
    Source[] sources = new Source[extensionSchemaSources.size() + 1];
    sources[0] = new StreamSource("classpath:schema/decima/decima-requirements.xsd");
    int index = 1;
    for (Source source : extensionSchemaSources) {
      sources[index++] = source;
    }
    this.saxEngine = initSAXEngine(sources);
  }

  protected SAXEngine initSAXEngine(Source[] schemaSources) throws MalformedURLException, SAXException, JDOMException {
    return JDOMUtil.newValidatingSAXEngine(schemaSources);
  }

  private SAXEngine getSaxEngine() {
    return saxEngine;
  }

  /**
   * Parse requirements from a file source.
   * 
   * @param file
   *          the file containing the requirements
   * @param appender
   *          the {@link RequirementAppender} used to add the parsed requirements to an associated
   *          {@link RequirementsManager}
   * @throws RequirementsParserException
   *           if an error occurred while parsing the requirements
   */
  public void parse(File file, RequirementAppender appender) throws RequirementsParserException {
    log.info("Loading requirements from: " + file);
    Document document;
    try {
      document = getSaxEngine().build(file);
    } catch (IOException | JDOMException e) {
      throw new RequirementsParserException("unable to load requirements", e);
    }
    URI base = file.toURI();

    parseRequirements(document, base, appender);
  }

  /**
   * Parse requirements from a URL source.
   * 
   * @param url
   *          the URL pointing to the resource containing the requirements
   * @param appender
   *          the {@link RequirementAppender} used to add the parsed requirements to an associated
   *          {@link RequirementsManager}
   * @throws RequirementsParserException
   *           if an error occurred while parsing the requirements
   * @throws URISyntaxException
   *           if the provided URL is not a valid URI
   */
  public void parse(URL url, RequirementAppender appender) throws RequirementsParserException, URISyntaxException {
    log.info("Loading requirements from: " + url);
    Document document;
    try {
      document = getSaxEngine().build(url);
    } catch (IOException | JDOMException e) {
      throw new RequirementsParserException("unable to load requirements", e);
    }
    parseRequirements(document, url.toURI(), appender);
  }

  protected void parseRequirements(Document document, URI base, RequirementAppender appender) {
    appender.addRequirementDefinition(base);
    Element requirements = document.getRootElement();

    Map<String, Specification> resources
        = parseResources(requirements.getChildren("resource", requirements.getNamespace()), base);

    int requirementCount = 0;
    for (Element requirement : requirements
        .getDescendants(Filters.element("requirement", requirements.getNamespace()))) {
      appender.addBaseRequirement(parseRequirement(requirement, resources, base));

      ++requirementCount;
    }
    if (log.isDebugEnabled()) {
      if (requirementCount == 1) {
        log.debug("  1 requirement loaded");
      } else {
        log.debug("  {} requirements loaded", requirementCount);
      }
    }
  }

  protected Map<String, Specification> parseResources(List<Element> resources, URI base) {
    Map<String, Specification> retval = new HashMap<>();
    for (Element resource : resources) {
      String id = resource.getAttributeValue("id");
      URI href = base.resolve(resource.getAttributeValue("href"));
      retval.put(id, new DefaultSpecification(id, href));
    }
    return Collections.unmodifiableMap(retval);
  }

  protected BaseRequirement parseRequirement(Element requirement, Map<String, Specification> resources, URI base) {
    Element referenceElement = requirement.getChild("reference", requirement.getNamespace());
    Specification specification = resources.get(referenceElement.getAttributeValue("ref"));
    SpecificationReference reference = new DefaultSpecificationReference(specification,
        referenceElement.getAttributeValue("section"), referenceElement.getAttributeValue("section-fragment"),
        referenceElement.getAttributeValue("requirement-fragment"));

    DefaultBaseRequirement baseRequirement = new DefaultBaseRequirement(requirement.getAttributeValue("id"),
        requirement.getChildText("statement", requirement.getNamespace()), reference);

    // Get metadata tags from the base requirement
    parseMetadata(requirement, baseRequirement);

    Element derivedRequirements = requirement.getChild("derived-requirements", requirement.getNamespace());
    if (derivedRequirements != null) {
      for (Element element : derivedRequirements
          .getDescendants(Filters.element("derived-requirement", requirement.getNamespace()))) {
        DerivedRequirement derivedRequirement = new DefaultDerivedRequirement(baseRequirement,
            element.getAttributeValue("id"), element.getChildText("statement", element.getNamespace()),
            translateType(element.getAttributeValue("type")),
            Boolean.parseBoolean(element.getAttributeValue("conditional", "false")),
            element.getChildText("message", element.getNamespace()));

        // Get metadata tags from the derived requirement
        parseMetadata(requirement, baseRequirement);

        baseRequirement.addDerivedRequirement(derivedRequirement);
      }
    }
    return baseRequirement;
  }

  protected void parseMetadata(Element element, AbstractRequirement requirement) {
    @SuppressWarnings("unchecked")
    Filter<Attribute> filter = (Filter<Attribute>) Filters.attribute(element.getNamespace()).negate();
    for (Attribute attr : element.getAttributes()) {
      if (filter.matches(attr)) {
        String ns = attr.getNamespaceURI();
        StringBuilder builder = new StringBuilder();
        if (!ns.isEmpty()) {
          builder.append('{');
          builder.append(ns);
          builder.append('}');
        }
        builder.append(attr.getName());
        requirement.addMetadata(builder.toString(), attr.getValue());
      }
    }
  }
}
