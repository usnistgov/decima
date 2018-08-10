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

package gov.nist.decima.core.classpath;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class ClasspathHandler extends URLStreamHandler {
  /**
   * Provides a mechanism to manually configure the classpath URL scheme handler.
   */
  public static void initialize() {
    System.setProperty("java.protocol.handler.pkgs", "sun.net.www.protocol");
  }

  private static final Logger log = LogManager.getLogger(ClasspathHandler.class);
  private final ClassLoader classLoader;

  public ClasspathHandler() {
    this.classLoader = null;
  }

  public ClasspathHandler(ClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  @Override
  protected URLConnection openConnection(URL url) throws IOException {
    String path = url.getPath();
    final URL resourceUrl = getClassLoader().getResource(path);
    if (resourceUrl == null) {
      throw new IOException("Unable to resolve classpath resource: " + path);
    } else if (log.isTraceEnabled()) {
      log.trace("resolved URL '"+url+"' to: "+resourceUrl);
    }
    return resourceUrl.openConnection();
  }

  private ClassLoader getClassLoader() {
    ClassLoader retval = this.classLoader;
    if (retval == null) {
      // retval = getClass().getClassLoader();
      // retval = ClassLoader.getSystemClassLoader();
      retval = Thread.currentThread().getContextClassLoader();
    }
    return retval;
  }
}
