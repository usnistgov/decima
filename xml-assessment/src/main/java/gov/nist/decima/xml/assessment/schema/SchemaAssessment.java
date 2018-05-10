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

package gov.nist.decima.xml.assessment.schema;

import gov.nist.decima.core.assessment.AbstractAssessment;
import gov.nist.decima.core.assessment.Assessment;
import gov.nist.decima.core.assessment.AssessmentException;
import gov.nist.decima.core.assessment.result.AssessmentResultBuilder;
import gov.nist.decima.core.assessment.result.TestResult;
import gov.nist.decima.core.util.ObjectUtil;
import gov.nist.decima.xml.document.XMLDocument;
import gov.nist.decima.xml.service.ResourceResolverExtensionService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaderSchemaFactory;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;
import org.xml.sax.ext.EntityResolver2;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

public class SchemaAssessment extends AbstractAssessment<XMLDocument> {
  private static final Logger log = LogManager.getLogger(SchemaAssessment.class);

  private static final String ASSESSMENT_TYPE = "XML Schema";
  private final String derivedRequirementId;
  private final List<? extends Source> schemaSources;
  private LSResourceResolver lsResourceResolver;
  private EntityResolver2 entityResolver;
  private XMLReaderSchemaFactory xmlFactory;

  public SchemaAssessment(String derivedRequirementId) {
    this(derivedRequirementId, new LinkedList<>());
  }

  /**
   * Constructs a new XML schema-based {@link Assessment} that can be used to validate assessed
   * documents against a set of XML schema. The result of the {@link SchemaAssessment} is reported
   * against a single provided derived requirement identifier.
   * 
   * @param derivedRequirementId
   *          the identifier of the derived requirement to report {@link TestResult} instances
   *          against based on the schema validation results
   * @param schemaSources
   *          a collection of {@link Source} instances that point to schema resources
   */
  public SchemaAssessment(String derivedRequirementId, List<? extends Source> schemaSources) {
    ObjectUtil.requireNonEmpty(derivedRequirementId, "derivedRequirementId");
    Objects.requireNonNull(schemaSources, "schemaSources");

    if (log.isInfoEnabled()) {
      log.info("Creating a schema assessment for derived requirement: " + derivedRequirementId);
    }

    this.derivedRequirementId = derivedRequirementId;
    this.lsResourceResolver = ResourceResolverExtensionService.getInstance().getLSResolver();
    this.entityResolver = ResourceResolverExtensionService.getInstance().getEntityResolver();
    this.schemaSources = schemaSources;
  }

  public String getDerivedRequirementId() {
    return derivedRequirementId;
  }

  public List<? extends Source> getSchemaSources() {
    return schemaSources;
  }

  @Override
  public String getAssessmentType() {
    return ASSESSMENT_TYPE;
  }

  public LSResourceResolver getLSResourceResolver() {
    return lsResourceResolver;
  }

  /**
   * Sets the {@link LSResourceResolver} instance to use to resolve schema resources.
   * 
   * @param resourceResolver
   *          the resolver instance
   */
  public void setLSResourceResolver(LSResourceResolver resourceResolver) {
    this.lsResourceResolver = resourceResolver;
    // reset the factory instance
    this.xmlFactory = null;
  }

  public EntityResolver2 getEntityResolver() {
    return entityResolver;
  }

  public void setEntityResolver(EntityResolver2 entityResolver) {
    this.entityResolver = entityResolver;
  }

  @Override
  protected String getNameDetails() {
    StringBuilder builder = new StringBuilder();

    boolean first = true;
    for (Source source : getSchemaSources()) {
      if (first) {
        first = false;
      } else {
        builder.append(", ");
      }
      builder.append(source.getSystemId());
    }
    return builder.toString();
  }

  private synchronized XMLReaderSchemaFactory getXMLReaderSchemaFactory() throws AssessmentException {
    if (this.xmlFactory == null) {
      SchemaFactory schemafactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      LSResourceResolver lsResourceResolver = getLSResourceResolver();
      if (lsResourceResolver != null) {
        schemafactory.setResourceResolver(new ProxyResolver(lsResourceResolver));
      }
      Schema schema;
      try {
        if (!schemaSources.isEmpty()) {
          schema = schemafactory.newSchema(schemaSources.toArray(new Source[schemaSources.size()]));
        } else {
          schema = schemafactory.newSchema();
        }
      } catch (SAXException e) {
        throw new AssessmentException(e);
      }

      this.xmlFactory = new XMLReaderSchemaFactory(schema);
    }
    return this.xmlFactory;
  }

  @Override
  protected void executeInternal(XMLDocument doc, AssessmentResultBuilder builder) throws AssessmentException {
    XMLReaderSchemaFactory xmlFactory = getXMLReaderSchemaFactory();
    SAXBuilder saxBuilder = new SAXBuilder(xmlFactory);

    EntityResolver2 entityResolver = getEntityResolver();
    if (entityResolver != null) {
      saxBuilder.setEntityResolver(entityResolver);
    }

    XMLPathLocationAssessmentXMLFilter filter = new XMLPathLocationAssessmentXMLFilter();
    AssessmentSAXErrorHandler receiver
        = new AssessmentSAXErrorHandler(this, doc, getDerivedRequirementId(), builder, filter);
    saxBuilder.setErrorHandler(receiver);
    saxBuilder.setXMLFilter(filter);
    try {
      log.debug("Schema validating XML document: {}", doc.getSystemId());
      saxBuilder.build(doc.newInputStream(), doc.getSystemId());
      log.debug("[{}]XML Schema validation complete", getId());
    } catch (JDOMException | IOException e) {
      throw new AssessmentException(e);
    }
  }

  private static class ProxyResolver implements LSResourceResolver {
    private final LSResourceResolver proxy;

    public ProxyResolver(LSResourceResolver lsResourceResolver) {
      this.proxy = lsResourceResolver;
    }

    @Override
    public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
      LSInput retval = proxy.resolveResource(type, namespaceURI, publicId, systemId, baseURI);
      return retval;
    }

  }
}
