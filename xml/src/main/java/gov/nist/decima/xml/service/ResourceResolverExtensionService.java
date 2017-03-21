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
package gov.nist.decima.xml.service;

import org.apache.xerces.util.XMLCatalogResolver;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ext.EntityResolver2;

import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * This extension mechanism provides for a means for registering additional {@link EntityResolver2}
 * and {@link LSResourceResolver} instances. This extension mechanism allows Decima-based
 * implementations to register resolvers in a way that these resolvers are automatically used by
 * core Decima capabilities.
 */
public class ResourceResolverExtensionService {
  private static ResourceResolverExtensionService service;
  private static final XMLCatalogResolver catalogResolver
      = new XMLCatalogResolver(new String[] { "classpath:schema/decima-catalog.xml" });

  /**
   * Retrieves the singleton instance of the {@link ResourceResolverExtensionService}.
   * 
   * @return the singleton instance
   */
  public static synchronized ResourceResolverExtensionService getInstance() {
    if (service == null) {
      service = new ResourceResolverExtensionService();
    }
    return service;
  }

  private final ServiceLoader<ResourceResolverExtension> loader;
  private EntityResolver2 entityResolver;
  private LSResourceResolver lsResourceResolver;

  private ResourceResolverExtensionService() {
    loader = ServiceLoader.load(ResourceResolverExtension.class);
  }

  /**
   * Creates an {@link EntityResolver2} by combining the Decima and any extension resolvers. The
   * Decima resolver is a {@link XMLCatalogResolver} that resolves many of the core XML DTDs and
   * schema.
   * 
   * @return a single or a composite of two or more {@link EntityResolver2} instances
   */
  public synchronized EntityResolver2 getEntityResolver() {
    if (entityResolver == null) {
      List<EntityResolver2> resolvers = new LinkedList<EntityResolver2>();

      // Add the decima catalog first
      resolvers.add(catalogResolver);

      for (ResourceResolverExtension extension : loader) {
        EntityResolver2 resolver = extension.getEntityResolver();
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
   * Decima resolver is a {@link XMLCatalogResolver} that resolves many of the core XML DTDs and
   * schema.
   * 
   * @return a single or a composite of two or more {@link LSResourceResolver} instances
   */
  public LSResourceResolver getLSResolver() {
    if (lsResourceResolver == null) {
      List<LSResourceResolver> resolvers = new LinkedList<LSResourceResolver>();

      // Add the decima catalog first
      resolvers.add(catalogResolver);

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
