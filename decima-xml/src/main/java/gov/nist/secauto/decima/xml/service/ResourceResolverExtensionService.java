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

package gov.nist.secauto.decima.xml.service;

import gov.nist.secauto.decima.core.classpath.ClasspathHandler;

import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.EntityResolver;
import org.xml.sax.ext.EntityResolver2;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;

import javax.xml.catalog.CatalogFeatures;
import javax.xml.catalog.CatalogFeatures.Feature;
import javax.xml.catalog.CatalogManager;
import javax.xml.catalog.CatalogResolver;

/**
 * This extension mechanism provides for a means for registering additional {@link EntityResolver2}
 * and {@link LSResourceResolver} instances. This extension mechanism allows Decima-based
 * implementations to register resolvers in a way that these resolvers are automatically used by
 * core Decima capabilities.
 */
public class ResourceResolverExtensionService {
  public static final CatalogResolver DEFAULT_CATALOG_RESOLVER
      = newCatalogResolver(URI.create("classpath:schema/decima-xml-catalog.xml"));

  public static synchronized CatalogResolver newCatalogResolver(URI uri) {
    ClasspathHandler.initialize();
    return CatalogManager.catalogResolver(CatalogFeatures.builder().with(Feature.RESOLVE, "continue").build(), uri);
  }

  private static ResourceResolverExtensionService service;

  /**
   * Retrieves the singleton instance of the {@link ResourceResolverExtensionService}.
   * 
   * @return the singleton instance
   */
  public static synchronized ResourceResolverExtensionService getInstance() {
    ClasspathHandler.initialize();
    if (service == null) {
      service = new ResourceResolverExtensionService();
    }
    return service;
  }

  private final ServiceLoader<ResourceResolverExtension> loader;
  private EntityResolver entityResolver;
  private LSResourceResolver lsResourceResolver;

  private ResourceResolverExtensionService() {
    loader = ServiceLoader.load(ResourceResolverExtension.class);
  }

  /**
   * Creates an {@link EntityResolver} by combining the Decima and any extension resolvers. The Decima
   * resolver is a {@link CatalogResolver} that resolves many of the core XML DTDs and schema.
   * 
   * @return a single or a composite of two or more {@link EntityResolver} instances
   */
  public synchronized EntityResolver getEntityResolver() {
    if (entityResolver == null) {
      List<EntityResolver> resolvers = new LinkedList<EntityResolver>();

      // Add the decima catalog first
      resolvers.add(DEFAULT_CATALOG_RESOLVER);

      for (ResourceResolverExtension extension : loader) {
        EntityResolver resolver = extension.getEntityResolver();
        if (resolver != null) {
          resolvers.add(resolver);
        }
      }

      if (resolvers.isEmpty()) {
        // will never happen
        entityResolver = null;
      } else if (resolvers.size() == 1) {
        entityResolver = resolvers.get(0);
      } else {
        entityResolver = new CompositeEntityResolver(resolvers);
      }
    }
    return entityResolver;
  }

  /**
   * Creates an {@link LSResourceResolver} by combining the Decima and any extension resolvers. The
   * Decima resolver is a {@link CatalogResolver} that resolves many of the core XML DTDs and schema.
   * 
   * @return a single or a composite of two or more {@link LSResourceResolver} instances
   */
  public LSResourceResolver getLSResolver() {
    if (lsResourceResolver == null) {
      List<LSResourceResolver> resolvers = new LinkedList<LSResourceResolver>();

      // Add the decima catalog first
      resolvers.add(DEFAULT_CATALOG_RESOLVER);

      for (ResourceResolverExtension extension : loader) {
        LSResourceResolver resolver = extension.getLSResourceResolver();
        if (resolver != null) {
          resolvers.add(resolver);
        }
      }

      if (resolvers.isEmpty()) {
        lsResourceResolver = null;
      } else if (resolvers.size() == 1) {
        lsResourceResolver = resolvers.get(0);
      } else {
        lsResourceResolver = new CompositeLSResourceResolver(resolvers);
      }
    }

    return lsResourceResolver;
  }
}
