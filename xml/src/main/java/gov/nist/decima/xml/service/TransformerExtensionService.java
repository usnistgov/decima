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

import net.sf.saxon.Configuration;

import java.util.ServiceLoader;

/**
 * This extension mechanism provides for a means for registering custom XSL transformer and XPath
 * functions. This extension mechanism allows Decima-based implementations to register new XPath
 * functions in a way that these functions are automatically used by core Decima capabilities.
 */

public class TransformerExtensionService {
  private static TransformerExtensionService service;

  /**
   * Retrieves the singleton instance of the {@link TransformerExtensionService}.
   * 
   * @return the singleton instance
   */

  public static synchronized TransformerExtensionService getInstance() {
    if (service == null) {
      service = new TransformerExtensionService();
    }
    return service;
  }

  private final ServiceLoader<TransformerExtension> loader;

  private TransformerExtensionService() {
    loader = ServiceLoader.load(TransformerExtension.class);
  }

  /**
   * Provides a callback mechanism allowing each extension to make configuration modifications.
   * 
   * @param config
   *          the Saxon configuration to modify
   */
  public void registerExtensions(Configuration config) {
    for (TransformerExtension extension : loader) {
      extension.registerExtensions(config);
    }
  }
}
